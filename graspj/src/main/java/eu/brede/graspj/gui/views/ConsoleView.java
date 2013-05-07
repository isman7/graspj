package eu.brede.graspj.gui.views;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

import org.jdesktop.swingx.VerticalLayout;

import com.jidesoft.swing.AutoResizingTextArea;
import com.jidesoft.swing.JideScrollPane;

public class ConsoleView extends View {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final AutoResizingTextArea textArea = new AutoResizingTextArea();
	private JideScrollPane scrollPane = new JideScrollPane(textArea); 
	
	public ConsoleView() {
//		System.out.println("wtf");
		initPanel();
	}
	
	private void initPanel() {
		this.removeAll();
		setLayout(new VerticalLayout());
		textArea.setMaxRows(40);
		textArea.setRows(0);
		textArea.append("Console output redirected...\n");
		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.add(scrollPane);
		redirectSystemStreams();
//		this.add(acqForm.getGUI());
//		this.add(notesForm.getGUI());
	}
	
	private void updateTextArea(final String text) {
	    SwingUtilities.invokeLater(new Runnable() {
	      public void run() {
	        textArea.append(text);
	        getManager().getDisplayFrame().pack();
	      }
	    });
	  }

	protected void redirectSystemStreams() {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				updateTextArea(String.valueOf((char) b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextArea(new String(b, off, len));
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}
	  
	protected void restoreSystemStreams() {
		PrintStream ps = new PrintStream(new FileOutputStream(FileDescriptor.out));
		System.setOut(ps);
		System.setErr(ps);
	}

	@Override
	public void runCommand(String cmd) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		restoreSystemStreams();
		super.dispose();
	}
	
	

}
