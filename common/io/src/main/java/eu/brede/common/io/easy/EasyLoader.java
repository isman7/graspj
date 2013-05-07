package eu.brede.common.io.easy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JFileChooser;

import eu.brede.common.io.Loader;
import eu.brede.common.util.ExceptionTools.OperationCanceledException;

public abstract class EasyLoader<T> extends LinkedHashMap<EasyFileFilter,Loader<T>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
//	private EasyLoadable<T> easyLoadable;
//	
//	public EasyLoader(EasyLoadable<T> easyLoadable) {
//		super();
//		this.easyLoadable = easyLoadable;
//	}

	public List<EasyFileFilter> getFileFilters() {
		return new ArrayList<EasyFileFilter>(this.keySet());
	}
	
	public T showLoadDialog(ExtFileChooser fileChooser) 
			throws ClassNotFoundException, IOException, OperationCanceledException {
		fileChooser.resetChoosableFileFilters();
		fileChooser.addChoosableFileFilters(getFileFilters());
		int returnVal = fileChooser.showOpenDialog(fileChooser);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            return load(fileChooser);
        }
		else {
			throw new OperationCanceledException();
        }
	}
	
	public T load(ExtFileChooser fileChooser) throws ClassNotFoundException, IOException {
		return get(fileChooser.getFileFilter()).loadFrom(fileChooser.getSelectedFile());
	}
	
	public T load(File file) throws ClassNotFoundException, IOException {
		for(Entry<EasyFileFilter,Loader<T>> entry : entrySet()) {
			if(entry.getKey().accept(file)) {
				return entry.getValue().loadFrom(file);
			}
		}
		throw new ClassNotFoundException("Unknown File Type");
	}
	
	public void easyLoad(ExtFileChooser fileChooser) throws ClassNotFoundException, IOException {
		easyLoad(load(fileChooser));
	}
	
	public void easyLoad(File file) throws ClassNotFoundException, IOException {
		easyLoad(load(file));
	}
	
	public void easyLoadDialog(ExtFileChooser fileChooser) 
			throws ClassNotFoundException, IOException, OperationCanceledException {
		easyLoad(showLoadDialog(fileChooser));
	}
	
	abstract public void easyLoad(T item);

}
