package com.mzy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.mzy.imageprocessing.R;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;

/** 
 * @ClassName: FileOperateUtil 
 * @Description:  �ļ�����������
 * @author WANDERYUREN  
 */
public class FileOperateUtil {
	public final static String TAG="FileOperateUtil";

	public final static int ROOT=0;//��Ŀ¼
	public final static int TYPE_IMAGE=1;//ͼƬ
	public final static int TYPE_THUMBNAIL=2;//����ͼ

	/**
	 *��ȡ�ļ���·��
	 * @param type �ļ������
	 * @param rootPath ��Ŀ¼�ļ������� Ϊҵ����ˮ��
	 * @return
	 */
	public static String getFolderPath(Context context,int type,String rootPath) {
		//��ҵ���ļ���Ŀ¼
		StringBuilder pathBuilder=new StringBuilder();
		//���Ӧ�ô洢·��
		pathBuilder.append(context.getExternalFilesDir(null).getAbsolutePath());
		pathBuilder.append(File.separator);
		//����ļ���Ŀ¼
		pathBuilder.append(context.getString(R.string.Files));
		pathBuilder.append(File.separator);
		//��ӵ�Ȼ�ļ�����·��
		pathBuilder.append(rootPath);
		pathBuilder.append(File.separator);
		switch (type) {
		case TYPE_IMAGE:
			pathBuilder.append(context.getString(R.string.Image));
			break;
		case TYPE_THUMBNAIL:
			pathBuilder.append(context.getString(R.string.Thumbnail));
			break;
		default:
			break;
		}
		return pathBuilder.toString();
	}

	/**
	 * ��ȡĿ���ļ�����ָ����׺�����ļ�����,�����޸���������
	 * @param file Ŀ���ļ���·��
	 * @param extension ָ����׺��
	 * @param content ����������,���Բ�����Ƶ����ͼ
	 * @return
	 */
	public static List<File> listFiles(String file,final String format,String content){
		return listFiles(new File(file), format,content);
	}
	public static List<File> listFiles(String file,final String format){
		return listFiles(new File(file), format,null);
	}
	/**
	 * ��ȡĿ���ļ�����ָ����׺�����ļ�����,�����޸���������
	 * @param file Ŀ���ļ���
	 * @param extension ָ����׺��
	 * @param content ����������,���Բ�����Ƶ����ͼ
	 * @return
	 */
	public static List<File> listFiles(File file,final String extension,final String content){
		File[] files=null;
		if(file==null||!file.exists()||!file.isDirectory())
			return null;
		files=file.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File arg0, String arg1) {
				// TODO Auto-generated method stub
				if(content==null||content.equals(""))
					return arg1.endsWith(extension);
				else {
                    return  arg1.contains(content)&&arg1.endsWith(extension);           		
				}
			}
		});
		if(files!=null){
			List<File> list=new ArrayList<File>(Arrays.asList(files));
			sortList(list, false);
			return list;
		}
		return null;
	}

	/**  
	 *  �����޸�ʱ��Ϊ�ļ��б�����
	 *  @param list ������ļ��б�
	 *  @param asc  �Ƿ��������� trueΪ���� falseΪ���� 
	 */
	public static void sortList(List<File> list,final boolean asc){
		//���޸���������
		Collections.sort(list, new Comparator<File>() {
			public int compare(File file, File newFile) {
				if (file.lastModified() > newFile.lastModified()) {
					if(asc){
						return 1;
					}else {
						return -1;
					}
				} else if (file.lastModified() == newFile.lastModified()) {
					return 0;
				} else {
					if(asc){
						return -1;
					}else {
						return 1;
					}
				}

			}
		});
	}

	/**
	 * 
	 * @param extension ��׺�� ��".jpg"
	 * @return
	 */
	public static String createFileNmae(String extension){
		DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss",Locale.getDefault());
		// ת��Ϊ�ַ���
		String formatDate = format.format(new Date());
		//�鿴�Ƿ��"."
		if(!extension.startsWith("."))
			extension="."+extension;
		return formatDate+extension;
	}

	/**  
	 *  ɾ������ͼ ͬʱɾ��Դͼ
	 *  @param thumbPath ����ͼ·��
	 *  @return   
	 */
	public static boolean deleteThumbFile(String thumbPath,Context context) {
		boolean flag = false;

		File file = new File(thumbPath);
		if (!file.exists()) { // �ļ�������ֱ�ӷ���
			return flag;
		}

		flag = file.delete();
		//Դ�ļ�·��
		String sourcePath=thumbPath.replace(context.getString(R.string.Thumbnail),
				context.getString(R.string.Image));
		file = new File(sourcePath);
		if (!file.exists()) { // �ļ�������ֱ�ӷ���
			return flag;
		}
		flag = file.delete();
		return flag;
	}
	/**  
	 *  ɾ��Դͼ ͬʱɾ������ͼ
	 *  @param sourcePath ����ͼ·��
	 *  @return   
	 */
	public static boolean deleteSourceFile(String sourcePath,Context context) {
		boolean flag = false;

		File file = new File(sourcePath);
		if (!file.exists()) { // �ļ�������ֱ�ӷ���
			return flag;
		}

		flag = file.delete();
		//����ͼ�ļ�·��
		String thumbPath=sourcePath.replace(context.getString(R.string.Image),
				context.getString(R.string.Thumbnail));
		file = new File(thumbPath);
		if (!file.exists()) { // �ļ�������ֱ�ӷ���
			return flag;
		}
		flag = file.delete();
		return flag;
	}
}