package com.eroelf.tfserver.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.eroelf.tfserver.data.ArrayWrapper;
import com.eroelf.tfserver.data.ArrayWrapper4J;
import com.eroelf.tfserver.data.Sample;
import com.eroelf.tfserver.listener.DataInitListener;

/**
 * 
 * @author weikun.zhong
 */
@Service
public class TfService
{
	public Map<String, ArrayWrapper> process(final Sample sample) throws InterruptedException
	{
		return DataInitListener.run(sample.getModelName(), sample.getModelVersion(), sample, ArrayWrapper4J.class);
	}
}
