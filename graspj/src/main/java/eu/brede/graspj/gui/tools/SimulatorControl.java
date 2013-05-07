package eu.brede.graspj.gui.tools;

import ij.plugin.PlugIn;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;




import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JTextField;
import javax.swing.JFormattedTextField;

import org.jdesktop.swingx.VerticalLayout;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.gui.configurator.ConfigForm;


public class SimulatorControl extends JFrame implements PlugIn {

	/**
	 * Create the panel.
	 */
	private SimTask task;
	
	public static void main(String[] args) {
		new SimulatorControl().setVisible(true);
	}
	
	public SimulatorControl() {
		
		
		EnhancedConfig config = new EnhancedConfig();
		config.setProperty("srcFile", new File(""));
		config.setProperty("dstFile", new File(""));
		config.setProperty("interval", Long.valueOf(16));
		
		final ConfigForm form = new ConfigForm(config);
		
		final Timer timer = new Timer();
		
//		final JFormattedTextField periodField = 
//			new JFormattedTextField(NumberFormat.getNumberInstance());
//		periodField.setValue(new Long(16));
//		getContentPane().add(periodField, BorderLayout.CENTER);
		
		final JButton btnPause = new JButton("pause");
		btnPause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(task.isPaused()) {
					btnPause.setText("pause");
					task.setPaused(false);
				}
				else {
					btnPause.setText("resume");
					task.setPaused(true);
				}
			}
		});
		
		JButton btnStart = new JButton("start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EnhancedConfig cfg = form.getConfig();
				task = new SimTask(cfg.<File>gett("srcFile"), cfg.<File>gett("dstFile"));
//				timer.scheduleAtFixedRate(task, 100, ((Number)periodField.getValue()).longValue());
				timer.scheduleAtFixedRate(task, 100, cfg.<Long>gett("interval"));
				btnPause.setText("pause");
				btnPause.setEnabled(true);
			}
			
		});
		
		this.setLayout(new VerticalLayout());
//		getContentPane().add(btnStart, BorderLayout.NORTH);
		
		
		
		
		JButton btnStop = new JButton("stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				task.cancel();
				btnPause.setEnabled(false);
			}
		});
//		getContentPane().add(btnNewButton, BorderLayout.SOUTH);
		
		this.add(form);
		this.add(btnStart);
		this.add(btnPause);
		this.add(btnStop);
		
		pack();

	}

	@Override
	public void run(String arg) {
		main(null);
	}

}
