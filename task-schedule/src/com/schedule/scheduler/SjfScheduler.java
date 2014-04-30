package com.schedule.scheduler;

import java.util.ArrayList;
import java.util.Collections;

import com.schedule.ArrivedTask;

public class SjfScheduler extends MFQScheduler {

	@Override
	protected ArrayList<QueueEntry> createQueues() {
		ArrayList<QueueEntry> queues = new ArrayList<QueueEntry>();
		queues.add(new QueueEntry(new SRFQueue(), new PriorityRange(MIN_PRIORITY, MAX_PRIORITY)));
		return queues;
	}
	
	private class SRFQueue extends FifoQueue {

		@Override
		public void enqueue(ArrivedTask task, int time) {
			enqueue(new ScheduleImpl(task, this, time, task.getRemainingTime()));
			Collections.sort(internalQueue, Comparators.Processes.orderByRemainingTime());
		}
		
	}
	
}
