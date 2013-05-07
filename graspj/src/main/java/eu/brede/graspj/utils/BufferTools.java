package eu.brede.graspj.utils;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;


public class BufferTools {
	
	public static Object arrayFromBuffer(Buffer buffer) {
		if (buffer==null) {
			return null;
		}
		Object array = null;
		buffer.rewind();
		if (buffer instanceof ShortBuffer) {
			array = new short[buffer.limit()];
			((ShortBuffer)buffer).get((short[])array);
		}
		else if (buffer instanceof IntBuffer) {
			array = new int[buffer.limit()];
			((IntBuffer)buffer).get((int[])array);
		}
		else if (buffer instanceof FloatBuffer) {
			array = new float[buffer.limit()];
			((FloatBuffer)buffer).get((float[])array);
		}
		else if (buffer instanceof LongBuffer) {
			array = new long[buffer.limit()];
			((LongBuffer)buffer).get((long[])array);
		}
		else if (buffer instanceof CharBuffer) {
			array = new char[buffer.limit()];
			((CharBuffer)buffer).get((char[])array);
		}
		else if (buffer instanceof DoubleBuffer) {
			array = new double[buffer.limit()];
			((DoubleBuffer)buffer).get((double[])array);
		}
		else if (buffer instanceof ByteBuffer) {
			array = new byte[buffer.limit()];
			((ByteBuffer)buffer).get((byte[])array);
		}
		else {
			// TODO throw unsupported error
		}
		return array;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Buffer> T newDirectBuffer(Object array) {
		T buffer;
		if (array instanceof short[]) {
			buffer = (T) Buffers.newDirectShortBuffer((short[])array);
		}
		else if (array instanceof int[]) {
			buffer = (T) Buffers.newDirectIntBuffer((int[])array);
		}
		else if (array instanceof float[]) {
			buffer = (T) Buffers.newDirectFloatBuffer((float[])array);
		}
		else if (array instanceof byte[]) {
			buffer = (T) Buffers.newDirectByteBuffer((byte[])array);
		}
		else if (array instanceof double[]) {
			buffer = (T) Buffers.newDirectDoubleBuffer((double[])array);
		}
		else if (array instanceof long[]) {
			buffer = (T) Buffers.newDirectLongBuffer((long[])array);
		}
		else if (array instanceof char[]) {
			buffer = (T) Buffers.newDirectCharBuffer((char[])array);
		}
		else {
			throw new IllegalArgumentException("array type not supported");
		}
		return buffer;
	}
	
	@SuppressWarnings("unchecked")
	public static <B extends Buffer> B append(B dest, B src) {
		
		int freeSpaceSpots = dest.capacity()-dest.limit();
		if(freeSpaceSpots>=src.limit()) {
			src.rewind();
			dest.position(dest.limit());
			dest.limit(dest.limit()+src.limit());
			Buffers.put(dest,src);
			dest.rewind();
			return dest;
		}
		else {
			// TODO make use of copyBufferDirect?!
			int newLimit = dest.limit()+src.limit();
			int newBufferSize = (newLimit)*2; // TODO 2 --> parameter "load factor"
			B combined;
			if (src instanceof ShortBuffer) {
				combined = (B) Buffers.newDirectShortBuffer(newBufferSize);
			}
			else if (src instanceof IntBuffer) {
				combined = (B) Buffers.newDirectIntBuffer(newBufferSize);
			}
			else if (src instanceof LongBuffer) {
				combined = (B) Buffers.newDirectLongBuffer(newBufferSize);
			}
			else if (src instanceof CharBuffer) {
				combined = (B) Buffers.newDirectCharBuffer(newBufferSize);
			}
			else if (src instanceof FloatBuffer) {
				combined = (B) Buffers.newDirectFloatBuffer(newBufferSize);
			}
			else if (src instanceof DoubleBuffer) {
				combined = (B) Buffers.newDirectDoubleBuffer(newBufferSize);
			}
			else if (src instanceof ByteBuffer) {
				combined = (B) Buffers.newDirectByteBuffer(newBufferSize);
			}
			else {
				throw new IllegalArgumentException(
						"Incompatible Buffer classes: dest = " 
						+ dest.getClass().getName() + ", src = " 
						+ src.getClass().getName());
			}
			combined.limit(newLimit);
			dest.rewind();
			src.rewind();
			combined.rewind();
			Buffers.put(combined, dest);
			Buffers.put(combined, src);
			combined.rewind();
			return combined;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <B extends Buffer> B copyBufferDirect(B buffer) {
		if(buffer==null) {
			return null;
			// TODO think of that!
		}
		int newBufferSize = buffer.capacity(); // TODO 2 --> parameter "load factor"
		B copy;
		if (buffer instanceof ShortBuffer) {
			copy = (B) Buffers.newDirectShortBuffer(newBufferSize);
		}
		else if (buffer instanceof IntBuffer) {
			copy = (B) Buffers.newDirectIntBuffer(newBufferSize);
		}
		else if (buffer instanceof LongBuffer) {
			copy = (B) Buffers.newDirectLongBuffer(newBufferSize);
		}
		else if (buffer instanceof CharBuffer) {
			copy = (B) Buffers.newDirectCharBuffer(newBufferSize);
		}
		else if (buffer instanceof FloatBuffer) {
			copy = (B) Buffers.newDirectFloatBuffer(newBufferSize);
		}
		else if (buffer instanceof DoubleBuffer) {
			copy = (B) Buffers.newDirectDoubleBuffer(newBufferSize);
		}
		else if (buffer instanceof ByteBuffer) {
			copy = (B) Buffers.newDirectByteBuffer(newBufferSize);
		}
		else {
			throw new IllegalArgumentException(
					"Unsupported Buffer type: " 
					+ buffer.getClass().getName());
		}
		buffer.rewind();
		Buffers.put(copy, buffer);
		copy.rewind();
		copy.limit(buffer.limit());
		return copy;
	}
	
	@SuppressWarnings("unchecked")
	public static <B extends Buffer> B copyBufferDirect(B buffer, int start, int end) {
		if(buffer==null) {
			return null;
			// TODO think of that!
		}
		int newBufferSize = end-start;
		if(newBufferSize<1) { 
			return null; 
		}
		
		B copy;
		if (buffer instanceof ShortBuffer) {
			copy = (B) Buffers.newDirectShortBuffer(newBufferSize);
		}
		else if (buffer instanceof IntBuffer) {
			copy = (B) Buffers.newDirectIntBuffer(newBufferSize);
		}
		else if (buffer instanceof LongBuffer) {
			copy = (B) Buffers.newDirectLongBuffer(newBufferSize);
		}
		else if (buffer instanceof CharBuffer) {
			copy = (B) Buffers.newDirectCharBuffer(newBufferSize);
		}
		else if (buffer instanceof FloatBuffer) {
			copy = (B) Buffers.newDirectFloatBuffer(newBufferSize);
		}
		else if (buffer instanceof DoubleBuffer) {
			copy = (B) Buffers.newDirectDoubleBuffer(newBufferSize);
		}
		else if (buffer instanceof ByteBuffer) {
			copy = (B) Buffers.newDirectByteBuffer(newBufferSize);
		}
		else {
			throw new IllegalArgumentException(
					"Unsupported Buffer type: " 
					+ buffer.getClass().getName());
		}
		int currentPos = buffer.position();
		int currentLimit = buffer.limit();
		
		buffer.position(start);
		buffer.limit(end);
//		buffer.rewind();
		Buffers.put(copy, buffer);
		copy.rewind();
		copy.limit(newBufferSize);
		
		buffer.position(currentPos);
		buffer.limit(currentLimit);
		return copy;
	}
	
	@SuppressWarnings("unchecked")
	@Deprecated
	public static <T extends Buffer> T copyBufferDirectDeprecated(T buffer) {
		int bytesPerElement = 1;
		
		if (buffer instanceof ShortBuffer) {
			bytesPerElement = 4;
			return (T) ByteBuffer.allocateDirect(
								buffer.limit()*bytesPerElement)
								.order(ByteOrder.nativeOrder())
								.asShortBuffer().put((ShortBuffer) buffer);
		}
		else if (buffer instanceof IntBuffer) {
			bytesPerElement = 4;
			return (T) ByteBuffer.allocateDirect(
					buffer.limit()*bytesPerElement)
					.order(ByteOrder.nativeOrder())
					.asIntBuffer().put((IntBuffer) buffer);
		}
		else if (buffer instanceof ShortBuffer) {
			bytesPerElement = 2;
			return (T) ByteBuffer.allocateDirect(
					buffer.limit()*bytesPerElement)
					.order(ByteOrder.nativeOrder())
					.asShortBuffer().put((ShortBuffer) buffer);
		}
		else if (buffer instanceof LongBuffer) {
			bytesPerElement = 8;
			return (T) ByteBuffer.allocateDirect(
					buffer.limit()*bytesPerElement)
					.order(ByteOrder.nativeOrder())
					.asLongBuffer().put((LongBuffer) buffer);
		}
		else if (buffer instanceof CharBuffer) {
			bytesPerElement = 2;
			return (T) ByteBuffer.allocateDirect(
					buffer.limit()*bytesPerElement)
					.order(ByteOrder.nativeOrder())
					.asCharBuffer().put((CharBuffer) buffer);
		}
		else if (buffer instanceof DoubleBuffer) {
			bytesPerElement = 8;
			return (T) ByteBuffer.allocateDirect(
					buffer.limit()*bytesPerElement)
					.order(ByteOrder.nativeOrder())
					.asDoubleBuffer().put((DoubleBuffer) buffer);
		}
		else if (buffer instanceof ByteBuffer) {
			bytesPerElement = 1;
			return (T) ByteBuffer.allocateDirect(
					buffer.limit()*bytesPerElement)
					.order(ByteOrder.nativeOrder())
					.put((ByteBuffer) buffer);
		}
		return null;		
		
	}
	
//	TEMPLATE
//		if (buffer instanceof ShortBuffer) {
//			
//		}
//		else if (buffer instanceof IntBuffer) {
//
//		}
//		else if (buffer instanceof LongBuffer) {
//
//		}
//		else if (buffer instanceof CharBuffer) {
//
//		}
//		else if (buffer instanceof FloatBuffer) {
//		
//		}
//		else if (buffer instanceof DoubleBuffer) {
//
//		}
//		else if (buffer instanceof ByteBuffer) {
//
//		}
//	}
}
