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
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.experiment.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.experiment.TmfExperimentUpdatedSignal;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class HistogramView extends TmfView {

    public static final String ID = "org.eclipse.linuxtools.lttng.ui.views.histogram";
    
    private static int FULL_TRACE_CANVAS_HEIGHT = 50;
    private static int FULL_TRACE_BAR_WIDTH = 1;
    private static double FULL_TRACE_DIFFERENCE_TO_AVERAGE = 2.0;
    
    private static int SELECTED_WINDOW_CANVAS_WIDTH = 300;
    private static int SELECTED_WINDOW_CANVAS_HEIGHT = 100;
    private static int SELECTED_WINDOW_BAR_WIDTH = 1;
    private static double SELECTED_WINDOW_DIFFERENCE_TO_AVERAGE = 10.0;
    
    // *** TODO ***
    // This need to be changed as soon the framework implement a "window"
    private static long DEFAULT_WINDOW_SIZE = (1L * 1000000000);
    
    private TmfExperiment<LttngEvent> lastUsedExperiment = null;
    
    private HistogramRequest dataBackgroundFullRequest = null;
    private ParentTraceCanvas fullTraceCanvas = null;
    
	private HistogramRequest selectedWindowRequest = null;
    private ChildrenTraceCanvas selectedWindowCanvas = null;
    
	private Label lblStartTime = null;
	private Label lblStopTime = null;
	private Label lblTopEvent = null;
	private Label lblBottomEvent = null;
    
	public HistogramView() {
		super(ID);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		
		Font font = parent.getFont();
		Composite folderGroup = new Composite(parent, SWT.BORDER);
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.verticalSpacing = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		folderGroup.setLayout(gridLayout);
		folderGroup.setSize(parent.getDisplay().getBounds().width, parent.getDisplay().getBounds().height);
		
		GridData gridData1 = new GridData(SWT.FILL, SWT.TOP, true, false, 3, 2);
		gridData1.heightHint = FULL_TRACE_CANVAS_HEIGHT;
		gridData1.minimumHeight = FULL_TRACE_CANVAS_HEIGHT;
		fullTraceCanvas = new ParentTraceCanvas(this, folderGroup, SWT.BORDER, FULL_TRACE_BAR_WIDTH, FULL_TRACE_CANVAS_HEIGHT);
		fullTraceCanvas.setLayoutData(gridData1);
		fullTraceCanvas.redraw();
		
		GridData gridData2 = new GridData(SWT.LEFT, SWT.TOP, true, false, 2, 2);
		gridData2.minimumWidth = 200;
		gridData2.grabExcessHorizontalSpace = true;
		lblStartTime = new Label(folderGroup, SWT.LEFT | SWT.TOP);
		lblStartTime.setFont(font);
		lblStartTime.setText("");
		lblStartTime.setAlignment(SWT.LEFT);
		lblStartTime.setLayoutData(gridData2);
		
		GridData gridData3 = new GridData(SWT.RIGHT, SWT.TOP, true, false, 1, 2);
		gridData3.minimumWidth = 200;
		gridData3.grabExcessHorizontalSpace = true;
		lblStopTime = new Label(folderGroup, SWT.RIGHT | SWT.TOP);
		lblStopTime.setFont(font);
		lblStopTime.setAlignment(SWT.RIGHT);
		lblStopTime.setText("");
		lblStopTime.setLayoutData(gridData3);
		
		GridData gridDataSpace = new GridData(SWT.FILL, SWT.TOP, true, false, 3, 2);
		gridDataSpace.minimumHeight = 20;
		gridDataSpace.heightHint = 20;
		Label lblSpace = new Label(folderGroup, SWT.TOP);
		lblSpace.setFont(font);
		lblSpace.setText("");
		lblSpace.setLayoutData(gridDataSpace);
		
		GridData gridData4 = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 2);
		gridData4.heightHint = SELECTED_WINDOW_CANVAS_HEIGHT;
		gridData4.minimumHeight = SELECTED_WINDOW_CANVAS_HEIGHT;
		gridData4.widthHint = SELECTED_WINDOW_CANVAS_WIDTH;
		gridData4.minimumWidth = SELECTED_WINDOW_CANVAS_WIDTH;
		selectedWindowCanvas = new ChildrenTraceCanvas(this, folderGroup, SWT.BORDER, SELECTED_WINDOW_BAR_WIDTH, SELECTED_WINDOW_CANVAS_HEIGHT);
		selectedWindowCanvas.setLayoutData(gridData4);
		selectedWindowCanvas.redraw();
		
		
		GridData gridData5 = new GridData(SWT.LEFT, SWT.TOP, true, false, 2, 1);
		gridData5.minimumWidth = 150;
		gridData5.grabExcessHorizontalSpace = true;
		lblTopEvent = new Label(folderGroup, SWT.LEFT | SWT.TOP);
		lblTopEvent.setFont(font);
		lblTopEvent.setText("");
		lblTopEvent.setLayoutData(gridData5);
		
		GridData gridData6 = new GridData(SWT.LEFT, SWT.BOTTOM, true, false, 2, 1);
		gridData6.minimumWidth = 150;
		gridData6.grabExcessHorizontalSpace = true;
		lblBottomEvent = new Label(folderGroup, SWT.LEFT | SWT.BOTTOM);
		lblBottomEvent.setFont(font);
		lblBottomEvent.setText("");
		lblBottomEvent.setLayoutData(gridData6);
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
    
    public void createCanvasAndRequests(TmfExperiment<LttngEvent> newExperiment) {
    	lastUsedExperiment = newExperiment;
    	
    	lblStartTime.setText( "" +  newExperiment.getStartTime().getValue() );
		lblStopTime.setText( "" + newExperiment.getEndTime().getValue() );
    	
		fullTraceCanvas.createNewHistogramContent( DEFAULT_WINDOW_SIZE, FULL_TRACE_DIFFERENCE_TO_AVERAGE);
		selectedWindowCanvas.createNewHistogramContent(0, SELECTED_WINDOW_DIFFERENCE_TO_AVERAGE);
		
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
    		curSelectedWindow = fullTraceCanvas.createNewSelectedWindow( getTimeWindowSize() );
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
    public synchronized HistogramRequest performRequest(TmfExperiment<LttngEvent> experiment, TraceCanvas targetCanvas, TmfTimeRange newRange, long newInterval) {
    	HistogramRequest returnedRequest = null;
    	
        // The content holder we will use
        HistogramContent content = targetCanvas.getHistogramContent();
        
        // *** FIXME ***
        // EVIL BUG!
	    // We use integer.MAX_VALUE because we want every events BUT we don't know the number inside the range.
        // HOWEVER, this would cause the request to run forever (or until it reach the end of trace).
        // Seeting an EndTime does not seems to stop the request
        returnedRequest = new HistogramRequest(LttngEvent.class, newRange, Integer.MAX_VALUE, content, targetCanvas, newInterval );
        
        experiment.sendRequest(returnedRequest);
        
        return returnedRequest;
    }

    
    public void windowChangedNotification() {
    	if ( lastUsedExperiment != null ) {
    		if ( selectedWindowRequest.isCompleted() == false ) {
    			selectedWindowRequest.cancel();
    		}
    		performSelectedWindowEventsRequest(lastUsedExperiment);
    	}
    }
    
    public boolean isRequestRunning() {
    	boolean returnedValue = true;
    	
    	if ( ( dataBackgroundFullRequest.isCompleted() == true ) && ( selectedWindowRequest.isCompleted() == true ) ) {
    		returnedValue = false;
    	}
    	
    	return returnedValue;
    }
    
    
    @TmfSignalHandler
    public void experimentUpdated(TmfExperimentUpdatedSignal signal) {
    	System.out.println("experimentUpdated");
    	
    	// *** TODO ***
    	// Update the histogram if the time changed
    	//
    }
    
    
    @TmfSignalHandler
    public void currentTimeUpdated(TmfTimeSynchSignal signal) {
    	System.out.println("currentTimeUpdated");
    	
    	// *** TODO ***
    	// Update the histogram if the time changed
    	//
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
	
	public void updateViewInformation() {
		lblStartTime.setText( fullTraceCanvas.getHistogramContent().getStartTime().toString() );
		lblStopTime.setText( fullTraceCanvas.getHistogramContent().getEndTime().toString() );
		
		if ( selectedWindowRequest.isCompleted() == true ) {
			lblTopEvent.setText( selectedWindowCanvas.getHistogramContent().getHeighestEventCount().toString() );
			lblBottomEvent.setText("0");
		}
	}
	
}
