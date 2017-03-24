package com.mzy.imageprocessing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.mzy.FileOperateUtil;
import com.mzy.album.view.AlbumViewPager;
//import com.mzy.album.view.AlbumViewPager.OnPlayVideoListener;
import com.mzy.album.view.AlbumViewPager.ViewPagerAdapter;
import com.mzy.album.view.MatrixImageView.OnSingleTapListener;
//import com.mzy.video.view.VideoPlayerContainer;
//import com.mzy.video.view.VideoPlayerView;




import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.BitmapFactory;
/** 
 * @ClassName: AlbumItemAty 
 * @Description:相册图片大图Activity 包含图片删除功能
 * @author WANDERYUREN
 */
public class AlbumItemAty extends Activity implements OnClickListener,OnSingleTapListener
{
	public final static String TAG="AlbumDetailAty";
	private String mSaveRoot;
	private AlbumViewPager mViewPager;//显示大图
	private ImageView mBackView;
	private ImageView mCameraView;
	private TextView mCountView;
	private View mHeaderBar,mBottomBar;
	private Button mDeleteButton;
	private Button mImageProcessing;
	
	
	//paths 
	List<String> paths=new ArrayList<String>();
	private int currentItem=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.albumitem);
		
		mViewPager=(AlbumViewPager)findViewById(R.id.albumviewpager);
		
		//左上角回退按钮
		mBackView=(ImageView)findViewById(R.id.header_bar_photo_back);
		
		//右上角照相按钮
		mCameraView=(ImageView)findViewById(R.id.header_bar_photo_to_camera);
		
		//计数按钮
		mCountView=(TextView)findViewById(R.id.header_bar_photo_count);
		
		mHeaderBar=findViewById(R.id.album_item_header_bar);
		mBottomBar=findViewById(R.id.album_item_bottom_bar);
		
		mDeleteButton=(Button)findViewById(R.id.delete);
		mImageProcessing=(Button) findViewById(R.id.btn_processing);
		

		mBackView.setOnClickListener(this);
		mCameraView.setOnClickListener(this);
		mCountView.setOnClickListener(this);
		mDeleteButton.setOnClickListener(this);
		mImageProcessing.setOnClickListener(this);

		mSaveRoot="test";
		mViewPager.setOnPageChangeListener(pageChangeListener);
		mViewPager.setOnSingleTapListener(this);
		
		String currentFileName=null;
		if(getIntent().getExtras()!=null)
			currentFileName=getIntent().getExtras().getString("path");
		if(currentFileName!=null){
			File file=new File(currentFileName);
			//获得文件或文件夹的名称
			currentFileName=file.getName();
			if(currentFileName.indexOf(".")>0)
				currentFileName=currentFileName.substring(0, currentFileName.lastIndexOf("."));
		}
		
		loadAlbum(mSaveRoot,currentFileName);
	}


	/**  
	 *  加载图片
	 *  @param rootPath   图片根路径
	 */
	public void loadAlbum(String rootPath,String fileName){
		//获取根目录下缩略图文件夹
		String folder=FileOperateUtil.getFolderPath(this, FileOperateUtil.TYPE_IMAGE, rootPath);
		String thumbnailFolder=FileOperateUtil.getFolderPath(this, FileOperateUtil.TYPE_THUMBNAIL, rootPath);
		//获取图片文件大图
		List<File> imageList=FileOperateUtil.listFiles(folder, ".jpg");
		List<File> files=new ArrayList<File>();
		
		if(imageList!=null&&imageList.size()>0){
			files.addAll(imageList);
		}
		FileOperateUtil.sortList(files, false);
		if(files.size()>0){
			//List<String> paths=new ArrayList<String>();
			//int currentItem=0;
			for (File file : files) {
				if(fileName!=null&&file.getName().contains(fileName))
					currentItem=files.indexOf(file);
				paths.add(file.getAbsolutePath());
			}
			mViewPager.setAdapter(mViewPager.new ViewPagerAdapter(paths));
			mViewPager.setCurrentItem(currentItem);
			mCountView.setText((currentItem+1)+"/"+paths.size());
		}
		else {
			mCountView.setText("0/0");
		}
	}


	private OnPageChangeListener pageChangeListener=new OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			if(mViewPager.getAdapter()!=null){
				String text=(position+1)+"/"+mViewPager.getAdapter().getCount();
				mCountView.setText(text);
			}else {
				mCountView.setText("0/0");
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}
	};

	@Override
	public void onSingleTap() {
		if(mHeaderBar.getVisibility()==View.VISIBLE){
			AlphaAnimation animation=new AlphaAnimation(1, 0);
			animation.setDuration(300);
			mHeaderBar.startAnimation(animation);
			mBottomBar.startAnimation(animation);
			mHeaderBar.setVisibility(View.GONE);
			mBottomBar.setVisibility(View.GONE);
		}else {
			AlphaAnimation animation=new AlphaAnimation(0, 1);
			animation.setDuration(300);
			mHeaderBar.startAnimation(animation);
			mBottomBar.startAnimation(animation);
			mHeaderBar.setVisibility(View.VISIBLE);
			mBottomBar.setVisibility(View.VISIBLE);
		}	
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.header_bar_photo_back:
			startActivity(new Intent(this,AlbumAty.class));
			break;
		case R.id.header_bar_photo_to_camera:
			startActivity(new Intent(this,CameraAty.class));
			break;
		case R.id.delete:
			String result=mViewPager.deleteCurrentPath();
			if(result!=null)
				mCountView.setText(result);
			break;
		case R.id.btn_processing:
			//跳转到processingaty  进行   图像处理
			Intent intent=new Intent(AlbumItemAty.this,ProcessingAty.class);
			intent.putExtra("path",paths.get(currentItem));
			startActivity(intent);
			break;
		default:
			break;
		}
	}
	

}
