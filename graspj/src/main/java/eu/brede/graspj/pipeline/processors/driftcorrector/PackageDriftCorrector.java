package eu.brede.graspj.pipeline.processors.driftcorrector;

import java.awt.geom.Point2D;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.optimization.DifferentiableMultivariateVectorOptimizer;
import org.apache.commons.math3.optimization.fitting.CurveFitter;
import org.apache.commons.math3.optimization.fitting.PolynomialFitter;
import org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.brede.graspj.utils.Buffers;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.pipeline.NeedsProduct;
import eu.brede.common.util.DeepCopy;
import eu.brede.graspj.configs.driftcorrection.DriftConfig;
import eu.brede.graspj.configs.rendering.RenderConfig;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.cycle.CycleWithOffset;
import eu.brede.graspj.pipeline.processors.AbstractAIProcessor;
import eu.brede.graspj.pipeline.processors.renderer.Gaussian2D;
import eu.brede.graspj.pipeline.processors.renderer.SpotRenderer;

public class PackageDriftCorrector extends AbstractAIProcessor implements
		NeedsProduct<AnalysisItem> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final static Logger logger = LoggerFactory
			.getLogger(PackageDriftCorrector.class);

	transient private AnalysisItem baseItem;

	private DriftConfig config;

	private transient DifferentiableMultivariateVectorOptimizer opti;
	private transient ArrayList<Double> observationsX;
	private transient ArrayList<Double> observationsY;
	private transient ArrayList<Double> centerFrames;

	private transient float previousShiftXnm;
	private transient float previousShiftYnm;

	public PackageDriftCorrector() {
		config = new DriftConfig();
	}

	public PackageDriftCorrector(AnalysisItem baseItem) {
		this.baseItem = baseItem;
		config = new DriftConfig();
	}
	
	private class DriftFunction extends PolynomialFunction.Parametric {
		
		private final int degree;
		
		public DriftFunction(int degree) {
			super();
			this.degree = degree;
		}
		 

		@Override
		public double[] gradient(double x, double... parameters) {
			final double[] gradient = new double[parameters.length];
			gradient[0]=0;
            double xn = x;
            for (int i = 1; i < degree+1; ++i) {
                gradient[i] = xn;
                xn *= x;
            }
            return gradient;
		}
		
		public double[] initialGuess() {
			return new double[degree+1];
		}
		
	}

	// TODO enable multiple drift corrections
	// TODO make compatible with shallow copies
	@Override
	public void process(AnalysisItem newItem) {

		if (observationsX == null) {
			observationsX = new ArrayList<>();
			observationsY = new ArrayList<>();
			centerFrames = new ArrayList<>();
		}

		int numNewFrames = newItem.getSpots().getFrameCount();

		// if this is the first item, no drift can be determined, set to 0!

		if (baseItem.getSpots().getSpotCount() == 0) {
			newItem.lock();
			newItem.getDrift().setBuffer(
					Buffers.newDirectFloatBuffer(2 * numNewFrames));
			newItem.getDrift().getBuffer().rewind();
			for (int frameNr = 0; frameNr < numNewFrames; frameNr++) {
				newItem.getDrift().getBuffer().put(0f);
				newItem.getDrift().getBuffer().put(0f);
			}
			newItem.getDrift().getBuffer().rewind();
			newItem.unlock();
			observationsX.add(0d);
			observationsY.add(0d);
			centerFrames.add(0d);
			return;
		}
		

		RenderConfig baseRenderConfig = (RenderConfig) getConfig().get(
				"renderConfig");

		float pixelSize = baseRenderConfig.getFloat("pixelSize");

		SpotRenderer baseRenderer = new Gaussian2D();
		baseRenderer.setConfig(baseRenderConfig);

		SpotRenderer newRenderer = new Gaussian2D();
		newRenderer.setConfig(DeepCopy.copy(baseRenderConfig));

		baseRenderer.process(baseItem);
		baseItem.lock();
		newRenderer.process(newItem);

		RenderingManipulator manipulator = new RenderingManipulator();

		Point2D.Float shift = manipulator.phaseCorrelate(
				baseRenderer.getRendering(), newRenderer.getRendering());

		newItem.getDrift().setBuffer(
				Buffers.newDirectFloatBuffer(2 * numNewFrames));

		float shiftXnm = shift.x * pixelSize;
		float shiftYnm = shift.y * pixelSize;

		CycleWithOffset cycle = newItem.getAcquisitionConfig().gett(
				"frameCycle");
		int frameOffset = cycle.getOffset();
		double centerFrame = frameOffset
				+ (((double) (numNewFrames)) / 2);

		
		// test here if shift to previous is bigger than something, perhaps move to config
		if (Math.abs(shiftXnm - previousShiftXnm) > 500
				|| Math.abs(shiftYnm - previousShiftYnm) > 500
				|| newItem.getSpots().getSpotCount() < 1e3) {

			logger.warn("improbable drift values, x: {}, y: {}, previous shifts x: {}, y: {}", new Object[] {shiftXnm,
					shiftYnm, previousShiftXnm, previousShiftYnm});
		} else {
			previousShiftXnm = shiftXnm;
			previousShiftYnm = shiftYnm;
			observationsX.add((double) shiftXnm);
			observationsY.add((double) shiftYnm);
			centerFrames.add(centerFrame);
		}
		
		int packageNr = newItem.getNotes().gett("packageNr");
		if(packageNr==32) {
			System.out.println();
		}
		
		
		UnivariateFunction funcX = null;
		UnivariateFunction funcY = null;
		
		if(centerFrames.size()<7) {

			opti = new LevenbergMarquardtOptimizer();
	
			CurveFitter fitterX = new CurveFitter(opti);
			CurveFitter fitterY = new CurveFitter(opti);
		
			for(int i=0;i<centerFrames.size();i++) {
				fitterX.addObservedPoint(centerFrames.get(i), observationsX.get(i));
				fitterY.addObservedPoint(centerFrames.get(i), observationsY.get(i));
			}
			
			try {
				DriftFunction driftFunction = new DriftFunction(3);
				
				funcX = new PolynomialFunction(fitterX.fit(driftFunction,
						driftFunction.initialGuess()));
				
				funcY = new PolynomialFunction(fitterY.fit(driftFunction,
						driftFunction.initialGuess()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			
			UnivariateInterpolator interpolator = new LoessInterpolator();
			
			PolynomialFitter polyFitterX = new PolynomialFitter(1, opti);
			PolynomialFitter polyFitterY = new PolynomialFitter(1, opti);
			int last = observationsX.size()-1;
			polyFitterX.addObservedPoint(centerFrames.get(last-1), observationsX.get(last-1));
			polyFitterX.addObservedPoint(centerFrames.get(last), observationsX.get(last));
			polyFitterY.addObservedPoint(centerFrames.get(last-1), observationsY.get(last-1));
			polyFitterY.addObservedPoint(centerFrames.get(last), observationsY.get(last));
			
			PolynomialFunction extrapolationX = new PolynomialFunction(polyFitterX.fit());
			PolynomialFunction extrapolationY = new PolynomialFunction(polyFitterY.fit());
			
			ArrayList<Double> tempObsX = new ArrayList<>(observationsX);
			ArrayList<Double> tempObsY = new ArrayList<>(observationsY);
			ArrayList<Double> tempFrames = new ArrayList<>(centerFrames);
			
			int lastFrame = frameOffset + numNewFrames;
			tempFrames.add((double)lastFrame);
			tempObsX.add(extrapolationX.value(lastFrame));
			tempObsY.add(extrapolationY.value(lastFrame));
			
			
			funcX = interpolator.interpolate(
					ArrayUtils.toPrimitive(tempFrames.toArray(new Double[]{})), 
					ArrayUtils.toPrimitive(tempObsX.toArray(new Double[]{})));
			
			funcY = interpolator.interpolate(
					ArrayUtils.toPrimitive(tempFrames.toArray(new Double[]{})), 
					ArrayUtils.toPrimitive(tempObsY.toArray(new Double[]{})));
		}
		
		FloatBuffer buffer = baseItem.getDrift().getBuffer(); 

		buffer.rewind();
		
		
		for (int frameNr = 0; frameNr < frameOffset; frameNr++) {
			float frameShiftX = (float) funcX.value(frameNr);
			buffer.put(frameShiftX);
			buffer.put((float) funcY.value(frameNr));
		}
		buffer.limit(buffer.position());
		buffer.rewind();
		baseItem.getDrift().setCLBuffer(null);
	
		
		baseItem.unlock();

		newItem.lock();
		buffer = newItem.getDrift().getBuffer();
		buffer.rewind();
		for (int frameNr = 0; frameNr < numNewFrames; frameNr++) {
			int funcFrameNr = frameNr + frameOffset;
			float frameShiftX = (float) funcX.value(funcFrameNr);
			buffer.put(frameShiftX);
			float frameShiftY = (float) funcY.value(funcFrameNr);
			buffer.put(frameShiftY);
		}
		buffer.limit(buffer.position());
		buffer.rewind();
		newItem.getDrift().setCLBuffer(null);
		newItem.unlock();

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

	@Override
	public void setProduct(AnalysisItem product) {
		baseItem = product;
	}

}
