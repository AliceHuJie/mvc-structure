# 手写Spring mvc 核心逻辑
1. 自定义注解，不借助Spring boot   
   包括HJController, HJService, HJAutowired, HJRequestParam, HJRequestMapping等
   
2. 配置web.xml，加载自定义的DispatchServlet

3. 手写DispatchServlet实现以下逻辑：  
   - 加载配置文件  
   将配置文件中的值加载到contextConfig（Properties）中   
   - 扫描指定扫描路径下的所有相关的类
   将所有扫描到的类名保存到List<String>中  
   - 初始化所有相关的类并保存到ioc容器中  
   实例化加了注解的类，保存到ioc容器（Map<String, Object>）。其中实例名key为类名首字母小写，或注解中指定实例名称，或为接口类型名称  
   - 执行依赖注入，把加了autowired注解的字段赋值  
   循环ioc容器中的所有类，对需要自动赋值的属性进行赋值，利用实例名为key从ioc容器中取值并注入对象
   - 构造handleMapping, 将url和方法进行关联

4. 运行阶段  
每一次请求将会调用doGet或doPost方法，它会根据url请求去HandlerMapping中匹配到对应的Method，然后利用反射机制调用Controller中的url对应的方法，并得到结果返回。
   

