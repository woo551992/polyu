package com.schedule.scheduler;

import static com.schedule.util.Preconditions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.schedule.ArrivedTask;
import com.schedule.Processor;
import com.schedule.Schedule;
import com.schedule.TaskInfo;

public class Statistics {
	private final int endTime;
	private final SortedSet<ArrivedTask> sortedTasks;
	private final Calculation waitingTimes;
	private final Calculation responseTimes;
	private final Calculation turnaroundTimes;
	private final SortedMap<? extends TaskInfo, ? extends List<? extends ProcessRange<Void>>> taskGanttChart;
	private final SortedMap<? extends Processor, ? extends List<? extends ProcessRange<? extends TaskInfo>>> processorGanttChart;

	public Statistics(int endTime, Collection<ArrivedTask> tasks, Collection<Processor> processors) {
		this.endTime = endTime;
		this.sortedTasks = sortTasks(tasks);
		
		waitingTimes = new Calculation(sortedTasks) {
			@Override
			protected int getValue(ArrivedTask task) {
				return task.getWaitingTime();
			}
		};
		responseTimes = new Calculation(sortedTasks) {
			@Override
			protected int getValue(ArrivedTask task) {
				return task.getResponseTime();
			}
		};
		turnaroundTimes = new Calculation(sortedTasks) {
			@Override
			protected int getValue(ArrivedTask task) {
				return task.getWaitingTime() + task.getDuration();
			}
		};
		taskGanttChart = buildTaskGanttChart(sortedTasks);
		processorGanttChart = buildProcessorGanttChart(processors);
	}
	
	private SortedSet<ArrivedTask> sortTasks(Collection<ArrivedTask> tasks) {
		TreeSet<ArrivedTask> sort = new TreeSet<ArrivedTask>(Comparators.ArrivedTasks.orderById());
		sort.addAll(tasks);
		return Collections.unmodifiableSortedSet(sort);
	}

	private SortedMap<? extends Processor, ? extends List<? extends ProcessRange<? extends TaskInfo>>> buildProcessorGanttChart(
			Collection<Processor> processors) {
		final TreeMap<Processor, List<ProcessRange<TaskInfo>>> map = new TreeMap<Processor, List<ProcessRange<TaskInfo>>>(Comparators.Processors.orderById());
		
		for (Processor p : processors) {
			ArrayList<ProcessRange<TaskInfo>> allRanges = new ArrayList<ProcessRange<TaskInfo>>();
			
			ProcessRange<TaskInfo> range = null;
			
			SortedMap<Integer,Schedule> processRecordMap = p.getProcessRecordMap();
			int start = processRecordMap.firstKey();
			int end = processRecordMap.lastKey();

			for (int i = start; i <= end; i++) {
				Schedule schedule = processRecordMap.get(i);
				boolean isProcessing = schedule != null;
				
				if (isProcessing) {
					if (range == null || 
							range.tag != schedule.getTask().getTaskInfo()	// different task, split it
							) {
						allRanges.add(range = new ProcessRange<TaskInfo>(schedule.getTask().getTaskInfo()));
						range.start = i;
					}
					range.end = i + 1;
				} else {
					range = null;
				}
			}
			
			map.put(p, Collections.unmodifiableList(allRanges));
		}
		return Collections.unmodifiableSortedMap(map);
	}

	private SortedMap<? extends TaskInfo, ? extends List<? extends ProcessRange<Void>>> buildTaskGanttChart(
			Collection<ArrivedTask> tasks) {
		final TreeMap<TaskInfo, List<ProcessRange<Void>>> map = new TreeMap<TaskInfo, List<ProcessRange<Void>>>(Comparators.TaskInfos.orderById());
		
		for (ArrivedTask task : tasks) {
			TaskInfo taskInfo = task.getTaskInfo();
			ArrayList<ProcessRange<Void>> allRanges = new ArrayList<ProcessRange<Void>>();
			
			ProcessRange<Void> range = null;
			
			// transform processRecordMap [0=[P0],1=[P0],2=null,3=[P1]] to [0-2,3-4]
			SortedMap<Integer,Processor> processRecordMap = task.getProcessRecordMap();
			// simply iterate it by time sequentially, not care performance
			int start = processRecordMap.firstKey();
			int end = processRecordMap.lastKey();
			
			for (int i = start; i <= end; i++) {
				boolean isProcessing = processRecordMap.get(i) != null;
				
				if (isProcessing) {
					if (range == null) {
						range = new ProcessRange<Void>(null);
						range.start = i;
					}
					range.end = i + 1;
				} else {
					if (range != null) {
						allRanges.add(range);
						range = null;
					}
				}
			}
			
			map.put(taskInfo, Collections.unmodifiableList(allRanges));
		}
		return Collections.unmodifiableSortedMap(map);
	}


	public class ProcessRange<TAG> {
		private int start;
		private int end;
		private final TAG tag;
		
		ProcessRange(TAG tag) {
			this.tag = tag;
		}

		public int getStart() {
			return start;
		}
		
		public int getEnd() {
			return end;
		}
		
		@Override
		public String toString() {
			return start + "-" + end;
		}
		
		public TAG getTag() {
			return tag;
		}
	}
	
	public int getEndTime() {
		return endTime;
	}
	
	/** Returns all tasks with process record, sorted by id. */
	public SortedSet<ArrivedTask> getTasks() {
		return sortedTasks;
	}
	
	public Calculation getWaitingTimes() {
		return waitingTimes;
	}
	
	public Calculation getResponseTimes() {
		return responseTimes;
	}
	
	public Calculation getTurnaroundTimes() {
		return turnaroundTimes;
	}
	
	/** Returns a map which ordered by task id, each value represent a list of duration that the task was processed. */
	public SortedMap<? extends TaskInfo, ? extends List<? extends ProcessRange<Void>>> getTaskGanttChart() {
		return taskGanttChart;
	}
	
	/** Returns a map which ordered by processor id, each value represent a list of task with duration that was processed. */
	public SortedMap<? extends Processor, ? extends List<? extends ProcessRange<? extends TaskInfo>>> getProcessorGanttChart() {
		return processorGanttChart;
	}
	
	public abstract class Calculation implements Iterable<TaskValue> {
		
		private final ArrayList<TaskValue> taskValues = new ArrayList<TaskValue>();
		private final double average;
		private final int total;
		
		public Calculation(Collection<ArrivedTask> tasks) {
			int total = 0;
			for (ArrivedTask task : tasks) {
				int value = getValue(task);
				total += value;
				taskValues.add(new TaskValue(task.getTaskInfo(), value));
			}
			this.total = total;
			average = (double) total / tasks.size();
		}
		
		protected abstract int getValue(ArrivedTask task);

		public int getTotal() {
			return total;
		}
		
		public double getAverage() {
			return average;
		}
		
		@Override
		public Iterator<TaskValue> iterator() {
			return taskValues.iterator();
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (TaskValue taskValue : this) {
				sb.append(taskValue.getTaskId() + "\t" + taskValue.getValue() + "\n");
			}
			sb.append("=\t" + getTotal() + "\n");
			sb.append("avg=\t" + getAverage());
			return sb.toString();
		}
		
	}
	
	public class TaskValue {
		private final TaskInfo taskInfo;
		private final int value;
					
		public TaskValue(TaskInfo taskInfo, int value) {
			this.taskInfo = checkNotNull(taskInfo);
			this.value = value;
		}

		public int getTaskId() {
			return taskInfo.taskId;
		}
		
		public TaskInfo getTaskInfo() {
			return taskInfo;
		}
		
		public int getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return "[" + getTaskId() + "=" + getValue() + "]";
		}
		
	}
	
}