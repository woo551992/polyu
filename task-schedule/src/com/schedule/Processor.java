package com.schedule;

import static com.schedule.util.Preconditions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.schedule.scheduler.MFQScheduler.IQueue;

public class Processor {
	
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
	 */
	public void runScheduleInQueue(IQueue queue, int time) {
		checkNotNull(queue);
		
		if (processingSchedule == null || processingSchedule.isFinish()) {
			// pick a schedule from the queue to process
			if (queue.isEmpty())
				return;
			processingSchedule = queue.dequeue();
		}
		processSchedule(processingSchedule, time);
	}

	private void processSchedule(Schedule schedule, int time) {
		checkArgument(time >= 0);
		checkArgument(!processRecordMap.containsKey(time), "duplicate running time");

		schedule.process(this, time);
		// record
		processRecordMap.put(time, schedule);
	}
	
	/** Returns process record that map key(time) to value(schedule) */
	public SortedMap<Integer, Schedule> getProcessRecordMap() {
		return Collections.unmodifiableSortedMap(processRecordMap);
	}
	
}