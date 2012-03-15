package org.eclipse.linuxtools.lttng.core.tests.headless;
/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *******************************************************************************/

import java.util.ArrayList;

import org.eclipse.linuxtools.internal.lttng.jni.common.JniTime;
import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniMarkerField;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceFactory;


@SuppressWarnings("nls")
public class JniTraceTest {
    
    public static void main(String[] args) {
        
    	// Path of the trace
        final String TRACE_PATH = "/home/william/trace-614601events-nolost-newformat";
        
        // *** Change this to run several time over the same trace
        final int NB_OF_PASS = 1;
        
        // *** Change this to true to parse all the events in the trace
        //	Otherwise, events are just read
        final boolean PARSE_EVENTS = true;
        
        
        // Work variables
        JniTrace tmptrace = null;
        JniEvent tmpevent = null;
        Long nbEvent = 0L;
        
        try {
        	// Get the trace from the Factory... 
        	//	This assume the path is correct and that the correct version of the lib is installed
            tmptrace = JniTraceFactory.getJniTrace(TRACE_PATH, null, false);
        	
            // Seek beginning
            tmptrace.seekToTime(new JniTime(0L));
            
            // Run up to "NB_OF_PASS" on the same trace
            for (int x=0; x<NB_OF_PASS; x++ ){
            	tmpevent = tmptrace.readNextEvent();
            	nbEvent++;
            	
            	while ( tmpevent != null ) {
	            	
	            	// Parse event if asked
	            	if ( PARSE_EVENTS ) {
		            	ArrayList<JniMarkerField> tmpFields = tmpevent.getMarkersMap().get(tmpevent.getEventMarkerId()).getMarkerFieldsArrayList();
		            	for ( int pos=0; pos<tmpFields.size(); pos++ ) {
		                    @SuppressWarnings("unused")
							Object newValue = tmpevent.parseFieldById(pos);
		                    
		                    // *** Uncomment the following to print the parsed content
		                    // Warning : this is VERY intensive
		                    //if ( pos == (tmpFields.size() -1) ) {
		                    //	tmptrace.printC(tmpevent.getEventPtr().getLibraryId(), tmpFields.get(pos).getField() + ":" + newValue + " ");
		                    //} else {
		                    //	tmptrace.printlnC(tmpevent.getEventPtr().getLibraryId(), tmpFields.get(pos).getField() + ":" + newValue + " ");
		                    //}	
		                }
	            	}
                    
	            	tmpevent = tmptrace.readNextEvent();
	                nbEvent++;
	            }
            }
            
            System.out.println("NB Events read : " + nbEvent);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
