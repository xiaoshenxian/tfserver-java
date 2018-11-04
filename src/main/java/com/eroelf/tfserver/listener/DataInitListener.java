package com.eroelf.tfserver.listener;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eroelf.tfserver.controller.TfGrpcController;
import com.eroelf.tfserver.data.ArrayWrapper;
import com.eroelf.tfserver.data.Sample;
import com.eroelf.tfserver.model.ModelPool;

/**
 * 
 * @author weikun.zhong
 */
public class DataInitListener implements ServletContextListener
{
	private static final Logger LOGGER=LoggerFactory.getLogger(DataInitListener.class);

	private static ModelPool modelPool;
	private static ScheduledExecutorService ses=Executors.newScheduledThreadPool(1);

	private TfGrpcController grpcController;

	public DataInitListener()
	{}

	public static Map<String, ArrayWrapper> run(String modelName, String modelVersion, Sample sample, Class<? extends ArrayWrapper> outputClass) throws InterruptedException
	{
		return modelPool.run(modelName, modelVersion, sample, outputClass);
	}

	@Override
	public void contextInitialized(final ServletContextEvent sce)
	{
		LOGGER.info("Begin initialization...");
		try
		{
			Properties properties=new Properties();
			properties.load(DataInitListener.class.getResourceAsStream("/config.properties"));
			modelPool=new ModelPool(properties.getProperty("model_base_dir"));
			int defaultWorkerNum=Integer.parseInt(properties.getProperty("default_worker_num"));
			int maxWorkerNum=Integer.parseInt(properties.getProperty("max_worker_num"));
			modelPool.update(defaultWorkerNum, maxWorkerNum);
			ses.scheduleWithFixedDelay(() -> {
				modelPool.update(defaultWorkerNum, maxWorkerNum);
			}, 0, Integer.parseInt(properties.getProperty("update_schedule_seconds")), TimeUnit.SECONDS);
			grpcController=new TfGrpcController(Integer.parseInt(properties.getProperty("grpc_port")));
			grpcController.start();
			LOGGER.info("Initialization finished");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			LOGGER.error("Initialization error!", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		try
		{
			LOGGER.info("Cancelling scheduled model updating procedure...");
			ses.shutdownNow();
			while(!ses.awaitTermination(1, TimeUnit.MINUTES));
			LOGGER.info("Scheduled model updating procedure has been cancelled.");

			LOGGER.info("Shutting down gRPC server...");
			grpcController.stop();
			grpcController.awaitTermination();
			LOGGER.info("gRPC server has been shut down.");

			LOGGER.info("Closing model pool...");
			modelPool.close();
			LOGGER.info("Model pool has been closed.");
		}
		catch(Exception e)
		{
			LOGGER.error("contextDestroyed error!", e);
		}
	}
}
