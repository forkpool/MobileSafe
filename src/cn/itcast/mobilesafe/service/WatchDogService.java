package cn.itcast.mobilesafe.service;

import java.util.ArrayList;
import java.util.List;

import cn.itcast.mobilesafe.db.dao.AppLockDao;
import cn.itcast.mobilesafe.ui.LockScreenActivity;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
/**
 * ������
 * @author Administrator
 *
 */
public class WatchDogService extends Service {
	 private List<String> lockInfoList = null;
	 private AppLockDao dao = null;
	 private ActivityManager am = null;
	 private Intent intent = null;
	 private boolean flag;
	 private List<String> tempstopapps;
	 private MyBinder myBinder;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return myBinder;
	}
	public class MyBinder extends Binder implements IService{

		public void callAppProtectStart(String packname) {
			// TODO Auto-generated method stub
			appProtectStart(packname);
		}

		public void callAppProtectStop(String packname) {
			// TODO Auto-generated method stub
			appProtectStop(packname);
		}

		

		
		
	}
	
    /**
     * ���¿�����Ӧ�ó���ı���
     */
	public void appProtectStart(String packname){
		if(tempstopapps.contains(packname)){
			tempstopapps.remove(packname);
		}
	}
	 /**
     *ֹͣ��Ӧ�ó���ı���
     */
	public void appProtectStop(String packname){
			tempstopapps.add(packname);
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		getContentResolver().registerContentObserver(Uri.parse("content://cn.itcast.applockprovider"), true, new mObserver(new Handler()));
		
		myBinder = new MyBinder();
		intent = new Intent(this,LockScreenActivity.class);
		// �����ǲ���������ջ�� Ҫ�ڷ������濪��activity�Ļ� �����������һ��flag
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		lockInfoList = new ArrayList<String>();
		dao = new AppLockDao(this);
		flag = true;
		//��ȡ���б����ĳ���
		lockInfoList = dao.getAllApps();
		tempstopapps = new ArrayList<String>();
		am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		new Thread(){

			@Override
			public void run() {
				
				while(flag){
					//����ϵͳ���������ջ����Ϣ��taskinfos�ļ�������ֻ��һ��Ԫ�أ����ݾ��ǵ�ǰ�������еĽ��̶�Ӧ������ջ
					//���ݾ��ǵ�ǰ�������еĽ��̶�Ӧ������ջ
					List<RunningTaskInfo> taskinfos = am.getRunningTasks(1);
					//��õ�ǰ��������Ϣ
					RunningTaskInfo currenttask = taskinfos.get(0);
//					�����˵İ���,�����û�����������İ���
					String packname = currenttask.topActivity.getPackageName();
					System.out.println("��ǰ����"+packname);
					if(lockInfoList.contains(packname)){
						if (tempstopapps.contains(packname)) {

							try {
								sleep(1000);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							continue;
						}
						System.out.println("��Ҫ����"+packname);
						intent.putExtra("packagename", packname);
						startActivity(intent);
					}else{
						
					}
					try {
						sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
                
			}
			
		}.start();
		
		
		
	}
    private class mObserver extends ContentObserver{

		public mObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onChange(boolean selfChange) {
			// TODO Auto-generated method stub
			super.onChange(selfChange);
			lockInfoList = dao.getAllApps();
		}
    	
    }
}












