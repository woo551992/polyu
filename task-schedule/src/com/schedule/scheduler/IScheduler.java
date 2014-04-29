package com.schedule.scheduler;

import java.util.Collection;

import com.schedule.TaskInfo;

public interface IScheduler {

	public abstract Statistics execute();

	public abstract void addFutureTasks(Collection<TaskInfo> allTasks);

}