/**
 * 
 */
package com.loyaltymethods.fx.log.util;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * This is a very simple filter based on logger name matching
 * <p>The filter admits three options <b>LoggerToMatch</b>, <b>ExactMatch</b> and
 * <b>AcceptOnMatch</b>. If there is an exact match between the value
 * of the <b>LoggerToMatch</b> option and the logger name of the {@link
 * LoggingEvent}, then the {@link #decide} method returns {@link
 * Filter#NEUTRAL} in case the <b>AcceptOnMatch</b> option value is set
 * to <code>true</code>, if it is <code>false</code> then {@link
 * Filter#DENY} is returned. If there is no match, {@link
 * Filter#DENY} is returned.
 * 
 * @author Ravi
 *
 */
public class LoggerMatchFilter extends Filter {
	/**
    Do we return ACCEPT when a match occurs. Default is
    <code>true</code>.  */
	boolean acceptOnMatch = true;
	/**
	 * The value to match in the NDC value of the LoggingEvent.
	 */
	String loggerToMatch;

	/**
	 * Do we look for an exact match or just a "contains" match?
	 */
	boolean exactMatch = true;
	
	public LoggerMatchFilter() {
		super();
	}

	@Override
	public int decide(LoggingEvent loggingEvent) {
		String loggerName = loggingEvent.getLoggerName();
	    if(this.getLoggerToMatch() == null ||  loggerName == null) {
		      return Filter.DENY;		
	    }
		if((isExactMatch() && loggerName.equalsIgnoreCase(this.getLoggerToMatch()))
				|| (!isExactMatch() && loggerName.indexOf(this.getLoggerToMatch()) != -1)) {
			// We've got a match
			if(this.isAcceptOnMatch()) {
				return Filter.NEUTRAL;
			}
			return Filter.DENY;
		}
		// No match
		return Filter.DENY;
	}


	@Override
	protected LoggerMatchFilter clone() throws CloneNotSupportedException {
		LoggerMatchFilter copy = new LoggerMatchFilter();
		copy.setAcceptOnMatch(this.isAcceptOnMatch());
		copy.setExactMatch(this.isExactMatch());
		copy.setNext(this.getNext());
		if(!this.isExactMatch()){
			copy.setLoggerToMatch(this.getLoggerToMatch());
		}
		return copy;
	}
	
	@Override
	public String toString() {
		return "LoggerMatchFilter [acceptOnMatch=" + acceptOnMatch
				+ ", loggerToMatch=" + loggerToMatch + ", exactMatch="
				+ exactMatch + ", getNext()=" + getNext() + "]";
	}

	public boolean isAcceptOnMatch() {
		return acceptOnMatch;
	}

	public void setAcceptOnMatch(boolean acceptOnMatch) {
		this.acceptOnMatch = acceptOnMatch;
	}

	public String getLoggerToMatch() {
		return loggerToMatch;
	}

	public void setLoggerToMatch(String loggerToMatch) {
		this.loggerToMatch = loggerToMatch;
	}

	public boolean isExactMatch() {
		return exactMatch;
	}

	public void setExactMatch(boolean exactMatch) {
		this.exactMatch = exactMatch;
	}
}
