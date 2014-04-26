package com.schedule;

public interface ISchedule {

	public abstract TaskInfo getTaskInfo();
	public abstract IQueue getCurrentQueue();
	
}
