package com.mzy.imageprocessing;

import java.io.File;
import java.util.List;

import com.mzy.FileOperateUtil;
import com.mzy.FileOperateUtil;
import com.mzy.album.view.FilterImageView;
import com.mzy.camera.view.CameraContainer;
import com.mzy.camera.view.CameraContainer.TakePictureListener;
import com.mzy.camera.view.CameraView.FlashMode;



import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

/** 
 * @ClassName: CameraAty 
 * @Description:  自定义照相机类
 * @author WANDERYUREN
 */
public class CameraAty extends Activity implements View.OnClickListener,TakePictureListener{
	public final static String TAG="CameraAty";
	private String mSaveRoot;
	private CameraContainer mContainer;
	private FilterImageView mThumbView;
	private ImageButton mCameraShutterButton;
	private ImageView mFlashView;
	private ImageButton mChat;
	private ImageView mSettingView;
	private View mHeaderBar;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.camera);

		mHeaderBar=findViewById(R.id.camera_header_bar);
		mContainer=(CameraContainer)findViewById(R.id.container);
		mThumbView=(FilterImageView)findViewById(R.id.btn_thumbnail);
		mCameraShutterButton=(ImageButton)findViewById(R.id.btn_shutter_camera);
		mFlashView=(ImageView)findViewById(R.id.btn_flash_mode);
		mSettingView=(ImageView)findViewById(R.id.btn_other_setting);
        mChat=(ImageButton)findViewById(R.id.btn_chat);

		mThumbView.setOnClickListener(this);
		mCameraShutterButton.setOnClickListener(this);
		mFlashView.setOnClickListener(this);
		mSettingView.setOnClickListener(this);
        mChat.setOnClickListener(this);
        
		mSaveRoot="test";
		mContainer.setRootPath(mSaveRoot);
		initThumbnail();
	}


	/**
	 * 加载缩略图
	 */
	private void initThumbnail() {
		String thumbFolder=FileOperateUtil.getFolderPath(this, FileOperateUtil.TYPE_THUMBNAIL, mSaveRoot);
		List<File> files=FileOperateUtil.listFiles(thumbFolder, ".jpg");
		if(files!=null&&files.size()>0){
			Bitmap thumbBitmap=BitmapFactory.decodeFile(files.get(0).getAbsolutePath());
			if(thumbBitmap!=null){
				mThumbView.setImageBitmap(thumbBitmap);
		}else {
			mThumbView.setImageBitmap(null);
		}
		}
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.btn_shutter_camera:
			mCameraShutterButton.setClickable(false);
			mContainer.takePicture(this);
			break;
		case R.id.btn_chat:
			startActivity(new Intent(this,ChatAty.class));
			break;
		case R.id.btn_thumbnail:
			startActivity(new Intent(this,AlbumAty.class));
			break;
		case R.id.btn_flash_mode:
			if(mContainer.getFlashMode()==FlashMode.ON){
				mContainer.setFlashMode(FlashMode.OFF);
				mFlashView.setImageResource(R.drawable.btn_flash_off);
			}else if (mContainer.getFlashMode()==FlashMode.OFF) {
				mContainer.setFlashMode(FlashMode.AUTO);
				mFlashView.setImageResource(R.drawable.btn_flash_auto);
			}
			else if (mContainer.getFlashMode()==FlashMode.AUTO) {
				mContainer.setFlashMode(FlashMode.TORCH);
				mFlashView.setImageResource(R.drawable.btn_flash_torch);
			}
			else if (mContainer.getFlashMode()==FlashMode.TORCH) {//TORCH 手电筒
				mContainer.setFlashMode(FlashMode.ON);
				mFlashView.setImageResource(R.drawable.btn_flash_on);
			}
			break;
		
	
		default:
			break;
		}
	}


	
	@Override
	public void onTakePictureEnd(Bitmap thumBitmap) {
		mCameraShutterButton.setClickable(true);	
	}

    @Override
	public void onAnimtionEnd(Bitmap bm) {
		if(bm!=null){
			//生成缩略图
			Bitmap thumbnail=ThumbnailUtils.extractThumbnail(bm, 213, 213);
			mThumbView.setImageBitmap(thumbnail);
		}
	}

	@Override
	protected void onResume() {		
		super.onResume();
	}
}