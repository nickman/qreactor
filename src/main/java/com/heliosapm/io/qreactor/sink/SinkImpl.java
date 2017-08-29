// This file is part of OpenTSDB.
// Copyright (C) 2010-2016  The OpenTSDB Authors.
//
// This program is free software: you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 2.1 of the License, or (at your
// option) any later version.  This program is distributed in the hope that it
// will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
// of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
// General Public License for more details.  You should have received a copy
// of the GNU Lesser General Public License along with this program.  If not,
// see <http://www.gnu.org/licenses/>.
package com.heliosapm.io.qreactor.sink;

import java.io.File;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.jctools.maps.NonBlockingHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.heliosapm.io.qreactor.json.JSONOps;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesRingBufferStats;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;
import net.openhft.chronicle.queue.BufferMode;
import net.openhft.chronicle.queue.RollCycle;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.Wire;
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
			.rollCycle(rollCycle)
			.buffered(buffered)
			.writeBufferMode(writeBufferMode);
		builder.onRingBufferStats(this);
			
		queue =	builder.build();
		LOG.info("Created sink: {}", sinkFile.getAbsolutePath());
		writer = writer();
		
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
		}
		throw new IllegalStateException("Failed to determine write strategy");
	}
	
	@Override
	public void accept(BytesRingBufferStats t) {
		bufferStats = t;		
	}
	
	public static void main(String[] args) {
		//new SinkImpl();
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
