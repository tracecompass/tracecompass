package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;
import org.eclipse.swt.widgets.Display;

public class HistogramRequest extends TmfEventRequest<LttngEvent> {
	
	final static int MAX_EVENTS_PER_REQUEST = 1;
	final static int REDRAW_EVERY_NB_EVENTS = 10000;
	
	private HistogramContent histogramContent = null;
	
	private int lastPos = 0;
	private long lastRangeTime = 0L;
	private long nbEventsInRange = 1;
	
	private int  nbPosNotEmpty = 1;
	private int  nbEventRead = 0;
	
	private TraceCanvas parentCanvas = null;
	
	private boolean requestCompleted = false;
	
	@SuppressWarnings("unchecked")
	public HistogramRequest(Class<? extends TmfEvent> dataType, TmfTimeRange range, int nbRequested, HistogramContent newContent, TraceCanvas newParentCanvas, Long timeInterval) {
        super((Class<LttngEvent>)dataType, range, nbRequested, MAX_EVENTS_PER_REQUEST);
        
        // *** FIXME ***
        // This does not work! The request won't be processed or the number of events returned is wrong!
        // We cannot use this !
		//super((Class<LttngEvent>)dataType, range);
		
        histogramContent = newContent;
        parentCanvas = newParentCanvas;
        
        histogramContent.resetContentData();
        histogramContent.setStartTime(range.getStartTime().getValue());
        histogramContent.setEndTime(range.getEndTime().getValue());
        histogramContent.setIntervalTime(timeInterval);
        histogramContent.resetTable();
        
        lastRangeTime = histogramContent.getStartTime();
    }

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
        	
	        	long distance = ( tmpEvent.getTimestamp().getValue() - lastRangeTime );
				
				if  ( distance > histogramContent.getIntervalTime() ) {
					
					histogramContent.getElementByIndex(lastPos).intervalNbEvents = nbEventsInRange;
					lastRangeTime = tmpEvent.getTimestamp().getValue();
					
					lastPos = (int)((lastRangeTime - histogramContent.getStartTime()) / histogramContent.getIntervalTime() );
					
					// *** HACK ***
					// Because of the threads, weird phenomenons seem to happen here, like a position after the 
					//	 element range because another request was issued.
					// This enforce the position but may result in slightly inconsistent result (i.e. a weird misplaced bar sometime).
					if ( lastPos < 0 ) {
						lastPos = 0;
					}
					else if ( lastPos >= histogramContent.getNbElement() ) {
						lastPos = (histogramContent.getNbElement()-1);
					}
					
					histogramContent.getElementByIndex(lastPos).firstIntervalTimestamp = lastRangeTime;
					histogramContent.setReadyUpToPosition(lastPos);
					
					nbPosNotEmpty++;
					nbEventsInRange = 1;
				}
				else {
					nbEventsInRange++;
					if ( nbEventsInRange > histogramContent.getHeighestEventCount() ) {
						histogramContent.setHeighestEventCount(nbEventsInRange);
					}
				}
				
				nbEventRead++;
        	}
        	else {
        		// *** FIXME ***
            	// *** EVIL FIX ***
                // Because of the other evil bug (see above), we have to ignore extra useless events we will get
        		// However, we might be far away from the end so we better start a redraw now
        		redrawAsyncronously();
        		requestCompleted = true;
        	}
			
			if ( nbEventRead % REDRAW_EVERY_NB_EVENTS == 0 ) {
				redrawAsyncronously();
			}
			
		}
    }
	
    @Override
    public void handleCompleted() {
		redrawAsyncronously();
    }
    
    @Override
    public void handleSuccess() {
    }
    
    @Override
    public void handleFailure() {
    }
    
    @Override
    public void handleCancel() {
    }
	
    
    public void updateEventsInfo() {
    	int averageNumberOfEvents = nbEventRead / nbPosNotEmpty;
		histogramContent.setAverageNumberOfEvents(averageNumberOfEvents);
		histogramContent.recalculateEventHeight();
    }
    
    public void redrawAsyncronously() {
    	updateEventsInfo();
    	
    	Display display = parentCanvas.getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				parentCanvas.redraw();
			}
		});
    }
    
}
