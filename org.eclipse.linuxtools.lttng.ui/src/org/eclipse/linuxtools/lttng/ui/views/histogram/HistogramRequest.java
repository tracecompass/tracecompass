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
	
	@SuppressWarnings("unchecked")
	public HistogramRequest(Class<? extends TmfEvent> dataType, TmfTimeRange range, int nbRequested, HistogramContent newContent, TraceCanvas newParentCanvas) {
        super((Class<LttngEvent>)dataType, range, nbRequested, MAX_EVENTS_PER_REQUEST);
        
        histogramContent = newContent;
        parentCanvas = newParentCanvas;
        
        lastRangeTime = histogramContent.getStartTime();
        
        histogramContent.resetContentData();
    }

	@Override
    public void handleData() {
        TmfEvent[] result = getData();
        TmfEvent[] evt = new TmfEvent[1];
        
        evt[0] = (result.length > 0) ? result[0] : null;
        
        if ( evt[0] != null ) {
        	LttngEvent tmpEvent = (LttngEvent)evt[0];
        	
        	long distance = ( tmpEvent.getTimestamp().getValue() - lastRangeTime );
			
			if  ( distance > histogramContent.getIntervalTime() ) {
				
				histogramContent.getElementByIndex(lastPos).intervalNbEvents = nbEventsInRange;
				lastRangeTime = tmpEvent.getTimestamp().getValue();
				
				lastPos = (int)((lastRangeTime - histogramContent.getStartTime()) / histogramContent.getIntervalTime() );
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
