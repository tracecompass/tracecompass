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
	final static int MAX_EVENTS_PER_READ = 1;
	final static int REDRAW_EVERY_NB_EVENTS = 10000;
	final static boolean SKIP_EMPTY_INTERVALS_WHEN_CALCULATING_AVERAGE = true;
	
	
	// Constant relative to the content
	final static double DEFAULT_DIFFERENCE_TO_AVERAGE = 1000.0;
	
	
	
	// Constants relative to zoom. Factors need to be a percentage ( 0 < factors < 1 )
	final static Double ZOOM_IN_FACTOR = 0.1;
	final static Double ZOOM_OUT_FACTOR = 0.1;
	
	
	// Constants relative to wait time while listening for scroll events
	// "FULL" is time to wait to stop "to count" mouse scroll click events
	// "INTERVAL" is time to wait between polling for scroll click events
	final static Long FULL_WAIT_MS_TIME_BETWEEN_MOUSE_SCROLL = 500L;
	final static Long INTERVAL_WAIT_MS_TIME_BETWEEN_POLL = 100L;
	
	
	// Constants relative to the displacement in the trace
	// Factor represent a number of HistogramContent interval
	// Multiple is the factor to multiply to basic during "fast" displacement 
	final static Integer BASIC_DISPLACEMENT_FACTOR = 1;
	final static Double  FAST_DISPLACEMENT_MULTIPLE = 10.0;
	
	
	// Constants relative to the drawing of the Histogram
	// Colors for the histogram. Background should be the same as the background in use
	final static Integer EMPTY_BACKGROUND_COLOR = SWT.COLOR_WHITE;
	final static Integer HISTOGRAM_BARS_COLOR = SWT.COLOR_DARK_CYAN;
	final static Integer SELECTION_WINDOW_COLOR = SWT.COLOR_RED;
	
	// Dimension for the line of the "Selection Window"
	final static Integer MINIMUM_WINDOW_WIDTH = 3;
	final static Integer SELECTION_LINE_WIDTH = 2;
	final static Integer SELECTION_CROSSHAIR_LENGTH = 3;
	
	public static String formatNanoSecondsTime(Long nanosecTime) {
		String returnedTime = nanosecTime.toString();
		
		if ( returnedTime.length() > 9 ) {
			returnedTime = returnedTime.substring(0, returnedTime.length() - 9 ) + "." + returnedTime.substring( returnedTime.length() - 9 );
		}
		else {
			int curSize = returnedTime.length();
			for (int l=0; (curSize+l)< 9; l++) {
				returnedTime = "0" + returnedTime;
			}
			returnedTime = "0." + returnedTime;
		}
		
		return returnedTime;
	}
	
	public static Integer getTextSizeInControl(Composite parent, String text) {
		GC graphicContext = new GC(parent);
        int textSize = 0;
        for ( int pos=0; pos<text.length(); pos++ ) {
        	textSize += graphicContext.getAdvanceWidth( text.charAt(pos) );
        }
        textSize += graphicContext.getAdvanceWidth( ' ' );
        
        return textSize;
	}
}
