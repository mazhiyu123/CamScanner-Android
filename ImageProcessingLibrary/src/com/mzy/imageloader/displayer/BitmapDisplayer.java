

package com.mzy.imageloader.displayer;

import android.graphics.Bitmap;
import android.widget.ImageView;
/** 
* @ClassName: BitmapDisplayer 
* @Description:  显示图片接口类
* @author WANDERYUREN
*/
public interface BitmapDisplayer {
	
	void display(Bitmap bitmap, ImageView imageView);
	void display(int resouceID,ImageView imageView);
}
