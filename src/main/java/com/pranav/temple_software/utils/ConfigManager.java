// FILE: src/main/java/com/pranav/temple_software/utils/ConfigManager.java

package com.pranav.temple_software.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigManager {
	private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
	private static ConfigManager instance;
	private Properties properties;

	// Path to the internal, default config file
	private static final String INTERNAL_CONFIG_FILE = "/config.properties";

	// Path to the external, writable config file
	private static final Path EXTERNAL_CONFIG_PATH = Paths.get(DatabaseManager.APP_DATA_FOLDER, "config.properties");

	private ConfigManager() {
		loadProperties();
	}

	public static synchronized ConfigManager getInstance() {
		if (instance == null) {
			instance = new ConfigManager();
		}
		return instance;
	}

	private void loadProperties() {
		properties = new Properties();
		File externalConfigFile = EXTERNAL_CONFIG_PATH.toFile();

		// First, try to load from the external file
		if (externalConfigFile.exists()) {
			try (InputStream input = new FileInputStream(externalConfigFile)) {
				properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
				logger.info("Loaded configuration from external file: {}", externalConfigFile.getAbsolutePath());
				return; // Stop if loaded successfully
			} catch (IOException e) {
				logger.error("Error loading external config file. Falling back to defaults.", e);
			}
		}

		// If external file doesn't exist or fails to load, load from internal resources
		try (InputStream input = getClass().getResourceAsStream(INTERNAL_CONFIG_FILE)) {
			if (input == null) {
				logger.error("Default config.properties not found in resources!");
				return;
			}
			properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
			logger.info("Loaded default configuration from internal resources.");
		} catch (IOException e) {
			logger.error("Error loading default config.properties.", e);
		}
	}

	public String getProperty(String key) {
		return properties.getProperty(key, "");
	}

	public void saveProperty(String key, String value) {
		try {
			properties.setProperty(key, value);
			// Ensure the parent directory exists
			Files.createDirectories(EXTERNAL_CONFIG_PATH.getParent());
			// Write to the external file
			try (OutputStream output = new FileOutputStream(EXTERNAL_CONFIG_PATH.toFile())) {
				properties.store(new OutputStreamWriter(output, StandardCharsets.UTF_8), "Updated application properties");
				logger.info("Property saved to external config: {} = {}", key, value);
			}
		} catch (IOException e) {
			logger.error("FATAL: Could not save config.properties to external location.", e);
		}
	}
}