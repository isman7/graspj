package eu.brede.graspj.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import eu.brede.graspj.datatypes.Option;

public enum GroupManager {
	INSTANCE;
	
	private static Map<Class<Object>,Map<String,Group>> map;
	
	private GroupManager() {
		
	}
	
	protected static synchronized Map<Class<Object>,Map<String,Group>> getMap() {
		if(map==null) {			
			map = Collections.synchronizedMap(new HashMap<Class<Object>, Map<String,Group>>());
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public static synchronized Map<String,Group> getGroups(Class<?> clazz) {
		Map<String,Group> classMap = getMap().get(clazz);
		if(classMap==null) {
			classMap = Collections.synchronizedMap(new HashMap<String,Group>());
			getMap().put((Class<Object>)clazz, classMap);
		}
		return classMap;
	}
	
	public static synchronized Group getGroup(Class<?> clazz, String name) {
		Map<String,Group> classMap = getGroups(clazz);
		Group group = classMap.get(name);
		if(group==null) {
			group = new Group();
			classMap.put(name, group);
		}
		return group;
	}
	
	public static class Group extends HashSet<Object> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public synchronized boolean add(Object e) {
			return super.add(e);
		}
		
		
	}
	
	public static class GroupNames extends HashSet<String> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public GroupNames() {
			super();
		}
		
		public GroupNames(String... names) {
			this();
			for(String name : names) {
				add(name);
			}
		}
		
	}
	
	public static <T> void registerReceiver(T receiver, Option receiveActions) {
		if(receiveActions.isSelected()) {
			Set<String> names = receiveActions.gett("groupNames");
			for(String name : names) {
				Group group = GroupManager.getGroup(receiver.getClass(), name);
				synchronized (group) {					
					group.add(receiver);
				}
			}
		}
	}
	
	public static <T> void unRegisterReceiver(T receiver) {
		Map<String, Group> groupMap = GroupManager.getGroups(receiver.getClass());
		synchronized (groupMap) {			
			for(Group group : groupMap.values()) {
				synchronized (group) {					
					group.remove(receiver);
				}
			}
		}
	}
	
	public static abstract class PushActionHelper<T> {
		
		private Set<String> pushActionsGroups;
		private T pusher;
		private boolean pushActions;
		
		public PushActionHelper(T pusher, Option pushActions) {
			this.pusher = pusher;
			this.pushActions = pushActions.isSelected();
			if(this.pushActions) {
				this.pushActionsGroups = pushActions.gett("groupNames");
			}
		}
		
		@SuppressWarnings("unchecked")
		public void push() {
			if(!pushActions) {
				return;
			}
			for(String groupName : pushActionsGroups) {
				Group group = GroupManager.getGroup((Class<Object>) pusher.getClass(), groupName);
				synchronized (group) {					
					for(Object object : group) {
						if(object!=pusher) {
							if(pusher.getClass().isAssignableFrom(object.getClass())) {
								action((T)object);
							}
						}
					}
				}
			}
		}
		
		public abstract void action(T object);
	}
}
