package com.alex.speedup.junit.impl;

import com.alex.speedup.junit.Property;
import com.alex.speedup.junit.SystemProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class AbstractBehavior implements Behavior {
    protected Map<String, String> properties = Collections.emptyMap();

    public AbstractBehavior() {
    }

    protected void processSystemProperties(Class<?> clazz) {
        SystemProperties systemProperties = (SystemProperties)clazz.getAnnotation(SystemProperties.class);
        if (systemProperties != null) {
            Property[] properties = systemProperties.value();
            this.properties = new HashMap();
            Property[] var4 = properties;
            int var5 = properties.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                Property property = var4[var6];
                this.properties.put(property.key(), property.value());
            }

        }
    }

    public void before() {
        Iterator var1 = this.properties.keySet().iterator();

        while(var1.hasNext()) {
            String key = (String)var1.next();
            System.setProperty(key, (String)this.properties.get(key));
            System.out.println("[PandoraBootRunner] set system property: " + key + "->" + (String)this.properties.get(key));
        }

    }

    public void after() {
        Iterator var1 = this.properties.keySet().iterator();

        while(var1.hasNext()) {
            String key = (String)var1.next();
            System.clearProperty(key);
        }

        System.clearProperty("com.taobao.pandora.tmp_path");
    }
}
