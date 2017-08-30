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
package com.heliosapm.io.qreactor.common;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import net.openhft.chronicle.queue.BufferMode;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.RollCycle;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.wire.WireType;

/**
 * <p>Title: QBuilders</p>
 * <p>Description: Builders for Queue Sinks and Sources</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.io.qreactor.common.QBuilders</code></p>
 */

public class QBuilders {
	
	private static final AtomicInteger SINK_SOURCE_ID = new AtomicInteger();
	
	public class SinkBuilder<T> {
		private final File sinkFile;
		private final Class<T> messageType;
		private RollCycle rollCycle = RollCycles.DAILY;
		private WireType wireType = WireType.JSON; 
		private boolean buffered = false;
		private BufferMode writeBufferMode = BufferMode.None;
		private BufferMode readBufferMode = BufferMode.None;
		private int blockSize = ChronicleQueue.TEST_BLOCK_SIZE;
		private long bufferCapacity = 2 << 20;
		private long timeoutMS = 10_000;
		private int sourceId = -1;

		private SinkBuilder(Class<T> messageType, File sinkFile) {
			Objects.requireNonNull(sinkFile, "sinkFile");
			Objects.requireNonNull(messageType, "messageType");
			this.sinkFile = sinkFile;
			this.messageType = messageType;
		}

		/**
		 * Returns the sink file
		 * @return the sinkFile
		 */
		public File getSinkFile() {
			return sinkFile;
		}
		
		/**
		 * Sets the roll cycle for the queue data files
		 * @param rollCycle A queue roll cycle
		 * @return this builder
		 */
		public SinkBuilder<T> rollCycle(RollCycles rollCycle) {
			Objects.requireNonNull(rollCycle, "rollCycle");
			this.rollCycle = rollCycle;
			return this;
		}
		
		/**
		 * Sets the wire type of the sink's queue
		 * @param wireType The wire type of the.re queue
		 * @return this builder
		 */
		public SinkBuilder<T> wireType(WireType wireType) {
			Objects.requireNonNull(wireType, "wireType");
			this.wireType = wireType;
			return this;
		}
		
		/**
		 * Sets the write buffer mode
		 * @param bufferMode the buffer mode (None, Copy or Asynchronous)
		 * @return this builder
		 */
		public SinkBuilder<T> writeBufferMode(BufferMode bufferMode) {
			Objects.requireNonNull(bufferMode, "bufferMode");
			this.writeBufferMode = bufferMode;
			return this;
		}
		
		/**
		 * Sets the read buffer mode
		 * @param bufferMode the buffer mode (None, Copy or Asynchronous)
		 * @return this builder
		 */
		public SinkBuilder<T> readBufferMode(BufferMode bufferMode) {
			Objects.requireNonNull(bufferMode, "bufferMode");
			this.readBufferMode = bufferMode;
			return this;
		}
		
				
		/**
		 * Sets the buffer enablement for queue
		 * @param buffered true to buffer, false otherwise
		 * @return this builder
		 */
		public SinkBuilder<T> buffered(boolean buffered) {
			this.buffered = buffered;
			return this;
		}

		/**
		 * Sets the queue block size in bytes
		 * @param blockSize the block size in bytes
		 * @return this builder
		 */
		public SinkBuilder<T>  setBlockSize(int blockSize) {
			this.blockSize = blockSize;
			return this;
		}

		/**
		 * The queue lock timeout in ms
		 * @param timeoutMS The timeout in ms
		 * @return this builder
		 */
		public SinkBuilder<T> timeoutMS(long timeoutMS) {
			this.timeoutMS = timeoutMS;
			return this;
		}

		/**
		 * Sets the queue buffer capacity
		 * @param bufferCapacity the buffer capacity to set
		 * @return this builder
		 */
		public SinkBuilder<T> bufferCapacity(long bufferCapacity) {
			this.bufferCapacity = bufferCapacity;
			return this;
		}

		/**
		 * Sets the sink source id
		 * @param sourceId the source id to set
		 * @return this builder
		 */
		public SinkBuilder<T> sourceId(int sourceId) {
			this.sourceId = sourceId;
			return this;
		}

		/**
		 * Returns the message type writen to this sink
		 * @return the message type writen to this sink
		 */
		public Class<T> getMessageType() {
			return messageType;
		}

		/**
		 * Returns the roll cycle for the queue
		 * @return the roll cycle for the queue
		 */
		public RollCycle getRollCycle() {
			return rollCycle==null ? RollCycles.DAILY : rollCycle;
		}

		/**
		 * Returns the queue wire type
		 * @return the queue wire type
		 */
		public WireType getWireType() {
			return wireType;
		}

		/**
		 * Indicates if the queue will be buffered 
		 * @return true if buffered, false otherwise
		 */
		public boolean isBuffered() {
			return buffered;
		}

		/**
		 * Returns the queue write buffer mode
		 * @return the write buffer mode
		 */
		public BufferMode getWriteBufferMode() {
			return writeBufferMode;
		}

		/**
		 * Returns the queue read buffer mode
		 * @return the read buffer mode
		 */
		public BufferMode getReadBufferMode() {
			return readBufferMode;
		}

		/**
		 * Returns the queue block size
		 * @return the block size
		 */
		public int getBlockSize() {
			return blockSize;
		}

		/**
		 * Returns the queue buffer capacity
		 * @return the buffer capacity
		 */
		public long getBufferCapacity() {
			return bufferCapacity;
		}

		/**
		 * Returns the timeout
		 * @return the timeout
		 */
		public long getTimeoutMS() {
			return timeoutMS;
		}

		/**
		 * Returns the source id
		 * @return the source id
		 */
		public synchronized int getSourceId() {
			if(sourceId==-1) {
				sourceId = SINK_SOURCE_ID.incrementAndGet();
			}
			return sourceId;
		}
		
		
		
//		Sink<T> doBuild() {
//			return new SinkImpl<T>(messageType, sinkFile, rollCycle, wireType, buffered, writeBufferMode);
//		}
//		
//		/**
//		 * Builds the sink
//		 * @return the sink
//		 */
//		public Sink<T> build() {
//			return SinkRepo.<T>sink(this);
//			
//		}
	}

}
