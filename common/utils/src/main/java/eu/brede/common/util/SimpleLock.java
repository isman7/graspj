package eu.brede.common.util;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.concurrent.Semaphore;

public class SimpleLock implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected transient Semaphore resource = new Semaphore(1);
	
	private Object readResolve() throws ObjectStreamException {
		resource = new Semaphore(1);
		return this;
	}
	
	public SimpleLock() {
		resource = new Semaphore(1);
	}
	
	public void lock() {
		resource.acquireUninterruptibly();
	}
	
	public void unlock() {
		resource.release();
	}
}
