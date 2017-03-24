
package com.mzy.imageloader.displayer;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;


/** 
* @ClassName: MatrixBitmapDisplayer 
* @Description:  MatrixЧ����BitmapDisplayer
* @author WANDERYUREN
*/
public class MatrixBitmapDisplayer implements BitmapDisplayer {
	
	public MatrixBitmapDisplayer() {
		
	}

	@Override
	public void display(Bitmap bitmap, ImageView imageView) {
		//������ͼƬ����ΪFIT_CENTER
		imageView.setScaleType(ScaleType.FIT_CENTER);
		imageView.setImageBitmap(bitmap);
	}

	@Override
	public void display(int resouceID, ImageView imageView) {
		// ����ǰ�ͳ���ĵ�ͼƬ���Զ�����
		imageView.setScaleType(ScaleType.CENTER);
		imageView.setImageResource(resouceID);
	}
}
