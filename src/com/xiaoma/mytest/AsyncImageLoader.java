package com.xiaoma.mytest;

import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

/**
 * 自定义图片线程池图片下载器
 * 
 * @author Administrator
 * 
 */
public class AsyncImageLoader {

	public Map<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();
	private ExecutorService executors = Executors.newFixedThreadPool(2);
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (callback != null) {
				callback.onImageLoaded((Drawable) msg.obj);
			}
		};
	};

	public void loadImage(final String url, final ImageCallback callback) {
		if (imageCache.containsKey(url)) {
			SoftReference<Drawable> soft = imageCache.get(url);
			if (callback != null) {
				callback.onImageLoaded(soft.get());
			}
		}
		executors.execute(new Runnable() {

			@Override
			public void run() {
				final Drawable drawable = loadFromURl(url);
				imageCache.put(url, new SoftReference<Drawable>(drawable));
				handler.post(new Runnable() {

					@Override
					public void run() {

						if (callback != null) {
							callback.onImageLoaded(drawable);
						}
					}
				});
			}
		});

	}

	/**
	 * Drawable drawable = loadFromURl(url); imageCache.put(url, new
	 * SoftReference<Drawable>(drawable)); Message msg =
	 * handler.obtainMessage(); msg.obj = drawable; msg.sendToTarget();
	 * 
	 * @param url
	 * @return
	 */

	private Drawable loadFromURl(String url) {
		Drawable drawable = null;
		try {
			drawable = BitmapDrawable.createFromStream(
					new URL(url).openStream(), "img.gif");
		} catch (Exception e) {
		}
		return drawable;
	}

	/**
	 * 回调接口
	 * 
	 * @author Administrator
	 * 
	 */
	public interface ImageCallback {
		public void onImageLoaded(Drawable drawable);
	}

	ImageCallback callback;

	public void setImageCallBack(ImageCallback callback) {
		this.callback = callback;
	}

}
