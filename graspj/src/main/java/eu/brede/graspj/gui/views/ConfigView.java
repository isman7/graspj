package eu.brede.graspj.gui.views;

import eu.brede.common.config.Configurable;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.gui.configurator.ConfigForm;

public class ConfigView extends View implements Configurable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected ConfigForm configForm;
	protected EnhancedConfig config;
//	private final AutoResizingTextArea textArea = new AutoResizingTextArea();
//	private JideScrollPane scrollPane = new JideScrollPane(textArea); 
	
	public ConfigView(EnhancedConfig config) {
//		this.config = config;
//		initPanel();
		this();
//		this.config = config;
//		configForm.setConfig(config);
		setConfig(config);
	}
	
	public ConfigView() {
//		this.configForm = new ConfigForm();
//		add(configForm);
//		initPanel();
	}
	
//	private void initPanel() {
//		this.removeAll();
//		setLayout(new VerticalLayout());
//		textArea.setMaxRows(40);
//		textArea.setRows(0);
//		textArea.append("Console output redirected...\n");
//		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
//		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
//		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//		this.add(scrollPane);
//		redirectSystemStreams();
////		this.add(acqForm.getGUI());
////		this.add(notesForm.getGUI());
//	}
	
//	private void updateTextArea(final String text) {
//	    SwingUtilities.invokeLater(new Runnable() {
//	      public void run() {
//	        textArea.append(text);
//	        getManager().getDisplayFrame().pack();
//	      }
//	    });
//	  }
//
//	  private void redirectSystemStreams() {
//	    OutputStream out = new OutputStream() {
//	      @Override
//	      public void write(int b) throws IOException {
//	        updateTextArea(String.valueOf((char) b));
//	      }
//
//	      @Override
//	      public void write(byte[] b, int off, int len) throws IOException {
//	        updateTextArea(new String(b, off, len));
//	      }
//
//	      @Override
//	      public void write(byte[] b) throws IOException {
//	        write(b, 0, b.length);
//	      }
//	    };
//
//	    System.setOut(new PrintStream(out, true));
//	    System.setErr(new PrintStream(out, true));
//	  }

	@Override
	public void runCommand(String cmd) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		refresh();
		super.resume();
	}
	
	@Override
	public void refresh() {
		if(configForm!=null) {			
			configForm.refresh();
		}
//		configForm.setConfig(config);
		super.refresh();
	}

	@Override
	public EnhancedConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig config) {
		removeAll();
		this.config = config;
		configForm = new ConfigForm(config);
//		configForm.setConfig(config);
		add(configForm);
//		revalidate();
		validate();
		repaint();
	}

}
