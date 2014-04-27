package com.schedule.scheduler;

import java.util.Comparator;

import com.schedule.ArrivedTask;
import com.schedule.TaskInfo;

public class Comparators {
	public static class TaskInfos {
		
		public static Comparator<TaskInfo> orderByStartingTime() {
			return TASK_INFO_ORDER_BY_STARTING_TIME;
		}
		
		public static Comparator<TaskInfo> orderByPriority() {
			return TASK_INFO_ORDER_BY_PRIORITY;
		}
		
		private static final Comparator<TaskInfo> TASK_INFO_ORDER_BY_STARTING_TIME = new Comparator<TaskInfo>() {
			@Override
			public int compare(TaskInfo o1, TaskInfo o2) {
				if (o1.startingTime == o2.startingTime)
					return 0;
				return o1.startingTime < o2.startingTime ? -1 : 1;
			}
		};
		
		private static final Comparator<TaskInfo> TASK_INFO_ORDER_BY_PRIORITY = new Comparator<TaskInfo>() {
			@Override
			public int compare(TaskInfo o1, TaskInfo o2) {
				if (o1.priority == o2.priority)
					return 0;
				return o1.priority < o2.priority ? -1 : 1;
			}
		};
		
	}
	public static class ArrivedTasks {
		
		public static Comparator<ArrivedTask> orderByPriority() {
			return TASK_ORDER_BY_PRIORITY;
		}
		
		private static final Comparator<ArrivedTask> TASK_ORDER_BY_PRIORITY = new Comparator<ArrivedTask>() {
			@Override
			public int compare(ArrivedTask o1, ArrivedTask o2) {
				return Comparators.TaskInfos.orderByPriority().compare(o1.getTaskInfo(), o2.getTaskInfo());
			}
		};
		
	}
}