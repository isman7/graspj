package eu.brede.graspj.gui.configurator;

import java.util.Vector;

import ij.ImagePlus;
import ij.WindowManager;

public class ImagePlusComboBox extends ObjectComboBox<ImagePlus> {
	
	public ImagePlusComboBox(ImagePlus imp) {
		super(imp);
	}

	@Override
	protected Vector<ImagePlus> getAvailableObjects() {
		Vector<ImagePlus> imps = new Vector<>();
		int[] windowList = WindowManager.getIDList();
		if (windowList==null) {
			//IJ.noImage();
			return imps;
		}
		for (int i=0; i<windowList.length; i++) {
			ImagePlus imp = WindowManager.getImage(windowList[i]);
			if (imp!=null) {
				imps.add(imp);
			}
		}
		return imps;
	}

}
