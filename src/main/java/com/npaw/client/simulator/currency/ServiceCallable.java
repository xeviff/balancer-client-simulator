package com.npaw.client.simulator.currency;


import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;

/**
 * 
 * @author Xavier
 *
 */
public class ServiceCallable implements Callable<String> {
	
	private static final String ENDPOINT = "localhost";
	private static final String PORT = "8080";
	private static final String SERVICE_NAME = "balancer-service";
	private static final String SERVLET_NAME = "getData";

	private String parametros;
	
	public ServiceCallable(String pars) {
		parametros=pars;
	}
	
	@Override
	public String call() {
		try {
			String urlBase = "http://{0}:{1}/{2}/{3}?"+parametros;
			String url = MessageFormat.format(urlBase, ENDPOINT, PORT, SERVICE_NAME, SERVLET_NAME);
		
			URLConnection connection = new URL(url).openConnection();
			connection.setRequestProperty("Accept-Charset", java.nio.charset.StandardCharsets.UTF_8.name());
			InputStream response = connection.getInputStream();
			String theString = IOUtils.toString(response, java.nio.charset.StandardCharsets.UTF_8.name());
			System.out.println(theString);	
			return theString;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<Future<String>> groupCall (ExecutorService executor, String parameters, int groupCallsAmount) {
		List<Future<String>> list = new ArrayList<>();
		for (int i = 0; i < groupCallsAmount; i++) {
			Callable<String> worker = new ServiceCallable(parameters);
			Future<String> submit = executor.submit(worker);
			list.add(submit);
		}		
		return list;
	}	
} 