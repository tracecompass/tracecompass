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
 * 2010-06-10 Yuriy Vashchuk - GUI reorganisation, simplification and some
 *                             related code improvements.
 * 2010-06-20 Yuriy Vashchuk - Histograms optimisation.   
 * 2010-07-16 Yuriy Vashchuk - Histogram Canvas Heritage correction
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.trace.TmfContext;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * <b><u>HistogramView</u></b>
 * <p>
 * View that contain an visual approach to the window that control the request.
 * This is intended to replace the TimeFrameView
 * <p>
 * This view is composed of 2 canvas, one for the whole experiment and one for the selectionned window in the experiment.
 * It also contain a certain number of controls to print or change informations about the experiment.
 */
public class HistogramView extends TmfView implements ControlListener {
	
	// *** TODO ***
	// Here is what's left to do in this view
	//
	// 1- Make sure the interval time is small enough on very big trace (bug 311930)
	//		The interval time of the content is dynamically assigned from the screen width and trace duration.
	//		However, on very big trace (more than 1 hour), we could end up with time interval that are > 1 seconds,
	//			which is not very precise. 
	//		An algorithm need to be implemented to make sure we "increase" the number of interval in the content if
	//			their precision is getting too bad. 
	//
	// 2- Make sure all control are thread safe (bug 309348)
	//		Right now, all the basic controls (i.e. Text and Label) are sensible to "Thread Access Exception" if
	//			updated from different threads; we need to carefully decide when/where to redraw them.
	//		This is a real problem since there is a lot of thread going on in this view.
	//		All basic control should be subclassed to offer "Asynchronous" functions.
	//
	// 3- Implement a "preferences view" for the HistogramView (bug 311935)
	//		There is a lot of adjustable preferences in the view, however there is no way to adjust them right now
	//			at run time. There should be a view of some kind of "menu" to allow the user to change them while executing.
	//		Most of the pertinent values are in HistogramConstant.java or in this file.
	
    public static final String ID = "org.eclipse.linuxtools.lttng.ui.views.histogram";
    
    // "Minimum" screen width size. On smaller screen, we will apply several space saving technique
    private static final int SCREEN_SMALL_IF_SMALLER_THAN = 1600;
    
/*
    // 2010-06-20 Yuriy: We will use the dynamic height.
    // Size of the "full trace" canvas
    private static final int FULL_TRACE_CANVAS_HEIGHT = 25;
*/    
    private static final int FULL_TRACE_BAR_WIDTH = 1;
    private static final double FULL_TRACE_DIFFERENCE_TO_AVERAGE = 1.5;
    
    // Size of the "Selected Window" canvas
/*    
    // 2010-06-20 Yuriy 
    private static final int SELECTED_WINDOW_CANVAS_WIDTH = 300;
    private static final int SMALL_SELECTED_WINDOW_CANVAS_WIDTH = 200;
    private static final int SELECTED_WINDOW_CANVAS_HEIGHT = 60;
*/    
    private static final int SELECTED_WINDOW_BAR_WIDTH = 1;
    private static final double SELECTED_WINDOW_DIFFERENCE_TO_AVERAGE = 10.0;
    
    // For the two "events" label (Max and Min number of events in the selection), we force a width
    // This will prevent the control from moving horizontally if the number of events in the selection varies
    private static final int NB_EVENTS_FIXED_WIDTH = 50;
    
    // The "small font" height used to display time will be "default font" minus this constant
    private static final int SMALL_FONT_MODIFIER = 2;
    private static final int VERY_SMALL_FONT_MODIFIER = 4;
    
    // *** TODO ***
    // This need to be changed as soon the framework implement a "window"
    private static long DEFAULT_WINDOW_SIZE = (10L * 100 * 1000 * 1000); // 1sec
    
    // The last experiment received/used by the view
    private TmfExperiment<LttngEvent> lastUsedExperiment = null;
    
    // Parent of the view
    private Composite parent = null;
    
    // Request and canvas for the "full trace" part
    private HistogramRequest dataBackgroundFullRequest = null;
    private static ParentHistogramCanvas fullExperimentCanvas = null;
    
    // Request and canvas for the "selected window"
	private HistogramRequest selectedWindowRequest = null;
    private static ChildrenHistogramCanvas selectedWindowCanvas = null;
    
    // Content of the timeTextGroup
    //	Since the user can modify them with erroneous value, 
    //	we will keep track of the value internally 
	private long currentEventTime = 0L;
	
    // *** All the UI control below
	//
	// NOTE : All textboxes will be READ_ONLY.
	//			So the user will be able to select/copy the value in them but not to change it
	private Text txtExperimentStartTime = null;
	private Text txtExperimentStopTime = null;
	
	private Text  txtWindowStartTime = null;
	private Text  txtWindowStopTime  = null;
	private Text  txtWindowMaxNbEvents = null;
	private Text  txtWindowMinNbEvents = null;
    
	// We move the time label to header from TimeTextGroup.java
	protected static final String NANOSEC_LABEL = "(sec)";
	private static final String WINDOW_TIMERANGE_LABEL_TEXT 	= "Window Timerange, " + NANOSEC_LABEL;
	private static final String WINDOW_CURRENT_TIME_LABEL_TEXT 	= "Cursor Centered on, " + NANOSEC_LABEL;
	private static final String EVENT_CURRENT_TIME_LABEL_TEXT 	= "Current Event Time, " + NANOSEC_LABEL;
	private TimeTextGroup  ntgTimeRangeWindow = null;
	private TimeTextGroup  ntgCurrentWindowTime = null;
	private TimeTextGroup  ntgCurrentEventTime = null;
	
	/**
	 * Default contructor of the view
	 */
	public HistogramView() {
		super(ID);
	}
	
	/**
	 * Create the UI controls of this view
	 * 
	 * @param  parent	The composite parent of this view
	 */
	@Override
	public void createPartControl(Composite newParent) {
		// Save the parent
		parent = newParent;
		
		// Default font
		Font font = parent.getFont();
		FontData tmpFontData = font.getFontData()[0];
		
		
		Font smallFont = null;
		int  nbEventWidth = -1; 
		int selectedCanvasWidth = -1;
		boolean doesTimeTextGroupNeedAdjustment = false;
		
		// Calculate if we need "small screen" fixes
		if ( parent.getDisplay().getBounds().width < SCREEN_SMALL_IF_SMALLER_THAN ) {
			
			// A lot smaller font for timstampe
			smallFont = new Font(font.getDevice(), tmpFontData.getName(), tmpFontData.getHeight() - VERY_SMALL_FONT_MODIFIER, tmpFontData.getStyle());
			
/*			
			// 2010-06-20 Yuriy
			// Smaller selection window canvas
			selectedCanvasWidth = SMALL_SELECTED_WINDOW_CANVAS_WIDTH;
*/			
			// Smaller event number text field
			nbEventWidth = NB_EVENTS_FIXED_WIDTH/2;
			
			// Tell the text group to ajust
			doesTimeTextGroupNeedAdjustment = true;

		} else {
			
			// Slightly smaller font for timestamp
			smallFont = new Font(font.getDevice(), tmpFontData.getName(), tmpFontData.getHeight() - SMALL_FONT_MODIFIER, tmpFontData.getStyle());
			// Usual size for selected window and event number text field
			nbEventWidth = NB_EVENTS_FIXED_WIDTH;
/*			
			// 2010-06-20 Yuriy
			selectedCanvasWidth = SELECTED_WINDOW_CANVAS_WIDTH;
*/			
			// No ajustement needed by the text group
			doesTimeTextGroupNeedAdjustment = false;
			
		}

		
		/////////////////////////////////////////////////////////////////////////////////////
		// Layout for the whole view, other elements will be in a child composite of this one 
		// Contains :
		// 		Composite layoutSelectionWindow
		//		Composite layoutTimesSpinner
		//		Composite layoutExperimentHistogram
		/////////////////////////////////////////////////////////////////////////////////////
		Composite layoutFullView = new Composite(parent, SWT.FILL);
		GridLayout gridFullView = new GridLayout();
		gridFullView.numColumns = 2;
		gridFullView.horizontalSpacing = 0;
		gridFullView.verticalSpacing = 0;
		gridFullView.marginHeight = 0;
		gridFullView.marginWidth = 0;
		layoutFullView.setLayout(gridFullView);

		
		/////////////////////////////////////////////////////////////////////////////////////
		// Layout that contain the time spinner
		// Contains : 
		// 		NanosecTextGroup  spTimeRangeWindow
		// 		NanosecTextGroup  spCurrentWindowTime
		// 		NanosecTextGroup  spCurrentEventTime
		/////////////////////////////////////////////////////////////////////////////////////
		Composite layoutTimesSpinner = new Composite(layoutFullView, SWT.NONE);
		GridLayout gridTimesSpinner = new GridLayout();
		gridTimesSpinner.numColumns = 3;
		gridTimesSpinner.marginHeight = 0;
		gridTimesSpinner.marginWidth = 0;
		gridTimesSpinner.horizontalSpacing = 0;
		gridTimesSpinner.verticalSpacing = 0;
		layoutTimesSpinner.setLayout(gridTimesSpinner);
		
		GridData gridDataCurrentEvent = new GridData();
		gridDataCurrentEvent.horizontalAlignment = SWT.LEFT;
		gridDataCurrentEvent.verticalAlignment = SWT.CENTER;
		ntgCurrentEventTime = new TimeTextGroup(this, layoutTimesSpinner, SWT.BORDER, SWT.BORDER, EVENT_CURRENT_TIME_LABEL_TEXT, HistogramConstant.formatNanoSecondsTime( 0L ), doesTimeTextGroupNeedAdjustment);
		ntgCurrentEventTime.setLayoutData(gridDataCurrentEvent);		
		
		GridData gridDataTimeRange = new GridData();
		gridDataCurrentEvent.horizontalAlignment = SWT.CENTER;
		gridDataCurrentEvent.verticalAlignment = SWT.CENTER;
		ntgTimeRangeWindow = new TimeTextGroup(this, layoutTimesSpinner, SWT.BORDER, SWT.BORDER, WINDOW_TIMERANGE_LABEL_TEXT, HistogramConstant.formatNanoSecondsTime( 0L ), doesTimeTextGroupNeedAdjustment);
		ntgTimeRangeWindow.setLayoutData(gridDataTimeRange);
		 		
		GridData gridDataCurrentWindow = new GridData();
		gridDataCurrentEvent.horizontalAlignment = SWT.RIGHT;
		gridDataCurrentEvent.verticalAlignment = SWT.CENTER;	
		ntgCurrentWindowTime = new TimeTextGroup(this, layoutTimesSpinner, SWT.BORDER, SWT.BORDER, WINDOW_CURRENT_TIME_LABEL_TEXT, HistogramConstant.formatNanoSecondsTime( 0L ), doesTimeTextGroupNeedAdjustment);
		ntgCurrentWindowTime.setLayoutData(gridDataCurrentWindow);
		
		
		/////////////////////////////////////////////////////////////////////////////////////
		// Layout that contain the SelectionWindow
		// Contains : 
		// 		Label txtWindowStartTime
		// 		Label txtWindowStopTime
		// 		Label txtWindowMaxNbEvents
		// 		Label txtWindowMinNbEvents
		// 		ChildrenHistogramCanvas selectedWindowCanvas
		/////////////////////////////////////////////////////////////////////////////////////
		Composite layoutSelectionWindow = new Composite(layoutFullView, SWT.FILL);
		GridLayout gridSelectionWindow = new GridLayout();
		gridSelectionWindow.numColumns = 3;
		gridSelectionWindow.marginHeight = 0;
		gridSelectionWindow.marginWidth = 0;
		gridSelectionWindow.horizontalSpacing = 0;
		gridSelectionWindow.verticalSpacing = 0;
		layoutSelectionWindow.setLayout(gridSelectionWindow);
		
		GridData gridDataSelectionWindow = new GridData();
		gridDataSelectionWindow.horizontalAlignment = SWT.FILL;
		gridDataSelectionWindow.verticalAlignment = SWT.FILL;	
		layoutSelectionWindow.setLayoutData(gridDataSelectionWindow);
		
		GridData gridDataSelectionWindowCanvas = new GridData();
		gridDataSelectionWindowCanvas.horizontalSpan = 2;
		gridDataSelectionWindowCanvas.verticalSpan = 2;
		gridDataSelectionWindowCanvas.horizontalAlignment = SWT.FILL;
		gridDataSelectionWindowCanvas.grabExcessHorizontalSpace = true;
		gridDataSelectionWindowCanvas.verticalAlignment = SWT.FILL;
/*
 		// 2010-06-20 Yuriy 
		gridDataSelectionWindowCanvas.heightHint = SELECTED_WINDOW_CANVAS_HEIGHT;
		gridDataSelectionWindowCanvas.minimumHeight = SELECTED_WINDOW_CANVAS_HEIGHT;
*/		
		gridDataSelectionWindowCanvas.widthHint = selectedCanvasWidth;
		gridDataSelectionWindowCanvas.minimumWidth = selectedCanvasWidth;
		selectedWindowCanvas = new ChildrenHistogramCanvas(this, layoutSelectionWindow, SWT.BORDER);
		selectedWindowCanvas.setLayoutData(gridDataSelectionWindowCanvas);
		
		GridData gridDataWindowMaxEvents = new GridData();
		gridDataWindowMaxEvents.horizontalAlignment = SWT.RIGHT;
		gridDataWindowMaxEvents.verticalAlignment = SWT.TOP;
		// Force a width, to avoid the control to enlarge if the number of events change
		gridDataWindowMaxEvents.minimumWidth = nbEventWidth;
		txtWindowMaxNbEvents = new Text(layoutSelectionWindow, SWT.READ_ONLY);
		txtWindowMaxNbEvents.setFont(smallFont);
		txtWindowMaxNbEvents.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		txtWindowMaxNbEvents.setEditable(false);
		txtWindowMaxNbEvents.setText("0");
		txtWindowMaxNbEvents.setLayoutData(gridDataWindowMaxEvents);
		
		GridData gridDataWindowMinEvents = new GridData();
		gridDataWindowMinEvents.horizontalAlignment = SWT.RIGHT;
		gridDataWindowMinEvents.verticalAlignment = SWT.BOTTOM;
		// Force a width, to avoid the control to enlarge if the number of events change
		gridDataWindowMinEvents.minimumWidth = nbEventWidth;
		txtWindowMinNbEvents = new Text(layoutSelectionWindow, SWT.READ_ONLY);
		txtWindowMinNbEvents.setFont(smallFont);
		txtWindowMinNbEvents.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		txtWindowMinNbEvents.setEditable(false);
		txtWindowMinNbEvents.setText("0");
		txtWindowMinNbEvents.setLayoutData(gridDataWindowMinEvents);
		
		GridData gridDataWindowStart = new GridData();
		gridDataWindowStart.horizontalAlignment = SWT.LEFT;
		gridDataWindowStart.verticalAlignment = SWT.BOTTOM;
		txtWindowStartTime = new Text(layoutSelectionWindow, SWT.READ_ONLY);
		txtWindowStartTime.setFont(smallFont);
		txtWindowStartTime.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		txtWindowStartTime.setEditable(false);
		txtWindowStartTime.setText("0.000000000");
		txtWindowStartTime.setLayoutData(gridDataWindowStart);
		
		GridData gridDataWindowStop = new GridData();
		gridDataWindowStop.horizontalAlignment = SWT.RIGHT;
		gridDataWindowStop.verticalAlignment = SWT.BOTTOM;
		txtWindowStopTime = new Text(layoutSelectionWindow, SWT.READ_ONLY);
		txtWindowStopTime.setFont(smallFont);
		txtWindowStopTime.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		txtWindowStopTime.setEditable(false);
		txtWindowStopTime.setText("0.000000000");
		txtWindowStopTime.setLayoutData(gridDataWindowStop);
		

/*
		// 2010-06-10 Yuriy: NOT NEEDED AFTER GUI IMPROVEMENTS. WORK FINE WITOUT THIS HACK 
		// *** HACK ***
		// To align properly AND to make sure the canvas size is fixed, we NEED to make sure all "section" of the 
		//		gridlayout are taken (and if possible of a fixed size).
		// However, SWT is VERY VERY DUMB and won't consider griddata that contain no control. 
		// Since there will be missing a section, the SelectedWindowCanvas + NbEventsText will take 3 spaces, but
		//		startTimeText + stopTimeText will take only 2 (as if empty the other griddata of 1 will get ignored).
		// StopTime will then take over the missing space; I want to align "stopTime" right on the end of canvas, so 
		// 		the added space to stop time would make it being aligned improperly
		// So I NEED the empty griddata to be considered! 
		// Visually : 
		// |---------------|---------------|-----------|
		// |SelectionCanvas SelectionCanvas|NbEventText|
		// |SelectionCanvas SelectionCanvas|NbEventText|
		// |---------------|---------------|-----------|
		// |StartTime      |       StopTime|    ???    |
		// |---------------|---------------|-----------|
		//
		// So since SWT will only consider griddata with control, 
		//		I need to create a totally useless control in the ??? section.
		// That's ugly, useless and it is generally a bad practice.
		//
		// *** SUB-HACK ***
		// Other interesting fact about SWT : the way it draws (Fill/Expand control in grid) will change if 
		//		the control is a Text or a Label. 
		// A Label here will be "pushed" by startTime/stopTime Text and won't fill the full space as NbEventText.
		// A Text  here will NOT be "pushed" and would give a nice visual output.
		// 		(NB : No, I am NOT kidding, try it for yourself!)
		//
		// Soooooo I guess I will use a Text here. Way to go SWT!
		// Downside is that disabled textbox has a slightly different color (even if you force it yourself) so if I want
		//		to make the text "invisible", I have to keep it enabled (but read only), so it can be clicked on.
		//
		// Label uselessControlToByPassSWTStupidBug = new Label(layoutSelectionWindow, SWT.BORDER); // WON'T align correctly!!!
		//GridData gridDataSpacer = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		GridData gridDataSpacer = new GridData();
		gridDataWindowStop.horizontalAlignment = SWT.FILL;
		gridDataWindowStop.verticalAlignment = SWT.TOP;
		gridDataSpacer.minimumWidth = nbEventWidth;
		Text uselessControlToByPassSWTStupidBug = new Text(layoutSelectionWindow, SWT.READ_ONLY); // WILL align correctly!!!
		uselessControlToByPassSWTStupidBug.setEditable(false);
		uselessControlToByPassSWTStupidBug.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		uselessControlToByPassSWTStupidBug.setLayoutData(gridDataSpacer);
*/		
		
		
		/////////////////////////////////////////////////////////////////////////////////////
		// Layout that contain the complete experiment histogram and related controls.
		// Contains : 
		// 		ParentHistogramCanvas fullExperimentCanvas
		//		Text txtExperimentStartTime
		//		Text txtExperimentStopTime
		/////////////////////////////////////////////////////////////////////////////////////
		Composite layoutExperimentHistogram = new Composite(layoutFullView, SWT.FILL);
		
		GridLayout gridExperimentHistogram = new GridLayout();
		gridExperimentHistogram.numColumns = 2;
		gridExperimentHistogram.marginHeight = 0;
		gridExperimentHistogram.marginWidth = 0;
		gridExperimentHistogram.horizontalSpacing = 0;
		gridExperimentHistogram.verticalSpacing = 0;
		layoutExperimentHistogram.setLayout(gridExperimentHistogram);
		
/*
		// 2010-06-10 Yuriy: NOT NEEDED AFTER GUI IMPROVEMENTS
		GridData gridDataExperimentHistogram = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		layoutExperimentHistogram.setLayoutData(gridDataExperimentHistogram);
*/
		
		// *** Everything related to the experiment canvas is below
		GridData gridDataExperimentCanvas = new GridData();
		gridDataExperimentCanvas.horizontalSpan = 2;
		gridDataExperimentCanvas.horizontalAlignment = SWT.FILL;
		gridDataExperimentCanvas.grabExcessHorizontalSpace = true;
		gridDataExperimentCanvas.verticalAlignment = SWT.FILL;
		gridDataExperimentCanvas.grabExcessVerticalSpace = true;
/*		
		// 2010-06-20 Yuriy: We use the dynamic height.
		gridDataExperimentCanvas.heightHint = FULL_TRACE_CANVAS_HEIGHT;
		gridDataExperimentCanvas.minimumHeight = FULL_TRACE_CANVAS_HEIGHT;
*/		
		fullExperimentCanvas = new ParentHistogramCanvas(this, layoutExperimentHistogram, SWT.BORDER);
		fullExperimentCanvas.setLayoutData(gridDataExperimentCanvas);
		layoutExperimentHistogram.setLayoutData(gridDataExperimentCanvas);
		
		GridData gridDataExperimentStart = new GridData();
		gridDataExperimentStart.horizontalAlignment = SWT.LEFT;
		gridDataExperimentStart.verticalAlignment = SWT.BOTTOM;
		txtExperimentStartTime = new Text(layoutExperimentHistogram, SWT.READ_ONLY);
		txtExperimentStartTime.setFont(smallFont);
		txtExperimentStartTime.setText("0.000000000");
		txtExperimentStartTime.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		txtExperimentStartTime.setEditable(false);
		txtExperimentStartTime.setLayoutData(gridDataExperimentStart);
		
		GridData gridDataExperimentStop = new GridData();
		gridDataExperimentStop.horizontalAlignment = SWT.RIGHT;
		gridDataExperimentStop.verticalAlignment = SWT.BOTTOM;
		txtExperimentStopTime = new Text(layoutExperimentHistogram, SWT.READ_ONLY);
		txtExperimentStopTime.setFont(smallFont);
		txtExperimentStopTime.setText("0.000000000");
		txtExperimentStopTime.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		txtExperimentStopTime.setEditable(false);
		txtExperimentStopTime.setLayoutData(gridDataExperimentStop);
	}
	
	// *** FIXME ***
	// This is mainly used because of a because in the "experimentSelected()" notification, we shouldn't need this
	/**
	 * Method called when the view receive the focus.<p>
	 * If ExperimentSelected didn't send us a request yet, get the current Experiment and fire requests
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setFocus() {
		// WARNING : This does not seem to be thread safe
		TmfExperiment<LttngEvent> tmpExperiment = (TmfExperiment<LttngEvent>)TmfExperiment.getCurrentExperiment();
		
		if ( (dataBackgroundFullRequest == null) && (tmpExperiment != null) ) {
			createCanvasAndRequests(tmpExperiment);
		}
		
		// Call a redraw for everything
		parent.redraw();
	}
	
	/**
	 * Method called when the user select (double-click on) an experiment.<p>
	 * We will create the needed canvas and fire the requests.
	 * 
	 * @param signal	Signal received from the framework. Contain the experiment.
	 */
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
    /**
	 * Method called when an experiment is updated (??).<p>
	 * ...for now, do nothing, as udating an experiment running in the background might cause crash 
	 * 
	 * @param signal	Signal received from the framework. Contain the experiment.
	 */
/*
    @SuppressWarnings("unchecked")
	@TmfSignalHandler
    public void experimentUpdated(TmfExperimentUpdatedSignal signal) {
		    	
		TmfExperiment<LttngEvent> tmpExperiment = (TmfExperiment<LttngEvent>)signal.getExperiment();
		    	
		// Make sure the UI object are sane
		resetControlsContent();
				
		// Redraw the canvas right away to have something "clean" as soon as we can
		fullExperimentCanvas.redraw();
		selectedWindowCanvas.redraw();
		
		// Recreate the request
		createCanvasAndRequests(tmpExperiment);
    }
*/
    
    /**
     * Method called when synchonization is active and that the user select an event.<p>
     * We update the current event timeTextGroup and move the selected window if needed.
     * 
     * @param signal	Signal received from the framework. Contain the event.
     */
    @TmfSignalHandler
    public void currentTimeUpdated(TmfTimeSynchSignal signal) {
    	// In case we received our own signal
    	if ( (signal != null) && (signal.getSource() != this) ) {
            TmfTimestamp currentTime = signal.getCurrentTime();
            
            // Update the current event controls
            currentEventTime = currentTime.getValue();
            updateSelectedEventTime();
            
            // If the given event is outside the selection window, recenter the window
            if ( isGivenTimestampInSelectedWindow( currentEventTime ) == false) {
            	fullExperimentCanvas.setWindowCenterPosition( fullExperimentCanvas.getHistogramContent().getClosestXPositionFromTimestamp(currentEventTime) );
            	// Notify control that the window changed
            	windowChangedNotification();
            	// Send a broadcast to the framework about the window change
        		sendTmfRangeSynchSignalBroadcast();
            }
    	}
    }
    
    @TmfSignalHandler
	public void synchToTimeRange(TmfRangeSynchSignal signal) {
		if ( (signal != null) && (signal.getSource() != this) ) {
			if ( lastUsedExperiment != null ) {
				long currentTime 		= signal.getCurrentTime().getValue();
				long windowStart 		= signal.getCurrentRange().getStartTime().getValue();
				long windowEnd 			= signal.getCurrentRange().getEndTime().getValue();
				long windowTimeWidth 	= (windowEnd - windowStart); 
				
				// Recenter the window
				fullExperimentCanvas.setSelectedWindowSize(windowTimeWidth);
				fullExperimentCanvas.setWindowCenterPosition( fullExperimentCanvas.getHistogramContent().getClosestXPositionFromTimestamp(windowStart + (windowTimeWidth/2)) );
				
				// *** HACK ***
				// Views could send us incorrect current event value (event outside the current window)
				// Here we make sure the value is sane, otherwise, we force it as the left border of the window
				if ( isGivenTimestampInSelectedWindow( currentTime ) == false ) {
					currentTime = windowStart;
				}
				currentEventTime = currentTime;
				
				// Notify control that the window changed
            	windowChangedNotification();
            	
            	// Make sure we redraw the change
            	fullExperimentCanvas.redraw();
			}
		}
	}
    
    
    /*
     * Create the canvas needed and issue the requests
     * 
     * @param newExperiment	Experiment we will use for the request
     */
    private void createCanvasAndRequests(TmfExperiment<LttngEvent> newExperiment) {
    	// Save the experiment we are about to use
    	lastUsedExperiment = newExperiment;
    	
//    	// Create a copy of the trace that will be use only by the full experiment request
//    	TmfExperiment<LttngEvent> experimentCopy = newExperiment.createTraceCopy();
    	
    	// Create the content for the full experiment. 
    	// This NEED to be created first, as we use it in the selectedWindowCanvas
		fullExperimentCanvas.createNewHistogramContent(
			fullExperimentCanvas.getSize().x,
			FULL_TRACE_BAR_WIDTH,
/*
			// 2010-06-20 Yuriy: We will use the dynamic height.
			FULL_TRACE_CANVAS_HEIGHT
*/
		  	fullExperimentCanvas.getSize().y / 2,
			FULL_TRACE_DIFFERENCE_TO_AVERAGE
		);

		TmfTimeRange timeRange = getExperimentTimeRange(newExperiment);

		// We will take the half of the full experiment length in case of bigger window size than the full experiment length
		if(timeRange.getEndTime().getValue() - timeRange.getStartTime().getValue() >  DEFAULT_WINDOW_SIZE ) {
			fullExperimentCanvas.createNewSelectedWindow(
					timeRange.getStartTime().getValue(),
					DEFAULT_WINDOW_SIZE
					);
		} else {
			fullExperimentCanvas.createNewSelectedWindow(
					timeRange.getStartTime().getValue(),
					(timeRange.getEndTime().getValue() - timeRange.getStartTime().getValue() ) / 2
					);
		}

		currentEventTime = timeRange.getStartTime().getValue();

		// Set the window of the fullTrace canvas visible.
		fullExperimentCanvas.getCurrentWindow().setSelectedWindowVisible(true);
		fullExperimentCanvas.getHistogramContent().resetTable(timeRange.getStartTime().getValue(), timeRange.getEndTime().getValue());

		// Create the content for the selected window. 
		selectedWindowCanvas.createNewHistogramContent(
				selectedWindowCanvas.getSize().x,
				SELECTED_WINDOW_BAR_WIDTH,
				selectedWindowCanvas.getSize().y,
				SELECTED_WINDOW_DIFFERENCE_TO_AVERAGE
				);
		selectedWindowCanvas.getHistogramContent().resetTable(fullExperimentCanvas.getCurrentWindow().getTimestampOfLeftPosition(), fullExperimentCanvas.getCurrentWindow().getTimestampOfRightPosition());
		
    	// Make sure the UI object are sane
		resetControlsContent();
		
		// Redraw the canvas right away to have something "clean" as soon as we can
    	if ( dataBackgroundFullRequest != null ) {
    		fullExperimentCanvas.redraw();
    		selectedWindowCanvas.redraw();
    	}
    	// Nullify the (possible) old request to be sure we start we something clean
    	// Note : this is very important for the order of the request below, 
    	//	see "TODO" in performSelectedWindowEventsRequest
    	dataBackgroundFullRequest = null;
    	selectedWindowRequest = null;
		
		// Perform both request. 
		// Order is important here, the small/synchronous request for the selection window should go first
		performSelectedWindowEventsRequest(newExperiment);
		performAllTraceEventsRequest(newExperiment);
    }

	// Before completing its indexing, the experiment doesn't know start/end time.
	// However, LTTng individual traces have this knowledge so we should ask them
	// directly.
    private TmfTimeRange getExperimentTimeRange(TmfExperiment<LttngEvent> experiment) {
    	// Before completing its indexing, the experiment doesn't know start/end time.
    	// However, LTTng individual traces have this knowledge so we should ask them
    	// directly.
		TmfTimestamp startTime = TmfTimestamp.BigCrunch;
		TmfTimestamp endTime   = TmfTimestamp.BigBang;
		for (ITmfTrace trace : experiment.getTraces()) {
			TmfContext context = trace.seekLocation(null);
			context.setRank(0);
			TmfEvent event = trace.getNextEvent(context);
			TmfTimestamp traceStartTime = event.getTimestamp();
			if (traceStartTime.compareTo(startTime, true) < 0)
				startTime = traceStartTime;
			TmfTimestamp traceEndTime = trace.getEndTime();
			if (traceEndTime.compareTo(endTime, true) > 0)
				endTime = traceEndTime;
    	}
        TmfTimeRange tmpRange = new TmfTimeRange(startTime, endTime);
        return tmpRange;
    }

    /**
     * Perform a new request for the Selection window.<p>
     * This assume the full experiment canvas has correct information about the selected window; 
     * 		we need the fullExperimentCanvas' HistogramContent to be created and a selection window to be set.
     * 
     * @param experiment	The experiment we will select from
     */
    public void performSelectedWindowEventsRequest(TmfExperiment<LttngEvent> experiment) {
    	
    	if(fullExperimentCanvas != null) {
	    	HistogramSelectedWindow curSelectedWindow = fullExperimentCanvas.getCurrentWindow();
	    	
	    	TmfTimeRange timeRange = getExperimentTimeRange(experiment);
	    	
	    	// If no selection window exists, we will try to create one; 
	    	//	however this will most likely fail as the content is probably not created either
	    	if ( curSelectedWindow == null ) {
	    		// We will take the half of the full experiment length in case of bigger window size than the full experiment length
	    		if(timeRange.getEndTime().getValue() - timeRange.getStartTime().getValue() >  DEFAULT_WINDOW_SIZE ) {
	    			fullExperimentCanvas.createNewSelectedWindow(
	    					timeRange.getStartTime().getValue(),
	    					DEFAULT_WINDOW_SIZE
	    					);
	    		} else {
	    			fullExperimentCanvas.createNewSelectedWindow(
	    					timeRange.getStartTime().getValue(),
	    					(timeRange.getEndTime().getValue() - timeRange.getStartTime().getValue() ) / 2
	    					);
	    		}
	    		curSelectedWindow = fullExperimentCanvas.getCurrentWindow();
	    	}
	    	
	    	// The request will go from the Left timestamp of the window to the Right timestamp
	    	// This assume that out-of-bound value are handled by the SelectionWindow itself
			LttngTimestamp ts1 = new LttngTimestamp( curSelectedWindow.getTimestampOfLeftPosition() );
			LttngTimestamp ts2 = new LttngTimestamp( curSelectedWindow.getTimestampOfRightPosition() );
	        TmfTimeRange tmpRange = new TmfTimeRange(ts1, ts2);
	        
	        // Set a (dynamic) time interval
	        long intervalTime = ( (ts2.getValue() - ts1.getValue()) / selectedWindowCanvas.getHistogramContent().getNbElement() );
	        
			selectedWindowRequest = performRequest(experiment, selectedWindowCanvas, tmpRange, intervalTime,
					ExecutionType.SHORT);
	        selectedWindowCanvas.redrawAsynchronously();
    	}
    	
    }
    
    /**
     * Perform a new request for the full experiment.<p>
     * NOTE : this is very long, we need to implement a way to run this in parallel (see TODO)
     * 
     * @param experiment	The experiment we will select from
     */
    public void performAllTraceEventsRequest(TmfExperiment<LttngEvent> experiment) {
    	// Create a new time range from "start" to "end"
        //	That way, we will get "everything" in the trace
//        LttngTimestamp ts1 = new LttngTimestamp( experiment.getStartTime() );
//        LttngTimestamp ts2 = new LttngTimestamp( experiment.getEndTime() );
//        TmfTimeRange tmpRange = new TmfTimeRange(ts1, ts2);
    	
        TmfTimeRange tmpRange  = getExperimentTimeRange(experiment);
		TmfTimestamp startTime = tmpRange.getStartTime();
		TmfTimestamp endTime   = tmpRange.getEndTime();
        
        // Set a (dynamic) time interval
        long intervalTime = ( (endTime.getValue() - startTime.getValue()) / fullExperimentCanvas.getHistogramContent().getNbElement() );
        
        // *** VERIFY ***
        // This would enable "fixed interval" instead of dynamic one.
        // ... we don't need it, do we?
        //
        // long intervalTime = ((long)(0.001 * (double)1000000000));
        
        // *** TODO ***
        // It would be interesting if there was a way to tell the framework to run the request "in parallel" here.
        // Mean a completetly independant copy of the Expereiment would be done and we would proceed on that.
        //
        dataBackgroundFullRequest = performRequest(experiment, fullExperimentCanvas, tmpRange, intervalTime, ExecutionType.LONG);
        
		
        fullExperimentCanvas.getCurrentWindow().setWindowXPositionLeft(fullExperimentCanvas.getHistogramContent().getClosestXPositionFromTimestamp(fullExperimentCanvas.getCurrentWindow().getTimestampOfLeftPosition()));
        fullExperimentCanvas.getCurrentWindow().setWindowXPositionCenter(fullExperimentCanvas.getHistogramContent().getClosestXPositionFromTimestamp(fullExperimentCanvas.getCurrentWindow().getTimestampOfCenterPosition()));
        fullExperimentCanvas.getCurrentWindow().setWindowXPositionRight(fullExperimentCanvas.getHistogramContent().getClosestXPositionFromTimestamp(fullExperimentCanvas.getCurrentWindow().getTimestampOfRightPosition()));

        fullExperimentCanvas.redrawAsynchronously();
    }
    
    // *** VERIFY ***
    // This function is synchronized, is it a good idea?
    // Tis is done to make sure requests arrive somewhat in order, 
    //	this is especially important when request are issued from different thread.
    /**
     * Create a new request from the given data and send it to the framework.<p>
     * The request will be queued and processed later.
     * 
     * @param experiment 		The experiment we will process the request on
     * @param targetCanvas		The canvas that will received the result
     * @param newRange			The range of the request
     * @param newInterval		The interval of time we use to store the result into the HistogramContent
     */
    private synchronized HistogramRequest performRequest(TmfExperiment<LttngEvent> experiment, HistogramCanvas targetCanvas, TmfTimeRange newRange, long newInterval, ITmfDataRequest.ExecutionType execType) {
    	HistogramRequest returnedRequest = null;
    	
        // *** FIXME ***
        // EVIL BUG!
	    // We use int.MAX_VALUE because we want every events BUT we don't know the number inside the range.
        // HOWEVER, this would cause the request to run forever (or until it reach the end of trace).
        // Seeting an EndTime does not seems to stop the request
        returnedRequest = new HistogramRequest(newRange, Integer.MAX_VALUE, targetCanvas, newInterval, execType );
        
        // Send the request to the framework : it will be queued and processed later
        experiment.sendRequest(returnedRequest);
        
        return returnedRequest;
    }
    
    /**
     * Function used to warn that the selection window changed.<p>
     * This might be called because the window moved or because its size changed.<p>
     * 
     * We will update the different control related to the selection window.
     */
    public void windowChangedNotification() {
    	
    	if ( lastUsedExperiment != null ) {
    		// If a request is ongoing, try to stop it
    		if ( selectedWindowRequest != null && selectedWindowRequest.isCompleted() == false ) {
    			selectedWindowRequest.cancel();
    		}
    		
    		if(fullExperimentCanvas != null) {
	    		// If the current event time is outside the new window, change the current event
	    		//		The new current event will be the one closest to the LEFT side of the new window
	    		if ( isGivenTimestampInSelectedWindow(currentEventTime) == false ) {
	    			currentEventChangeNotification( fullExperimentCanvas.getCurrentWindow().getTimestampOfLeftPosition() );
	    		}
     		}
    		
    		// Perform a new request to read data about the new window
    		performSelectedWindowEventsRequest(lastUsedExperiment);
    	}
    }
    
    /**
     * Function used to tell that the current event changed.<p>
     * This might be called because the user changed the current event or
     * 		because the last current event is now outside the selection window.<p>
     * 
     * We update the related control and send a signal to notify other views of the new current event.
     * 
     * @param newCurrentEventTime
     */
    public void currentEventChangeNotification(long newCurrentEventTime) {
    	
    	// Notify other views in the framework
        if (currentEventTime != newCurrentEventTime) {
        	currentEventTime = newCurrentEventTime;
        	
        	// Update the UI control
        	updateSelectedEventTime();
        }
    }
    
    public void sendTmfTimeSynchSignalBroadcast() {
    	
//    	System.out.println("sendTmfTimeSynchSignalBroadcast " + System.currentTimeMillis());
    	
    	// Send a signal to the framework
        LttngTimestamp tmpTimestamp = new LttngTimestamp(currentEventTime);
        broadcast(new TmfTimeSynchSignal(this, tmpTimestamp));
    }
    
    /**
     * Function used to tell that the timerange (window) changed.<p>
     * This will most likely be called if the time window is resized.
     * 
     * We send a signal to notify other views of the new timerange.
     */
    public void sendTmfRangeSynchSignalBroadcast() {
    	
    	if (TmfExperiment.getCurrentExperiment() == null)
    		return;

    	long startTime = fullExperimentCanvas.getCurrentWindow().getTimestampOfLeftPosition();
    	if ( startTime < fullExperimentCanvas.getHistogramContent().getStartTime() ) {
    		startTime = fullExperimentCanvas.getHistogramContent().getStartTime();
    	}
        LttngTimestamp tmpStartTime = new LttngTimestamp(startTime);
        
        long endTime = fullExperimentCanvas.getCurrentWindow().getTimestampOfRightPosition();
    	if ( endTime > fullExperimentCanvas.getHistogramContent().getEndTime() ) {
    		endTime = fullExperimentCanvas.getHistogramContent().getEndTime();
    	}
    	LttngTimestamp tmpEndTime = new LttngTimestamp(endTime);
    	
        TmfTimeRange tmpTimeRange = new TmfTimeRange(tmpStartTime, tmpEndTime);
        LttngTimestamp tmpEventTime = new LttngTimestamp(currentEventTime);
        
        // Send a signal to the framework
        broadcast(new TmfRangeSynchSignal(this, tmpTimeRange, tmpEventTime));
    }
    
    /**
     * Function that will be called when one of the time text group value is changed.<p>
     * Since we don't (and can't unless we subclass them) know which one, we check them all.
     */
    public void timeTextGroupChangeNotification() {
    	
    	if(ntgCurrentEventTime != null) {
	    	// If the user changed the current event time, call the notification
	    	long newCurrentTime = ntgCurrentEventTime.getValue();
	    	if ( newCurrentTime != currentEventTime ) {
	    		currentEventChangeNotification( newCurrentTime );
	    		// Send a broadcast to the framework about the window change
	    		sendTmfTimeSynchSignalBroadcast();
	    	}
     	}
    	
    	if(ntgCurrentWindowTime != null && fullExperimentCanvas != null) {
	    	// If the user changed the selected window time, recenter the window and call the notification
	    	long newSelectedWindowTime = ntgCurrentWindowTime.getValue();
	    	if ( newSelectedWindowTime != fullExperimentCanvas.getCurrentWindow().getTimestampOfCenterPosition() ) {
	    		fullExperimentCanvas.setWindowCenterPosition(newSelectedWindowTime);	
	    		windowChangedNotification();
	    		// Send a broadcast to the framework about the window change
	    		sendTmfRangeSynchSignalBroadcast();
	    	}
    	}
    	
    	if(ntgTimeRangeWindow != null && fullExperimentCanvas != null) {
	    	// If the user changed the selected window size, resize the window and call the notification
	    	long newSelectedWindowTimeRange = ntgTimeRangeWindow.getValue();
	    	if ( newSelectedWindowTimeRange != fullExperimentCanvas.getCurrentWindow().getWindowTimeWidth() ) {
	    		fullExperimentCanvas.resizeWindowByAbsoluteTime(newSelectedWindowTimeRange);
	    		windowChangedNotification();
	    		// Send a broadcast to the framework about the window change
	    		sendTmfRangeSynchSignalBroadcast();
	    	}
    	}
    	
    }
    
    /**
     * Getter for the last used experiment.<p>
     * This might be different than the current experiment or even null.
     * 
     * @return	the last experiment we used in this view
     */
	public TmfExperiment<LttngEvent> getLastUsedExperiment() {
		return lastUsedExperiment;
	}
	
	/**
	 * Check if a given timestamp is inside the selection window.<p>
	 * This assume fullExperimentCanvas contain a valid HistogramContent
	 * 
	 * @param timestamp	the timestamp to check
	 * 
	 * @return	if the time is inside the selection window or not
	 */
	public boolean isGivenTimestampInSelectedWindow(long timestamp) {
		boolean returnedValue = true;
		
		// If the content is not set correctly, this will return weird (or even null) result
		if ( (timestamp < fullExperimentCanvas.getCurrentWindow().getTimestampOfLeftPosition()  ) ||
	         (timestamp > fullExperimentCanvas.getCurrentWindow().getTimestampOfRightPosition() ) ) 
		{
			returnedValue = false;
		}
		
		return returnedValue;
	}
	
	/**
	 * Reset the content of all Controls.<p>
	 * WARNING : Calls in there are not thread safe and can't be called from different thread than "main"
	 */
	public void resetControlsContent() {
		
		TmfExperiment<LttngEvent> tmpExperiment = getLastUsedExperiment();
		
		// Use the previous Start and End time, or default if they are not available
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
		
		txtWindowMaxNbEvents.setText("" + 0);
		txtWindowMinNbEvents.setText("" + 0);
		txtWindowStartTime.setText( HistogramConstant.formatNanoSecondsTime( 0L ) );
		txtWindowStopTime.setText( HistogramConstant.formatNanoSecondsTime( 0L ) );
		txtWindowStartTime.getParent().layout();
		
		ntgCurrentWindowTime.setValue( HistogramConstant.formatNanoSecondsTime( 0L ) );
		ntgTimeRangeWindow.setValue( HistogramConstant.formatNanoSecondsTime( 0L ) );
		
		// Using "startTime" here can avoid an useless TmfTimeSynchSignal here
		// However it look ugly to have only this time
		ntgCurrentEventTime.setValue( HistogramConstant.formatNanoSecondsTime( 0L ) );
	}
	
	/**
	 * Update the content of the controls related to the full experiment canvas<p>
	 * WARNING : Calls in there are not thread safe and can't be called from different thread than "main"
	 */
	public void updateFullExperimentInformation() {
		
		if(fullExperimentCanvas != null) {
			String startTime = HistogramConstant.formatNanoSecondsTime( fullExperimentCanvas.getHistogramContent().getStartTime() );
			String stopTime = HistogramConstant.formatNanoSecondsTime( fullExperimentCanvas.getHistogramContent().getEndTime() );
			
			txtExperimentStartTime.setText( startTime );
			txtExperimentStopTime.setText( stopTime );
		}
		
		// Take one of the parent and call its layout to update control size
		// Since both control have the same parent, only one call is needed 
		txtExperimentStartTime.getParent().layout();
		
		// Update the selected window, just in case
		// This should give a better user experience and it is low cost 
		updateSelectedWindowInformation();
	}
	
	/**
	 * Update the content of the controls related to the selection window canvas<p>
	 * WARNING : Calls in there are not thread safe and can't be called from different thread than "main"
	 */
	public void updateSelectedWindowInformation() {
		// Update the timestamp as well
		updateSelectedWindowTimestamp();
		
		if(selectedWindowCanvas != null) {
			txtWindowMaxNbEvents.setText( Long.toString(selectedWindowCanvas.getHistogramContent().getHeighestEventCount()) );
			txtWindowMinNbEvents.setText(Long.toString(0));
		}
		
		// Refresh the layout
		txtWindowMaxNbEvents.getParent().layout();
	}
	
	/**
	 * Update the content of the controls related to the timestamp of the selection window<p>
	 * WARNING : Calls in there are not thread safe and can't be called from different thread than "main"
	 */
	public void updateSelectedWindowTimestamp() {
		
		if(selectedWindowCanvas != null) {
			String startTime = HistogramConstant.formatNanoSecondsTime( selectedWindowCanvas.getHistogramContent().getStartTime() );
			String stopTime = HistogramConstant.formatNanoSecondsTime( selectedWindowCanvas.getHistogramContent().getEndTime() );
			txtWindowStartTime.setText( startTime );
			txtWindowStopTime.setText( stopTime );
		}
		
		if(fullExperimentCanvas != null) {
			ntgCurrentWindowTime.setValue( fullExperimentCanvas.getCurrentWindow().getTimestampOfCenterPosition() );
			ntgTimeRangeWindow.setValue(  fullExperimentCanvas.getCurrentWindow().getWindowTimeWidth() );
			
			// If the current event time is outside the selection window, recenter our window 
			if ( isGivenTimestampInSelectedWindow(ntgCurrentEventTime.getValue()) == false ) {
				currentEventChangeNotification( fullExperimentCanvas.getCurrentWindow().getTimestampOfCenterPosition() );
			}
		}
		
		// Take one control in each group to call to refresh the layout
		// Since both control have the same parent, only one call is needed 
		txtWindowStartTime.getParent().layout();
		ntgCurrentWindowTime.getParent().layout();
	}
	
	/**
	 * Update the controls related current event.<p>
	 * The call here SHOULD be thread safe and can be call from any threads.
	 */
	public void updateSelectedEventTime() {
    	ntgCurrentEventTime.setValueAsynchronously(currentEventTime);
    	// Tell the selection canvas which event is currently selected
    	// This give a nice graphic output
    	selectedWindowCanvas.getHistogramContent().setSelectedEventTimeInWindow(currentEventTime);
    	selectedWindowCanvas.redrawAsynchronously();
	}
	
	/**
	 * Method called when the view is moved.<p>
	 * 
	 * Just redraw everything...
	 * 
	 * @param event 	The controle event generated by the move.
	 */
	public void controlMoved(ControlEvent event) {
		parent.redraw();
	}
	
	/**
	 * Method called when the view is resized.<p>
	 * 
	 * We will make sure that the size didn't change more than the content size.<p>
	 * Otherwise we need to perform a new request for the full experiment because we are missing data).
	 * 
	 * @param event 	The control event generated by the resize.
	 */
	public void controlResized(ControlEvent event) {
		
		// Ouch! The screen enlarged (screen resolution changed?) so far that we miss content to fill the space.
		if ( parent.getDisplay().getBounds().width > fullExperimentCanvas.getHistogramContent().getNbElement() ) {
			if ( lastUsedExperiment != null ) {
				createCanvasAndRequests(lastUsedExperiment);
			}
		}
		
	}

	/*
	 * Getter of FullExperimentCanvas
	 * 
	 * @return FullExperimentCanvas object
	 */
	public static ParentHistogramCanvas getFullExperimentCanvas() {
		return fullExperimentCanvas;
	}

	/*
	 * Getter of SelectedWindowCanvas
	 * 
	 * @return SelectedWindowCanvas object
	 */
	public static ChildrenHistogramCanvas getSelectedWindowCanvas() {
		return selectedWindowCanvas;
	}

	
	/*
	 * Getter of DEFAULT_WINDOW_SIZE
	 * 
	 * @return DEFAULT_WINDOW_SIZE value
	 */
	public static long getDEFAULT_WINDOW_SIZE() {
		return DEFAULT_WINDOW_SIZE;
	}

	/**
	 * Getter for dataBackgroundFullRequest variable
	 * @return the dataBackgroundFullRequest instance
	 */
	public HistogramRequest getDataBackgroundFullRequest() {
		return dataBackgroundFullRequest;
	}

	/**
	 * Getter for selectedWindowRequest variable
	 * @return the selectedWindowRequest instance
	 */
	public HistogramRequest getSelectedWindowRequest() {
		return selectedWindowRequest;
	}

}
