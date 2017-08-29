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

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.FluxSink.OverflowStrategy;

/**
 * <p>Title: QSinkFactory</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.io.qreactor.sink.QSinkFactory</code></p>
 */

public class QSinkFactory<T> {
	private final Class<T> type;
	private final Flux<T> flux;
	private FluxSink<T> emitter;
	private final OverflowStrategy backpressure;
	
	
	
	/**
	 * Creates a new QSinkFactory
	 * @param type The type of messages that will be sunk
	 * @param backpressure An optional overflow strategy. If null, defaults to {@link OverflowStrategy#ERROR}.
	 */
	public QSinkFactory(Class<T> type, OverflowStrategy backpressure) {
		this.type = type;
		this.backpressure = backpressure==null ? OverflowStrategy.ERROR : backpressure;
		flux = Flux.create(emitter -> {
			QSinkFactory.this.emitter = emitter;
		}, backpressure);
	}
	

}
