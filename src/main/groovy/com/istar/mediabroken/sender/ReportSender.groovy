package com.istar.mediabroken.sender

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import groovy.util.logging.Slf4j
import kafka.javaapi.producer.Producer
import kafka.producer.KeyedMessage
import kafka.producer.ProducerConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Slf4j
@Service
class ReportSender implements Sender {

    @Value('${push.event.kafka.server}')
    String serversSend

    @Value('${push.event.kafka.topic.report}')
    public String topicSend

    @Lazy
    Producer<String, String> producer = {
        Properties props = new Properties();
        props.putAll([
                'metadata.broker.list': serversSend,
                'serializer.class'    : 'kafka.serializer.StringEncoder',
                'key.serializer.class': 'kafka.serializer.StringEncoder'
        ])
        Producer<String, String> producer = new Producer<String, String>(new ProducerConfig(props));
        return producer
    }.call()

    void send(String json) {
        producer.send(new KeyedMessage<String, String>(topicSend, json));
    }

    @Override
    void send(Map map) {
        producer.send(new KeyedMessage<String, String>(topicSend, JSON.toJSONString(map,SerializerFeature.WriteMapNullValue)));
    }
}