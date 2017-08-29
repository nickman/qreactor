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

import org.jctools.maps.NonBlockingHashMap;

import com.heliosapm.io.qreactor.sink.Sink.SinkBuilder;

/**
 * <p>Title: SinkRepo</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.io.qreactor.sink.SinkRepo</code></p>
 */

public class SinkRepo {
	@SuppressWarnings("rawtypes")
	private static final NonBlockingHashMap<String, SinkImpl>  SINKS = new NonBlockingHashMap<>();
	
	@SuppressWarnings("unchecked")
	static <T> SinkImpl<T> sink(SinkBuilder<T> builder) {
		String key = builder.getSinkFile().getAbsolutePath();
		SinkImpl<T> impl = SINKS.get(key);
		if(impl==null) {
			synchronized(SINKS) {
				impl = SINKS.get(key);
				if(impl==null) {
					impl = (SinkImpl<T>)builder.doBuild();
					SINKS.put(key, impl);
				}
			}
		}
		return impl;
	}
	
	private SinkRepo() {}

}
