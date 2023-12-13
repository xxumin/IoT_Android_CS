package com.washerServer.clientPC;

import javax.swing.JComponent;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.netty.handler.codec.json.JsonObjectDecoder;

public class ClientPC {
	ClientPCtoDB pcToDB = new ClientPCtoDB();
	JSONObject resJson = null;
	JSONParser parser = new JSONParser();
	String response = null;
	//POST
	public byte[] recieveJSONFromPC(String msgJson, String checkCode) {
		switch (checkCode) {
		case "login": 
			response = loginHttpRequest(msgJson);
			break;
		case "register": 
			response = registerHttpRequest(msgJson);
			break;
		case "devManage":
			response = devManegeRequest(msgJson);
			break;
		case "lock":
			response = lockDevHttpRequest(msgJson);
			break;
		case "devStatus":
			response = washerNumChangeHttpRequest(msgJson);
			break;
		case "shopUpdate":
			response = shopUpdateHttpRequest(msgJson);
			break;
		case "deputy":
			response = deputyOkHttpRequest(msgJson);
			break;
		case "devSearch":
			response = devSearchHttpRequest(msgJson);
			break;
		case "bookAssign":
			response = bookAssignHttpRequest(msgJson);
			break;
		case "avgTime":
			response = avgTimeHttpRequest(msgJson);
			break;
		}
		System.out.println(response);
		return response.getBytes();
	}
	//GET
	public byte[] recieveGETFromPC(String msgJson, String checkCode, String data) {
		switch (checkCode) {
		case "data":
			response = dataHttpRequest(msgJson, data);
			break;
		case "devStatus":
			response = devStatusHttpRequest(msgJson, data);
			break;		
		case "deputy":
			response = deputyListHttpRequest(msgJson, data);
			break;
		/*case "book":
			response = bookListHttpRequest(msgJson, data);
			break;*/
		}
		System.out.println(response);
		return response.getBytes();
	}
	
	//POST
	//로그인
	private String loginHttpRequest(String msg) {
		try {
			resJson = new JSONObject();
			JSONObject jsonObj = (JSONObject) parser.parse(msg);
			String LicenseNum = jsonObj.get("LicenseNum").toString();
			String Pwd = jsonObj.get("Pwd").toString();
			
			String searchDB = pcToDB.loginDB(LicenseNum, Pwd);
			if (!(searchDB == null)) {
				String[] temp = searchDB.split("/");
				resJson.put("Result", "ok");
				resJson.put("ManagerNo", temp[0]);
				resJson.put("SerialNum", temp[1]);
				resJson.put("ShopName", temp[2]);
				resJson.put("ShopAddr", temp[3]);
				resJson.put("OpenTime", temp[4]);
				resJson.put("CloseTime", temp[5]);
				resJson.put("AvgTime", temp[6]);
			} else {
				resJson.put("Result", "fail");				
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return resJson.toString();
	}
	//회원가입
	private String registerHttpRequest(String msg) {
		try {
			resJson = new JSONObject();
			JSONObject jsonObj = (JSONObject) parser.parse(msg);
			String LicenseNum = jsonObj.get("LicenseNum").toString();
			String Pwd = jsonObj.get("Pwd").toString();
			String SerialNum = jsonObj.get("SerialNum").toString();
			String ShopName = jsonObj.get("ShopName").toString();
			String ShopAddr = jsonObj.get("ShopAddr").toString();
			String openTime = jsonObj.get("OpenTime").toString();
			String closeTime = jsonObj.get("CloseTime").toString();
			String ShopGPS = jsonObj.get("ShopGPS").toString(); 
			if (pcToDB.resgisterDB(LicenseNum, Pwd, ShopName, ShopAddr, openTime, closeTime, SerialNum, ShopGPS)) {
				resJson.put("Result", "ok");
			} else {
				resJson.put("Result", "fail");	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resJson.toString();
	}
	//세탁기 관리(장치 제거 및 번호 등록)
	private String devManegeRequest(String msg) {
		try {
			resJson = new JSONObject();
			JSONObject jsonObj = (JSONObject) parser.parse(msg);
			String endDevAddr = jsonObj.get("EndDevAddr").toString();
			int WasherNum = Integer.parseInt(jsonObj.get("WasherNum").toString());
			String action = jsonObj.get("Action").toString();
			
			if (pcToDB.devMngUpdate(WasherNum, endDevAddr, action)) {
				resJson.put("Result", "ok");
			} else {
				resJson.put("Result", "fail");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resJson.toString();
	}
	//세탁기 열기 및 닫기
	private String lockDevHttpRequest(String msg) {
		try {
			resJson = new JSONObject();
			JSONObject jsonObj;
			jsonObj = (JSONObject) parser.parse(msg);
			String endDevAddr = jsonObj.get("EndDevAddr").toString();
			String action = jsonObj.get("Action").toString();
			
			if (pcToDB.openDev(endDevAddr, action)) {
				resJson.put("Result", "ok");
			} else {
				resJson.put("Result", "fail");
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resJson.toString();
	}
	//세탁기 번호 부여
	private String washerNumChangeHttpRequest(String msg) {
		try {
			resJson = new JSONObject();
			JSONObject jsonObj;
			jsonObj = (JSONObject) parser.parse(msg);
			String SerialNum = jsonObj.get("SerialNum").toString();
			String WasherNum = jsonObj.get("WasherNum").toString();
			String cleanDay = pcToDB.cleanDev(SerialNum, WasherNum);
			if (cleanDay != null) {
				resJson.put("Result", "ok");
				resJson.put("LastClean", cleanDay);
			} else {
				resJson.put("Result", "fail");
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resJson.toString();
	}
	
	//세탁소 수정
	private String shopUpdateHttpRequest(String msg) {
		try {
			resJson = new JSONObject();
			JSONObject jsonObj;
			jsonObj = (JSONObject) parser.parse(msg);
			String ManagerNo = jsonObj.get("ManagerNo").toString();
			String ShopName = jsonObj.get("ShopName").toString();
			String ShopAddr = jsonObj.get("ShopAddr").toString();
			String ShopGPS = jsonObj.get("ShopGPS").toString();
			String SerialNum = jsonObj.get("SerialNum").toString();
			String OpenTime = jsonObj.get("OpenTime").toString();
			String CloseTime = jsonObj.get("CloseTime").toString();
			
			if (pcToDB.updateShop(ManagerNo, ShopName, ShopAddr, ShopGPS, SerialNum, OpenTime, CloseTime)) {
				resJson.put("Result", "ok");
			} else {
				resJson.put("Result", "fail");
			}			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resJson.toString();
	}
	//대리 수령 완료
	private String deputyOkHttpRequest(String msg) {
		try {
			resJson = new JSONObject();
			JSONObject jsonObj;
			jsonObj = (JSONObject) parser.parse(msg);
			
			String BookingNo = jsonObj.get("BookingNo").toString();
			String WasherNum = jsonObj.get("WasherNum").toString();
			
			if (pcToDB.deputyOK(BookingNo, WasherNum)) {
				resJson.put("Result", "ok");
			} else {
				resJson.put("Result", "fail");
			}
		} catch (ParseException e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		return resJson.toString();
	}
	//장치 찾기
	private String devSearchHttpRequest(String msg) {
		try {
			resJson = new JSONObject();
			JSONObject jsonObj;
			jsonObj = (JSONObject) parser.parse(msg);
			
			String SerialNum = jsonObj.get("SerialNum").toString();
			String EndDevAddr = jsonObj.get("EndDevAddr").toString();
			
			if (pcToDB.searchDev(SerialNum, EndDevAddr)) {
				resJson.put("Result", "ok");
			} else {
				resJson.put("Result", "fail");
			}
		}catch (ParseException e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		return resJson.toString();
	}
	//예약자 세탁기 할당
	private String bookAssignHttpRequest(String msg) {
		try {
			resJson = new JSONObject();
			JSONObject jsonObj;
			jsonObj = (JSONObject) parser.parse(msg);
			
			String ManagerNo = jsonObj.get("ManagerNo").toString();
			JSONArray bookDev = (JSONArray) parser.parse(jsonObj.get("BookDev").toString());
			if (pcToDB.bookDevAssign(ManagerNo, bookDev)) {
				resJson.put("Result", "ok");
			} else {
				resJson.put("Result", "fail");
			}
			
		}catch (ParseException e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		return resJson.toString();
	}
	//평균 이용 시간대
	private String avgTimeHttpRequest(String msg) {
		try {
			resJson = new JSONObject();
			
			JSONObject jsonObj;
			jsonObj = (JSONObject) parser.parse(msg);
			String ManagerNo = jsonObj.get("ManagerNo").toString();
			String Action = jsonObj.get("Action").toString();
			String time = jsonObj.get("Time").toString();
			JSONArray resArray = pcToDB.avgTime(ManagerNo, Action, time);
			if (!(resArray == null)) {
				resJson.put("Result", "ok");
				resJson.put("AvgTime", resArray);				
			} else {
				resJson.put("Result", "fail");
			}			
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		
		return resJson.toString();
	}
	//GET	
	//polling 데이터 응답
	private String dataHttpRequest(String msg, String data) {
		try {
			String[] temp = data.split("&");
			resJson = new JSONObject();
			JSONArray devArray = pcToDB.pollingData(temp[0]);
			JSONArray bookArray = pcToDB.bookList(temp[1]);
			if (!(devArray == null) && !(bookArray == null)) {
				resJson.put("Result", "ok");
				resJson.put("EndDev", devArray);
				resJson.put("bookList", bookArray);
			} else {
				resJson.put("Result", "fail");
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		return resJson.toString();
	}
	//세탁기 점검 현황 응답
	private String devStatusHttpRequest(String msg, String SerialNum) {
		try {
			resJson = new JSONObject();
			JSONArray resArray = pcToDB.devStatus(SerialNum);
			if (!(resArray == null)) {
				resJson.put("Result", "ok");
				resJson.put("endDev", resArray);				
			} else {
				resJson.put("Result", "fail");
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		
		return resJson.toString();
	}	
	//대리수령자 리스트 
	private String deputyListHttpRequest(String msg, String ManagerNo) {
		try	{
			resJson = new JSONObject();
			JSONArray resArray = pcToDB.deputyList(ManagerNo);
			if (resArray != null) {
				resJson.put("Result", "ok");
				resJson.put("deputyList", resArray);				
			} else {
				resJson.put("Result", "fail");				
			}
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		return resJson.toString();
	}
	//예약 리스트
	/*private String bookListHttpRequest(String msg, String ManagerNo) {
		try {
			resJson = new JSONObject();	
			JSONArray resArray = pcToDB.bookList(ManagerNo);
			if (!(resArray == null)) {
				resJson.put("Result", "ok");
				resJson.put("bookList", resArray);				
			} else {
				resJson.put("Result", "fail");				
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}		
		return resJson.toString();
	}*/	
}
