package com.example.mvcframework.servlet;

import com.example.mvcframework.annotaion.*;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyDispatcherServlet extends HttpServlet {
    private Properties contextConfig = new Properties();
    private List<String> classNames = new ArrayList<>();
    private Map<String, Object> ioc = new HashMap<>();
    private List<Handler> handlerMapping = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Server Error");
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {
        Handler handler = getHandler(req);
        if (handler == null) {
            resp.getWriter().write("404 Not Found");
            return;
        }
        Method method = handler.method;
        Class<?>[] paramTypes = method.getParameterTypes();

        // 保存所有需要自动赋值的参数值
        Object[] paramValues = new Object[paramTypes.length];


        Map<String, String[]> params = req.getParameterMap();
        for (Map.Entry<String, String[]> param : params.entrySet()) {
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "");

            // 如果找到匹配的对象，则开始填充参数值
            if (!handler.paramIndexMapping.containsKey(param.getKey())) {
                continue;
            }
            Integer index = handler.paramIndexMapping.get(param.getKey());
            paramValues[index] = convert(paramTypes[index], value);
        }

        // 设置方法中的request, response对象
        Integer reqIndex = handler.paramIndexMapping.get(HttpServletRequest.class.getName());
        if (reqIndex != null && reqIndex > -1) {
            paramValues[reqIndex] = req;
        }

        Integer respIndex = handler.paramIndexMapping.get(HttpServletResponse.class.getName());
        if (respIndex != null && respIndex > -1) {
            paramValues[respIndex] = resp;
        }
        handler.method.invoke(handler.controller, paramValues);
    }

    private Handler getHandler(HttpServletRequest req) {
        if (handlerMapping.isEmpty()) {
            return null;
        }
        String url = req.getRequestURI(); // 得到请求的绝对路径
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        for (Handler handler : handlerMapping) {
            Matcher matcher = handler.pattern.matcher(url);
            if (matcher.matches()) {
                return handler;
            }
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) {
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
                    // 2. 指定了service名称时按指定名称为key
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
        if (ioc.isEmpty()) {
            return;
        }
        // 循环ioc容器中的所有类，对需要自动赋值的属性进行赋值
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {

            // 依赖注入，不管是谁
            Field[] declaredFields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                if (!field.isAnnotationPresent(HjAutowired.class)) {
                    continue;
                }
                // 只对加了autowire注解的属性进行赋值
                String beanName = field.getAnnotation(HjAutowired.class).value().trim();  // 获取注解中的注入实例名称
                if (beanName.equals("")) {
                    beanName = field.getType().getName();
                }

                // 暴力访问，不管是什么访问限制
                field.setAccessible(true);

                // ioc 容器中取值注入对象
                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }
            }

        }
    }

    // 5. 构造handleMapping, 将url和方法进行关联
    private void initHandleMapping() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(HjController.class)) { // 过滤掉所有非controller的实例
                continue;
            }


            String baseUrl = "";
            if (clazz.isAnnotationPresent(HjRequestMapping.class)) {
                HjRequestMapping requestMapping = clazz.getAnnotation(HjRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            // 扫描controller中所有的公共方法, 并将路径与方法进行映射绑定
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(HjRequestMapping.class)) { // 过滤掉controller下没有requestMapping注解的方法
                    continue;
                }
                HjRequestMapping requestMapping = method.getAnnotation(HjRequestMapping.class);
                String regex = ("/" + baseUrl + requestMapping.value()).replaceAll("/+", "/");
//                handlerMapping.put(methodUrl, method);
                Pattern pattern = Pattern.compile(regex);
                handlerMapping.add(new Handler(entry.getValue(), pattern, method));
                System.out.println(String.format("mapped %s to method %s", regex, method));
            }
        }
    }

    private class Handler {
        private Object controller;    // 保存方法对应的实例
        private Pattern pattern;  // url patten
        private Method method;    // 保存映射的方法
        private Map<String, Integer> paramIndexMapping; //  参数顺序

        public Handler(Object controller, Pattern pattern, Method method) {
            this.controller = controller;
            this.pattern = pattern;
            this.method = method;

            paramIndexMapping = new HashMap<>();
            putParamIndexMapping(method);
        }

        private void putParamIndexMapping(Method method) {
            // 提取方法中加了注解的参数
            Annotation[][] pa = method.getParameterAnnotations();
            for (int i = 0; i < pa.length; i++) {
                for (Annotation a : pa[i]) {
                    if (a instanceof HjRequestParam) {
                        String paramName = ((HjRequestParam) a).value();
                        if (!"".equals(paramName.trim())) {
                            paramIndexMapping.put(paramName, i);     // i 为参数顺序
                        }
                    }
                }
            }

            // 提取方法中的request和response参数
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> type = parameterTypes[i];
                if (HttpServletRequest.class == type || HttpServletResponse.class == type) {
                    paramIndexMapping.put(type.getName(), i);
                }
            }
        }
    }


    private Object convert(Class<?> type, String value) {
        if (Integer.class == type) {
            return Integer.valueOf(value);
        }
        return value;
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
