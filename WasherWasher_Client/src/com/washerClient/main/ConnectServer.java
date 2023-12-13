package com.washerClient.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.digi.xbee.api.models.XBee64BitAddress;
import com.washerClient.xbee.xbeeMain;

public class ConnectServer {

	public void setupConnectionToServer() throws Exception {
		//String httpsURL = "http://210.119.12.76:8080/raspi/data";
		 String httpsURL = "http://192.168.0.150:8080/raspi/data";
		URL myurl = new URL(httpsURL);
		HttpURLConnection con = (HttpURLConnection) myurl.openConnection();
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");// ������
																	// JSON����
																	// ����

		String param = xbeeDataJSON().toString();
		OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
		System.out.println("send : " + param);
		try {
			osw.write(param);
			osw.flush();

			// ����
			BufferedReader br = null;
			br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

			String line = null;
			while ((line = br.readLine()) != null) {
				responseJSON(line);
			}

			// �ݱ�
			osw.close();
			br.close();

		} catch (MalformedURLException e) {
			e.printStackTrace();

		} catch (ProtocolException e) {
			e.printStackTrace();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void responseJSON(String msg) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject jsonObj = (JSONObject) parser.parse(msg);

			System.out.println("Get Data From Server : " + jsonObj);

			if (xbeeMain.myDevice.get64BitAddress().toString().equals(jsonObj.get("HubAddr").toString())) {
				if (jsonObj.get("dataSave").toString().equals("ok")) {
				}
			}
			if (jsonObj.containsKey("LockList")) {
				JSONArray LockList = (JSONArray) jsonObj.get("LockList");
				for (Object item : LockList) {
					JSONObject endDev = (JSONObject) item;
					xbeeMain.sendXBeecode(endDev.get("EndDevAddr").toString(), endDev.get("LockCode").toString());
				}
			}
			if (jsonObj.containsKey("CallList")) {
				JSONArray CallList = (JSONArray) jsonObj.get("CallList");
				for (Object item : CallList) {
					JSONObject endDev = (JSONObject) item;
					xbeeMain.sendXBeecode(endDev.get("EndDevAddr").toString(), endDev.get("SreachCode").toString());
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public JSONObject xbeeDataJSON() {
		JSONObject result = new JSONObject();
		result.put("HubAddr", xbeeMain.myDevice.get64BitAddress().toString());
		result.put("EndDev_Num", (int) xbeeMain.deviceAddr.size());

		JSONArray endDevList = new JSONArray();
		for (XBee64BitAddress EndDevAddr : xbeeMain.deviceAddr) {
			char[] temp = xbeeMain.deviceState.get(EndDevAddr).toCharArray();
			boolean LockStatus, WorkStatus, SreachCode;
			int BatteryStatus;
			if (temp[0] == 'A') {
				LockStatus = true;
			} else {
				LockStatus = false;
			}
			if (temp[1] == 'A') {
				WorkStatus = true;
			} else {
				WorkStatus = false;
			}
			if (temp[2] == 'A') {
				SreachCode = true;
			} else {
				SreachCode = false;
			}
			if (temp[3] == 'A') {
				BatteryStatus = 2;
			} else if (temp[2] == 'B') {
				BatteryStatus = 1;
			} else {
				BatteryStatus = 0;
			}

			JSONObject endDevObj = new JSONObject();
			endDevObj.put("EndDevAddr", EndDevAddr.toString());
			endDevObj.put("LockStatus", LockStatus);
			endDevObj.put("WorkStatus", WorkStatus);
			endDevObj.put("SreachCode", SreachCode);
			endDevObj.put("BatteryStatus", BatteryStatus);
			endDevList.add(endDevObj);
		}
		result.put("endDevList", endDevList);
		return result;
	}
}
