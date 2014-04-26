package com.schedule;

import java.util.SortedMap;

/** A runnable process that can process by {@link Processor}. */
public interface IProcess {

	/** Returns how long the schedule requires to process. */
	public abstract int getDuration();

	public abstract boolean isFinish();

	public abstract void process(Processor processor, int time);

	public abstract int getRemainingTime();

	public abstract boolean isStarted();

	public abstract int getResponseTime();

	public abstract int getStartingTime();

	public abstract int getWaitingTime();

	/** Returns process record that map key(time) to value(processor) */
	public abstract SortedMap<Integer, Processor> getProcessRecordMap();
	
}