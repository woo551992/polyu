package com.schedule;
import static com.schedule.util.Preconditions.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class TaskInfo {
	
	public static void main(String[] args) throws IOException {
		System.out.println("TaskID, StartingTime, Duration, Priority");
		List<TaskInfo> dataSet = defaultDataSet();
		for (TaskInfo pendingTask : dataSet) {
			System.out.println(pendingTask);
		}
	}
	
	private static final String DEFAULT_PATH = "./Comp307_group31.txt";
	public static final String HEADER_STRING = "TaskID, StartingTime, Duration, Priority";
	
	public static List<TaskInfo> defaultDataSet() throws IOException {
		return decodeDataSet(DEFAULT_PATH);
	}
	
	public static List<TaskInfo> decodeDataSet(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		List<TaskInfo> tasks = new ArrayList<TaskInfo>();
		try {
			reader.readLine();	// first line is column name, skip
			for (String line; (line = reader.readLine()) != null;) {
				String[] segments = line.split(",");
				int[] values;
				TaskInfo task;
				try {
					values = toIntArray(segments);
				} catch (NumberFormatException e) {
					throw new IOException("all data should be integer", e);
				}
				try {
					task = new TaskInfo(values[0], values[1], values[2], values[3]);
				} catch (IllegalArgumentException e) {
					throw new IOException("invalid task data", e);
				}
				
				tasks.add(task);
			}
		} finally {
			reader.close();
		}
		return tasks;
	}
	
	private static int[] toIntArray(String[] src) throws NumberFormatException {
		int[] array = new int[src.length];
		for (int i = 0; i < src.length; i++) {
			String s = src[i].trim();
			array[i] = Integer.valueOf(s);
		}
		return array;
	}
	
	public final int taskId;
	public final int startingTime;
	public final int duration;
	public final int priority;
			
	public TaskInfo(int taskId, int startingTime, int duration, int priority) {
		checkArgument(taskId >= 0, "taskId < 0");
		checkArgument(startingTime >= 0, "startingTime < 0");
		checkArgument(duration >= 0, "duration < 0");
		checkArgument(priority > 0, "priority <= 0");
		this.taskId = taskId;
		this.startingTime = startingTime;
		this.duration = duration;
		this.priority = priority;
	}
	
	public String getName() {
		return "Task" + taskId;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(taskId + ", ");
		sb.append(startingTime + ", ");
		sb.append(duration + ", ");
		sb.append(priority);
		return sb.toString();
	}

}
