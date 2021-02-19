package com.insrb.app;

import com.insrb.app.filter.AuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

	// uncomment this and comment the @Component in the filter class definition to register only for a url pattern
	@Bean
	public FilterRegistrationBean<AuthFilter> rulOfAuthFilter() {
		FilterRegistrationBean<AuthFilter> registrationBean = new FilterRegistrationBean<>();

		registrationBean.setFilter(new AuthFilter());
		// Rule out : /okcert/rtn/*
		// Rule out : /kginicis/rtn/*
		// Rule out : /kginicis/noti/*
		registrationBean.addUrlPatterns("/users/*", "/house/*", "/ww/*", "/okcert/house/*", "/okcert/ww/*", "/kginicis/vacct/*");

		return registrationBean;
	}
}
