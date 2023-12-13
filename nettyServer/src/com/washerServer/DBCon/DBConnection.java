package com.washerServer.DBCon;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DBConnection {
	Connection con = null;
	ResultSet rs;
	Statement stmt;

	public DBConnection() {
		startDB();
	}

	public Connection getConnection() {
		return con;
	}

	public void startDB() {
		try {
			String driver = "org.mariadb.jdbc.Driver";
			Class.forName(driver);
//			String url = "jdbc:mariadb://127.0.0.1:3306/washerDB";
			String url = "jdbc:mariadb://192.168.100.50:3306/washerDB";
			String user = "root";
			String psword = "1234";

			con = DriverManager.getConnection(url, user, psword);
			con.setAutoCommit(false); // make sure auto commit is off!

			stmt = con.createStatement();

		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void myCommit() {
		try {
			con.commit();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public void myRollback() {
		try {
			con.rollback();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public void destroyCon() {
		if (con != null) {
			try {
				stmt.close(); // close the second statement
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
