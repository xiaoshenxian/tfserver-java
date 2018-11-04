package com.eroelf.tfserver.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eroelf.tfserver.service.TfServiceGrpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * 
 * @author weikun.zhong
 */
public class TfGrpcController
{
	private static final Logger LOGGER=LoggerFactory.getLogger(TfGrpcController.class);

	private final int port;
	private final Server server;

	public TfGrpcController(int port) throws IOException
	{
		this(port, ServerBuilder.forPort(port));
	}

	public TfGrpcController(int port, ServerBuilder<?> serverBuilder)
	{
		this.port=port;
		server=serverBuilder.addService(new TfServiceGrpc()).build();
	}

	public void start() throws IOException
	{
		server.start();
		LOGGER.info("Server started, listening on "+port);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run()
			{
				// Use stderr here since the logger may has been reset by its JVM shutdown hook.
				System.err.println("*** shutting down gRPC server since JVM is shutting down");
				TfGrpcController.this.stop();
				System.err.println("*** gRPC server shut down");
			}
		});
	}

	public void stop()
	{
		if(server!=null)
			server.shutdown();
	}

	public void awaitTermination() throws InterruptedException
	{
		if(server!=null)
			server.awaitTermination();
	}
}
