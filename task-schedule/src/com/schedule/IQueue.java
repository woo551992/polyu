package com.schedule;


/** A queue create Schedule for each task, then the Schedule will be pick up if it is ready to run. */
public interface IQueue extends Iterable<Schedule>, Cloneable {
	
	public abstract void enqueue(ArrivedTask task, int time);
	public abstract Schedule dequeue();
	public abstract boolean remove(Schedule schedule);
	public abstract boolean isEmpty();
	public abstract boolean contains(Schedule schedule);
	public abstract IQueue clone();
	public abstract int size();
}