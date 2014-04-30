package com.schedule;

import static com.schedule.util.Preconditions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.schedule.util.Log;


public class Processor {
	
	public static final String TAG = "Processor";
	
	public static List<Processor> createProcessors(int count) {
		ArrayList<Processor> processors = new ArrayList<Processor>(count);
		for (int i = 0; i < count; i++) {
			processors.add(new Processor(i));
		}
		return processors;
	}
	
	private final int id;
	private Schedule processingSchedule;
	private TreeMap<Integer, Schedule> processRecordMap = new TreeMap<Integer, Schedule>();
	private int startTime = -1;
	private int endTime = -1;
	private int processTime = 0;

	public Processor(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	/**
	 * Run schedule by one unit. 
	 * Processor will pick schedule from the queue if it is idle.
	 * @param queue - provides schedules
	 * @param time - the current time
	 * @return true if the processor has pick schedule from the queue, otherwise the processor is focusing on its current process.
	 */
	public boolean runScheduleInQueue(IQueue queue, int time) {
		checkNotNull(queue);
		boolean picked = false;
		
		if (!isBusy()) {
			// pick a schedule from the queue to process
			if (queue.isEmpty())
				return false;
			processingSchedule = queue.dequeue();
			picked = true;
		}
		processSchedule(processingSchedule, time);
		return picked;
	}

	private void processSchedule(Schedule schedule, int time) {
		checkArgument(time >= 0);
		checkArgument(!processRecordMap.containsKey(time), "duplicate running time");

		Log.d(TAG, this + " - task=" + schedule.getTask().getTaskInfo().taskId + " remain=" + schedule.getRemainingTime() + " timeuse=" + (processTime + 1));
		schedule.process(this, time);
		// record
		if (startTime == -1)
			startTime = time;
		endTime = time + 1;
		processTime++;
		processRecordMap.put(time, schedule);
	}
	
	public Schedule getProcessingSchedule() {
		return processingSchedule;
	}
	
	public boolean isBusy() {
		return processingSchedule != null && !processingSchedule.isFinish();
	}
	
	public int getThroughput() {
		return processTime;
	}
	
	public int getStartTime() {
		return startTime;
	}
	
	public int getEndTime() {
		return endTime;
	}
	
	/** Returns process record that map key(time) to value(schedule) */
	public SortedMap<Integer, Schedule> getProcessRecordMap() {
		return Collections.unmodifiableSortedMap(processRecordMap);
	}
	
	@Override
	public String toString() {
		return "[P" + id + "]";
	}
	
}