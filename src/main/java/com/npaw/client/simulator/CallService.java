package com.npaw.client.simulator;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;

import com.npaw.client.simulator.currency.ServiceCallable;



/**
 * Clase de test que llama el servicio
 * @author Xavier
 *
 */
public class CallService {


	/**
	 * Este m�todo hace una llamada en masa concurrente de varias peticiones, desordenadamente
	 * para simular una situaci�n el m�ximo de realista. En el final se sacan las estad�sticas del balanceo.
	 * @throws Exception
	 */
	public static List<List<Future<String>>> concurrentExecution (int threadsAmount, int groupCallsAmount) throws Exception {
		
		ExecutorService executor = Executors.newFixedThreadPool(threadsAmount);
		
		List<Future<String>> list_A_Panasonic1 = ServiceCallable.groupCall(executor, "accountCode=clienteA&targetDevice=Panasonic", groupCallsAmount);
		
		List<Future<String>> list_B_osmf1 = ServiceCallable.groupCall(executor, "accountCode=clienteB&targetDevice=osmf", groupCallsAmount);
		
		List<Future<String>> list_A_xbox1 = ServiceCallable.groupCall(executor, "accountCode=clienteA&targetDevice=xbox", groupCallsAmount);
		
		List<Future<String>> list_B_osmf2 = ServiceCallable.groupCall(executor, "accountCode=clienteB&targetDevice=osmf", groupCallsAmount);		
		
		List<Future<String>> list_A_xbox2 = ServiceCallable.groupCall(executor, "accountCode=clienteA&targetDevice=xbox", groupCallsAmount);
		
		List<Future<String>> list_A_Panasonic2 = ServiceCallable.groupCall(executor, "accountCode=clienteA&targetDevice=Panasonic", groupCallsAmount);
		
		// and finish all existing threads in the queue
		executor.shutdown();
		
		//esperamos a que termine para recoger las estad�sticas
		executor.awaitTermination(30, TimeUnit.SECONDS);
		
		//Merge groups of the same account+device
		list_B_osmf1.addAll(list_B_osmf2);
		list_A_xbox1.addAll(list_A_xbox2);
		list_A_Panasonic1.addAll(list_A_Panasonic2);
		
		return Arrays.asList(
			list_B_osmf1,
	        list_A_xbox1,
	        list_A_Panasonic1
	    );
	}	
	
	/**
	 * Con este se comprueba el correcto funcionamiento del balanceo de carga. 
	 * Se puede probar con 2, 3 o m�s clusters (s�lo hay que a�adirlos correctamente en el fichero properties)
	 * @throws Exception
	 */
	public static int[] noConcurrent (int callsAmount) throws Exception {
		int clusterA=0;
		int clusterB=0;
		int clusterC=0;
		String url = "http://localhost:8080/balancer-service/getData?accountCode=clienteB&targetDevice=osmf";

		for (int i=0; i<callsAmount; i++) {
			URLConnection connection = new URL(url).openConnection();
			connection.setRequestProperty("Accept-Charset", java.nio.charset.StandardCharsets.UTF_8.name());
			InputStream response = connection.getInputStream();
			String theString = IOUtils.toString(response, java.nio.charset.StandardCharsets.UTF_8.name());
			System.out.println(theString);
			if (theString.contains("clusterA"))
				clusterA++;
			else if (theString.contains("clusterB"))
				clusterB++;
			else if (theString.contains("clusterC"))
				clusterC++;
		}
		System.out.println("a:"+clusterA);
		System.out.println("B:"+clusterB);
//		System.out.println("C:"+clusterC);
		int[] ret = {clusterA,clusterB,clusterC};
		return ret;
	}
	
	public static List<Future<String>> fakeAccountExecution (int threadsAmount, int groupCallsAmount) throws InterruptedException {
		
		ExecutorService executor = Executors.newFixedThreadPool(threadsAmount);
		
		List<Future<String>> results = ServiceCallable.groupCall(executor, "accountCode=HACKER&targetDevice=ALL", groupCallsAmount);
		
		// and finish all existing threads in the queue
		executor.shutdown();
		
		//esperamos a que termine para recoger las estad�sticas
		executor.awaitTermination(5, TimeUnit.SECONDS);
		
		return results;
	}

}
