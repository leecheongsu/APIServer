package com.insrb.app;

import com.insrb.app.filter.AuthFilter;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FilterConfig implements WebMvcConfigurer {

	// uncomment this and comment the @Component in the filter class definition to register only for a url pattern
	@Bean
	public FilterRegistrationBean<AuthFilter> rulOfAuthFilter() {
		FilterRegistrationBean<AuthFilter> registrationBean = new FilterRegistrationBean<>();

		registrationBean.setFilter(new AuthFilter());
		// Rule out : /okcert/rtn/*
		// Rule out : /kginicis/rtn/*
		// Rule out : /kginicis/noti/*
		registrationBean.addUrlPatterns("/users/*", "/house/*", "/ww/*", "/okcert/house/*", "/okcert/ww/*", "/kginicis/vacct/*", "/batch/*",
				"/brs/*", "/hsart/*", "/ocr/*", "/apis/*", "/sociallogin/*");

		return registrationBean;
	}

	@Bean
	public ConfigurableServletWebServerFactory webServerFactory() {
		TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
		factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
			@Override
			public void customize(Connector connector) {
				connector.setProperty("relaxedQueryChars", "|{}[]");
			}
		});
		return factory;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins("*")
				.allowedMethods("GET", "POST");
	}
}
