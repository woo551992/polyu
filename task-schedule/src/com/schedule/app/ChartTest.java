package com.schedule.app;

import java.awt.EventQueue;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.IntervalCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;

import com.schedule.ArrivedTask;
import com.schedule.Processor;
import com.schedule.TaskInfo;
import com.schedule.jfreechart.TaskFactory;
import com.schedule.scheduler.Comparators;
import com.schedule.scheduler.MFQScheduler;
import com.schedule.scheduler.Statistics;
import com.schedule.scheduler.Statistics.ProcessRange;

public class ChartTest{


	 class MyToolTipGenerator extends IntervalCategoryToolTipGenerator {

	        public MyToolTipGenerator(String labelFormat, NumberFormat formatter) {
			super(labelFormat, formatter);
		}

			@Override
	        public String generateToolTip(CategoryDataset cds, int row, int col) {
	            final String s = super.generateToolTip(cds, row, col);
	            TaskSeriesCollection tsc = (TaskSeriesCollection) cds;
	            StringBuilder sb = new StringBuilder(s);
	            for (int i = 0; i < tsc.getSubIntervalCount(row, col); i++) {
//	                sb.append(format.format(tsc.getStartValue(row, col, i)));
//	                sb.append("-");
//	                sb.append(format.format(tsc.getEndValue(row, col, i)));
//	                sb.append(",");
	                sb.append(tsc.getStartValue(row, col, i));
	                sb.append("-");
	                sb.append(tsc.getEndValue(row, col, i));
	                sb.append(",");
	            }
	            sb.deleteCharAt(sb.length() - 1);
	            return sb.toString();
	        }
	    }

   /*
    * Create Chart
    * */
   private JFreeChart createChart() {
//       IntervalCategoryDataset xyDataset = createDataset();
//       IntervalCategoryDataset xyDataset = createProcessorViewDataset();
       IntervalCategoryDataset xyDataset = createTaskBasedDataset();
       JFreeChart jFreeChart = ChartFactory.createGanttChart("CPU Scheduling",
           "CPU Tasks", "Times(min)", xyDataset);
       CategoryPlot plot = jFreeChart.getCategoryPlot();
       plot.getRenderer().setBaseToolTipGenerator(
           new MyToolTipGenerator(
           "{0}, {1}: ", new DecimalFormat()));
//       CategoryPlot plot = jFreeChart.getCategoryPlot();
//       CategoryToolTipGenerator generator;
//       generator = new IntervalCategoryToolTipGenerator();
//	plot.getRenderer().setBaseToolTipGenerator(generator);
       
       ValueAxis axis = new NumberAxis();
       plot.setRangeAxis(axis);
       return jFreeChart;
   }

   /*
    * DataSet
    * */
   private IntervalCategoryDataset createDataset() {
       TaskSeriesCollection dataset = new TaskSeriesCollection();
       TaskSeries myTasks = new TaskSeries("Task");
       Task[] tasks = new Task[30];

       for(int i=0;i<30;i++){
       	tasks[i] =  new Task("Task"+(i+1), date(0), date(180));
       }

       tasks[0].addSubtask(new Task("a", date(0), date(9)));
       tasks[1].addSubtask(new Task("b", date(4), date(8)));
       tasks[2].addSubtask(new Task("c", date(6), date(13)));
       tasks[0].addSubtask(new Task("d", date(13), date(18)));
       /*
       t2.addSubtask(new Task("Task2-1", date(10), date(11)));
       t2.addSubtask(new Task("Task2-2", date(13), date(15)));
       t2.addSubtask(new Task("Task2-3", date(16), date(18)));
       */

       for(int i=0;i<30;i++){
       	if (tasks[i] != null){
       	    myTasks.add(tasks[i]);
       	}

       }
       dataset.add(myTasks);
       return dataset;
   }
   
   private TaskSeriesCollection createTaskBasedDataset() {
       TaskSeriesCollection dataset = new TaskSeriesCollection();
       if (Boolean.FALSE)
       {
	   Task task;
	   
	   TaskSeries p0 = new TaskSeries("p0");
	   TaskSeries p1 = new TaskSeries("p1");
	   
	   p0.add(task = new Task("t0", new SimpleTimePeriod(0, 10)));
	   task.addSubtask(new Task("", new SimpleTimePeriod(0, 1)));

	   p1.add(task = new Task("t0", new SimpleTimePeriod(0, 10)));
	   task.addSubtask(new Task("", new SimpleTimePeriod(4, 7)));
	   task.addSubtask(new Task("", new SimpleTimePeriod(7, 8)));
	   
	   p1.add(task = new Task("t1", new SimpleTimePeriod(1, 2)));
	   p1.add(task = new Task("t2", new SimpleTimePeriod(3, 4)));
	   p0.add(task = new Task("t3", new SimpleTimePeriod(2, 3)));
	   
	   dataset.add(p0);
	   dataset.add(p1);
       }
	   /*
	    * 
	    */
//       if (Boolean.FALSE)
       {
	   MFQScheduler scheduler = new MFQScheduler();
		try {
			scheduler.addFutureTasks(TaskInfo.defaultDataSet());
		} catch (IOException e) {
			e.printStackTrace();
		}
		Statistics stats = scheduler.execute();
//		
//		// init total range
//		ArrayList<Task> totalRanges = new ArrayList<Task>();
//		for (ArrivedTask taskStats : stats.getTasks()) {
//			int start = taskStats.getStartingTime();
//			int end = taskStats.getStartingTime() + taskStats.getWaitingTime() + taskStats.getDuration();
//			totalRanges.add(new Task(taskStats.getTaskInfo().getName(), new SimpleTimePeriod(start, end)));
//		}
//		
//		
//		for (Map.Entry<? extends Processor, ? extends List<? extends ProcessRange<? extends TaskInfo>>> entry : stats.getProcessorGanttChart().entrySet()) {
//			Processor processor = entry.getKey();
//			List<? extends ProcessRange<? extends TaskInfo>> allRanges = entry.getValue();
//			
//			TaskFactory factory = new TaskFactory();
//			factory.initTotalRanges(totalRanges);
//			for (ProcessRange<? extends TaskInfo> range : allRanges) {
//				TaskInfo taskInfo = range.getTag();
//				factory.addTask(taskInfo.getName(), range.getStart(), range.getEnd());
//			}
//			dataset.add(factory.transferTasksTo(new TaskSeries(processor.toString())));
//		}
		dataset = TaskFactory.createSchedulingDataset(stats);
       }
	   
       return dataset;
   }
   
   private IntervalCategoryDataset createProcessorViewDataset() {
       final TaskSeriesCollection dataset = new TaskSeriesCollection();
	   TaskSeries series;
	   Task task;
	   
	   dataset.add(series = new TaskSeries("t0"));
	   series.add(task = new Task("p0", new SimpleTimePeriod(0, 100)));
//	   task.addSubtask(task = new Task("aa", new SimpleTimePeriod(0, 10)));
	   dataset.add(series = new TaskSeries("t1"));
	   series.add(task = new Task("p0", new SimpleTimePeriod(100, 200)));
	   series.add(task = new Task("p0", new SimpleTimePeriod(300, 400)));
	   
	   dataset.add(series = new TaskSeries("t2"));
	   series.add(task = new Task("p1", new SimpleTimePeriod(0, 100)));
	   dataset.add(series = new TaskSeries("t3"));
	   series.add(task = new Task("p1", new SimpleTimePeriod(100, 200)));
	   
	   return dataset;
   }


   /*
    * Set Value by hour
    * */
   private Date date(int timesValue) {
       final Calendar calendar = Calendar.getInstance();
//       calendar.set(2014, Calendar.DECEMBER, 1, 0, timesValue, 0);
//       calendar.set(Calendar.YEAR, timesValue);
       calendar.setTimeInMillis(timesValue);
       return calendar.getTime();
   }

   /*
    * gui
    * */
   private void display() {
       JFrame f = new JFrame("OS Team Project");
       f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       f.add(new ChartPanel(createChart()));
       f.pack();
       f.setLocationRelativeTo(null);
       f.setVisible(true);
       f.setSize(1600, 800);
   }

   /*
    * Main
    *
    * */
   public static void main(String[] args) {
       EventQueue.invokeLater(new Runnable() {
           @Override
           public void run() {
               new ChartTest().display();
           }
       });
   }
}