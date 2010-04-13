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
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.experiment.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.experiment.TmfExperimentUpdatedSignal;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class HistogramView extends TmfView {

    public static final String ID = "org.eclipse.linuxtools.lttng.ui.views.histogram";
    
    
    private static final boolean TEST_UI = true;
    
    
    private static final int FULL_TRACE_CANVAS_HEIGHT = 25;
    private static final int FULL_TRACE_BAR_WIDTH = 1;
    private static final double FULL_TRACE_DIFFERENCE_TO_AVERAGE = 2.0;
    
    private static final int SELECTED_WINDOW_CANVAS_WIDTH = 600;
    private static final int SELECTED_WINDOW_CANVAS_HEIGHT = 75;
    private static final int SELECTED_WINDOW_BAR_WIDTH = 1;
    private static final double SELECTED_WINDOW_DIFFERENCE_TO_AVERAGE = 10.0;
    
    // For the two "events" label (Max and min number of events in the selection), we force a width
    // This will prevent the control from moving horizontally if the number of events in the selection varies
    private static final int NB_EVENTS_FIXED_WIDTH = 75;
    
    
    // The "small font" height used to display time will be "default font" minus this constant
    private static final int SMALL_FONT_MODIFIER = 2;
    
    // *** TODO ***
    // This need to be changed as soon the framework implement a "window"
    private static long DEFAULT_WINDOW_SIZE = (1L * 1000000000);
    
    
    
    private TmfExperiment<LttngEvent> lastUsedExperiment = null;
    
    private HistogramRequest dataBackgroundFullRequest = null;
    private ParentHistogramCanvas fullTraceCanvas = null;
    
	private HistogramRequest selectedWindowRequest = null;
    private ChildrenHistogramCanvas selectedWindowCanvas = null;
    
    
	private Text txtExperimentStartTime = null;
	private Text txtExperimentStopTime = null;
	
	private Text  txtWindowStartTime = null;
	private Text  txtWindowStopTime  = null;
	private Label lblWindowMaxNbEvents = null;
	private Label lblWindowMinNbEvents = null;
    
	private static final String WINDOW_TIMERANGE_LABEL_TEXT 	= "Window Timerange   ";
	private static final String WINDOW_CURRENT_TIME_LABEL_TEXT 	= "Window Current Time";
	private static final String EVENT_CURRENT_TIME_LABEL_TEXT 	= "Event Current Time ";
	private TimeTextGroup  ntgTimeRangeWindow = null;
	private TimeTextGroup  ntgCurrentWindowTime = null;
	private TimeTextGroup  ntgCurrentEventTime = null;
	
	private Long selectedWindowTime = 0L;
	private Long selectedWindowTimerange = 0L;
	private Long currentEventTime = 0L;
	
	public HistogramView() {
		super(ID);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		
		// Default font
		Font font = parent.getFont();
		FontData tmpFontData = font.getFontData()[0];
		// Slightly smaller font for time
		Font smallFont = new Font(font.getDevice(), tmpFontData.getName(), tmpFontData.getHeight() - SMALL_FONT_MODIFIER, tmpFontData.getStyle());
		
		
		// Layout for the whole view, other elements will be in a child composite of this one 
		// Contains :
		// 		Composite layoutSelectionWindow
		//		Composite layoutTimesSpinner
		//		Composite layoutExperimentHistogram
		Composite layoutFullView = new Composite(parent, SWT.NONE);
		GridLayout gridFullView = new GridLayout();
		gridFullView.numColumns = 2;
		gridFullView.marginHeight = 0;
		gridFullView.marginWidth = 0;
		layoutFullView.setLayout(gridFullView);
		//layoutFullView.setSize(parent.getDisplay().getBounds().width, parent.getDisplay().getBounds().height);
		
		
		// Layout that contain the SelectionWindow
		// Contains : 
		// 		Label lblWindowStartTime
		// 		Label lblWindowStopTime
		// 		Label lblWindowMaxNbEvents
		// 		Label lblWindowMinNbEvents
		// 		ChildrenHistogramCanvas selectedWindowCanvas
		Composite layoutSelectionWindow = new Composite(layoutFullView, SWT.NONE);
		GridLayout gridSelectionWindow = new GridLayout();
		gridSelectionWindow.numColumns = 3;
		gridSelectionWindow.marginHeight = 0;
		gridSelectionWindow.marginWidth = 0;
		layoutSelectionWindow.setLayout(gridSelectionWindow);
		GridData gridDataSelectionWindow = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
		layoutSelectionWindow.setLayoutData(gridDataSelectionWindow);
		
		
		// Layout that contain the time spinner
		// Contains : 
		// 		NanosecTextGroup  spTimeRangeWindow
		// 		NanosecTextGroup  spCurrentWindowTime
		// 		NanosecTextGroup  spCurrentEventTime
		Composite layoutTimesSpinner = new Composite(layoutFullView, SWT.NONE);
		GridLayout gridTimesSpinner = new GridLayout();
		
		if ( TEST_UI ) {
			gridTimesSpinner.numColumns = 3;
		}
		else {
			gridTimesSpinner.numColumns = 2;
		}
		gridTimesSpinner.marginHeight = 0;
		gridTimesSpinner.marginWidth = 0;
		layoutTimesSpinner.setLayout(gridTimesSpinner);
		GridData gridDataTimesSpinner = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		layoutTimesSpinner.setLayoutData(gridDataTimesSpinner);
		
		
		// Layout that contain the complete experiment histogram and related controls.
		// Contains : 
		//		Label lblExperimentStartTime
		//		Label lblExperimentStopTime
		// 		ParentHistogramCanvas fullTraceCanvas
		Composite layoutExperimentHistogram = new Composite(layoutFullView, SWT.NONE);
		GridLayout gridExperimentHistogram = new GridLayout();
		gridExperimentHistogram.numColumns = 2;
		gridExperimentHistogram.marginHeight = 0;
		gridExperimentHistogram.marginWidth = 0;
		layoutExperimentHistogram.setLayout(gridExperimentHistogram);
		GridData gridDataExperimentHistogram = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
		layoutExperimentHistogram.setLayoutData(gridDataExperimentHistogram);
		
		
		
		// *** Everything related to the selection window is below
		GridData gridDataSelectionWindowCanvas = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 2);
		gridDataSelectionWindowCanvas.heightHint = SELECTED_WINDOW_CANVAS_HEIGHT;
		gridDataSelectionWindowCanvas.minimumHeight = SELECTED_WINDOW_CANVAS_HEIGHT;
		
		int size = 0;
		if ( TEST_UI ) {
			size = SELECTED_WINDOW_CANVAS_WIDTH/2;
		}
		else {
			size = SELECTED_WINDOW_CANVAS_WIDTH;
		}
		
		gridDataSelectionWindowCanvas.widthHint = size;
		gridDataSelectionWindowCanvas.minimumWidth = size;
		
		
		selectedWindowCanvas = new ChildrenHistogramCanvas(this, layoutSelectionWindow, SWT.BORDER);
		selectedWindowCanvas.setLayoutData(gridDataSelectionWindowCanvas);
		
		GridData gridDataWindowMaxEvents = new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1);
		// Force a width, to avoid the control to enlarge if the number of events change
		gridDataWindowMaxEvents.minimumWidth = NB_EVENTS_FIXED_WIDTH;
		gridDataWindowMaxEvents.widthHint = NB_EVENTS_FIXED_WIDTH;
		lblWindowMaxNbEvents = new Label(layoutSelectionWindow, SWT.NONE);
		lblWindowMaxNbEvents.setFont(smallFont);
		lblWindowMaxNbEvents.setText("");
		lblWindowMaxNbEvents.setLayoutData(gridDataWindowMaxEvents);
		
		GridData gridDataWindowMinEvents = new GridData(SWT.LEFT, SWT.BOTTOM, true, false, 1, 1);
		// Force a width, to avoid the control to enlarge if the number of events change
		gridDataWindowMinEvents.minimumWidth = NB_EVENTS_FIXED_WIDTH;
		gridDataWindowMinEvents.widthHint = NB_EVENTS_FIXED_WIDTH;
		lblWindowMinNbEvents = new Label(layoutSelectionWindow, SWT.NONE);
		lblWindowMinNbEvents.setFont(smallFont);
		lblWindowMinNbEvents.setText("");
		lblWindowMinNbEvents.setLayoutData(gridDataWindowMinEvents);
		
		GridData gridDataWindowStart = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		txtWindowStartTime = new Text(layoutSelectionWindow, SWT.READ_ONLY);
		txtWindowStartTime.setFont(smallFont);
		txtWindowStartTime.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		txtWindowStartTime.setEditable(false);
		txtWindowStartTime.setText("");
		txtWindowStartTime.setLayoutData(gridDataWindowStart);
		
		GridData gridDataWindowStop = new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1);
		txtWindowStopTime = new Text(layoutSelectionWindow, SWT.READ_ONLY);
		txtWindowStopTime.setFont(smallFont);
		txtWindowStopTime.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		txtWindowStopTime.setEditable(false);
		txtWindowStopTime.setText("");
		txtWindowStopTime.setLayoutData(gridDataWindowStop);
		
		
		
		// *** Everything related to the spinner is below
		if ( TEST_UI ) {
			GridData gridDataCurrentWindow = new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 2);
			ntgCurrentWindowTime = new TimeTextGroup(this, layoutTimesSpinner, SWT.BORDER, SWT.BORDER, WINDOW_CURRENT_TIME_LABEL_TEXT, HistogramConstant.formatNanoSecondsTime( 0L ));
			ntgCurrentWindowTime.setLayoutData(gridDataCurrentWindow);
			
			GridData gridDataTimeRange = new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 2);
			ntgTimeRangeWindow = new TimeTextGroup(this, layoutTimesSpinner, SWT.BORDER, SWT.BORDER, WINDOW_TIMERANGE_LABEL_TEXT, HistogramConstant.formatNanoSecondsTime( 0L ));
			ntgTimeRangeWindow.setLayoutData(gridDataTimeRange);
			
			GridData gridDataCurrentEvent = new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 2);
			ntgCurrentEventTime = new TimeTextGroup(this, layoutTimesSpinner, SWT.BORDER, SWT.BORDER, EVENT_CURRENT_TIME_LABEL_TEXT, HistogramConstant.formatNanoSecondsTime( 0L ));
			ntgCurrentEventTime.setLayoutData(gridDataCurrentEvent);
		}
		else {
			GridData gridDataTimeRange = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
			ntgTimeRangeWindow = new TimeTextGroup(this, layoutTimesSpinner, SWT.BORDER, SWT.BORDER, WINDOW_TIMERANGE_LABEL_TEXT, HistogramConstant.formatNanoSecondsTime( 0L ));
			ntgTimeRangeWindow.setLayoutData(gridDataTimeRange);
			
			GridData gridDataCurrentEvent = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 2);
			ntgCurrentEventTime = new TimeTextGroup(this, layoutTimesSpinner, SWT.BORDER, SWT.BORDER, EVENT_CURRENT_TIME_LABEL_TEXT, HistogramConstant.formatNanoSecondsTime( 0L ));
			ntgCurrentEventTime.setLayoutData(gridDataCurrentEvent);
			
			GridData gridDataCurrentWindow = new GridData(SWT.CENTER, SWT.BOTTOM, true, false, 1, 1);
			ntgCurrentWindowTime = new TimeTextGroup(this, layoutTimesSpinner, SWT.BORDER, SWT.BORDER, WINDOW_CURRENT_TIME_LABEL_TEXT, HistogramConstant.formatNanoSecondsTime( 0L ));
			ntgCurrentWindowTime.setLayoutData(gridDataCurrentWindow);
		}
		
		
		// Everything related to the experiment canvas is below
		GridData gridDataExperimentCanvas = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
		gridDataExperimentCanvas.heightHint = FULL_TRACE_CANVAS_HEIGHT;
		gridDataExperimentCanvas.minimumHeight = FULL_TRACE_CANVAS_HEIGHT;
		fullTraceCanvas = new ParentHistogramCanvas(this, layoutExperimentHistogram, SWT.BORDER);
		fullTraceCanvas.setLayoutData(gridDataExperimentCanvas);
		
		GridData gridDataExperimentStart = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		txtExperimentStartTime = new Text(layoutExperimentHistogram, SWT.READ_ONLY);
		txtExperimentStartTime.setFont(smallFont);
		txtExperimentStartTime.setText("");
		txtExperimentStartTime.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		txtExperimentStartTime.setEditable(false);
		txtExperimentStartTime.setLayoutData(gridDataExperimentStart);
		
		GridData gridDataExperimentStop = new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1);
		txtExperimentStopTime = new Text(layoutExperimentHistogram, SWT.READ_ONLY);
		txtExperimentStopTime.setFont(smallFont);
		txtExperimentStopTime.setText("");
		txtExperimentStopTime.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		txtExperimentStopTime.setEditable(false);
		txtExperimentStopTime.setLayoutData(gridDataExperimentStop);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void setFocus() {
		
		TmfExperiment<LttngEvent> tmpExperiment = (TmfExperiment<LttngEvent>)TmfExperiment.getCurrentExperiment();
		
		if ( (dataBackgroundFullRequest == null) && (tmpExperiment != null) ) {
			createCanvasAndRequests(tmpExperiment);
		}
	}
	
	
    @SuppressWarnings("unchecked")
	@TmfSignalHandler
    public void experimentSelected(TmfExperimentSelectedSignal<LttngEvent> signal) {
    	TmfExperiment<LttngEvent> tmpExperiment = (TmfExperiment<LttngEvent>)signal.getExperiment();
    	createCanvasAndRequests(tmpExperiment);
    }
    
    // *** VERIFY ***
	// Not sure what this should do since I don't know when it will be called
	// Let's do the same thing as experimentSelected for now
	//
    @SuppressWarnings("unchecked")
	@TmfSignalHandler
    public void experimentUpdated(TmfExperimentUpdatedSignal signal) {
    	TmfExperiment<LttngEvent> tmpExperiment = (TmfExperiment<LttngEvent>)signal.getExperiment();
    	
    	// Make sure the UI object are sane
		resetLabelContent();
		
		// Redraw the canvas right away to have something "clean" as soon as we can
		fullTraceCanvas.redraw();
		selectedWindowCanvas.redraw();
		
		performAllTraceEventsRequest(tmpExperiment);
		performSelectedWindowEventsRequest(tmpExperiment);
    }
    
    @TmfSignalHandler
    public void currentTimeUpdated(TmfTimeSynchSignal signal) {
    	if (signal.getSource() != this) {
            TmfTimestamp currentTime = signal.getCurrentTime();
            
            currentEventTime = currentTime.getValue();
            updateSelectedEventTime();
            
            if ( isGivenTimestampInSelectedWindow( currentEventTime ) == false)
            {
            	fullTraceCanvas.centerWindow( fullTraceCanvas.getHistogramContent().getClosestXPositionFromTimestamp(currentEventTime) );
            	windowChangedNotification();
            }
    	}
    }
    
    public void createCanvasAndRequests(TmfExperiment<LttngEvent> newExperiment) {
    	lastUsedExperiment = newExperiment;
    	
		fullTraceCanvas.createNewHistogramContent( DEFAULT_WINDOW_SIZE, FULL_TRACE_BAR_WIDTH, FULL_TRACE_CANVAS_HEIGHT, FULL_TRACE_DIFFERENCE_TO_AVERAGE);
		selectedWindowCanvas.createNewHistogramContent(0, SELECTED_WINDOW_BAR_WIDTH, SELECTED_WINDOW_CANVAS_HEIGHT, SELECTED_WINDOW_DIFFERENCE_TO_AVERAGE);
		
		// Make sure the UI object are sane
		resetLabelContent();
		
		// Redraw the canvas right away to have something "clean" as soon as we can
    	if ( dataBackgroundFullRequest != null ) {
    		fullTraceCanvas.redraw();
    		selectedWindowCanvas.redraw();
    	}
		
		fullTraceCanvas.getCurrentWindow().setSelectedWindowVisible(true);
		
		performAllTraceEventsRequest(newExperiment);
		performSelectedWindowEventsRequest(newExperiment);
    }
    
    public void performSelectedWindowEventsRequest(TmfExperiment<LttngEvent> experiment) {
    	
    	HistogramSelectedWindow curSelectedWindow = fullTraceCanvas.getCurrentWindow();
    	
    	if ( curSelectedWindow == null ) {
    		fullTraceCanvas.createNewSelectedWindow( getTimeWindowSize() );
    		curSelectedWindow = fullTraceCanvas.getCurrentWindow();
    	}
    	
		LttngTimestamp ts1 = new LttngTimestamp( curSelectedWindow.getTimestampLeft() );
		LttngTimestamp ts2 = new LttngTimestamp( curSelectedWindow.getTimestampRight() );
		
        if ( ts2.getValue() > experiment.getEndTime().getValue() ) {
        	ts2 = new LttngTimestamp( experiment.getEndTime().getValue() );
        }
        
        TmfTimeRange tmpRange = new TmfTimeRange(ts1, ts2);
        
        // Set a (dynamic) time interval
        long intervalTime = ( (ts2.getValue() - ts1.getValue()) / selectedWindowCanvas.getHistogramContent().getNbElement() );
        
        // *** VERIFY ***
        // This would enable "fixed interval" instead of dynamic one.
        // ... we don't need it, do we?
        //
        // long intervalTime = ((long)(0.001 * (double)1000000000));
        selectedWindowRequest = performRequest(experiment, selectedWindowCanvas, tmpRange, intervalTime);
        selectedWindowCanvas.redrawAsynchronously();
    }
    
    public void performAllTraceEventsRequest(TmfExperiment<LttngEvent> experiment) {
    	// Create a new time range from "start" to "end"
        //	That way, we will get "everything" in the trace
        LttngTimestamp ts1 = new LttngTimestamp( experiment.getStartTime() );
        LttngTimestamp ts2 = new LttngTimestamp( experiment.getEndTime() );
        TmfTimeRange tmpRange = new TmfTimeRange(ts1, ts2);
        
        // Set a (dynamic) time interval
        long intervalTime = ( (ts2.getValue() - ts1.getValue()) / fullTraceCanvas.getHistogramContent().getNbElement() );
        
        dataBackgroundFullRequest = performRequest(experiment, fullTraceCanvas, tmpRange, intervalTime);
        fullTraceCanvas.redrawAsynchronously();
    }
    
    // *** VERIFY ***
    // this function is synchronized, is it a good idea?
    public synchronized HistogramRequest performRequest(TmfExperiment<LttngEvent> experiment, HistogramCanvas targetCanvas, TmfTimeRange newRange, long newInterval) {
    	HistogramRequest returnedRequest = null;
    	
        // *** FIXME ***
        // EVIL BUG!
	    // We use integer.MAX_VALUE because we want every events BUT we don't know the number inside the range.
        // HOWEVER, this would cause the request to run forever (or until it reach the end of trace).
        // Seeting an EndTime does not seems to stop the request
        returnedRequest = new HistogramRequest(newRange, Integer.MAX_VALUE, targetCanvas, newInterval );
        experiment.sendRequest(returnedRequest);
        
        return returnedRequest;
    }
    
    
    public void windowChangedNotification() {
    	
    	if ( lastUsedExperiment != null ) {
    		if ( selectedWindowRequest.isCompleted() == false ) {
    			selectedWindowRequest.cancel();
    		}
    		
    		selectedWindowTime = fullTraceCanvas.getCurrentWindow().getTimestampCenter();
    		selectedWindowTimerange = fullTraceCanvas.getCurrentWindow().getWindowTimeWidth();
    		
    		if ( isGivenTimestampInSelectedWindow(ntgCurrentEventTime.getValue()) == false ) {
    			currentEventChangeNotification( selectedWindowTime );
    		}
    		
    		performSelectedWindowEventsRequest(lastUsedExperiment);
    	}
    }
    
    
    public void currentEventChangeNotification(Long newCurrentEventTime) {
    	// Notify other views in the framework
        if (currentEventTime != newCurrentEventTime) {
        	currentEventTime = newCurrentEventTime;
        	
        	updateSelectedEventTime();
        	
            LttngTimestamp tmpTimestamp = new LttngTimestamp(newCurrentEventTime);
            broadcast(new TmfTimeSynchSignal(this, tmpTimestamp));
        }
    }
    
    public void timeTextGroupChangeNotification() {
    	
    	Long newCurrentTime = ntgCurrentEventTime.getValue();
    	Long newSelectedWindowTime = ntgCurrentWindowTime.getValue();
    	Long newSelectedWindowTimeRange = ntgTimeRangeWindow.getValue();
    	
    	if ( newCurrentTime != currentEventTime ) {
    		currentEventChangeNotification( newCurrentTime );
    	}
    	
    	if ( newSelectedWindowTime != selectedWindowTime ) {
    		selectedWindowTime = newSelectedWindowTime;
    		fullTraceCanvas.centerWindow( fullTraceCanvas.getHistogramContent().getClosestXPositionFromTimestamp(selectedWindowTime) );
    	}
    	
    	if ( newSelectedWindowTimeRange != selectedWindowTimerange ) {
    		selectedWindowTimerange = newSelectedWindowTimeRange;
    		fullTraceCanvas.resizeWindowByAbsoluteTime(selectedWindowTimerange);
    	}
    	
    }
    
    public boolean isRequestRunning() {
    	boolean returnedValue = true;
    	
    	if ( ( dataBackgroundFullRequest.isCompleted() == true ) && ( selectedWindowRequest.isCompleted() == true ) ) {
    		returnedValue = false;
    	}
    	
    	return returnedValue;
    }
    
    
	public TmfExperiment<LttngEvent> getLastUsedExperiment() {
		return lastUsedExperiment;
	}
	
	public Long getTimeWindowSize() {
		return fullTraceCanvas.getSelectedWindowSize();
	}
	
	public void setTimeWindowSize(long newTimeWidth) {
		fullTraceCanvas.setSelectedWindowSize(newTimeWidth);
	}
	
	public boolean isGivenTimestampInSelectedWindow(Long timestamp) {
		boolean returnedValue = true;
		
		if ( (timestamp < fullTraceCanvas.getCurrentWindow().getTimestampLeft()  ) ||
	         (timestamp > fullTraceCanvas.getCurrentWindow().getTimestampRight() ) ) 
		{
			returnedValue = false;
		}
		
		return returnedValue;
	}
	
	public void resetLabelContent() {
		
		TmfExperiment<LttngEvent> tmpExperiment = getLastUsedExperiment();
		
		String startTime = null;
		String stopTime = null;
		if ( tmpExperiment != null ) {
			startTime = HistogramConstant.formatNanoSecondsTime( tmpExperiment.getStartTime().getValue() );
			stopTime = HistogramConstant.formatNanoSecondsTime( tmpExperiment.getEndTime().getValue() );
		}
		else {
			startTime = HistogramConstant.formatNanoSecondsTime( 0L );
			stopTime = HistogramConstant.formatNanoSecondsTime( 0L );
		}
		
    	txtExperimentStartTime.setText( startTime );
		txtExperimentStopTime.setText( stopTime );
		txtExperimentStartTime.getParent().layout();
		
		lblWindowMaxNbEvents.setText("" + 0);
		lblWindowMinNbEvents.setText("" + 0);
		txtWindowStartTime.setText( HistogramConstant.formatNanoSecondsTime( 0L ) );
		txtWindowStopTime.setText( HistogramConstant.formatNanoSecondsTime( 0L ) );
		txtWindowStartTime.getParent().layout();
		
		ntgCurrentWindowTime.setValue( HistogramConstant.formatNanoSecondsTime( 0L ) );
		ntgTimeRangeWindow.setValue( HistogramConstant.formatNanoSecondsTime( 0L ) );
		ntgCurrentEventTime.setValue( HistogramConstant.formatNanoSecondsTime( 0L ) );
	}
	
	public void updateFullTraceInformation() {
		
		String startTime = HistogramConstant.formatNanoSecondsTime( fullTraceCanvas.getHistogramContent().getStartTime() );
		String stopTime = HistogramConstant.formatNanoSecondsTime( fullTraceCanvas.getHistogramContent().getEndTime() );
		
		txtExperimentStartTime.setText( startTime );
		txtExperimentStopTime.setText( stopTime );
		
		// Take one of the parent and call its layout to update control size
		// Since both control have the same parent, only one call is needed 
		txtExperimentStartTime.getParent().layout();
		
		// Update the selected window, just in case
		// This should give a better user experience and it is low cost 
		updateSelectedWindowInformation();
	}
	
	public void updateSelectedWindowInformation() {
		// Update the timestamp as well
		updateSelectedWindowTimestamp();
		
		lblWindowMaxNbEvents.setText( selectedWindowCanvas.getHistogramContent().getHeighestEventCount().toString() );
		lblWindowMinNbEvents.setText("0");
		
		// Refresh the layout
		lblWindowMaxNbEvents.getParent().layout();
	}
	
	public void updateSelectedWindowTimestamp() {
		String startTime = HistogramConstant.formatNanoSecondsTime( selectedWindowCanvas.getHistogramContent().getStartTime() );
		String stopTime = HistogramConstant.formatNanoSecondsTime( selectedWindowCanvas.getHistogramContent().getEndTime() );
		txtWindowStartTime.setText( startTime );
		txtWindowStopTime.setText( stopTime );
		
		ntgCurrentWindowTime.setValue( fullTraceCanvas.getCurrentWindow().getTimestampCenter() );
		ntgTimeRangeWindow.setValue(  fullTraceCanvas.getCurrentWindow().getWindowTimeWidth() );
		
		if ( isGivenTimestampInSelectedWindow(ntgCurrentEventTime.getValue()) == false ) {
			currentEventChangeNotification( fullTraceCanvas.getCurrentWindow().getTimestampCenter() );
		}
		
		// Take one control in each group to call to refresh the layout
		// Since both control have the same parent, only one call is needed 
		txtWindowStartTime.getParent().layout();
		ntgCurrentWindowTime.getParent().layout();
	}
	
	public void updateSelectedEventTime() {
    	ntgCurrentEventTime.setValueAsynchronously(currentEventTime);
    	selectedWindowCanvas.getHistogramContent().setSelectedEventTimeInWindow(currentEventTime);
    	selectedWindowCanvas.redrawAsynchronously();
	}
	
}
