package com.ecwid.redisAndCollections.map;

import com.ecwid.redisAndCollections.config.RedisConfig;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;

public class RedisMap implements Map<Object, Object> {

    private final List<RedisTemplate<Object, Object>> redisTemplates;
    private final String redisMapKeyPrefix;

    public RedisMap(List<RedisTemplate<Object, Object>> redisTemplates) {
        this.redisTemplates = redisTemplates;
        this.redisMapKeyPrefix = "RedisMap:" + UUID.randomUUID();
    }

    public RedisMap() {
        RedisConfig redisConfig = new RedisConfig();
        RedisTemplate<Object, Object> redTemplate = redisConfig.redisTemplate(getLettuceConnectionFactory(redisConfig,
                "CLUSTER", "localhost", 7000, "", Collections.singletonList("127.0.0.1:7000")));
        this.redisTemplates = new ArrayList<>(Collections.singletonList(redTemplate));
        this.redisMapKeyPrefix = "RedisMap:" + UUID.randomUUID();

//        Clear the map when the JVM shuts down
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    public void addRedisServer(String host, int port) {
        RedisConfig redisConfig = new RedisConfig();
        RedisTemplate<Object, Object> redTemplate = redisConfig.redisTemplate(createConnectionFactory(host, port));
        this.redisTemplates.add(redTemplate);
    }

    private LettuceConnectionFactory getLettuceConnectionFactory(
            RedisConfig redisConfig, String redisMode, String redisHost, int redisPort,
            String redisPassword, List<String> redisClusterNodes) {
        LettuceConnectionFactory connectionFactory = redisConfig.redisConnectionFactory(
                redisMode, redisHost, redisPort, redisPassword, redisClusterNodes);
        connectionFactory.afterPropertiesSet();
        return connectionFactory;
    }

    private LettuceConnectionFactory createConnectionFactory(String host, int port) {
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(host, port);
        connectionFactory.afterPropertiesSet();
        return connectionFactory;
    }

    private RedisTemplate<Object, Object> getRedisTemplate(Object key) {
        int hash = key.hashCode();
        int index = Math.abs(hash % redisTemplates.size());
        return redisTemplates.get(index);
    }

    private String getRedisKey(Object key) {
        return redisMapKeyPrefix + ":" + key.toString();
    }

    @Override
    public int size() {
        int totalSize = 0;
        for (RedisTemplate<Object, Object> redisTemplate : redisTemplates) {
            Set<Object> keys = redisTemplate.keys(redisMapKeyPrefix + ":*");
            totalSize += (keys != null) ? keys.size() : 0;
        }
        return totalSize;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        RedisTemplate<Object, Object> redisTemplate = getRedisTemplate(key);
        String redisKey = getRedisKey(key);
        return redisTemplate.opsForHash().hasKey(redisKey, key);
    }

    @Override
    public boolean containsValue(Object value) {
        for (RedisTemplate<Object, Object> redisTemplate : redisTemplates) {
            Set<Object> keys = redisTemplate.keys(redisMapKeyPrefix + ":*");
            if (keys != null) {
                for (Object key : keys) {
                    Collection<Object> values = redisTemplate.opsForHash().values(key.toString());
                    if (values.contains(value)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Object get(Object key) {
        RedisTemplate<Object, Object> redisTemplate = getRedisTemplate(key);
        String redisKey = getRedisKey(key);
        return redisTemplate.opsForHash().get(redisKey, key);
    }

    @Override
    public Object put(Object key, Object value) {
        RedisTemplate<Object, Object> redisTemplate = getRedisTemplate(key);
        String redisKey = getRedisKey(key);
        redisTemplate.opsForHash().put(redisKey, key, value);
        return value;
    }

    @Override
    public Object remove(Object key) {
        RedisTemplate<Object, Object> redisTemplate = getRedisTemplate(key);
        String redisKey = getRedisKey(key);
        Object value = get(key);
        redisTemplate.opsForHash().delete(redisKey, key);
        return value;
    }

    @Override
    public void putAll(Map<?, ?> m) {
        for (Entry<?, ?> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        for (RedisTemplate<Object, Object> redisTemplate : redisTemplates) {
            Set<Object> keys = redisTemplate.keys(redisMapKeyPrefix + ":*");
            if (keys != null) {
                for (Object key : keys) {
                    redisTemplate.delete(key);
                }
            }
        }
    }

    @Override
    public Set<Object> keySet() {
        Set<Object> allKeys = new HashSet<>();
        for (RedisTemplate<Object, Object> redisTemplate : redisTemplates) {
            Set<Object> keys = redisTemplate.keys(redisMapKeyPrefix + ":*");
            if (keys != null) {
                allKeys.addAll(keys);
            }
        }
        return allKeys;
    }

    @Override
    public Collection<Object> values() {
        List<Object> allValues = new ArrayList<>();
        for (RedisTemplate<Object, Object> redisTemplate : redisTemplates) {
            Set<Object> keys = redisTemplate.keys(redisMapKeyPrefix + ":*");
            if (keys != null) {
                for (Object key : keys) {
                    Collection<Object> values = redisTemplate.opsForHash().values(key.toString());
                    allValues.addAll(values);
                }
            }
        }
        return allValues;
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        Set<Map.Entry<Object, Object>> allEntries = new HashSet<>();
        for (RedisTemplate<Object, Object> redisTemplate : redisTemplates) {
            Set<Object> keys = redisTemplate.keys(redisMapKeyPrefix + ":*");
            if (keys != null) {
                for (Object key : keys) {
                    Map<Object, Object> entries = redisTemplate.opsForHash().entries(key.toString());
                    allEntries.addAll(entries.entrySet());
                }
            }
        }
        return allEntries;
    }

    public String getRedisMapKeyPrefix() {
        return redisMapKeyPrefix;
    }

    public void close() {
        clear();
    }
}

