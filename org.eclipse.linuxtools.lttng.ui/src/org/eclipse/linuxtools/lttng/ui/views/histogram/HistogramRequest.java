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
	
	private Integer lastInterval = 0;
	private Long 	lastRangeTime = 0L;
	private Long 	nbEventsInInterval = 1L;
	
	private Integer nbIntervalNotEmpty = 1;
	private Integer nbEventRead = 0;
	
	private Integer	lastDrawPosition = 0;
	
	private HistogramCanvas parentCanvas = null;
	
	private Boolean requestCompleted = false;
	
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
	public HistogramRequest(TmfTimeRange range, Integer nbRequested, HistogramCanvas newParentCanvas, Long timeInterval) {
        super((Class<LttngEvent>)LttngEvent.class, range, nbRequested, HistogramConstant.MAX_EVENTS_PER_READ);
        
        // *** FIXME ***
        // This does not work! The request won't be processed or the number of events returned is wrong!
        // We cannot use this !
		//super((Class<LttngEvent>)dataType, range);
        
        parentCanvas = newParentCanvas;
        histogramContent = parentCanvas.getHistogramContent();
        
        // Reset the content of the HistogramContent... the given data better be valid or this will fail.
        histogramContent.clearContentData();
        histogramContent.resetTable(range.getStartTime().getValue(), range.getEndTime().getValue(), timeInterval);
        
        lastRangeTime = range.getStartTime().getValue();
        
        // Notify the UI even before the request started, so we set the timestamp already.
        parentCanvas.notifyParentUpdatedInformationAsynchronously();
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
        	if ( ( tmpEvent.getTimestamp().getValue() >= histogramContent.getStartTime() ) &&
        		 ( tmpEvent.getTimestamp().getValue() <= histogramContent.getEndTime() ) )
        	{
        		
        		// Distance (in time) between this event and the last one we read
	        	long distance = ( tmpEvent.getTimestamp().getValue() - lastRangeTime );
				
	        	// Check if we changed of interval (the distance is higher than the interval time)
				if  ( distance > histogramContent.getElementsTimeInterval() ) {
					
					histogramContent.getElementByIndex(lastInterval).intervalNbEvents = nbEventsInInterval;
					lastRangeTime = tmpEvent.getTimestamp().getValue();
					
					// * NOTE *
					// We can skip several interval at once, so we need to find what was our interval now
					lastInterval = (int)((lastRangeTime - histogramContent.getStartTime()) / histogramContent.getElementsTimeInterval() );
					
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
					// We save the time we have here. This mean only the FIRST time read in an interval will be saved. 
					histogramContent.getElementByIndex(lastInterval).firstIntervalTimestamp = lastRangeTime;
					histogramContent.setReadyUpToPosition(lastInterval);
					
					nbIntervalNotEmpty++;
					nbEventsInInterval = 1L;
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
        		//System.out.println("Requested Timerange is : " + histogramContent.getStartTime() + " / " + histogramContent.getEndTime());
        		//System.out.println("Time is : " + tmpEvent.getTimestamp().getValue());
        		// *** FIXME ***
            	// *** EVIL FIX ***
                // Because of the other evil bug (see above), we have to ignore extra useless events we will get
        		// However, we might be far away from the end so we better start a redraw now
        		if (tmpEvent.getTimestamp().getValue() >= histogramContent.getEndTime()) {
	        		redrawAsyncronously();
	        		requestCompleted = true;
        		}
        	}
		}
    }
	
	/**
	 * Function that is called when the request completed (successful or not).<p>
	 * Update information and redraw the screen.
	 */
    @Override
    public void handleCompleted() {
    	parentCanvas.notifyParentUpdatedInformationAsynchronously();
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
	 * This will perform some calculation that might be a bit harsh so it should'nt be called too often.
	 */
    public void updateEventsInfo() {
    	// *** Note *** 
    	// The average number of event is calculated while skipping empty interval if asked
    	int averageNumberOfEvents = 0;
    	if ( HistogramConstant.SKIP_EMPTY_INTERVALS_WHEN_CALCULATING_AVERAGE ) {
    		averageNumberOfEvents = nbEventRead / nbIntervalNotEmpty;
    	}
    	else {
    		averageNumberOfEvents = nbEventRead / histogramContent.getNbElement();
    	}
    	histogramContent.setAverageNumberOfEvents(averageNumberOfEvents);
    	
    	// It is possible that the height factor didn't change; 
    	//		If not, we only need to redraw the updated section, no the whole content
    	// Save the actual height, recalculate the height and check if there was any changes
    	double previousHeightFactor = histogramContent.getHeightFactor();
    	histogramContent.recalculateHeightFactor();
    	if ( histogramContent.getHeightFactor() != previousHeightFactor ) {
			histogramContent.recalculateEventHeight();
    	}
    	else {
    		histogramContent.recalculateEventHeightInInterval(lastDrawPosition, histogramContent.getReadyUpToPosition());
    	}
    	
    	lastDrawPosition = histogramContent.getReadyUpToPosition();
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
