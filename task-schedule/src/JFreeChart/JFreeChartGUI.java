package JFreeChart;

import java.awt.EventQueue;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.IntervalCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

public class JFreeChartGUI{


	 class MyToolTipGenerator extends IntervalCategoryToolTipGenerator {

	        DateFormat format;

	        private MyToolTipGenerator(String value, DateFormat format) {
	            super(value, format);
	            this.format = format;
	        }

	        @Override
	        public String generateToolTip(CategoryDataset cds, int row, int col) {
	            final String s = super.generateToolTip(cds, row, col);
	            TaskSeriesCollection tsc = (TaskSeriesCollection) cds;
	            StringBuilder sb = new StringBuilder(s);
	            for (int i = 0; i < tsc.getSubIntervalCount(row, col); i++) {
	                sb.append(format.format(tsc.getStartValue(row, col, i)));
	                sb.append("-");
	                sb.append(format.format(tsc.getEndValue(row, col, i)));
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
       IntervalCategoryDataset xyDataset = createDataset();
       JFreeChart jFreeChart = ChartFactory.createGanttChart("CPU Scheduling",
           "CPU Tasks", "Times(min)", xyDataset);
       CategoryPlot plot = jFreeChart.getCategoryPlot();
       plot.getRenderer().setBaseToolTipGenerator(
           new MyToolTipGenerator(
           "{0}, {1}: ", DateFormat.getTimeInstance(DateFormat.DATE_FIELD)));
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

       tasks[0].addSubtask(new Task("Task1", date(0), date(9)));
       tasks[1].addSubtask(new Task("Task2", date(4), date(8)));
       tasks[2].addSubtask(new Task("Task3", date(6), date(13)));
       tasks[0].addSubtask(new Task("Task3", date(13), date(18)));
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


   /*
    * Set Value by hour
    * */
   private Date date(int timesValue) {
       final Calendar calendar = Calendar.getInstance();
       calendar.set(2014, Calendar.DECEMBER, 1, 0, timesValue, 0);
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
               new JFreeChartGUI().display();
           }
       });
   }
}