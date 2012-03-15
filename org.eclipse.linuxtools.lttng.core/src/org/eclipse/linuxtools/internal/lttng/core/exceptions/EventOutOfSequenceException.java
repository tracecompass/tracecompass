/**
 * 
 */
package org.eclipse.linuxtools.internal.lttng.core.exceptions;

/**
 * @author francois
 *
 */
public class EventOutOfSequenceException extends Exception {

	private static final long serialVersionUID = -3537822357348706661L;

	public EventOutOfSequenceException(String errMsg) {
        super(errMsg);
    }
}
