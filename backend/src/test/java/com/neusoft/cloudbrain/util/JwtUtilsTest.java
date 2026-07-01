package com.neusoft.cloudbrain.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtils 单元测试")
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    // 测试用密钥（至少32字节用于HS256）
    private static final String TEST_SECRET = "neusoft-cloudbrain-secret-key-for-testing-purposes-12345";
    private static final Long TEST_EXPIRATION = 3600000L; // 1小时

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "expiration", TEST_EXPIRATION);
    }

    @Nested
    @DisplayName("生成Token测试")
    class GenerateTokenTests {

        @Test
        @DisplayName("生成Token成功")
        void generateToken_success() {
            String token = jwtUtils.generateToken(1L, "testuser", "DOCTOR");

            assertNotNull(token);
            assertFalse(token.isEmpty());
            assertTrue(token.split("\\.").length == 3); // JWT格式: header.payload.signature
        }

        @Test
        @DisplayName("生成不同用户的Token不相同")
        void generateToken_differentUsers_differentTokens() {
            String token1 = jwtUtils.generateToken(1L, "user1", "DOCTOR");
            String token2 = jwtUtils.generateToken(2L, "user2", "PATIENT");

            assertNotEquals(token1, token2);
        }
    }

    @Nested
    @DisplayName("解析Token测试")
    class ParseTokenTests {

        @Test
        @DisplayName("解析有效Token成功")
        void parseToken_success() {
            String token = jwtUtils.generateToken(1L, "testuser", "DOCTOR");

            Claims claims = jwtUtils.parseToken(token);

            assertNotNull(claims);
            assertEquals("testuser", claims.getSubject());
            assertEquals(1L, claims.get("userId", Long.class));
            assertEquals("DOCTOR", claims.get("role", String.class));
        }

        @Test
        @DisplayName("解析无效Token抛出异常")
        void parseToken_invalidToken_throwsException() {
            assertThrows(Exception.class, () -> jwtUtils.parseToken("invalid.token.here"));
        }

        @Test
        @DisplayName("解析被篡改的Token抛出异常")
        void parseToken_tamperedToken_throwsException() {
            String token = jwtUtils.generateToken(1L, "testuser", "DOCTOR");
            String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";

            assertThrows(Exception.class, () -> jwtUtils.parseToken(tamperedToken));
        }
    }

    @Nested
    @DisplayName("Token过期测试")
    class IsTokenExpiredTests {

        @Test
        @DisplayName("新生成的Token未过期")
        void isTokenExpired_newToken_false() {
            String token = jwtUtils.generateToken(1L, "testuser", "DOCTOR");

            boolean expired = jwtUtils.isTokenExpired(token);

            assertFalse(expired);
        }

        @Test
        @DisplayName("过期时间已到的Token判定为过期")
        void isTokenExpired_expiredToken_true() {
            // 创建一个过期的JwtUtils实例
            JwtUtils expiredJwtUtils = new JwtUtils();
            ReflectionTestUtils.setField(expiredJwtUtils, "secret", TEST_SECRET);
            ReflectionTestUtils.setField(expiredJwtUtils, "expiration", -1000L); // 负数表示已过期

            String token = expiredJwtUtils.generateToken(1L, "testuser", "DOCTOR");

            boolean expired = jwtUtils.isTokenExpired(token);

            assertTrue(expired);
        }

        @Test
        @DisplayName("无效Token判定为过期")
        void isTokenExpired_invalidToken_true() {
            boolean expired = jwtUtils.isTokenExpired("invalid.token");

            assertTrue(expired);
        }
    }

    @Nested
    @DisplayName("从Token提取用户信息测试")
    class GetUserInfoTests {

        @Test
        @DisplayName("提取用户ID成功")
        void getUserIdFromToken_success() {
            String token = jwtUtils.generateToken(123L, "testuser", "DOCTOR");

            Long userId = jwtUtils.getUserIdFromToken(token);

            assertEquals(123L, userId);
        }

        @Test
        @DisplayName("提取用户名成功")
        void getUsernameFromToken_success() {
            String token = jwtUtils.generateToken(1L, "doctorZhang", "DOCTOR");

            String username = jwtUtils.getUsernameFromToken(token);

            assertEquals("doctorZhang", username);
        }

        @Test
        @DisplayName("提取角色成功")
        void getRoleFromToken_success() {
            String token = jwtUtils.generateToken(1L, "testuser", "PATIENT");

            String role = jwtUtils.getRoleFromToken(token);

            assertEquals("PATIENT", role);
        }

        @Test
        @DisplayName("不同角色生成不同Token")
        void getRoleFromToken_differentRoles_differentTokens() {
            String doctorToken = jwtUtils.generateToken(1L, "doctor", "DOCTOR");
            String patientToken = jwtUtils.generateToken(1L, "patient", "PATIENT");

            assertEquals("DOCTOR", jwtUtils.getRoleFromToken(doctorToken));
            assertEquals("PATIENT", jwtUtils.getRoleFromToken(patientToken));
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("空Token抛出异常")
        void parseToken_emptyToken_throwsException() {
            assertThrows(Exception.class, () -> jwtUtils.parseToken(""));
        }

        @Test
        @DisplayName("null Token抛出异常")
        void parseToken_nullToken_throwsException() {
            assertThrows(Exception.class, () -> jwtUtils.parseToken(null));
        }

        @Test
        @DisplayName("提取用户ID使用Long类型接收")
        void getUserIdFromToken_returnsLong() {
            String token = jwtUtils.generateToken(1L, "testuser", "DOCTOR");

            Object userId = jwtUtils.getUserIdFromToken(token);

            assertInstanceOf(Long.class, userId);
        }
    }
}
