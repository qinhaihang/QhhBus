package com.example.qhheventbus;

/**
 * @author qinhaihang_vendor
 * @version $Rev$
 * @time 2019/3/5 19:20
 * @des
 * @packgename com.example.qhheventbus
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes
 */
public class Subscription {
    final Object subscriber;
    final SubscriberMethod subscriberMethod;

    volatile boolean active;

    Subscription(Object subscriber, SubscriberMethod subscriberMethod) {
        this.subscriber = subscriber;
        this.subscriberMethod = subscriberMethod;
        active = true;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Subscription) {
            Subscription otherSubscription = (Subscription) other;
            return subscriber == otherSubscription.subscriber
                    && subscriberMethod.equals(otherSubscription.subscriberMethod);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return subscriber.hashCode() + subscriberMethod.methodString.hashCode();
    }

}
