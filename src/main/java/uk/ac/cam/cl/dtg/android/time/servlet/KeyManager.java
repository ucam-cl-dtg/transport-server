package uk.ac.cam.cl.dtg.android.time.servlet;

import java.sql.*;

/**
 * Deals with verifying authentication of API keys
 * 
 * @author dt316
 * 
 */
public class KeyManager {

	/**
	 * Determines if a key is valid for a certain permission
	 * 
	 * @param key
	 *            The supplied API key
	 * @param desiredPermission
	 *            Permission the key is trying to use
	 * @param db
	 * @return True if allowed, False if not
	 */
	public static boolean isValidKey(String key, String desiredPermission,
			Connection db) {
		try {
			PreparedStatement checkStatement = db
					.prepareStatement("SELECT COUNT(1) "
							+ "FROM allowed_keys "
							+ "LEFT OUTER JOIN permissions "
							+ "ON (permissions.permission_id=allowed_keys.permission_id) WHERE key = ? AND permission_string = ?");
			try {
				checkStatement.setString(1, key);
				checkStatement.setString(2, desiredPermission);
				ResultSet rs = checkStatement.executeQuery();
				try {
					if (rs.next()) {
						int count = rs.getInt(1);
						return count > 0;
					}
				} finally {
					rs.close();
				}
			} finally {
				checkStatement.close();
			}
		} catch (SQLException e) {
			// TODO: some sort of logging framework needed...
			System.err.println("Error checking for key: " + e.getMessage());
		}
		return false;
	}

	/**
	 * Check whether the key is valid for services and if not throws an
	 * {@link InvalidKeyException}
	 * 
	 * @param key
	 * @param db
	 *            TODO
	 * @throws InvalidKeyException
	 */
	public static void isValidForServices(String key, Connection db)
			throws InvalidKeyException {
		if (key == null || !isValidKey(key, "services", db))
			throw new InvalidKeyException();
	}

	/**
	 * The provided API key was invalid
	 * 
	 * @author drt24
	 * 
	 */
	public static class InvalidKeyException extends Exception {
		private static final long serialVersionUID = 1L;

		public InvalidKeyException() {
			super("Invalid API key.");
		}
	}
}
