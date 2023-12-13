package com.washerServer.phone;

import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.synth.SynthSeparatorUI;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ClientAndroid {
	ClientAndroidtoDB AntoDB = new ClientAndroidtoDB();
	JSONObject resJson = null;
	JSONParser parser = new JSONParser();
	JSONArray resJsonArr;
	String response = null;
	String[] datas;
	
	public byte[] recieveJSONFromAn(String msgJson, String checkcode) {
		switch(checkcode) {
		case "login": 
			response = AnloginHttpReq(msgJson);		
			break;
		case "join":
			response = AnjoinHttpReq(msgJson);
			break;
		case "idcheck": // id 중복 체크
			response = idcheckHttpReq(msgJson);		
			break;
		case "token":
			response = TokenHttpReq(msgJson);
			break;
		case "myBookmark": // 내 증겨찾기(북마크)
			response = BookmarkShopHttpReq(msgJson); // 북마크 리스트 불러오기
			break;
		case "shopInfo": // 세탁소 정보보기
			 response = ShopHubHttpReq(msgJson);
			break;
		case "registerBook": // 북마크 등록하기 
			response = RegistermarkHttpReq(msgJson);
			break;	
		//case "unBook": 
			//response = UnmarkHttpReq(msgJson);
			//break;
		case "reserve": // 세탁 예약신청
			response = ReserveHttpReq(msgJson);
			break;
		case "myLaundry": // 세탁 상태 확인
			response = myLaundryHttpReq(msgJson);
			break;
		case "end": // 빨래 수거 완료
			response = endHttpReq(msgJson);
			break;
		case "del": // 대리수령
			response = delHttpReq(msgJson);
			break;
		default:
			break;
		}		
		System.out.println(response);
		return response.getBytes();
	}	
	
	// 로그인
	// Login Request
	public String AnloginHttpReq(String msg) {		
		try {
			resJson = new JSONObject();
			JSONObject jsonObj = (JSONObject) parser.parse(msg);
			
			// input
			String id = jsonObj.get("UserId").toString();
			String pwd = jsonObj.get("Pwd").toString();
			
			String condb = AntoDB.login(id, pwd);
			
			if(condb != null) {
				// output
				String temp[] = condb.split("/");			
				
				resJson.put("Pass", "ok");
				resJson.put("Name", temp[0]); // name
				resJson.put("MyLocation", temp[1]); // location
				resJson.put("MyBook1", temp[2]); // BookMark
				resJson.put("MyBook2", temp[3]); // BookMark
				resJson.put("MyBook3", temp[4]); // BookMark
				resJson.put("UserGPS", temp[5]); // UserGPS
				resJson.put("UserNo", temp[6]);
			} else {
				resJson.put("Pass", "fail");
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return resJson.toString();
	}
	
	public String TokenHttpReq(String msg) {
		try {
			resJson = new JSONObject();
			JSONObject jsonObj = (JSONObject) parser.parse(msg);
			
			// input
			String id = jsonObj.get("UserId").toString();						
			String token = jsonObj.get("Token").toString();
			
			AntoDB.tokenUpdate(token, id);
			return "SUCCESS";
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	// 회원가입
	public String AnjoinHttpReq(String msg) { // Join Member
		try {
			resJson = new JSONObject();
			JSONObject jsonObj = (JSONObject) parser.parse(msg);
			
			String id = jsonObj.get("UserId").toString();
			String pwd = jsonObj.get("Pwd").toString();
			String name = jsonObj.get("Name").toString();
			String addr= jsonObj.get("UserAddr").toString();
			String gps = jsonObj.get("lat").toString() + "," +jsonObj.get("lng").toString();
		    System.out.println(gps);
			
			boolean condb = AntoDB.JoinUser(id, pwd, name, addr, gps);
			
			if(condb) {				
				resJson.put("Pass", "ok");				
			} else {
				resJson.put("Pass", "false");
			}
		} catch(ParseException e) {
			e.printStackTrace();
		}
		return resJson.toString();
	}
	
	public String idcheckHttpReq(String msg) {
		try {
			resJson = new JSONObject();
			JSONObject jsonObj = (JSONObject) parser.parse(msg);
			
			String id = jsonObj.get("UserId").toString();		
		    
			
			boolean condb = AntoDB.IdCheckReq(id);
			
			if(condb) { // if condb is true
				resJson.put("Pass", "ok"); // 아이디 가입 가능
			} else {
				resJson.put("Pass", "false"); // 아이디 가입 불가
			}
		} catch(ParseException e) {
			e.printStackTrace();
		}
		return resJson.toString();
	}
	
	// 즐겨 찾기 세탁소 정보 불러오기
	public String BookmarkShopHttpReq(String msg) { // Request for BookMark Information
		JSONArray resArray = null;
		try {
			resArray = new JSONArray();
			int j = 0;
			resJson = new JSONObject();
			JSONObject jsonObj = (JSONObject) parser.parse(msg);
			
			// input
			//int bookmark1 = Integer.parseInt(jsonObj.get("Bookmark1").toString());
			//int bookmark2 = Integer.parseInt(jsonObj.get("Bookmark2").toString());
			//int bookmark3 = Integer.parseInt(jsonObj.get("Bookmark3").toString());
			int userno = Integer.parseInt(jsonObj.get("UserNo").toString());
			
			// 즐겨찾기 찾기
			String condb = AntoDB.BookmarkShopReq(userno); 
			System.out.println(condb);			
			
			if(condb != null) {
				// output
				String temp[] = condb.split("/");
				resJson.put("B1_name", temp[0]);
				resJson.put("B1_open", temp[1]);
				resJson.put("B1_close", temp[2]);
				resJson.put("B2_name", temp[3]);
				resJson.put("B2_open", temp[4]);
				resJson.put("B2_close", temp[5]);
				resJson.put("B3_name", temp[6]);
				resJson.put("B3_open", temp[7]);
				resJson.put("B3_close", temp[8]);
				resJson.put("B1_Mng", temp[9]);
				resJson.put("B2_Mng", temp[10]);
				resJson.put("B3_Mng", temp[11]);
				
				resArray.add(j, resJson); // resArray[0, 북마크 정보]
				j++;
			} else {
				resJson.put("Pass", "fail");
			}
			// 지도 세탁소 찾기
			 datas = AntoDB.getShopInfomation();
			 String str[] = datas;
             int countCols = str.length;	
			
			 if(str != null) {
				 for(int i=0; i<countCols; i++) {
					 String data[] = str[i].split("/");
					 JSONObject resJson2 = new JSONObject();
					 resJson2.put("Pass", "ok");
					 resJson2.put("name", data[0]);
					 resJson2.put("address", data[1]);
					 resJson2.put("gps", data[2]);			 
	                 resJson2.put("opentime", data[3]);
	                 resJson2.put("closetime", data[4]);
	                
	                 resArray.add(j, resJson2);
	                 j++;
	                }
	            }
	            else {
	                resJson.put("Pass", "fail");	              
	            }
		} catch(ParseException e) {
			e.printStackTrace();
		}		
		return resArray.toString();
	}
	
	// Request for Hub Address -> 세탁소 정보 찾기
	public String ShopHubHttpReq(String msg) { 
		try {
			resJson = new JSONObject();
			JSONObject jsonObj = (JSONObject) parser.parse(msg);
			
			String checkcode = jsonObj.get("CheckCode").toString();		
			String params = jsonObj.get("Params").toString();		
			String id = jsonObj.get("UserId").toString();		
			
			// input
			JSONArray resArray = new JSONArray();			
			if(checkcode.equals("1")) {
				resArray = AntoDB.ShopHubReq(id, checkcode, params);
			} else if(checkcode.equals("2")) {
				resArray = AntoDB.ShopHubReq(id, checkcode, params);
			}					
			
			if(resArray != null) {		
				resJson.put("Pass", "ok");				
				resJson.put("ShopInfo", resArray);
			} else {
				resJson.put("Pass", "fail");
			}
		} catch(ParseException e) {
			e.printStackTrace();
		}	
				
		return resJson.toString();
	}
	

	
	public String RegistermarkHttpReq(String msg) {
		try {
			resJson = new JSONObject();
			JSONObject jsonObj = (JSONObject) parser.parse(msg);			
			
			String id = jsonObj.get("UserId").toString();		
			String params = jsonObj.get("Params").toString();
			//String check = jsonObj.get("Check").toString();
			String managerno = jsonObj.get("ManagerNo").toString();
			//System.out.println("불린 스트링 확인" + check);			
			// input			
			
			String condb = AntoDB.CheckBookmarkReq(id);
			String temp[] = condb.split("/");
			String status = null;
			if(condb != null) {
				if(managerno.equals(temp[0])) {
					status = AntoDB.UnBookmarkReq("Bookmark1", id); 
					resJson.put("Status", status + "1");
				} else if (managerno.equals(temp[1])) {
					status = AntoDB.UnBookmarkReq("Bookmark2", id);
					resJson.put("Status", status + "2");
				} else if (managerno.equals(temp[2])) {
					status = AntoDB.UnBookmarkReq("Bookmark3", id);
					resJson.put("Status", status + "3");
				} else { // 중복이 없을 때
					if(temp[0].equals("null")) { // BookMark is null > register BookMark available
						String update = AntoDB.UpdateBookmarkReq("Bookmark1", params, id);
						resJson.put("Status", update);
					} else { 
						// BookMark is not null > register BookMark not available
						if(temp[1].equals("null")) {
							String update = AntoDB.UpdateBookmarkReq("Bookmark2", params, id);
							resJson.put("Status", update);
						} else {							
							if(temp[2].equals("null")) {
								String update = AntoDB.UpdateBookmarkReq("Bookmark3", params, id);
								resJson.put("Status", update);
							} else { 								
								resJson.put("Status", "full"); // BookMark List is FULL
							}
						}
					}	
				}
				
			} else {
				resJson.put("Status", "empty");
			}
		} catch(ParseException e) {
			e.printStackTrace();
		}	
				
		return resJson.toString();
	}
	
	public String UnmarkHttpReq(String msg) {
		try{
			resJson = new JSONObject();
			JSONObject jsonObj = (JSONObject) parser.parse(msg);
			
			String id = jsonObj.get("UserId").toString();
			String managerno = jsonObj.get("ManagerNo").toString();
			
			String condb = AntoDB.CheckBookmarkReq(id);
			
			if(condb != null) {
				String temp[] = condb.split("/");
				String status = null;
				
				// temp[0] = Bookmark1, temp[1] = Bookmark2, temp[2] = Bookmark3 
				if(temp[0].equals(managerno)) {
					status = AntoDB.UnBookmarkReq("Bookmark1", id);
				} else if (temp[1].equals(managerno)) {
					status = AntoDB.UnBookmarkReq("Bookmark2", id);
				} else if (temp[2].equals(managerno)) {
					status = AntoDB.UnBookmarkReq("Bookmark3", id);
				}				
				
				resJson.put("Pass", status);					
				
			}
		} catch (ParseException e) { e.printStackTrace(); }
			
		return resJson.toString();
	}	
	
	
	public String ReserveHttpReq(String msg) {
		try {
			resJson = new JSONObject();
			JSONObject jsonObj = (JSONObject) parser.parse(msg);
			
			int managerno =Integer.parseInt(jsonObj.get("ManagerNo").toString());
			String id = jsonObj.get("UserId").toString();					
			
			int condb = AntoDB.ReserveUser(id);			

			if(condb != 0) {				
				resJson.put("Status", AntoDB.ReserveReq(managerno, condb));
			} else {
				resJson.put("Pass", "fail");
			}
		} catch(ParseException e) {
			
		}
		return "SUCCESS";
	}
	
	public String myLaundryHttpReq(String msg) {
		try {
			resJson = new JSONObject();
			JSONObject jsonObj = (JSONObject) parser.parse(msg);			
			
			String userno = jsonObj.get("UserNo").toString();					
			
			String condb = AntoDB.myLaundry(userno);	

			if(condb != null) {				
				String temp[] = condb.split("/");
				resJson.put("BookingType", temp[0]);
				resJson.put("ShopName", temp[1]);
				resJson.put("WasherNum", temp[2]);
				resJson.put("BookingNo", temp[3]);
				resJson.put("Delegate", temp[4]);
				System.out.println(temp[0] + "/" + temp[1] + "/" + temp[2] + "/" + temp[3] + "/" + temp[4]);
			} else {
				resJson.put("Pass", "fail");
			}
		} catch(ParseException e) {
			e.printStackTrace();
		}
		return resJson.toString();
	}
	
	// 세탁기 사용 끝남 -> 세탁 수거 완료
	public String endHttpReq(String msg) {
		try {
			resJson = new JSONObject();
			JSONObject jsonObj = (JSONObject) parser.parse(msg);			
			
			String bookingno = jsonObj.get("BookingNo").toString();			
			
			String condb = AntoDB.endReq(bookingno);
			resJson.put("Status", condb);	
			
		} catch(ParseException e) {
			e.printStackTrace();
		}
		return resJson.toString();
	}
	
	// 대리수령 신청
	public String delHttpReq(String msg) {
		try {
			resJson = new JSONObject();
			JSONObject jsonObj = (JSONObject) parser.parse(msg);			
			
			String bookingno = jsonObj.get("BookingNo").toString();			
			
			String condb = AntoDB.DelReq(bookingno);
			resJson.put("Status", condb);	
			
		} catch(ParseException e) {
			e.printStackTrace();
		}
		return resJson.toString();
	}
	
	/*
	// 등록된 세탁소 불러오기
    public String reqShopInformation()
    {
        try
        {
        	resJsonArr = new JSONArray();
        	// JSONObject jsonObj = (JSONObject) parser.parse(msg);
        	
        	datas = AntoDB.getShopInfomation();
            String str[] = datas;
            int countCols = str.length;

            if(str != null)
            {
                for(int i=0; i<countCols; i++)
                {
                    String data[] = str[i].split("/");
                    resJson = new JSONObject();
                    resJson.put("Pass", "ok");
                    resJson.put("name", data[0]);
                    resJson.put("address", data[1]);
                    resJson.put("gps", data[2]);
                    resJson.put("opentime", data[3]);
                    resJson.put("closetime", data[4]);

                    resJsonArr.add(resJson);
                }
            }
            else
            {
                resJson.put("Pass", "fail");
                resJsonArr.add(resJson);
            }
        }catch (Exception e) { e.printStackTrace(); }

        return resJsonArr.toString();

    }*/
	
	
}
