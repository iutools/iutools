package ca.inuktitutcomputing.utilities;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

public class StopWatchTest {

	////////////////////////////
	// DOCUMENTATION TESTS
	////////////////////////////

	@Test
	public void test__StopWatch__Synopsis() throws Exception {
		//
		// Use StopWatch to monitor execution of a long task, and raise 
		// a TimeoutException if it reaches a given maximum.
		//
		// The class can be used in one of two modes:
		// - Executor mode
		// - Standalone  mode
		//
		// In our experience, it's best to use the Executor mode as it is the 
		// that seems more reliable.
		// 
		// Executor mode
		//    In this mode, the  task is run through an ExecutorService which 
		//    will interrupt the task when the run time is exceeded. In this 
		//    mode, the StopWatch plays a dual role:
		//
		//    - ensure that the task is responsive to thread interruption
		//    - monitor running time and raise a TimeoutException 
		//
		//    The second role might seem redundant since in principle the 
		//    ExecutorService is supposed to do that monitoring. But we have 
		//    found that sometimes, the task does not get interrupted by the 
		//    ExecutorService for some unfathomable reasons.
		//
		//  
		// Standalone mode
		//    In this mode, the long task is run directly, without going through 
		//    an ExecutorService. In this mode, the StopWatch is solely 
		//    responsible for monitoring running time and raising a 
		//    if it is exceeded. 
		//
		
		// Here is how you use StopWatch in Executor mode.
		//
		// First, create a StopWatch
		long timeoutAfter = 2*1000;
		StopWatch sw = new StopWatch(timeoutAfter);
		
		// Then create a task that will use that StopWatch to check for  
		// interruptions. Note that the task must be implemented as an instance 
		// of Callable.
		//
		LongTask task = new LongTask(sw);
		
		// Run that task with an ExecutorService
		//
		ExecutorService executor = Executors.newCachedThreadPool();
		Future<Long> future = executor.submit(task);
		Long taskReturnValue = null;
		try {
			taskReturnValue = future.get(timeoutAfter, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			// Means the StopWatch caught the time overrun
			//
		} catch (ExecutionException e) {
			Exception cause = (Exception) e.getCause();
			if (cause instanceof TimeoutException) {
				// Means the Executor caught the time overrun
			} else {
				// Something else went wrong during the task's execution
			}
		}	
				
		//
		// If you don't want to use an ExecutorService to run the task, 
		// you can just run it directly using a STANDALONE StopWatch.
		// Note that in this mode, you don't need to implement the task as an 
		// instance of Callable. Any object or method that periodically checks 
		// the StopWatch can be called.
		//
		try {
			longTask(sw);
		} catch (TimeoutException e) {
			// Means the task timed out
		} catch (Exception e) {
			// Means something else went wrong
		}
	}
		
	////////////////////////////
	// VERIFICATION TESTS
	///////////////////////////
	
	@Test(expected=TimeoutException.class)
	public void test__CallThroughExecutor_Timesout() throws Exception {
		// First create a EXECUTOR mode StopWatch 
		long timeoutAfter = 1*1000;
		runExecutorTask(timeoutAfter);
	}
	
	@Test
	public void test__CallThroughExecutor__DoesNotTimeOut() throws Exception {
		// First create a EXECUTOR mode StopWatch 
		long timeoutAfter = 1*1000;
		runExecutorTask(timeoutAfter, 10);
	}

	@Test(expected=TimeoutException.class)
	public void test__CallDirectly_Timesout() throws Exception {
		// First create a EXECUTOR mode StopWatch 
		long timeoutAfter = 1*1000;
		runDirectTask(timeoutAfter);
	}
	
	@Test
	public void test__CallDirectly_NoTimeout() throws Exception {
		// First create a EXECUTOR mode StopWatch 
		long timeoutAfter = 1*1000;
		runDirectTask(timeoutAfter, 100);
	}
	
	/////////////////////////////////////
	// TEST HELPERS
	/////////////////////////////////////

	private void longTask(StopWatch sw) throws Exception {
		StopWatch stopWatch = new StopWatch(1*1000);
		int number = 0;
		while (true) {
			number++;
			stopWatch.check("Iteration #"+number);
			double dblNumber = 1.0 * number;
		}
	}
	
	private void runExecutorTask(long timeoutAfter) throws Exception {
		runExecutorTask(timeoutAfter, null);
	}

	private long runExecutorTask(long timeoutAfter, Integer numIterations) throws Exception {
		StopWatch sw = new StopWatch(timeoutAfter);
		LongTask task = new LongTask(sw, numIterations);
		ExecutorService executor = Executors.newCachedThreadPool();
		Future<Long> future = executor.submit(task);
		Long taskReturnValue = null;
		try {
			taskReturnValue = future.get(timeoutAfter, TimeUnit.MILLISECONDS);
		} catch (ExecutionException e) {
			Exception cause = (Exception) e.getCause();
			throw cause;
		}
		
		return taskReturnValue;
	}

	
	private void runDirectTask(long timeoutAfter) throws Exception {
		runDirectTask(timeoutAfter, null);
	}

	private Long runDirectTask(long timeoutAfter, Integer maxIterations) 
		throws Exception {

		long start = System.currentTimeMillis();
		
		StopWatch stopWatch = new StopWatch(timeoutAfter);
		int number = 0;
		while (true) {
			number++;
			stopWatch.check("Iteration #"+number);
			double dblNumber = 1.0 * number;
			if (maxIterations != null && number > maxIterations) {
				break;
			}
		}
		
		long elapsed = System.currentTimeMillis() - start;
		return elapsed;
	}
	
	// This is a "dummy" class used for testing the StopWatch class
	//
	///
	class LongTask implements Callable<Long> {
		
		Integer maxIterations = null;
		private StopWatch stopWatch;
		
		public LongTask(StopWatch sw) {
			initializeLongTask(sw, null);
		}

		public LongTask(StopWatch sw, Integer _maxIterations) {
			initializeLongTask(sw, _maxIterations);
		}
		
		private void initializeLongTask(StopWatch sw, Integer _maxIterations) {
			this.stopWatch = sw;
			this.maxIterations = _maxIterations;
		}

		@Override
		public Long call() throws Exception {
			long start = System.currentTimeMillis();
			int number = 0;
			while (true) {
				number++;
				stopWatch.check("Computing "+number+"!");
				double fact = number * 1.0;
				if (maxIterations != null && number > maxIterations) {
					break;
				}
			}
			
			long elapsed = System.currentTimeMillis() - start;
			
			return new Long(elapsed);
		}	
	}
}
