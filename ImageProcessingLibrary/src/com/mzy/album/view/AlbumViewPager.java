package com.mzy.album.view;


import java.io.File;
import java.util.ArrayList;
import java.util.List;


import com.mzy.FileOperateUtil;
import com.mzy.album.view.MatrixImageView.OnMovingListener;
import com.mzy.album.view.MatrixImageView.OnSingleTapListener;
import com.mzy.imageprocessing.R;
import com.mzy.imageloader.DisplayImageOptions;
import com.mzy.imageloader.ImageLoader;
import com.mzy.imageloader.displayer.MatrixBitmapDisplayer;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


/** 
 * @ClassName: AlbumViewPager 
 * @Description:  �Զ���viewpager  �Ż����¼����ص���ͼƬ���� �Զ���ؼ�
 * @author WANDERYUREN
 */
public class AlbumViewPager extends ViewPager implements OnMovingListener {
	public final static String TAG="AlbumViewPager";

	/**  ͼƬ������ �Ż����˻���  */ 
	private ImageLoader mImageLoader;
	/**  ����ͼƬ���ò��� */ 
	private DisplayImageOptions mOptions;	

	/**  ��ǰ�ӿؼ��Ƿ����϶�״̬  */ 
	private boolean mChildIsBeingDragged=false;

	/**  ���浥���¼� ������ʾ�����ز˵��� */ 
	private OnSingleTapListener onSingleTapListener;
	
	/**  ���Ű�ť����¼� */ 
	//private OnPlayVideoListener onPlayVideoListener;
	public AlbumViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		mImageLoader= ImageLoader.getInstance(context);
		//��������ͼƬ���ز���
		DisplayImageOptions.Builder builder= new DisplayImageOptions.Builder();
		builder =builder
				.showImageOnLoading(R.drawable.ic_stub)
				.showImageOnFail(R.drawable.ic_error)
				.cacheInMemory(true)
				.cacheOnDisk(false)
				.displayer(new MatrixBitmapDisplayer());
		mOptions=builder.build();
	}


	

	/**  
	 *  ɾ����ǰ��
	 *  @return  ����ǰλ��/��������
	 */
	public String deleteCurrentPath(){
		return ((ViewPagerAdapter)getAdapter()).deleteCurrentItem(getCurrentItem());

	}


	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		if(mChildIsBeingDragged)
			return false;
		return super.onInterceptTouchEvent(arg0);
	}

	@Override
	public void startDrag() {
		// TODO Auto-generated method stub
		mChildIsBeingDragged=true;
	}


	@Override
	public void stopDrag() {
		// TODO Auto-generated method stub
		mChildIsBeingDragged=false;
	}

	public void setOnSingleTapListener(OnSingleTapListener onSingleTapListener) {
		this.onSingleTapListener = onSingleTapListener;
	}
	
	public class ViewPagerAdapter extends PagerAdapter {
		private List<String> paths;//��ͼ��ַ ���Ϊ����ͼƬ ��Ϊ��ͼurl
		public ViewPagerAdapter(List<String> paths){
			this.paths=paths;
		}

		@Override
		public int getCount() {
			return paths.size();
		}

		@Override
		public Object instantiateItem(ViewGroup viewGroup, int position) {
			//ע�⣬���ﲻ���Լ�inflate��ʱ��ֱ����ӵ�viewGroup�£�����Ҫ��addView�������
			//��Ϊֱ�Ӽӵ�viewGroup�»ᵼ�·��ص�viewΪviewGroup
			View imageLayout = inflate(getContext(),R.layout.item_album_pager, null);
			viewGroup.addView(imageLayout);
			assert imageLayout != null;
			MatrixImageView imageView = (MatrixImageView) imageLayout.findViewById(R.id.image);
			imageView.setOnMovingListener(AlbumViewPager.this);
			imageView.setOnSingleTapListener(onSingleTapListener);
			String path=paths.get(position);
			
			mImageLoader.loadImage(path, imageView, mOptions);
			return imageLayout;
		}

	

		@Override
		public int getItemPosition(Object object) {
			//��notifyDataSetChangedʱ����None�����»���
			return POSITION_NONE;
		}

		@Override
		public void destroyItem(ViewGroup container, int arg1, Object object) {
			((ViewPager) container).removeView((View) object);  
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;			
		}

		//�Զ����ȡ��ǰview����                              
		public String deleteCurrentItem(int position) {
			String path=paths.get(position);
			if(path!=null) {
				FileOperateUtil.deleteSourceFile(path, getContext());
				paths.remove(path);
				notifyDataSetChanged();
				if(paths.size()>0)
					return (getCurrentItem()+1)+"/"+paths.size();
				else {
					return "0/0";
				}
			}
			return null;
		}
	}


}
