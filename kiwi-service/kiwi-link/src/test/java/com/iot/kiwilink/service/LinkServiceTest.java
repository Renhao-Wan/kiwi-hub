package com.iot.kiwilink.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBloomFilter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.iot.common.exception.ServiceException;
import com.iot.kiwilink.config.LinkUrlProperties;
import com.iot.kiwilink.service.impl.LinkServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * LinkService 单元测试类
 * 测试 {@link LinkService#generateShortLink(String)} 方法的核心逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LinkService 单元测试")
class LinkServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private RBloomFilter<String> bloomFilter;

    @Mock
    private LinkUrlProperties linkUrlProperties;

    @InjectMocks
    private LinkServiceImpl linkService;

    private static final String TEST_ARTICLE_ID = "test-article-123";
    private static final String BASE_LONG_URL = "http://127.0.0.1/articles/";
    private static final String BASE_SHORT_URL = "http://127.0.0.1/links/s/";
    private static final String REDIS_KEY_PREFIX = "kiwi:short_link:";

    @BeforeEach
    void setUp() {
        // 在构造函数被调用之前配置 mock
        // 使用 lenient 模式避免 UnnecessaryStubbingException
        lenient().when(linkUrlProperties.getBaseLongLink()).thenReturn(BASE_LONG_URL);
        lenient().when(linkUrlProperties.getBaseShortLink()).thenReturn(BASE_SHORT_URL);
        
        // 重新创建 LinkServiceImpl 实例，确保构造函数使用配置好的 mock
        linkService = new LinkServiceImpl(linkUrlProperties, stringRedisTemplate, bloomFilter);
    }

    @Test
    @DisplayName("generateShortLink_Success - 正常生成短链接场景")
    void generateShortLink_Success() {
        // Arrange (准备)
        // 模拟布隆过滤器不包含该 code
        when(bloomFilter.contains(anyString())).thenReturn(false);
        
        // 模拟 Redis 操作
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act (执行)
        String result = linkService.generateShortLink(TEST_ARTICLE_ID);

        // Assert (验证)
        // 验证返回的短链接格式正确
        assertThat(result).isNotNull();
        assertThat(result).startsWith(BASE_SHORT_URL);
        
        // 验证布隆过滤器被调用
        verify(bloomFilter).contains(anyString());
        verify(bloomFilter).add(anyString());
        
        // 验证 Redis 存储被调用
        verify(stringRedisTemplate.opsForValue()).set(
            anyString(), 
            eq(BASE_LONG_URL + TEST_ARTICLE_ID)
        );
    }

    @Test
    @DisplayName("generateShortLink_Fail_ConflictTooMany - 布隆过滤器冲突过多异常场景")
    void generateShortLink_Fail_ConflictTooMany() {
        // Arrange (准备)
        // 模拟布隆过滤器始终返回 true（表示冲突）
        when(bloomFilter.contains(anyString())).thenReturn(true);

        // Act & Assert (执行和验证)
        // 验证抛出 ServiceException
        assertThatThrownBy(() -> linkService.generateShortLink(TEST_ARTICLE_ID))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("生成短链失败，冲突过多");
        
        // 验证布隆过滤器被调用多次（最多5次）
        verify(bloomFilter, atLeast(1)).contains(anyString());
        verify(bloomFilter, atMost(5)).contains(anyString());
        
        // 验证 Redis 存储没有被调用
        verify(stringRedisTemplate.opsForValue(), never()).set(anyString(), anyString());
    }

    @Test
    @DisplayName("generateShortLink_Success_WithRetry - 第一次冲突，第二次成功场景")
    void generateShortLink_Success_WithRetry() {
        // Arrange (准备)
        // 模拟布隆过滤器：第一次冲突，第二次不冲突
        when(bloomFilter.contains(anyString()))
            .thenReturn(true)   // 第一次调用返回 true（冲突）
            .thenReturn(false); // 第二次调用返回 false（不冲突）
        
        // 模拟 Redis 操作
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act (执行)
        String result = linkService.generateShortLink(TEST_ARTICLE_ID);

        // Assert (验证)
        assertThat(result).isNotNull();
        assertThat(result).startsWith(BASE_SHORT_URL);
        
        // 验证布隆过滤器被调用2次
        verify(bloomFilter, times(2)).contains(anyString());
        // 验证布隆过滤器 add 被调用1次
        verify(bloomFilter, times(1)).add(anyString());
        
        // 验证 Redis 存储被调用
        verify(stringRedisTemplate.opsForValue()).set(anyString(), anyString());
    }

    @Test
    @DisplayName("generateShortLink_Success_EmptyArticleId - 空文章ID场景")
    void generateShortLink_Success_EmptyArticleId() {
        // Arrange (准备)
        String emptyArticleId = "";
        
        when(bloomFilter.contains(anyString())).thenReturn(false);
        
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act (执行)
        String result = linkService.generateShortLink(emptyArticleId);

        // Assert (验证)
        assertThat(result).isNotNull();
        assertThat(result).startsWith(BASE_SHORT_URL);
        
        // 验证长链接拼接正确
        String expectedLongUrl = BASE_LONG_URL + emptyArticleId;
        verify(stringRedisTemplate.opsForValue()).set(anyString(), eq(expectedLongUrl));
    }

    @Test
    @DisplayName("generateShortLink_Success_NullArticleId - null文章ID场景")
    void generateShortLink_Success_NullArticleId() {
        // Arrange (准备)
        String nullArticleId = null;
        
        when(bloomFilter.contains(anyString())).thenReturn(false);
        
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act (执行)
        String result = linkService.generateShortLink(nullArticleId);

        // Assert (验证)
        assertThat(result).isNotNull();
        assertThat(result).startsWith(BASE_SHORT_URL);
        
        // 验证长链接拼接正确（null 会被转换为 "null" 字符串）
        String expectedLongUrl = BASE_LONG_URL + "null";
        verify(stringRedisTemplate.opsForValue()).set(anyString(), eq(expectedLongUrl));
    }

    @Test
    @DisplayName("generateShortLink_Success_VerifyRedisKeyFormat - 验证Redis key格式")
    void generateShortLink_Success_VerifyRedisKeyFormat() {
        // Arrange (准备)
        when(bloomFilter.contains(anyString())).thenReturn(false);
        
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act (执行)
        String result = linkService.generateShortLink(TEST_ARTICLE_ID);

        // Assert (验证)
        assertThat(result).isNotNull();
        
        // 验证 Redis key 以正确的前缀开头
        verify(stringRedisTemplate.opsForValue()).set(
            argThat((String key) -> key.startsWith(REDIS_KEY_PREFIX)),
            anyString()
        );
    }

    /**
     * 获取长链接测试 - 内部类
     * 测试 {@link LinkService#getLongLink(String)} 方法的核心逻辑
     */
    @Nested
    @DisplayName("获取长链接测试")
    class GetLongLinkTests {

        private static final String TEST_CODE = "abc123";
        private static final String TEST_LONG_URL = BASE_LONG_URL + TEST_ARTICLE_ID;
        private static final String REDIS_KEY = REDIS_KEY_PREFIX + TEST_CODE;

        @Test
        @DisplayName("getLongLink_Success - 正常获取长链接场景")
        void getLongLink_Success() {
            // Arrange (准备)
            // 模拟 Redis 返回长链接
            ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(REDIS_KEY)).thenReturn(TEST_LONG_URL);

            // Act (执行)
            String result = linkService.getLongLink(TEST_CODE);

            // Assert (验证)
            // 验证返回的长链接正确
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(TEST_LONG_URL);
            
            // 验证 Redis 查询被正确调用
            verify(stringRedisTemplate.opsForValue()).get(REDIS_KEY);
        }

        @Test
        @DisplayName("getLongLink_Success_EmptyCode - 空code场景")
        void getLongLink_Success_EmptyCode() {
            // Arrange (准备)
            String emptyCode = "";
            String redisKeyForEmptyCode = REDIS_KEY_PREFIX + emptyCode;
            
            ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(redisKeyForEmptyCode)).thenReturn(TEST_LONG_URL);

            // Act (执行)
            String result = linkService.getLongLink(emptyCode);

            // Assert (验证)
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(TEST_LONG_URL);
            
            // 验证 Redis 查询被正确调用
            verify(stringRedisTemplate.opsForValue()).get(redisKeyForEmptyCode);
        }

        @Test
        @DisplayName("getLongLink_Success_NullCode - null code场景")
        void getLongLink_Success_NullCode() {
            // Arrange (准备)
            String nullCode = null;
            String redisKeyForNullCode = REDIS_KEY_PREFIX + "null";
            
            ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(redisKeyForNullCode)).thenReturn(TEST_LONG_URL);

            // Act (执行)
            String result = linkService.getLongLink(nullCode);

            // Assert (验证)
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(TEST_LONG_URL);
            
            // 验证 Redis 查询被正确调用
            verify(stringRedisTemplate.opsForValue()).get(redisKeyForNullCode);
        }

        @Test
        @DisplayName("getLongLink_Success_ReturnNull - Redis中不存在对应code场景")
        void getLongLink_Success_ReturnNull() {
            // Arrange (准备)
            ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(REDIS_KEY)).thenReturn(null);

            // Act (执行)
            String result = linkService.getLongLink(TEST_CODE);

            // Assert (验证)
            // 验证返回 null（表示长链接不存在）
            assertThat(result).isNull();
            
            // 验证 Redis 查询被正确调用
            verify(stringRedisTemplate.opsForValue()).get(REDIS_KEY);
        }

        @Test
        @DisplayName("getLongLink_Success_VerifyRedisKeyFormat - 验证Redis key格式")
        void getLongLink_Success_VerifyRedisKeyFormat() {
            // Arrange (准备)
            ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

            // Act (执行)
            linkService.getLongLink(TEST_CODE);

            // Assert (验证)
            // 验证 Redis key 以正确的前缀开头
            verify(stringRedisTemplate.opsForValue()).get(
                argThat((String key) -> key.startsWith(REDIS_KEY_PREFIX))
            );
        }

        @Test
        @DisplayName("getLongLink_Success_WithSpecialCharacters - 包含特殊字符的code场景")
        void getLongLink_Success_WithSpecialCharacters() {
            // Arrange (准备)
            String specialCode = "abc-123_xyz";
            String redisKeyForSpecialCode = REDIS_KEY_PREFIX + specialCode;
            
            ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(redisKeyForSpecialCode)).thenReturn(TEST_LONG_URL);

            // Act (执行)
            String result = linkService.getLongLink(specialCode);

            // Assert (验证)
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(TEST_LONG_URL);
            
            // 验证 Redis 查询被正确调用
            verify(stringRedisTemplate.opsForValue()).get(redisKeyForSpecialCode);
        }
    }
}