
package com.heliosapm.io.qreactor.queue;

import java.io.File;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.jctools.maps.NonBlockingHashMap;
import org.jctools.maps.NonBlockingHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.heliosapm.io.qreactor.common.QBuilders.SinkBuilder;
import com.heliosapm.io.qreactor.json.JSONOps;
import com.heliosapm.io.qreactor.sink.QSink;
import com.heliosapm.io.qreactor.sink.QSinkImpl;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesRingBufferStats;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;
import net.openhft.chronicle.queue.BufferMode;
import net.openhft.chronicle.queue.RollCycle;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.WireType;
import net.openhft.chronicle.wire.WriteMarshallable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.FluxSink.OverflowStrategy;

/**
 * <p>Title: SinkImpl</p>
 * <p>Description: The actual sink implementation</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.io.qreactor.sink.SinkImpl</code></p>
 */

public class SinkImpl<T> implements Sink<T>, Consumer<BytesRingBufferStats> {
	private static final Logger LOG = LoggerFactory.getLogger(SinkImpl.class);
	private static final NonBlockingHashMap<String, SinkImpl<?>> SINKS = new NonBlockingHashMap<>(128);
	private final Class<T> messageType;
	private final BufferMode writeBufferMode;
	private final BufferMode readBufferMode;
	private final long timeoutMS;	
	private final SingleChronicleQueue queue;
	private BytesRingBufferStats bufferStats;
	private final Set<Flux<T>> subs = new NonBlockingHashSet<>();
	private final Consumer<T> writer;
	
	
//    long minNumberOfWriteBytesRemaining();
//    long capacity();
//    long getAndClearReadCount();
//    long getAndClearWriteCount();
//    long maxCopyTimeNs();
	
	
	/**
	 * Acquires the Sink for the passed sink file
	 * @param sinkFile The sink queue directory
	 * @param messageType The type of messages to sink
	 * @param rollCycle The queue file roll cycle
	 * @param wireType The queue wire type
	 * @param buffered true to buffer queue operations
	 * @param writeBufferMode The queue write buffer mode
	 * @param readBufferMode The queue read buffer mode
	 * @param blockSize The queue file block size
	 * @param bufferCapacity The queue buffer capacity
	 * @param timeoutMS The queue lock timeout in ms.
	 * @param sourceId The queue source id
	 */
	@SuppressWarnings("unchecked")
	public static <T> Sink<T> newSinkInstance(File sinkFile, Class<T> messageType, RollCycle rollCycle, WireType wireType, boolean buffered,
			BufferMode writeBufferMode, BufferMode readBufferMode, int blockSize, long bufferCapacity, long timeoutMS,
			int sourceId) {
		Objects.requireNonNull(sinkFile, "SinkFile");
		final String key = sinkFile.getAbsolutePath();
		SinkImpl<T> sink = (SinkImpl<T>) SINKS.get(key);
		if(sink==null) {
			synchronized(SINKS) {
				sink = (SinkImpl<T>) SINKS.get(key);
				if(sink==null) {
					sink = new SinkImpl<T>(
						sinkFile, messageType, rollCycle, wireType, buffered,
						writeBufferMode, readBufferMode, blockSize, bufferCapacity, timeoutMS,
						sourceId
					);
				}
			}
		}
		return sink;
	}
	
	/**
	 * Acquires the Sink for the passed builder
	 * @param builder The Sink builder
	 * @return the Sink matching the sink file in the passed builder 
	 */
	public static <T> Sink<T> newSinkInstance(SinkBuilder<T> builder) {
		Objects.requireNonNull(builder, "SinkBuilder");
		return newSinkInstance(
			builder.getSinkFile(), builder.getMessageType(), builder.getRollCycle(), builder.getWireType(),
			builder.isBuffered(), builder.getWriteBufferMode(), builder.getReadBufferMode(),
			builder.getBlockSize(), builder.getBufferCapacity(), builder.getTimeoutMS(), builder.getSourceId()
		);
	}

	
	private SinkImpl(File sinkFile, Class<T> messageType, RollCycle rollCycle, WireType wireType, boolean buffered,
			BufferMode writeBufferMode, BufferMode readBufferMode, int blockSize, long bufferCapacity, long timeoutMS,
			int sourceId) {
		this.messageType = messageType;
		this.writeBufferMode = writeBufferMode;
		this.readBufferMode = readBufferMode;
		this.timeoutMS = timeoutMS;


		SingleChronicleQueueBuilder<?> builder = SingleChronicleQueueBuilder.binary(sinkFile)
				.rollCycle(rollCycle)
				.buffered(buffered)			
				.writeBufferMode(writeBufferMode)
				.readBufferMode(readBufferMode)
				.blockSize(blockSize)
				.bufferCapacity(bufferCapacity)
				.timeoutMS(timeoutMS)
				.sourceId(sourceId);
		builder.onRingBufferStats(this);

		queue =	builder.build();
		LOG.info("Created sink: {}", sinkFile.getAbsolutePath());
		writer = writer();

	}

	public QSink<T> sink() {
		return sink(OverflowStrategy.ERROR);
	}
	
	public QSink<T> sink(OverflowStrategy backpressure) {
		AtomicReference<FluxSink<T>> em = new AtomicReference<>(null);
		Flux<T> flux = Flux.<T>create(emitter -> {			
			em.set(emitter);			
		}, backpressure);
		
		flux.subscribe(t -> sink(t));
		subs.add(flux);
		flux.doFinally(sig -> {
			subs.remove(flux);
		});
		return new QSinkImpl<T>(em.get());
	}

	private void sink(T message) {
		writer.accept(message);
	}
	
	private Consumer<T> writer() {
		if(Marshallable.class.isAssignableFrom(messageType)) {
			return new Consumer<T>() {
				@Override
				public void accept(T t) {
					queue.acquireAppender().writeDocument((WriteMarshallable)t);
				}
			};
		} else if(WriteBytesMarshallable.class.isAssignableFrom(messageType)) {
			return new Consumer<T>() {
				@Override
				public void accept(T t) {
					queue.acquireAppender().writeBytes((WriteBytesMarshallable)t);					
				}
			};
		} else if(BytesStore.class.isAssignableFrom(messageType)) {
			return new Consumer<T>() {
				@SuppressWarnings("rawtypes")
				@Override
				public void accept(T t) {
					queue.acquireAppender().writeBytes((BytesStore)t);	
					
				}
			};
		} else if(messageType.getAnnotation(JsonSerialize.class)!=null) {
			return new Consumer<T>() {
				@SuppressWarnings("rawtypes")
				@Override
				public void accept(T t) {
					Bytes bytes = JSONOps.serializeToChronicleBytes(t);
					queue.acquireAppender().writeBytes(bytes);
					bytes.release();
				}
			};
		} else if(CharSequence.class.isAssignableFrom(messageType)) {
			return new Consumer<T>() {
				@Override
				public void accept(T t) {
					queue.acquireAppender().writeText((CharSequence)t);
				}
			};
		}
		throw new IllegalStateException("Failed to determine write strategy");
	}
	
	@Override
	public void accept(BytesRingBufferStats t) {
		bufferStats = t;		
	}
	
	public static void main(String[] args) {
//		SinkImpl<String> sink = (SinkImpl<String>)Sink.builder(String.class, new File("/tmp/qreactor/foo"))
//				.wireType(WireType.TEXT)
//				.build();
//		LOG.info("Sink: {}", sink);
//		ExcerptAppender appender =  sink.queue.acquireAppender();
//		WireKey key = new WireKey() {
//
//			@Override
//			public CharSequence name() {
//				return "FooKey";
//			}
//			
//		};
//		
//		appender.writeMessage(key, "YYY");
//		
////		DocumentContext dc = appender.writingDocument();
////		dc.wire().write().text("YoYo!");
////		dc.close();
//
//		appender.writeDocument(w -> {
//			LOG.info("w: {} / {}", w.getClass().getName(), System.identityHashCode(w));
//			w.write("foo").marshallable(m -> {
//				LOG.info("m: {} / {}", m.getClass().getName(), System.identityHashCode(m));
//				m.write("fooval").text(new java.util.Date().toString());
//			});
//		});
		
		
	}


	/**
	 * Returns 
	 * @return the messageType
	 */
	public Class<T> getMessageType() {
		return messageType;
	}


	/**
	 * Returns the queue directory
	 * @return the queue directory
	 */
	public File getSinkFile() {
		return queue.file();
	}


	/**
	 * Returns the queue file roll cycle
	 * @return the queue file roll cycle
	 */
	public RollCycle getRollCycle() {
		return queue.rollCycle();
	}


	/**
	 * Returns the queue wire type
	 * @return the queue wire type
	 */
	public WireType getWireType() {
		return queue.wireType();
	}


	/**
	 * Indicates if the queue is buffered 
	 * @return true if the queue is buffered, false otherwise
	 */
	public boolean isBuffered() {
		return queue.buffered();
	}


	/**
	 * Returns the queue write buffer mode
	 * @return the queue write buffer mode
	 */
	public BufferMode getWriteBufferMode() {
		return writeBufferMode;
	}
	

	/**
	 * Returns the queue read buffer mode
	 * @return the queue read buffer mode
	 */
	public BufferMode getReadBufferMode() {
		return readBufferMode;
	}
	
	/**
	 * Returns the queue block size
	 * @return the queue block size
	 */
	public long getBlockSize() {
		return queue.blockSize();
	}
	
	/**
	 * Returns the queue buffer capacity
	 * @return the queue buffer capacity
	 */
	public long getBufferCapacity() {
		return queue.bufferCapacity();
	}

	/**
	 * Returns the sink source id
	 * @return the sink source id
	 */
	public int getSourceId() {
		return queue.sourceId();
	}

	/**
	 * Returns the queue lock timeout in ms
	 * @return the queue lock timeout in ms
	 */
	public long getLockTimeout() {
		return timeoutMS;
	}
}
