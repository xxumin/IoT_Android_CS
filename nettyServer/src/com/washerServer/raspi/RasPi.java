package com.washerServer.raspi;

import java.util.Date;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.washerServer.main.HttpWasherServerHandler;

public class RasPi {
	RasPitoDB rasDB = new RasPitoDB();
	static HashMap<String, EndDevData> endDevOp = new HashMap<>();
	int workChangeTime =1;
	byte[] responseData = { 'r', 'a', 's', 'p', 'i' };

	public byte[] recieveJSONFromRaspi(String msg, String type) {
		System.out.println("recieve: " + msg);
		JSONParser parser = new JSONParser();
		JSONObject result = new JSONObject();
		try {
			JSONObject jsonObj = (JSONObject) parser.parse(msg);
			String HubAddr = (String) jsonObj.get("HubAddr");
			int EndDev_Num = Integer.parseInt(jsonObj.get("EndDev_Num").toString());
			rasDB.raspiCoordi(HubAddr, EndDev_Num);
			JSONArray endDevList = (JSONArray) jsonObj.get("endDevList");
			JSONArray LockList = new JSONArray();

			for (Object obj : endDevList) {
				JSONObject endDevObj = (JSONObject) obj;
				String EndDevAddr = (String) endDevObj.get("EndDevAddr");
				boolean LockStatus = (boolean) endDevObj.get("LockStatus");
				boolean WorkStatus = (boolean) endDevObj.get("WorkStatus");
				boolean SreachCode = (boolean) endDevObj.get("SreachCode");
				int BatteryStatus = Integer.parseInt(endDevObj.get("BatteryStatus").toString());
				
				JSONObject resultObj = new JSONObject();

				switch (rasDB.raspiEndDev(EndDevAddr, HubAddr, LockStatus, BatteryStatus)) {
				case "true":
					resultObj.put("EndDevAddr", EndDevAddr);
					resultObj.put("LockCode", "a");
					LockList.add(resultObj);
					break;
				case "false":
					resultObj.put("EndDevAddr", EndDevAddr);
					resultObj.put("LockCode", "b");
					LockList.add(resultObj);
					break;
				default:
					break;
				}
				rasDB.raspiSreachCode(EndDevAddr, SreachCode);
				
				if (!WorkStatus) {
					endDevCheck(EndDevAddr);					
				}else{
					endDevWorkSaver(EndDevAddr);
				}
			}
			JSONArray CallList = rasDB.raspiXbeeSreach(HubAddr);
			result.put("dataSave", "ok");
			result.put("HubAddr", HubAddr);
			result.put("LockList", LockList);
			result.put("CallList", CallList);

			HttpWasherServerHandler.dbcon.myCommit();
		} catch (Exception e) {
			e.printStackTrace();
			result.put("dataSave", "error");
			result.put("HubAddr", null);
			result.put("callList", null);
		}
		responseData = result.toString().getBytes();
		return responseData;
	}
	
	public void endDevCheck(String EndDevAddr) {
		EndDevData temp = endDevOp.get(EndDevAddr);
		if(temp.WorkStatus){
			temp.WorkStatus = false;
			temp.sendMsgCode = true;
			endDevOp.replace(EndDevAddr, temp);
		}
		//if((new Date(System.currentTimeMillis()).getTime()-temp.changeTime.getTime())>=workChangeTime*60000){
		if((new Date(System.currentTimeMillis()).getTime()-temp.changeTime.getTime())>=workChangeTime*30000){
			rasDB.workStatusChange(EndDevAddr,false); 
		}
	}
	
	public void endDevWorkSaver(String EndDevAddr) {
		EndDevData temp = endDevOp.get(EndDevAddr);
		temp.changeTime=new Date(System.currentTimeMillis());
		temp.WorkStatus = true;
		endDevOp.replace(EndDevAddr, temp);
		if(temp.BookStatus){
			rasDB.workStatusChange(EndDevAddr,true);
		}
	}	
}
