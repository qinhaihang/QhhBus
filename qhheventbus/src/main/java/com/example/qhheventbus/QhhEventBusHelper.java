package com.example.qhheventbus;

import android.os.Handler;
import android.os.Looper;

import com.example.qhheventbus.listener.IEventListener;

import java.util.ArrayList;
import java.util.List;

public class QhhEventBusHelper {

    private static QhhEventBusHelper instance;

    private List<IEventListener> mListerners = new ArrayList<>();

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public QhhEventBusHelper() {

    }

    public static QhhEventBusHelper getInstance() {
        if (instance == null) {
            synchronized (QhhEventBusHelper.class) {
                if (instance == null) {
                    instance = new QhhEventBusHelper();
                }
            }
        }
        return instance;
    }

    public void register(IEventListener eventListner){
        mListerners.add(eventListner);
    }

    public void unRegister(IEventListener eventListner){
        mListerners.remove(eventListner);
    }

    public void post(Object object){
        for (IEventListener listener : mListerners) {
            listener.onMessage(object);
        }
    }

    public void postMainThread(final Object object){
        for (final IEventListener listener : mListerners) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onMessage(object);
                }
            });
        }
    }
}
