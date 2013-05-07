package eu.brede.graspj.pipeline.processors.driftcorrector;

import java.io.IOException;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.optimization.DifferentiableMultivariateVectorOptimizer;
import org.apache.commons.math3.optimization.fitting.PolynomialFitter;
import org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
//import org.apache.commons.math.stat.regression.SimpleRegression;

import eu.brede.graspj.utils.Buffers;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.configs.driftcorrection.DriftConfig;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.spot.TrailSpot;
import eu.brede.graspj.datatypes.spotcollection.TrailSpotTable;
import eu.brede.graspj.pipeline.processors.AbstractAIProcessor;
import eu.brede.graspj.pipeline.processors.renderer.TempShifter;


public class BeadDriftCorrector extends AbstractAIProcessor {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//	private DriftCorrection dc;
//	private Fit fit;
//	private Filtering filtering;
//	private CLPipe pipe;
//	private AnalysisItem newItem;
//	private SpotRenderer renderer;
	private DriftConfig config;
	
	
//	public PackageDriftCorrector(DriftCorrection dc, Fit fit, Filtering filtering, CLPipe pipe) {
//		this.dc = dc;
//		this.fit = fit;
//		this.filtering = filtering;
//		this.pipe = pipe;
//	}
	
	public BeadDriftCorrector() {
//		this.dc = dc;
		config = new DriftConfig();
//		this.newItem = newItem;
	}
	
	public BeadDriftCorrector(AnalysisItem baseItem) {
//		this.dc = dc;
		config = new DriftConfig();
//		this.newItem = newItem;
	}
	
//	public PackageDriftCorrector(AnalysisItem baseItem) {
//		this(baseItem, null);
//	}
	
	// TODO enable multiple drift corrections
	// TODO make compatible with shallow copies
	@Override
	public void process(AnalysisItem newItem) {
		
		// TODO check for baseItem set!
		
		int numNewFrames = newItem.getSpots().getFrameCount();
		DescriptiveStatistics[] statsX = new DescriptiveStatistics[numNewFrames];
		DescriptiveStatistics[] statsY = new DescriptiveStatistics[numNewFrames];
		for(int i=0;i<numNewFrames;i++) {
			statsX[i] = new DescriptiveStatistics();
			statsY[i] = new DescriptiveStatistics();
		}
		
		//TODO check existence first
		TrailSpotTable beadTrails = newItem.getNotes().gett("trailSpots");
		// those ones need to be removed from item (setMask to 0)
		int tableSize = beadTrails.size();
		int currentTrail = -1;
		TrailSpot spotZero = null;
    	
		for(int i=0;i<tableSize;i++) {
    		
    		int trailNr = -1;
    		TrailSpot spot = null;
    		if(i<tableSize) {
    			spot = (TrailSpot) beadTrails.get(i);
    			trailNr = spot.getTrailNr();
    		}
    		
    		
    		// if trail ended, analyze it & clean cache
    		if(trailNr != currentTrail) {
    			currentTrail = trailNr;
    			spotZero = spot;
    		}
    		
    		if(i<tableSize) {
	    		int frameNr = spot.frameNr();
	    		statsX[frameNr].addValue(spot.x().value()-spotZero.x().value());
	    		statsY[frameNr].addValue(spot.y().value()-spotZero.y().value());
    		}
    	}
		
		newItem.getDrift().setBuffer(Buffers.newDirectFloatBuffer(2*numNewFrames));
//		SimpleRegression regX = new SimpleRegression();
//		SimpleRegression regY = new SimpleRegression();
		
//		GaussNewtonOptimizer opti = new GaussNewtonOptimizer(false);
		DifferentiableMultivariateVectorOptimizer opti = new LevenbergMarquardtOptimizer();
		PolynomialFitter polyFitX = new PolynomialFitter(30, opti);
		PolynomialFitter polyFitY = new PolynomialFitter(30, opti);
		
		DescriptiveStatistics tempX = new DescriptiveStatistics();
		DescriptiveStatistics tempY = new DescriptiveStatistics();
		
		for(int frameNr=0;frameNr<numNewFrames;frameNr++) {
//			regX.addData(frameNr,statsX[frameNr].getMean());
//			regY.addData(frameNr,statsY[frameNr].getMean());
			System.out.println(frameNr + ";" + statsX[frameNr].getMean() + ";" + statsY[frameNr].getMean() + ";" + statsY[frameNr].getN());			
			tempX.addValue(statsX[frameNr].getMean());
			tempY.addValue(statsY[frameNr].getMean());
			
			if(frameNr % 4 == 3) {
				polyFitX.addObservedPoint(1, frameNr-1.5, tempX.getMean());
				polyFitY.addObservedPoint(1, frameNr-1.5, tempY.getMean());
				tempX.clear();
				tempY.clear();
			}
			
//			newItem.getDrift().getBuffer().put((float) -statsX[frameNr].getMean());
//			newItem.getDrift().getBuffer().put((float) -statsY[frameNr].getMean());
		}
		
		try {
			System.in.read();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		PolynomialFunction fitFuncX = null;
		PolynomialFunction fitFuncY = null;
		
		try {
			fitFuncX = new PolynomialFunction(polyFitX.fit());
			fitFuncY = new PolynomialFunction(polyFitY.fit());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int frameNr=0;frameNr<numNewFrames;frameNr++) {
//			System.out.println(frameNr + ";" + statsX[frameNr].getMean() + ";" + fitFuncX.value(frameNr) + ";" + statsY[frameNr].getMean() + ";" + fitFuncY.value(frameNr) + ";" + statsY[frameNr].getN());
			System.out.println(frameNr + ";" + fitFuncX.value(frameNr) + ";" + fitFuncY.value(frameNr) + ";" + statsY[frameNr].getN());
//			newItem.getDrift().getBuffer().put((float) -(regX.getIntercept()+frameNr*regX.getSlope()));
//			newItem.getDrift().getBuffer().put((float) -(regY.getIntercept()+frameNr*regY.getSlope()));
			newItem.getDrift().getBuffer().put((float) -fitFuncX.value(frameNr));
			newItem.getDrift().getBuffer().put((float) -fitFuncY.value(frameNr));
		}
		
		
		newItem.getDrift().getBuffer().rewind();
    	
    	
//		SpotManipulator spotManipulator = new SpotManipulator(pipe);
//		fit.setSpotCollection(spotManipulator.shiftFrameWise(fit.getSpotCollection(), driftBuffer));
		TempShifter shifter = new TempShifter();
		shifter.process(newItem);
		// TODO copy fit instead?
		return;
	}


	@Override
	public DriftConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig config) {
		this.config = (DriftConfig) config; // TODO convert instead of cast
	}
	
}
