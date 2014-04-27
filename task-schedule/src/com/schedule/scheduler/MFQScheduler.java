package com.schedule.scheduler;

import static com.schedule.util.Preconditions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import com.schedule.ArrivedTask;
import com.schedule.IQueue;
import com.schedule.Processor;
import com.schedule.Schedule;
import com.schedule.TaskInfo;
import com.schedule.util.Log;

public class MFQScheduler {
	
	private static final String TAG = "MFQScheduler";

	private static class TestQueue extends FifoQueue {

		@Override
		public void enqueue(ArrivedTask task, int time) {
		}
	}
	
	public static void main(String[] args) throws IOException {
		Log.setDebugEnabled(TAG, true);
//		if (Boolean.FALSE)
		{
			MFQScheduler scheduler = new MFQScheduler();
			scheduler.setProcessorNum(2);
			scheduler.addFutureTasks(TaskInfo.defaultDataSet());
//			scheduler.addFutureTasks(Collections.singleton(new TaskInfo(0, 0, 1, 1)));
//			scheduler.addFutureTasks(Collections.singleton(new TaskInfo(1, 1, 5, 1)));
//			scheduler.addFutureTasks(Collections.singleton(new TaskInfo(2, 10, 20, 1)));
			int finishTime = scheduler.execute();
			for (Processor processor : scheduler.getProcessors()) {
				System.out.println(processor);
				SortedMap<Integer,Schedule> processRecordMap = processor.getProcessRecordMap();
				Set<Entry<Integer,Schedule>> entrySet = processRecordMap.entrySet();
				for (Map.Entry<Integer, Schedule> entry : entrySet) {
					System.out.println(entry.getKey() + "\t" + entry.getValue().getTask().getTaskInfo().taskId);
				}
			}
			System.out.println(finishTime);
		}
		if (Boolean.TRUE)
			return;
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
	
	/** The priority range is from {@value #MIN_PRIORITY} to {@value #MAX_PRIORITY} */
	private static final int MIN_PRIORITY = 1, MAX_PRIORITY = 9;
	
	/** When scheudle's idle time greater {@value #MAX_IDLE_TIME}, the schedule aging its task to a higher priority queue. */
	private static final int MAX_IDLE_TIME = 15;
	
	/** Collections of future that not started yet, ordered by {@link TaskInfo#startingTime} */
	private LinkedList<TaskInfo> sortedFutureTasks = new LinkedList<TaskInfo>();
	private List<Processor> processors;

	private ArrayList<ArrivedTask> inCompleteTasks = new ArrayList<ArrivedTask>();
	private ArrayList<ArrivedTask> completedTasks = new ArrayList<ArrivedTask>();
	private ArrayList<Schedule> inCompleteSchedules = new ArrayList<Schedule>();
	
	/** The major scheduling queues. */
	private final ArrayList<QueueEntry> queueEntries = createQueues();
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
		sortedFutureTasks.addAll(allTasks);
		Collections.sort(sortedFutureTasks, Comparators.TaskInfos.orderByStartingTime());
		checkIdDuplicate(sortedFutureTasks);
	}
	
	private static void checkIdDuplicate(Collection<TaskInfo> taskInfos) {
		HashSet<Integer> hashSet = new HashSet<Integer>();
		for (TaskInfo taskInfo : taskInfos) {
			int taskId = taskInfo.taskId;
			checkState(!hashSet.contains(taskId), "duplicate task id " + taskId);
			hashSet.add(taskId);
		}
	}
	
	public void setProcessorNum(int num) {
		checkArgument(num > 0);
		processors = Processor.createProcessors(num);
		// common ready queue max size = processor count
		commonReadyQueue = new CommonReadyQueue(num);
	}
	
	public List<Processor> getProcessors() {
		return Collections.unmodifiableList(processors);
	}
	
	public int execute() {
		int curTime = 0;
		
		while (!sortedFutureTasks.isEmpty() || !inCompleteTasks.isEmpty()) {
			Log.d(TAG, "time=" + curTime);
			HashSet<ArrivedTask> arrivedTasks = new HashSet<ArrivedTask>();
			while (!sortedFutureTasks.isEmpty()) {
				if (sortedFutureTasks.peek().startingTime != curTime) {
					// no task arrive at the moment
					break;
				}
				arrivedTasks.add(new ArrivedTaskImpl(sortedFutureTasks.poll()));
			}
			
			if (!arrivedTasks.isEmpty()) {
				arriveTasks(arrivedTasks, curTime);
				// now, all tasks is scheduled on queues
			}
			
			// add schedule to common ready queue
			for (Schedule nextProcessSchedule; 
					!commonReadyQueue.isFull() && (nextProcessSchedule = nextSchedule()) != null; ) {
				commonReadyQueue.enqueue(nextProcessSchedule);
			}
			
			
			HashSet<Schedule> idleSchedules = new HashSet<Schedule>(inCompleteSchedules);
			
			// let processors pick schedule from the common ready queue
			for (Processor p : processors) {
				p.runScheduleInQueue(commonReadyQueue, curTime);
				idleSchedules.remove(p.getProcessingSchedule());
			}
			
			// notify all schedules are idle, except the processors' current job
			for (Schedule schedule : idleSchedules) {
				checkState(!schedule.isFinish());
				schedule.idle(curTime);
			}
			
			curTime++;
		}
		return curTime;
	}

	/**
	 * Called when there is some task arrive at the moment.
	 * Decide which queue to arrive for a task
	 * @param tasks - arrive tasks, never empty
	 * @param time - current time
	 */
	protected void arriveTasks(Set<ArrivedTask> tasks, int time) {
		// order the tasks by priority
		ArrayList<ArrivedTask> orderedTasks = new ArrayList<ArrivedTask>(tasks);
		Collections.sort(orderedTasks, Comparators.ArrivedTasks.orderByPriority());
		
		// choose queue to arrive base on its priority
		for (ArrivedTask arrivedTask : orderedTasks) {
			IQueue queue = findQueueByPriority(arrivedTask.getTaskInfo().priority);
			checkNotNull(queue, "no queue found for priorty " + arrivedTask.getTaskInfo().priority);
			
			Log.d(TAG, "arrive\t- task=" + arrivedTask.getTaskInfo().toString() + " queue=" + queueEntries.indexOf(findQueueEntry(queue)));
			queue.enqueue(arrivedTask, time);
		}
	}
	
	/** Returns and pull the next schedule for processing, or null if there is no schedule on queues. */
	protected Schedule nextSchedule() {
		// get schedules to run from queues, higher priority -> lower priority
		for (QueueEntry queueEntry : queueEntries) {
			IQueue queue = queueEntry.queue;
			if (!queue.isEmpty()) {
				Schedule schedule = queue.dequeue();
				Log.d(TAG, "gotoCRQ\t- task=" + schedule.getTask().getTaskInfo().taskId + " queue=" + queueEntries.indexOf(findQueueEntry(queue)) + " size=" + queue.size());
				return schedule;
			}
		}
		return null;
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
		int index = queueEntries.indexOf(findQueueEntry(queue));
		checkArgument(index >= 0);
		index++;
		return queueEntries.size() > index ? queueEntries.get(index).queue : null;
	}
	
	/** Returns the higher priority queue, or null if {@code queue} is the highest. */
	private IQueue getUpperQueue(IQueue queue) {
		int index = queueEntries.indexOf(findQueueEntry(queue));
		checkArgument(index >= 0);
		index--;
		return index >= 0 ? queueEntries.get(index).queue : null;
	}
	
	private QueueEntry findQueueEntry(IQueue queue) {
		for (int i = 0; i < queueEntries.size(); i++) {
			QueueEntry queueEntry = queueEntries.get(i);
			if (queueEntry.queue == queue) {
				return queueEntry;
			}
		}
		return null;
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

	private class ArrivedTaskImpl extends ArrivedTask {

		public ArrivedTaskImpl(TaskInfo taskInfo) {
			super(taskInfo);
			inCompleteTasks.add(this);
		}

		@Override
		protected void onFinish(Processor processor, int time) {
			Log.d(TAG, "finish\t- task=" + getTaskInfo().taskId);
			inCompleteTasks.remove(this);
			completedTasks.add(this);
		}
		
	}
	
	private class ScheduleImpl extends Schedule {

		public ScheduleImpl(ArrivedTask task, IQueue queue, int initialTime,
				int duration) {
			super(task, queue, initialTime, duration);
			inCompleteSchedules.add(this);
		}

		@Override
		protected void onFinish(Processor processor, int time) {
			inCompleteSchedules.remove(this);
			if (getTask().isFinish()) {
				// nothing to do when task is end
				return;
			}
			IQueue curQueue = getQueue();
			IQueue destQueue = getLowerQueue(curQueue);
			checkNotNull(destQueue, "no lower priorty queue for downgrade");
			
			// transfer to a lower priority queue
			checkState(!curQueue.contains(this));
			Log.d(TAG, "down\t- task=" + getTask().getTaskInfo().taskId + " from=" + queueEntries.indexOf(findQueueEntry(curQueue)) + " to=" + queueEntries.indexOf(findQueueEntry(destQueue)));
			destQueue.enqueue(getTask(), time);
		}
		
		@Override
		protected void onIdle(int time) {
			if (getWaitingTime() <= MAX_IDLE_TIME || 
					commonReadyQueue.contains(this)	// no need to aging if this is on common ready queue
					) {
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
			Log.d(TAG, "up\t- task=" + getTask().getTaskInfo().taskId + " from=" + queueEntries.indexOf(findQueueEntry(curQueue)) + " to=" + queueEntries.indexOf(findQueueEntry(destQueue)));
			destQueue.enqueue(getTask(), time);
			inCompleteSchedules.remove(this);	// this schedule is trashed since the destQueue reschedule the task
		}
		
	}
	

}
