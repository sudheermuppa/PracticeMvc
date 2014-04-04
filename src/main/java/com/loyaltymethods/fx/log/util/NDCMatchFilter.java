/**
 * 
 */
package com.loyaltymethods.fx.log.util;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A Nested Diagnostic Context, or NDC in short, is an instrument to distinguish interleaved log output from different sources. 
 * Log output is typically interleaved when a server handles multiple clients near-simultaneously. Interleaved log output can 
 * still be meaningful if each log entry from different contexts had a distinctive stamp.
 * 
 * The NDCMatchFilter matches the supplied NDC value against the 
 * NDC value of a logging event
 * <p>The filter admits three options <b>ValueToMatch</b>, <b>ExactMatch</b> and
 * <b>AcceptOnMatch</b>. If there is a match between the value
 * of the <b>ValueToMatch</b> option and the NDC value of the {@link
 * LoggingEvent}, then the {@link #decide} method returns {@link
 * Filter#NEUTRAL} in case the <b>AcceptOnMatch</b> option value is set
 * to <code>true</code>, if it is <code>false</code> then {@link
 * Filter#DENY} is returned. If there is no match, {@link
 * Filter#DENY} is returned.
 * 
 * @author Ravi
 *
 */
public class NDCMatchFilter extends Filter {
	/**
    Do we return ACCEPT when a match occurs. Default is
    <code>true</code>.  */
	boolean acceptOnMatch = true;
	/**
	 * The value to match in the NDC value of the LoggingEvent.
	 */
	String valueToMatch;

	/**
	 * Do we look for an exact match or just a "contains" match?
	 */
	boolean exactMatch = true;
	
	public NDCMatchFilter() {
		super();
	}

	@Override
	public int decide(LoggingEvent loggingEvent) {
		String ndcValue = loggingEvent.getNDC();
	    if(this.getValueToMatch() == null ||  ndcValue == null) {
		      return Filter.DENY;		
	    }
		if((isExactMatch() && ndcValue.equalsIgnoreCase(this.getValueToMatch()))
				|| (!isExactMatch() && ndcValue.indexOf(this.getValueToMatch()) != -1)) {
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
	protected NDCMatchFilter clone() throws CloneNotSupportedException {
		NDCMatchFilter copy = new NDCMatchFilter();
		copy.setAcceptOnMatch(this.isAcceptOnMatch());
		copy.setExactMatch(this.isExactMatch());
		copy.setNext(this.getNext());
		if(!this.isExactMatch()){
			copy.setValueToMatch(this.getValueToMatch());
		}
		return copy;
	}

	@Override
	public String toString() {
		return "NDCMatchFilter [acceptOnMatch=" + acceptOnMatch
				+ ", valueToMatch=" + valueToMatch + ", exactMatch="
				+ exactMatch + ", getNext()=" + getNext() + "]";
	}

	public boolean isAcceptOnMatch() {
		return acceptOnMatch;
	}

	public void setAcceptOnMatch(boolean acceptOnMatch) {
		this.acceptOnMatch = acceptOnMatch;
	}

	public String getValueToMatch() {
		return valueToMatch;
	}

	public void setValueToMatch(String valueToMatch) {
		this.valueToMatch = valueToMatch;
	}

	public boolean isExactMatch() {
		return exactMatch;
	}

	public void setExactMatch(boolean exactMatch) {
		this.exactMatch = exactMatch;
	}	
}
