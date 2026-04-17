import fs from "fs";
import path from "path";
import { spawn, spawnSync } from "child_process";

const rootDir = process.cwd();
const backendDir = path.join(rootDir, "backend");
const frontendDir = path.join(rootDir, "frontend");

const localBackendApi = process.env.EXPO_PUBLIC_BACKEND_API || "http://localhost:8080/api/v1";
const localMavenCmd = path.join(rootDir, ".tools", "apache-maven-3.9.9", "bin", "mvn.cmd");

function runSync(command, args, options = {}) {
  return spawnSync(command, args, {
    shell: process.platform === "win32",
    encoding: "utf8",
    stdio: "pipe",
    ...options,
  });
}

function logStep(message) {
  process.stdout.write(`\n[dev:local] ${message}\n`);
}

function detectJava21Home() {
  const check = runSync("java", ["-version"]);
  const output = `${check.stdout || ""}${check.stderr || ""}`;
  if (check.status === 0 && output.includes('version "21')) {
    if (process.platform === "win32") {
      const whereJava = runSync("where", ["java"]);
      if (whereJava.status === 0) {
        const javaPath = (whereJava.stdout || "").split(/\r?\n/).find(Boolean);
        if (javaPath) {
          return path.dirname(path.dirname(javaPath.trim()));
        }
      }
    }
    return process.env.JAVA_HOME || null;
  }

  const candidates = [
    "C:\\Program Files\\Eclipse Adoptium",
    "C:\\Program Files\\Microsoft",
    "C:\\Program Files\\Java",
  ];

  for (const base of candidates) {
    if (!fs.existsSync(base)) continue;
    const entries = fs
      .readdirSync(base, { withFileTypes: true })
      .filter((entry) => entry.isDirectory() && /jdk-?21/i.test(entry.name))
      .map((entry) => path.join(base, entry.name));
    if (entries.length > 0) {
      return entries[0];
    }
  }
  return null;
}

function ensureMaven() {
  const mvnCheck = runSync("mvn", ["-v"]);
  if (mvnCheck.status === 0) {
    return "mvn";
  }

  if (fs.existsSync(localMavenCmd)) {
    return localMavenCmd;
  }

  logStep("Maven not found globally. Downloading workspace-local Maven 3.9.9...");
  const powershellScript = [
    "$ErrorActionPreference='Stop';",
    "New-Item -ItemType Directory -Force -Path '.tools' | Out-Null;",
    "$zipPath = '.tools\\apache-maven-3.9.9-bin.zip';",
    "if (!(Test-Path $zipPath)) {",
    "  Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip' -OutFile $zipPath",
    "}",
    "if (!(Test-Path '.tools\\apache-maven-3.9.9')) {",
    "  Expand-Archive -Path $zipPath -DestinationPath '.tools' -Force",
    "}",
  ].join(" ");

  const download = spawnSync("powershell", ["-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", powershellScript], {
    cwd: rootDir,
    stdio: "inherit",
  });

  if (download.status !== 0 || !fs.existsSync(localMavenCmd)) {
    throw new Error("Failed to prepare local Maven.");
  }

  return localMavenCmd;
}

function terminateChildTree(child) {
  if (!child || child.killed) return;
  if (process.platform === "win32") {
    spawnSync("taskkill", ["/pid", String(child.pid), "/t", "/f"], {
      stdio: "ignore",
      shell: true,
    });
  } else {
    child.kill("SIGTERM");
  }
}

async function main() {
  if (!fs.existsSync(backendDir) || !fs.existsSync(frontendDir)) {
    throw new Error("Run this script from the repository root (expected ./backend and ./frontend).");
  }

  const javaHome21 = detectJava21Home();
  if (!javaHome21) {
    throw new Error("Java 21 is required. Install JDK 21 or set JAVA_HOME to a JDK 21 path.");
  }

  const mvnCommand = ensureMaven();
  const backendEnv = {
    ...process.env,
    JAVA_HOME: javaHome21,
    PATH: `${path.join(javaHome21, "bin")};${process.env.PATH || ""}`,
    SPRING_PROFILES_ACTIVE: "local",
    APP_SEED_ENABLED: "true",
    AUTH_STORE_FALLBACK_ENABLED: "true",
  };

  const frontendEnv = {
    ...process.env,
    EXPO_PUBLIC_BACKEND_API: localBackendApi,
  };

  logStep("Starting backend on http://localhost:8080/api/v1 ...");
  const backendProcess = spawn(mvnCommand, ["spring-boot:run"], {
    cwd: backendDir,
    env: backendEnv,
    stdio: "inherit",
    shell: process.platform === "win32",
  });

  logStep("Starting frontend web on Expo...");
  const frontendProcess = spawn("npm", ["run", "web"], {
    cwd: frontendDir,
    env: {
      ...frontendEnv,
      CI: "1",
    },
    stdio: "inherit",
    shell: true,
  });

  const shutdown = () => {
    logStep("Stopping local processes...");
    terminateChildTree(frontendProcess);
    terminateChildTree(backendProcess);
  };

  process.on("SIGINT", shutdown);
  process.on("SIGTERM", shutdown);
  process.on("exit", shutdown);

  frontendProcess.on("exit", (code) => {
    logStep(`Frontend exited with code ${code ?? 0}.`);
    shutdown();
    process.exit(code ?? 0);
  });

  backendProcess.on("exit", (code) => {
    logStep(`Backend exited with code ${code ?? 0}.`);
    shutdown();
    process.exit(code ?? 0);
  });
}

main().catch((error) => {
  console.error(`[dev:local] ${error.message}`);
  process.exit(1);
});
