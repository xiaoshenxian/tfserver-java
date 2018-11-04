package com.eroelf.tfserver.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * 
 * @author weikun.zhong
 */
public class MvcWebApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer
{
	@Override
	protected Class<?>[] getRootConfigClasses()
	{
		return null;
	}

	@Override
	protected Class<?>[] getServletConfigClasses()
	{
		return new Class[]{WebMvcConfig.class};
	}

	@Override
	protected String[] getServletMappings()
	{
		return new String[]{"/"};
	}
}
