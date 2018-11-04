package com.eroelf.tfserver.service;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eroelf.tfserver.data.ArrayWrapper;
import com.eroelf.tfserver.data.ArrayWrapper4Pb;
import com.eroelf.tfserver.data.ArrayWrapperSummary;
import com.eroelf.tfserver.data.Param;
import com.eroelf.tfserver.data.Sample4Pb;
import com.eroelf.tfserver.data.SampleSummary;
import com.eroelf.tfserver.datastream.RequestInfo;
import com.eroelf.tfserver.datastream.Response;
import com.eroelf.tfserver.datastream.Status;
import com.eroelf.tfserver.datastream.TfServiceGrpc.TfServiceImplBase;
import com.eroelf.tfserver.exception.WorkingFlowException;
import com.eroelf.tfserver.listener.DataInitListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.grpc.stub.StreamObserver;

/**
 * 
 * @author weikun.zhong
 */
public class TfServiceGrpc extends TfServiceImplBase
{
	private static final Logger LOGGER=LoggerFactory.getLogger(TfServiceGrpc.class);
	private static final Gson GSON=new GsonBuilder().serializeSpecialFloatingPointValues().create();

	@Override
	public void request(RequestInfo requestInfo, StreamObserver<Response> responseObserver)
	{
		Response response=run(requestInfo);
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public StreamObserver<RequestInfo> requestStream(StreamObserver<Response> responseObserver)
	{
		return new StreamObserver<RequestInfo>() {
			@Override
			public void onNext(RequestInfo value)
			{
				Response response=run(value);
				responseObserver.onNext(response);
			}

			@Override
			public void onError(Throwable t)
			{
				LOGGER.warn("requestStream cancelled!", t);
			}

			@Override
			public void onCompleted()
			{
				responseObserver.onCompleted();
			}
		};
	}

	private Response run(RequestInfo requestInfo)
	{
		Response response;
		try
		{
			long start=System.currentTimeMillis();
			com.eroelf.tfserver.data.RequestInfo info=parseRequestParams(requestInfo);
			try
			{
				Map<String, ArrayWrapper> resMap=DataInitListener.run(info.sample.getModelName(), info.sample.getModelVersion(), info.sample, ArrayWrapper4Pb.class);
				response=Response.newBuilder().setStatus(Status.newBuilder().setCode(0).setDes("")).putAllData(resMap.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getWrappedData()))).build();
				info.resultSummary=resMap.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> new ArrayWrapperSummary(e.getValue())));
				LOGGER.info(String.format("TfGrpcServiceResultLog####%s####time cost: %d ms", GSON.toJson(info), System.currentTimeMillis()-start));
			}
			catch(WorkingFlowException e)
			{
				response=Response.newBuilder().setStatus(Status.newBuilder().setCode(e.getCode()).setDes(e.getClass().getName()+": "+e.getMessage())).build();
				LOGGER.warn(String.format("TfGrpcServiceWorkingFlowFailedLog####%s####time cost: %d ms", GSON.toJson(info), System.currentTimeMillis()-start), e);
			}
			catch(Exception e)
			{
				response=Response.newBuilder().setStatus(Status.newBuilder().setCode(-1).setDes(e.getClass().getName()+": "+e.getMessage())).build();
				LOGGER.error(String.format("TfGrpcServiceFailedLog####%s####time cost: %d ms", GSON.toJson(info), System.currentTimeMillis()-start), e);
			}
		}
		catch(Exception e)
		{
			response=Response.newBuilder().setStatus(Status.newBuilder().setCode(-1).setDes(e.getClass().getName()+": "+e.getMessage())).build();
			LOGGER.error("Tf Grpc service failed!", e);
		}
		return response;
	}

	public com.eroelf.tfserver.data.RequestInfo parseRequestParams(RequestInfo requestInfo)
	{
		com.eroelf.tfserver.data.RequestInfo info=new com.eroelf.tfserver.data.RequestInfo();
		info.sample=new Sample4Pb(requestInfo.getSample());
		try
		{
			Param param=new Param();
			param.trace=requestInfo.getParam().getTrace();
			info.param=param;
		}
		catch(Exception e)
		{}
		info.sampleSummary=new SampleSummary(info.sample);
		return info;
	}
}
