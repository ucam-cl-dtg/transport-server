package uk.ac.cam.cl.dtg.android.time.servlet;

import java.sql.*;

/**
 * Deals with verifying authentication of API keys
 * @author dt316
 *
 */
public class KeyManager {

	/**
	 * Determines if a key is valid for a certain permission
	 * 
	 * @param key The supplied API key
	 * @param desiredPermission Permission the key is trying to use
	 * @return True if allowed, False if not
	 */
	public static boolean isValidKey(String key, String desiredPermission) {

		try {
			
			// Look for matches in the database
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

	/**
	 * Check whether the key is valid for services and if not throws an {@link InvalidKeyException}
	 * @param key
	 * @throws InvalidKeyException
	 */
	public static void isValidForServices(String key) throws InvalidKeyException{
	  if(key == null || !KeyManager.isValidKey(key, "services")) throw new InvalidKeyException();
	}


	/**
	 * The provided API key was invalid
	 * 
	 * @author drt24
	 *
	 */
	public static class InvalidKeyException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidKeyException(){
	    super("Invalid API key.");
	  }
	}
}
