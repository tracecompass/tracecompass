/**
 * 
 */
package org.eclipse.linuxtools.internal.lttng.stubs;

import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;

/**
 * @author francois
 *
 */
@SuppressWarnings("nls")
public class LTTngTimestampStub extends TmfTimestamp {

    /**
	 * 
	 */
	@SuppressWarnings("unused")
    private static final long serialVersionUID = 216576768443708259L;

	/**
     * @param value
     * @param scale
     * @param precision
     */
    public LTTngTimestampStub(long value) {
		super(value, (byte) -3, 0);	// millisecs
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.event.TmfTimestamp#toString()
	 */
	@Override
    public String toString() {

	    long value = getValue();
	    int scale = getScale();
	    
		// If we are dealing with units of seconds (or higher),
		// use the plain formatter
		if (scale >= 0) {
	    	Double dvalue = value * Math.pow(10, scale);
	        return dvalue.toString();
		}

		// Define a format string
        String format = String.format("%%1d.%%0%dd", -scale);

        // And format the timestamp value
        double dscale = Math.pow(10, scale);
        long seconds = (long) (value * dscale);
        long fracts  = value - (long) ((double) seconds / dscale); 
        String result = String.format(format, seconds, fracts);

        return result;
    }
}
