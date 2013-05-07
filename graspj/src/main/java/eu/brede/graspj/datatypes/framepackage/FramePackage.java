package eu.brede.graspj.datatypes.framepackage;

import java.nio.ShortBuffer;

public class FramePackage {
	private ShortBuffer packageBuffer;
	private int packageNr;
	private boolean isTermination;
	
	public FramePackage(ShortBuffer packageBuffer, int packageNr, boolean isTermination) {
		setPackageBuffer(packageBuffer);
		this.packageNr = packageNr;
		this.isTermination = isTermination;
	}
	
	public FramePackage(ShortBuffer packageBuffer, int packageNr) {
		this(packageBuffer, packageNr, false);
	}
	
	public void setPackageBuffer(ShortBuffer packageBuffer) {
		this.packageBuffer = packageBuffer;
		return;
	}
	
	public ShortBuffer getPackageBuffer() {
		return packageBuffer;
	}
	
	public int getPackageNr() {
		return packageNr;
	}
	
	public boolean isTermination() {
		return isTermination;
	}

}
