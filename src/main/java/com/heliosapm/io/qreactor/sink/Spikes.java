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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Title: Spikes</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.io.qreactor.sink.Spikes</code></p>
 */

public class Spikes<T> {
	private static final Logger LOG = LoggerFactory.getLogger(Spikes.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

	}
	
	
	private void sink() {
		Flux<Integer> bridge = Flux.create(sink -> {
		    myEventProcessor.register( 
		      new MyEventListener<String>() { 

		        public void onDataChunk(Integer message) {
		          for(String s : chunk) {
		            sink.next(s); 
		          }
		        }

		        public void processComplete() {
		            sink.complete(); 
		        }
		    });
		});
	}
	
	interface MyEventListener<T> {
	    void onMessage(T message);
	    void processComplete();
	}	

}
