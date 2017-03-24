package com.mzy.camera.view;

import android.graphics.Bitmap;
import android.hardware.Camera.PictureCallback;

import com.mzy.camera.view.CameraContainer.TakePictureListener;
import com.mzy.camera.view.CameraView.FlashMode;

/** 
 * @ClassName: CameraOperation 
 * @Description:��������ӿڣ�����ͳһCameraContainer��CameraView�Ĺ���
 * @author WANDERYUREN
 */
public interface CameraOperation {
	

	/**  
	 *  ��ȡ��ǰ�����ģʽ
	 *  @return   
	 */
	public FlashMode getFlashMode();
	/**  
	 *  ���������ģʽ
	 *  @param flashMode   
	 */
	public void setFlashMode(FlashMode flashMode);
	/**  
	 *  ����
	 *  @param callback ���ջص����� 
	 *  @param listener ���ն�����������  
	 */
	public void takePicture(PictureCallback callback,TakePictureListener listener);
	/**  
	 *  ���������ż���
	 *  @return   
	 */
	
}
