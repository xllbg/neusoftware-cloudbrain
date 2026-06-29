package com.neusoft.cloudbrain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
public class CloudBrainApplication {

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(CloudBrainApplication.class, args);
    }

    /**
     * 在 Spring 启动前加载 .env 到系统属性，确保 @Value 能解析 ${VAR_NAME}
     */
    private static void loadDotEnv() {
        Path envFile = findEnv();
        if (envFile == null) return;

        try {
            List<String> lines = Files.readAllLines(envFile);
            for (String raw : lines) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                if ((value.startsWith("\"") && value.endsWith("\"")) ||
                    (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                System.setProperty(key, value);
            }
        } catch (IOException ignored) {
            // 加载失败，${VAR:default} 会使用默认值
        }
    }

    private static Path findEnv() {
        Path cwd = Paths.get("").toAbsolutePath();
        Path env = cwd.resolve(".env");
        if (Files.exists(env)) return env;

        Path parent = cwd.getParent();
        if (parent != null) {
            env = parent.resolve(".env");
            if (Files.exists(env)) return env;
        }
        return null;
    }
}
