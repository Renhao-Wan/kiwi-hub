package com.iot.kiwilink.service;

import com.iot.kiwilink.config.LinkUrlProperties;
import com.iot.kiwilink.service.impl.LinkServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RBloomFilter;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * 短链接服务单元测试
 * @author opencode
 */
@DisplayName("短链接服务测试")
class LinkServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private RBloomFilter<String> bloomFilter;

    private LinkServiceImpl linkService;

    private static final String BASE_LONG_URL = "https://kiwihub.com/article/";
    private static final String BASE_SHORT_URL = "https://s.kiwihub.com/";
    private static final String REDIS_KEY_PREFIX = "kiwi:short_link:";
    private static final String TEST_ARTICLE_ID = "article123";

    @BeforeEach
    void setUp() {
        LinkUrlProperties properties = new LinkUrlProperties();
        properties.setBaseLongLink(BASE_LONG_URL);
        properties.setBaseShortLink(BASE_SHORT_URL);
        linkService = new LinkServiceImpl(properties, stringRedisTemplate, bloomFilter);
    }

    @Test
    @DisplayName("生成短链接成功")
    void generateShortLink_Success() {
        MockitoAnnotations.openMocks(this);
        String articleId = TEST_ARTICLE_ID;
        String expectedCode = "abc123";
        String expectedShortLink = BASE_SHORT_URL + expectedCode;
        String expectedLongUrl = BASE_LONG_URL + articleId;

        when(bloomFilter.contains(expectedCode)).thenReturn(false);
        doAnswer(invocation -> {
            stringRedisTemplate.opsForValue().set(expectedCode, expectedLongUrl);
            return Boolean.TRUE;
        }).when(stringRedisTemplate).opsForValue().set(expectedCode, expectedLongUrl);

        String actualShortLink = linkService.generateShortLink(articleId);

        assertThat(actualShortLink).isEqualTo(expectedShortLink);
        verify(stringRedisTemplate).opsForValue().set(expectedCode, expectedLongUrl);
    }

    @Test
    @DisplayName("获取长链接成功")
    void getLongLink_Success() {
        MockitoAnnotations.openMocks(this);
        String code = "abc123";
        String expectedLongUrl = BASE_LONG_URL + TEST_ARTICLE_ID;

        when(stringRedisTemplate.opsForValue().get(REDIS_KEY_PREFIX + code)).thenReturn(expectedLongUrl);

        String actualLongUrl = linkService.getLongLink(code);

        assertThat(actualLongUrl).isEqualTo(expectedLongUrl);
        verify(stringRedisTemplate).opsForValue().get(REDIS_KEY_PREFIX + code);
    }

    @Test
    @DisplayName("获取长链接失败 - Redis 中不存在数据")
    void getLongLink_Fail() {
        MockitoAnnotations.openMocks(this);
        String code = "nonexistent";
        when(stringRedisTemplate.opsForValue().get(REDIS_KEY_PREFIX + code)).thenReturn(null);

        assertThatThrownBy(() -> linkService.getLongLink(code))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("短链接不存在或已过期");
    }
}
