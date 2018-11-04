package com.eroelf.tfserver.data;

/**
 * 
 * @author weikun.zhong
 */
public class Param
{
	public String trace;

	public Param()
	{}

	public Param(com.eroelf.tfserver.datastream.Param param)
	{
		trace=param.getTrace();
	}
}
