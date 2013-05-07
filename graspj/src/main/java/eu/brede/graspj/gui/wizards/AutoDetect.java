package eu.brede.graspj.gui.wizards;

import ij.ImagePlus;
import ij.measure.Measurements;
import ij.process.ImageStatistics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.vecmath.Point3d;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import eu.brede.graspj.datatypes.cycle.Cycle;
import eu.brede.graspj.datatypes.cycle.CycleEntry;
import eu.brede.graspj.datatypes.range.Range;
import eu.brede.graspj.datatypes.range.RangeComparable;

public class AutoDetect {

	public static <T, N extends Number & Comparable<N>> Range<Entry<T, N>> findMinMaxEntry(
			Map<T, N> map) {
		Entry<T, N> min = null;
		Entry<T, N> max = null;
		for (Entry<T, N> entry : map.entrySet()) {
			// aka if(min>entry)
			if (min == null || min.getValue().compareTo(entry.getValue()) > 0) {
				min = entry;
			}
			// aka if(max<entry)
			if (max == null || max.getValue().compareTo(entry.getValue()) < 0) {
				max = entry;
			}
		}
		return new Range<>(min, max);

	}

	public static Range<Point3d> findMinMax(ImagePlus imp, int border) {
		// x,y are coordinates, z is value
		Point3d min = new Point3d();
		min.z = Double.MAX_VALUE;

		Point3d max = new Point3d();
		max.z = Double.MIN_VALUE;

		int width = imp.getWidth();
		int height = imp.getHeight();

		for (int x = 1 + border; x <= width - border; x++) {
			for (int y = 1 + border; y <= height - border; y++) {
				double value = imp.getProcessor().get(x, y);
				if (value > max.z) {
					max.x = x;
					max.y = y;
				}
				if (value < min.z) {
					min.x = x;
					min.y = y;
				}
			}
		}

		return new Range<>(min, max);
	}

	public static Range<Point3d> findMinMax(ImagePlus imp) {
		return findMinMax(imp, 0);
	}

	public static Cycle detectCycle(
			LinkedHashMap<ImagePlus, Double> imageMeanMap, int numColors,
			int maxN) {

		// biggest entries first
		List<Double> sortedMMMs = Lists.reverse(Ordering.natural().sortedCopy(
				imageMeanMap.values()));
		
		int ignoreHighestN = (int) Math.ceil(0.1 * sortedMMMs.size());
		// remove highest N to exclude outliers TODO: perhaps start with lowest instead 
		for(int i=0;i<ignoreHighestN;i++) {
			sortedMMMs.remove(0);
		}

		ArrayList<DescriptiveStatistics> stats = new ArrayList<>();
		DescriptiveStatistics stat = new DescriptiveStatistics();
		for (Double mmm : sortedMMMs) {
			if ((stat.getN() < 2) || stat.getStandardDeviation() == 0.0) {
				stat.addValue(mmm);
				continue;
			}

			double fraction = 0.05;
			double spread = Math.max(stat.getStandardDeviation(),
					stat.getMean() * fraction);
			double diff = mmm + maxN * spread - stat.getMean();

			if (diff < 0) {
				stats.add(stat);
				stat = new DescriptiveStatistics();
			}
			stat.addValue(mmm);
		}
		stats.add(stat);

		// LinkedHashMap<ImagePlus, Double> mapCopy = new LinkedHashMap<>(
		// imageMeanMap);
		// DescriptiveStatistics activationStats = new DescriptiveStatistics();
		// for (int i = 0; i < 5; i++) {
		// Entry<ImagePlus, Double> entry = AutoDetect
		// .findMinMaxEntry(mapCopy).getMin();
		// activationStats.addValue(entry.getValue());
		// mapCopy.remove(entry.getKey());
		// }

		Cycle cycle = new Cycle();
		cycle.clear();

		DescriptiveStatistics activationStats = null;
		if(stats.size()>1) {
			activationStats = Lists.reverse(stats).get(0);
		}
		else {
			activationStats = new DescriptiveStatistics();
			activationStats.addValue(0);
		}
		// double activationMean = activationStats.getMean();
		// double activationStdDev = activationStats.getStandardDeviation();

		int currentColor = 0;

		// first previous does not enter the cycle, must be set to
		// be an activation channel to support acquisition frame as first frame
		CycleEntry previousCycleEntry = new CycleEntry(1, true);

		// if there is no activation frame, all acquisition frames are equal,
		// needed to set Cycle to "1" in taht case
		boolean cycleHasActivationFrame = false;

		// mmm = mean minus min = mean - min
		for (Double mmm : imageMeanMap.values()) {
			CycleEntry cycleEntry = null;

			// if mmm is below the maxN*StdDev range
			// double diff = mmm - (activationMean + maxN * activationStdDev);
			RangeComparable<Double> acqRange = new RangeComparable<Double>(
					Math.floor(activationStats.getMin()),
					Math.ceil(activationStats.getMax()));
			if (acqRange.contains(mmm)) {
				// this is an activation frame
				cycleHasActivationFrame = true;
				currentColor++;
				if (currentColor > numColors) {
					// we already went through a full cycle, stop here
					// return cycle;
					break;
				}
				cycleEntry = new CycleEntry(currentColor, true);
			} else {
				// this is a acquisition frame

				// if this is the first frame there probably are no activation
				// frames (e.g. continuous activation) or the first activation
				// frame was skipped. Color-channel will be 1.
				if (currentColor == 0) {
					currentColor = 1;
				}

				if (previousCycleEntry.isActivation()) {
					// this is the first acquisition frame for this color,
					// hence it belongs to this color-channel
					cycleEntry = new CycleEntry(currentColor, false);
				} else {
					// not the first acquisition frame for this color,
					// hence it goes to non-specific (9)
					cycleEntry = new CycleEntry(9, false);
				}
			}
			cycle.add(cycleEntry);
			previousCycleEntry = cycleEntry;
		}
		
		if(!cycleHasActivationFrame) {
			cycle = new Cycle();
		}

		// acqConfig.put("frameCycle", cycle);
		return cycle;
	}

	public static int detectThreshold(Set<ImagePlus> imageSet, Cycle cycle,
			int maxNthreshold) {
		ImagePlus chosenImp = null;
		int frameNr = 0;
		for (ImagePlus imp : imageSet) {
			if (cycle.isAcquisition(frameNr)) {
				chosenImp = imp;
				break;
				// if (!cycle.isFirstAfterActivation(frameNr)) {
				// // we are done, we just don't want the first after
				// // activation (could be too crowded in the beginning?)
				// break;
				// }
			}
			frameNr++;
		}

		// border=2, we want to create a 5x5 box, mins next too close to the
		// edge of the image will be ignored
		Range<Point3d> minMax = AutoDetect.findMinMax(chosenImp, 4);
		int xMin = (int) minMax.getMin().x;
		int yMin = (int) minMax.getMin().y;
		chosenImp.setRoi(xMin - 2, yMin - 2, 9, 9);
		ImageStatistics imgStats = chosenImp.getStatistics(Measurements.MEAN
				+ Measurements.STD_DEV);

		return (int) Math.round((imgStats.mean + maxNthreshold
				* imgStats.stdDev));
	}
}
