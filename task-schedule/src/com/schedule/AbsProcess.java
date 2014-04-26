package com.schedule;

import static com.schedule.util.Preconditions.*;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class AbsProcess implements IProcess {
	
	private final int startingTime;
	private final int duration;
	private IProcess parentProcess;
	
	private int remainingTime;
	private int actualStartingTime = -1;
	private TreeMap<Integer, Processor> processRecordMap = new TreeMap<Integer, Processor>();
	
	//TODO: handle idle
	private int waitingTime = 0;
	

	/** Construct without parent process. */
	public AbsProcess(int startingTime, int duration) {
		this(startingTime, duration, null);
	}

	/**
	 * 
	 * @param startingTime - to calculate response time
	 * @param duration - process duration
	 * @param parentProcess - {@link #process(Processor, int)} will perform parent's too
	 */
	public AbsProcess(int startingTime, int duration, IProcess parentProcess) {
		checkArgument(startingTime >= 0);
		checkArgument(duration > 0);
		this.startingTime = startingTime;
		this.duration = duration;
		this.parentProcess = parentProcess;
		remainingTime = duration;
	}

	/** Returns when is this process created. */
	@Override
	public int getStartingTime() {
		return startingTime;
	}

	@Override
	public int getDuration() {
		return duration;
	}

	@Override
	public int getRemainingTime() {
		return remainingTime;
	}

	/** Returns the response time, or a negative number if the task not started yet. */
	@Override
	public int getResponseTime() {
		return actualStartingTime - startingTime;
	}

	@Override
	public boolean isFinish() {
		return remainingTime <= 0;
	}

	@Override
	public boolean isStarted() {
		return actualStartingTime >= 0;
	}

	@Override
	public void process(Processor processor, int time) {
		checkNotNull(processor);
		checkArgument(time >= 0);
		checkState(!isFinish(), "task is finished");
		// ensure running time not duplicate
		checkState(!processRecordMap.containsKey(time), "duplicate running time");
		
		// record
		processRecordMap.put(time, processor);
		// ensure running time are added sequentially
		checkState(processRecordMap.lastKey() == time, "the running time is not added sequentially");

		// actual process
		if (!isStarted()) {
			actualStartingTime = time;
		}
		remainingTime--;
		if (isFinish()) {
			onFinish(processor, time);
		}
		
		// process the parent process
		if (parentProcess != null) {
			parentProcess.process(processor, time);
		}
	}

	/**
	 * Called when this process is completed. 
	 * @param processor - the processor which process this process
	 * @param time - when do this schedule finish
	 */
	protected abstract void onFinish(Processor processor, int time);

	/** Returns process record that map key(time) to value(processor) */
	public SortedMap<Integer, Processor> getProcessRecordMap() {
		return Collections.unmodifiableSortedMap(processRecordMap);
	}

	@Override
	public int getWaitingTime() {
		return waitingTime;
	}
	
	private void doSomethingToIncrementWaitingTime() {
		checkState(!isFinish(), "task is finished");
		waitingTime++;
	}

}
