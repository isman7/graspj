package eu.brede.graspj.pipeline.consumers;

import ij3d.Content;
import ij3d.Image3DUniverse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javax.media.j3d.PointArray;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

import customnode.CustomPointMesh;
import eu.brede.common.config.Configurable;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.config.MetaData;
import eu.brede.common.pipeline.consumers.AbstractConsumer;
import eu.brede.graspj.configs.Global;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.cycle.Cycle;
import eu.brede.graspj.datatypes.spot.Spot;

public class Live3DPreview extends AbstractConsumer<AnalysisItem>
		implements Configurable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private EnhancedConfig config;
	
	private HashMap<Integer,CustomPointMesh> cmMap;
	
	transient private Image3DUniverse universe = new Image3DUniverse();
	
	public Live3DPreview() {
		config = new EnhancedConfig();
		config.put("metaData", new MetaData());
		
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		universe = new Image3DUniverse();
	}

	@Override
	protected void use(AnalysisItem item) throws InterruptedException {
		System.out.println("LiveConsumerRenderer uses item");
		
		Cycle cycle = item.getAcquisitionConfig().gett("frameCycle");
		
		HashMap<Integer,ArrayList<Point3f>> meshMap = new HashMap<>();
		for(Integer color : cycle.getColors()) {
			meshMap.put(color, new ArrayList<Point3f>());
		}
				
		
//		ArrayList<Point3f> mesh = new ArrayList<>();
		for(Spot spot: item.getSpots()) {
			Integer maskEntry = (int) item.getMask().getBuffer().get(spot.spotNr());
			if(maskEntry > 0) {				
				meshMap.get(maskEntry).add(
					new Point3f(spot.x().value(), spot.y().value(), spot.z().value()));
			}
		}
		
//		if(universe==null) {
//			universe = new Image3DUniverse();
//		}
		if(cmMap==null) {
//			
			cmMap = new HashMap<>();
			javax.media.j3d.PointArray test = new PointArray(1, 1);
			test.getClass();
			for(Integer color : cycle.getColors()) {
				cmMap.put(color, new CustomPointMesh(meshMap.get(color),
						new Color3f(Global.getStdColors().get(color)),0.8f));
				Content content = 
						universe.addCustomMesh(cmMap.get(color), "Channel_" + color);
				content.setLocked(true);
				content.setTransparency(0.5f);
			}
			
			universe.show();
//			cm = new CustomPointMesh(mesh,
//				new Color3f(Global.getStdColors().get(0)),0.8f);
//			universe.addCustomMesh(cm, "myp");
//			
//			System.out.println("##############");
//			System.out.println("##############");
//			System.out.println("##############");
//			System.out.println("created");
//			System.out.println("##############");
//			System.out.println("##############");
//			System.out.println("##############");
		}
		else {
			for(Integer color : cycle.getColors()) {
				cmMap.get(color).addPoints(meshMap.get(color).toArray(new Point3f[0]));
			}
//			System.out.println("##############");
//			System.out.println("##############");
//			System.out.println("##############");
//			System.out.println("added");
//			System.out.println("##############");
//			System.out.println("##############");
//			System.out.println("##############");
		}
//		cm.add
		for(Object content : universe.getContents()) {
			System.out.println(((Content) content).getName());
		}
		
	}
	
	@Override
	public EnhancedConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig config) {
		this.config = config;
	}

	
}
