package eu.brede.graspj.datatypes.bufferholder;

import java.nio.Buffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLMemory.Mem;

import eu.brede.common.opencl.utils.CLSystem;

public interface BufferHolderInterface<T extends Buffer> {
	public T setBuffer(T newBuffer);
	public T getBuffer();
	public void removeBuffer();
	public boolean holdsBuffer();
	public void append(T newBuffer);
	public void append(BufferHolder<T> newBufferHolder);
	public CLBuffer<T> getCLBuffer(CLSystem cl);
	public CLBuffer<T> setCLBuffer(CLBuffer<T> newCLBuffer);
	public Mem[] getFlags();
	public Mem[] setFlags(Mem... flags);
}
