/**
 * 
 */
package com.heliosapm.io.qreactor.ringbuffer;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.lmax.disruptor.WaitStrategy;
/**
 * @author nwhitehead
 *
 */
public interface WaitStrategyFactory {

	/** The config prop for the raw ring buffer wait strategy */
	public static final String CONF_WAIT_STRAT = "waitstrat.agile.slow";	
	/** The default raw ring buffer wait strategy */
	public static final RBWaitStrategy DEFAULT_WAIT_STRAT = RBWaitStrategy.YIELD;
	
	
	
	/** The config prop for the agile slow wait strategy */
	public static final String CONF_AGILE_SLOW = "waitstrat.agile.slow";	
	/** The default agile slow wait strategy */
	public static final RBWaitStrategy DEFAULT_AGILE_SLOW = RBWaitStrategy.BLOCK;

	/** The config prop for the agile fast wait strategy */
	public static final String CONF_AGILE_FAST = "waitstrat.agile.fast";	
	/** The default agile slow wait strategy */
	public static final RBWaitStrategy DEFAULT_AGILE_FAST = RBWaitStrategy.YIELD;
	
	/** The config prop for the park wait strategy park time in nanos */
	public static final String CONF_PARK_TIME = "waitstrat.parkwait.time";	
	/** The default park wait strategy park time in nanos */
	public static final long DEFAULT_PARK_TIME = 250;

	/** The config prop for the sleep wait strategy retry count */
	public static final String CONF_SLEEP_RETRIES = "waitstrat.sleepwait.time";	
	/** The default sleep wait strategy retry count */
	public static final int DEFAULT_SLEEP_RETRIES = 200;

	/** The config prop for the timeout wait strategy timeout */
	public static final String CONF_TIMEOUT_TIME = "waitstrat.timeoutwait.time";	
	/** The default timeout wait strategy timeout */
	public static final long DEFAULT_TIMEOUT_TIME = 10;
	
	/** The config prop for the timeout wait strategy timeout unit */
	public static final String CONF_TIMEOUT_UNIT = "waitstrat.timeoutwait.unit";	
	/** The default timeout wait strategy timeout unit */
	public static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.MILLISECONDS;
	

	/** The config prop for the phased wait strategy spin timeout */
	public static final String CONF_PHASED_SPIN = "waitstrat.phased.spintime";	
	/** The default phased wait strategy spin timeout */
	public static final long DEFAULT_PHASED_SPIN = 10;
	
	/** The config prop for the phased wait strategy yield timeout */
	public static final String CONF_PHASED_YIELD = "waitstrat.phased.yieldtime";	
	/** The default phased wait strategy yield timeout */
	public static final long DEFAULT_PHASED_YIELD = 10;
	
	/** The config prop for the phased wait strategy timeout unit */
	public static final String CONF_PHASED_UNIT = "waitstrat.phased.unit";	
	/** The default phased wait strategy timeout unit */
	public static final TimeUnit DEFAULT_PHASED_UNIT = TimeUnit.MILLISECONDS;

	/** The config prop for the phased wait strategy fallback strategy */
	public static final String CONF_PHASED_FALLBACK = "waitstrat.phased.fallback";	
	/** The default phased wait strategy fallback strategy */
	public static final RBWaitStrategy DEFAULT_PHASED_FALLBACK = RBWaitStrategy.YIELD;
	
	/**
	 * Creates a new WaitStrategy instance
	 * @param properties The optional configuration
	 * @return a new WaitStrategy instance
	 */
	public WaitStrategy waitStrategy(final Properties properties);
}

