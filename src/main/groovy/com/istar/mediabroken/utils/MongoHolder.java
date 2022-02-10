package com.istar.mediabroken.utils;

import com.mongodb.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MongoHolder implements InitializingBean {

    @Value("${mongo.host}")
    public String host;

    @Value("${mongo.dbName}")
    public String dbName;

    @Value("${mongo.options.threadsAllowedToBlockForConnectionMultiplier}")
    public int threadsAllowedToBlockForConnectionMultiplier;

    @Value("${mongo.options.connectTimeout}")
    public int connectTimeout;

    @Value("${mongo.options.socketTimeout}")
    public int socketTimeout;

    @Value("${mongo.options.connectionsPerHost}")
    public int connectionsPerHost;

    @Value("${mongo.options.maxWaitTime}")
    public int maxWaitTime;

    private Mongo mongo;

    private DB db;

    public void afterPropertiesSet() throws Exception {
        String[] hosts = host.split(",");
        List<ServerAddress> serverAddressList = new ArrayList<>();
        for (int i = 0; i < hosts.length; i++) {
            serverAddressList.add(new ServerAddress(hosts[i]));
        }
        mongo = new MongoClient(serverAddressList, getDefaultOptions());
        db = mongo.getDB(dbName);
    }

    private MongoClientOptions getDefaultOptions() {
        return new MongoClientOptions.Builder().socketKeepAlive(true) // 是否保持长链接
                .connectTimeout(connectTimeout) // 链接超时时间
                .socketTimeout(socketTimeout) // read数据超时时间
                .readPreference(ReadPreference.primary()) // 最近优先策略
                .connectionsPerHost(connectionsPerHost) // 每个地址最大请求数
                .maxWaitTime(maxWaitTime) // 长链接的最大等待时间
                .threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier) // 一个socket最大的等待请求数
                .writeConcern(WriteConcern.NORMAL).build();
    }

    public DBCollection getCollection(String name) {
        return db.getCollection(name);
    }

    public void close() {
        mongo.close();
    }

}