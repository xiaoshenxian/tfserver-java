package com.eroelf.tfserver.controller;

import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eroelf.tfserver.data.ArrayWrapper;
import com.eroelf.tfserver.data.ArrayWrapper4J;
import com.eroelf.tfserver.data.ArrayWrapperSummary;
import com.eroelf.tfserver.data.Param;
import com.eroelf.tfserver.data.RequestInfo;
import com.eroelf.tfserver.data.Response;
import com.eroelf.tfserver.data.Sample4J;
import com.eroelf.tfserver.data.SampleSummary;
import com.eroelf.tfserver.exception.WorkingFlowException;
import com.eroelf.tfserver.service.TfService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author weikun.zhong
 */
@Controller
public class TfController
{
	private static final Logger LOGGER=LoggerFactory.getLogger(TfController.class);
	private static final Gson GSON=new GsonBuilder().serializeSpecialFloatingPointValues().create();

	@Resource
	private TfService tfService;

	@RequestMapping("/tfserver")
	@ResponseBody
	public Response index(@RequestParam String s, @RequestParam(required=false, defaultValue="") String p)
	{
		try
		{
			long start=System.currentTimeMillis();
			RequestInfo requestInfo=new RequestInfo();
			try
			{
				parseRequestParams(requestInfo, s, p);
				Map<String, ArrayWrapper> res=tfService.process(requestInfo.sample);
				requestInfo.resultSummary=res.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> new ArrayWrapperSummary(e.getValue())));
				LOGGER.info(String.format("TfServiceResultLog####%s####time cost: %d ms", GSON.toJson(requestInfo), System.currentTimeMillis()-start));
				return Response.success(res);
			}
			catch(WorkingFlowException e)
			{
				LOGGER.warn(String.format("TfServiceWorkingFlowFailedLog####%s####time cost: %d ms", GSON.toJson(requestInfo), System.currentTimeMillis()-start), e);
				return Response.failed(e.getCode(), e.getClass().getName()+": "+e.getMessage(), null);
			}
			catch(Exception e)
			{
				LOGGER.error(String.format("TfServiceFailedLog####%s####time cost: %d ms", GSON.toJson(requestInfo), System.currentTimeMillis()-start), e);
				return Response.failed(-1, e.getClass().getName()+": "+e.getMessage(), null);
			}
		}
		catch(Exception e)
		{
			LOGGER.error("Tf service failed!", e);
			return Response.failed(-1, e.getClass().getName()+": "+e.getMessage(), null);
		}
	}

	public void parseRequestParams(RequestInfo requestInfo, final String s, final String p)
	{
		Sample4J sample=GSON.fromJson(s, Sample4J.class);
		for(ArrayWrapper4J<?> arrayWrapper4J : sample.inputs.values())
		{
			arrayWrapper4J.tuneData();
		}
		requestInfo.sample=sample;
		requestInfo.sampleSummary=new SampleSummary(sample);
		try
		{
			requestInfo.param=GSON.fromJson(p, Param.class);
		}
		catch(Exception e)
		{}
	}
}
