
package com.heliosapm.io.qreactor.queue;

import java.io.File;
import java.util.Objects;

import net.openhft.chronicle.queue.BufferMode;
import net.openhft.chronicle.queue.RollCycle;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.wire.WireType;

/**
 * <p>Title: Sink</p>
 * <p>Description: Defines a sink endpoint for queueing a message</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.io.qreactor.sink.Sink</code></p>
 */

public interface Sink<T> {
	
//	File (directory name)
//	Data Format  (BinaryWire or TextWire)
//	Roll Cycle
//	Reader Pause
//	Buffered
//	BufferMode (None, Copy, Asynchronous)
	
	

	public class SinkBuilder<T> {
		private final File sinkFile;
		private final Class<T> messageType;
		private RollCycle rollCycle = RollCycles.DAILY;
		private WireType wireType = WireType.JSON; 
		private boolean buffered = false;
		private BufferMode writeBufferMode = BufferMode.None;

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
		 * @param wireType The wire type of the queue
		 * @return this builder
		 */
		public SinkBuilder<T> wireType(WireType wireType) {
			Objects.requireNonNull(wireType, "wireType");
			this.wireType = wireType;
			return this;
		}
		
		/**
		 * Sets the buffer mode
		 * @param bufferMode the buffer mode (None, Copy or Asynchronous)
		 * @return this builder
		 */
		public SinkBuilder<T> writeBufferMode(BufferMode bufferMode) {
			Objects.requireNonNull(bufferMode, "bufferMode");
			this.writeBufferMode = bufferMode;
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
		
		Sink<T> doBuild() {
			return new SinkImpl<T>(messageType, sinkFile, rollCycle, wireType, buffered, writeBufferMode);
		}
		
		/**
		 * Builds the sink
		 * @return the sink
		 */
		public Sink<T> build() {
			return SinkRepo.<T>sink(this);
			
		}
	}
	
	public static <T> SinkBuilder<T> builder(Class<T> messageType, File sinkFile) {
		return new SinkBuilder<T>(messageType, sinkFile);
	}
}
