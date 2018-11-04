package com.eroelf.tfserver.data;

/**
 * 
 * @author weikun.zhong
 */
public class Response
{
	public static class Status
	{
		public int code;
		public String des;

		public Status(int code, String des)
		{
			this.code=code;
			this.des=des;
		}
	}

	public Status status;
	public Object data;

	public static Response success(final Object obj)
	{
		Response response=new Response();
		response.status=new Status(0, "");
		response.data=obj;
		return response;
	}

	public static Response failed(final int code, final String des, final Object obj)
	{
		Response response=new Response();
		response.status=new Status(code, des);
		response.data=obj;
		return response;
	}
}
