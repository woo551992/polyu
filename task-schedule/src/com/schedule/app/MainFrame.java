package com.schedule.app;

import static com.schedule.util.Preconditions.checkNotNull;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.schedule.Processor;
import com.schedule.TaskInfo;
import com.schedule.scheduler.FsfsScheduler;
import com.schedule.scheduler.IScheduler;
import com.schedule.scheduler.MFQScheduler;
import com.schedule.scheduler.SjfScheduler;
import com.schedule.scheduler.Statistics;
import com.schedule.scheduler.Statistics.ProcessRange;
import com.schedule.util.Log;

public class MainFrame extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_DATASET_PATH = "./Comp307_group31.txt";
	
	private JFileChooser datasetChooser;
	private SchedulerCreator curSchedulerCreator;
	private Statistics curResult;
	private String curLog;

	private JPanel contentPane;
	private JTextField txt_path;
	private JButton btn_ganttchart;
	private JLabel lb_tot_t;
	private JLabel lb_avg_tt;
	private JLabel lb_avg_rt;
	private JLabel lb_avg_wt;
	private JButton btn_avg_wt;
	private JButton btn_avg_rt;
	private JButton btn_avg_tt;
	private JButton btn_go;
	private JButton btn_choose_file;
	private JButton btn_log;
	private JButton btn_tot_t;
	private JLabel lb_fin_t;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
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
	public MainFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 450);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JPanel panel_2 = new JPanel();
		panel.add(panel_2);
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));

		btn_choose_file = new JButton("Select Dataset");
		btn_choose_file.addActionListener(this);
		panel_2.add(btn_choose_file);

		txt_path = new JTextField();
		txt_path.setText(DEFAULT_DATASET_PATH);
		panel_2.add(txt_path);
		txt_path.setColumns(10);
		
		buildSchedulerSelections(panel, createSchedulers());

		btn_go = new JButton("Go");
		btn_go.addActionListener(this);
		panel.add(btn_go);
		panel.getRootPane().setDefaultButton(btn_go);
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_3 = new JPanel();
		panel_1.add(panel_3, BorderLayout.NORTH);
		
		btn_ganttchart = new JButton("Gantt Chart");
		btn_ganttchart.addActionListener(this);
		btn_ganttchart.setEnabled(false);
		panel_3.add(btn_ganttchart);
		
		btn_log = new JButton("Log");
		btn_log.setEnabled(false);
		btn_log.addActionListener(this);
		panel_3.add(btn_log);
		
		JPanel panel_4 = new JPanel();
		panel_1.add(panel_4, BorderLayout.CENTER);
		panel_4.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel lblFinishTime = new JLabel("Finish time:");
		panel_4.add(lblFinishTime, "2, 2");
		
		lb_fin_t = new JLabel("");
		panel_4.add(lb_fin_t, "4, 2");
		
		lb_tot_t = new JLabel("");
		panel_4.add(lb_tot_t, "4, 4");
		
		JLabel lblTotalThroughput = new JLabel("Total throughput:");
		panel_4.add(lblTotalThroughput, "2, 4");
		
		btn_tot_t = new JButton("Detail");
		btn_tot_t.addActionListener(this);
		btn_tot_t.setEnabled(false);
		panel_4.add(btn_tot_t, "6, 4");
		
		lb_avg_wt = new JLabel("");
		panel_4.add(lb_avg_wt, "4, 6");
		
		JLabel lblAverageWaitingTime = new JLabel("Average waiting time:");
		panel_4.add(lblAverageWaitingTime, "2, 6");
		
		btn_avg_wt = new JButton("Detail");
		btn_avg_wt.addActionListener(this);
		btn_avg_wt.setEnabled(false);
		panel_4.add(btn_avg_wt, "6, 6");
		
		lb_avg_rt = new JLabel("");
		panel_4.add(lb_avg_rt, "4, 8");
		
		JLabel lblAverageResponseTime = new JLabel("Average response time:");
		panel_4.add(lblAverageResponseTime, "2, 8");
		
		btn_avg_rt = new JButton("Detail");
		btn_avg_rt.addActionListener(this);
		btn_avg_rt.setEnabled(false);
		panel_4.add(btn_avg_rt, "6, 8");
		
		lb_avg_tt = new JLabel("");
		panel_4.add(lb_avg_tt, "4, 10");
		
		JLabel lblAverageTurnaroundTime = new JLabel("Average turnaround time:");
		panel_4.add(lblAverageTurnaroundTime, "2, 10");
		
		btn_avg_tt = new JButton("Detail");
		btn_avg_tt.addActionListener(this);
		btn_avg_tt.setEnabled(false);
		panel_4.add(btn_avg_tt, "6, 10");
	}
	
	private void promptChooseFile() {
		if (datasetChooser == null) {
			datasetChooser = new JFileChooser();
		}
		int result = datasetChooser.showOpenDialog(this);
		if (JFileChooser.APPROVE_OPTION == result) {
			txt_path.setText(datasetChooser.getSelectedFile().toString());
		}
	}
	
	public String getDatasetFilePath() {
		return txt_path.getText().trim();
	}
	
	private void buildSchedulerSelections(JPanel panel,
			Collection<SchedulerCreator> createSchedulers) {
		ButtonGroup group = new ButtonGroup();
		for (final SchedulerCreator creator : createSchedulers) {
			JRadioButton button = new JRadioButton();
			group.add(button);
			panel.add(button);
			button.setText(creator.schedulerName);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					curSchedulerCreator = creator;
				}
			});
		}
		// select the first radio button
		group.getElements().nextElement().doClick();
	}
	
	private void executeScheduler(SchedulerCreator creator) {
		String datasetFilePath = getDatasetFilePath();
		if (datasetFilePath.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please select dataset");
			clearResult();
			return;
		}
		List<TaskInfo> dataSet;
		try {
			dataSet = TaskInfo.decodeDataSet(datasetFilePath);
			if (dataSet.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Empty dataset");
				clearResult();
				return;
			}
			IScheduler scheduler = creator.createScheduler();
			scheduler.addFutureTasks(dataSet);	// may throw if data not valid
			
			// enable logging
			Log.setDebugEnabled(MFQScheduler.TAG, true);
			Log.setDebugEnabled(Processor.TAG, true);
			Log.startCustomPrint();
			curLog = null;
			
			Statistics stats = scheduler.execute();
			setTitle(creator.schedulerName);
			setResult(stats);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e);
			clearResult();
			return;
		}
	}
	
	void clearResult() {
		curResult = null;
		btn_ganttchart.setEnabled(false);
		
		lb_fin_t.setText("");
		lb_tot_t.setText("");
		lb_avg_wt.setText("");
		lb_avg_rt.setText("");
		lb_avg_tt.setText("");

		btn_tot_t.setEnabled(false);
		btn_avg_wt.setEnabled(false);
		btn_avg_rt.setEnabled(false);
		btn_avg_tt.setEnabled(false);

		btn_log.setEnabled(false);
		
		setTitle("");
	}
	
	void setResult(Statistics result) {
		curResult = checkNotNull(result);
		btn_ganttchart.setEnabled(true);
		
		lb_fin_t.setText(String.valueOf(result.getEndTime()));
		lb_tot_t.setText(String.valueOf(calculateTotalThroughput(result)));
		lb_avg_wt.setText(String.valueOf(result.getWaitingTimes().getAverage()));
		lb_avg_rt.setText(String.valueOf(result.getResponseTimes().getAverage()));
		lb_avg_tt.setText(String.valueOf(result.getTurnaroundTimes().getAverage()));
		
		btn_tot_t.setEnabled(true);
		btn_avg_wt.setEnabled(true);
		btn_avg_rt.setEnabled(true);
		btn_avg_tt.setEnabled(true);
		
		btn_log.setEnabled(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (btn_tot_t == src) {
			new TextDialog("Throughput", throughputString(curResult))
			.setVisible(true);
		} else if (btn_avg_wt == src) {
			new TextDialog("Waiting time", genCalString(curResult, FLAG_WAITING_TIME))
			.setVisible(true);
		} else if (btn_avg_rt == src) {
			new TextDialog("Response time", genCalString(curResult, FLAG_RESPONSE_TIME))
			.setVisible(true);
		} else if (btn_avg_tt == src) {
			new TextDialog("Turnaround time", genCalString(curResult, FLAG_TURNAROUND_TIME))
			.setVisible(true);
		} else if (btn_ganttchart == src) {
			new GanntChartFrame(curResult, curSchedulerCreator.schedulerName)
			.setVisible(true);
		} else if (btn_go == src) {
			executeScheduler(curSchedulerCreator);
		} else if (btn_choose_file == src) {
			promptChooseFile();
		} else if (btn_log == src) {
			String log = curLog != null ? curLog : (curLog = Log.endCustomPrint());
			new TextDialog("Log", log)
			.setVisible(true);
		}
	}

	private static String throughputString(Statistics stats) {
		StringBuilder sb = new StringBuilder();
		sb.append("Engineer\tstart\tend\tthroughput\n");
		for (Processor processor : stats.getProcessorGanttChart().keySet()) {
			sb.append(processor.toString() + "\t\t");
			sb.append(processor.getStartTime() + "\t");
			sb.append(processor.getEndTime() + "\t");
			sb.append(processor.getThroughput() + "\t");
			sb.append("\n");
		}
		sb.append("=" + calculateTotalThroughput(stats));
		return sb.toString();
	}
	
	private static int calculateTotalThroughput(Statistics stats) {
		int total = 0;
		for (Processor processor : stats.getProcessorGanttChart().keySet()) {
			total += processor.getThroughput();
		}
		return total;
	}

	private static int FLAG_WAITING_TIME = 0;
	private static int FLAG_RESPONSE_TIME = 1;
	private static int FLAG_TURNAROUND_TIME = 2;
	
	private static String genCalString(Statistics stats, int flag) {
		StringBuilder sb = new StringBuilder();
		int totalOfAll = 0;
		int count = 0;
		for (Entry<? extends TaskInfo, ? extends List<? extends ProcessRange<Void>>> entry : stats.getTaskGanttChart().entrySet()) {
			TaskInfo taskInfo = entry.getKey();
			List<? extends ProcessRange<Void>> allRanges = entry.getValue();
			
			sb.append(taskInfo.getName() + ": ");
			int totalOfTask = 0;
			
			if (FLAG_TURNAROUND_TIME == flag && !allRanges.isEmpty()) {
				int start = taskInfo.startingTime;
				int end = allRanges.get(allRanges.size() - 1).getEnd();
				
				totalOfTask = end - start;
				sb.append("(" + end + " - " + start + ")");
			} else {
				int lastEnd = taskInfo.startingTime;
				
				for (Iterator<? extends ProcessRange<Void>> iterator = allRanges.iterator(); iterator.hasNext();) {
					ProcessRange<Void> range = (ProcessRange<Void>) iterator.next();
					
					totalOfTask += range.getStart() - lastEnd;
					sb.append("(" + range.getStart() + " - " + lastEnd + ")");
					if (FLAG_RESPONSE_TIME == flag) {
						// response time use the first range only
						break;
					}
					
					lastEnd = range.getEnd();
					if (iterator.hasNext()) {
						sb.append(" + ");
					}
				}				
			}
			
			totalOfAll += totalOfTask;
			count++;
			sb.append(" = " + totalOfTask + "\n");
		}
		sb.append("total = " + totalOfAll + "\n");
		sb.append("avg = " + totalOfAll + " / " + count + " = " + (double) totalOfAll / count);
		return sb.toString();
	}

	protected Collection<SchedulerCreator> createSchedulers() {
		ArrayList<SchedulerCreator> creators = new ArrayList<SchedulerCreator>();
		creators.add(new SchedulerCreator("Multi-level feedback queue") {
			@Override
			protected IScheduler createScheduler() {
				return new MFQScheduler();
			}
		});
		creators.add(new SchedulerCreator("First come first serve queue") {
			@Override
			protected IScheduler createScheduler() {
				return new FsfsScheduler();
			}
		});
		creators.add(new SchedulerCreator("Shortest job first queue") {
			@Override
			protected IScheduler createScheduler() {
				return new SjfScheduler();
			}
		});
		return creators;
	}
	
	private abstract static class SchedulerCreator {
		private final String schedulerName;
		
		public SchedulerCreator(String schedulerName) {
			this.schedulerName = schedulerName;
		}
		
		protected abstract IScheduler createScheduler();
	}
	
	private static class TextDialog extends JDialog {
		private static final long serialVersionUID = 1L;

		public TextDialog(String title, Object text) {
			setAlwaysOnTop(true);
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setBounds(100, 100, 450, 750);
			
			setTitle(title);
			JTextArea textArea;
			add(new JScrollPane(textArea = new JTextArea()));
			
			textArea.setFont(new Font("Courier New", Font.BOLD, 12));
			textArea.setText(text.toString());
		}
		
	}
	

}
