package org.eclipse.linuxtools.internal.lttng.core.exceptions;

/**
 * <b><u>LttngException</u></b>
 * <p>
 * Super exception class for Lttng
 */
public class LttngException extends Exception {
	static final long serialVersionUID = 4016530589556719360L;

    public LttngException(String errMsg) {
        super(errMsg);
    }
}
