package com.pranav.temple_software.utils;

import ch.qos.logback.classic.Logger;
import com.pranav.temple_software.controllers.AdminLoginController;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class ConfigManager {
	Logger logger = (Logger) LoggerFactory.getLogger(AdminLoginController.class);
	private static ConfigManager instance;
	private Properties properties;
	private static final String CONFIG_FILE = "/config.properties";

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
		try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
			assert input != null;
			try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
				assert properties != null;
				properties.load(reader);
			}
		} catch (IOException | NullPointerException e) {
			System.err.println("Error loading config.properties: " + e.getMessage());
		}
	}

	public String getProperty(String key) {
		return properties.getProperty(key, "");
	}

	// --- NEW METHOD TO SAVE PROPERTIES ---
	public void saveProperty(String key, String value) {
		try {
			properties.setProperty(key, value);

			URL resourceUrl = getClass().getResource(CONFIG_FILE);
			if (resourceUrl == null) {
				throw new FileNotFoundException("Cannot find config.properties in resources.");
			}
			File configFile = new File(resourceUrl.toURI());

			try (OutputStream output = new FileOutputStream(configFile);
			     OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
				properties.store(writer, "Updated application properties");
				System.out.println("Property saved: " + key + " = " + value);
			}
		} catch (IOException | URISyntaxException | NullPointerException e) {
			System.err.println("FATAL: Could not save config.properties. Check file permissions. Error: " + e.getMessage());
			logger.error("Could not save config ",e);
		}
	}
}