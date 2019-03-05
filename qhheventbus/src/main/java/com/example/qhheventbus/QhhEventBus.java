package com.example.qhheventbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author qinhaihang_vendor
 * @version $Rev$
 * @time 2019/3/5 15:46
 * @des 使用注解实现
 * @packgename com.example.qhheventbus
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes
 */
public class QhhEventBus {

    private static QhhEventBus instance;

    private final SubscriberMethodFinder mSubscriberMethodFinder;
    private final Map<Class<?>, CopyOnWriteArrayList<Subscription>> mSubscriberByParamsType;
    private final Map<Object, List<Class<?>>> typesBySubscriber;

    public static QhhEventBus getInstance() {
        if (instance == null) {
            synchronized (QhhEventBus.class) {
                if (instance == null) {
                    instance = new QhhEventBus();
                }
            }
        }
        return instance;
    }

    public QhhEventBus() {
        mSubscriberMethodFinder = new SubscriberMethodFinder();
        mSubscriberByParamsType = new HashMap<>();
        typesBySubscriber = new HashMap<>();
    }

    public void register(Object subscriber){
        Class<?> subscriberClass = subscriber.getClass();
        //通过反射拿到 subscriber 中的接收信息的方法
        List<SubscriberMethod> subscriberMethods = mSubscriberMethodFinder.findSubscriberMethods(subscriberClass);
        synchronized (this){
            for (SubscriberMethod subscriberMethod : subscriberMethods) {
                subscribe(subscriber, subscriberMethod);
            }
        }
    }

    /**
     * 简单的注册流程
     * @param subscriber
     * @param subscriberMethod
     */
    private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
        //通过反射获取得到的方法的参数类型来保存subscriber?? 也就是事件类型
        Class<?> parameterType = subscriberMethod.mParameterType;
        CopyOnWriteArrayList<Subscription> subscriptions = mSubscriberByParamsType.get(parameterType);
        Subscription newSubscription = new Subscription(subscriber, subscriberMethod);

        if(subscriptions == null){
            subscriptions = new CopyOnWriteArrayList<>();
            mSubscriberByParamsType.put(parameterType,subscriptions);
        }else{
            if(subscriptions.contains(newSubscription)){
                //存在则不用再次注册
                return;
            }
        }

        subscriptions.add(newSubscription);

        List<Class<?>> subscribedEvents = typesBySubscriber.get(subscriber);
        if (subscribedEvents == null) {
            subscribedEvents = new ArrayList<>();
            typesBySubscriber.put(subscriber, subscribedEvents);
        }
        subscribedEvents.add(parameterType);

    }

    public synchronized void unRegister(Object subscriber){
        //找到缓存的参数type，通过参数type清除缓存
        List<Class<?>> eventTypes = typesBySubscriber.get(subscriber);

        if(eventTypes != null){
            for (Class<?> eventType : eventTypes) {
                List<Subscription> subscriptions = mSubscriberByParamsType.get(eventType);

                //这里引入一个集合中 java.util.ConcurrentModificationException
//                for (Subscription subscription : subscriptions) {
//                    if (subscription.subscriber == subscriber) {
//                        subscription.active = false;
//                        subscriptions.remove(subscriber);
//                    }
//                }

                //这种写法就是为了避免 java.util.ConcurrentModificationException
                if(subscriptions != null){
                    int size = subscriptions.size();
                    for (int i = 0; i < size; i++) {
                        Subscription subscription = subscriptions.get(i);
                        if(subscription.subscriber == subscriber){
                            subscription.active = false;
                            subscriptions.remove(i);
                            i--;
                            size--;
                        }
                    }
                }

            }
            typesBySubscriber.remove(subscriber);
        }

    }
}


























