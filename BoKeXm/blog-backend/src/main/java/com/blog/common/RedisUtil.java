package com.blog.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.warn("Redis set 失败，key={}，已降级跳过缓存：{}", key, e.getMessage());
        }
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.warn("Redis set(带过期) 失败，key={}，已降级跳过缓存：{}", key, e.getMessage());
        }
    }

    public Object get(String key) {
        if (key == null) {
            return null;
        }
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis get 失败，key={}，已降级返回 null：{}", key, e.getMessage());
            return null;
        }
    }

    public Boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis delete 失败，key={}，已降级：{}", key, e.getMessage());
            return false;
        }
    }

    public Long delete(Collection<String> keys) {
        try {
            return redisTemplate.delete(keys);
        } catch (Exception e) {
            log.warn("Redis delete(批量) 失败，已降级：{}", e.getMessage());
            return 0L;
        }
    }

    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.warn("Redis hasKey 失败，key={}，已降级返回 false：{}", key, e.getMessage());
            return false;
        }
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.warn("Redis expire 失败，key={}，已降级：{}", key, e.getMessage());
            return false;
        }
    }

    public Long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis getExpire 失败，key={}，已降级返回 -1：{}", key, e.getMessage());
            return -1L;
        }
    }

    public void hSet(String key, String hashKey, Object value) {
        try {
            redisTemplate.opsForHash().put(key, hashKey, value);
        } catch (Exception e) {
            log.warn("Redis hSet 失败，key={} hashKey={}，已降级：{}", key, hashKey, e.getMessage());
        }
    }

    public Object hGet(String key, String hashKey) {
        try {
            return redisTemplate.opsForHash().get(key, hashKey);
        } catch (Exception e) {
            log.warn("Redis hGet 失败，key={} hashKey={}，已降级返回 null：{}", key, hashKey, e.getMessage());
            return null;
        }
    }

    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.warn("Redis hGetAll 失败，key={}，已降级返回空 Map：{}", key, e.getMessage());
            return Collections.emptyMap();
        }
    }

    public Long hDelete(String key, Object... hashKeys) {
        try {
            return redisTemplate.opsForHash().delete(key, hashKeys);
        } catch (Exception e) {
            log.warn("Redis hDelete 失败，key={}，已降级：{}", key, e.getMessage());
            return 0L;
        }
    }

    public Boolean hHasKey(String key, String hashKey) {
        try {
            return redisTemplate.opsForHash().hasKey(key, hashKey);
        } catch (Exception e) {
            log.warn("Redis hHasKey 失败，key={} hashKey={}，已降级返回 false：{}", key, hashKey, e.getMessage());
            return false;
        }
    }

    public void lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
        } catch (Exception e) {
            log.warn("Redis lSet 失败，key={}，已降级：{}", key, e.getMessage());
        }
    }

    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.warn("Redis lGet 失败，key={}，已降级返回空 List：{}", key, e.getMessage());
            return Collections.emptyList();
        }
    }

    public Long lSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.warn("Redis lSize 失败，key={}，已降级返回 0：{}", key, e.getMessage());
            return 0L;
        }
    }

    public void sAdd(String key, Object... values) {
        try {
            redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.warn("Redis sAdd 失败，key={}，已降级：{}", key, e.getMessage());
        }
    }

    public Set<Object> sMembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.warn("Redis sMembers 失败，key={}，已降级返回空 Set：{}", key, e.getMessage());
            return Collections.emptySet();
        }
    }

    public Boolean sIsMember(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            log.warn("Redis sIsMember 失败，key={}，已降级返回 false：{}", key, e.getMessage());
            return false;
        }
    }

    public Long sSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.warn("Redis sSize 失败，key={}，已降级返回 0：{}", key, e.getMessage());
            return 0L;
        }
    }

    public Long sRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            log.warn("Redis sRemove 失败，key={}，已降级：{}", key, e.getMessage());
            return 0L;
        }
    }

    public void zAdd(String key, Object value, double score) {
        try {
            redisTemplate.opsForZSet().add(key, value, score);
        } catch (Exception e) {
            log.warn("Redis zAdd 失败，key={}，已降级：{}", key, e.getMessage());
        }
    }

    public Set<Object> zRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().range(key, start, end);
        } catch (Exception e) {
            log.warn("Redis zRange 失败，key={}，已降级返回空 Set：{}", key, e.getMessage());
            return Collections.emptySet();
        }
    }

    public Set<Object> zReverseRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().reverseRange(key, start, end);
        } catch (Exception e) {
            log.warn("Redis zReverseRange 失败，key={}，已降级返回空 Set：{}", key, e.getMessage());
            return Collections.emptySet();
        }
    }

    public Double zIncrementScore(String key, Object value, double delta) {
        try {
            return redisTemplate.opsForZSet().incrementScore(key, value, delta);
        } catch (Exception e) {
            log.warn("Redis zIncrementScore 失败，key={}，已降级返回 null：{}", key, e.getMessage());
            return null;
        }
    }

    public Long zRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForZSet().remove(key, values);
        } catch (Exception e) {
            log.warn("Redis zRemove 失败，key={}，已降级：{}", key, e.getMessage());
            return 0L;
        }
    }

}
