package eu.brede.common.util;

public class ExceptionTools {
	public static void tryNTimes(int attempts, Runnable runnable) {
		int numExceptions = 0;
		while(numExceptions<attempts) {
			try {					
				runnable.run();
				break;
			}
			catch (Exception e) {
				System.out.println("Error occured:");
				e.printStackTrace();
				
				numExceptions++;
				if(numExceptions<attempts) {
					System.out.println("Retrying... (attempt " 
							+ (numExceptions+1) + " of " + attempts + ")");
				}
			}
		}
	}
	
	static public class OperationCanceledException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	}
}
