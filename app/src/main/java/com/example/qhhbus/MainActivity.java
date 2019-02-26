package com.example.qhhbus;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.qhheventbus.QhhEventBusHelper;
import com.example.qhheventbus.listener.IEventListener;

public class MainActivity extends AppCompatActivity implements IEventListener {

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        QhhEventBusHelper.getInstance().register(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                mHandler.sendMessage(message);
                String msgStr = "message";
                QhhEventBusHelper.getInstance().post(msgStr);
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        QhhEventBusHelper.getInstance().unRegister(this);
    }

    @Override
    public void onMessage(Object object) {
        String msg = (String) object;
        String threadName = Thread.currentThread().getName();
        Log.d("qhh", "threadName = " + threadName + ", msg = " + msg);
    }
}
