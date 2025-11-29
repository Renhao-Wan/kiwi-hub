package com.iot.kiwilink.service.impl;

import com.iot.common.exception.ServiceException;
import com.iot.kiwilink.config.LinkUrlProperties;
import com.iot.kiwilink.service.LinkService;
import com.iot.kiwilink.utils.ShortLinkUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 短链接服务实现类
 * @author wan
 */
@Service
public class LinkServiceImpl implements LinkService {

    private final StringRedisTemplate stringRedisTemplate;

    // 假设你引入了 Redisson 或其他布隆过滤器组件
//    private final RBloomFilter<String> bloomFilter;

    private final String BASE_LONG_URL;
    private final String BASE_SHORT_URL;
    private static final String REDIS_KEY_PREFIX = "kiwi:short_link:";

    public LinkServiceImpl(LinkUrlProperties linkUrlProperties, StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        BASE_LONG_URL = linkUrlProperties.getBaseLongLink();
        BASE_SHORT_URL = linkUrlProperties.getBaseShortLink();
    }

    /**
     * 生成短链接
     * @param articleId 文章ID
     * @return 短链接
     */
    @Override
    public String generateShortLink(String articleId) {
        // 拼接一个长链接（或者是仅针对 articleId 生成）
        // 如果目的是跳转到文章详情页
        String longUrl = BASE_LONG_URL + articleId;

        // 为了避免冲突，可以加盐或者循环处理冲突
        String code = generateCode(longUrl);

        // 存入 Redis (Key: Code, Value: LongUrl)
        // 设置过期时间，或者永久存储，看业务需求
        stringRedisTemplate.opsForValue().set(REDIS_KEY_PREFIX + code, longUrl);

        // 返回完整链接
        return BASE_SHORT_URL + code;
    }

    /**
     * TODO 完成布隆过滤器去重
     * 生成 Code 并处理布隆过滤器冲突
     */
    private String generateCode(String input) {
        String currentInput = input;
        // 最多尝试5次，防止死循环
        for (int i = 0; i < 5; i++) {
            // MurmurHash 计算 Hash 值
            long hash = ShortLinkUtils.murmurHash32(currentInput);
            // Base62 转换
            String code = ShortLinkUtils.toBase62(hash);

            // 布隆过滤器判重
            // 如果布隆过滤器说不存在，那一定不存在 -> 直接用
//            if (!bloomFilter.contains(code)) {
//                bloomFilter.add(code);
//                return code;
//            }

            // 如果布隆过滤器说存在，可能是误判，也可能是真冲突
            // 这里为了保险，修改输入（加盐），重新 Hash
            currentInput += "SALT";
            return code;
        }
        throw new ServiceException("生成短链失败，冲突过多");
    }

    /**
     * 获取长链接
     * @param code 短链接
     * @return 完整的长链接
     */
    @Override
    public String getLongLink(String code) {
        return stringRedisTemplate.opsForValue().get(REDIS_KEY_PREFIX + code);
    }
}
