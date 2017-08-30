/**
 * 
 */
package com.heliosapm.io.qreactor.queue;

import java.io.File;

import org.springframework.context.annotation.Configuration;

import net.openhft.chronicle.queue.BufferMode;
import net.openhft.chronicle.queue.RollCycle;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.wire.WireType;

/**
 * @author nwhitehead
 *
 */
@Configuration
public class SinkConfiguration {
	private File sinkFile;
	private Class<?> messageType;
	private RollCycle rollCycle = RollCycles.DAILY;
	private WireType wireType = WireType.JSON; 
	private boolean buffered = false;
	private BufferMode writeBufferMode = BufferMode.None;

}
