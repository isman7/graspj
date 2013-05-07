package eu.brede.graspj.pipeline.processors.filterer;

import java.nio.ByteBuffer;

import eu.brede.graspj.utils.Buffers;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.configs.filtering.FrameFilterConfig;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.Option;
import eu.brede.graspj.datatypes.cycle.Cycle;
import eu.brede.graspj.datatypes.filtermask.FilterMask;
import eu.brede.graspj.datatypes.spotcollection.BufferSpotCollection;

// TODO: unify, simplify
public class SpotFrameFilterer extends SpotFilterer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private FrameFilterConfig config;

	public SpotFrameFilterer(FrameFilterConfig config) {
		super();
		this.config = config;
	}

	public SpotFrameFilterer() {
		super();
		this.config = new FrameFilterConfig();
	}

	@Override
	protected FilterMask calcMask(AnalysisItem item) {

		BufferSpotCollection spotCollection = item.getSpots();

		ByteBuffer maskBuffer = Buffers.newDirectByteBuffer(spotCollection
				.getSpotCount());

		Cycle cycle = (Cycle) item.getAcquisitionConfig().get("frameCycle");

		for (int frameNr = 0; frameNr < spotCollection.getFrameCount(); frameNr++) {
			final byte maskEntry = calcMaskEntry(frameNr, cycle);
			for (int ssNr = 0; ssNr < spotCollection.getSpotCount(frameNr); ssNr++) {
				maskBuffer.put(maskEntry);
			}
		}

		maskBuffer.rewind();
		FilterMask newMask = new FilterMask();
		newMask.setBuffer(maskBuffer);

		return newMask;
	}

	private byte calcMaskEntry(final int frameNr, final Cycle cycle) {
		Option filterByColorOption = getConfig().gett("filterByColor");
		Option filterByTypeOption = getConfig().gett("filterByType");

		final int chosenColor = filterByColorOption.getInt("color");
		final boolean chosenIsActivation = filterByTypeOption
				.getBoolean("isActivation");

		final boolean encodeColor = getConfig().getBoolean("encodeColor");

		final int frameColor = cycle.getColor(frameNr);

		final byte nullEntry = 0;

		if (filterByColorOption.isSelected()) {
			if (frameColor != chosenColor) {
				return nullEntry;
			}
		}

		if (filterByTypeOption.isSelected()) {
			if (cycle.isActivation(frameNr) != chosenIsActivation) {
				return nullEntry;
			}
		}

		byte maskEntry = 1;
		if (encodeColor) {
			maskEntry = (byte) frameColor;
		}

		return maskEntry;
	}

	@Override
	public FrameFilterConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig config) {
		// TODO if cast fails, wrap instead!
		this.config = (FrameFilterConfig) config;
	}

}
