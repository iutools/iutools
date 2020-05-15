package ca.inuktitutcomputing.utilities;

import static org.junit.Assert.*;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.junit.Test;

import com.google.common.math.IntMath;

import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzerException;
import ca.nrc.debug.Debug;

public class StopWatchTest {

	////////////////////////////
	// DOCUMENTATION TESTS
	////////////////////////////

	@Test(expected=TimeoutException.class)
	public void test__WhyWeNeed__StopWatch() throws Exception {
		//
		// Use StopWatch to monitor execution of a long task, and raise 
		// a TimeoutException if it reaches a given maximum.
		//
		// In theory we shouldn't need a homegrown class to do this, as 
		// you are supposed to do be able to do this using Java's 
		// native Executor class.
		//
		// But we have found this native approach to cause all sorts of problems 
		// with hanging threads and all that.
		// 
		// But for what it's worth, here is how you would normally handle this 
		// with native Java classes 
		//
		
		long timeoutAfter = 2*1000;
		LongTask task = new LongTask();
		ExecutorService executor = Executors.newCachedThreadPool();
		Future<Long> future = executor.submit(task);
		Long taskReturnValue = null;
		try {
			taskReturnValue = future.get(timeoutAfter, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// Means execution was interrupted somehow
		} catch (ExecutionException e) {
			Exception cause = (Exception) e.getCause();
			if (cause instanceof TimeoutException) {
				// Means the task reached its timeout limit
			} else {
				// Something else went wrong during the task's execution
			}
		} finally {
			// "finalize" the task's execution.
			future.cancel(true); // may or may not desire this
		}	
	}
		
	/////////////////////////////////////
	// TEST HELPERS
	/////////////////////////////////////

	/**
	 * This is a "dummy" class used for testing the StopWatch class
	 * @author desilets
	 *
	 */
	class LongTask implements Callable<Long> {
		
		Integer maxIterations = null;
		
		public LongTask() {
			initializeLongTask(null);
		}

		public LongTask(Integer _maxIterations) {
			initializeLongTask(_maxIterations);
		}
		
		private void initializeLongTask(Integer _maxIterations) {
			this.maxIterations = _maxIterations;
		}

		@Override
		public Long call() throws Exception {
			long start = System.currentTimeMillis();
			int number = 0;
			while (true) {
				number++;
				try {
					long fact = CombinatoricsUtils.factorial(number);
//					System.out.println(" = "+fact);
				} catch (Exception e) {
					// Do nothing... Factorial may raise arithmetic exceptions 
					// once we get to a large enough number. This does not 
					// matter since we are only invoking it to create a long 
					// task.
//					System.out.println("raised exception e="+e.getMessage());
				}
				if (maxIterations != null && number > maxIterations) {
					break;
				}
			}
			
			long elapsed = System.currentTimeMillis() - start;
			
			return new Long(elapsed);
		}	
	}
}
