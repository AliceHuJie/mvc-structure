package com.example.mvcframework.servlet;

import com.example.mvcframework.annotaion.HjController;
import com.example.mvcframework.annotaion.HjService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.util.*;

public class MyDispatcherServlet extends HttpServlet {
    private Properties contextConfig = new Properties();
    private List<String> classNames = new ArrayList<>();
    private Map<String, Object> ioc = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 6. 等待请求阶段
        req.getRequestURI();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1. 加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        // 2. 扫描所有相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
        // 3. 初始化所有相关的类并且将其保存到IOC容器中
        doInstance();
        // 4. 执行依赖注入 把加了autowired注解的字段赋值
        doAutowired();
        // 5. 构造handleMapping, 将url和方法进行关联
        initHandleMapping();

        System.out.println("HJ MVC FRAMEWORK IS INIT.");
    }

    // 1. 加载配置文件
    private void doLoadConfig(String contextConfigLocation) {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != resourceAsStream) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 2. 扫描所有相关的类
    private void doScanner(String basePackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + basePackage.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(basePackage + "." + file.getName());
            } else {
                String className = basePackage + "." + file.getName().replace(".class", "");
                classNames.add(className);
                System.out.println(className);
            }
        }
    }

    // 3. 初始化所有相关的类并且将其保存到IOC容器中
    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }
        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                // 不是所有的类都要实例化，只实例化加了注解的类
                if (clazz.isAnnotationPresent(HjController.class)) {
                    // 默认以类名首字母小写为key，将实例化对象放入ioc
                    String key = firstCharToLowerCase(className);
                    ioc.put(key, clazz.newInstance());
                } else if (clazz.isAnnotationPresent(HjService.class)) {
                    // 1. 默认key为类名首字母小写
                    //2. 指定了service名称时按指定名称为key
                    HjService serviceAnnotation = clazz.getAnnotation(HjService.class);
                    String beanName = serviceAnnotation.value();
                    if ("".equals(beanName.trim())) {
                        beanName = firstCharToLowerCase(className);
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                    // 3. 根据接口类型赋值
                    for (Class<?> i : clazz.getInterfaces()) {
                        ioc.put(i.getName(), instance);
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 4. 执行依赖注入 把加了autowired注解的字段赋值
    private void doAutowired() {
    }

    // 5. 构造handleMapping, 将url和方法进行关联
    private void initHandleMapping() {
    }


    private String firstCharToLowerCase(String str) {
        char[] chars = str.toCharArray();
        if (str.isEmpty() || Character.isLowerCase(chars[0])) {
            return str;
        }
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
