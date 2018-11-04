package com.eroelf.tfserver.data;

import java.util.Map;

public class RequestInfo
{
	public Param param;
	public SampleSummary sampleSummary;
	public Map<String, ArrayWrapperSummary> resultSummary;
	public transient Sample sample;
}
