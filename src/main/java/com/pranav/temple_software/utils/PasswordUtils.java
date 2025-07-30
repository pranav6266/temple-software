package com.pranav.temple_software.utils;

import com.pranav.temple_software.utils.BCrypt;

/**
 * A utility class for handling password hashing and verification using the jBCrypt library.
 * This ensures that passwords are never stored in plain text.
 */
public class PasswordUtils {

	/**
	 * Hashes a plain-text password using the BCrypt algorithm.
	 * The gensalt method automatically handles creating a random salt.
	 *
	 * @param plainPassword The password to hash.
	 * @return A salted and hashed password string (e.g., "$2a$12$Abc...").
	 */
	public static String hashPassword(String plainPassword) {
		// The second argument to gensalt is the log_rounds (work factor).
		// 12 is a good modern balance between security and performance.
		return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
	}

	/**
	 * Checks if a plain-text password matches a previously hashed password.
	 *
	 * @param plainPassword  The password entered by the user.
	 * @param hashedPassword The hashed password retrieved from the database.
	 * @return true if the passwords match, false otherwise.
	 */
	public static boolean checkPassword(String plainPassword, String hashedPassword) {
		// BCrypt.checkpw automatically extracts the salt from the hashed password
		// and performs the comparison securely.
		try {
			return BCrypt.checkpw(plainPassword, hashedPassword);
		} catch (Exception e) {
			// Log the error in case of a malformed hash or other issue.
			System.err.println("Error during password check: " + e.getMessage());
			return false;
		}
	}
}
