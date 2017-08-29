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

import java.io.Closeable;

/**
 * <p>Title: QSink</p>
 * <p>Description: Defines a sink that messages are queued into</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.io.qreactor.sink.QSink</code></p>
 */

public interface QSink<T> extends Closeable {
	/**
	 * Sinks the passed message
	 * @param message The message to sink
	 */
	public void onMessage(T message);
	
	/**
	 * Callback when the source completes
	 */
	public void processComplete();	
	
	
}
