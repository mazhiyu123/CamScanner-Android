package com.mzy.camera.view;

import android.graphics.Bitmap;
import android.hardware.Camera.PictureCallback;

import com.mzy.camera.view.CameraContainer.TakePictureListener;
import com.mzy.camera.view.CameraView.FlashMode;

/** 
 * @ClassName: CameraOperation 
 * @Description:相机操作接口，用以统一CameraContainer和CameraView的功能
 * @author WANDERYUREN
 */
public interface CameraOperation {
	

	/**  
	 *  获取当前闪光灯模式
	 *  @return   
	 */
	public FlashMode getFlashMode();
	/**  
	 *  设置闪光灯模式
	 *  @param flashMode   
	 */
	public void setFlashMode(FlashMode flashMode);
	/**  
	 *  拍照
	 *  @param callback 拍照回调函数 
	 *  @param listener 拍照动作监听函数  
	 */
	public void takePicture(PictureCallback callback,TakePictureListener listener);
	/**  
	 *  相机最大缩放级别
	 *  @return   
	 */
	
}
