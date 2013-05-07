package eu.brede.common.util;

import java.util.ArrayList;

public class MathTools {
	
	public static double average(Number... numbers) {
		double average=0;
		for(Number number : numbers) {
			average += number.doubleValue();
		}
		average /= numbers.length;
		return average;
	}
	
	public static <T extends Number> Double coerce(T value, T lower, T upper) {
		return Math.max(
			Math.min(value.doubleValue(), upper.doubleValue()),
			lower.doubleValue());
	}
	
	public static double sqr(double value) {
		return Math.pow(value, 2);
	}
	
//	public static float sqr(float value) {
//		return (float) Math.pow(value, 2);
//	}
	
	public static float combineSigmas(float s1, float u1, float n1, float s2, float u2, float n2) {
		// TODO perhaps use doubles (sqr casts to float for float arg, change?)
		// --^ is old, now using double. use float instead?
		double t1 = (n1*sqr(s1)+n2*sqr(s2))/(n1+n2);
		double t2 = ((n1*n2)/sqr(n1+n2))*sqr(u1-u2);
		return (float) Math.sqrt(t1+t2);
	}
	
	public static float weightedAverage(float v1, float n1, float v2, float n2) {
		return (v1*n1+v2*n2)/(n1+n2);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Number> ArrayList<T> multiplyAllElements(ArrayList<T> list, T multiplicator) {
		for(int i=0; i<list.size(); i++) {
			T element = list.get(i);
			list.set(i, (T)(Double)(element.doubleValue()*multiplicator.doubleValue()));
		}
		return list;
	}
}
