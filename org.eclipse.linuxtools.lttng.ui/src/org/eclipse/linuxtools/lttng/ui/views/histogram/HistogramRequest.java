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

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;

/**
 * <b><u>HistogramRequest</u></b>
 * <p>
 * Request class, to perform a request to TMF for the histograms.
 * <p>
 */
public class HistogramRequest extends TmfEventRequest<LttngEvent> {
	private HistogramContent histogramContent = null;
	
	private int  lastInterval = 0;
	private long lastRangeTime = 0L;
	private long nbEventsInInterval = 1;
	
	private int  nbIntervalNotEmpty = 1;
	private int  nbEventRead = 0;
	
	private HistogramCanvas parentCanvas = null;
	
	private boolean requestCompleted = false;
	
	/**
	 * Constructor for HistogramRequest.<p>
	 * Prepare the request in TMF and reset the histogram content.
	 * 
	 * @param range				Range of the request.
	 * @param nbRequested		Nb events requested. Can be "Infinity" for all.
	 * @param newParentCanvas	HistogramCanvas related to the request.
	 * @param timeInterval		Time interval to consider (i.e. : 1 interval is 1 bar in the histogram)
	 * 
	 * @see org.eclipse.linuxtools.tmf.request.TmfEventRequest
	 */
	public HistogramRequest(TmfTimeRange range, int nbRequested, HistogramCanvas newParentCanvas, Long timeInterval) {
        super((Class<LttngEvent>)LttngEvent.class, range, nbRequested, HistogramConstant.MAX_EVENTS_PER_READ);
        
        // *** FIXME ***
        // This does not work! The request won't be processed or the number of events returned is wrong!
        // We cannot use this !
		//super((Class<LttngEvent>)dataType, range);
		
        parentCanvas = newParentCanvas;
        histogramContent = parentCanvas.getHistogramContent();
        
        // Reset the content of the HistogramContent... the given data better be valid or this will fail.
        histogramContent.resetContentData();
        histogramContent.setStartTime(range.getStartTime().getValue());
        histogramContent.setEndTime(range.getEndTime().getValue());
        histogramContent.setIntervalTime(timeInterval);
        histogramContent.resetTable();
        
        lastRangeTime = histogramContent.getStartTime();
    }
	
	/**
	 * HandleData function : will be called by TMF each time a new event is receive for the request.<p>
	 * Calculation for the content is done here.
	 */
	@Override
    public void handleData() {
        TmfEvent[] result = getData();
        TmfEvent[] evt = new TmfEvent[1];
        
        evt[0] = (result.length > 0) ? result[0] : null;
        
        // *** FIXME ***
    	// *** EVIL BUG ***
        // The request by timerange only does not work! (see constructor above) 
    	// 	However, the request with number of events will loop until it reach its number or EOF
    	//  We have to filter out ourself the extra useless events!
    	//
        if ( (evt[0] != null) && (requestCompleted == false) ) {
        	LttngEvent tmpEvent = (LttngEvent)evt[0];
        	
        	// This check is linked to the evil fix mentionned above
        	if ( tmpEvent.getTimestamp().getValue() <= histogramContent.getEndTime() ) {
        		
        		// Distance (in time) between this event and the last one we read
	        	long distance = ( tmpEvent.getTimestamp().getValue() - lastRangeTime );
				
	        	// Check if we changed of interval (the distance is higher than the interval time)
				if  ( distance > histogramContent.getIntervalTime() ) {
					
					histogramContent.getElementByIndex(lastInterval).intervalNbEvents = nbEventsInInterval;
					lastRangeTime = tmpEvent.getTimestamp().getValue();
					
					// * NOTE *
					// We can skip several interval at once, so we need to find what was our interval now
					lastInterval = (int)((lastRangeTime - histogramContent.getStartTime()) / histogramContent.getIntervalTime() );
					
					// *** HACK ***
					// Because of the threads, weird phenomenons seem to happen here, like a position after the 
					//	 element range because another request was issued.
					// This enforce the position but may result in slightly inconsistent result (i.e. a weird misplaced bar sometime).
					if ( lastInterval < 0 ) {
						lastInterval = 0;
					}
					else if ( lastInterval >= histogramContent.getNbElement() ) {
						lastInterval = (histogramContent.getNbElement()-1);
					}
					
					// * NOTE * 
					// We save the time we have. This mean only the FIRST time read in an interval will be saved. 
					histogramContent.getElementByIndex(lastInterval).firstIntervalTimestamp = lastRangeTime;
					histogramContent.setReadyUpToPosition(lastInterval);
					
					nbIntervalNotEmpty++;
					nbEventsInInterval = 1;
				}
				// We are still in the same interval, just keep counting
				else {
					nbEventsInInterval++;
					if ( nbEventsInInterval > histogramContent.getHeighestEventCount() ) {
						histogramContent.setHeighestEventCount(nbEventsInInterval);
					}
				}
				
				nbEventRead++;
				
				// Call an asynchronous redraw every REDRAW_EVERY_NB_EVENTS events
				// That way we don't need to wait until to end to have something on the screen
				if ( nbEventRead % HistogramConstant.REDRAW_EVERY_NB_EVENTS == 0 ) {
					redrawAsyncronously();
				}
        	}
        	else {
        		// *** FIXME ***
            	// *** EVIL FIX ***
                // Because of the other evil bug (see above), we have to ignore extra useless events we will get
        		// However, we might be far away from the end so we better start a redraw now
        		redrawAsyncronously();
        		requestCompleted = true;
        		
        		// Althought it won't do anything, try to call control functions to stop the request
        		done();
        		cancel();
        		fail();
        	}
		}
    }
	
	/**
	 * Function that is called when the request completed (successful or not).<p>
	 * Update information and redraw the screen.
	 */
    @Override
    public void handleCompleted() {
    	parentCanvas.canvasRedrawer.asynchronousNotifyParentUpdatedInformation();
		redrawAsyncronously();
    }
    
    /**
	 * Function that is called when the request completed successfully.<p>
	 */
    @Override
    public void handleSuccess() {
    	// Nothing different from completed.
    }
    
    /**
	 * Function that is called when the request completed in failure.<p>
	 */
    @Override
    public void handleFailure() {
    	// Nothing different from cancel.
    }
    
    /**
	 * Function that is called when the request was cancelled.<p>
	 * Redraw and set the requestCompleted flag to true;
	 */
    @Override
    public void handleCancel() {
    	redrawAsyncronously();
		requestCompleted = true;
    }
	
    /**
	 * Update the HistogramContent with the latest information.<p>
	 * This will perform some calculation that might be a bit harsh so it shouldnt be called too often.
	 */
    public void updateEventsInfo() {
    	int averageNumberOfEvents = nbEventRead / nbIntervalNotEmpty;
		histogramContent.setAverageNumberOfEvents(averageNumberOfEvents);
		histogramContent.recalculateEventHeight();
    }
    
    /**
	 * Perform an asynchonous redraw of the screen.
	 */
    public void redrawAsyncronously() {
    	updateEventsInfo();
    	// Canvas redraw is already asynchronous
    	parentCanvas.redrawAsynchronously();
    }
    
}
