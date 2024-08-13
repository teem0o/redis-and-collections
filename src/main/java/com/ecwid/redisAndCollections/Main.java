package com.ecwid.redisAndCollections;

import com.ecwid.redisAndCollections.map.RedisMap;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        RedisMap map = new RedisMap();
        map.addRedisServer("localhost", 7001);
        map.addRedisServer("localhost", 7002);
        map.put("1", "one");
        map.put("2", "two");
        map.putAll(new HashMap<>(Map.of("3", "three", "4", "four")));
//		map.clear();
//		System.out.println(map.containsKey("11"));
//		System.out.println(map.containsValue("one"));
//		System.out.println(map.get("1"));
//		System.out.println(map.isEmpty());
//		System.out.println(map.size());
//		System.out.println(map.remove("1"));
//		map.values().forEach(System.out::println);
        map.entrySet().forEach(System.out::println);
    }
}
