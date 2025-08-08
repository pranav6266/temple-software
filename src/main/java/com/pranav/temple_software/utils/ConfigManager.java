package com.pranav.temple_software.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
		try (InputStream input = getClass().getResourceAsStream("/config.properties");
		     InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
			properties.load(reader);
		} catch (IOException e) {
			System.err.println("Error loading config.properties: " + e.getMessage());
		}
	}

	public String getProperty(String key) {
		return properties.getProperty(key, "");
	}
}
