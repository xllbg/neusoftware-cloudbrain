package com.neusoft.cloudbrain.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DecryptFilter {

    private final RsaService rsaService;

    private static final Set<String> ENCRYPTED_PATHS = Set.of(
            "/api/patient/login",
            "/api/patient/login/phone",
            "/api/patient/register",
            "/api/doctor/login",
            "/api/doctor/login/phone",
            "/api/doctor/register"
    );

    @Bean
    public FilterRegistrationBean<Filter> decryptBodyFilter() {
        FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                HttpServletRequest httpReq = (HttpServletRequest) request;
                String path = httpReq.getServletPath();
                String encrypted = httpReq.getHeader("X-Encrypted");

                if (!"true".equals(encrypted) || !ENCRYPTED_PATHS.contains(path)) {
                    chain.doFilter(request, response);
                    return;
                }

                byte[] rawBody = StreamUtils.copyToByteArray(httpReq.getInputStream());
                String encryptedBody = new String(rawBody, StandardCharsets.UTF_8);

                try {
                    String decryptedJson = rsaService.decrypt(encryptedBody.trim());
                    log.debug("已解密请求体: {} -> {}", path, decryptedJson);

                    ByteArrayInputStream decryptedStream = new ByteArrayInputStream(
                            decryptedJson.getBytes(StandardCharsets.UTF_8));

                    HttpServletRequest wrapped = new HttpServletRequestWrapper(httpReq) {
                        @Override
                        public String getContentType() {
                            return "application/json";
                        }

                        @Override
                        public int getContentLength() {
                            return decryptedJson.getBytes(StandardCharsets.UTF_8).length;
                        }

                        @Override
                        public long getContentLengthLong() {
                            return decryptedJson.getBytes(StandardCharsets.UTF_8).length;
                        }

                        @Override
                        public String getHeader(String name) {
                            if ("Content-Type".equalsIgnoreCase(name)) {
                                return "application/json";
                            }
                            return super.getHeader(name);
                        }

                        @Override
                        public java.util.Enumeration<String> getHeaders(String name) {
                            if ("Content-Type".equalsIgnoreCase(name)) {
                                return java.util.Collections.enumeration(java.util.List.of("application/json"));
                            }
                            return super.getHeaders(name);
                        }

                        @Override
                        public ServletInputStream getInputStream() {
                            return new ServletInputStream() {
                                private final ByteArrayInputStream stream = decryptedStream;

                                @Override
                                public int read() {
                                    return stream.read();
                                }

                                @Override
                                public boolean isFinished() {
                                    return stream.available() == 0;
                                }

                                @Override
                                public boolean isReady() {
                                    return true;
                                }

                                @Override
                                public void setReadListener(ReadListener listener) {
                                }
                            };
                        }

                        @Override
                        public BufferedReader getReader() {
                            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
                        }
                    };

                    chain.doFilter(wrapped, response);
                } catch (Exception e) {
                    log.error("RSA 解密请求体失败: {}", path, e);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":400,\"message\":\"请求体解密失败\",\"data\":null}");
                }
            }
        });
        bean.addUrlPatterns("/api/*");
        bean.setOrder(1);
        return bean;
    }
}
