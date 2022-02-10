package com.istar.mediabroken.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by luda on 2018/5/3.
 */
public class RedisClientUtil {

    private static JedisPool pool = null;
    public static JedisPool getPool() {
        if (pool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            //控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
            //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
            config.setMaxTotal(50);
            //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
            config.setMaxIdle(5);
            //表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
            config.setMaxWaitMillis(1024 * 1024);
            //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
            config.setTestOnBorrow(true);
            pool = new JedisPool(config, "192.168.184.213");
        }
        return pool;
    }

    public static void returnResource(JedisPool pool, Jedis redis) {
        if (redis != null) {
            redis.close();
        }
    }

    public static String get(String key) {
        String value = null;

        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            value = jedis.get(key);
        } catch (Exception e) {
            //释放redis对象
            jedis.close();
            e.printStackTrace();
        } finally {
            //返还到连接池
            returnResource(pool, jedis);
        }

        return value;
    }

    public static void main(String[] args) {
        //连接本地的 Redis 服务
//        Jedis jedis = new Jedis("192.168.184.213");
//      jedis.select(1);
//        System.out.println("Connection to server sucessfully");
        //查看服务是否运行
//        System.out.println("Server is running: " + jedis.ping());
//        String bjzx = get("luda_test:bjzx");
//        JedisPool pool = new JedisPool("redis://192.168.184.213/0/luda_test");
//        JedisPoolConfig config = new JedisPoolConfig();
//        //控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
//        //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
//        config.setMaxTotal(50);
//        //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
//        config.setMaxIdle(5);
//        //表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
//        config.setMaxWaitMillis(1024 * 1024);
//
//        JedisPool pool = new JedisPool(config,"192.168.184.213");
//        Jedis jedis = pool.getResource();

        JSONArray bjzxList = JSON.parseArray(get("luda_test:bjzx"));
        for (int i = 0; i < bjzxList.size(); i++) {
            System.out.println(bjzxList.get(i));
        }
    }
}
