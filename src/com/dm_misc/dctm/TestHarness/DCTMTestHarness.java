package com.dm_misc.dctm.TestHarness;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DCTMTestHarness {

	public static void main(String[] args) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss.SS");
		Date now;
		Long startTime = 0L;
		Long endTime = 0L;
		
		try {

			// get current time and print harness header
			now = new Date();
			System.out.println("===== Start Test Harness " + sdf.format(now) + " =====");
			System.out.println();
			startTime = System.currentTimeMillis();
			
			// =================
			// instantiate class to test here
			DCTMTestHarness a = new DCTMTestHarness();
			a.run(args);
			// =================
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}  finally {
			// get current time and print harness footer
			System.out.println();
			now = new Date();
			endTime = System.currentTimeMillis();
			System.out.println("Total Run Time: " + (endTime - startTime) + " msec");
			System.out.println("===== End Test Harness " + sdf.format(now) + " =====");
		}


	}
	
	public DCTMTestHarness() {
		
	}
	
	private void run(String[] args) {
		
	}
}