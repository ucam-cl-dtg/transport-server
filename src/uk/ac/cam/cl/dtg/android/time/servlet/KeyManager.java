package uk.ac.cam.cl.dtg.android.time.servlet;

import java.sql.*;
import java.util.Properties;

public class KeyManager {

	public static boolean isValidKey(String key, String desiredPermission) {

		try {
			
			PreparedStatement checkStatement;
			Connection conn = DatabaseManager.getConnection();
			checkStatement = conn.prepareStatement("SELECT COUNT(1) FROM allowed_keys LEFT OUTER JOIN permissions ON (permissions.permission_id=allowed_keys.permission_id) WHERE key = ? AND permission_string = ?");
	
			checkStatement.setString(1, key);
			checkStatement.setString(2, desiredPermission);
			
			ResultSet rs = checkStatement.executeQuery();
			
			rs.next();
			int count = rs.getInt(1);

			return (count > 0) ? true : false;

		} catch (SQLException e) {
			
			System.err.println("Exception during isValidKey: "+e);
			return false;
			
		}
	}



}
