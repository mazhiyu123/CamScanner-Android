package com.mzy.camera.view;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import com.mzy.FileOperateUtil;
import com.mzy.camera.view.CameraView.FlashMode;
import com.mzy.imageprocessing.R;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;


/** 
 * @ClassName: CameraContainer 
 * @Description:  ������������ ��������󶨵�surfaceview�����պ����ʱͼƬView�;۽�View 
 * @author WANDERYUREN 
 */
public class CameraContainer extends RelativeLayout implements CameraOperation{

	public final static String TAG="CameraContainer";

	/** ����󶨵�SurfaceView  */ 
	private CameraView mCameraView;

	/** �������ɵ�ͼƬ������һ�����Ƶ����½ǵĶ���Ч�������� */ 
	private TempImageView mTempImageView;

	/** ������Ļʱ��ʾ�ľ۽�ͼ��  */ 
	private FocusImageView mFocusImageView;

	/** ��ʾ¼����ʱ��TextView  */ 
	private TextView mRecordingInfoTextView;

	/** ��ʾˮӡͼ��  */ 
	private ImageView mWaterMarkImageView; 

	/** �����Ƭ�ĸ�Ŀ¼ */ 
	private String mSavePath;

	/** ��Ƭ�ֽ���������  */ 
	private DataHandler mDataHandler;

	/** ���ռ����ӿڣ����������տ�ʼ�ͽ�����ִ����Ӧ����  */ 
	private TakePictureListener mListener;

	/** ���ż����϶��� */ 
	private SeekBar mZoomSeekBar;

	/** ����ִ�ж�ʱ�����Handler����*/
	private Handler mHandler;
	private long mRecordStartTime;
	private SimpleDateFormat mTimeFormat;
	public CameraContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
		mHandler=new Handler();
		mTimeFormat=new SimpleDateFormat("mm:ss",Locale.getDefault());
		setOnTouchListener(new TouchListener());
	}

	/**  
	 *  ��ʼ���ӿؼ�
	 *  @param context   
	 */
	private void initView(Context context) {
		inflate(context, R.layout.cameracontainer, this);
		mCameraView=(CameraView) findViewById(R.id.cameraView);

		mTempImageView=(TempImageView) findViewById(R.id.tempImageView);

		mFocusImageView=(FocusImageView) findViewById(R.id.focusImageView);

		
		
	}


	/**  
	 *  ��ȡ��ǰ���������
	 *  @return   
	 */
	@Override
	public FlashMode getFlashMode() {
		return mCameraView.getFlashMode();
	}

	/**  
	 *  �������������
	 *  @param flashMode   
	 */
	@Override
	public void setFlashMode(FlashMode flashMode) {
		mCameraView.setFlashMode(flashMode);
	}

	/**
	 * �����ļ�����·��
	 * @param rootPath
	 */
	public void setRootPath(String rootPath){
		this.mSavePath=rootPath;

	}



	/**
	 * ���շ���
	 * @param callback
	 */
	public void takePicture(){
		takePicture(pictureCallback,mListener);
	}

	/**  
	 * @Description: ���շ���
	 * @param @param listener ���ռ����ӿ�
	 * @return void    
	 * @throws 
	 */
	public void takePicture(TakePictureListener listener){
		this.mListener=listener;
		takePicture(pictureCallback, mListener);
	}


	@Override
	public void takePicture(PictureCallback callback,
			TakePictureListener listener) {
		mCameraView.takePicture(callback,listener);
	}




	private final AutoFocusCallback autoFocusCallback=new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			//�۽�֮����ݽ���޸�ͼƬ
			if (success) {
				mFocusImageView.onFocusSuccess();
			}else {
				//�۽�ʧ����ʾ��ͼƬ������δ�ҵ����ʵ���Դ����������ʾͬһ��ͼƬ
				mFocusImageView.onFocusFailed();

			}
		}
	};

	private final PictureCallback pictureCallback=new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			if(mSavePath==null) throw new RuntimeException("mSavePath is null");
			if(mDataHandler==null) mDataHandler=new DataHandler();	
			mDataHandler.setMaxSize(200);
			Bitmap bm=mDataHandler.save(data);
			mTempImageView.setListener(mListener);
			//mTempImageView.isVideo(false);
			mTempImageView.setImageBitmap(bm);
			mTempImageView.startAnimation(R.anim.tempview_show);
			//���´�Ԥ��ͼ��������һ�ε�����׼��
			camera.startPreview();
			if(mListener!=null) mListener.onTakePictureEnd(bm);
		}
	};

	private final class TouchListener implements OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			/** ͨ�������㱣������λ MotionEvent.ACTION_MASK = 255 */
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			
			case MotionEvent.ACTION_UP:
				
					//���þ۽�
					Point point=new Point((int)event.getX(), (int)event.getY());
					mCameraView.onFocus(point,autoFocusCallback);
					mFocusImageView.startFocus(point);
				
				break;
			}
			return true;
		}

	}

	/**
	 * ���շ��ص�byte���ݴ�����
	 *
	 */
	private final class DataHandler{
		/** ��ͼ���·��  */
		private String mThumbnailFolder;
		/** Сͼ���·�� */
		private String mImageFolder;
		/** ѹ�����ͼƬ���ֵ ��λKB*/
		private int maxSize=200;

		public DataHandler(){
			mImageFolder=FileOperateUtil.getFolderPath(getContext(), FileOperateUtil.TYPE_IMAGE, mSavePath);
			mThumbnailFolder=FileOperateUtil.getFolderPath(getContext(),  FileOperateUtil.TYPE_THUMBNAIL, mSavePath);
			File folder=new File(mImageFolder);
			if(!folder.exists()){
				folder.mkdirs();
			}
			folder=new File(mThumbnailFolder);
			if(!folder.exists()){
				folder.mkdirs();
			}
		}

		/**
		 * ����ͼƬ
		 * @param ������ص��ļ���
		 * @return ���������ɵ�����ͼ
		 */
		public Bitmap save(byte[] data){
			if(data!=null){
				//��������������ص�ͼƬ
				Bitmap bm=BitmapFactory.decodeByteArray(data, 0, data.length);
				
				//��������ͼ
				Bitmap thumbnail=ThumbnailUtils.extractThumbnail(bm, 213, 213);
				//�����µ��ļ���
				String imgName=FileOperateUtil.createFileNmae(".jpg");
				String imagePath=mImageFolder+File.separator+imgName;//ͼƬ·��
				String thumbPath=mThumbnailFolder+File.separator+imgName;//����ͼ·��

				File file=new File(imagePath);  
				File thumFile=new File(thumbPath);
				try{
					//��ͼƬ��ͼ
					FileOutputStream fos=new FileOutputStream(file);
					ByteArrayOutputStream bos=compress(bm);
					fos.write(bos.toByteArray());
					fos.flush();
					fos.close();
					//��ͼƬСͼ
					BufferedOutputStream bufferos=new BufferedOutputStream(new FileOutputStream(thumFile));
					thumbnail.compress(Bitmap.CompressFormat.JPEG, 50, bufferos);
					bufferos.flush();
					bufferos.close();
					return bm; 
				}catch(Exception e){
					Log.e(TAG, e.toString());
					Toast.makeText(getContext(), "�������������ʧ��", Toast.LENGTH_SHORT).show();

				}
			}else{
				Toast.makeText(getContext(), "����ʧ�ܣ�������", Toast.LENGTH_SHORT).show();
			}
			return null;
		}


		/**
		 * ͼƬѹ������
		 * @param bitmap ͼƬ�ļ�
		 * @param max �ļ���С���ֵ
		 * @return ѹ������ֽ���
		 * @throws Exception
		 */
		public ByteArrayOutputStream compress(Bitmap bitmap){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);// ����ѹ������������100��ʾ��ѹ������ѹ��������ݴ�ŵ�baos��
			int options = 99;
			while ( baos.toByteArray().length / 1024 > maxSize) { // ѭ���ж����ѹ����ͼƬ�Ƿ����100kb,���ڼ���ѹ��
				options -= 3;// ÿ�ζ�����10
				//ѹ����С��0������ѹ��
				if (options<0) {
					break;
				}
				Log.i(TAG,baos.toByteArray().length / 1024+"");
				baos.reset();// ����baos�����baos
				bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);// ����ѹ��options%����ѹ��������ݴ�ŵ�baos��
			}
			return baos;
		}

		public void setMaxSize(int maxSize) {
			this.maxSize = maxSize;
		}
	}

	/** 
	 * @ClassName: TakePictureListener 
	 * @Description:  ���ռ����ӿڣ����������տ�ʼ�ͽ�����ִ����Ӧ���� 
	 *  
	 */
	public static interface TakePictureListener{		
		/**  
		 *���ս���ִ�еĶ������÷�������onPictureTaken����ִ�к󴥷�
		 *  @param bm �������ɵ�ͼƬ 
		 */
		public void onTakePictureEnd(Bitmap bm);

		/**  ��ʱͼƬ���������󴥷�
		 * @param bm �������ɵ�ͼƬ 
		 * @param isVideo true����ǰΪ¼������ͼ false:Ϊ��������ͼ
		 * */
		public void onAnimtionEnd(Bitmap bm);
	}


	/**  
	 * dipתpx
	 *  @param dipValue
	 *  @return   
	 */
	private  int dip2px(float dipValue){ 
		final float scale = getResources().getDisplayMetrics().density; 
		return (int)(dipValue * scale + 0.5f); 
	}
}