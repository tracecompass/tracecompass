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
 *
 * Modifications:
 * 2010-07-16 Yuriy Vashchuk - Heritage correction.
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;

/**
 * <b><u>HistogramRequest</u></b>
 * <p>
 * Request class, to perform a request to TMF for the histograms.
 * <p>
 */
public class HistogramRequest extends TmfEventRequest<LttngEvent> {
/*	
	private HistogramContent histogramContent = null;
*/	
	
	private int 	lastInterval = 0;
	private long 	lastRangeTime = 0L;
	private long 	nbEventsInInterval = 0L;
	
	private int 	nbIntervalNotEmpty = 1;
	private int 	nbEventRead = 0;
	
	private int	lastDrawPosition = 0;
	
	private HistogramCanvas parentCanvas = null;
	
	private boolean	isCompleted = false;
	
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
	public HistogramRequest(TmfTimeRange range, int nbRequested, HistogramCanvas newParentCanvas, long timeInterval, ITmfDataRequest.ExecutionType execType) {
        super((Class<LttngEvent>)LttngEvent.class, range, nbRequested, HistogramConstant.MAX_EVENTS_PER_READ, execType);
        
    	setIsCompleted(false);
        
        // *** FIXME ***
        // This does not work! The request won't be processed or the number of events returned is wrong!
        // We cannot use this !
		//super((Class<LttngEvent>)dataType, range);
        
        parentCanvas = newParentCanvas;
        
        // Reset the content of the HistogramContent... the given data better be valid or this will fail.
        parentCanvas.getHistogramContent().clearContentData();
        parentCanvas.getHistogramContent().resetTable(range.getStartTime().getValue(), range.getEndTime().getValue(), timeInterval);
        
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
        LttngEvent[] result = getData();
        LttngEvent event = (result.length > 0) ? result[0] : null;
        
        // *** FIXME ***
    	// *** EVIL BUG ***
        // The request by timerange only does not work! (see constructor above) 
    	// 	However, the request with number of events will loop until it reach its number or EOF
    	//  We have to filter out ourself the extra useless events!
    	//
        if (event != null) {
        
        	LttngEvent tmpEvent = (LttngEvent) event;

//			Tracer.trace("Hst: " + event.getTimestamp());
        	
        	// This check is linked to the evil fix mentionned above
        	if ( ( tmpEvent.getTimestamp().getValue() >= parentCanvas.getHistogramContent().getStartTime() ) &&
        		 ( tmpEvent.getTimestamp().getValue() <= parentCanvas.getHistogramContent().getEndTime() ) )
        	{
        		
        		// Distance (in time) between this event and the last one we read
	        	long distance = ( tmpEvent.getTimestamp().getValue() - lastRangeTime );
				
	        	// Check if we changed of interval (the distance is higher than the interval time)
				if  ( distance > parentCanvas.getHistogramContent().getElementsTimeInterval() ) {
					
					parentCanvas.getHistogramContent().getElementByIndex(lastInterval).intervalNbEvents = nbEventsInInterval;
					lastRangeTime = tmpEvent.getTimestamp().getValue();
					
					// * NOTE *
					// We can skip several interval at once, so we need to find what was our interval now
					lastInterval = (int)((lastRangeTime - parentCanvas.getHistogramContent().getStartTime()) / parentCanvas.getHistogramContent().getElementsTimeInterval() );
					
					// *** HACK ***
					// Because of the threads, weird phenomenons seem to happen here, like a position after the 
					//	 element range because another request was issued.
					// This enforce the position but may result in slightly inconsistent result (i.e. a weird misplaced bar sometime).
					if ( lastInterval < 0 ) {
						lastInterval = 0;
					}
					else if ( lastInterval >= parentCanvas.getHistogramContent().getNbElement() ) {
						lastInterval = (parentCanvas.getHistogramContent().getNbElement()-1);
					}
					
					// * NOTE * 
					// We save the time we have here. This mean only the FIRST time read in an interval will be saved. 
					parentCanvas.getHistogramContent().getElementByIndex(lastInterval).firstIntervalTimestamp = lastRangeTime;
					parentCanvas.getHistogramContent().setReadyUpToPosition(lastInterval);
					
					nbIntervalNotEmpty++;
					nbEventsInInterval = 1L;
				}
				// We are still in the same interval, just keep counting
				else {
					nbEventsInInterval++;
				}
				
				if ( nbEventsInInterval > parentCanvas.getHistogramContent().getHeighestEventCount() ) {
					parentCanvas.getHistogramContent().setHeighestEventCount(nbEventsInInterval);
				}
				nbEventRead++;
				
				// Call an asynchronous redraw every REDRAW_EVERY_NB_EVENTS events
				// That way we don't need to wait until to end to have something on the screen
				if ( nbEventRead % HistogramConstant.REDRAW_EVERY_NB_EVENTS == 0 ) {
					redrawAsyncronously();
				}
        	}
		}
        // We got a null event! This mean we reach the end of the request. 
        // Save the last interval we had, so we won't miss the very last events at the end. 
        else {
        	// Save the last events
        	parentCanvas.getHistogramContent().getElementByIndex(lastInterval).intervalNbEvents = nbEventsInInterval;
        	// We reached the end of the request, so assume we fill up the content as well
        	parentCanvas.getHistogramContent().setReadyUpToPosition(parentCanvas.getHistogramContent().getNbElement());
			
			// If the interval wasn't null, count this as a "non empty" interval
			if (nbEventsInInterval > 0) {
				nbIntervalNotEmpty++;
			}
        }
    }
	
	/**
	 * Function that is called when the request completed (successful or not).<p>
	 * Update information and redraw the screen.
	 */
    @Override
    public void handleCompleted() {
    	setIsCompleted(true);
    	parentCanvas.notifyParentUpdatedInformationAsynchronously();
		redrawAsyncronously();
		super.handleCompleted();
//		System.out.println(System.currentTimeMillis() + ": HistogramView (" + ((getExecType() == ExecutionType.LONG) ? "long" : "short") + ") completed");
    }
    
//    /**
//	 * Function that is called when the request completed successfully.<p>
//	 */
//    @Override
//    public void handleSuccess() {
//    	// Nothing different from completed.
//    }
    
//    /**
//	 * Function that is called when the request completed in failure.<p>
//	 */
//    @Override
//    public void handleFailure() {
//    	// Nothing different from cancel.
//    }
    
    /**
	 * Function that is called when the request was cancelled.<p>
	 * Redraw and set the requestCompleted flag to true;
	 */
    @Override
    public void handleCancel() {
    	redrawAsyncronously();
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
    		averageNumberOfEvents = (int)Math.ceil((double)nbEventRead / (double)nbIntervalNotEmpty);
    	}
    	else {
    		averageNumberOfEvents = (int)Math.ceil((double)nbEventRead / (double)parentCanvas.getHistogramContent().getNbElement());
    	}
    	
    	parentCanvas.getHistogramContent().setAverageNumberOfEvents(averageNumberOfEvents);
    	
    	// It is possible that the height factor didn't change; 
    	//		If not, we only need to redraw the updated section, no the whole content
    	// Save the actual height, recalculate the height and check if there was any changes
    	double previousHeightFactor = parentCanvas.getHistogramContent().getHeightFactor();
    	parentCanvas.getHistogramContent().recalculateHeightFactor();
    	if ( parentCanvas.getHistogramContent().getHeightFactor() != previousHeightFactor ) {
    		parentCanvas.getHistogramContent().recalculateEventHeight();
    	}
    	else {
    		parentCanvas.getHistogramContent().recalculateEventHeightInInterval(lastDrawPosition, parentCanvas.getHistogramContent().getReadyUpToPosition());
    	}
    	
    	lastDrawPosition = parentCanvas.getHistogramContent().getReadyUpToPosition();
    }
    
    /**
	 * Perform an asynchonous redraw of the screen.
	 */
    public void redrawAsyncronously() {
    	updateEventsInfo();
    	// Canvas redraw is already asynchronous
    	parentCanvas.redrawAsynchronously();
    }

	/**
	 * Getter for isCompleted variable
	 * @return true if the request is completed
	 */
	public boolean getIsCompleted() {
		return isCompleted;
	}

	/**
	 * Setter for isCompleted variable
	 * @param isCompleted value to set the completed flag
	 */
	public void setIsCompleted(boolean isCompleted) {
		this.isCompleted = isCompleted;
	}
    
}
