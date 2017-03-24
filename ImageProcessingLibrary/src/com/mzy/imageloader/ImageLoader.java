package com.mzy.imageloader;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

import com.mzy.imageloader.ImageSizeUtil.ImageSize;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;


/** 
 * @ClassName: ImageLoader 。。。图片加载类。。。。从网络。。和本地
 * @Description:  图片加载器
 * @author WANDERYUREN
 */
public class ImageLoader {
	private static final String TAG = "ImageLoader";
	/**
	 * 单例对象。。。。。包含一个LruCache用于管理我们的图片；
	 */
	private static ImageLoader mInstance;
	/**
	 * 消息队列。。。。。我们每来一次加载图片的请求，我们会封装成Task存入我们的TaskQueue;
	 */
	private LinkedBlockingDeque<Runnable> mTaskQueue;

	/**
	 * 图片缓存的核心对象。。。。。。
	 */
	private LruCache<String, Bitmap> mLruCache;
	/**
	 * 线程池。。。。。。包含一个后台线程，这个线程在第一次初始化实例的时候启动，
	 * 然后会一直在后台运行；任务呢？还记得我们有个任务队列么，有队列存任务，得有人干活呀；
	 * 所以，当每来一次加载图片请求的时候，我们同时发一个消息到后台线程，后台线程去使用线程池去TaskQueue去取一个任务执行；
	 */
	private ExecutorService mThreadPool;
	private static final int DEAFULT_THREAD_COUNT = 1;
	/**
	 * 队列的调度方式。。。。。。后台线程去TaskQueue去取一个任务，这个任务不是随便取的，
	 * 有策略可以选择，一个是FIFO，一个是LIFO，我倾向于后者
	 */
	private Type mType = Type.LIFO;

	/**
	 * 后台轮询线程
	 */
	private Thread mPoolThread;
	/**
	 * UI线程中的Handler
	 */
	private Handler mUIHandler;

	/**   信号控制*/ 
	private Semaphore mSemaphoreThreadPool;

	private Context mContext;

	public enum Type
	{
		FIFO, LIFO;
	}
	public static ImageLoader getInstance(Context context)
	{
		if (mInstance == null)
		{
			/**synchronized：同步的。。Java语言的关键字，可用来给对象和方法或者代码块加锁，
			*当它锁定一个方法或者一个代码块的时候，同一时刻最多只有一个线程执行这段代码。当两个并发线程
			*访问同一个对象object中的这个加锁同步代码块时，一个时间内只能有一个线程得到执行。
			*另一个线程必须等待当前线程执行完这个代码块以后才能执行该代码块。然而，当一个线程访问object
			*的一个加锁代码块时，另一个线程仍然可以访问该object中的非加锁代码块。
			*/
			synchronized (ImageLoader.class)
			{
				if (mInstance == null)
				{
					mInstance = new ImageLoader(DEAFULT_THREAD_COUNT, Type.LIFO,context);
				}
			}
		}
		return mInstance;
	}
	private ImageLoader(int threadCount, Type type,Context context)
	{
		init(threadCount, type,context);
	}
	public static ImageLoader getInstance(int threadCount, Type type,Context context)
	{
		if (mInstance == null)
		{
			synchronized (ImageLoader.class)
			{
				if (mInstance == null)
				{

					mInstance = new ImageLoader(threadCount, type,context);
				}
			}
		}
		return mInstance;
	}

	/**
	 * 初始化
	 * 
	 * @param threadCount
	 * @param type
	 */
	private void init(int threadCount, Type type,Context context)
	{
		// 获取我们应用的最大可用内存
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheMemory = maxMemory / 8;
		//注意此处要获取全局Context，避免引用Activity造成资源无法释放
		mContext=context.getApplicationContext();
		mLruCache = new LruCache<String, Bitmap>(cacheMemory){
			@Override
			protected int sizeOf(String key, Bitmap value)
			{
				//				return value.getAllocationByteCount();
				return value.getRowBytes() * value.getHeight(); //旧版本方法
			}

		};

		// 创建线程池
		mThreadPool = Executors.newFixedThreadPool(threadCount);
		mType = type;
		//初始化了一个mPoolThreadHandler用于发送消息到此线程；
		mSemaphoreThreadPool = new Semaphore(threadCount,true);
		mTaskQueue = new LinkedBlockingDeque<Runnable>();	
		initBackThread();
	}
	/**
	 * 初始化后台轮询线程
	 */
	private void initBackThread()
	{
		// 后台轮询线程
		mPoolThread = new Thread()
		{
			@Override
			public void run()
			{
				while(true){
					try {
						// 获取一个信号，若无可用信号，阻塞线程
						mSemaphoreThreadPool.acquire();
						// 线程池去取出一个任务进行执行，当任务队列为空时，阻塞线程
						Runnable runnable=getTask();
						//使用线程池执行任务
						mThreadPool.execute(runnable);	
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}


			};
		};

		mPoolThread.start();
	}

	/**单张图片处理加载函数
	 * 根据path为imageview设置图片
	 * 
	 * @param path 图片路径
	 * @param imageView 加载图片的ImageView
	 * @param options 图片加载参数
	 * @throws InterruptedException 
	 */
	public void loadImage( String path,  ImageView imageView, DisplayImageOptions options) 
	{
		options.displayer.display(options.imageResOnLoading, imageView);
		if (mUIHandler == null)
		{
			//这个mUIHandler用户更新我们的imageview，因为这个方法肯定是主线程调用的
			mUIHandler = new Handler(mContext.getMainLooper())
			{
				public void handleMessage(Message msg)
				{
					// 获取得到图片，为imageview回调设置图片
					ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
					Bitmap bm = holder.bitmap;
					ImageView view = holder.imageView;
					DisplayImageOptions options=holder.options;
					if(bm!=null){			
						options.displayer.display(bm, view);
					}
					else {
						options.displayer.display(options.imageResOnFail, view);
					}
				};
			};
		}

		// 根据path在缓存中获取bitmap。。。；如果找到那么直接去设置我们的图片
		Bitmap bm = getBitmapFromLruCache(path);

		if (bm != null)
		{
			refreashBitmap(path, imageView, bm,options);
		} else{
			addTask(buildTask(path, imageView,options));
		}

	}

	/**
	 * 根据传入的参数，新建一个任务
	 * @param path
	 * @param imageView
	 */
	private Runnable buildTask(final String path, final ImageView imageView,
			final DisplayImageOptions options)
	{
		return new Runnable()
		{
			@Override
			public void run()
			{
				Bitmap bm = null;
				if (options.fromNet)
				{
					//先去缓存文件夹查找
					File file = getDiskCacheDir(imageView.getContext(),
							md5(path));
					// 如果在缓存文件中发现
					if (file.exists()){
						bm = loadImageFromLocal(file.getAbsolutePath(),
								imageView);
					} else{
						// 检测是否开启硬盘缓存
						if (options.cacheOnDisk){
							boolean downloadState = DownloadImgUtils
									.downloadImgByUrl(path, file);
							if (downloadState){
								bm = loadImageFromLocal(file.getAbsolutePath(),
										imageView);
							}
						} else{
							bm = DownloadImgUtils.downloadImgByUrl(path,
									imageView);
						}
					}
				} else{
					bm = loadImageFromLocal(path, imageView);
				}
				// 是否开启内存中缓存
				if (options.cacheInMemory) {
					addBitmapToLruCache(path, bm);
				}
				//发送消息至UI线程
				refreashBitmap(path, imageView, bm,options);
				//释放信号
				mSemaphoreThreadPool.release();
			}


		};
	}

	private Bitmap loadImageFromLocal(final String path,
			final ImageView imageView)
	{
		Bitmap bm;
		// 加载图片
		// 图片的压缩
		// 1、获得图片需要显示的大小
		ImageSize imageSize = ImageSizeUtil.getImageViewSize(imageView);
		// 2、压缩图片
		bm = decodeSampledBitmapFromPath(path, imageSize.width,
				imageSize.height);
		return bm;
	}


	/**
	 * 利用签名辅助类，将字符串字节数组
	 * 
	 * @param str
	 * @return
	 */
	public String md5(String str)
	{
		byte[] digest = null;
		try
		{
			MessageDigest md = MessageDigest.getInstance("md5");
			digest = md.digest(str.getBytes());
			return bytes2hex02(digest);

		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 方式二
	 * 
	 * @param bytes
	 * @return
	 */
	public String bytes2hex02(byte[] bytes)
	{
		StringBuilder sb = new StringBuilder();
		String tmp = null;
		for (byte b : bytes)
		{
			// 将每个字节与0xFF进行与运算，然后转化为10进制，然后借助于Integer再转化为16进制
			tmp = Integer.toHexString(0xFF & b);
			if (tmp.length() == 1)// 每个字节8为，转为16进制标志，2个16进制位
			{
				tmp = "0" + tmp;
			}
			sb.append(tmp);
		}

		return sb.toString();

	}

	private void refreashBitmap(String path, ImageView imageView,
			Bitmap bm,DisplayImageOptions options){
		Message message = Message.obtain();
		ImgBeanHolder holder = new ImgBeanHolder();
		holder.bitmap = bm;
		holder.path = path;
		holder.imageView = imageView;
		holder.options=options;
		message.obj = holder;
		mUIHandler.sendMessage(message);
	}

	/**
	 * 将图片加入LruCache
	 * 
	 * @param path
	 * @param bm
	 */
	protected void addBitmapToLruCache(String path, Bitmap bm)
	{
		if (getBitmapFromLruCache(path) == null){
			if (bm != null)
				mLruCache.put(path, bm);
		}
	}

	/**
	 * 根据图片需要显示的宽和高对图片进行压缩
	 * 
	 * @param path
	 * @param width
	 * @param height
	 * @return
	 */
	protected Bitmap decodeSampledBitmapFromPath(String path, int width,
			int height)
	{
		// 获得图片的宽和高，并不把图片加载到内存中
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		options.inSampleSize = ImageSizeUtil.caculateInSampleSize(options,
				width, height);

		// 使用获得到的InSampleSize再次解析图片
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		return bitmap;
	}



	/**
	 * 获得缓存图片的地址
	 * 
	 * @param context
	 * @param uniqueName
	 * @return
	 */
	public File getDiskCacheDir(Context context, String uniqueName)
	{
		String cachePath;
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState()))
		{
			cachePath = context.getExternalCacheDir().getPath();
		} else
		{
			cachePath = context.getCacheDir().getPath();
		}
		return new File(cachePath + File.separator + uniqueName);
	}

	/**
	 * 根据path在缓存中获取bitmap
	 * 
	 * @param key
	 * @return
	 */
	private Bitmap getBitmapFromLruCache(String key)
	{
		return mLruCache.get(key);
	}


	/**
	 * 从任务队列取出一个方法，当队列为空时，将阻塞该方法
	 * 
	 * @return
	 * @throws InterruptedException 
	 */
	private Runnable getTask() throws InterruptedException
	{
		if (mType == Type.FIFO)
		{
			return mTaskQueue.takeFirst();
		} else 
		{
			return mTaskQueue.takeLast();
		}
	}
	/**
	 * 将任务添加入队列
	 * @param runnable
	 * @throws InterruptedException
	 */
	private  void addTask(Runnable runnable)
	{
		try {
			mTaskQueue.put(runnable);
		} catch (Exception e) {
			Log.i(TAG, e.toString());
		}

	}
	private class ImgBeanHolder
	{
		Bitmap bitmap;
		ImageView imageView;
		String path;
		DisplayImageOptions options;
	}
}
