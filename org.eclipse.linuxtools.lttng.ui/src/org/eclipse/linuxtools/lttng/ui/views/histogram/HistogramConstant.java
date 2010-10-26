/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;

/**
 * <b><u>HistogramConstant</u></b>
 * <p>
 * Empty interface class to hold the different constants needed by the histogram.
 * <p>
 */
public abstract class HistogramConstant {
	// Constants relative to requests
	public final static int MAX_EVENTS_PER_READ = 1;
	public final static int REDRAW_EVERY_NB_EVENTS = 10000;
	public final static Boolean SKIP_EMPTY_INTERVALS_WHEN_CALCULATING_AVERAGE = true;
	
	
	// Constant relative to the content
	public final static double DEFAULT_DIFFERENCE_TO_AVERAGE = 1000.0;
	
	
	
	// Constants relative to zoom. Factors need to be a percentage ( 0 < factors < 1 )
	public final static double ZOOM_IN_FACTOR = 0.1;
	public final static double ZOOM_OUT_FACTOR = 0.1;
	
	
	// Constants relative to wait time while listening for scroll events
	// "FULL" is time to wait to stop "to count" mouse scroll click events
	// "INTERVAL" is time to wait between polling for scroll click events
	public final static long FULL_WAIT_MS_TIME_BETWEEN_MOUSE_SCROLL = 1000L;
	public final static long INTERVAL_WAIT_MS_TIME_BETWEEN_POLL = 100L;
	
	
	// Constants relative to the displacement in the trace
	// Factor represent a number of HistogramContent interval
	// Multiple is the factor to multiply to basic during "fast" displacement 
	public final static int BASIC_DISPLACEMENT_FACTOR = 1;
	public final static double  FAST_DISPLACEMENT_MULTIPLE = 10.0;
	
	
	// Constants relative to the drawing of the Histogram
	// Colors for the histogram. Background should be the same as the background in use
	public final static int EMPTY_BACKGROUND_COLOR = SWT.COLOR_WHITE;
	public final static int SELECTED_EVENT_COLOR = SWT.COLOR_RED;
	
	// Dimension for the line of the "Selection Window"
	public final static int MINIMUM_WINDOW_WIDTH = 3;
	public final static int SELECTION_LINE_WIDTH = 1;
	public final static int SELECTION_CROSSHAIR_WIDTH = 1;
	
	
	/**
	 * Method to format a long representing nanosecond into a proper String.<p>
	 * The returned String will always be like "0.000000000", missing decimal will be added.
	 * 
	 * @param nanosecTime	This time to format
	 * 
	 * @return	The formatted string
	 */
	public static String formatNanoSecondsTime(long nanosecTime) {
		String returnedTime = Long.toString(nanosecTime);
		
		// If our number has over 9 digits, just add a dot after the ninth digits
		if ( returnedTime.length() > 9 ) {
			returnedTime = returnedTime.substring(0, returnedTime.length() - 9 ) + "." + returnedTime.substring( returnedTime.length() - 9 );
		}
		// Otherwise, patch missing decimal with 0
		else {
			int curSize = returnedTime.length();
			for (int l=0; (curSize+l)< 9; l++) {
				returnedTime = "0" + returnedTime;
			}
			returnedTime = "0." + returnedTime;
		}
		
		return returnedTime;
	}
	
	/**
	 * Convert a String representing nanoseconds into a valid long.<p>
	 * This can handle number like "0.5", "0.123456789" as well as plain number like "12".<p>
	 * 
	 * Note : This function ALWAYS return a number, if conversion failed, 0 will be returned.<p>
	 * 
	 * @param timeString	The string to convert
	 * 
	 * @return				The converted nanoseconds time as long
	 */
	public static long convertStringToNanoseconds( String timeString ) {
		long returnedNumber = 0L;
		
	    try {
	    	// Avoid simple commat/dot mistake
	        timeString = timeString.replace(",", ".");
	
	        // If we have a dot, we have a decimal number to convert
	        int dotPosition = timeString.indexOf(".");
	        
	        // If the user begun the line with a dot, we add a zero
	        if ( dotPosition == 0 ) {
                timeString = "0" + timeString;
                dotPosition = 1;
	        }
	        
	        // If we found a dot, verify that we have 9 digits
	        if ( dotPosition != -1 ) {
                int decimalNumber = (timeString.length() - dotPosition -1);
                
                // If we have less than 9 digits, we fill with 0
                if ( decimalNumber <= 9 ) {
                	StringBuffer strBuffer = new StringBuffer(timeString);
                    for ( int nbDec=decimalNumber; nbDec<9; nbDec++) {
                    	strBuffer.append("0");
                    }
                    timeString = strBuffer.toString();
                }
                // We have OVER 9 digits, skip the useless part
                else {
                	timeString = timeString.substring(dotPosition, 9);
                }
	        }
	        
	        // Conversion into decimal seconds
	        double dblMaxTimerange = Double.parseDouble(timeString);
	        // Conversion into nanoseconds
	        returnedNumber = (long)(dblMaxTimerange * 1000000000.0);
	    }
	    catch (NumberFormatException e) {
	        System.out.println("Warning : Could not convert string into nanoseconds (convertStringToLong)");
	    }
	    
	    return returnedNumber;
    }
	
	/**
	 * Calculate the correcte width of a String.<p>
	 * Useful to set a control to its maximum size; since the size depends on characters, 
	 * 		this will calculate the correct sum... should be platform independant (we hope).
	 * 
	 * @param parent	Parent control we will use as a reference. Could be any composite.
	 * @param text		The Text to measure the size from
	 * 
	 * @return			The size calculated.
	 */
	public static int getTextSizeInControl(Composite parent, String text) {
		GC graphicContext = new GC(parent);
        int textSize = 0;
        for ( int pos=0; pos<text.length(); pos++ ) {
        	textSize += graphicContext.getAdvanceWidth( text.charAt(pos) );
        }
        // Add an extra space in case there was trailing whitespace in the message
        textSize += graphicContext.getAdvanceWidth( ' ' );
        
        return textSize;
	}
}
