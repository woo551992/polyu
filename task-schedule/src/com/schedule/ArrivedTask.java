package com.schedule;

import java.util.TreeSet;

/** Represent a arrived task which created by a future task. Store some process record. */
public abstract class ArrivedTask extends AbsProcess {
	
	private final TaskInfo taskInfo;
	private int remainingTime;
	private int actualStartingTime = -1;
	private TreeSet<Integer> processTimeRecordSet = new TreeSet<Integer>();
	
	public ArrivedTask(TaskInfo taskInfo) {
		super(taskInfo.startingTime, taskInfo.duration);
		this.taskInfo = taskInfo;
		remainingTime = taskInfo.duration;
	}

	/** Returns the initial task info which is associated with this instance. */
	public TaskInfo getTaskInfo() {
		return taskInfo;
	}
	
}