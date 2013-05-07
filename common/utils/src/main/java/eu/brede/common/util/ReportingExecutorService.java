package eu.brede.common.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportingExecutorService extends ThreadPoolExecutor {// implements Serializable {

	final static Logger logger = LoggerFactory.getLogger(ReportingExecutorService.class);
	
	public ReportingExecutorService(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}
	
	public ReportingExecutorService() {
		this(0, Integer.MAX_VALUE,
	            60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	}
	
	@Override
	public <Y> Future<Y> submit(final Callable<Y> task) {
		Callable<Y> wrappedTask = new Callable<Y>() {
            @Override
            public Y call() throws Exception {
                try {
                    return task.call();
                }
                catch (Exception e) {
                	logger.error(e.getMessage(), e);
//                    System.out.println("Problem reported by ExecutorService:\n");
                    e.printStackTrace();
                    throw e;
                }
            }
        };
        
		return super.submit(wrappedTask);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return submit(task,null);
	}

	@Override
	public <Y> Future<Y> submit(final Runnable task, final Y result) {
		Callable<Y> wrappedTask = new Callable<Y>() {
            @Override
            public Y call() throws Exception {
                try {
                    task.run();
                }
                catch (Exception e) {
                	logger.error(e.getMessage(), e);
//                    System.out.println("Problem reported by ExecutorService:\n");
//                    e.printStackTrace();
                    throw e;
                }
                return result;
            }
        };
		return super.submit(wrappedTask);
	}	

}
