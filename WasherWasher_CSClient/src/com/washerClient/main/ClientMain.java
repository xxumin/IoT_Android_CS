package com.washerClient.main;

import java.util.Timer;
import java.util.TimerTask;

import com.digi.xbee.api.models.XBee64BitAddress;
import com.washerClient.xbee.xbeeMain;

public class ClientMain {

	static int checkDev =0;
	public static void main(String[] args) {
		
		// TODO Auto-generated method stub
		xbeeMain xbeeMain = new xbeeMain();
		xbeeMain.startXbee();
		ConnectServer server = new ConnectServer();
		
		Timer dataSender = new Timer();
		TimerTask s_task=new TimerTask() {
			@Override
			public void run() {
				try {
					server.setupConnectionToServer();
					checkDev++;
					if(checkDev>10){
						for(XBee64BitAddress addr: xbeeMain.deviceAddr){
							if(!(xbeeMain.deviceActCkeck.get(addr)>0)){
								xbeeMain.deviceAddr.remove(addr);
								xbeeMain.deviceMap.remove(addr);
								xbeeMain.deviceState.remove(addr);
								xbeeMain.deviceActCkeck.remove(addr);
							}
							xbeeMain.deviceActCkeck.replace(addr,0);
						}
						checkDev=0;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		dataSender.schedule(s_task, 2000, 1000);
		
	}

}
