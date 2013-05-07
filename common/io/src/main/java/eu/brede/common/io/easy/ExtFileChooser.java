package eu.brede.common.io.easy;

import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;


public class ExtFileChooser extends JFileChooser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public void addChoosableFileFilters(FileFilter... filters) {
		for(FileFilter filter:filters) {
			addChoosableFileFilter(filter);
		}
	}
	
	public void addChoosableFileFilters(List<EasyFileFilter> filters) {
		for(FileFilter filter:filters) {
			addChoosableFileFilter(filter);
		}
	}

}
