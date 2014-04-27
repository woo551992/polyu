package com.schedule;

import static com.schedule.util.Preconditions.checkArgument;
import static com.schedule.util.Preconditions.checkNotNull;

import com.schedule.scheduler.MFQScheduler.IQueue;

public abstract class Schedule extends AbsProcess {
	
	private final ArrivedTask task;
	private final IQueue queue;

	public Schedule(ArrivedTask task, IQueue queue, int initialTime, int duration) {
		super(initialTime, duration, task);
		checkArgument(duration <= task.getRemainingTime(), "schedule duration > task's remaining time");
		this.task = task;
		this.queue = checkNotNull(queue);
	}

	public ArrivedTask getTask() {
		return task;
	}

	/** Returns the queue that this schedule belongs to. */
	public IQueue getQueue() {
		return queue;
	}
	
}