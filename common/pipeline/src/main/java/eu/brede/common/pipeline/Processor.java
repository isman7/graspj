package eu.brede.common.pipeline;

import java.io.Serializable;


public interface Processor<T> extends Component, Serializable { //extends BufferHolder {
	public void process(T item);
}
