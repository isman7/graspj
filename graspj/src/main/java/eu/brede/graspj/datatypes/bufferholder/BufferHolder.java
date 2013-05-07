package eu.brede.graspj.datatypes.bufferholder;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLMemory.Mem;

import eu.brede.common.opencl.utils.CLSystem;
import eu.brede.common.util.SimpleLock;
import eu.brede.graspj.opencl.utils.CLSystemGJ;
import eu.brede.graspj.utils.BufferTools;
import eu.brede.graspj.utils.Utils;

public class BufferHolder<T extends Buffer> 
	implements BufferHolderInterface<T>, Externalizable {
	
	private static final long serialVersionUID = 1L;
	
	protected T buffer;
	protected CLBuffer<T> clBuffer;
	protected Mem[] flags = new Mem[] {Mem.READ_WRITE};
	
	protected CLSystem cl = CLSystemGJ.getDefault();
	protected SimpleLock lock = new SimpleLock();
	
	public BufferHolder() {
		
	}
	
	
	
	public CLSystem getCLSystem() {
		if(cl==null) {
			cl = CLSystemGJ.getDefault();
		}
		return cl;
	}



	public void setCLSystem(CLSystem cl) {
		this.cl = cl;
	}



	// Copy Constructor, copies contents of buffer and clBuffer separately
	public BufferHolder(BufferHolder<T> bh) {
		buffer = BufferTools.copyBufferDirect(bh.getBuffer());
		flags = bh.flags.clone();
//		int newSize = (int) bh.getCLBuffer().getCLSize();
//		clBuffer = (CLBuffer<T>) pipe.getContext().createBuffer(newSize,flags);
		cl.returnQueue(cl.pollQueue().putCopyBuffer(bh.getCLBuffer(cl), getCLBuffer(cl))
			.finish());
	}
	
	@Override
	public T setBuffer(T newBuffer) {
		T oldBuffer = buffer;
		buffer = newBuffer;
		// TODO also release clBuffer? if so, check for isReleased first
//		if(clBuffer!=null) {
//			clBuffer.release();
//			clBuffer=null;
//		}
		return oldBuffer;
	}

	@Override
	public T getBuffer() {
//		if(buffer!=null) {
//			return buffer;			
//		}
//		if(clBuffer==null) {
//			return buffer;
//		}
//		if(clBuffer.getBuffer()!=null) {
//			buffer = clBuffer.getBuffer();
//			return buffer;
//		}
		return buffer;
	}

	@Override
	public void removeBuffer() {
		setBuffer(null);
	}

	@Override
	public boolean holdsBuffer() {
		return getBuffer()==null?false:true;
	}
	
	@Override
	public CLBuffer<T> getCLBuffer(CLSystem cl) {
		setCLSystem(cl);
		if(clBuffer==null) {
			createCLBuffer();
		}
		if(clBuffer.isReleased()) {
			createCLBuffer();
		}
		else {			
			if(clBuffer.getContext()!=cl.getContext()) {
				clBuffer.release();
				createCLBuffer();
			}
		}
		return clBuffer;
	}

	@Override
	public CLBuffer<T> setCLBuffer(CLBuffer<T> newCLBuffer) {
		CLBuffer<T> oldCLBuffer = clBuffer;
		if(newCLBuffer == null) {
			clBuffer = null;
			return oldCLBuffer;
		}
		if(newCLBuffer.getBuffer()==null) {
			throw new IllegalArgumentException("CLBuffer must have NIO buffer attached");
		}
//		CLBuffer<T> oldCLBuffer = clBuffer;
		clBuffer = newCLBuffer;
		buffer = clBuffer.getBuffer();
		flags = clBuffer.getConfig().toArray(flags);
		return oldCLBuffer;
	}

	@Override
	public void append(T newBuffer) {
		setBuffer(BufferTools.append(getBuffer(),newBuffer));
	}
	
	@Override
	public void append(BufferHolder<T> newBufferHolder) {
		if(!newBufferHolder.holdsBuffer()) {
			return;
		}
		if(!holdsBuffer()) {
			setBuffer(BufferTools.copyBufferDirect(newBufferHolder.getBuffer()));
			return;
		}
		setBuffer(BufferTools.append(getBuffer(),newBufferHolder.getBuffer()));
		return;
	}
	
	@Override
	public Mem[] getFlags() {
		return flags;
	}

	@Override
	public Mem[] setFlags(Mem... newFlags) {
		Mem[] oldFlags = flags;
		// TODO insert check if new flags==old flags, then clbuffer can be kept
		flags = newFlags;
		clBuffer.release();
		clBuffer = null;
		return oldFlags;
	}
	
	protected void createCLBuffer() {
		getBuffer().rewind();
		clBuffer = Utils.tryAllocation(new Callable<CLBuffer<T>>() {

			@Override
			public CLBuffer<T> call() throws Exception { 
				return cl.getContext()
						.createBuffer(buffer,Mem.flagsToInt(flags));
			}
			
		});
		
	}
	
	// TODO this is a temporary solution, needed for easy hue cleaning in ColorCodedRendering
	// Think about a better way!
	public void clean() {
		throw new Error("unsupported operation");
	}
	
	// moved here from Rendering, good idea?
	// Answer: Doesn't work that way as type is unknown at compile time
//	public void clean() {
//		getBuffer().rewind();
//		while(getBuffer().hasRemaining()) {
//			Buffers.putf(getBuffer(), 0f);
//		}
//		getBuffer().rewind();
//		pipe.getTransferQueue().putWriteBuffer(getCLBuffer(), true);
//		return;
//	}
	
	// best effort to free memory used by the BufferHolder
	public void free() {
		if(clBuffer!=null) {
			if(!clBuffer.isReleased()) {
				clBuffer.release();
			}
			clBuffer = null;
		}
		if(buffer != null) {
			buffer.clear();
			removeBuffer();
		}
		// TODO perhaps run GC?
//		System.gc();
		return;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(BufferTools.arrayFromBuffer(buffer));
		out.writeObject(flags);
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		Object object = in.readObject();
		buffer = (T) (object==null?null:
			BufferTools.newDirectBuffer(object));
		flags = (Mem[]) in.readObject();
		cl = CLSystemGJ.getDefault();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		free();
	}
	
	public void lock() {
		lock.lock();
	}
	
	public void unlock() {
		lock.unlock();
	}

	@SuppressWarnings("unchecked")
	public <E extends BufferHolder<T>> ArrayList<E> split(
			ArrayList<Integer> splitPoints) {
		
		ArrayList<E> newBuffers = new ArrayList<>();
		
		splitPoints.add(getBuffer().limit());
		int start = 0;
		for(Number splitPoint: splitPoints) {
			int end = splitPoint.intValue();
			T newBuffer = BufferTools.copyBufferDirect(getBuffer(), start, end);
			E newBH = null;
			try {
				newBH = (E) this.getClass().newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
//					new BufferHolder<>();
			newBH.setBuffer(newBuffer);
			newBuffers.add((E)newBH);
			start = end;
		}
		
		return newBuffers;
	}

}
