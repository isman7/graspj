package eu.brede.graspj.pipeline.producers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import eu.brede.common.config.Configurable;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.pipeline.producers.AbstractProducer;
import eu.brede.graspj.configs.acquisition.AcquisitionConfig;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.Option;
import eu.brede.graspj.gui.GraspJ;
import eu.brede.graspj.gui.views.AnalysisItemView;
import eu.brede.graspj.gui.views.View;

public class SplitExistingProducer extends AbstractProducer<AnalysisItem>
	implements Configurable, CanUseExistent, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private EnhancedConfig config;
	
	public SplitExistingProducer() {
		
		config = new EnhancedConfig() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public EnhancedConfig getRequiredDefault() {
				EnhancedConfig requiredConfig = super.getRequiredDefault();
				requiredConfig.setPrefix("SplitExistingProducerConfig.");
				
				requiredConfig.initMetaData();
				Option splitByFrame = new Option();
				splitByFrame.put("framesPerPackage", Integer.valueOf(1024));
				
				requiredConfig.put("splitByFrame", splitByFrame);
				requiredConfig.put("srcItem", new AnalysisItem());
				
				return requiredConfig;
			}
			
		};
		
		config.ensureCompliance();
	}
	
	public SplitExistingProducer(AnalysisItem srcItem) {
		this();
		setAnalysisItem(srcItem);
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		AnalysisItem srcItem = config.gett("srcItem");
		String itemName = "";
		if(srcItem!=null) {
			itemName = srcItem.getConfig().getMetaData().getName();
		}
		config.put("srcItem", itemName);
		out.defaultWriteObject();
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
//		Object itemObject = config.getProperty("srcItem");
//		
//		if(itemObject instanceof AnalysisItem) {
//			config.put("srcItem", 
//					((AnalysisItem)itemObject).getConfig().getMetaData().getName());
//		}
		
		setAnalysisItem(getItemSafe());
	}
	
	
	
//	public FramePackageProducerFromStack() { // TODO use this at all?
//		// TODO calculate & set size of queue
//		new LinkedBlockingQueue<FramePackage>();
//	}
	
	private AnalysisItem getItemSafe() {
		
		Object srcItemObject = config.get("srcItem");
		if(srcItemObject instanceof AnalysisItem) {
			return (AnalysisItem) srcItemObject;
		}
		else {
			
			HashMap<String,AnalysisItem> itemMap = new HashMap<>();
			for(GraspJ instance : GraspJ.getInstances()) {
				for(View view : instance.getViewManager().getAllViews()) {
					if(view instanceof AnalysisItemView) {
						AnalysisItem viewItem = ((AnalysisItemView) view).getAnalysisItem();
						itemMap.put(viewItem.getConfig().getMetaData().getName(),viewItem);
					}
				}
			}
			
			AnalysisItem item = itemMap.get(srcItemObject);
			if(item==null) { 
				item=new AnalysisItem();
			}
			
			config.put("srcItem", item);
			
			return item;
		}
		
		
		
	}
	
	@Override
	public Integer call() throws Exception {	
		AnalysisItem srcItem = getItemSafe();
		Option splitByFrame = getConfig().gett("splitByFrame");
		
		ArrayList<AnalysisItem> items = null;//srcItem.splitByFrame(getConfig().getInt("framesPerPackage"));
		
		if(splitByFrame.isSelected()) {
			items = srcItem.splitByFrame(splitByFrame.getInt("framesPerPackage"));
		}
		else {
			items = new ArrayList<>();
			items.add(srcItem);
		}
		
		Integer packageNr = 0;
		for(AnalysisItem item : items) {
			if(getController().isStopped()) {
				break;
			}
			while(getController().isPaused()) {
				wait();
			}
			try {
				item.getNotes().put("packageNr", packageNr);
				queue.put(item);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			packageNr++;// = item.getNotes().gett("packageNr");
		}	
		
		return packageNr;
	}

	@Override
	public EnhancedConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig config) {
		this.config = config;
		// ensure complicance?
	}

	@Override
	public void setAnalysisItem(AnalysisItem analysisItem) {
		config.put("srcItem", analysisItem);
	}
	
	public void initProduct(AnalysisItem product) {
		product.setAcquisitionConfig(getItemSafe().getAcquisitionConfig());
	}

}
