package eu.brede.common.pipeline.hybrids;

import eu.brede.common.pipeline.consumers.Consumer;
import eu.brede.common.pipeline.producers.Producer;

public interface ProducingConsumer<P,C> extends Producer<P>, Consumer<C> {

}
