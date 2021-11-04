package com.npaw.client.simulator.stats;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.Future;

public class StatsHelper {
	
	static DecimalFormat df = new DecimalFormat("###.##");
	
	public static int[] retrieveCallableStadisticsFromGroup (List<Future<String>> list) {
		int clusterA=0;
		int clusterB=0;
		int clusterC=0;
		// now retrieve the result
		for (Future<String> future : list) {
			try {
				String theString=future.get();
				if (theString!=null) {
					if (theString.contains("clusterA"))
						clusterA++;
					else if (theString.contains("clusterB"))
						clusterB++;
					else if (theString.contains("clusterC"))
						clusterC++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}	  
		int[] ret = {clusterA, clusterB, clusterC};
		return ret;
	}
	
	public static float[] calcNprintConcurrentCaseStats (List<Future<String>> list_B_osmf1, List<Future<String>> list_A_xbox, List<Future<String>> list_A_Panasonic1, int groupCallsAmount) {
		//Sacamos las estad�sticas del balanceo
		int[] balancedB_osmf = retrieveCallableStadisticsFromGroup(list_B_osmf1);
		System.out.println("Estadisticas de las llamadas del clienteB para el dispositivo -osmf-. Total de peticiones="+(balancedB_osmf[0]+balancedB_osmf[1]));
		float balancedB0_osmfPC = calcPer100(balancedB_osmf[0],groupCallsAmount);
		float balancedB1_osmfPC = calcPer100(balancedB_osmf[1],groupCallsAmount);
		System.out.println("clusterA = "+printPercent(balancedB0_osmfPC));
		System.out.println("clusterB = "+printPercent(balancedB1_osmfPC));
		System.out.println();	
		
		int[] balancedA_xbox = retrieveCallableStadisticsFromGroup(list_A_xbox);
		System.out.println("Estadisticas de las llamadas del clienteA para el dispositivo -xbox-. Total de peticiones="+(balancedA_xbox[0]+balancedA_xbox[1]));
		float balancedA0_xboxPC = calcPer100(balancedA_xbox[0],groupCallsAmount);
		float balancedA1_xboxfPC = calcPer100(balancedA_xbox[1],groupCallsAmount);
		System.out.println("clusterA = "+printPercent(balancedA0_xboxPC));
		System.out.println("clusterB = "+printPercent(balancedA1_xboxfPC));
		System.out.println();	
		
		int[] balancedA_panasonic = retrieveCallableStadisticsFromGroup(list_A_Panasonic1);
		System.out.println("Estadisticas de las llamadas del clienteA para el dispositivo -Panasonic-. Total de peticiones="+(balancedA_panasonic[0]+balancedA_panasonic[1]));
		float balancedA0_panasonicPC = calcPer100(balancedA_panasonic[0],groupCallsAmount);
		float balancedA1_panasonicfPC = calcPer100(balancedA_panasonic[1],groupCallsAmount);
		System.out.println("clusterA = "+printPercent(balancedA0_panasonicPC));
		System.out.println("clusterB = "+printPercent(balancedA1_panasonicfPC));
		
		float[] ret = {balancedB0_osmfPC, balancedA0_xboxPC, balancedA0_panasonicPC};
		return ret;
	}

	public static void printNonConcurrentCaseStats(float cluster1Balance, float cluster2Balance, byte cluster1LoadDef, byte cluster2LoadDef, int callsAmount) {
		String headerLog = MessageFormat.format("Balancing test made for {0} calls",callsAmount);
		System.out.println();
		System.out.println(headerLog);
		System.out.println("Balanced to cluster1Balance: "+printPercent(cluster1Balance));
		System.out.println("Balanced to cluster2Balance: "+printPercent(cluster2Balance));
		System.out.println("Error margin: "+printPercent(cluster1Balance-(float)cluster1LoadDef));
		
	}
	
	public static void printConcurrentErrorMargin(float osmfB, byte osmfLoadDef, float xboxB, byte xboxLoadDef,
			float pB, byte panasonicLoadDef) {
		System.out.println();
		System.out.println("Margenes de error: ");
		System.out.println("osmf: "+printPercent(osmfB-(float)osmfLoadDef));
		System.out.println("xbox: "+printPercent(xboxB-(float)xboxLoadDef));
		System.out.println("panasonic: "+printPercent(pB-(float)panasonicLoadDef));
	}
	
	public static String printPercent (float value) {
		return df.format(value)+" %";
	}
	
	public static float calcPer100 (int amount, int total) {
		float perc = amount*(float)100/(float)total;
		return perc;
	}
	
	public static boolean isValueInAllowedErrMargin (float value, byte defined, byte margin) {
		float mgn = (float)margin;
		float def = (float)defined;
		return value >= def-mgn && value <= def+mgn;
	}


}
