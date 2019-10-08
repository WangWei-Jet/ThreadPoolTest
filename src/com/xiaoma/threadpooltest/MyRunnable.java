/*
 * FileName:  MyRunnable.java
 * CopyRight:  Belong to  <XiaoMaGuo Technologies > own 
 * Description:  <description>
 * Modify By :  XiaoMaGuo ^_^ 
 * Modify Date:   2013-10-21
 * Follow Order No.:  <Follow Order No.>
 * Modify Order No.:  <Modify Order No.>
 * Modify Content:  <modify content >
 */
package com.xiaoma.threadpooltest;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

/**
 * @TODO [The Class File Description]
 * @author XiaoMaGuo ^_^
 * @version [version-code, 2013-10-21]
 * @since [Product/module]
 */
public class MyRunnable implements Runnable
{
    
    private boolean cancleTask = false;
    
    private boolean cancleException = false;
    
    private Handler mHandler = null;
    
    public MyRunnable(Handler handler)
    {
        mHandler = handler;
    }
    
    /**
     * Overriding methods
     */
    @Override
    public void run()
    {
        Log.i("KKK", "MyRunnable  run() is executed!!! ");
        runBefore();
        if (cancleTask == false)
        {
            running();
            Log.i("KKK", "����MyRunnable run()����");
        }
        
        runAfter();
    }
    
    /**
     * <Summary Description>
     */
    private void runAfter()
    {
        Log.i("KKK", "runAfter()");
    }
    
    /**
     * <Summary Description>
     */
    private void running()
    {
        Log.i("KKK", "running()");
        try
        {
            // �����п��ܻ���쳣�����飡����
            int prog = 0;
            if (cancleTask == false && cancleException == false)
            {
                while (prog < 101)
                {
                    if ((prog > 0 || prog == 0) && prog < 70)
                    {
                        SystemClock.sleep(100);
                    }
                    else
                    {
                        SystemClock.sleep(300);
                    }
                    if (cancleTask == false)
                    {
                        mHandler.sendEmptyMessage(prog++);
                        Log.i("KKK", "���� prog++ = " + (prog));
                    }
                }
            }
        }
        catch (Exception e)
        {
            cancleException = true;
        }
    }
    
    /**
     * <Summary Description>
     */
    private void runBefore()
    {
        // TODO Auto-generated method stub
        Log.i("KKK", "runBefore()");
    }
    
    public void setCancleTaskUnit(boolean cancleTask)
    {
        this.cancleTask = cancleTask;
        Log.i("KKK", "�����ȡ������ť ������");
        // mHandler.sendEmptyMessage(0);
    }
    
}
