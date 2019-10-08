package com.xiaoma.mytest;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xiaoma.threadpooltest.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PoolTestActivity extends Activity {

	ListView listView;
	private static int order = 0;
	boolean isCancle = false;

	/** �ܹ��������񣨸���CPU��������������̵߳ĸ���,����ȡ�ĺô����ǿ������ֻ����ܵ�ס�� */
	// private static final int count =
	// Runtime.getRuntime().availableProcessors() * 3 + 2;
	private static final int count = 10;
	ArrayList<AsyTaskItem> list;
	// ����ÿ��ִֻ��һ��������̳߳�
	private static ExecutorService singleTaskService = null;
	// ����ÿ��ִ���޶�����������̳߳�
	private static ExecutorService limitedTaskService = null;
	// ���� ��������һ���Կ�ʼ���̳߳�
	private static ExecutorService allTaskService = null;
	// ����ָ��ʱ����ִ��������̳߳أ����ظ�ʹ��
	private static ExecutorService scheduledTaskService = null;
	/** ����һ������ָ��ʱ����ִ��������̳߳أ�����ظ�ִ�У���֮ͬ����ʹ�ù���ģʽ�� */
	private static ExecutorService scheduledTaskFactoryExecutor = null;

	Button button;

	private int maxIndex = 0;
	private String defaultServerIP = "59.173.2.76";
	private String defaultServerPort = "5001";
	private String uploadDeviceInfoURL = "http://59.173.2.76:5001/DeviceConfigsServer/uploadDeviceInfo";

	static {
		singleTaskService = Executors.newSingleThreadExecutor();
		limitedTaskService = Executors.newFixedThreadPool(3);
		allTaskService = Executors.newCachedThreadPool();
		scheduledTaskService = Executors.newScheduledThreadPool(3);
		System.out.println("count:" + count);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.demo);
		listView = (ListView) findViewById(R.id.task_list);
		listView.setAdapter(new MyAdapter(this, count));
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				if (position == 0) {
					/**
					 * ��ر��̳߳ط�ʽһ�����������µ�Task,�رպ����ڵȴ� ִ�е��������κ�Ӱ�죬������ִ��,�޷���ֵ!
					 */
					// allTaskService.shutdown();

					/**
					 * ��ر��̳߳ط�ʽ����Ҳ�������µ�Task����ֹͣ���ȴ�ִ�е�Task��Ҳ����˵��
					 * ִ�е�һ�����������ִ����ȥ�������ջ�����㷵��һ�����ڵȴ�ִ�е��̳߳عر�ȴû�б�ִ�е�Task���ϣ�
					 */
					if (allTaskService != null) {
						List<Runnable> unExecRunn = allTaskService
								.shutdownNow();

						for (Runnable r : unExecRunn) {
							Log.i("KKK", "δִ�е�������Ϣ��=" + unExecRunn.toString());
						}
						Log.i("KKK",
								"Is shutdown ? = "
										+ String.valueOf(allTaskService
												.isShutdown()));
					}
					allTaskService = null;
				}
				if (position == 1) {

				} else {

				}
			}
		});
	}

	class MyAdapter extends BaseAdapter {
		Context context;
		LayoutInflater inflater;
		int taskCount;

		public MyAdapter(Context context, int taskCount) {
			this.context = context;
			this.taskCount = taskCount;
			inflater = LayoutInflater.from(context);
			list = new ArrayList<PoolTestActivity.AsyTaskItem>(taskCount);
			System.out.println("taskCount:" + taskCount + "\nlist size:"
					+ list.size());
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return taskCount;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			System.out.println("get item:" + position);
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			System.out.println("getview involked.position:" + position);
			if (convertView == null) {
				System.out.println("�½�view:" + position);
				convertView = inflater.inflate(R.layout.list_view_item, parent,
						false);
				// convertView = inflater.inflate(R.layout.list_view_item,
				// parent,
				// false);
				// if (position == 1) {

				MyItem item = new MyItem(context, convertView, 1);
				AsyTaskItem asyitem = new AsyTaskItem(item);
				/**
				 * ������������ִ��Ч����һ��,�α��ʲ���
				 * */
				// asyitem.execute();
				// asyitem.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
				/**
				 * һ��һ��ִ�����ǵ�����,Ч���밴˳��ִ����һ����(AsyncTask.SERIAL_EXECUTOR)
				 * */
				// asyitem.executeOnExecutor(singleTaskService);

				/**
				 * ������ָ���ĸ�����ִ��������̳߳�
				 * */
				// asyitem.executeOnExecutor(limitedTaskService);
				/**
				 * ���޶�ָ���������̳߳أ�Ҳ����˵������������˼���������ȫ��ͬһʱ�俪ʼִ�У� �������ֻ��ܵ����ܲ��� // *
				 */
				asyitem.executeOnExecutor(allTaskService);
				/**
				 * ����һ������ָ��ʱ����ִ��������̳߳أ�����ظ�ִ��
				 * */
				// asyitem.executeOnExecutor(scheduledTaskService);
				list.add(asyitem);
				// }
			}
			// if (position > maxIndex) {}

			return convertView;
		}

	}

	class AsyTaskItem extends AsyncTask<Void, Integer, Void> {

		MyItem item;

		String id;

		public AsyTaskItem(MyItem item) {
			this.item = item;
			if (order < count || order == count) {
				id = "ִ��:" + (++order);
			} else {
				order = 0;
				id = "ִ��:" + (++order);
			}
			System.out.println("�½�����:" + order);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			item.setTitle(id);
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			if (!isCancelled() && isCancle == false) {
				int pro = 0;
				// while (pro < 101) {
				// if (pro < 70 && (pro > 0 || pro == 0)) {
				// SystemClock.sleep(100);
				// } else {
				// SystemClock.sleep(800);
				// }
				// publishProgress(pro);
				// pro++;
				// }
				uploadUnsupportPhone("http://192.168.1.102:8080/DeviceConfigsServer/uploadDeviceInfo");
			}
			System.out.println("����ִ�����");
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			item.setProgress(values[0]);
		}

	}

	/**
	 * �Զ���һ����ʾ��item
	 * 
	 * @author Administrator
	 * 
	 */
	class MyItem extends LinearLayout {
		public MyItem(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}

		public MyItem(Context context, AttributeSet attrs) {
			this(context, attrs, 0);
		}

		public MyItem(Context context) {
			this(context, null);
		}

		public MyItem(Context context, View def, int s) {
			this(context);
			this.rootView = def;

		}

		private View rootView = null;
		private TextView mTitle;

		private ProgressBar mProgress;

		public void setTitle(String title) {

			if (mTitle == null) {
				mTitle = (TextView) rootView.findViewById(R.id.task_name);
			}
			mTitle.setText(title);
		}

		public void setProgress(int pro) {
			if (mProgress == null) {
				mProgress = (ProgressBar) rootView
						.findViewById(R.id.task_progress);
			}
			mProgress.setProgress(pro);
		}
	}

	private void hideSoftInputMode(EditText editText) {
		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(editText.getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
	}

	public String uploadUnsupportPhone(String targetUrl) {
		if (targetUrl != null) {
			uploadDeviceInfoURL = targetUrl;
		} else {
			uploadDeviceInfoURL = "http://" + defaultServerIP + ":"
					+ defaultServerPort
					+ "/DeviceConfigsServer/uploadUnsupportPhone";
		}

		String outputString = "";
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss",
				Locale.getDefault());
		String sn = format.format(new Date());
		// Timestamp recordtime = new Timestamp(System.currentTimeMillis());
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 1500);
		httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				5000);
		try {
			// String baseURL =
			// "http://10.8.50.163:8080/DeviceConfigsServer/uploadUnsupportPhone";

			// ʹ��GET������������,��Ҫ�Ѳ�������URL���棬�ã����ӣ�����֮����&�ָ�
			// String url = uploadDeviceInfoURL + "?sn=" + sn + "&recordtime="
			// + sn;

			// ƴװJson����
			Timestamp testtime = new Timestamp(System.currentTimeMillis());
			JSONObject total = new JSONObject();
			JSONArray array = new JSONArray();
			JSONObject itemOne = new JSONObject();
			itemOne.put("sn", sn);
			itemOne.put("recordtime", new Timestamp(System.currentTimeMillis()
					- 1000 * 60 * 60 * 24));
			itemOne.put("dismantledState", true);
			itemOne.put("note", "test");
			JSONObject itemTwo = new JSONObject();
			itemTwo.put("sn", sn);
			itemTwo.put("recordtime", testtime);
			itemTwo.put("dismantledState", true);
			itemTwo.put("note", "test");
			array.put(itemOne);
			array.put(itemTwo);
			total.put("total", array);

			// ********************http get******************************
			// JSONObject jsonObj = new JSONObject();
			// jsonObj.put("sn", sn);
			// jsonObj.put("recordtime", sn);
			// Log.d(tag, "Json data:" + jsonObj.toString());
			// JSONObject jsonResult = new JSONObject(jsonObj.toString());
			// String requestSN = jsonResult.getString("sn");
			// String requestTime = jsonResult.getString("recordtime");
			// // Timestamp requestTestTime = Timestamp.valueOf((String)
			// jsonResult
			// // .get("testtime"));
			// Log.d(tag, "request sn:" + requestSN + "\nrequest time:"
			// + requestTime);
			// String request = jsonObj.toString().replace("\"", "%22")
			// .replace("{", "%7b").replace("}", "%7d");
			// String url = uploadDeviceInfoURL + "?requestData=" + request;
			// // �����������
			// HttpGet httpGet = new HttpGet(url);
			// ********************http get********************************

			HttpPost httpRequest = new HttpPost(uploadDeviceInfoURL);
			HttpEntity requestBodies = new StringEntity(total.toString());
			httpRequest.setEntity(requestBodies);
			// httpRequest.setHeader("content-type",
			// "application/x-www-form-urlencoded");

			// ResponseHandler
			ResponseHandler<String> responseHandler = new BasicResponseHandler();

			// outputString = httpclient.execute(httpGet, responseHandler);
			outputString = httpclient.execute(httpRequest, responseHandler);
			outputString = new String(outputString.getBytes("ISO-8859-1"),
					"utf-8"); // �����������
			Log.d("xx", "Upload Unsupport Phone��" + outputString);
			Log.d("HttpClientConnector", "connect server success.");
		} catch (Exception e) {
			Log.d("HttpClientConnector", "connect server fail.");
			e.printStackTrace();
		} finally {
			// ��������ҪHttpClientʵ��ʱ,�ر����ӹ�������ȷ���ͷ�����ռ�õ�ϵͳ��Դ
			httpclient.getConnectionManager().shutdown();
		}
		return outputString;
	}
}
