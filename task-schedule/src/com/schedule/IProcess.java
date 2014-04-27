package com.schedule;

import java.util.SortedMap;

/** A runnable process that can process by {@link Processor}. */
public interface IProcess {

	public abstract int getDuration();

	public abstract boolean isFinish();

	public abstract void process(Processor processor, int time);

	public abstract int getRemainingTime();

	public abstract boolean isStarted();

	public abstract int getResponseTime();

	public abstract int getStartingTime();

	public abstract int getWaitingTime();

	public abstract SortedMap<Integer, Processor> getProcessRecordMap();

	public abstract void idle(int time);
	
}