package com.pranav.temple_software.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
	private static final ConfigManager instance = new ConfigManager();
	private final Properties properties = new Properties();

	private ConfigManager() {
		try (InputStream input = ConfigManager.class.getResourceAsStream("/config.properties")) {
			if (input == null) {
				System.err.println("Sorry, unable to find config.properties");
				return;
			}
			// Load a properties file from class path, inside static method
			properties.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static ConfigManager getInstance() {
		return instance;
	}

	public String getProperty(String key) {
		return properties.getProperty(key, ""); // Return empty string if key not found
	}
}