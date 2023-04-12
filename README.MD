##Introduction
Unit testing speed up framework

##Steps
- install jrebel first
- using jrebel to boot Speedup in debug mode
  ![avatar](./png/Snapshot2021-10-29_08-21-30.jpg)
- using jrebel to boot Speedup as debug mode
    ![avatar](./png/Snapshot2021-10-29_08-23-28.jpg)
    wait a second，later the spring ioc will boot inside the jetty，The whole j2ee container will be initialized and execute the UT
- You can try to change implemention of UT or can refactor the definition of UT,Jrebel will reload the UT after 1-5s
   ![avatar](./png/Snapshot2021-10-29_08-27-25.jpg)
- The you can rerun the UT，You will find the UT has taken effective,No matter how you have changed the UT.

## The mechanism
I using a custom class loader and use it to load the whole project to be tested into jetty when jetty was starting.Jetty acts like a http server so that it will be listening on a specific port.I using a custom listener to intercept the UT execution when junit starting and performing the UT case.The listener will perform a remote invocation(by a GET request with the UT case's package name、class name、method name and related params) after getting the method to be run to the jetty server.After jetty receiving the http call and getting the parameters(package+class name+method+parameters).The class loader inside the jetty will use these parameters to conduct a reflection and execute the UT case really.
Currently the jetty server is run by jrebel so that we can use the capbility of hotswap of jrebel to reload UT on the fly without rebooting.
So we only need to start the jetty server for one time.We don't have to reboot/rerun the junit each time when we change the UT case's logic afterwards
Improve efficiency by at least 70%

## 扩展更新
集成了mockito的自定义装配
如类A依赖于接口B的实现类：

```java
class A{
    @Autowired
    CLassB b;
}
```

那么使用mockito对B进行mock，首先完成mock对象的定义：
````java
@Mock
ClassB b;

@before
public void init(){
    when(b.method()).thenReturn(xxxxx);
}
````
然后在ut上下文中完成类的装配：
````java

//在ut的上下文中autowired类A，同时使用InjectMocks注解，则会自动对ClassA的实例注入mock对象，完成真实类和mock类的装配，完成集成测试
@Autowired
@InjectMocks
ClassA a;
````

使用该类体验一下：
```java
com.alex.speedup.SpeedupApplicationTests
```

any questions,please call me:
![avatar](./png/WechatIMG31.jpeg)
