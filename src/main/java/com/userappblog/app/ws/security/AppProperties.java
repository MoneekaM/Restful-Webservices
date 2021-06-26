package com.userappblog.app.ws.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AppProperties {

	@Autowired
	private Environment env;
	
	//This method used to read the application properties from properties file using environment
	public String getTokeSecret() {
		return env.getProperty("tokensecret");
	}
}
