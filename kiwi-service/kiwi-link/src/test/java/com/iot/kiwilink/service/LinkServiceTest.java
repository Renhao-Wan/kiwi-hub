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
 * 测试 {@link LinkService#generateShortLink(Long)} 方法的核心逻辑
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

    private static final Long TEST_ARTICLE_ID = 123L;
    private static final String BASE_LONG_URL = "http://127.0.0.1/articles/";
    private static final String BASE_SHORT_URL = "http://127.0.0.1/links/s/";
    private static final String REDIS_KEY_PREFIX = "kiwi:short_link:";

    @BeforeEach
    void setUp() {
        lenient().when(linkUrlProperties.getBaseLongLink()).thenReturn(BASE_LONG_URL);
        lenient().when(linkUrlProperties.getBaseShortLink()).thenReturn(BASE_SHORT_URL);
        linkService = new LinkServiceImpl(linkUrlProperties, stringRedisTemplate, bloomFilter);
    }

    @Test
    @DisplayName("generateShortLink_Success - 正常生成短链接场景")
    void generateShortLink_Success() {
        when(bloomFilter.contains(anyString())).thenReturn(false);

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        String result = linkService.generateShortLink(TEST_ARTICLE_ID);

        assertThat(result).isNotNull();
        assertThat(result).startsWith(BASE_SHORT_URL);

        verify(bloomFilter).contains(anyString());
        verify(bloomFilter).add(anyString());
        verify(stringRedisTemplate.opsForValue()).set(
            anyString(),
            eq(BASE_LONG_URL + TEST_ARTICLE_ID)
        );
    }

    @Test
    @DisplayName("generateShortLink_Fail_ConflictTooMany - 布隆过滤器冲突过多异常场景")
    void generateShortLink_Fail_ConflictTooMany() {
        when(bloomFilter.contains(anyString())).thenReturn(true);

        assertThatThrownBy(() -> linkService.generateShortLink(TEST_ARTICLE_ID))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("生成短链失败，冲突过多");

        verify(bloomFilter, atLeast(1)).contains(anyString());
        verify(bloomFilter, atMost(5)).contains(anyString());
        verify(stringRedisTemplate.opsForValue(), never()).set(anyString(), anyString());
    }

    @Test
    @DisplayName("generateShortLink_Success_WithRetry - 第一次冲突，第二次成功场景")
    void generateShortLink_Success_WithRetry() {
        when(bloomFilter.contains(anyString()))
            .thenReturn(true)
            .thenReturn(false);

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        String result = linkService.generateShortLink(TEST_ARTICLE_ID);

        assertThat(result).isNotNull();
        assertThat(result).startsWith(BASE_SHORT_URL);

        verify(bloomFilter, times(2)).contains(anyString());
        verify(bloomFilter, times(1)).add(anyString());
        verify(stringRedisTemplate.opsForValue()).set(anyString(), anyString());
    }

    @Test
    @DisplayName("generateShortLink_Success_ZeroArticleId - articleId 为 0 的边界场景")
    void generateShortLink_Success_ZeroArticleId() {
        Long zeroArticleId = 0L;

        when(bloomFilter.contains(anyString())).thenReturn(false);

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        String result = linkService.generateShortLink(zeroArticleId);

        assertThat(result).isNotNull();
        assertThat(result).startsWith(BASE_SHORT_URL);

        String expectedLongUrl = BASE_LONG_URL + zeroArticleId;
        verify(stringRedisTemplate.opsForValue()).set(anyString(), eq(expectedLongUrl));
    }

    @Test
    @DisplayName("generateShortLink_Success_NullArticleId - null 文章ID场景")
    void generateShortLink_Success_NullArticleId() {
        Long nullArticleId = null;

        when(bloomFilter.contains(anyString())).thenReturn(false);

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        String result = linkService.generateShortLink(nullArticleId);

        assertThat(result).isNotNull();
        assertThat(result).startsWith(BASE_SHORT_URL);

        String expectedLongUrl = BASE_LONG_URL + "null";
        verify(stringRedisTemplate.opsForValue()).set(anyString(), eq(expectedLongUrl));
    }

    @Test
    @DisplayName("generateShortLink_Success_VerifyRedisKeyFormat - 验证Redis key格式")
    void generateShortLink_Success_VerifyRedisKeyFormat() {
        when(bloomFilter.contains(anyString())).thenReturn(false);

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        String result = linkService.generateShortLink(TEST_ARTICLE_ID);

        assertThat(result).isNotNull();

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
            ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(REDIS_KEY)).thenReturn(TEST_LONG_URL);

            String result = linkService.getLongLink(TEST_CODE);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(TEST_LONG_URL);
            verify(stringRedisTemplate.opsForValue()).get(REDIS_KEY);
        }

        @Test
        @DisplayName("getLongLink_Success_EmptyCode - 空code场景")
        void getLongLink_Success_EmptyCode() {
            String emptyCode = "";
            String redisKeyForEmptyCode = REDIS_KEY_PREFIX + emptyCode;

            ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(redisKeyForEmptyCode)).thenReturn(TEST_LONG_URL);

            String result = linkService.getLongLink(emptyCode);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(TEST_LONG_URL);
            verify(stringRedisTemplate.opsForValue()).get(redisKeyForEmptyCode);
        }

        @Test
        @DisplayName("getLongLink_Success_NullCode - null code场景")
        void getLongLink_Success_NullCode() {
            String nullCode = null;
            String redisKeyForNullCode = REDIS_KEY_PREFIX + "null";

            ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(redisKeyForNullCode)).thenReturn(TEST_LONG_URL);

            String result = linkService.getLongLink(nullCode);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(TEST_LONG_URL);
            verify(stringRedisTemplate.opsForValue()).get(redisKeyForNullCode);
        }

        @Test
        @DisplayName("getLongLink_Success_ReturnNull - Redis中不存在对应code场景")
        void getLongLink_Success_ReturnNull() {
            ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(REDIS_KEY)).thenReturn(null);

            String result = linkService.getLongLink(TEST_CODE);

            assertThat(result).isNull();
            verify(stringRedisTemplate.opsForValue()).get(REDIS_KEY);
        }

        @Test
        @DisplayName("getLongLink_Success_VerifyRedisKeyFormat - 验证Redis key格式")
        void getLongLink_Success_VerifyRedisKeyFormat() {
            ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

            linkService.getLongLink(TEST_CODE);

            verify(stringRedisTemplate.opsForValue()).get(
                argThat((String key) -> key.startsWith(REDIS_KEY_PREFIX))
            );
        }

        @Test
        @DisplayName("getLongLink_Success_WithSpecialCharacters - 包含特殊字符的code场景")
        void getLongLink_Success_WithSpecialCharacters() {
            String specialCode = "abc-123_xyz";
            String redisKeyForSpecialCode = REDIS_KEY_PREFIX + specialCode;

            ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(redisKeyForSpecialCode)).thenReturn(TEST_LONG_URL);

            String result = linkService.getLongLink(specialCode);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(TEST_LONG_URL);
            verify(stringRedisTemplate.opsForValue()).get(redisKeyForSpecialCode);
        }
    }
}
