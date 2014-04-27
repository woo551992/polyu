package com.schedule;


/** Represent a arrived task which created by a future task. Store some process record. */
public abstract class ArrivedTask extends AbsProcess {
	
	private final TaskInfo taskInfo;
	
	public ArrivedTask(TaskInfo taskInfo) {
		super(taskInfo.startingTime, taskInfo.duration);
		this.taskInfo = taskInfo;
	}

	/** Returns the initial task info which is associated with this instance. */
	public TaskInfo getTaskInfo() {
		return taskInfo;
	}
	
}