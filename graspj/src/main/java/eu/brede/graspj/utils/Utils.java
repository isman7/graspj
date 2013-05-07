package eu.brede.graspj.utils;

import java.awt.Font;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.text.DefaultCaret;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jidesoft.swing.AutoResizingTextArea;
import com.jidesoft.swing.JideScrollPane;

import eu.brede.common.config.Configurable;
import eu.brede.common.util.StringTools;
import eu.brede.graspj.configs.Global;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.gui.GraspJ;
import eu.brede.graspj.gui.views.AnalysisItemView;
import eu.brede.graspj.gui.views.View;

public class Utils {
	
	public static double JAVA_VERSION = getVersion();
	
	final static Logger logger = LoggerFactory.getLogger(Utils.class);
	
	public static Collection<AnalysisItem> getOpenAnalysisItems() {
		Vector<AnalysisItem> items = new Vector<>();
		for (GraspJ instance : GraspJ.getInstances()) {
			for (View view : instance.getViewManager().getAllViews()) {
				if (view instanceof AnalysisItemView) {
					items.add(((AnalysisItemView) view).getAnalysisItem());
				}
			}
		}
		return items;
	}

	public static String componentToString(Object component) {
		return Global.getStringfromBundle("ComponentLabels", component
				.getClass().getSimpleName());
	}

	public static String htmlify(String tooltipText) {
		return htmlify(tooltipText, 400, 100);
	}

	public static String htmlify(String text, int width,
			int maxSingleLine) {
//		tooltipText = StringUtils.replaceEach(tooltipText, new String[] { "&",
//				"\"", "<", ">" }, new String[] { "&amp;", "&quot;", "&lt;",
//				"&gt;" });
//		text = StringEscapeUtils.escapeHtml(text);
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append(css(width));
		if (text.length() > maxSingleLine) {
			sb.append("<div class=\"main\">");
		} else {
			sb.append("<div>");
		}
		sb.append(text);
		sb.append("</div></html>");
		
		return sb.toString();
	}

	private static String css(int width) {
		String css = "<style>"
				+ "h1,h2,h3,h4,h5,h6 { font-size: 100%; font-weight: bold; }"
				+ "h1  { font-size: 120%; }" + "h2  { font-size: 110%; }"
				+ ".main  { width: " + width + "px; }" + "</style>";
		return css;
	}

	public static String buildSimpleName(Configurable configurable) {
		return configurable.getConfig().getMetaData().getName("unnamed")
				+ " - "
				+ Global.getStringfromBundle("MenuLabels", configurable
						.getClass().getSimpleName());
	}
	
	public static void showError(Throwable e) {
		String messageURI = "GraspJ.genericError";
		showError(messageURI,null,e);
	}
	
	public static void showError(String messageURI,
			Map<String, String> replacements, Throwable e) {
		
		String[] titleAndText = createMessageTitleAndText(messageURI,
				replacements);

		AutoResizingTextArea textArea = new AutoResizingTextArea();
		
		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		Font font = new Font("Monospaced", Font.PLAIN, 12);
		textArea.setFont(font);
		
		textArea.setEditable(false);
		textArea.setMaxRows(30);
		textArea.setColumns(90);
		
		textArea.setText(ExceptionUtils.getStackTrace(e));
		
		JideScrollPane scrollPane = new JideScrollPane(textArea);

		JOptionPane.showMessageDialog(getFirstGraspJMainFrame(), new JComponent[] {
				new JLabel(titleAndText[1]), scrollPane }, titleAndText[0],
				getMessageType(messageURI));
		
		String exit = Global.getStringfromBundle("Messages", messageURI + ".exit", "true");
		if(Boolean.parseBoolean(exit)) {
			System.exit(1);
		}	
	}

	public static void showError(String messageURI, Throwable e) {
		showError(messageURI, null, e);
	}
	
	public static JFrame getFirstGraspJMainFrame() {
		List<GraspJ> gjs = GraspJ.getInstances();
		if(gjs.isEmpty()) {
			return null;
		}
		else {
			return gjs.get(0).getMainFrame();
		}
	}
	
//	public static void showError(String messageURI) {
//		showError(messageURI, null);
//	}
//	
//	public static void showError(String messageURI, Map<String, String> replacements) {
//		showMessage(messageURI, replacements);
//	}
	
	public static void showMessage(String messageURI,
			Map<String, String> replacements) {
		String[] titleAndText = createMessageTitleAndText(messageURI,
				replacements);
		
		JOptionPane.showMessageDialog(getFirstGraspJMainFrame(), titleAndText[1], titleAndText[0],
				getMessageType(messageURI));
		// IJ.showMessage(titleAndText[0], titleAndText[1]);
	}
	
	public static int getMessageType(String messageURI) {
		String type = Global.getStringfromBundle("Messages", messageURI + ".type", "PLAIN_MESSAGE");
		switch(type) {
			case "ERROR_MESSAGE":
				return 0;
			case "INFORMATION_MESSAGE":
				return 1;
			case "WARNING_MESSAGE":
				return 2;
			case "QUESTION_MESSAGE":
				return 3;
			case "PLAIN_MESSAGE":
				return -1;
			default:
				return -1;
		}
	}
	
	public static String[] createMessageTitleAndText(String messageURI, Map<String, String> replacements) {
		String title = Global.getStringfromBundle("Messages", messageURI + ".title");
		String htmlify = Global.getStringfromBundle("Messages", messageURI + ".htmlify", "true");
		String msg = Global.getStringfromBundle("Messages", messageURI + ".text");
		
		title = StringTools.replaceTokens(title, replacements);
		msg = StringTools.replaceTokens(msg, replacements);
		
		if(Boolean.parseBoolean(htmlify)) {
			msg = htmlify(msg);
		}		
		return new String[] {title,msg};
	}
	
	public static void showMessage(String messageURI) {
		showMessage(messageURI, null);
	}
	
	public static double getVersion() {
	    String version = System.getProperty("java.version");
	    int pos = 0;
	    int count = 0;
	    for( ; pos<version.length(); pos++) {
	        if(version.charAt(pos) == '.') count++;
	        if(count>1) break;
	    }
	    return Double.parseDouble(version.substring (0, pos));
	}
	
	public static <T> T getComponentIfExistent(Class<T> componentType, List<?> list) {
		for(Object component : list) {
			if(componentType.isAssignableFrom(component.getClass())) {
				return componentType.cast(component);
			}
		}
		return null;
	}
	
	public static <T> T tryAllocation(Callable<T> callable) {
		int numExceptions=0;
		int allocationAttempts=6;
		int waitAfter=4;
		int waitTime=100;
		while (numExceptions <= allocationAttempts) {
			if(numExceptions==allocationAttempts) {
				Utils.showMessage("GraspJ.OutOfMemory");
				return null;
			}
			try {
				T returnValue = callable.call(); 
				return returnValue;
			}
			catch (OutOfMemoryError e) {
				numExceptions++;
				if(numExceptions>=waitAfter) {
					try {
						Thread.sleep(waitTime);
					}
					catch (InterruptedException e1) {
						logger.warn("Waiting period was interrupted.");
						e1.printStackTrace();
					}
				}
				logger.warn(
						"GraspJ ran out of memory, running GC and retrying ({}/{})",
						(numExceptions + 1), allocationAttempts);
				System.gc();
			}
			catch (Exception e) {
				logger.error(e.getMessage(),e);
				showError(e);
			}
		}
		return null; // TODO return false here?
	}

}
