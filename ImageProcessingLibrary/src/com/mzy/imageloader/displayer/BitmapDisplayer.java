

package com.mzy.imageloader.displayer;

import android.graphics.Bitmap;
import android.widget.ImageView;
/** 
* @ClassName: BitmapDisplayer 
* @Description:  ��ʾͼƬ�ӿ���
* @author WANDERYUREN
*/
public interface BitmapDisplayer {
	
	void display(Bitmap bitmap, ImageView imageView);
	void display(int resouceID,ImageView imageView);
}
