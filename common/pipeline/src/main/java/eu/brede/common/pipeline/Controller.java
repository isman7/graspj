package eu.brede.common.pipeline;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class Controller {
	private AtomicBoolean pause;
	private AtomicBoolean stop;
	private volatile boolean restartable;
	
	Set<Controllable> notificationList;
	
	public Controller() {
		this(true);
	}
	
	public Controller(boolean restartable) {
		pause = new AtomicBoolean(false);
		stop = new AtomicBoolean(false);
		this.restartable = restartable;
		this.notificationList = Collections.synchronizedSet(
				new HashSet<Controllable>());
	}
	
	public synchronized void stop() {
		setStop(true);
	}
	
	public synchronized void start() {
		if(restartable) {
			setStop(false);
		}
	}
	
	public synchronized void pause() {
		setPause(true);
	}
	
	public synchronized void resume() {
		setPause(false);
	}
	
	private synchronized void setPause(boolean newPause) {
		if(pause.compareAndSet(!newPause, newPause)) {
			notifyList();
		}
	}
	
	private synchronized void setStop(boolean newStop) {
		if(stop.compareAndSet(!newStop, newStop)) {
			notifyList();
		}
	}
	
	private synchronized void notifyList() {
		for(Controllable object : getList()) {
			synchronized(object) {
				object.notifyAll();
			}
		}
	}
	
	public boolean isPaused() {
		return pause.get();
	}
	
	public boolean isStopped() {
		return stop.get();
	}
	
	public Set<Controllable> getList() {
		return notificationList;
	}
}
