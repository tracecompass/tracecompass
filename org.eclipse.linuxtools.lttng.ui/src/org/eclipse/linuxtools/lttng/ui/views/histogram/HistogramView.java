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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class HistogramView extends TmfView {

    public static final String ID = "org.eclipse.linuxtools.lttng.ui.views.histogram";
    
    
    private HistogramRequest dataBackgroundFullRequest = null;
    private TraceCanvas fullTraceCanvas = null;
    
    private HistogramRequest selectedWindowRequest = null;
    private TraceCanvas selectedWindowCanvas = null;
    
	public HistogramView() {
		super(ID);
	}
	
	
	@Override
	public void createPartControl(Composite parent) {
		
		Composite folderGroup = new Composite(parent, SWT.NONE);
		
//		GridLayout gl = new GridLayout();
//		gl.marginHeight = 0;
//		gl.marginWidth = 0;
//		folderGroup.setLayout(gl);
		
		folderGroup.setSize(parent.getDisplay().getBounds().width, parent.getDisplay().getBounds().height);
		folderGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		fullTraceCanvas = new TraceCanvas(folderGroup, SWT.NONE, 2, 50);
		
		
//		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
//		gd.heightHint = 50;
//		fullTraceCanvas.setLayoutData(gd);
		
		fullTraceCanvas.redraw();
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public void setFocus() {
		
		TmfExperiment<LttngEvent> tmpExperiment = (TmfExperiment<LttngEvent>)TmfExperiment.getCurrentExperiment();
		
		if ( (dataBackgroundFullRequest == null) && (tmpExperiment != null) ) {
	    	// Create a new time range from "start" to "end"
	        //	That way, we will get "everything" in the trace
	        LttngTimestamp ts1 = new LttngTimestamp( tmpExperiment.getStartTime() );
	        LttngTimestamp ts2 = new LttngTimestamp( tmpExperiment.getEndTime() );
	        TmfTimeRange tmpRange = new TmfTimeRange(ts1, ts2);
	        dataBackgroundFullRequest = performRequest(tmpExperiment, fullTraceCanvas, tmpRange);
	        
	        fullTraceCanvas.redraw();
		}
	}
	
	
    @SuppressWarnings("unchecked")
	@TmfSignalHandler
    public void experimentSelected(TmfExperimentSelectedSignal<LttngEvent> signal) {
    	fullTraceCanvas.getHistogramContent().resetContentData();
    	fullTraceCanvas.resetSelectedWindow();
    	
    	TmfExperiment<LttngEvent> tmpExperiment = (TmfExperiment<LttngEvent>)signal.getExperiment();
		
    	// Create a new time range from "start" to "end"
        //	That way, we will get "everything" in the trace
        //LttngTimestamp ts1 = new LttngTimestamp( tmpExperiment.getStartTime() );
        //LttngTimestamp ts2 = new LttngTimestamp( tmpExperiment.getEndTime() );
    	LttngTimestamp ts1 = new LttngTimestamp( tmpExperiment.getStartTime() );
        LttngTimestamp ts2 = new LttngTimestamp( tmpExperiment.getEndTime() );
        TmfTimeRange tmpRange = new TmfTimeRange(ts1, ts2);
        
        dataBackgroundFullRequest = performRequest(tmpExperiment, fullTraceCanvas, tmpRange);
        
        fullTraceCanvas.redraw();
    }
    
    // *** VERIFY ***
    // this function is synchronized, is it a good idea?
    public synchronized HistogramRequest performRequest(TmfExperiment<LttngEvent> experiment, TraceCanvas targetCanvas, TmfTimeRange newRange) {
    	
    	HistogramRequest returnedRequest = null;
    	
        // The content holder we will use
        HistogramContent content = targetCanvas.getHistogramContent();
        
	    // We use integer.MAX_VALUE because we want to make sure we get every events
        returnedRequest = new HistogramRequest(LttngEvent.class, newRange, Integer.MAX_VALUE, content, targetCanvas );
        
        content.setStartTime( experiment.getStartTime().getValue() );
        content.setEndTime( experiment.getEndTime().getValue() );
        
        // Set a (dynamic) time interval
        content.setIntervalTime( (content.getEndTime() - content.getStartTime()) / content.getNbElement() );
        
        // *** VERIFY ***
        // This would enable "fixed interval" instead of dynamic one.
        //
        //content.setIntervalTime((long)(0.001 * (double)1000000000));
        
        experiment.sendRequest(returnedRequest);
        
        return returnedRequest;
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
    }
}
