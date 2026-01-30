package com.ailianlian.ablecisi.constant;

import java.io.InputStream;
import java.util.Properties;

/**
 * ailianlian
 * com.ailianlian.ablecisi.constant
 * NetWorkPathConstant <br>
 * 网络请求路径常量类
 * @author Ablecisi
 * @version 1.0
 * 2025/4/26
 * 星期六
 * 12:26
 */
public class NetWorkPathConstant {
    //本地
    // public static final String BASE_URL = "http://10.0.2.2:8230";
    // 远程
    public static String BASE_URL;

    /* 在这个类被调用的时候动态从配置文件里获取URL */
    static {
        try {
            Properties props = new Properties();
            // 加载 classpath 下的配置文件
            InputStream in = NetWorkPathConstant.class.getClassLoader().getResourceAsStream("assets/config/config.properties");
            props.load(in);
            BASE_URL = props.getProperty("NetWorkPathBaseURL.value");
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            BASE_URL = "http://10.0.2.2:8230"; // 异常处理
        }
    }
}
