package com.b2s.rewards.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesUtils {

	private final static Logger LOG = LoggerFactory.getLogger(PropertiesUtils.class);
	private static Properties properties = new Properties();
	private static PropertiesUtils config = new PropertiesUtils();
	private static Map<String, String> map = new HashMap<String, String>();

	public static synchronized PropertiesUtils getInstance(String file) {
		if (!map.containsKey(file)) {
			try {
				config.load(file);
			} catch (FileNotFoundException e) {
				LOG.error("Unknown Error",e);
			} catch (IOException e) {
				LOG.error("Unknown Error",e);
			}
		}
		return config;
	}

	private void load(String file) throws FileNotFoundException, IOException {
		properties.load(this.getClass().getClassLoader().getResourceAsStream(file));
	}

	public String getValue(String key) {
		return properties.getProperty(key);
	}

}
