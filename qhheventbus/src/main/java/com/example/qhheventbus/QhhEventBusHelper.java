package com.example.qhheventbus;

import com.example.qhheventbus.listener.IEventListener;

import java.util.ArrayList;
import java.util.List;

public class QhhEventBusHelper {

    private static QhhEventBusHelper mEventBus;
    private List<IEventListener> mListerners = new ArrayList<>();

    public QhhEventBusHelper() {

    }

    public static QhhEventBusHelper getInstance(){
        if(null == mEventBus){
            return new QhhEventBusHelper();
        }
        return mEventBus;
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
}
