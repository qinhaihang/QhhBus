package com.example.qhhbus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.qhheventbus.QhhEventBusHelper;
import com.example.qhheventbus.listener.IEventListener;

public class MainActivity extends AppCompatActivity implements IEventListener {

    private TextView mHello;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHello = findViewById(R.id.tv_hello);
        mHello.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        QhhEventBusHelper.getInstance().postMainThread("hello");
                    }
                }).start();
            }
        });

        QhhEventBusHelper.getInstance().register(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //QhhEventBusHelper.getInstance().unRegister(this);
    }

    @Override
    public void onMessage(Object object) {
        String msg = (String) object;
        String threadName = Thread.currentThread().getName();
        Log.d("qhhqhh", "threadName = " + threadName + ", msg = " + msg);
        mHello.setText(msg);
    }
}
