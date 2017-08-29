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
import java.util.Objects;

import net.openhft.chronicle.queue.BufferMode;
import net.openhft.chronicle.queue.RollCycle;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.wire.WireType;

/**
 * <p>Title: Sink</p>
 * <p>Description: </p> 
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
		 * Returns 
		 * @return the sinkFile
		 */
		public File getSinkFile() {
			return sinkFile;
		}
		
		public SinkBuilder<T> rollCycle(RollCycles rollCycle) {
			Objects.requireNonNull(rollCycle, "rollCycle");
			this.rollCycle = rollCycle;
			return this;
		}
		
		public SinkBuilder<T> wireType(WireType wireType) {
			Objects.requireNonNull(wireType, "wireType");
			this.wireType = wireType;
			return this;
		}
		
		public SinkBuilder<T> writeBufferMode(BufferMode bufferMode) {
			Objects.requireNonNull(bufferMode, "bufferMode");
			this.writeBufferMode = bufferMode;
			return this;
		}
				
		public SinkBuilder<T> buffered(boolean buffered) {
			this.buffered = buffered;
			return this;
		}
		
		Sink<T> doBuild() {
			return new SinkImpl<T>(messageType, sinkFile, rollCycle, wireType, buffered, writeBufferMode);
		}
		
		public Sink<T> build() {
			return SinkRepo.<T>sink(this);
			
		}
	}
	
	public static <T> SinkBuilder<T> builder(Class<T> messageType, File sinkFile) {
		return new SinkBuilder<T>(messageType, sinkFile);
	}
}
