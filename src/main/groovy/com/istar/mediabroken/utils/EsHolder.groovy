package com.istar.mediabroken.utils

import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.transport.TransportAddress
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository

@Repository
class EsHolder implements InitializingBean {
    @Value('${es.host}')
    private String host

    @Value('${es.port}')
    private int port

    @Value('${es.cluster}')
    private String cluster

    private Client client = null;

    /*
    * 获取es的链接
    * */

    public Client getClient() {
        return client;
    }

    /*
    * 关闭es的链接
    * */

    public void shutDown() {
        if (client != null) {
            client.close();
        }
    }

    @Override
    void afterPropertiesSet() throws Exception {
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", cluster)
                .put("-Des.max-open-files", true)
                .put("client.transport.sniff", true).build();
        if (client == null || ((TransportClient) client).connectedNodes().isEmpty()) {
            String[] hostList = host.split(',')
            client = TransportClient.builder().settings(settings)
                    .build()
            TransportAddress[] addresses = new TransportAddress[hostList.size()]

            for (int i = 0; i < hostList.size(); i++) {
                try {
                    InetSocketTransportAddress inetSocketTransportAddress =
                            new InetSocketTransportAddress(InetAddress.getByName(hostList[i]), port)
                    client.addTransportAddress(inetSocketTransportAddress);
                } catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }
    }
}

