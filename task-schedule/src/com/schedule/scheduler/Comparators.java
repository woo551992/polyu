package com.schedule.scheduler;

import java.util.Comparator;

import com.schedule.ArrivedTask;
import com.schedule.IProcess;
import com.schedule.Processor;
import com.schedule.TaskInfo;

public class Comparators {
	public static class Processors {
		
		public static Comparator<Processor> orderById() {
			return PROCESSOR_ORDER_BY_ID;
		}
		
		private static final Comparator<Processor> PROCESSOR_ORDER_BY_ID = new Comparator<Processor>() {
			@Override
			public int compare(Processor o1, Processor o2) {
				if (o1.getId() == o2.getId())
					return 0;
				return o1.getId() < o2.getId() ? -1 : 1;
			}
		};
	}
	public static class TaskInfos {
		
		public static Comparator<TaskInfo> orderByStartingTime() {
			return TASK_INFO_ORDER_BY_STARTING_TIME;
		}
		
		public static Comparator<TaskInfo> orderByPriority() {
			return TASK_INFO_ORDER_BY_PRIORITY;
		}
		
		public static Comparator<TaskInfo> orderById() {
			return TASK_INFO_ORDER_BY_ID;
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
		
		private static final Comparator<TaskInfo> TASK_INFO_ORDER_BY_ID = new Comparator<TaskInfo>() {

			@Override
			public int compare(TaskInfo o1, TaskInfo o2) {
				if (o1.taskId == o2.taskId)
					return 0;
				return o1.taskId < o2.taskId ? -1 : 1;
			}
		};
		
	}
	public static class ArrivedTasks {
		
		public static Comparator<ArrivedTask> orderByPriority() {
			return TASK_ORDER_BY_PRIORITY;
		}
		
		public static Comparator<ArrivedTask> orderById() {
			return TASK_ORDER_BY_ID;
		}

		private static final Comparator<ArrivedTask> TASK_ORDER_BY_PRIORITY = newFromTaskInfo(Comparators.TaskInfos.orderByPriority());
		private static final Comparator<ArrivedTask> TASK_ORDER_BY_ID = newFromTaskInfo(Comparators.TaskInfos.orderById());
		
		private static Comparator<ArrivedTask> newFromTaskInfo(final Comparator<TaskInfo> comparator) {
			return new Comparator<ArrivedTask>() {

				@Override
				public int compare(ArrivedTask o1, ArrivedTask o2) {
					return comparator.compare(o1.getTaskInfo(), o2.getTaskInfo());
				}
			};
		}
		
	}
	public static class Processes {

		public static Comparator<IProcess> orderByRemainingTime() {
			return PROCESS_ORDER_BY_REMAINING_TIME;
		}
		
		private static final Comparator<IProcess> PROCESS_ORDER_BY_REMAINING_TIME = new Comparator<IProcess>() {
			@Override
			public int compare(IProcess o1, IProcess o2) {
				if (o1.getRemainingTime() == o2.getRemainingTime())
					return 0;
				return o1.getRemainingTime() < o2.getRemainingTime() ? -1 : 1;
			}
		};
	}
}