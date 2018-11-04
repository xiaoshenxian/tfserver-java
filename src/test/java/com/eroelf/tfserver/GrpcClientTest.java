package com.eroelf.tfserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.tensorflow.framework.DataType;

import com.eroelf.tfserver.data.ArrayWrapper4J;
import com.eroelf.tfserver.datastream.RequestInfo;
import com.eroelf.tfserver.datastream.Response;
import com.eroelf.tfserver.datastream.Sample;

/**
 * 
 * @author weikun.zhong
 */
public class GrpcClientTest
{
	public static void main(String[] args) throws InterruptedException
	{
		String host=args[0];
		int port=Integer.parseInt(args[1]);

		TfGrpcClient client=new TfGrpcClient(host, port, true);
		try
		{
			// Construct a Sample object
			String modelName="your_model_name";
			String modelVersion="your_model_version";
			String signatureName="your_signature";
			Map<String, ArrayWrapper4J<?>> inputs=new HashMap<>();
			inputs.put("your_input_name1", new ArrayWrapper4J<String>(DataType.DT_STRING, new int[]{1, 4}, Arrays.asList("a,b,c,d".split(","))));
			inputs.put("your_input_name2", new ArrayWrapper4J<Float>(DataType.DT_FLOAT, new int[]{2, 3}, Arrays.asList(new Float[]{1f, 2f, 3f, 4f, 5f, 6f})));
			Sample sample=TfGrpcClient.createSample(modelName, modelVersion, signatureName, inputs);

			// Construct a RequestInfo object
			RequestInfo requestInfo=TfGrpcClient.createRequestInfo(sample, null);

			System.out.println("Blocking unary call example:");
			Response response=client.request(requestInfo);
			String responseInfo=TfGrpcClient.formatResponse(response);
			System.out.println(responseInfo);

			System.out.println("Asynchronous stream call example:");
			List<String> resList=new ArrayList<>();
			CountDownLatch finishLatch=client.requestStream(new Iterator<RequestInfo>() {
				private int i=0;

				@Override
				public boolean hasNext()
				{
					return i++<10;
				}

				@Override
				public RequestInfo next()
				{
					return requestInfo;
				}
			}, (res) -> resList.add(TfGrpcClient.formatResponse(res)));
			if(finishLatch.await(1, TimeUnit.MINUTES))
			{
				int i=0;
				for(String resInfo : resList)
				{
					System.out.println("async "+i++);
					System.out.println(resInfo);
				}
			}
			else
				System.err.println("requestStream failed to finish within 1 minutes");
		}
		finally
		{
			client.shutdown(10, TimeUnit.SECONDS);
		}
	}
}
