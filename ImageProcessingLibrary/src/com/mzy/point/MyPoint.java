package com.mzy.point;

import android.graphics.Point;
import android.widget.Toast;


/**
 * @ClassName: MyPoint
 * @Description:  �Զ���㡣�����̳���Point
 * @author WANDERYUREN
 *
 */
public class MyPoint extends Point {
private  int x;
private  int y;

public MyPoint(int x,int y)
{
	super(x, y);
	this.x=x;
	this.y=y;
	
}

public int getX()
{
	return this.x;
}

public int getY()
{
	return this.y;
}



}
