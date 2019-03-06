package com.example.qhheventbus;

import java.lang.reflect.InvocationTargetException;
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
    private final Map<Class<?>, CopyOnWriteArrayList<Subscription>> mSubscriberByEventType;
    private final Map<Object, List<Class<?>>> typesBySubscriber;

    /**
     * ThreadLocal知识点，往ThreadLocal中填充的变量属于当前线程，该变量对其他线程而言是隔离的.
     * 一个Posting对应一个线程
     */
    private final ThreadLocal<PostingThreadState> currentPostingThreadState = new ThreadLocal<PostingThreadState>() {
        @Override
        protected PostingThreadState initialValue() {
            return new PostingThreadState();
        }
    };

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
        mSubscriberByEventType = new HashMap<>();
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
        Class<?> parameterType = subscriberMethod.mEvnetType;
        CopyOnWriteArrayList<Subscription> subscriptions = mSubscriberByEventType.get(parameterType);
        Subscription newSubscription = new Subscription(subscriber, subscriberMethod);

        if(subscriptions == null){
            subscriptions = new CopyOnWriteArrayList<>();
            mSubscriberByEventType.put(parameterType,subscriptions);
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
                List<Subscription> subscriptions = mSubscriberByEventType.get(eventType);

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

    public void post(Object event){
        //将event添加到队列中
        PostingThreadState postingState = currentPostingThreadState.get();
        List<Object> eventQueue = postingState.eventQueue;
        eventQueue.add(event);

        if(!postingState.isPosting){
            //postingState.isMainThread = isMainThread();
            postingState.isPosting = true;
            try {
                while (!eventQueue.isEmpty()) {
                    postSingleEvent(eventQueue.remove(0), postingState);
                }
            } finally {
                postingState.isPosting = false;
                postingState.isMainThread = false;
            }
        }
    }

    private void postSingleEvent(Object event, PostingThreadState postingState) throws Error {
        //获取 event class 文件
        Class<?> eventClass = event.getClass();
        boolean subscriptionFound = false;

            subscriptionFound = postSingleEventForEventType(event, postingState, eventClass);

//        if (!subscriptionFound) {
//            if (logNoSubscriberMessages) {
//                logger.log(Level.FINE, "No subscribers registered for event " + eventClass);
//            }
//            if (sendNoSubscriberEvent && eventClass != NoSubscriberEvent.class &&
//                    eventClass != SubscriberExceptionEvent.class) {
//                post(new NoSubscriberEvent(this, event));
//            }
//        }
    }

    private boolean postSingleEventForEventType(Object event, PostingThreadState postingState, Class<?> eventClass) {
        CopyOnWriteArrayList<Subscription> subscriptions;
        synchronized (this) {
            subscriptions = mSubscriberByEventType.get(eventClass);
        }
        if (subscriptions != null && !subscriptions.isEmpty()) {
            for (Subscription subscription : subscriptions) {
                postingState.event = event;
                postingState.subscription = subscription;
                boolean aborted = false;
                try {
                    postToSubscription(subscription, event, postingState.isMainThread);
                    aborted = postingState.canceled;
                } finally {
                    postingState.event = null;
                    postingState.subscription = null;
                    postingState.canceled = false;
                }
                if (aborted) {
                    break;
                }
            }
            return true;
        }
        return false;
    }

    private void postToSubscription(Subscription subscription, Object event, boolean isMainThread) {
        switch (subscription.subscriberMethod.mThreadMode) {
            case POSTING:
                invokeSubscriber(subscription, event);
                break;
            default:
                throw new IllegalStateException("Unknown thread mode: " + subscription.subscriberMethod.mThreadMode);
        }
    }

    void invokeSubscriber(Subscription subscription, Object event) {
        try {
            //最终在这里通过反射拿到 方法和ThreadMode之后 invoke 执行方法，最终将方Event传到对应的 Subscribe的标记方法中
            subscription.subscriberMethod.mMethod.invoke(subscription.subscriber, event);
        } catch (InvocationTargetException e) {
            //handleSubscriberException(subscription, event, e.getCause());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unexpected exception", e);
        }
    }

    /** For ThreadLocal, much faster to set (and get multiple values). */
    final static class PostingThreadState {
        final List<Object> eventQueue = new ArrayList<>();
        boolean isPosting;
        boolean isMainThread;
        Subscription subscription;
        Object event;
        boolean canceled;
    }
}


























