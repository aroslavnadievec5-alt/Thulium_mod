package com.thulium.util;

import java.util.Locale;

public class ProcessPriorityManager {

    public static void applyIfEnabled(boolean enabled) {
        if (!enabled) {
            System.out.println("[Thulium] Process priority boost is disabled in config.");
            return;
        }

        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        long pid = ProcessHandle.current().pid();

        if (os.contains("win")) {
            applyWindowsPriority(pid);
        } else if (os.contains("linux")) {
            applyLinuxPriority(pid);
        } else {
            System.out.println("[Thulium] Process priority boost is skipped on OS: " + os);
        }
    }

    private static void applyWindowsPriority(long pid) {
        String command = "Get-Process -Id " + pid + " -ErrorAction SilentlyContinue | ForEach-Object { $_.PriorityClass = 'AboveNormal' }";
        try {
            Process process = new ProcessBuilder("powershell.exe", "-NoProfile", "-Command", command)
                    .redirectErrorStream(true)
                    .start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("[Thulium] Set AboveNormal process priority for PID " + pid);
            } else {
                System.err.println("[Thulium] Failed to set process priority on Windows (exit code: " + exitCode + ")");
            }
        } catch (Exception e) {
            System.err.println("[Thulium] Error while setting process priority on Windows: " + e.getMessage());
        }
    }

    private static void applyLinuxPriority(long pid) {
        try {
            Process process = new ProcessBuilder("renice", "-n", "-2", "-p", String.valueOf(pid))
                    .redirectErrorStream(true)
                    .start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("[Thulium] Updated nice priority for PID " + pid);
            } else {
                System.out.println("[Thulium] Linux denied nice priority change for PID " + pid + " (expected without elevated permissions).");
            }
        } catch (Exception e) {
            System.out.println("[Thulium] Skipped nice priority change on Linux: " + e.getMessage());
        }
    }
}