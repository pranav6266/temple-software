package com.pranav.temple_software.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
	private static ConfigManager instance;
	private Properties properties;

	private ConfigManager() {
		loadProperties();
	}

	public static ConfigManager getInstance() {
		if (instance == null) {
			instance = new ConfigManager();
		}
		return instance;
	}

	private void loadProperties() {
		properties = new Properties();
		try (InputStream input = ConfigManager.class.getResourceAsStream("/config.properties")) {
			if (input != null) {
				properties.load(input);
			}
		} catch (IOException e) {
			System.err.println("Failed to load config.properties: " + e.getMessage());
		}
	}

	public String getProperty(String key) {
		return properties.getProperty(key, "");
	}
}
