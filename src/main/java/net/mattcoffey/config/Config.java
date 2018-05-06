package net.mattcoffey.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Immutable file configuration container
 * 
 * @author mcoffey
 */
public class Config {
    
    /**
     * The file configuration is loaded into this 
     */
    private final Properties properties = new Properties();
   
    /**
     * Constructor.
     */
    public Config() {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("config.properties");
        try {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load config.properties file.");
        }
    }
    
    /**
     * @return the shopping list location
     */
    public String getShoppingListPath() {
        return properties.getProperty("shoppingListPath");
    }

    /**
     * @return the password
     */
    public CharSequence getPassword() {
        return properties.getProperty("password");
    }

    /**
     * @return the username
     */
    public CharSequence getUsername() {
        return properties.getProperty("username");
    }

    /**
     * @return the retry interval
     */
    public long getRetryInterval() {
        return Long.parseLong(properties.getProperty("retryInterval"));
    }

    /**
     * @return the maximum number of retries
     */
    public int getMaxRetries() {
        return Integer.parseInt(properties.getProperty("maxRetries"));
    }

    /**
     * @return the path to the firefox driver
     */
	public String getDriverPath() {
		return properties.getProperty("webdriver.gecko.driver");
	}
    
    
}