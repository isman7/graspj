package eu.brede.graspj.datatypes.filtermask;

import java.nio.ByteBuffer;

import eu.brede.graspj.datatypes.bufferholder.BufferHolder;

public class FilterMask extends BufferHolder<ByteBuffer> {
	
	public FilterMask() {
		super();
	}

	public FilterMask(BufferHolder<ByteBuffer> bh) {
		super(bh);
	}

	public int countPositives() {
		int count=0;
		if(getBuffer()!=null) {			
			getBuffer().rewind();
			while(getBuffer().hasRemaining()) {
				if(getBuffer().get()>0) {
					count++;
				}
			}
			getBuffer().rewind();
		}
		return count;
	}
	
	public void multiply(FilterMask mask) {
		if(!mask.holdsBuffer()) {
			return;
		}
		if(!holdsBuffer()) {
			setBuffer(mask.getBuffer());
			return;
		}
		// only multiply masks if their size (i.e. limit) is identical, 
		// otherwise keep bigger one
		if(mask.getBuffer().limit() < getBuffer().limit()) {
			return;
		}
		if(mask.getBuffer().limit() > getBuffer().limit()) {
			setBuffer(mask.getBuffer());
			return;
		}
		
		// if both masks hold buffers of the same size, multiply them:
		mask.getBuffer().rewind();
		getBuffer().rewind();

		while(getBuffer().hasRemaining() && mask.getBuffer().hasRemaining()) {
			byte maskEntry = mask.getBuffer().get();
			byte entry = getBuffer().get();

			if(maskEntry!=1) {
				getBuffer().position(getBuffer().position()-1);
				getBuffer().put((byte)(entry*maskEntry));
			}
		}
			
	}
}
