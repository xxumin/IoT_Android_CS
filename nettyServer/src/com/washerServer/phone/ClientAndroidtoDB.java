package com.washerServer.phone;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.plaf.synth.SynthSeparatorUI;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.washerServer.DBCon.DBConnection;
import com.washerServer.main.HttpWasherServerHandler;

public class ClientAndroidtoDB {
	DBConnection dbcon = HttpWasherServerHandler.dbcon;
	Connection con = dbcon.getConnection();
		
	
	// Login
	public String login(String id, String pwd) {		
		try {
			Statement s = con.createStatement();
			String sql = String.format("SELECT Pwd, Name, UserAddr, UserGPS, Bookmark1, Bookmark2, Bookmark3, UserNo "
					+ "FROM muser_tb WHERE UserId='%s';", id);
			ResultSet res = s.executeQuery(sql);
			con.commit();			
			while(res.next()) {
				String passwd = res.getString("Pwd");
				if (passwd.equals(pwd)) {				
					String Name = res.getString("Name");
					String MyLocation = res.getString("UserAddr");
					String Mybook1 = res.getString("Bookmark1");
					String Mybook2 = res.getString("Bookmark2");
					String Mybook3 = res.getString("Bookmark3");
					String UserGPS = res.getString("UserGPS");	
					String UserNo = res.getString("UserNo");
					
					return Name + "/" + MyLocation + "/" + Mybook1 + "/" + Mybook2 + "/" + Mybook3 + "/" + UserGPS + "/" + UserNo;
				}			
			}
			s.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return null;	
	}
	
	public void tokenUpdate(String token, String id) {
		try {
			Statement s = con.createStatement();
			String sql = String.format("UPDATE muser_tb SET Token = '%s' "
					+ "WHERE UserId = '%s';", token, id);
			System.out.println("Token Update  >" + sql);
			s.executeUpdate(sql);
			con.commit();
			s.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	
	// 회원가입 / 회원가입시 자신의 기본 주소 추가
	public boolean JoinUser(String id, String pwd, String name, String addr, String gps) {
		try {
			Statement s =  con.createStatement();
			String sql = String.format("INSERT INTO muser_tb(UserId, Pwd, Name, UserAddr, UserGPS) "
					+ "VALUES('%s', '%s', '%s', '%s', '%s');"
					,id, pwd, name, addr, gps);
			System.out.println(sql);
			s.executeQuery(sql);
			con.commit();
			s.close();			
			return true;
		} catch (SQLException e) {
			e.printStackTrace();			
		}	
		return false;
	}
	
	public boolean IdCheckReq(String id) {
		try {
			Statement s =  con.createStatement();
			String sql = String.format("SELECT COUNT(*) FROM muser_tb "
					+ "WHERE UserId = '%s'", id);
			int idnum = -1;
			ResultSet res = s.executeQuery(sql);
			
			while(res.next()) {
				idnum = res.getInt("COUNT(*)");
			}
			
			if(idnum >= 1) { // id 존재
				return false;
			} else if (idnum == 0) {
				return true;
			}
			
			con.commit();
			s.close();		
			
		} catch (SQLException e) {
			e.printStackTrace();			
		}	
		return false;
	}
	
	// BookMark Cleaner's List Request
	public String BookmarkShopReq(int userno) {
		String name[]= new String[3];
		String open[] = new String[3];
		String close[] = new String[3];
		String mngnum[] = new String[3];
				
		try {				
			Statement s= con.createStatement();	
			String sql;
			ResultSet res;
			for(int i=0; i <3; i++) {
				sql = String.format("SELECT ShopName, OpenTime, CloseTime, ManagerNo"
						+ " FROM manager_tb, muser_tb"
						+ " WHERE ManagerNo = (SELECT Bookmark" + (i+1) 
						+ " FROM muser_tb WHERE UserNo=%d);"
						, userno);
				res = s.executeQuery(sql);	
					
				while(res.next()) {
					name[i] = res.getString("ShopName");
					open[i]= res.getString("OpenTime");
					close[i] = res.getString("CloseTime");
					mngnum[i] = res.getString("ManagerNo");					
				}
				
				System.err.println(">>>>>>>" +  mngnum[i]);
				con.commit();	
			}
			s.close();				
		} catch (SQLException ex) {
			ex.printStackTrace();			 
		}	
		return name[0] + "/" + open[0] + "/" + close[0] + "/" + name[1] + "/" + open[1] + "/" + close[1] + "/" + name[2] + "/" + open[2] + "/" + close[2] 
				+ "/" + mngnum[0] + "/" + mngnum[1] + "/" + mngnum[2];
	}
	
	// Select Hub Address SQL
	public JSONArray ShopHubReq(String id, String checkcode, String params) { 
		String Snum = "";
		try {
			Statement s = con.createStatement();
			
			if(checkcode.equals("1")) {
				// 내정보 Activity에서 Bookmark 버튼을 눌러서 ShopInfo를 얻고자 할때
				String sql = String.format("SELECT SerialNum "
						+ "FROM manager_tb WHERE ManagerNo = '%s';" , params);					
				ResultSet res = s.executeQuery(sql);		
				
				while(res.next()) {
					Snum = res.getString("SerialNum");
				}
			} else if(checkcode.equals("2")) { // 맵에서 ShopInfo 얻고자 할때
				String sql = String.format("SELECT SerialNum, ShopName, ShopAddr, OpenTime, CloseTime, ManagerNo "
						+ "FROM manager_tb WHERE ShopGPS = '%s';", params);
				ResultSet res = s.executeQuery(sql);
				
				while(res.next()) {
					Snum = res.getString("SerialNum");
				}
			}			
			
			con.commit();
			s.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		JSONArray req = ShopInfoReq(Snum);
		System.out.println("ShopInfoReq : " + req);
		
		return req; 
	}
	
	// 세탁소 정보 불러오기
	public JSONArray ShopInfoReq(String license) {		
		int i = 0;
		try {
			JSONArray resArray = new JSONArray();
			Statement s = con.createStatement();		
			// 1 > Shop's Basic Information
			String sql = String.format("SELECT ShopName, ShopAddr, OpenTime, CloseTime, ManagerNo "
					+ "FROM manager_tb WHERE SerialNum = '%s';", license);
			
			ResultSet res = s.executeQuery(sql);
			
			while(res.next()) {
				JSONObject shopbasic = new JSONObject();
				shopbasic.put("ShopName", res.getString("ShopName"));
				shopbasic.put("ShopAddr", res.getString("ShopAddr"));
				shopbasic.put("OpenTime", res.getString("OpenTime"));
				shopbasic.put("CloseTime", res.getString("CloseTime"));
				shopbasic.put("ManagerNo", res.getString("ManagerNo"));
				resArray.add(i, shopbasic);
				i++;
				System.out.println("세탁소 기본 정보1 >>      " + shopbasic);
			}			
			
			// 2 > Shop's Washer Information
			s = con.createStatement();
			sql = String.format("SELECT WasherNum, WorkStatus FROM enddev_tb WHERE HubAddr = "
					+ "(SELECT HubAddr FROM coordi_tb WHERE SerialNum = '%s');", license);
			
			res = s.executeQuery(sql);
			
			while(res.next()) {		
				JSONObject enddev = new JSONObject();	
				enddev.put("WasherNum", Integer.parseInt(res.getString("WasherNum")));
				enddev.put("WorkStatus", Boolean.parseBoolean( res.getString("WorkStatus")));
				resArray.add(i, enddev);		
				i++;
				System.out.println("세탁소 세탁기 상태 정보 2 >>      " + enddev);
			}	
			s.close();
			
			con.commit();
			
			return resArray;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	// Checking BookMark register available 
	public String CheckBookmarkReq(String id) {
		try {
			Statement s = con.createStatement();			
			
			String sql = String.format("SELECT Bookmark1, Bookmark2, Bookmark3 from muser_tb "
					+ "WHERE UserId = '%s';", id);
			ResultSet res = s.executeQuery(sql);	
			
			while(res.next()) {				
				String b1 = res.getString("Bookmark1");
				String b2 = res.getString("Bookmark2");
				String b3 = res.getString("Bookmark3");
				
				System.out.println(b1 + "/" + b2 + "/" + b3);
				return b1 + "/" + b2 + "/" + b3;
			}
			con.commit();
			s.close();			
		} catch(SQLException e) {
			
		}
		return null;
	}
	
	public String UpdateBookmarkReq(String mark, String params, String id) {
		try {
			Statement s = con.createStatement();
			if(params.contains(",")) {
				String sql = String.format("UPDATE muser_tb SET %s = "
						+ "(SELECT ManagerNo FROM manager_tb WHERE ShopGPS = '%s') WHERE UserId = '%s';", mark, params, id);
				System.out.println("UpBook:"+sql);
				s.executeUpdate(sql);				
			} else {
				String sql = String.format("UPDATE muser_tb SET %s = %d "
						+ "WHERE UserId = '%s';", mark, Integer.parseInt(params), id);
				s.executeUpdate(sql);				
			}
			con.commit();
			s.close();
			return "Success";
		} catch(SQLException e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	
	// UnBookMark
	public String UnBookmarkReq(String mark, String id) {
		try {
			Statement s = con.createStatement();
			
			String sql = String.format("UPDATE muser_tb SET %s = NULL "
						+ "WHERE UserId = '%s';", mark, id);
			System.out.println("UpBook2:"+sql);
			s.executeUpdate(sql);
			con.commit();
			s.close();
			return "Removed";
		} catch(SQLException e) {
			e.printStackTrace();
		}		
		return null;
	}

	public int ReserveUser(String id) {
		try {
			Statement s= con.createStatement();
			String sql = String.format("SELECT UserNo FROM muser_tb "
					+ "WHERE UserId = '%s';", id);
			ResultSet res = s.executeQuery(sql);
			
			while(res.next()) {
				int userno = Integer.parseInt(res.getString("UserNo"));
				return userno;
			}
			con.commit();			
			s.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	// 세탁기 예약하기 
	public String ReserveReq(int managerno, int userno) {		
		try {
			Statement s= con.createStatement();
			String sql = String.format("INSERT INTO booking_tb (ManagerNo, UserNo, BookingType, BookingTime) "
					+ "VALUES (%d, %d, 0, sysdate());", managerno, userno);
			s.executeUpdate(sql);
			con.commit();
			s.close();
			return "SUCCESS";
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String myLaundry(String userno) {
		try {
			Statement s = con.createStatement();
			String sql = String.format("SELECT BookingNo, ManagerNo, EnddevAddr, BookingType, Delegate FROM booking_tb "
					+ "WHERE UserNo = '%s' ORDER BY BookingTime DESC LIMIT 1", userno);			
			ResultSet res = s.executeQuery(sql); 
			
			String BookingNo = null;
			String ManagerNo = null;
			String EnddevAddr = null;
			String BookingType = null;
			String Delegate = null;
			
			while(res.next()) {
				BookingNo = res.getString("BookingNo");
				ManagerNo = res.getString("ManagerNo");
				EnddevAddr = res.getString("EnddevAddr");
				BookingType = res.getString("BookingType");
				Delegate = res.getString("Delegate");
			}
			
			s.close();
			
			// 세탁소 이름 앙아오기
			Statement s2 = con.createStatement();
			String sql2 = String.format("SELECT ShopName FROM manager_tb "
					+ "WHERE ManagerNo = '%s'", ManagerNo);
			ResultSet res2 = s2.executeQuery(sql2);
			
			String ShopName = null;
			while(res2.next()) {
				ShopName = res2.getString("ShopName");
			}
			
			s2.close();
			
			Statement s3 = con.createStatement();
			String sql3 = String.format("SELECT WasherNum FROM enddev_tb "
					+ "WHERE EnddevAddr = '%s'", EnddevAddr);
			ResultSet res3 = s3.executeQuery(sql3);
			
			String WasherNum = null;
			while(res3.next()) {
				WasherNum = res3.getString("WasherNum");
			}
			
			s3.close();
			
			return BookingType + "/" + ShopName + "/" + WasherNum + "/" + BookingNo + "/" + Delegate;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String endReq(String bookingno) {
		try {
			Statement s = con.createStatement();
			
			String sql = String.format("SELECT Delegate FROM booking_tb "
					+ "WHERE BookingNo = '%s'", bookingno);
			ResultSet res = s.executeQuery(sql);
			String chk = null;
			
			while(res.next()) {
				chk = res.getString("Delegate");
			}
			
			if(chk.equals("0")) {
				String sql2 = String.format("UPDATE booking_tb b, enddev_tb e SET b.Delegate = 3, e.lockStatus=false "
						+ "WHERE b.BookingNo = '%s' and e.EndDevAddr=b.EndDevAddr; ", bookingno);
				s.executeUpdate(sql2);
				System.out.println(sql2);
				con.commit();
				s.close();
			} else if(chk.equals("3")) {
				return "End";
			} else {
				return "Already";
			}
			s.close(); 
			return "SUCCESS";			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// 세탁물 대리 수거 요청하기
	public String DelReq(String bookingno) {
		try {
			Statement s = con.createStatement();
			String sql = String.format("UPDATE booking_tb SET Delegate = 1 "
					+ "WHERE BookingNo = '%s'", bookingno);
			s.executeUpdate(sql);
			con.commit();
			s.close();
			return "SUCCESS";
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}	
	

	
	public String[] getShopInfomation()
	{
	   try {
	   // row 갯수
		   Statement s = con.createStatement();
		   String sql = String.format("SELECT count(*) as total " +
	                  "FROM manager_tb;");
		   ResultSet res = s.executeQuery(sql);
		   int count = 0;
		   while(res.next()) {
			  count = res.getInt("total");	   
		   }
		   con.commit();
		   s.close();
		   
		   String datas[] = new String[count];

		   // 데이터 읽ㄱ
		   Statement s2 = con.createStatement();
		   String sql2 = String.format("SELECT ShopName, ShopAddr, ShopGPS, OpenTime, CloseTime " +
	                  "FROM manager_tb;");
		   ResultSet res2 = s2.executeQuery(sql2);
		   int i=0;
		   while(res2.next())
		   {
			   String name = res2.getString("ShopName");
			   String address = res2.getString("ShopAddr");
			   String gps = res2.getString("ShopGPS");
			   String opentime = res2.getString("OpenTime");
			   String closetime = res2.getString("CloseTime");

			   String data = name + "/" + address + "/" + gps + "/" + opentime + "/" + closetime;
			   System.err.println(data);
			   datas[i]=data;
			   i++;
	      }
		   con.commit();
		   s2.close();
	      return datas;
	   } catch(SQLException e) { e.printStackTrace(); }

	   return null;
	}
}
