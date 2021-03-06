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
 * @Description: 和相机绑定的SurfaceView 封装了拍照方法
 * @author WANDERYUREN 
 */
public class CameraView extends SurfaceView implements CameraOperation{

	public final static String TAG="CameraView";
	/** 和该View绑定的Camera对象 */
	private Camera mCamera;

	/** 当前闪光灯类型，默认为关闭 */ 
	private FlashMode mFlashMode=FlashMode.ON;

	

	/** 当前屏幕旋转角度*/ 
	private int mOrientation=0;
	
	/**  相机配置，在录像前记录，用以录像结束后恢复原配置 */ 
	private Camera.Parameters mParameters;
	/**  录像存放路径 ，用以生成缩略图*/ 
	//private String mRecordPath=null;
	public CameraView(Context context){
		super(context);
		//初始化容器
		getHolder().addCallback(callback);
		openCamera();
		//mIsFrontCamera=false;
	}

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//初始化容器
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
				Toast.makeText(getContext(), "打开相机失败", Toast.LENGTH_SHORT).show();
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
		
			//创建缩略图,该方法只能获取384X512的缩略图，舍弃，使用源码中的获取缩略图方法
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
				//存图片小图
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
	 *   根据当前照相机状态(前置或后置)，打开对应相机
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
	 *  获取当前闪光灯类型
	 *  @return   
	 */
	@Override
	public FlashMode getFlashMode() {
		return mFlashMode;
	}

	/**  
	 *  设置闪光灯类型
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
	 * 手动聚焦 
	 *  @param point 触屏坐标
	 */
	protected void onFocus(Point point,AutoFocusCallback callback){
		Camera.Parameters parameters=mCamera.getParameters();
		//不支持设置自定义聚焦，则使用自动聚焦，返回
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
			//，在此try掉，对实际聚焦效果没影响
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		mCamera.autoFocus(callback);
	}




	/**
	 * 设置照相机参数
	 */
	private void setCameraParameters(){
		Camera.Parameters parameters = mCamera.getParameters();
		// 选择合适的预览尺寸   
		List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
		if (sizeList.size()>0) {
			Size cameraSize=sizeList.get(0);
			//预览图片大小
			parameters.setPreviewSize(cameraSize.width, cameraSize.height);
		}

		//设置生成的图片大小
		sizeList = parameters.getSupportedPictureSizes();
		if (sizeList.size()>0) {
			Size cameraSize=sizeList.get(0);
			for (Size size : sizeList) {
				//小于100W像素
				if (size.width*size.height<100*10000) {
					cameraSize=size;
					break;
				}
			}
			parameters.setPictureSize(cameraSize.width, cameraSize.height);
		}
		//设置图片格式
		parameters.setPictureFormat(ImageFormat.JPEG);       
		parameters.setJpegQuality(100);
		parameters.setJpegThumbnailQuality(100);
		//自动聚焦模式
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		mCamera.setParameters(parameters);
		//设置闪光灯模式。此处主要是用于在相机摧毁后又重建，保持之前的状态
		setFlashMode(mFlashMode);
		
		//开启屏幕朝向监听
		startOrientationChangeListener();
	}

	/**  
	 *   启动屏幕朝向改变监听函数 用于在屏幕横竖屏切换时改变保存的图片的方向  
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
	 *   根据当前朝向修改保存图片的旋转角度
	 */
	private void updateCameraOrientation(){
		if(mCamera!=null){
			Camera.Parameters parameters = mCamera.getParameters();
			//rotation参数为 0、90、180、270。水平方向为0。
			int rotation=90+mOrientation==360?0:90+mOrientation;
			
			parameters.setRotation(rotation);//生成的图片转90°
			//预览图片旋转90°
			mCamera.setDisplayOrientation(90);//预览转90°
			mCamera.setParameters(parameters);
		}
	}

	/** 
	 * @Description: 闪光灯类型枚举 默认为关闭
	 */
	public enum FlashMode{
		/** ON:拍照时打开闪光灯   */ 
		ON,
		/** OFF：不打开闪光灯  */ 
		OFF,
		/** AUTO：系统决定是否打开闪光灯  */ 
		AUTO,
		/** TORCH：一直打开闪光灯  */ 
		TORCH
	}
}