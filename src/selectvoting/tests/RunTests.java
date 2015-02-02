package selectvoting.tests;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

@SuppressWarnings("rawtypes")
public class RunTests {	
	public static void main(String[] args){
		List<Class> testCases = new ArrayList<Class>();

		//Add test cases
		testCases.add(TestCoreSystem.class);

		for (Class testCase : testCases)
			runTestCase(testCase);
	}

    private static void runTestCase(Class testCase){
    	Result result = JUnitCore.runClasses(testCase);
    	for (Failure failure : result.getFailures())
            System.out.println(failure.toString());
    }
}
