package com.jdsh.example;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.jdsh.sdk.ir.JdshIRInterface;
import com.jdsh.sdk.ir.JdshIRInterfaceImpl;
import com.jdsh.sdk.ir.model.Brand;
import com.jdsh.sdk.ir.model.BrandResult;
import com.jdsh.sdk.ir.model.DeviceType;
import com.jdsh.sdk.ir.model.DeviceTypeResult;
import com.jdsh.sdk.ir.model.IRMessage;
import com.jdsh.sdk.ir.model.MatchRemoteControl;
import com.jdsh.sdk.ir.model.MatchRemoteControlResult;
import com.jdsh.sdk.ir.model.RemoteControl;
import com.jdsh.sdk.utils.Utility;

public class MainActivity extends Activity implements View.OnClickListener {
	
	private ProgressDialogUtils dialogUtils ;
	
	private JdshIRInterface irInterface;
	
	private String TAG = MainActivity.class.getSimpleName();
	
	private TextView showText ;
	
	private String appID = "apidemo";
	
	private String deviceId = "test_device";
	
	private String url = "https://api.jdshtech.com/open/m.php";//集成时可以把域名配置在应用的初始化方法中，方便升级
	
	private List<DeviceType> deviceType = new ArrayList<DeviceType>() ;//设备类型
	
	private List<Brand> brands = new ArrayList<Brand>() ; //品牌
	
	private List<MatchRemoteControl> remoteControls = new ArrayList<MatchRemoteControl>();//遥控器列表
	
	private List<String> nameType = new ArrayList<String>();
	private List<String> nameBrands  = new ArrayList<String>();
	private List<String> nameRemote = new ArrayList<String>();
	
	private MatchRemoteControlResult controlResult = null;// 匹配列表
	
	private RemoteControl remoteControl = null ; //遥控器对象
	
	private MatchRemoteControl currRemoteControl = null ; //当前匹配的遥控器对象
	
	private DeviceType currDeviceType = null ; //当前设备类型
	
	private Brand currBrand = null ; //当前品牌
	
	private Spinner spType ,spBrands , spRemotes; 
	
	private ArrayAdapter<String> typeAdapter,brandAdapter,remoteAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		dialogUtils = new ProgressDialogUtils(this);
		showText = (TextView)findViewById(R.id.showText);
		//初始化sdk
		irInterface = new JdshIRInterfaceImpl(getApplicationContext(),url, appID, deviceId);	
		initView();
	}
	
	private void initView() {
		spType = (Spinner) findViewById(R.id.spType);
		spBrands = (Spinner)findViewById(R.id.spBrand);
		spRemotes = (Spinner)findViewById(R.id.spData);
		typeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,nameType);
		brandAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,nameBrands);
		remoteAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,nameRemote);
		typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		remoteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spType.setAdapter(typeAdapter);
		spBrands.setAdapter(brandAdapter);
		spRemotes.setAdapter(remoteAdapter);
		spType.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parent, View view, 
		            int pos, long id) {
		    	currDeviceType = deviceType.get(pos);
		    }
		    @Override
		    public void onNothingSelected(AdapterView<?> parent) {
		    }
		});
		spBrands.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parent, View view, 
		            int pos, long id) {
		    	currBrand = brands.get(pos);
		    }
		    @Override
		    public void onNothingSelected(AdapterView<?> parent) {
		    }
		});
		
		spRemotes.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parent, View view, 
		            int pos, long id) {
		    	currRemoteControl = remoteControls.get(pos);
		    }
		    @Override
		    public void onNothingSelected(AdapterView<?> parent) {
		    }
		});
	}
	
	@Override
	public void onClick(View v) {
		new DownloadThread(v.getId()).start();
	}
	
	class DownloadThread extends Thread{
		private int viewId ;
		public  DownloadThread(int viewId){
			this.viewId  = viewId ;
		}
		@Override
		public void run() {
		dialogUtils.sendMessage(1);
		String result = "";
		Message message = mHandler.obtainMessage()  ;
		try{
			switch (viewId) {
			case R.id.registerDevice:
				 String registerDevice = irInterface.registerDevice();
				 result = registerDevice;
				 Log.d(TAG, " registerDevice result:" + registerDevice);
				break;
			case R.id.getDeviceType:
				 DeviceTypeResult deviceResult = irInterface.getDeviceType();
				 deviceType = deviceResult.getRs();
				 result = deviceResult.toString();
				 message.what = 0 ;
				 Log.d(TAG, " getDeviceType result:" +result);
				break;
			case R.id.getBrandByType:
				 if(currDeviceType != null){
					 BrandResult brandResult = irInterface.getBrandsByType(currDeviceType.getTid());
					 brands = brandResult.getRs();
					 result = brandResult.toString();
					 message.what = 1 ;
					 Log.d(TAG, " getBrandByType result:" +brandResult);
				 }else{
					 result = "请调用获取设备接口";
				 }
				break;
			case R.id.getMatchedDataByBrand:
				 if(currBrand != null){
					 controlResult = irInterface.getRemoteMatched(currBrand.getBid(),currDeviceType.getTid(),3,1);
					 remoteControls = controlResult.getRs();
					 result = controlResult.toString();
					 message.what = 2 ;
					 Log.d(TAG, " getMatchedDataByBrand result:" +result); 
				 }else{
					 result = "请调用获取设备接口";
				 }
				 Log.d(TAG, " getMatchedDataByBrand result:" +result);
				break;
			case R.id.getDetailByRCID:
				if(!Utility.isEmpty(currRemoteControl)){
					remoteControl = irInterface.getRemoteDetails(currRemoteControl.getRid(),1);
					result = remoteControl.toString();
				}else{
				   	 result = "请调用匹配数据接口";
					 Log.e(TAG, " getDetailByRCID 没有遥控器设备对象列表");
				}
				 Log.d(TAG, " getDetailByRCID result:" +result);
				
				break;
			case R.id.getFastMatched:
				MatchRemoteControlResult rcFastMatched = irInterface.getFastMatched(1101, 7, "1,38000,167,167,20,61,20,20,20,61,20,61,20,20,20,20,20,61,20,20,20,20,20,61,20,20,20,20,20,61,20,61,20,20,20,61,20,61,20,20,20,61,20,61,20,61,20,61,20,61,20,61,20,20,20,61,20,20,20,20,20,20,20,20,20,20,20,20,20,61,20,61,20,20,20,61,20,20,20,20,20,20,20,20,20,20,20,20,20,61,20,20,20,61,20,61,20,61,20,61,20,198,167,167,20,61,20,20,20,61,20,61,20,20,20,20,20,61,20,20,20,20,20,61,20,20,20,20,20,61,20,61,20,20,20,61,20,61,20,20,20,61,20,61,20,61,20,61,20,61,20,61,20,20,20,61,20,20,20,20,20,20,20,20,20,20,20,20,20,61,20,61,20,20,20,61,20,20,20,20,20,20,20,20,20,20,20,20,20,61,20,20,20,61,20,61,20,61,20,61,20,198",1);
				result = rcFastMatched.toString();
				Log.d(TAG, " getFastMatched result:" +result);
				break;
			case R.id.learnUpload:
				IRMessage msg = irInterface.learnUpload(1101, 7, "testBeModel","testRcmodel","1,38000,167,167,20,61,20,20,20,61,20,61,20,20,20,20,20,61,20,20,20,20,20,61,20,20,20,20,20,61,20,61,20,20,20,61,20,61,20,20,20,61,20,61,20,61,20,61,20,61,20,61,20,20,20,61,20,20,20,20,20,20,20,20,20,20,20,20,20,61,20,61,20,20,20,61,20,20,20,20,20,20,20,20,20,20,20,20,20,61,20,20,20,61,20,61,20,61,20,61,20,198,167,167,20,61,20,20,20,61,20,61,20,20,20,20,20,61,20,20,20,20,20,61,20,20,20,20,20,61,20,61,20,20,20,61,20,61,20,20,20,61,20,61,20,61,20,61,20,61,20,61,20,20,20,61,20,20,20,20,20,20,20,20,20,20,20,20,20,61,20,61,20,20,20,61,20,20,20,20,20,20,20,20,20,20,20,20,20,61,20,20,20,61,20,61,20,61,20,61,20,198");
				result = msg.toString();
				Log.d(TAG, " learnUpload result:" +result);
				break;
			default:
				break;
			}
		}catch(Exception exp){
			Log.e(TAG, "error:" +exp.getMessage());
		}
		message.obj = result;
		mHandler.sendMessage(message);
		dialogUtils.sendMessage(0);
		}
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			showText.setText("appID:"+appID +"\n" + (String)msg.obj);
			switch (msg.what) {
			case 0:
				if(deviceType != null){
					nameType.clear();
					for(int i = 0 ; i < deviceType.size() ; i++){
						nameType.add(deviceType.get(i).getName());
					}
				}
				typeAdapter.notifyDataSetInvalidated();
				spType.setAdapter(typeAdapter);
				break;
			case 1:
				if(brands != null){
					nameBrands.clear();
					for(int i = 0 ; i < brands.size() ; i++){
						nameBrands.add(brands.get(i).getName());
					}
				}
				brandAdapter.notifyDataSetInvalidated();
				spBrands.setAdapter(brandAdapter);
				break;
			case 2:
				if(remoteControls != null){
					nameRemote.clear();
					for(int i = 0 ; i < remoteControls.size() ; i++){
						nameRemote.add(remoteControls.get(i).getName() + "-" +remoteControls.get(i).getRmodel());
					}
				}
				remoteAdapter.notifyDataSetInvalidated();
				spRemotes.setAdapter(remoteAdapter);
				break;
			default:
				break;
			}
		};
	};
	
}
