package com.eroelf.tfserver.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author weikun.zhong
 */
@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer
{
	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer)
	{
		configurer.mediaType("html", MediaType.TEXT_HTML);
		configurer.mediaType("json", MediaType.APPLICATION_JSON_UTF8);
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters)
	{
		Gson gson=new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyyMMdd HH:mm:ss.SSS").create();
		GsonHttpMessageConverter messageConverter=new GsonHttpMessageConverter();
		messageConverter.setGson(gson);
		converters.add(messageConverter);
		WebMvcConfigurer.super.configureMessageConverters(converters);
	}
}
