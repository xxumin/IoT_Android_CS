package com.washerServer.clientPC;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.washerServer.DBCon.DBConnection;
import com.washerServer.main.HttpWasherServerHandler;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

public class ClientPCtoDB {
	DBConnection dbcon = HttpWasherServerHandler.dbcon;
	Connection con = dbcon.getConnection();

	private final static String AUTH_KEY_FCM = "AAAAF6boTCA:APA91bGf_Sl0p25rBwFYEj95ImqFx7FfdhZtg_czuExxIvH8QEVOWn3p8BB34gyJedM5SuEERpUss9mtPn8Xjm0aH889pzE24hXbFxBMrAumcfkPMaZgeVm9D6fILFjFmkad_0a9ye2A";
	private final static String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";

	private void sendPushMsg(String BookingNo, boolean isBook) {	
		try {
			Statement s = con.createStatement();
			String sql = String.format("SELECT U.Token, E.WasherNum " + 
					"	FROM booking_tb B" + 
					"	LEFT JOIN muser_tb U" + 
					"		ON B.UserNo=U.UserNo" +
					"	LEFT JOIN enddev_tb E" + 
					"		ON B.EndDevAddr=E.EndDevAddr" + 
					"	WHERE B.BookingNo=%s" , BookingNo);
			ResultSet res = s.executeQuery(sql);

			while(res.next()) {
				String token = res.getString("Token");
				String WasherNum = res.getString("WasherNum");

				URL url = new URL(API_URL_FCM);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();

				conn.setUseCaches(false);
				conn.setDoInput(true);
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Authorization", "key=" + AUTH_KEY_FCM);
				conn.setRequestProperty("Content-Type", "application/json");
				JSONObject json = new JSONObject();
				JSONObject info = new JSONObject();

				if (isBook) {
					info.put("body", WasherNum + "번 세탁기를 할당받았습니다."); // Notification body
				} else {
					info.put("body", WasherNum + "번 세탁기의 대리 수령이 완료되었습니다.");					
				}
				json.put("notification", info);
				//json.put("data", msgObj);
				json.put("to", token); // deviceID

				try(OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8")){ 
					wr.write(json.toString());
					wr.flush();
				}catch(Exception e){
				}

				if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
				}

				BufferedReader br = new BufferedReader(new InputStreamReader(
						(conn.getInputStream())));

				String output;
				System.out.println("Output from Server .... \n");
				while ((output = br.readLine()) != null) {
					System.out.println(output);
				}

				conn.disconnect();
			}		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	//POST
	//로그인
	public String loginDB(String LicenseNum, String Pwd) {
		try {
			Statement s =  con.createStatement();
			String sql = String.format("SELECT ManagerNo, SerialNum, Pwd, ShopName, ShopAddr, OpenTime, CloseTime "
					+ "FROM manager_tb WHERE LicenseNum=%s", LicenseNum);
			ResultSet res = s.executeQuery(sql);
			con.commit();
			int ManagerNo;
			String SerialNum;
			String ShopName;
			String ShopAddr;
			Time OpenTime;
			Time CloseTime;
			res.next();	
			int passwd = res.getInt("Pwd");
			if (passwd == Integer.parseInt(Pwd)) {
				ManagerNo = res.getInt("ManagerNo");
				SerialNum = res.getString("SerialNum");
				ShopName = res.getString("ShopName");
				ShopAddr = res.getString("ShopAddr");
				OpenTime = res.getTime("OpenTime");
				CloseTime = res.getTime("CloseTime");
				sql = String.format(
						"SELECT sec_to_time(avg(time_to_sec(TIMEDIFF(workEnd,workStart)))) AS AvgTime "
						+ "	FROM workdata_tb W "
						+ "	INNER JOIN booking_tb B "
						+ "		ON W.BookingNo = B.BookingNo "
						+ "	where workEnd is not null AND B.ManagerNo=%d;",
						ManagerNo);
				res = s.executeQuery(sql);
				res.next();
				Time AvgTime = res.getTime("AvgTime");
				s.close();
				if (AvgTime == null) {
					AvgTime = new Time(1,0,0);
				}
				return ManagerNo + "/" + SerialNum + "/" + ShopName + "/" + ShopAddr + "/" + OpenTime.toString() + "/" + CloseTime.toString() + "/" + AvgTime.toString();
			}			
		} catch (SQLException e) {
			e.printStackTrace();		
		}
		return null;	
	}
	
	//회원가입
	public boolean resgisterDB(String LicenseNum, String Pwd, String ShopName, String ShopAddr, 
			String OpenTime, String CloseTime,String SerialNum, String ShopGPS) {
		try {
			Statement s =  con.createStatement();

			String sql = String.format("INSERT INTO manager_tb"
					+ "(LicenseNum, Pwd, ShopName, ShopAddr, OpenTime, CloseTime, serialNum, ShopGPS) "
					+ "VALUES (%s, %s, '%s', '%s', \"%s\", \"%s\", '%s', '%s');", 
					LicenseNum, Pwd, ShopName, ShopAddr, 
					OpenTime, CloseTime, SerialNum, ShopGPS);
			s.executeUpdate(sql);
			con.commit();
			s.close();

			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	//장치 업데이트 및 제거
	public boolean devMngUpdate(int WasherNum, String EndDevAddr, String action) {
		try {
			Statement s = con.createStatement();
			String sql;
			if (action.equals("Update")) {
				sql = String.format("UPDATE enddev_tb "
						+ "SET WasherNum=%d "
						+ "WHERE EndDevAddr='%s';"
						, WasherNum, EndDevAddr);
			} else {
				sql = String.format("UPDATE enddev_tb "
						+ "SET WasherNum=99999 "
						+ "WHERE EndDevAddr='%s';", 
						EndDevAddr);
			}
			s.executeUpdate(sql);
			con.commit();
			s.close();

			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	//세탁기 열기 및 닫기	
	public boolean openDev(String EndDevAddr, String Action) {
		try {
			Statement s = con.createStatement();
			String sql = String.format(
					"UPDATE enddev_tb SET LockStatus=%d WHERE EndDevAddr='%s'", 
					Action.equals("Open")?0:1 ,
							EndDevAddr);
			s.executeUpdate(sql);
			con.commit();
			s.close();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	//세탁기 청소 완료
	public String cleanDev(String SerialNum, String WasherNum) {
		try {
			Statement s = con.createStatement();
			String sql = String.format("UPDATE enddev_tb SET LastClean=NOW(), UsedNum=0 "
					+ "WHERE WasherNum=%s "
					+ "AND HubAddr=(SELECT HubAddr FROM coordi_tb WHERE SerialNum='%s');", 
					WasherNum, SerialNum);
			s.executeUpdate(sql);
			con.commit();
			s.close();

			Date date = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");
			return format.format(date).toString();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	//세탁소 수정
	public boolean updateShop(String ManagerNo, String ShopName, String ShopAddr, String ShopGPS, String SerialNum, String OpenTime, String CloseTime) {
		try {
			Statement s = con.createStatement();
			String sql = String.format("UPDATE manager_tb "
					+ "SET ShopName='%s', ShopAddr='%s', ShopGPS='%s', SerialNum='%s', OpenTime=\"%s\", CloseTime=\"%s\""
					+ "WHERE ManagerNo=%s;", 
					ShopName, ShopAddr, ShopGPS, SerialNum, OpenTime, CloseTime, ManagerNo);
			s.executeUpdate(sql);
			con.commit();
			s.close();
			return true;
		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
	}
	//대리수령 완료
	public boolean deputyOK(String BookingNo, String WasherNum) {
		try {
			Statement s = con.createStatement();
			String sql = String.format("UPDATE booking_tb "
					+ "SET Delegate=2 "
					+ "WHERE BookingNo=%s;", BookingNo);
			System.out.println(sql);
			s.executeUpdate(sql);
			con.commit();		
			s.close();

			sendPushMsg(BookingNo, false);

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			// TODO: handle exception
			return false;
		}
	}
	//예약자 세탁기 할당
	public boolean bookDevAssign(String ManagerNo, JSONArray bookDev) {
		try {
			Statement s = con.createStatement();
			for (Object obj : bookDev) {
				if (obj instanceof JSONObject) {
					JSONObject resJson = (JSONObject)obj;
					String EndDevAddr = resJson.get("EndDevAddr").toString();
					String BookingNo = resJson.get("BookingNo").toString();
					String sql = String.format(
							"UPDATE booking_tb B, enddev_tb E "
									+ "SET B.EnddevAddr = '%s', "
									+ "	E.BookStatus = 1 "
									+ "WHERE B.BookingNo=%s "
									+ "AND E.EndDevAddr='%s';",
									EndDevAddr, BookingNo, EndDevAddr);
					s.executeUpdate(sql);	

					sendPushMsg(BookingNo, true);
				}
			}			
			con.commit();
			s.close();		
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			// TODO: handle exception
			return false;
		}
	}	
	//장치 인터럽트
	public boolean searchDev(String SerialNum, String EndDevAddr) {
		try {
			Statement s = con.createStatement();
			String sql = String.format("UPDATE enddev_tb "
					+ "SET SreachCode=1 "
					+ "WHERE EndDevAddr='%s';", EndDevAddr);
			s.executeUpdate(sql);
			con.commit();
			s.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			// TODO: handle exception
			return false;
		}
	}
	//평균 이용 시간대
	public JSONArray avgTime(String ManagerNo, String Action, String time) {
		try {
			JSONArray resArray = new JSONArray();
			Statement s = con.createStatement();

			String sql;
			ResultSet res = null;
			switch (Action) {
			case "DAY":
				sql = String.format(
						"SELECT COUNT(IF(HOUR(W.workStart)=1,1,null)) AS '1', "
						+ "		COUNT(IF(HOUR(W.workStart)=2,1,null)) AS '2', "
						+ "		COUNT(IF(HOUR(W.workStart)=3,1,null)) AS '3', "
						+ "		COUNT(IF(HOUR(W.workStart)=4,1,null)) AS '4', "
						+ "		COUNT(IF(HOUR(W.workStart)=5,1,null)) AS '5', "
						+ "		COUNT(IF(HOUR(W.workStart)=6,1,null)) AS '6', "
						+ "		COUNT(IF(HOUR(W.workStart)=7,1,null)) AS '7', "
						+ "		COUNT(IF(HOUR(W.workStart)=8,1,null)) AS '8', "
						+ "		COUNT(IF(HOUR(W.workStart)=9,1,null)) AS '9', "
						+ "		COUNT(IF(HOUR(W.workStart)=10,1,null)) AS '10', "
						+ "		COUNT(IF(HOUR(W.workStart)=11,1,null)) AS '11', "
						+ "		COUNT(IF(HOUR(W.workStart)=12,1,null)) AS '12', "
						+ "		COUNT(IF(HOUR(W.workStart)=13,1,null)) AS '13', "
						+ "		COUNT(IF(HOUR(W.workStart)=14,1,null)) AS '14', "
						+ "		COUNT(IF(HOUR(W.workStart)=15,1,null)) AS '15', "
						+ "		COUNT(IF(HOUR(W.workStart)=16,1,null)) AS '16', "
						+ "		COUNT(IF(HOUR(W.workStart)=17,1,null)) AS '17',	"
						+ "		COUNT(IF(HOUR(W.workStart)=18,1,null)) AS '18',	"
						+ "		COUNT(IF(HOUR(W.workStart)=19,1,null)) AS '19', "
						+ "		COUNT(IF(HOUR(W.workStart)=20,1,null)) AS '20', "
						+ "		COUNT(IF(HOUR(W.workStart)=21,1,null)) AS '21', "
						+ "		COUNT(IF(HOUR(W.workStart)=22,1,null)) AS '22', "
						+ "		COUNT(IF(HOUR(W.workStart)=23,1,null)) AS '23', "
						+ "		COUNT(IF(HOUR(W.workStart)=24,1,null)) AS '24'"
						+ "	FROM workdata_tb W "
						+ "	INNER JOIN booking_tb B "
						+ "		ON W.BookingNo=B.BookingNo "
						+ "	WHERE ManagerNo=%s AND DAYOFYEAR(W.workStart)=DAYOFYEAR(\"%s\");"
						, ManagerNo, time);
				res = s.executeQuery(sql);
				res.next(); 
				for (int i = 1; i <= 24 ; i++) {
					resArray.add(res.getInt(i));					
				}
				
				break;
			case "WEEK":
				sql = String.format(
						"SELECT COUNT(IF(DAYOFWEEK(W.workStart)=1,1,null)) AS '1', "
						+ "		COUNT(IF(DAYOFWEEK(W.workStart)=2,1,null)) AS '2', "
						+ "		COUNT(IF(DAYOFWEEK(W.workStart)=3,1,null)) AS '3', "
						+ "		COUNT(IF(DAYOFWEEK(W.workStart)=4,1,null)) AS '4', "
						+ "		COUNT(IF(DAYOFWEEK(W.workStart)=5,1,null)) AS '5', "
						+ "		COUNT(IF(DAYOFWEEK(W.workStart)=6,1,null)) AS '6', "
						+ "		COUNT(IF(DAYOFWEEK(W.workStart)=7,1,null)) AS '7' "
						+ "  	FROM workdata_tb W 	"
						+ "		INNER JOIN booking_tb B "
						+ "			ON W.BookingNo=B.BookingNo "
						+ "			WHERE ManagerNo=%s "
						+ "				AND YEARWEEK(W.workStart)=YEARWEEK(\"%s\");"
						, ManagerNo, time);
				res = s.executeQuery(sql);
				res.next(); 
				for (int i = 1; i <= 7 ; i++) {
					resArray.add(res.getInt(i));					
				}
				break;				
			case "MONTH":
				sql = String.format(
						"SELECT COUNT(IF(MONTH(W.workStart)=1,1,null)) AS '1', "
						+ "		COUNT(IF(MONTH(W.workStart)=2,1,null)) AS '2', "
						+ "		COUNT(IF(MONTH(W.workStart)=3,1,null)) AS '3', "
						+ "		COUNT(IF(MONTH(W.workStart)=4,1,null)) AS '4', "
						+ "		COUNT(IF(MONTH(W.workStart)=5,1,null)) AS '5', "
						+ " 	COUNT(IF(MONTH(W.workStart)=6,1,null)) AS '6', "
						+ "		COUNT(IF(MONTH(W.workStart)=7,1,null)) AS '7', "
						+ "		COUNT(IF(MONTH(W.workStart)=8,1,null)) AS '8', "
						+ " 	COUNT(IF(MONTH(W.workStart)=9,1,null)) AS '9', "
						+ "		COUNT(IF(MONTH(W.workStart)=10,1,null)) AS '10', "
						+ "		COUNT(IF(MONTH(W.workStart)=11,1,null)) AS '11', "
						+ "		COUNT(IF(MONTH(W.workStart)=12,1,null)) AS '12' "
						+ " FROM workdata_tb W "
						+ "	INNER JOIN booking_tb B "
						+ "		ON W.BookingNo=B.BookingNo "
						+ "	WHERE B.ManagerNo=%s AND YEAR(W.workStart)=YEAR(\"%s\");"
						, ManagerNo, time);
				res = s.executeQuery(sql);
				res.next(); 
				for (int i = 1; i <= 12 ; i++) {
					resArray.add(res.getInt(i));					
				}
				break;
			}		
			return resArray;
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
			return null;
		}
	}
	//GET
	//polling
	public JSONArray pollingData(String SerialNum) {
		try {
			JSONArray resArray = new JSONArray();
			Statement s = con.createStatement();
			String sql = String.format(
					"SELECT WasherNum, EndDevAddr, WorkStatus, BatteryStatus, LockStatus, BookStatus "
							+ "FROM enddev_tb  "
							+ "WHERE HubAddr=(SELECT HubAddr FROM coordi_tb WHERE SerialNum='%s');", SerialNum);			
			ResultSet res = s.executeQuery(sql);	
			con.commit();
			s.close();
			while (res.next()) {				
				JSONObject endDevData = new JSONObject();	
				endDevData.put("WasherNum", res.getInt("WasherNum"));
				endDevData.put("EndDevAddr", res.getString("EndDevAddr"));
				endDevData.put("WorkStatus", res.getString("WorkStatus"));
				endDevData.put("BatteryStatus", res.getString("BatteryStatus"));
				endDevData.put("LockStatus", res.getString("LockStatus"));
				endDevData.put("BookStatus", res.getString("BookStatus"));
				resArray.add(endDevData);
			}
			return resArray;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	//세탁기 점검 상태
	public JSONArray devStatus(String SerialNum) {
		try {
			Statement s = con.createStatement();
			String sql = String.format("SELECT WasherNum, EndDevAddr, LastClean, UsedNum "
					+ "FROM enddev_tb  "
					+ "WHERE HubAddr=(SELECT HubAddr FROM coordi_tb WHERE SerialNum='%s');", SerialNum);

			ResultSet res = s.executeQuery(sql);
			con.commit();
			s.close();
			JSONArray resArray = new JSONArray();			
			while (res.next()) {				
				JSONObject endDevData = new JSONObject();	
				endDevData.put("WasherNum", res.getInt("WasherNum"));
				endDevData.put("EndDevAddr", res.getString("EndDevAddr"));
				endDevData.put("LastClean", res.getString("LastClean"));
				endDevData.put("UsedNum", res.getString("UsedNum"));
				resArray.add(endDevData);
			}
			return resArray;
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
			return null;
		}
	}
	//대리 수령자
	public JSONArray deputyList(String ManagerNo) {
		try {
			JSONArray resArray = new JSONArray();
			Statement s = con.createStatement();

			String sql = String.format(
					"SELECT B.EndDevAddr, W.WorkStart, W.WorkEnd, M.Name, B.Delegate, B.BookingNo, B.EndDevAddr "
							+ "	FROM booking_tb B "
							+ "	LEFT JOIN workdata_tb W "
							+ "		ON B.BookingNo = W.BookingNo "
							+ "	LEFT JOIN muser_tb M "
							+ "		ON B.userNo = M.userNo "
							+ "	WHERE B.ManagerNo=%s AND B.Delegate IN (1,2);", ManagerNo);
			System.out.println(sql);
			ResultSet res = s.executeQuery(sql);
			con.commit();
			s.close();
			while (res.next()) {
				JSONObject obj = new JSONObject();
				obj.put("BookingNo", res.getInt("BookingNo"));
				obj.put("EndDevAddr", res.getString("EndDevAddr"));
				obj.put("WorkStart", res.getString("WorkStart"));
				obj.put("WorkEnd", res.getString("WorkEnd"));
				obj.put("Name", res.getString("Name"));	
				obj.put("Delegate", res.getInt("Delegate"));
				resArray.add(obj);
			}
			return resArray;
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
			return null;
		}
	}
	//예약 리스트
	public JSONArray bookList(String ManagerNo) {
		try {
			JSONArray resArray = new JSONArray();
			Statement s = con.createStatement();
			String sql = String.format(
					"SELECT B.BookingNo, B.BookingTime, B.EndDevAddr, M.Name, B.Delegate, B.BookingType "
							+ "	FROM booking_tb B 	"
							+ "	INNER JOIN muser_tb M 	"
							+ "		ON B.userNo = M.userNo "
							+ "	WHERE B.ManagerNo=%s AND B.Delegate!=3;", ManagerNo);
			ResultSet res = s.executeQuery(sql);
			con.commit();
			s.close();
			while (res.next()) {
				JSONObject obj = new JSONObject();
				obj.put("BookingNo", res.getInt("BookingNo"));
				obj.put("Name", res.getString("Name"));
				obj.put("EndDevAddr", res.getString("EndDevAddr"));
				obj.put("Delegate", res.getInt("Delegate"));
				obj.put("BookingType", res.getInt("BookingType"));
				obj.put("BookingTime", res.getTimestamp("BookingTime").toString());
				resArray.add(obj);
			}
			return resArray;
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
			return null;
		}
	}
}
