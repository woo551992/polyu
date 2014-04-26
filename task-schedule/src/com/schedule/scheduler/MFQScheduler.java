package com.schedule.scheduler;

import static com.schedule.util.Preconditions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import com.schedule.ArrivedTask;
import com.schedule.Processor;
import com.schedule.Schedule;
import com.schedule.TaskInfo;

public class MFQScheduler {
	
	public static void main(String[] args) throws IOException {
		{
			MFQScheduler scheduler = new MFQScheduler();
			scheduler.addFutureTasks(TaskInfo.defaultDataSet());
		}
		TaskInfo taskInfo = TaskInfo.defaultDataSet().iterator().next();
		System.out.println(TaskInfo.HEADER_STRING);
		System.out.println(taskInfo);

		int sDuration = taskInfo.duration;
		/*
		 * TODO
		 * for a task with duration 9
		 * it start from 0 and finish at 8
		 * correct?
		 */
		Processor processor = new Processor(0);
		
		ArrivedTask arrivedTask = new ArrivedTask(taskInfo) {
			
			@Override
			protected void onFinish(Processor processor, int time) {
				System.out.println("Task finish at " + time);
			}
		};

		Schedule schedule = new Schedule(arrivedTask, new RoundRobinQueue(), 0, sDuration) {
			
			@Override
			protected void onFinish(Processor processor, int time) {
				System.out.println("Schedule finish at " + time);
			}
		};
		
//		if (Boolean.FALSE)
		{
			for (int i = 0; i < sDuration; i++) {
				System.out.println(schedule.getRemainingTime());
				schedule.process(processor, i);
			}
		}
		if (Boolean.FALSE)
		{
			schedule.process(processor, 5);
			System.out.println(schedule.getResponseTime());			
		}
		
		System.out.println(schedule.getProcessRecordMap());
		System.out.println(arrivedTask.getProcessRecordMap());
	}
	
	private static final Comparator<TaskInfo> TASK_INFO_ORDER_BY_STARTING_TIME = new Comparator<TaskInfo>() {

		@Override
		public int compare(TaskInfo arg0, TaskInfo arg1) {
			if (arg0.startingTime == arg1.startingTime)
				return 0;
			return arg0.startingTime < arg1.startingTime ? -1 : 1;
		}
	};
	/** The priority range is from 1 to {@value #MAX_PRIORITY} */
	private static final int MAX_PRIORITY = 9;
	
	/** Collections of future that not started yet, ordered by {@link TaskInfo#startingTime} */
	private TreeSet<TaskInfo> futureTasks = new TreeSet<TaskInfo>(TASK_INFO_ORDER_BY_STARTING_TIME);
	private List<Processor> processors;
	
	private ArrayList<ArrivedTask> inCompleteTasks = new ArrayList<ArrivedTask>(); 

	public MFQScheduler() {
		setProcessorNum(2);
	}
	
	public void addFutureTasks(Collection<TaskInfo> allTasks) {
		futureTasks.addAll(allTasks);
	}
	
	public void setProcessorNum(int num) {
		checkArgument(num > 0);
		processors = Processor.createProcessors(num);
	}
	
	public void execute() {
		int curTime = 0;
		
		while (!futureTasks.isEmpty() || !inCompleteTasks.isEmpty()) {
			do {
				if (futureTasks.first().startingTime != curTime) {
					// no task arrive at the moment
					break;
				}
				ArrivedTaskImpl arrivedTask = new ArrivedTaskImpl(futureTasks.pollFirst());
				inCompleteTasks.add(arrivedTask);
				// choose queue to arrive base on its priority
				//TODO
			} while (!futureTasks.isEmpty());
			
			//TODO: Processor handles all process records, but we need to increment idle tasks' waiting time here
		}
	}

	private static class RoundRobinQueue implements IQueue {

		@Override
		public void enqueue(ArrivedTask task) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Schedule dequeue() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isEmpty() {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	/** A queue create Schedule for each task, then the Schedule will be pick up if it is ready to run. */
	public interface IQueue {
		
		public abstract void enqueue(ArrivedTask task);
		public abstract Schedule dequeue();
		public abstract boolean isEmpty();
	}
	
	private class ArrivedTaskImpl extends ArrivedTask {

		public ArrivedTaskImpl(TaskInfo taskInfo) {
			super(taskInfo);
		}

		@Override
		protected void onFinish(Processor processor, int time) {
			inCompleteTasks.remove(this);
		}
		
	}

}
