package com.device.hk;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginLoaderDemo {
    public static void main(String[] args) throws Exception {
        String path = Thread.currentThread().getContextClassLoader().getResource("").toURI().getPath();
        path = path.replace("/classes/", "/lib-jar/");
        path = path.replace("/target/", "/");

        // 1. 插件 JAR 路径
        String jarPath = path + "/hk-video-plugin-1.0-SNAPSHOT.jar"; // 改成你的路径
        File jarFile = new File(jarPath);
        if (!jarFile.exists()) {
            throw new RuntimeException("插件 JAR 不存在: " + jarPath);
        }

        // 2. 创建类加载器
        URL jarUrl = jarFile.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, PluginLoaderDemo.class.getClassLoader());

        // 3. 加载类
        Class<?> pluginClass = classLoader.loadClass("com.device.hk.VideoPluginMain"); // 替换为插件中 VideoPluginMain 的全类名
        Object pluginInstance = pluginClass.getDeclaredConstructor().newInstance();

        // 4. 调用 init 方法
        Method initMethod = pluginClass.getMethod("init", Map.class);
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("ehomePuIp", "192.168.1.242");
        configMap.put("ehomeInIp", "192.168.1.242");
        configMap.put("ehomeCmsPort", "7660");
        configMap.put("ehomeSmsPreViewPort", "9127");
        configMap.put("smsBackServerListenIP", "192.168.1.242");
        configMap.put("smsBackServerListenPort", "9128");
        initMethod.invoke(pluginInstance, configMap);

        // 5. 模拟执行逻辑（等设备上线）
        boolean tag = true;
        Method executeMethod = pluginClass.getMethod("execute", String.class, Map.class);
        while (tag) {
            Map<String, Object> deviceInfoMap = new HashMap<>();
            deviceInfoMap.put("deviceId", "245652081");
            deviceInfoMap.put("wsPort", "8024");
            Thread.sleep(1000);
            String result = (String) executeMethod.invoke(pluginInstance, "deviceStatus", deviceInfoMap);
            HashMap<String,Object> resultMap = (HashMap<String, Object>) JSONObject.parseObject(result, Map.class);
            System.out.println("获取设备状态: " + result);
            if(!resultMap.get("code").toString().equals("200")) {
                continue;
            }
            tag = false;

            // 6. 调用 execute 方法
            result = (String) executeMethod.invoke(pluginInstance, "startPreview", deviceInfoMap);
            System.out.println("预览结果: " + result);

            Thread.sleep(1000 * 20);
        }

        // 7. 保持程序运行
        while (true) {
            Thread.sleep(1000);
        }
    }
}
