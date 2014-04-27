package com.schedule.scheduler;

import static com.schedule.util.Preconditions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.schedule.ArrivedTask;
import com.schedule.Processor;
import com.schedule.Schedule;
import com.schedule.TaskInfo;

public class MFQScheduler {
	
	private static class TestQueue extends FifoQueue {

		@Override
		public void enqueue(ArrivedTask task, int time) {
		}
	}
	
	public static void main(String[] args) throws IOException {
		{
			MFQScheduler scheduler = new MFQScheduler();
			scheduler.addFutureTasks(TaskInfo.defaultDataSet());
		}
		TaskInfo taskInfo = TaskInfo.defaultDataSet().iterator().next();
		System.out.println(TaskInfo.HEADER_STRING);
		System.out.println(taskInfo);

		int sDuration = taskInfo.duration;
		
		Processor processor = new Processor(0);
		
		ArrivedTask arrivedTask = new ArrivedTask(taskInfo) {
			
			@Override
			protected void onFinish(Processor processor, int time) {
				System.out.println("Task finish at " + time);
			}
		};

		Schedule schedule = new Schedule(arrivedTask, new TestQueue(), 0, sDuration) {
			
			@Override
			protected void onFinish(Processor processor, int time) {
				System.out.println("Schedule finish at " + time);
			}
		};
		
		if (Boolean.FALSE)
		{
			for (int i = 0; i < sDuration; i++) {
				System.out.println("remain " + schedule.getRemainingTime());
				schedule.process(processor, i);
			}
		}
//		if (Boolean.FALSE)
		{
			int startProcessTime = 5;
			for (int i = 0; i < startProcessTime; i++) {
				schedule.idle(i);
			}
			schedule.process(processor, startProcessTime);
			System.out.println("response " + schedule.getResponseTime());
			System.out.println("waited " + schedule.getWaitingTime());
		}
		
		System.out.println(schedule.getProcessRecordMap());
		System.out.println(arrivedTask.getProcessRecordMap());
	}
	
	public static class Comparators {
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
	
	
	/** The priority range is from {@value #MIN_PRIORITY} to {@value #MAX_PRIORITY} */
	private static final int MIN_PRIORITY = 1, MAX_PRIORITY = 9;
	
	/** When scheudle's idle time greater {@value #MAX_IDLE_TIME}, the schedule aging its task to a higher priority queue. */
	private static final int MAX_IDLE_TIME = 16;
	
	/** Collections of future that not started yet, ordered by {@link TaskInfo#startingTime} */
	private TreeSet<TaskInfo> futureTasks = new TreeSet<TaskInfo>(Comparators.TaskInfos.orderByStartingTime());
	private List<Processor> processors;

	private ArrayList<ArrivedTask> inCompleteTasks = new ArrayList<ArrivedTask>();
	private ArrayList<ArrivedTask> completedTasks = new ArrayList<ArrivedTask>();
	
	/** The major scheduling queues. */
	private ArrayList<QueueEntry> queueEntries = createQueues();
	/** Buffer of task schedules. see {@link CommonReadyQueue} */
	private CommonReadyQueue commonReadyQueue;

	private static class QueueEntry {
		final IQueue queue;
		final PriorityRange priorityRange;
		public QueueEntry(IQueue queue, PriorityRange priorityRange) {
			this.queue = checkNotNull(queue);
			this.priorityRange = checkNotNull(priorityRange);
		}
	}
	private static class PriorityRange {
		final int min;
		final int max;
		public PriorityRange(int min, int max) {
			checkArgument(min <= max, "max > min");
			this.min = min;
			this.max = max;
		}
	}
	
	public MFQScheduler() {
		setProcessorNum(2);
	}
	
	private ArrayList<QueueEntry> createQueues() {
		ArrayList<QueueEntry> queues = new ArrayList<QueueEntry>();
		// hard code create 3 queues
		// the first queue schedule priority 1-3 tasks, with round robin time slice = 4
		queues.add(new QueueEntry(new RoundRobinQueue(4), new PriorityRange(MIN_PRIORITY, 3)));
		// the second queue schedule priority 4-6 tasks, with round robin time slice = 8
		queues.add(new QueueEntry(new RoundRobinQueue(8), new PriorityRange(4, 6)));
		// the last queue schedule priority 7-9 tasks, by FCFS
		queues.add(new QueueEntry(new FcfsQueue(), new PriorityRange(7, MAX_PRIORITY)));
		return queues;
	}

	public void addFutureTasks(Collection<TaskInfo> allTasks) {
		for (TaskInfo taskInfo : allTasks) {
			int priority = taskInfo.priority;
			checkArgument(priority >= MIN_PRIORITY && priority <= MAX_PRIORITY, "priority exceed the range");
		}
		futureTasks.addAll(allTasks);
	}
	
	public void setProcessorNum(int num) {
		checkArgument(num > 0);
		processors = Processor.createProcessors(num);
		commonReadyQueue = new CommonReadyQueue(num);
	}
	
	public void execute() {
		int curTime = 0;
		
		while (!futureTasks.isEmpty() || !inCompleteTasks.isEmpty()) {
			HashSet<ArrivedTask> arrivedTasks = new HashSet<ArrivedTask>();
			do {
				if (futureTasks.first().startingTime != curTime) {
					// no task arrive at the moment
					break;
				}
				ArrivedTaskImpl newArriveTask = new ArrivedTaskImpl(futureTasks.pollFirst());
				arrivedTasks.add(newArriveTask);
			} while (!futureTasks.isEmpty());
			
			if (!arrivedTasks.isEmpty()) {
				arriveTasks(arrivedTasks, curTime);
				// now, all tasks is scheduled on queues
			}
			//TODO: Processor handles all process records, but we need to increment idle tasks' waiting time here
			
			
			curTime++;
		}
	}

	/**
	 * Called when there is some task arrive at the moment.
	 * @param tasks - arrive tasks, never empty
	 * @param time - current time
	 */
	protected void arriveTasks(Set<ArrivedTask> tasks, int time) {
		// order the tasks by priority
		TreeSet<ArrivedTask> orderedTasks = new TreeSet<ArrivedTask>(Comparators.ArrivedTasks.orderByPriority());
		orderedTasks.addAll(tasks);
		
		// choose queue to arrive base on its priority
		for (ArrivedTask arrivedTask : orderedTasks) {
			IQueue queue = findQueueByPriority(arrivedTask.getTaskInfo().priority);
			checkNotNull(queue, "no queue found for priorty " + arrivedTask.getTaskInfo().priority);
			
			queue.enqueue(arrivedTask, time);
		}
	}
	
	private IQueue findQueueByPriority(int priority) {
		for (int i = 0; i < queueEntries.size(); i++) {
			QueueEntry queueEntry = queueEntries.get(i);
			PriorityRange range = queueEntry.priorityRange;
			if (priority >= range.min && priority <= range.max) {
				return queueEntry.queue;
			}
		}
		return null;
	}
	
	/** Returns the lower priority queue, or null if {@code queue} is the lowest. */
	private IQueue getLowerQueue(IQueue queue) {
		int index = queueEntries.indexOf(queue);
		checkArgument(index >= 0);
		index++;
		return queueEntries.size() > index ? queueEntries.get(index).queue : null;
	}
	
	/** Returns the higher priority queue, or null if {@code queue} is the highest. */
	private IQueue getUpperQueue(IQueue queue) {
		int index = queueEntries.indexOf(queue);
		checkArgument(index >= 0);
		index--;
		return index >= 0 ? queueEntries.get(index).queue : null;
	}

	private class RoundRobinQueue extends FifoQueue {
		
		private final int timeSlice;
		
		public RoundRobinQueue(int timeSlice) {
			this.timeSlice = timeSlice;
		}

		@Override
		public void enqueue(ArrivedTask task, int time) {
			int duration = Math.min(task.getRemainingTime(), timeSlice);
			Schedule schedule = new ScheduleImpl(task, this, time, duration);
			enqueue(schedule);			
		}
		
	}
	
	private class FcfsQueue extends FifoQueue {

		@Override
		public void enqueue(ArrivedTask task, int time) {
			enqueue(new ScheduleImpl(task, this, time, task.getRemainingTime()));
		}
		
	}
	
	/** Common ready queue is a buffer of schedules, processor will pick jobs from this queue. */
	private class CommonReadyQueue extends FifoQueue {

		final int maxSize;

		public CommonReadyQueue(int maxSize) {
			this.maxSize = maxSize;
		}
		
		/** @deprecated common ready queue not for scheduling purpose. */
		@Override @Deprecated
		public void enqueue(ArrivedTask task, int time) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void enqueue(Schedule schedule) {
			checkState(!isFull(), "already full");
			super.enqueue(schedule);
		}
		
		public boolean isFull() {
			return size() == maxSize;
		}
		
	}

	/**
	 * Abstract queue provide basic function, the schedules are natural time ordered.
	 */
	public static abstract class FifoQueue implements IQueue {
		
		private final LinkedList<Schedule> internalQueue = new LinkedList<Schedule>();
		
		protected void enqueue(Schedule schedule) {
			internalQueue.add(schedule);
		}
		
		@Override
		public Schedule dequeue() {
			return internalQueue.poll();
		}
		
		@Override
		public boolean remove(Schedule schedule) {
			return internalQueue.remove(schedule);
		}
		
		@Override
		public boolean isEmpty() {
			return internalQueue.isEmpty();
		}
		
		protected int size() {
			return internalQueue.size();
		}
		
	}
	
	/** A queue create Schedule for each task, then the Schedule will be pick up if it is ready to run. */
	public interface IQueue {
		
		public abstract void enqueue(ArrivedTask task, int time);
		public abstract Schedule dequeue();
		public abstract boolean remove(Schedule schedule);
		public abstract boolean isEmpty();
	}
	
	private class ArrivedTaskImpl extends ArrivedTask {

		public ArrivedTaskImpl(TaskInfo taskInfo) {
			super(taskInfo);
			inCompleteTasks.add(this);
		}

		@Override
		protected void onFinish(Processor processor, int time) {
			inCompleteTasks.remove(this);
			completedTasks.add(this);
		}
		
	}
	
	private class ScheduleImpl extends Schedule {

		public ScheduleImpl(ArrivedTask task, IQueue queue, int initialTime,
				int duration) {
			super(task, queue, initialTime, duration);
		}

		@Override
		protected void onFinish(Processor processor, int time) {
			if (getTask().isFinish()) {
				// nothing to do when task is end
				return;
			}
			IQueue curQueue = getQueue();
			IQueue destQueue = getLowerQueue(curQueue);
			checkNotNull(destQueue, "no lower priorty queue for downgrade");
			
			// transfer to a lower priority queue
			checkState(curQueue.remove(this));
			destQueue.enqueue(getTask(), time);
		}
		
		@Override
		protected void onIdle(int time) {
			if (getWaitingTime() <= MAX_IDLE_TIME) {
				return;
			}
			
			IQueue curQueue = getQueue();
			IQueue destQueue = getUpperQueue(curQueue);
			if (destQueue == null) {
				// does nothing, continue wait in the current queue
				return;
			}
			
			// transfer to a higher priority queue
			checkState(curQueue.remove(this));
			destQueue.enqueue(getTask(), time);
		}
		
	}
	

}
