package com.schedule.app;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import javax.swing.JComboBox;
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

import com.schedule.ArrivedTask;
import com.schedule.jfreechart.TaskFactory;
import com.schedule.scheduler.Comparators;
import com.schedule.scheduler.Statistics;

public class GanntChartFrame extends JFrame implements ItemListener {
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JComboBox<SortItem> cb_sort;

	private Statistics stats;
	private String title;
	private ChartPanel lastChartPanel;


	/**
	 * Create the frame.
	 */
	public GanntChartFrame(Statistics stats, String title) {
		this.stats = stats;
		this.title = title;
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		contentPane.add(panel, BorderLayout.NORTH);
		
		cb_sort = new JComboBox<SortItem>();
		cb_sort.addItemListener(this);
		for (SortItem sortItem : createSortItems()) {
			cb_sort.addItem(sortItem);
		}
		panel.add(cb_sort);
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		Object src = e.getSource();
		if (cb_sort == src) {
			displayChart(cb_sort.getItemAt(cb_sort.getSelectedIndex()).sort);
		}
	}
	
	private void displayChart(Comparator<ArrivedTask> sort) {
		if (lastChartPanel != null) {
			contentPane.remove(lastChartPanel);
		}
		contentPane.add(lastChartPanel = new ChartPanel(createChart(stats, title, sort)), BorderLayout.CENTER);
		contentPane.updateUI();
	}

	private Collection<SortItem> createSortItems() {
		ArrayList<SortItem> items = new ArrayList<SortItem>();
		items.add(new SortItem("Sort by id", Comparators.ArrivedTasks.orderById()));
		items.add(new SortItem("Sort by priority", Comparators.ArrivedTasks.orderByPriority()));
		return items;
	}
	
	private static class SortItem {
		final String name;
		final Comparator<ArrivedTask> sort;
		public SortItem(String name, Comparator<ArrivedTask> sort) {
			this.name = name;
			this.sort = sort;
		};
		@Override
		public String toString() {
			return name;
		}
	}
	
	private static JFreeChart createChart(Statistics stats, String title, Comparator<ArrivedTask> sort) {
		TaskSeriesCollection dataset = TaskFactory.createSchedulingDataset(stats, sort);
		String categoryAxisLabel = "Tasks";
		String dateAxisLabel = "";
		JFreeChart chart = ChartFactory.createGanttChart(title, categoryAxisLabel, dateAxisLabel, dataset);
		
		// configure
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setRangeAxis(new NumberAxis());	// use number for time unit
		plot.getRenderer().setBaseToolTipGenerator(new NumberToolTipGenerator());	// use number for task's tool tip
				
		return chart;
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
