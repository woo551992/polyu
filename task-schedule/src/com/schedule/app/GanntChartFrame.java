package com.schedule.app;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.text.DecimalFormat;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.IntervalCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.gantt.TaskSeriesCollection;

import com.schedule.jfreechart.TaskFactory;
import com.schedule.scheduler.Statistics;

public class GanntChartFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GanntChartFrame frame = new GanntChartFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GanntChartFrame() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
	}
	
	public GanntChartFrame(Statistics stats, String title) {
		this();
		ChartPanel chartPanel = new ChartPanel(createChart(stats, title));
		add(chartPanel);
	}
	
	private JFreeChart createChart(Statistics stats, String title) {
		TaskSeriesCollection dataset = TaskFactory.createSchedulingDataset(stats);
		String categoryAxisLabel = "Tasks";
		String dateAxisLabel = "";
		JFreeChart ganttChart = ChartFactory.createGanttChart(title, categoryAxisLabel, dateAxisLabel, dataset);
		
		// configure
		CategoryPlot plot = ganttChart.getCategoryPlot();
		plot.setRangeAxis(new NumberAxis());	// use number for time unit
		plot.getRenderer().setBaseToolTipGenerator(new NumberToolTipGenerator());	// use number for task's tool tip
				
		return ganttChart;
	}
	
	private static class NumberToolTipGenerator extends IntervalCategoryToolTipGenerator {
		private static final long serialVersionUID = 1L;

		public NumberToolTipGenerator() {
			super("{0}, {1}: ", new DecimalFormat());
		}
		
		@Override
		public String generateToolTip(CategoryDataset dataset, int row,
				int column) {
			StringBuilder sb = new StringBuilder(super.generateToolTip(dataset, row, column));
			TaskSeriesCollection tsc = (TaskSeriesCollection) dataset;
            for (int i = 0; i < tsc.getSubIntervalCount(row, column); i++) {
                sb.append(tsc.getStartValue(row, column, i));
                sb.append("-");
                sb.append(tsc.getEndValue(row, column, i));
                sb.append(",");            	
            }
            sb.setLength(sb.length() - 1);
			return sb.toString();
		}
		
	}

}
