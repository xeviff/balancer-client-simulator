package com.npaw.client.simulator;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.Future;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.npaw.client.simulator.stats.StatsHelper;

/**
 * Unit test for simple App.
 */
class CallServiceTest
{
	
	@Test
	@DisplayName("Non concurrency Test >>> Match 50-50 requisited with max 1% error margin")
	void should_beAlmost50vs50_WhenClienteB_osfm () throws Exception {
		//given
		int callsAmount = 10000;
		byte cluster1LoadDef = 50; 
		byte cluster2LoadDef = 50;
		byte errMrgn = 1; //%
		
		//when
		int[] results = CallService.noConcurrent(callsAmount);
		
		//then
		float cluster1Balance = StatsHelper.calcPer100(results[0],callsAmount);
		float cluster2Balance = StatsHelper.calcPer100(results[1],callsAmount);
		
		StatsHelper.printNonConcurrentCaseStats(cluster1Balance, cluster2Balance, cluster1LoadDef, cluster2LoadDef, callsAmount);

		assertAll(
				() -> assertTrue(StatsHelper.isValueInAllowedErrMargin(cluster1Balance, cluster1LoadDef, errMrgn)),
				() -> assertTrue(StatsHelper.isValueInAllowedErrMargin(cluster2Balance, cluster2LoadDef, errMrgn))
				);
	}
	
	@Test
	@DisplayName("Concurrency Test, All devices >>> osmf:50-50, xbox:30-70, panasonic:100/0")
	void should_matchPercetageRequirements_WhenConcurrentMassCallsDone () throws Exception {
		//given
		int threadsAmount=100;
		int groupCallsAmount=10000;
		byte errMrgn = 1; //%
		byte osmfLoadDef = 50; 
		byte xboxLoadDef = 30;
		byte panasonicLoadDef = 100;
		
		//when
		List<List<Future<String>>> results = CallService.concurrentExecution(threadsAmount, groupCallsAmount);
		
		//then
		float[] balancedLoadResults = StatsHelper.calcNprintConcurrentCaseStats(results.get(0), results.get(1), results.get(2), groupCallsAmount*2/*because they always are two groups*/);
		float osmfB = balancedLoadResults[0];
		float xboxB = balancedLoadResults[1];
		float pB = balancedLoadResults[2];
		
		StatsHelper.printConcurrentErrorMargin(osmfB, osmfLoadDef, xboxB, xboxLoadDef, pB, panasonicLoadDef);
		
		assertAll(
				() -> assertTrue(StatsHelper.isValueInAllowedErrMargin(osmfB, osmfLoadDef, errMrgn)),
				() -> assertTrue(StatsHelper.isValueInAllowedErrMargin(xboxB, xboxLoadDef, errMrgn)),
				() -> assertTrue(StatsHelper.isValueInAllowedErrMargin(pB, panasonicLoadDef, errMrgn))
				);
	}
	
	@Test
	@DisplayName("Security Test >>> Fakes Accounts not get balanced")
	void should_notBalanced_WhenFakeAccount () throws InterruptedException {
		//given
		int threadsAmount=10;
		int groupCallsAmount=100;
		
		//when
		List<Future<String>> results = CallService.fakeAccountExecution(threadsAmount, groupCallsAmount);
			
		//then
		int[] clustersBalanced = StatsHelper.retrieveCallableStadisticsFromGroup(results);
		int osmfB = clustersBalanced[0];
		int xboxB = clustersBalanced[1];
		int pB = clustersBalanced[2];
		System.out.println("Balanceds to osmfB: "+osmfB);
		System.out.println("Balanceds to xboxB: "+xboxB);
		System.out.println("Balanceds to pB: "+pB);
		
		assertAll(
				() -> assertTrue(osmfB < 1),
				() -> assertTrue(xboxB < 1),
				() -> assertTrue(pB < 1)
				);
	}
	
}
