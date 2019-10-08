/*
 * FileName:  MyRunnableActivity.java
 * CopyRight:  Belong to  <XiaoMaGuo Technologies > own 
 * Description:  <description>
 * Modify By :  XiaoMaGuo ^_^ 
 * Modify Date:   2013-10-21
 * Follow Order No.:  <Follow Order No.>
 * Modify Order No.:  <Modify Order No.>
 * Modify Content:  <modify content >
 */
package com.xiaoma.threadpooltest;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * @TODO [�̳߳ؿ��� ]
 * @author XiaoMaGuo ^_^
 * @version [version-code, 2013-10-22]
 * @since [Product/module]
 */
public class MyRunnableActivity extends Activity implements OnClickListener
{
    
    /** ����ִ�ж��� */
    private ConcurrentLinkedQueue<MyRunnable> taskQueue = null;
    
    /**
     * ���ڵȴ�ִ�л��Ѿ���ɵ��������
     * 
     * ��ע��Future�࣬һ�����ڴ洢�첽����ִ�еĽ�������磺�ж��Ƿ�ȡ�����Ƿ����ȡ�����Ƿ�����ִ�С��Ƿ��Ѿ���ɵ�
     * 
     * */
    private ConcurrentMap<Future, MyRunnable> taskMap = null;
    
    /**
     * ����һ�������ƴ�С���̳߳� ������Ҫ�����ºô� 1���Թ�����޽���з�ʽ��������Щ�߳�. 2��ִ��Ч�ʸߡ� 3��������㣬�ڴ���� nThreads �̻߳ᴦ�ڴ�������Ļ״̬
     * 4������ڹر�ǰ��ִ���ڼ�����ʧ�ܶ������κ��߳���ֹ����ôһ�����߳̽�������ִ�к��������������Ҫ����
     * 
     * */
    private ExecutorService mES = null;
    
    /** �ڴ�����ʹ��ͬ����ʱʹ������lock���󼴿ɣ��ٷ��Ƽ��ģ����Ƽ�ֱ��ʹ��MyRunnableActivity.this���͵�,������ϸ��һ��/framework/app��������һ����Ŀ */
    private Object lock = new Object();
    
    /** ���ѱ�־���Ƿ����̳߳ع��� */
    private boolean isNotify = true;
    
    /** �̳߳��Ƿ�������״̬(��:�Ƿ��ͷ�!) */
    private boolean isRuning = true;
    
    /** ������� */
    private ProgressBar pb = null;
    
    /** �ô�Handler���������ǵ�UI */
    private Handler mHandler = null;
    
    /**
     * Overriding methods
     * 
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_runnable_main);
        init();
    }
    
    public void init()
    {
        pb = (ProgressBar)findViewById(R.id.progressBar1);
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
        findViewById(R.id.button5).setOnClickListener(this);
        taskQueue = new ConcurrentLinkedQueue<MyRunnable>();
        taskMap = new ConcurrentHashMap<Future, MyRunnable>();
        if (mES == null)
        {
            mES = Executors.newCachedThreadPool();
        }
        
        // ���ڸ���ProgressBar������
        mHandler = new Handler()
        {
            /**
             * Overriding methods
             * 
             * @param msg
             */
            @Override
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);
                pb.setProgress(msg.what);
            }
            
        };
        
    }
    
    /**
     * Overriding methods
     * 
     * @param v
     */
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button1:
                start();
                break;
            case R.id.button2:
                stop();
                break;
            case R.id.button3:
                reload(new MyRunnable(mHandler));
                break;
            case R.id.button4:
                release();
                break;
            case R.id.button5:
                addTask(new MyRunnable(mHandler));
                break;
            
            default:
                break;
        }
    }
    
    /**
     * <Summary Description>
     */
    private void addTask(final MyRunnable mr)
    {
        
        mHandler.sendEmptyMessage(0);
        
        if (mES == null)
        {
            mES = Executors.newCachedThreadPool();
            notifyWork();
        }
        
        if (taskQueue == null)
        {
            taskQueue = new ConcurrentLinkedQueue<MyRunnable>();
        }
        
        if (taskMap == null)
        {
            taskMap = new ConcurrentHashMap<Future, MyRunnable>();
        }
        
        mES.execute(new Runnable()
        {
            
            @Override
            public void run()
            {
                /**
                 * ����һ��Runnable����������� ����ط�����һ��,offer��add����,������,Ч����һ��,û����,�ٷ��Ľ�������: 1 offer : Inserts the specified
                 * element at the tail of this queue. As the queue is unbounded, this method will never return
                 * {@code false}. 2 add: Inserts the specified element at the tail of this queue. As the queue is
                 * unbounded, this method will never throw {@link IllegalStateException} or return {@code false}.
                 * 
                 * 
                 * */
                taskQueue.offer(mr);
                // taskQueue.add(mr);
                notifyWork();
            }
        });
        
        Toast.makeText(MyRunnableActivity.this, "�����һ���������̳߳��� ��", 0).show();
    }
    
    /**
     * <Summary Description>
     */
    private void release()
    {
        Toast.makeText(MyRunnableActivity.this, "�ͷ�����ռ�õ���Դ��", 0).show();
        
        /** ��ProgressBar������Ϊ0 */
        mHandler.sendEmptyMessage(0);
        isRuning = false;
        
        Iterator iter = taskMap.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry<Future, MyRunnable> entry = (Map.Entry<Future, MyRunnable>)iter.next();
            Future result = entry.getKey();
            if (result == null)
            {
                continue;
            }
            result.cancel(true);
            taskMap.remove(result);
        }
        if (null != mES)
        {
            mES.shutdown();
        }
        
        mES = null;
        taskMap = null;
        taskQueue = null;
        
    }
    
    /**
     * <Summary Description>
     */
    private void reload(final MyRunnable mr)
    {
        mHandler.sendEmptyMessage(0);
        if (mES == null)
        {
            mES = Executors.newCachedThreadPool();
            notifyWork();
        }
        
        if (taskQueue == null)
        {
            taskQueue = new ConcurrentLinkedQueue<MyRunnable>();
        }
        
        if (taskMap == null)
        {
            taskMap = new ConcurrentHashMap<Future, MyRunnable>();
        }
        
        mES.execute(new Runnable()
        {
            
            @Override
            public void run()
            {
                /** ����һ��Runnable����������� */
                taskQueue.offer(mr);
                // taskQueue.add(mr);
                notifyWork();
            }
        });
        
        mES.execute(new Runnable()
        {
            @Override
            public void run()
            {
                if (isRuning)
                {
                    MyRunnable myRunnable = null;
                    synchronized (lock)
                    {
                        myRunnable = taskQueue.poll(); // ���̶߳�����ȡ��һ��Runnable������ִ�У�����˶���Ϊ�գ������poll()�����᷵��null
                        if (myRunnable == null)
                        {
                            isNotify = true;
                        }
                    }
                    
                    if (myRunnable != null)
                    {
                        taskMap.put(mES.submit(myRunnable), myRunnable);
                    }
                }
            }
        });
    }
    
    /**
     * <Summary Description>
     */
    private void stop()
    {
        
        Toast.makeText(MyRunnableActivity.this, "�����ѱ�ȡ����", 0).show();
        
        for (MyRunnable runnable : taskMap.values())
        {
            runnable.setCancleTaskUnit(true);
        }
    }
    
    /**
     * <Summary Description>
     */
    private void start()
    {
        
        if (mES == null || taskQueue == null || taskMap == null)
        {
            Log.i("KKK", "ĳ��Դ�ǲ����Ѿ����ͷ��ˣ�");
            return;
        }
        mES.execute(new Runnable()
        {
            @Override
            public void run()
            {
                if (isRuning)
                {
                    MyRunnable myRunnable = null;
                    synchronized (lock)
                    {
                        myRunnable = taskQueue.poll(); // ���̶߳�����ȡ��һ��Runnable������ִ�У�����˶���Ϊ�գ������poll()�����᷵��null
                        if (myRunnable == null)
                        {
                            isNotify = true;
                            // try
                            // {
                            // myRunnable.wait(500);
                            // }
                            // catch (InterruptedException e)
                            // {
                            // e.printStackTrace();
                            // }
                        }
                    }
                    
                    if (myRunnable != null)
                    {
                        taskMap.put(mES.submit(myRunnable), myRunnable);
                    }
                }
                
            }
        });
    }
    
    private void notifyWork()
    {
        synchronized (lock)
        {
            if (isNotify)
            {
                lock.notifyAll();
                isNotify = !isNotify;
            }
        }
    }
}
