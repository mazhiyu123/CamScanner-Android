package com.mzy.imageprocessing;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;



import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.mzy.point.*;
import com.mzy.imageloader.displayer.BitmapDisplayer;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

/** 
* @ClassName: ProcessingAty 
* @Description:  进行图形处理的activity
* @author WANDERYUREN
*/

public class ProcessingAty extends Activity implements OnClickListener,OnTouchListener {
	
	ImageView showProcessingImage;
	Button mToPdfButton,mCutButton,mSharpenButton;
	String currentFileName=null;
	Bitmap bmProcessing;
    MyPoint lefttop,leftbottom,rightbottom,righttop;
    public int i=0;
	
	  private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
	        @Override
	        public void onManagerConnected(int status) {
	            switch (status) {
	                case LoaderCallbackInterface.SUCCESS:
	                {
	                    
	               mCutButton.setOnClickListener(ProcessingAty.this);
	               Toast.makeText(getApplicationContext(), "opencv珂珂珂珂可用", 4000).show();
	                } break;
	                default:
	                {
	                    super.onManagerConnected(status);
	                    Toast.makeText(getApplicationContext(), "opencv不可用", 4000).show();
	                } break;
	            }
	        }
	    };

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.processing);
		
		showProcessingImage=(ImageView)findViewById(R.id.processingshow);
		
		mToPdfButton=(Button)findViewById(R.id.btn_topdf);
		mCutButton=(Button)findViewById(R.id.btn_cut);
		mSharpenButton=(Button)findViewById(R.id.btn_sharpen);
		
		mToPdfButton.setOnClickListener(this);
		//mCutButton.setOnClickListener(this);
		mSharpenButton.setOnClickListener(this);
		showProcessingImage.setOnTouchListener(this);
		
		if(getIntent().getExtras()!=null)
			currentFileName=getIntent().getExtras().getString("path");
		Toast.makeText(getApplicationContext(), currentFileName, 4000).show();
	    bmProcessing=BitmapFactory.decodeFile(currentFileName);
		showProcessingImage.setImageBitmap(bmProcessing);
		/*if(currentFileName!=null){
			File file=new File(currentFileName);
			//获得文件或文件夹的名称
			currentFileName=file.getName();
			if(currentFileName.indexOf(".")>0)
				currentFileName=currentFileName.substring(0, currentFileName.lastIndexOf("."));
		}
*/		
		}
	
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		switch (event.getAction() ) {
		
		      case MotionEvent.ACTION_UP:
			
				if(i==0){
					//int x=(int)event.getX();
					//int y=(int)event.getY();
					//Toast.makeText(getApplicationContext(),x, 1000).show();
					 lefttop=new MyPoint((int)event.getX(),(int)event.getY());
					i++;
					//Toast.makeText(getApplicationContext(),lefttop.getX()+"   "+"1", 1000).show();
					Toast.makeText(getApplicationContext(),lefttop.toString()+"1", 1000).show();
				}else if(i==1){
					 leftbottom=new MyPoint((int)event.getX(), (int)event.getY());
					i++;
					Toast.makeText(getApplicationContext(),leftbottom.toString()+"2", 1000).show();
				}else if(i==2){
					 rightbottom=new MyPoint((int)event.getX(), (int)event.getY());
					i++;
					Toast.makeText(getApplicationContext(),rightbottom.toString()+"3", 1000).show();
				}else if(i==3){
					 righttop=new MyPoint((int)event.getX(), (int)event.getY());
					i++;
					Toast.makeText(getApplicationContext(),righttop.toString()+"4", 1000).show();
				}
				
				 //Toast.makeText(getApplicationContext(),lefttop.toString(), 1000).show();
			break;
		}
		
		return true;
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		
		      
		case R.id.btn_topdf:
			Toast.makeText(getApplicationContext(), "格式转换pdf", 1000).show();
			startToPdf();
			break;
		case R.id.btn_cut:
			Toast.makeText(getApplicationContext(), "透视剪裁", 1000).show();
			Bitmap afterbm=warp(bmProcessing, lefttop, leftbottom, rightbottom, righttop);
			showProcessingImage.setImageBitmap(afterbm);
			break;
		case R.id.btn_sharpen:
			Bitmap bmAfter=SharpenImage(bmProcessing);
			showProcessingImage.setImageBitmap(bmAfter);
			break;
		 default:break;
			
		}
	}
	/*
	 * 图像锐化处理 拉普拉斯算子处理  
	 */
	public Bitmap SharpenImage(Bitmap bmp)  
	{  
	     
	    /* * 锐化基本思想是加强图像中景物的边缘和轮廓,使图像变得清晰 
	     * 而图像平滑是使图像中边界和轮廓变得模糊 
	     *  
	     * 拉普拉斯算子图像锐化 
	     * 获取周围9个点的矩阵乘以模板9个的矩阵 卷积 
	      */ 
	    //拉普拉斯算子模板 { 0, -1, 0, -1, -5, -1, 0, -1, 0 } { -1, -1, -1, -1, 9, -1, -1, -1, -1 }  
	    int[] laplacian = new int[] {  -1, -1, -1, -1, 9, -1, -1, -1, -1 };   
	    int width = bmp.getWidth();    
	    int height = bmp.getHeight();    
	    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);    
	    int pixR = 0;    
	    int pixG = 0;    
	    int pixB = 0;    
	    int pixColor = 0;    
	    int newR = 0;    
	    int newG = 0;    
	    int newB = 0;    
	    int idx = 0;    
	    float alpha = 0.3F;  //图片透明度  
	    int[] pixels = new int[width * height];    
	    bmp.getPixels(pixels, 0, width, 0, 0, width, height);    
	    //图像处理  
	    for (int i = 1; i < height - 1; i++)    
	    {    
	        for (int k = 1; k < width - 1; k++)    
	        {    
	            idx = 0;  
	            newR = 0;    
	            newG = 0;    
	            newB = 0;    
	            for (int n = -1; n <= 1; n++)   //取出图像3*3领域像素   
	            {    
	                for (int m = -1; m <= 1; m++)  //n行数不变 m列变换  
	                {    
	                    pixColor = pixels[(i + n) * width + k + m];  //当前点(i,k)  
	                    pixR = Color.red(pixColor);    
	                    pixG = Color.green(pixColor);    
	                    pixB = Color.blue(pixColor);    
	                    //图像像素与对应摸板相乘     
	                    newR = newR + (int) (pixR * laplacian[idx] * alpha);    
	                    newG = newG + (int) (pixG * laplacian[idx] * alpha);    
	                    newB = newB + (int) (pixB * laplacian[idx] * alpha);    
	                    idx++;   
	                }    
	            }  
	            newR = Math.min(255, Math.max(0, newR));    
	            newG = Math.min(255, Math.max(0, newG));    
	            newB = Math.min(255, Math.max(0, newB));    
	            //赋值    
	            pixels[i * width + k] = Color.argb(255, newR, newG, newB);     
	        }  
	    }  
	    bitmap.setPixels(pixels, 0, width, 0, 0, width, height);    
	    return bitmap;
	}  

/**
 * 透视变换函数
 */
	public static Bitmap warp(Bitmap image, MyPoint p1, MyPoint p2, MyPoint p3, MyPoint p4) {
	    int resultWidth = 500;
	    int resultHeight = 500;

	    Mat inputMat = new Mat(image.getHeight(), image.getHeight(), CvType.CV_8UC4);
	    Utils.bitmapToMat(image, inputMat);
	    Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);

	    Point ocvPIn1 = new Point(p1.getX(), p1.getY());
	    Point ocvPIn2 = new Point(p2.getX(), p2.getY());
	    Point ocvPIn3 = new Point(p3.getX(), p3.getY());
	    Point ocvPIn4 = new Point(p4.getX(), p4.getY());
	    List<Point> source = new ArrayList<Point>();
	    source.add(ocvPIn1);
	    source.add(ocvPIn2);
	    source.add(ocvPIn3);
	    source.add(ocvPIn4);
	    Mat startM = Converters.vector_Point2f_to_Mat(source);

	    Point ocvPOut1 = new Point(0, 0);
	    Point ocvPOut2 = new Point(0, resultHeight);
	    Point ocvPOut3 = new Point(resultWidth, resultHeight);
	    Point ocvPOut4 = new Point(resultWidth, 0);
	    List<Point> dest = new ArrayList<Point>();
	    dest.add(ocvPOut1);
	    dest.add(ocvPOut2);
	    dest.add(ocvPOut3);
	    dest.add(ocvPOut4);
	    Mat endM = Converters.vector_Point2f_to_Mat(dest);      

	    Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

	    Imgproc.warpPerspective(inputMat, 
	                            outputMat,
	                            perspectiveTransform,
	                            new Size(resultWidth, resultHeight), 
	                            Imgproc.INTER_CUBIC);

	    Bitmap output = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.RGB_565);
	    Utils.matToBitmap(outputMat, output);
	    return output;
	}
	
	/**
	 * image 转化为pdf
	 */
	public void startToPdf(){
		
		
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmProcessing.compress(Bitmap.CompressFormat.PNG, 100, stream);

        Document document = new Document();
        File f=new File(Environment.getExternalStorageDirectory(), "ImageProcessing.pdf");
    try {
		PdfWriter.getInstance(document,new FileOutputStream(f));
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (DocumentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    document.open();
   /* try {
		document.add(new Paragraph("Simple Image"));
	} catch (DocumentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/

    Image image = null;
	try {
		image = Image.getInstance(stream.toByteArray());
	} catch (BadElementException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	} catch (MalformedURLException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	
    try {
		document.add(image);
	} catch (DocumentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    
    document.close();
    
	}
		

	@Override
	public void onResume() {
	    super.onResume();
	    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5,this, mLoaderCallback);
	    // you may be tempted, to do something here, but it's *async*, and may take some time,
	    // so any opencv call here will lead to unresolved native errors.
	}
	
}
