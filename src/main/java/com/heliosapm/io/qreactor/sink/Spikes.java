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

import java.io.IOException;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heliosapm.utils.io.StdInCommandHandler;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * <p>Title: Spikes</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.io.qreactor.sink.Spikes</code></p>
 */

public class Spikes<T> {
	private static final Logger LOG = LoggerFactory.getLogger(Spikes.class);
	final EventProcessor eventProcessor = new EventProcessor();
	
	
	Spikes() {
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LOG.info("Running Sink Spike");
		Spikes<Integer> spikes =  new Spikes<>();
		Flux<Integer> flux = spikes.sink();
		spikes.runSink(flux);
		flux.doOnComplete(() -> {
			LOG.info("Bridge Flux Complete");
		});
		StdInCommandHandler.getInstance().run();
	}
	
	class EventProcessor {
		QSink<Integer> q = null;
		public void register(QSink<Integer> sink) {
			q = sink;
			IntStream.range(0, 10).parallel()
				.forEach(i -> {
					sink.onMessage(i);
				});
			try {
				sink.close();
			} catch (Exception x) {}
		}
	}
	
	private void runSink(Flux<Integer> flux) {
		sink();
		flux
		.doOnRequest(lv -> {
			LOG.info("CONSUMER REQUEST: {}", lv);
		})
		.doOnComplete(() -> {
			LOG.info("CONSUMER COMPLETE");
		})
		.doOnNext(i -> {
			LOG.info("CONSUMER NEXT: {}", i);
		})
		.doFinally(sig -> {
			LOG.info("CONSUMER TERMINATED: {}", sig);
		})
		.subscribe(i -> {
			LOG.info("CONSUMER: {}", i);
		});
	}
	
	private Flux<Integer> sink() {
		Flux<Integer> bridge = Flux.<Integer>create(sink -> {
			
			eventProcessor.register( 
		      new QSink<Integer>() {

				@Override
				public void close() throws IOException {
					sink.complete();
					LOG.info("Sink: QSink Closed");
					
				}

				@Override
				public void onMessage(Integer message) {
					LOG.info("Sink: Message: {}", message);
					sink.next(message);
				}

				@Override
				public void processComplete() {
					LOG.info("Sink: Process Complete");
					
				} 

//		        public void onDataChunk(Integer message) {
//		          for(String s : chunk) {
//		            sink.next(s); 
//		          }
//		        }
//
//		        public void processComplete() {
//		            sink.complete(); 
//		        }
		    });
		}).publishOn(Schedulers.parallel()).log();
		return bridge;
	}
	


}
