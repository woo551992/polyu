package com.schedule.scheduler;

import java.util.Iterator;
import java.util.LinkedList;

import com.schedule.IQueue;
import com.schedule.Schedule;

/**
 * Abstract queue provide basic function, the schedules are natural time ordered.
 */
public abstract class FifoQueue implements IQueue {
	
	protected final LinkedList<Schedule> internalQueue = new LinkedList<Schedule>();
	
	protected void enqueue(Schedule schedule) {
		internalQueue.add(schedule);
	}
	
	@Override
	public Schedule dequeue() {
		return internalQueue.poll();
	}
	
	@Override
	public boolean remove(Schedule schedule) {
		return internalQueue.remove(schedule);
	}
	
	@Override
	public boolean isEmpty() {
		return internalQueue.isEmpty();
	}
	
	@Override
	public int size() {
		return internalQueue.size();
	}
	
	@Override
	public Iterator<Schedule> iterator() {
		return internalQueue.iterator();
	}
	
	@Override
	public boolean contains(Schedule schedule) {
		return internalQueue.contains(schedule);
	}

	@Override
	public FifoQueue clone() {
		try {
			FifoQueue cloned = (FifoQueue) super.clone();
			cloned.internalQueue.addAll(this.internalQueue);
			return cloned;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError();
		}
	}
	
}