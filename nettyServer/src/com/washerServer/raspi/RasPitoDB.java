package com.washerServer.raspi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.washerServer.DBCon.DBConnection;
import com.washerServer.main.HttpWasherServerHandler;

public class RasPitoDB {
	DBConnection dbcon = HttpWasherServerHandler.dbcon;
	Connection con = dbcon.getConnection();
	ResultSet rs;
	Statement stmt;

	private final static String AUTH_KEY_FCM = "AAAAF6boTCA:APA91bGf_Sl0p25rBwFYEj95ImqFx7FfdhZtg_czuExxIvH8QEVOWn3p8BB34gyJedM5SuEERpUss9mtPn8Xjm0aH889pzE24hXbFxBMrAumcfkPMaZgeVm9D6fILFjFmkad_0a9ye2A";
	private final static String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";

	private void usedJCM(JSONObject result) {
		try {
			URL url = new URL(API_URL_FCM);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", "key=" + AUTH_KEY_FCM);
			conn.setRequestProperty("Content-Type", "application/json");

			try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8")) {
				wr.write(result.toString());
				wr.flush();
			} catch (Exception e) {

			}

			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String output;

			System.out.println("Output from Server .... \n");

			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			conn.disconnect();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private void sendWorkEndMsg(int BookingNo) {
		try {
			rs = stmt.executeQuery(String.format("SELECT b.BookingNo, u.Token, e.EndDevAddr, e.WasherNum, e.BookStatus "
					+ "FROM enddev_tb e, muser_tb u, booking_tb b "
					+ "WHERE b.BookingNo=%d and e.EndDevAddr=b.EndDevAddr and u.UserNo=b.UserNo;", BookingNo));

			while (rs.next()) {
				String token = rs.getString("Token");
				String WasherNum = rs.getString("WasherNum");
				boolean BookStatus = rs.getBoolean("BookStatus");
				String EndDevAddr = rs.getString("EndDevAddr");

				JSONObject json = new JSONObject();
				JSONObject info = new JSONObject();
				// JSONObject data = new JSONObject();

				info.put("body", WasherNum + "번 세탁기가 종료되었습니다."); // Notification
				// data.put("EndDevAddr", EndDevAddr);
				json.put("notification", info);
				// json.put("data", data);
				// json.put("data", msgObj);
				json.put("to", token); // deviceID
				if (BookStatus) {
					sendEndMsgForNewUser(EndDevAddr);
				}
				usedJCM(json);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private void sendEndMsgForNewUser(String EndDevAddr) {
		try {
			rs = stmt.executeQuery(String.format(
					"SELECT b.BookingNo, u.Token, e.WasherNum FROM enddev_tb e, muser_tb u, booking_tb b "
							+ "WHERE e.EndDevAddr=%d and b.EndDevAddr=e.EndDevAddr and u.UserNo=b.UserNo;",
					EndDevAddr));
			String token = rs.getString("Token");
			String WasherNum = rs.getString("WasherNum");

			JSONObject json = new JSONObject();
			JSONObject info = new JSONObject();

			info.put("body", WasherNum + "번 세탁기가 사용가능합니다.");
			json.put("notification", info);
			json.put("to", token);

			usedJCM(json);

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public RasPitoDB() {
		try {
			stmt = con.createStatement();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public void raspiCoordi(String HubAddr, int EndDev_Num) {
		try {
			rs = stmt.executeQuery(String.format("SELECT count(*) FROM coordi_tb WHERE HubAddr = '%s' ;", HubAddr));
			if (rs.next()) {
				if (rs.getInt(1) > 0) {
					stmt.executeUpdate(String.format("UPDATE coordi_tb SET EndDev_Num = %d WHERE HubAddr = '%s';",
							EndDev_Num, HubAddr));
				} else {
					stmt.executeUpdate(String.format(
							"INSERT INTO coordi_tb ( HubAddr, EndDev_Num ) VALUES ( '%s', %d );", HubAddr, EndDev_Num));
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			dbcon.myRollback();

		}
	}

	public String raspiEndDev(String EndDevAddr, String HubAddr, Boolean LockStatus, int BatteryStatus) {
		try {
			rs = stmt.executeQuery(
					String.format("SELECT count(*) FROM enddev_tb WHERE EndDevAddr = '%s' ;", EndDevAddr));
			if (rs.next()) {
				if (rs.getInt(1) > 0) {
					rs = stmt.executeQuery(String.format(
							"SELECT HubAddr, IF(LockStatus,'true','false') as LockStatus, IF(BookStatus,'true','false') as BookStatus FROM enddev_tb WHERE EndDevAddr = '%s' ;",
							EndDevAddr));
					if (rs.next()) {
						String tempHub = rs.getString("HubAddr");
						boolean tempLock = rs.getBoolean("LockStatus");
						boolean tempBook = rs.getBoolean("BookStatus");

						// for work check
						if (!RasPi.endDevOp.containsKey(EndDevAddr)) {
							EndDevData temp = new EndDevData();
							temp.EndDevAddr = EndDevAddr;
							temp.LockStatus = tempLock;
							temp.BookStatus = tempBook;
							temp.changeTime = new Date(System.currentTimeMillis());
							temp.sendMsgCode = false;
							RasPi.endDevOp.put(EndDevAddr, temp);
						} else {
							EndDevData temp = RasPi.endDevOp.get(EndDevAddr);

							if (temp.BookStatus != tempBook) {
								temp.BookStatus = tempBook;
								RasPi.endDevOp.replace(EndDevAddr, temp);
							}
						}

						if (tempHub.equals(HubAddr)) {
							stmt.executeUpdate(
									String.format("UPDATE enddev_tb SET BatteryStatus = %d WHERE EndDevAddr = '%s';",
											BatteryStatus, EndDevAddr));
						} else {
							stmt.executeUpdate(String.format(
									"UPDATE enddev_tb SET HubAddr = %s, BatteryStatus = %d WHERE EndDevAddr = '%s';",
									HubAddr, BatteryStatus, EndDevAddr));
						}

						if (tempLock != LockStatus) {
							return (tempLock ? "true" : "false");
						}
					} else {
					}

				} else {
					stmt.executeUpdate(String.format(
							"INSERT INTO enddev_tb ( EndDevAddr, HubAddr) VALUES ( '%s', '%s');", EndDevAddr, HubAddr));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			dbcon.myRollback();
		}
		return "ok";
	}

	public void raspiSreachCode(String EndDevAddr, boolean SreachCode) {
		try {
			rs = stmt.executeQuery(
					String.format("SELECT SreachCode FROM enddev_tb WHERE EndDevAddr = '%s';", EndDevAddr));
			if (rs.next()) {
				int DBSreachCode = rs.getInt("SreachCode");
				if (DBSreachCode == 1 && SreachCode) {
					stmt.executeUpdate(
							String.format("UPDATE enddev_tb SET SreachCode = 2 where EndDevAddr = '%s';", EndDevAddr));
				} else if (DBSreachCode == 2 && !SreachCode) {
					stmt.executeUpdate(
							String.format("UPDATE enddev_tb SET SreachCode = 0 where EndDevAddr = '%s';", EndDevAddr));
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			dbcon.myRollback();
		}

	}

	public JSONArray raspiXbeeSreach(String HubAddr) {
		JSONArray result = new JSONArray();
		try {
			rs = stmt.executeQuery(String
					.format("SELECT EndDevAddr FROM enddev_tb WHERE HubAddr = '%s' and SreachCode = 1;", HubAddr));
			while (rs.next()) {
				JSONObject resultObj = new JSONObject();
				resultObj.put("EndDevAddr", rs.getString("EndDevAddr"));
				resultObj.put("SreachCode", "c");
				result.add(resultObj);
			}
			return result;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
	}

	public void workStatusChange(String EndDevAddr, boolean WorkStatus) {
		try {
			int BookingNo = 0;
			int BookingType = 0;
			rs = stmt.executeQuery(String.format(
					"SELECT BookingNo, BookingType FROM booking_tb WHERE EndDevAddr = '%s' and BookingType != 2 order by BookingNo DESC limit 1;",
					EndDevAddr));
			while (rs.next()) {
				BookingNo = rs.getInt("BookingNo");
				BookingType = rs.getInt("BookingType");
			}
			if (BookingNo != 0) {
				if (WorkStatus) {
					stmt.executeUpdate(String.format(
							"UPDATE enddev_tb SET LockStatus=true, WorkStatus = %b, BookStatus = false WHERE EndDevAddr = '%s';",
							WorkStatus, EndDevAddr));

					stmt.executeUpdate(
							String.format("UPDATE booking_tb SET BookingType = 1 WHERE BookingNo = %d;", BookingNo));

					stmt.executeUpdate(String
							.format("INSERT INTO workdata_tb ( BookingNo, WorkStart) VALUES ( %d, now());", BookingNo));
				} else {
					stmt.executeUpdate(String.format("UPDATE enddev_tb SET WorkStatus = %b WHERE EndDevAddr = '%s';",
							WorkStatus, EndDevAddr));
					if (BookingType != 0) {
						stmt.executeUpdate(String.format("UPDATE booking_tb SET BookingType = 2 WHERE BookingNo = %d;",
								BookingNo));
					}
					stmt.executeUpdate(
							String.format("UPDATE workdata_tb SET WorkEnd = now() WHERE BookingNo = %d;", BookingNo));

					EndDevData temp = RasPi.endDevOp.get(EndDevAddr);
					if (temp.sendMsgCode) {
						sendWorkEndMsg(BookingNo);
						temp.sendMsgCode = false;
						RasPi.endDevOp.replace(EndDevAddr, temp);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			dbcon.myRollback();
		}
	}
}
