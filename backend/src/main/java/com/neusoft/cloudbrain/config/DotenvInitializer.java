package com.neusoft.cloudbrain.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 加载项目根目录 .env 文件到 Spring Environment
 */
public class DotenvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private static final String ENV_FILE_NAME = ".env";

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        Path envPath = findEnvFile();
        if (envPath == null || !Files.exists(envPath)) {
            return;
        }
        try {
            Map<String, Object> envMap = parseEnvFile(envPath);
            MutablePropertySources sources = ctx.getEnvironment().getPropertySources();
            sources.addFirst(new MapPropertySource("dotenv", envMap));
        } catch (IOException e) {
            // 加载失败不影响启动，${VAR:default} 会使用默认值
        }
    }

    private Path findEnvFile() {
        // 1. 尝试工作目录
        Path cwd = Paths.get("").toAbsolutePath();
        Path env = cwd.resolve(ENV_FILE_NAME);
        if (Files.exists(env)) return env;

        // 2. 尝试上级目录 (mvn spring-boot:run 在 backend/ 内执行时)
        Path parent = cwd.getParent();
        if (parent != null) {
            env = parent.resolve(ENV_FILE_NAME);
            if (Files.exists(env)) return env;
        }

        // 3. 从 classpath / user.home 回退
        Path home = Paths.get(System.getProperty("user.home"), ENV_FILE_NAME);
        if (Files.exists(home)) return home;

        return null;
    }

    private Map<String, Object> parseEnvFile(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        Map<String, Object> result = new HashMap<>();
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            int eq = line.indexOf('=');
            if (eq <= 0) continue;

            String key = line.substring(0, eq).trim();
            String value = line.substring(eq + 1).trim();

            if (value.startsWith("\"") && value.endsWith("\"") || value.startsWith("'") && value.endsWith("'")) {
                value = value.substring(1, value.length() - 1);
            }

            result.put(key, value);
        }
        return result;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
