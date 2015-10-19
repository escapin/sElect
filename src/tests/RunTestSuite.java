package tests;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import selectvoting.tests.TestCoreSystem;

@SuppressWarnings("rawtypes")
public class RunTestSuite {	
	public static void main(String[] args){
		List<Class> testCases = new ArrayList<Class>();

		//Add test cases
		testCases.add(de.unitrier.infsec.tests.TestCryptoLib.class);
		testCases.add(selectvoting.tests.TestCoreSystem.class);
		testCases.add(selectvoting.tests.TestMixServer.class);


		for (Class testCase : testCases)
			runTestCase(testCase);
	}

    private static void runTestCase(Class testCase){
    	Result result = JUnitCore.runClasses(testCase);
    	for (Failure failure : result.getFailures())
            System.out.println(failure.toString());
    	System.out.println();
    }
}
