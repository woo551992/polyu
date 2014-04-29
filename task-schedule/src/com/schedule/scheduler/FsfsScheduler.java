package com.schedule.scheduler;

import java.util.ArrayList;

public class FsfsScheduler extends MFQScheduler {

	@Override
	protected ArrayList<QueueEntry> createQueues() {
		ArrayList<QueueEntry> queues = new ArrayList<QueueEntry>();
		queues.add(new QueueEntry(new FcfsQueue(), new PriorityRange(MIN_PRIORITY, MAX_PRIORITY)));
		return queues;
	}
	
}
