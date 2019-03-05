package com.example.qhheventbus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinhaihang_vendor
 * @version $Rev$
 * @time 2019/3/5 16:43
 * @des
 * @packgename com.example.qhheventbus
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes
 */
public class SubscriberMethodFinder {

    private static final Map<Class<?>, List<SubscriberMethod>> METHOD_CACHE = new ConcurrentHashMap<>();

    List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
        List<SubscriberMethod> subscriberMethods = METHOD_CACHE.get(subscriberClass);
        if (subscriberMethods != null) {
            return subscriberMethods;
        }

        subscriberMethods = findUsingReflection(subscriberClass);

        if (subscriberMethods.isEmpty()) {
//            throw new EventBusException("Subscriber " + subscriberClass
//                    + " and its super classes have no public methods with the @Subscribe annotation");
            return null;
        } else {
            METHOD_CACHE.put(subscriberClass, subscriberMethods);
            return subscriberMethods;
        }
    }

    private List<SubscriberMethod> findUsingReflection(Class<?> subscriberClass) {

        Method[] methods = subscriberClass.getDeclaredMethods();

        List<SubscriberMethod> subscribeMethods = new ArrayList<>();
        for (Method method : methods) {
            //获取标记有自定义注解 Subscribe 的方法
            Subscribe annotation = method.getAnnotation(Subscribe.class);
            if(null != annotation){
                //获取方法参数
                Class<?>[] parameterTypes = method.getParameterTypes();
                if(parameterTypes.length == 1){
                    //只有一个参数的时候获取一个参数
                    Class<?> parameterType = parameterTypes[0];
                    //获取自定义注解Subscribe上的value,即时在哪个线程进行
                    ThreadMode threadMode = annotation.threadMode();
                    subscribeMethods.add(new SubscriberMethod(method,threadMode,parameterType));
                }
            }

        }


        return subscribeMethods;
    }


}
