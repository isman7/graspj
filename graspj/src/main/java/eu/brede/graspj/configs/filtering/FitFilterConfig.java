package eu.brede.graspj.configs.filtering;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.config.MetaData;
import eu.brede.graspj.datatypes.fitvariable.FitVariable;
import eu.brede.graspj.datatypes.range.RangeComparable;


public class FitFilterConfig extends FilterConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public FitFilterConfig() {
		super();
		ensureCompliance();
//		setProperty("metaData", new MetaData()); // new Range(min,max)
//		setProperty("x", new FitVariable<Range<Float>>(
////							new Range<Float>(20000f,30000f),
//							new Range<Float>(0.1f,41000f),
////							new Range<Float>(0.0f,18.0f)));
//							new Range<Float>(0.0f,25.0f)));
//		
//		setProperty("y", new FitVariable<Range<Float>>(
////							new Range<Float>(5000f,15000f),
//							new Range<Float>(0.1f,41000f),
////							new Range<Float>(0.0f,18.0f)));
//							new Range<Float>(0.0f,25.0f)));
//		
//		setProperty("z", new FitVariable<Range<Float>>(
//							new Range<Float>(-500.0f,500.0f),
//							new Range<Float>(0.0f,60.0f)));
//
//		setProperty("I", new FitVariable<Range<Float>>(
//							new Range<Float>(10.0f,100000f),
//							new Range<Float>(0.0f,1000f)));
//
//		setProperty("B", new FitVariable<Range<Float>>(
//							new Range<Float>(0.0f,700f),
//							new Range<Float>(0.0f,200f)));

	}
	
	@Override
	public EnhancedConfig getRequiredDefault() {
		EnhancedConfig requiredConfig = super.getRequiredDefault();
		requiredConfig.setPrefix("FitFilterConfig.");
		
		requiredConfig.put("metaData", new MetaData()); // new Range(min,max)
		requiredConfig.put("x", new FitVariable<RangeComparable<Float>>(
//							new Range<Float>(20000f,30000f),
							new RangeComparable<Float>(0.1f,Float.MAX_VALUE),
//							new Range<Float>(0.0f,18.0f)));
							new RangeComparable<Float>(0.0f,25.0f)));
		
		requiredConfig.put("y", new FitVariable<RangeComparable<Float>>(
//							new Range<Float>(5000f,15000f),
							new RangeComparable<Float>(0.1f,Float.MAX_VALUE),
//							new Range<Float>(0.0f,18.0f)));
							new RangeComparable<Float>(0.0f,25.0f)));
		
		requiredConfig.put("z", new FitVariable<RangeComparable<Float>>(
							new RangeComparable<Float>(-500.0f,500.0f),
							new RangeComparable<Float>(0.0f,60.0f)));

		requiredConfig.put("I", new FitVariable<RangeComparable<Float>>(
							new RangeComparable<Float>(0.1f,Float.MAX_VALUE),
							new RangeComparable<Float>(0.0f,Float.MAX_VALUE)));

		requiredConfig.put("B", new FitVariable<RangeComparable<Float>>(
							new RangeComparable<Float>(0.0f,Float.MAX_VALUE),
							new RangeComparable<Float>(0.0f,Float.MAX_VALUE)));
		
		return requiredConfig;
	}
	
	public RangeComparable<FloatBuffer> getRangeBuffers() {
		String[] varNames = new String[] {"x","y","z","I","B"};
		FloatBuffer minima = ByteBuffer.allocateDirect(
								4*2*varNames.length)
								.order(ByteOrder.nativeOrder())
								.asFloatBuffer();//ByteBuffer.allocateDirect(4*2*varNames.length).asFloatBuffer();
		FloatBuffer maxima = ByteBuffer.allocateDirect(
								4*2*varNames.length)
								.order(ByteOrder.nativeOrder())
								.asFloatBuffer();//ByteBuffer.allocateDirect(4*2*varNames.length).asFloatBuffer();
		
		
		for (int i=0; i<2; i++) {
			for (String varName : varNames) {
				@SuppressWarnings("unchecked")
				FitVariable<RangeComparable<? extends Number>> fitVariable = 
					(FitVariable<RangeComparable<? extends Number>>)(this.get(varName));
				
				if(i==0) { // first all values, then all uncertainties!
					// TODO .floatValue() is quite ugly, why problematic?
					minima.put(fitVariable.getValue().getMin().floatValue());
					maxima.put(fitVariable.getValue().getMax().floatValue());
				}
				else {
					minima.put(fitVariable.getUncertainty().getMin().floatValue());
					maxima.put(fitVariable.getUncertainty().getMax().floatValue());
				}
			}
		}
		
		minima.rewind();
		maxima.rewind();
		
		return new RangeComparable<FloatBuffer>(minima,maxima);
	}
}
