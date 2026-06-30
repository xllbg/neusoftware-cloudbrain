package com.neusoft.cloudbrain.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(15));
        factory.setReadTimeout(Duration.ofSeconds(30));

        // 代理配置：优先读取环境变量 PROXY_HOST / PROXY_PORT
        String proxyHost = System.getenv("PROXY_HOST");
        String proxyPortStr = System.getenv("PROXY_PORT");
        if (proxyHost != null && !proxyHost.isBlank() && proxyPortStr != null && !proxyPortStr.isBlank()) {
            try {
                int proxyPort = Integer.parseInt(proxyPortStr.trim());
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost.trim(), proxyPort));
                factory.setProxy(proxy);
            } catch (NumberFormatException e) {
                // 忽略无效端口
            }
        }

        return builder
                .requestFactory(() -> factory)
                .build();
    }
}
