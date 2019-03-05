package com.example.qhheventbus;

import java.lang.reflect.Method;

/**
 * @author qinhaihang_vendor
 * @version $Rev$
 * @time 2019/3/5 16:07
 * @des 订阅者的method信息
 * @packgename com.example.qhheventbus
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes
 */
public class SubscriberMethod {
    final Method mMethod;
    final ThreadMode mThreadMode;
    final Class<?> mParameterType;
    /** Used for efficient comparison */
    String methodString;

    public SubscriberMethod(Method method, ThreadMode threadMode,Class<?> parameterType) {
        mMethod = method;
        mThreadMode = threadMode;
        mParameterType = parameterType;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other instanceof SubscriberMethod) {
            checkMethodString();
            SubscriberMethod otherSubscriberMethod = (SubscriberMethod)other;
            otherSubscriberMethod.checkMethodString();
            // Don't use method.equals because of http://code.google.com/p/android/issues/detail?id=7811#c6
            return methodString.equals(otherSubscriberMethod.methodString);
        } else {
            return false;
        }
    }

    private synchronized void checkMethodString() {
        if (methodString == null) {
            // Method.toString has more overhead, just take relevant parts of the method
            StringBuilder builder = new StringBuilder(64);
            builder.append(mMethod.getDeclaringClass().getName());
            builder.append('#').append(mMethod.getName());
            builder.append('(').append(mParameterType.getName());
            methodString = builder.toString();
        }
    }

    @Override
    public int hashCode() {
        return mMethod.hashCode();
    }
}
