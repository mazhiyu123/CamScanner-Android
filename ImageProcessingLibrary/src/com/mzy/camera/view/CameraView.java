package com.mzy.camera.view;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mzy.imageprocessing.R;
import com.mzy.FileOperateUtil;
import com.mzy.camera.view.CameraContainer.TakePictureListener;

import android.R.integer;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;


/** 
 * @ClassName: CameraView 
 * @Description: ������󶨵�SurfaceView ��װ�����շ���
 * @author WANDERYUREN 
 */
public class CameraView extends SurfaceView implements CameraOperation{

	public final static String TAG="CameraView";
	/** �͸�View�󶨵�Camera���� */
	private Camera mCamera;

	/** ��ǰ��������ͣ�Ĭ��Ϊ�ر� */ 
	private FlashMode mFlashMode=FlashMode.ON;

	

	/** ��ǰ��Ļ��ת�Ƕ�*/ 
	private int mOrientation=0;
	
	/**  ������ã���¼��ǰ��¼������¼�������ָ�ԭ���� */ 
	private Camera.Parameters mParameters;
	/**  ¼����·�� ��������������ͼ*/ 
	//private String mRecordPath=null;
	public CameraView(Context context){
		super(context);
		//��ʼ������
		getHolder().addCallback(callback);
		openCamera();
		//mIsFrontCamera=false;
	}

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//��ʼ������
		getHolder().addCallback(callback);
		openCamera();
		//mIsFrontCamera=false;
	}

	private SurfaceHolder.Callback callback=new SurfaceHolder.Callback() {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				if(mCamera==null){
					openCamera();
				}
				setCameraParameters();
				mCamera.setPreviewDisplay(getHolder());
			} catch (Exception e) {
				Toast.makeText(getContext(), "�����ʧ��", Toast.LENGTH_SHORT).show();
				Log.e(TAG,e.getMessage());
			}
			mCamera.startPreview();
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			updateCameraOrientation();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
		
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}

		}
	};

	



/*	private Bitmap saveThumbnail() throws FileNotFoundException, IOException {
		
			//��������ͼ,�÷���ֻ�ܻ�ȡ384X512������ͼ��������ʹ��Դ���еĻ�ȡ����ͼ����
			//			Bitmap bitmap=ThumbnailUtils.createVideoThumbnail(mRecordPath, Thumbnails.MINI_KIND);
			//Bitmap bitmap=getVideoThumbnail(mRecordPath);

			if(bitmap!=null){
				String mThumbnailFolder=FileOperateUtil.getFolderPath(getContext(),  FileOperateUtil.TYPE_THUMBNAIL, "test");
				File folder=new File(mThumbnailFolder);
				if(!folder.exists()){
					folder.mkdirs();
				}
				File file=new File(mRecordPath);
				file=new File(folder+File.separator+file.getName().replace("3gp", "jpg"));
				//��ͼƬСͼ
				BufferedOutputStream bufferos=new BufferedOutputStream(new FileOutputStream(file));
				bitmap.compress(Bitmap.CompressFormat.JPEG,100, bufferos);
				bufferos.flush();
				bufferos.close();
				return bitmap;
			}
			mRecordPath=null;
		
		return null;
	}*/

  



	/**  
	 *   ���ݵ�ǰ�����״̬(ǰ�û����)���򿪶�Ӧ���
	 */
	private boolean openCamera()  {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}

			try {
				mCamera=Camera.open();
			} catch (Exception e) {
				mCamera =null;
				return false;
			}

		
		return true;
	}

	/**  
	 *  ��ȡ��ǰ���������
	 *  @return   
	 */
	@Override
	public FlashMode getFlashMode() {
		return mFlashMode;
	}

	/**  
	 *  �������������
	 *  @param flashMode   
	 */
	@Override
	public void setFlashMode(FlashMode flashMode) {
		if(mCamera==null) return;
		mFlashMode = flashMode;
		Camera.Parameters parameters=mCamera.getParameters();
		switch (flashMode) {
		case ON:
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
			break;
		case AUTO:
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
			break;
		case TORCH:
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			break;
		default:
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			break;
		}
		mCamera.setParameters(parameters);
	}
	@Override
	public void takePicture(PictureCallback callback,TakePictureListener listener){
		mCamera.takePicture(null, null, callback);
	}

	/**  
	 * �ֶ��۽� 
	 *  @param point ��������
	 */
	protected void onFocus(Point point,AutoFocusCallback callback){
		Camera.Parameters parameters=mCamera.getParameters();
		//��֧�������Զ���۽�����ʹ���Զ��۽�������
		if (parameters.getMaxNumFocusAreas()<=0) {
			mCamera.autoFocus(callback);
			return;
		}
		List<Area> areas=new ArrayList<Camera.Area>();
		int left=point.x-300;
		int top=point.y-300;
		int right=point.x+300;
		int bottom=point.y+300;
		left=left<-1000?-1000:left;
		top=top<-1000?-1000:top;
		right=right>1000?1000:right;
		bottom=bottom>1000?1000:bottom;
		areas.add(new Area(new Rect(left,top,right,bottom), 100));
		parameters.setFocusAreas(areas);
		try {
			//���ڴ�try������ʵ�ʾ۽�Ч��ûӰ��
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		mCamera.autoFocus(callback);
	}




	/**
	 * �������������
	 */
	private void setCameraParameters(){
		Camera.Parameters parameters = mCamera.getParameters();
		// ѡ����ʵ�Ԥ���ߴ�   
		List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
		if (sizeList.size()>0) {
			Size cameraSize=sizeList.get(0);
			//Ԥ��ͼƬ��С
			parameters.setPreviewSize(cameraSize.width, cameraSize.height);
		}

		//�������ɵ�ͼƬ��С
		sizeList = parameters.getSupportedPictureSizes();
		if (sizeList.size()>0) {
			Size cameraSize=sizeList.get(0);
			for (Size size : sizeList) {
				//С��100W����
				if (size.width*size.height<100*10000) {
					cameraSize=size;
					break;
				}
			}
			parameters.setPictureSize(cameraSize.width, cameraSize.height);
		}
		//����ͼƬ��ʽ
		parameters.setPictureFormat(ImageFormat.JPEG);       
		parameters.setJpegQuality(100);
		parameters.setJpegThumbnailQuality(100);
		//�Զ��۽�ģʽ
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		mCamera.setParameters(parameters);
		//���������ģʽ���˴���Ҫ������������ݻٺ����ؽ�������֮ǰ��״̬
		setFlashMode(mFlashMode);
		
		//������Ļ�������
		startOrientationChangeListener();
	}

	/**  
	 *   ������Ļ����ı�������� ��������Ļ�������л�ʱ�ı䱣���ͼƬ�ķ���  
	 */
	private  void startOrientationChangeListener() {  
		OrientationEventListener mOrEventListener = new OrientationEventListener(getContext()) {  
			@Override  
			public void onOrientationChanged(int rotation) { 

				if (((rotation >= 0) && (rotation <= 45)) || (rotation > 315)){
					rotation=0;
				}else if ((rotation > 45) && (rotation <= 135))  {
					rotation=90;
				}
				else if ((rotation > 135) && (rotation <= 225)) {
					rotation=180;
				} 
				else if((rotation > 225) && (rotation <= 315)) { 
					rotation=270;
				}else {
					rotation=0;
				}
				if(rotation==mOrientation)
					return;
				mOrientation=rotation;
				updateCameraOrientation();
			}  
		};  
		mOrEventListener.enable();  
	}  

	/**  
	 *   ���ݵ�ǰ�����޸ı���ͼƬ����ת�Ƕ�
	 */
	private void updateCameraOrientation(){
		if(mCamera!=null){
			Camera.Parameters parameters = mCamera.getParameters();
			//rotation����Ϊ 0��90��180��270��ˮƽ����Ϊ0��
			int rotation=90+mOrientation==360?0:90+mOrientation;
			
			parameters.setRotation(rotation);//���ɵ�ͼƬת90��
			//Ԥ��ͼƬ��ת90��
			mCamera.setDisplayOrientation(90);//Ԥ��ת90��
			mCamera.setParameters(parameters);
		}
	}

	/** 
	 * @Description: ���������ö�� Ĭ��Ϊ�ر�
	 */
	public enum FlashMode{
		/** ON:����ʱ�������   */ 
		ON,
		/** OFF�����������  */ 
		OFF,
		/** AUTO��ϵͳ�����Ƿ�������  */ 
		AUTO,
		/** TORCH��һֱ�������  */ 
		TORCH
	}
}