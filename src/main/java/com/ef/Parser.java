package com.ef;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

public class Parser {

	public class Key {
		public static final String ACCESS_LOG = "accesslog";
		public static final String START_DATE = "startDate";
		public static final String DURATION = "duration";
		public static final String THRESHOLD = "threshold";
	}
	
	public enum Duration {
		daily, hourly
	}

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static void main(String[] args) throws Exception {
		/********************************************************************
		 * 1. Create database connection 
		 * 2. Create all necessary tables and store procedures
		 ********************************************************************/
		Class.forName("com.mysql.jdbc.Driver");
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties props = new Properties();
		props.load(loader.getResourceAsStream("db.properties"));

		Parser parser = new Parser();

		String connStr = props.getProperty("ConnString");
		String username = props.getProperty("Username");
		String password = props.getProperty("Password");
		
		Connection conn = parser.createDbConnection(connStr, username, password);
		conn.setAutoCommit(true);

		String schema = parser.readSqlFile("/schema.sql");
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(schema);

		/********************************************************************
		 * 3. Parsing arguments 
		 * 4. Validate arguments 
		 ********************************************************************/
		Map<String, String> params = parser.parseArgs(args);
		if (parser.validateParams(params)) {
			switch (params.size()) {
			case 1:
				parser.persistsLogToDb(params.get(Key.ACCESS_LOG), conn);
				break;
			case 3:
				Duration type = Duration.valueOf(params.get(Key.DURATION));
				switch (type) {
				case daily:
					String date = params.get(Key.START_DATE).split("\\.")[0];
					parser.blockIPsByDailyThreshold(conn, date, Integer.parseInt(params.get(Key.THRESHOLD)));
					break;
				case hourly:
					String datetime = params.get(Key.START_DATE).replaceAll("\\.", " ");
					parser.blockIPsByHourly(conn, datetime, Integer.parseInt(params.get(Key.THRESHOLD)));
					break;
				}
				break;
			}
		}
	}
	
	/**
	 * Block IPs by hourly threshold
	 */
	public void blockIPsByHourly(Connection conn, String datetime, int threshold) throws Exception {
		
	}
	
	/**
	 * Block IPs by daily threshold
	 */
	public void blockIPsByDailyThreshold(Connection conn, String date, int threshold) throws Exception {
		/**
		 * 1. Perform store procedure call
		 * 2. Insert each record into tbl_blocked
		 */
		String callSql = "CALL `daily_passed_threshold_ips`(?, ?)";
		CallableStatement callStmt = conn.prepareCall(callSql);
		callStmt.setString(1, date);
		callStmt.setInt(2, threshold);
		ResultSet rs = callStmt.executeQuery();
		
		String writeSql = "INSERT INTO tbl_blocked(`time`, ip, hits, threshold, duration) VALUES(?,?,?,?,?)";
		PreparedStatement pstmt2 = conn.prepareStatement(writeSql);
		while(rs.next()) {
			String ip = rs.getString(1);
			System.out.println(ip);
			pstmt2.setString(1, sdf.format(new Date()));
			pstmt2.setString(2, ip);
			pstmt2.setLong(3, rs.getLong(2));
			pstmt2.setLong(4, threshold);
			pstmt2.setString(5, Duration.daily.name());
			pstmt2.addBatch();
		}
		pstmt2.executeBatch();
	}
	
	/**
	 * Parse and persists log records to db
	 */
	public void persistsLogToDb(String path, Connection conn) throws Exception {
		/************************************************************
		 * 1. Open log file
		 * 2. Parsing each line
		 * 3. Persists each line to database
		 ************************************************************/
		FileInputStream is = null;
		Scanner sc = null;
		try {
			String insert = "INSERT INTO tbl_logs(time, ip, method, response, client) VALUES(?,?,?,?,?)";
			PreparedStatement pstmt = conn.prepareStatement(insert);
			
			is = new FileInputStream(path);
			sc = new Scanner(is, "UTF-8");
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] items = line.split("\\|");
				pstmt.setString(1, items[0]);
				pstmt.setString(2, items[1]);
				pstmt.setString(3, items[2]);
				pstmt.setString(4, items[3]);
				pstmt.setString(5, items[4]);
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			
			if (sc.ioException() != null) {
				throw sc.ioException();
			}
		} finally {
			if (is != null) {
				is.close();
			}
			if (sc != null) {
				sc.close();
			}
		}
	}

	/**
	 * Create a db connection
	 */
	public Connection createDbConnection(String connString, String username, String passwd) throws Exception {
		return DriverManager.getConnection(connString, username, passwd);
	}

	/**
	 * Read sql file content to string
	 */
	public String readSqlFile(String path) throws Exception {
		InputStream in = this.getClass().getResourceAsStream(path);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder contentBuilder = new StringBuilder();
		String sCurrentLine;
		while ((sCurrentLine = reader.readLine()) != null) {
			contentBuilder.append(sCurrentLine).append("\n");
		}
		return contentBuilder.toString();
	}

	/**
	 * Validates input arguments
	 */
	public boolean validateParams(Map<String, String> params) {
		switch (params.size()) {
		case 1:
			if (params.containsKey(Key.ACCESS_LOG)) {
				if (params.get(Key.ACCESS_LOG).length() > 0) {
					return true;
				}
			}
			break;
		case 3:
			if (params.containsKey(Key.START_DATE) && params.containsKey(Key.DURATION)
					&& params.containsKey(Key.THRESHOLD)) {
				try {
					sdf.parse(params.get(Key.START_DATE).replaceAll("\\.", " "));
					if (params.get(Key.DURATION).equalsIgnoreCase("daily")
							|| params.get(Key.DURATION).equalsIgnoreCase("hourly")) {
						try {
							Integer.parseInt(params.get(Key.THRESHOLD));
							return true;
						} catch (Exception e) {
						}
					}
				} catch (Exception e) {
				}
			}
			break;
		}
		return false;
	}

	/**
	 * Parse string array of input arguments
	 */
	public Map<String, String> parseArgs(String[] args) {
		String joined = String.join("", args);
		String[] params = joined.split("--");
		Map<String, String> map = new HashMap<String, String>();
		for (int x = 0; x < params.length; x++) {
			if (params[x].length() > 0) {
				String[] param = params[x].split("=");
				String key = param[0];
				String val = param[1];
				map.put(key, val);
			}
		}
		return map;
	}

}
