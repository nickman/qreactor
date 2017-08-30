
package com.heliosapm.io.qreactor.queue;

import java.io.File;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.jctools.maps.NonBlockingHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.heliosapm.io.qreactor.json.JSONOps;
import com.heliosapm.io.qreactor.sink.QSink;
import com.heliosapm.io.qreactor.sink.QSinkImpl;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesRingBufferStats;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;
import net.openhft.chronicle.queue.BufferMode;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.RollCycle;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.WireKey;
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
	private final Class<T> messageType;
	private final File sinkFile;
	private final RollCycle rollCycle;
	private final WireType wireType; 
	private final boolean buffered;
	private final BufferMode writeBufferMode;
	private final SingleChronicleQueue queue;
	private BytesRingBufferStats bufferStats;
	private final Set<Flux<T>> subs = new NonBlockingHashSet<>();
	private final Consumer<T> writer;
	
	SinkImpl(Class<T> messageType, File sinkFile, RollCycle rollCycle, WireType wireType, boolean buffered,
			BufferMode writeBufferMode) {
		this.messageType = messageType;
		this.sinkFile = sinkFile;
		this.rollCycle = rollCycle;
		this.wireType = wireType;
		this.buffered = buffered;
		this.writeBufferMode = writeBufferMode;
		
		SingleChronicleQueueBuilder<?> builder = SingleChronicleQueueBuilder.builder(sinkFile, wireType)
//		SingleChronicleQueueBuilder<?> builder = SingleChronicleQueueBuilder.binary(sinkFile)
//		SingleChronicleQueueBuilder<?> builder = SingleChronicleQueueBuilder.text(sinkFile)
//		SingleChronicleQueueBuilder<?> builder = SingleChronicleQueueBuilder.
			
			.rollCycle(rollCycle)
			.buffered(buffered)
			.writeBufferMode(writeBufferMode);
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
		SinkImpl<String> sink = (SinkImpl<String>)Sink.builder(String.class, new File("/tmp/qreactor/foo"))
				.wireType(WireType.TEXT)
				.build();
		LOG.info("Sink: {}", sink);
		ExcerptAppender appender =  sink.queue.acquireAppender();
		WireKey key = new WireKey() {

			@Override
			public CharSequence name() {
				return "FooKey";
			}
			
		};
		
		appender.writeMessage(key, "YYY");
		
//		DocumentContext dc = appender.writingDocument();
//		dc.wire().write().text("YoYo!");
//		dc.close();

		appender.writeDocument(w -> {
			LOG.info("w: {} / {}", w.getClass().getName(), System.identityHashCode(w));
			w.write("foo").marshallable(m -> {
				LOG.info("m: {} / {}", m.getClass().getName(), System.identityHashCode(m));
				m.write("fooval").text(new java.util.Date().toString());
			});
		});
		
		
	}


	/**
	 * Returns 
	 * @return the messageType
	 */
	public Class<T> getMessageType() {
		return messageType;
	}


	/**
	 * Returns 
	 * @return the sinkFile
	 */
	public File getSinkFile() {
		return sinkFile;
	}


	/**
	 * Returns 
	 * @return the rollCycle
	 */
	public RollCycle getRollCycle() {
		return rollCycle;
	}


	/**
	 * Returns 
	 * @return the wireType
	 */
	public WireType getWireType() {
		return wireType;
	}


	/**
	 * Returns 
	 * @return the buffered
	 */
	public boolean isBuffered() {
		return buffered;
	}


	/**
	 * Returns 
	 * @return the writeBufferMode
	 */
	public BufferMode getWriteBufferMode() {
		return writeBufferMode;
	}


}
