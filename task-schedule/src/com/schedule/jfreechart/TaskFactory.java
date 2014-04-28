package com.schedule.jfreechart;

import static com.schedule.util.Preconditions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;

import com.schedule.ArrivedTask;
import com.schedule.Processor;
import com.schedule.TaskInfo;
import com.schedule.scheduler.Statistics;
import com.schedule.scheduler.Statistics.ProcessRange;

/**
 * This factory is designed for handling multiple TaskSeries with obtaining task display order.
 * <PRE>
 * 1)
 * Each factory is used for one TaskSeries only
 * 2)
 * You need to know the total duration for each task, then call {@link #initTotalRanges(Collection)}.
 * Note that the display order is the same as the collection.
 * 3)
 * Now you can add sub tasks by {@link #addTask(String, long, long)}.
 * If no task is added for an entry of (2), this factory will add a empty sub task for it automatically.
 * 4)
 * Finally, call {@link #transferTasksTo(TaskSeries)}.
 * </PRE>
 *
 */
public class TaskFactory {
	
	/** Create a standard task based dataset. */
	public static TaskSeriesCollection createSchedulingDataset(Statistics stats) {
		TaskSeriesCollection dataset = new TaskSeriesCollection();
		
		// init total range
		ArrayList<Task> totalRanges = new ArrayList<Task>();
		for (ArrivedTask taskStats : stats.getTasks()) {
			int start = taskStats.getStartingTime();
			int end = taskStats.getStartingTime() + taskStats.getWaitingTime() + taskStats.getDuration();
			totalRanges.add(new Task(taskStats.getTaskInfo().getName(), new SimpleTimePeriod(start, end)));
		}

		for (Map.Entry<? extends Processor, ? extends List<? extends ProcessRange<? extends TaskInfo>>> entry : stats.getProcessorGanttChart().entrySet()) {
			Processor processor = entry.getKey();
			List<? extends ProcessRange<? extends TaskInfo>> subRanges = entry.getValue();
			
			TaskFactory factory = new TaskFactory();
			factory.initTotalRanges(totalRanges);
			
			for (ProcessRange<? extends TaskInfo> range : subRanges) {
				TaskInfo taskInfo = range.getTag();
				factory.addTask(taskInfo.getName(), range.getStart(), range.getEnd());
			}
			dataset.add(factory.transferTasksTo(new TaskSeries(processor.toString())));
		}
		
		return dataset;
	}

	private LinkedHashMap<String, Task> branchesMap = new LinkedHashMap<String, Task>();
	
	public void initTotalRanges(Collection<Task> ranges) {
		for (Task task : ranges) {
			initTotalRange(task);
		}
	}
	
	public void initTotalRange(Task range) {
		branchesMap.put(range.getDescription(), new Task(range.getDescription(), range.getDuration()));
	}
	
	public void initTotalRange(String description, long start, long end) {
		branchesMap.put(description, _newTask(description, start, end));
	}
	
	public void addTask(String description, long start, long end) {
		addTask(_newTask(description, start, end));
	}
	
	public void addTask(Task task) {
		Task branch = branchesMap.get(task.getDescription());
		checkState(branch != null, "did not call initTotalRange for the task");
		branch.addSubtask(task);
	}

	public TaskSeries transferTasksTo(TaskSeries series) {
		for (Task branch : branchesMap.values()) {
			if (branch.getSubtaskCount() == 0) {
				branch.addSubtask(_newTask(branch.getDescription(), 0, 0));
			}
			series.add(branch);
		}
		return series;
	}
	
	private static Task _newTask(String description, long start, long end) {
		return new Task(description, new SimpleTimePeriod(start, end));
	}

}