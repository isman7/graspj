package eu.brede.common.io.easy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JFileChooser;

import eu.brede.common.io.Saver;
import eu.brede.common.util.ExceptionTools.OperationCanceledException;

public class EasySaver extends LinkedHashMap<EasyFileFilter,Saver<?>> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public List<EasyFileFilter> getFileFilters() {
		return new ArrayList<EasyFileFilter>(this.keySet());
	}
	
	public void save(ExtFileChooser fileChooser) throws IOException, FileNotFoundException {
		get(fileChooser.getFileFilter()).saveTo(
				fileChooser.getSelectedFile());
	}
	
	public void showSaveDialog(ExtFileChooser fileChooser) 
			throws IOException, FileNotFoundException, OperationCanceledException {
		fileChooser.resetChoosableFileFilters();
		fileChooser.addChoosableFileFilters(getFileFilters());
		int returnVal = fileChooser.showSaveDialog(fileChooser);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            save(fileChooser);
        }
		else {
			throw new OperationCanceledException();
        }
	}
}
