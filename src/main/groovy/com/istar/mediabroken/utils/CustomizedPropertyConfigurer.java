package com.istar.mediabroken.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CustomizedPropertyConfigurer extends PropertyPlaceholderConfigurer {

    private Map<String, Object> ctxPropertiesMap;

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactory,
                                     Properties props)throws BeansException {

        super.processProperties(beanFactory, props);
        //load properties to ctxPropertiesMap
        ctxPropertiesMap = new HashMap<String, Object>();
        for (Object key : props.keySet()) {
            String keyStr = key.toString();
            String value = props.getProperty(keyStr);
            ctxPropertiesMap.put(keyStr, value);
        }
//        System.out.println(ctxPropertiesMap);
    }

    public Object getProperty(String name) {
//        System.out.println(ctxPropertiesMap);
        return ctxPropertiesMap.get(name);
    }
}