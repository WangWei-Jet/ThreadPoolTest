/*
 * FileName:  Main.java
 * CopyRight:  Belong to  <XiaoMaGuo Technologies > own 
 * Description:  <description>
 * Modify By :  XiaoMaGuo ^_^ 
 * Modify Date:   2013-10-15
 * Follow Order No.:  <Follow Order No.>
 * Modify Order No.:  <Modify Order No.>
 * Modify Content:  <modify content >
 */
package com.xiaoma.threadpooltest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @TODO [The Class File Description]
 * @author XiaoMaGuo ^_^
 * @version [version-code, 2013-10-15]
 * @since [Product/module]
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Main extends Activity {
	private static int order = 0;

	/** 总共多少任务（根据CPU个数决定创建活动线程的个数,这样取的好处就是可以让手机承受得住） */
	// private static final int count =
	// Runtime.getRuntime().availableProcessors() * 3 + 2;

	/** 总共多少任务（我是在模拟器里面跑的，为了效果明显，所以写死了为10个，如果在手机上的话，推荐使用上面的那个count） */
	private static final int count = 4;

	/** 每次只执行一个任务的线程池 */
	private static ExecutorService singleTaskExecutor = null;

	/** 每次执行限定个数个任务的线程池 */
	private static ExecutorService limitedTaskExecutor = null;

	/** 所有任务都一次性开始的线程池 */
	private static ExecutorService allTaskExecutor = null;

	/** 创建一个可在指定时间里执行任务的线程池，亦可重复执行 */
	private static ExecutorService scheduledTaskExecutor = null;

	/** 创建一个可在指定时间里执行任务的线程池，亦可重复执行（不同之处：使用工程模式） */
	private static ExecutorService scheduledTaskFactoryExecutor = null;

	private List<AsyncTaskTest> mTaskList = null;

	/** 任务是否被取消 */
	private boolean isCancled = false;

	/** 是否点击并取消任务标示符 */
	private boolean isClick = false;

	/** 线程工厂初始化方式一 */
	ThreadFactory tf = Executors.defaultThreadFactory();

	/** 线程工厂初始化方式二 */
	private static class ThreadFactoryTest implements ThreadFactory {

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setName("XiaoMaGuo_ThreadFactory");
			thread.setDaemon(true); // 将用户线程变成守护线程,默认false
			return thread;
		}
	}

	static {
		singleTaskExecutor = Executors.newSingleThreadExecutor();// 每次只执行一个线程任务的线程池
		limitedTaskExecutor = Executors.newFixedThreadPool(3);// 限制线程池大小为7的线程池
		allTaskExecutor = Executors.newCachedThreadPool(); // 一个没有限制最大线程数的线程池
		scheduledTaskExecutor = Executors.newScheduledThreadPool(3);// 一个可以按指定时间可周期性的执行的线程池
		scheduledTaskFactoryExecutor = Executors.newFixedThreadPool(3,
				new ThreadFactoryTest());// 按指定工厂模式来执行的线程池
		scheduledTaskFactoryExecutor.submit(new Runnable() {

			@Override
			public void run() {
				Log.i("KKK", "This is the ThreadFactory Test  submit Run! ! ! ");
			}
		});
	};

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.demo);
		final ListView taskList = (ListView) findViewById(R.id.task_list);
		taskList.setAdapter(new AsyncTaskAdapter(getApplication(), count));
		taskList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) // 以第一项为例，来测试关闭线程池
				{
					/**
					 * 会关闭线程池方式一：但不接收新的Task,关闭后，正在等待 执行的任务不受任何影响，会正常执行,无返回值!
					 */
					// allTaskExecutor.shutdown();

					/**
					 * 会关闭线程池方式二：也不接收新的Task，并停止正等待执行的Task（也就是说，
					 * 执行到一半的任务将正常执行下去），最终还会给你返回一个正在等待执行但线程池关闭却没有被执行的Task集合！
					 */
					if (allTaskExecutor == null) {
						List<Runnable> unExecRunn = allTaskExecutor
								.shutdownNow();

						for (Runnable r : unExecRunn) {
							Log.i("KKK", "未执行的任务信息：=" + unExecRunn.toString());
						}
						Log.i("KKK",
								"Is shutdown ? = "
										+ String.valueOf(allTaskExecutor
												.isShutdown()));
					}
					allTaskExecutor = null;
				}

				// 以第二项为例来测试是否取消执行的任务
				AsyncTaskTest sat = mTaskList.get(1);
				if (position == 1) {
					if (!isClick) {
						// 当任务正在执行的时候选择取消任务，任务取消需要一个过程，任务取消成功后调用onCancelled回调，此时任务才是完整地取消，继而执行下一个任务
						sat.cancel(true);
						Log.e("tag", "cancel execute over");
						isCancled = true;
						isClick = !isClick;
					} else {
						sat.cancel(false);
						isCancled = false;
						// isClick = false;
						isClick = !isClick;
						if (null != sat
								&& sat.getStatus() == AsyncTask.Status.RUNNING) {
							if (sat.isCancelled()) {
								sat = new AsyncTaskTest(sat.mTaskItem);
							} else {
								Toast.makeText(Main.this,
										"A task is already running, try later",
										Toast.LENGTH_SHORT).show();
							}
						}

						/**
						 * 由于上面测试关闭，在不重新生成allTaskExecutor的同时，会报异常（没有可以使用的线程池，
						 * 故此处重新生成线程池对象）
						 */
						if (allTaskExecutor == null) {
							allTaskExecutor = Executors.newCachedThreadPool();
						}
						sat.executeOnExecutor(allTaskExecutor); // The task is
																// already
																// running(这也是个异常哦，小心使用！
																// )
					}
				} else {
					sat.cancel(false);
					isCancled = false;
					// sat.execute(sat.mTaskItem);
					// sat.executeOnExecutor(allTaskExecutor);
				}

			}
		});
	}

	/**
	 * @TODO [ListView Item的条目适配器]
	 * @author XiaoMaGuo ^_^
	 * @version [version-code, 2013-10-22]
	 * @since [Product/module]
	 */
	private class AsyncTaskAdapter extends BaseAdapter {
		private Context mContext;

		private LayoutInflater mFactory;

		private int mTaskCount;

		public AsyncTaskAdapter(Context context, int taskCount) {
			mContext = context;
			mFactory = LayoutInflater.from(mContext);
			mTaskCount = taskCount;
			mTaskList = new ArrayList<AsyncTaskTest>(taskCount);

			Log.i("tag", "AsyncTaskAdapter " + mTaskList.size());
		}

		@Override
		public int getCount() {
			Log.i("tag", "getCount " + mTaskList.size());
			return mTaskCount;
		}

		@Override
		public Object getItem(int position) {
			Log.i("tag", "getItem " + mTaskList.size());
			return mTaskList.get(position);
		}

		@Override
		public long getItemId(int position) {
			Log.i("tag", "getItemId " + mTaskList.size());
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mFactory.inflate(R.layout.list_view_item, null);
				Log.e(AsyncTaskAdapter.class.getSimpleName(), "创建任务");
				AsyncTaskTest task = new AsyncTaskTest((MyListItem) convertView);

				/**
				 * 下面两种任务执行效果都一样,形变质不变
				 * */
				// task.execute();
				// task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

				/**
				 * 下面的方式在小于API
				 * 11级时效果是一样的，但在高版本中的稍微有点不同,可以看以下AsyncTask核心变量的定义就知道了使用如下
				 * 方式时，系统会默认的采用五个一组，五个一组的方式来执行我们的任务，定义在：AsyncTask.class中，private
				 * static final int CORE_POOL_SIZE = 5;
				 * */
				// use AsyncTask#THREAD_POOL_EXECUTOR is the same to older
				// version #execute() (less than API 11)
				// but different from newer version of #execute()
				// task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

				/**
				 * 一个一个执行我们的任务,效果与按顺序执行是一样的(AsyncTask.SERIAL_EXECUTOR)
				 * */
				Log.e(AsyncTaskAdapter.class.getSimpleName(), "通知任务开始执行");
				task.executeOnExecutor(singleTaskExecutor);

				/**
				 * 按我们指定的个数来执行任务的线程池
				 * */
				// task.executeOnExecutor(limitedTaskExecutor);

				/**
				 * 不限定指定个数的线程池，也就是说：你往里面放了几个任务，他全部同一时间开始执行， 不管你手机受得了受不了 // *
				 */
				// task.executeOnExecutor(allTaskExecutor);

				/**
				 * 创建一个可在指定时间里执行任务的线程池，亦可重复执行
				 * */
				// task.executeOnExecutor(scheduledTaskExecutor);

				/**
				 * 创建一个按指定工厂模式来执行任务的线程池,可能比较正规,但也不常用
				 */
				// task.executeOnExecutor(scheduledTaskFactoryExecutor);
				mTaskList.add(task);
			}
			Log.i("tag", "getView " + mTaskList.size());
			return convertView;
		}
	}

	class AsyncTaskTest extends AsyncTask<Void, Integer, Void> {
		private MyListItem mTaskItem;

		private String id;

		@Override
		protected void onCancelled(Void result) {
			// TODO Auto-generated method stub
			Log.e("tag", "任务取消成功");
			super.onCancelled(result);
		}

		private AsyncTaskTest(MyListItem item) {
			mTaskItem = item;
			if (order < count || order == count) {
				id = "执行:" + String.valueOf(++order);
			} else {
				order = 0;
				id = "执行:" + String.valueOf(++order);
			}
		}

		@Override
		protected void onPreExecute() {
			mTaskItem.setTitle(id);
		}

		/**
		 * Overriding methods
		 */
		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected Void doInBackground(Void... params) {
			Log.e(AsyncTaskTest.class.getSimpleName(), "任务开始执行");
			if (!isCancelled() && isCancled == false) // 这个地方很关键，如果不设置标志位的话，直接setCancel（true）是无效的
			{
				int prog = 0;

				/**
				 * 下面的while中，小马写了个分支用来做个假象（任务东西刚开始下载的时候，速度快，快下载完成的时候就突然间慢了下来的效果，
				 * 大家可以想象一下，类似
				 * ：PP手机助手、91手机助手中或其它手机应用中，几乎都有这个假象，开始快，结束时就下载变慢了，讲白了
				 * 就是开发的人不想让你在下载到大于一半的时候，也就是快下载完的时候去点取消，你那样得多浪费
				 * ！所以造个假象，让你不想去取消而已）
				 */
				while (prog < 101) {

					if ((prog > 0 || prog == 0) && prog < 70) // 小于70%时，加快进度条更新
					{
						SystemClock.sleep(100);
					} else
					// 大于70%时，减慢进度条更新
					{
						SystemClock.sleep(300);
					}

					publishProgress(prog); // 更新进度条
					prog++;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			mTaskItem.setProgress(values[0]); // 设置进度
		}
	}
}

/**
 * @TODO [一个简单的自定义 ListView Item]
 * @author XiaoMaGuo ^_^
 * @version [version-code, 2013-10-22]
 * @since [Product/module]
 */
class MyListItem extends LinearLayout {
	private TextView mTitle;

	private ProgressBar mProgress;

	public MyListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyListItem(Context context) {
		super(context);
	}

	public void setTitle(String title) {
		if (mTitle == null) {
			mTitle = (TextView) findViewById(R.id.task_name);
		}
		mTitle.setText(title);
	}

	public void setProgress(int prog) {
		if (mProgress == null) {
			mProgress = (ProgressBar) findViewById(R.id.task_progress);
		}
		mProgress.setProgress(prog);
	}
}
