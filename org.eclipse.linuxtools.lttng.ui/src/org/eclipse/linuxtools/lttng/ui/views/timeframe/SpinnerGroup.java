/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.timeframe;

import org.eclipse.linuxtools.lttng.ui.views.Labels;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

// ========================================================================
// SpinnerGroup
// ========================================================================

/**
 * <b><u>SpinnerGroup</u></b>
 * <p>
 * A SpinnerGroup holds two coordinated spinners (for seconds and
 * nanoseconds) representing the current time within the trace.
 * <p>
 * The current time can take any value anything within the time range (start
 * and end time).
 */
public class SpinnerGroup {

    // The nanosecond scale (10^9)
    private static final int NANOSECOND_SCALE = 1000 * 1000 * 1000;
    private static final byte SCALE = -9;

    // Widgets
    private Group group;
    private Spinner seconds;
    private Spinner nanosec;

    // The valid time range - start time
    private TmfTimestamp startTime;
    private int startSeconds;
    private int startNanosec;

    // The valid time range - end time
    private TmfTimestamp endTime;
    private int endSeconds;
    private int endNanosec;

    // The current time value
    private TmfTimestamp currentTime;
    private int currentSeconds;
    private int currentNanosec;
    
	private TimeFrameView fOwner;

    /**
     * <b><u>Constructor</u></b>
     * <p>
     * <li>Creates the display group and formats it for the grid cell
     * <li>Sets the initial values for Start/End/Current time
     * </li>
     * <p>
     * @param parent    - the parent Composite
     * @param groupName - the group name
     * @param range     - the valid time range (start/end time)
     * @param current   - the current time
     */
    public SpinnerGroup(TimeFrameView owner, Composite parent, String groupName, TmfTimeRange range, TmfTimestamp current) {

    	fOwner = owner;

        // Create the group
        group = new Group(parent, SWT.BORDER);
        group.setText(groupName);

        // Make it use the whole grid cell
        GridData gridData = new GridData(SWT.LEFT, SWT.TOP, true, false);
        gridData.horizontalAlignment = SWT.FILL;
        group.setLayoutData(gridData);

        // Create and position the widgets
        seconds = new Spinner(group, SWT.BORDER);
        seconds.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                currentSeconds = seconds.getSelection();
                refreshCurrentTime();
            }
        });
        seconds.setBounds(5, 25, 110, 25);

        Label label = new Label(group, SWT.LEFT);
        label.setText(Labels.TimeFrameView_Seconds);
        label.setBounds(120, 28, 25, 22);

        nanosec = new Spinner(group, SWT.BORDER);
        nanosec.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                currentNanosec = nanosec.getSelection();
                // Correct for nanosec underflow
                if (currentNanosec < 0) {
                    currentSeconds--;
                    currentNanosec = NANOSECOND_SCALE - 1;
                }
                // Correct for nanosec overflow
                if (currentNanosec >= NANOSECOND_SCALE) {
                    currentSeconds++;
                    currentNanosec = 0;
                }
                refreshCurrentTime();
            }
        });
        nanosec.setBounds(150, 25, 110, 25);

        label = new Label(group, SWT.LEFT);
        label.setText(Labels.TimeFrameView_Nanosec);
        label.setBounds(265, 28, 25, 22);

        setContent(range, current);
    }

    private void refreshCurrentTime() {
        long newCurrentTime = (long) currentSeconds * NANOSECOND_SCALE + currentNanosec;
        TmfTimestamp ts = new TmfTimestamp(newCurrentTime, SCALE, 0);
        currentTime = ts;
        fOwner.synchTimeFrameWidgets(this);
    }

    // ====================================================================
    // Get/Set
    // ====================================================================

    public TmfTimestamp getStartTime() {
        return startTime;
    }

    public TmfTimestamp getEndTime() {
        return endTime;
    }

    public TmfTimestamp getCurrentTime() {
        return currentTime;
    }

    public TmfTimestamp getSpan() {
        TmfTimestamp span = new TmfTimestamp(startTime.getAdjustment(endTime), SCALE, 0);
        return span;
    }

    public TmfTimeRange getTimeRange() {
        TmfTimeRange range = new TmfTimeRange(startTime, endTime);
        return range;
    }

    public void setStartTime(TmfTimestamp ts) {
        startTime = ts.synchronize(0, SCALE);
        startSeconds = (int) (startTime.getValue() / NANOSECOND_SCALE);
        startNanosec = (int) (startTime.getValue() % NANOSECOND_SCALE);
    }

    public void setEndTime(TmfTimestamp ts) {
        endTime = ts.synchronize(0, SCALE);
        endSeconds = (int) (endTime.getValue() / NANOSECOND_SCALE);
        endNanosec = (int) (endTime.getValue() % NANOSECOND_SCALE);
    }

    public void setCurrentTime(TmfTimestamp ts) {
        currentTime = ts.synchronize(0, SCALE);
        currentSeconds = (int) (currentTime.getValue() / NANOSECOND_SCALE);
        currentNanosec = (int) (currentTime.getValue() % NANOSECOND_SCALE);
    }

    // ====================================================================
    // Operators
    // ====================================================================

    /**
     * <b><u>setContent</u></b>
     * <p>
     * <li>validates that [startTime <= currentTime <= endTime] is respected
     * <li>sets the start/current/end time and update the spinners
     * </li>
     * <p>
     * 
     * @param range
     * @param current
     */
    public void setContent(TmfTimeRange range, TmfTimestamp current) {

    	if (range != null) {
        	// Extract the time range
            TmfTimestamp start = range.getStartTime();
            TmfTimestamp end = range.getEndTime();

            // Assume start time is OK
            setStartTime(start);

            // Make sure end time >= start time
            if (end.compareTo(start, false) < 0) {
                end = start;
            }
            setEndTime(end);

            // Make sure [start time <= current time <= end time]
            // If not: current = min(max(start, current), end);
            if (current.compareTo(start, false) < 0) {
                current = start;
            }
            if (current.compareTo(end, false) > 0) {
                current = end;
            }
    	}
        setCurrentTime(current);

        // And configure the spinners
        updateSpinners();
    }

    /**
     * <b><u>setValue</u></b>
     * <p>
     * <li>validates that [startTime <= currentTime <= endTime] is respected
     * <li>sets the current time and the spinners
     * </li>
     * <p>
     * 
     * @param range
     * @param current
     */
    public void setValue(TmfTimestamp current) {

        // Make sure [start time <= current time <= end time]
        // If not: current = min(max(start, current), end);
        if (current.compareTo(startTime, false) < 0) {
            current = startTime;
        }
        if (current.compareTo(endTime, false) > 0) {
            current = endTime;
        }
        setCurrentTime(current);

        // And configure the spinners
        updateSpinners();
    }

    /**
     * Update the spinners with the new current time value
     * Perform the update on the UI thread
     */
    public void updateSpinners() {

        seconds.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!seconds.isDisposed() && !nanosec.isDisposed()) {
		            // If we are on the start second, ensure that [currentNS >= startNS]
		            // If the currentSeconds > startSeconds, set startns to -1 so we can
		            // "underflow"
		            int startns = -1;
		            if (currentSeconds <= startSeconds) {
		                currentSeconds = startSeconds;
		                startns = startNanosec;
		                if (currentNanosec < startns) {
		                    currentNanosec = startns;
		                }
		            }

		            // If we are on the end second, ensure that [currentNS <= endNS]
		            // If the currentSeconds < endSeconds, set endns to MAX so we can
		            // "overflow"
		            int endns = NANOSECOND_SCALE;
		            if (currentSeconds >= endSeconds) {
		                currentSeconds = endSeconds;
		                endns = endNanosec;
		                if (currentNanosec > endns) {
		                    currentNanosec = endns;
		                }
		            }

		            // Refresh the spinners (value, range, increments, ...)
					// To ensure that the spinners are properly set, the range has to be > 0 
					seconds.setValues(currentSeconds, startSeconds - 1, endSeconds + 1, 0, 1, 10);
					nanosec.setValues(currentNanosec, startns - 1, endns + 1, 0, 1, 1000000);

		            // If start == end (i.e. no range), disable the spinner
		            // (if start == end, the spinner widget range is set to [0..100] by default)
		            seconds.setEnabled(startSeconds != endSeconds);
		            nanosec.setEnabled(startns != endns);
				}
			}
        });
    }
};
///**
// * <b><u>LTTngTimeFrameView</u></b>
// * <p>
// * The TimeFrameView provides a set of spinners to monitor and set the start
// * time, end time, the current time interval and current time of the event log
// * set at the nanosecond level.
// * <p>
// * It ensures that the following relations are always true:
// * <p>
// * <li>[ startTime >= start time of the log set ]
// * <li>[ endTime <= end time of the log set ]
// * <li>[ startTime <= currentTime <= endTime ]
// * <li>[ interval == (endTime - startTime) ]
// * </li>
// * <p>
// * It provides a slider to rapidly set the current time within the time range
// * (i.e. between startTime and endTime).
// * <p>
// * Finally, it allows modification of the time range and the current time. This
// * triggers notifications to the other LTTng views.
// * <p>
// * TODO: View synchronization
// */
//public class SpinnerGroup {
//
////    public static final String ID = Labels.TimeFrameView_ID;
////
////    // ========================================================================
////    // SpinnerGroup
////    // ========================================================================
//
//    /**
//     * <b><u>SpinnerGroup</u></b>
//     * <p>
//     * A SpinnerGroup holds two coordinated spinners (for seconds and
//     * nanoseconds) representing the current time within the trace.
//     * <p>
//     * The current time can take any value anything within the time range (start
//     * and end time).
//     */
////    private class SpinnerGroup {
//
//        // The nanosecond scale (10^9)
//        private static final int NANOSECOND_SCALE = 1000 * 1000 * 1000;
//        private static final byte SCALE = -9;
//
//        // Widgets
//        private Group group;
//        private Spinner seconds;
//        private Spinner nanosec;
//
//        // The valid time range - start time
//        private TmfTimestamp startTime;
//        private int startSeconds;
//        private int startNanosec;
//
//        // The valid time range - end time
//        private TmfTimestamp endTime;
//        private int endSeconds;
//        private int endNanosec;
//
//        // The current time value
//        private TmfTimestamp currentTime;
//        private int currentSeconds;
//        private int currentNanosec;
//
//        private final TimeFrameView fOwner;
//
//        /**
//         * <b><u>Constructor</u></b>
//         * <p>
//         * <li>Creates the display group and formats it for the grid cell
//         * <li>Sets the initial values for Start/End/Current time
//         * </li>
//         * <p>
//         * @param parent    - the parent Composite
//         * @param groupName - the group name
//         * @param range     - the valid time range (start/end time)
//         * @param current   - the current time
//         */
//        public SpinnerGroup(TimeFrameView owner, Composite parent, String groupName, TmfTimeRange range, TmfTimestamp current) {
//
//        	fOwner = owner;
//        	
//            // Create the group
//            group = new Group(parent, SWT.BORDER);
//            group.setText(groupName);
//
//            // Make it use the whole grid cell
//            GridData gridData = new GridData(SWT.LEFT, SWT.TOP, true, false);
//            gridData.horizontalAlignment = SWT.FILL;
//            group.setLayoutData(gridData);
//
//            // Create and position the widgets
//            seconds = new Spinner(group, SWT.BORDER);
//            seconds.addModifyListener(new ModifyListener() {
//                public void modifyText(ModifyEvent e) {
//                    currentSeconds = seconds.getSelection();
//                    refreshCurrentTime();
//                }
//            });
//            seconds.setBounds(5, 25, 110, 25);
//
//            Label label = new Label(group, SWT.LEFT);
//            label.setText(Labels.TimeFrameView_Seconds);
//            label.setBounds(120, 28, 25, 22);
//
//            nanosec = new Spinner(group, SWT.BORDER);
//            nanosec.addModifyListener(new ModifyListener() {
//                public void modifyText(ModifyEvent e) {
//                    currentNanosec = nanosec.getSelection();
//                    // Correct for nanosec underflow
//                    if (currentNanosec < 0) {
//                        currentSeconds--;
//                        currentNanosec = NANOSECOND_SCALE - 1;
//                    }
//                    // Correct for nanosec overflow
//                    if (currentNanosec >= NANOSECOND_SCALE) {
//                        currentSeconds++;
//                        currentNanosec = 0;
//                    }
//                    refreshCurrentTime();
//                }
//            });
//            nanosec.setBounds(150, 25, 110, 25);
//
//            label = new Label(group, SWT.LEFT);
//            label.setText(Labels.TimeFrameView_Nanosec);
//            label.setBounds(265, 28, 25, 22);
//
//            setContent(range, current);
//        }
//
//        private void refreshCurrentTime() {
//            long newCurrentTime = (long) currentSeconds * NANOSECOND_SCALE + currentNanosec;
//            TmfTimestamp ts = new TmfTimestamp(newCurrentTime, SCALE, 0);
//            currentTime = ts;
//            fOwner.synchTimeFrameWidgets(this);
//        }
//
//        // ====================================================================
//        // Get/Set
//        // ====================================================================
//
//        public TmfTimestamp getStartTime() {
//            return startTime;
//        }
//
//        public TmfTimestamp getEndTime() {
//            return endTime;
//        }
//
//        public TmfTimestamp getCurrentTime() {
//            return currentTime;
//        }
//
//        public TmfTimestamp getSpan() {
//            TmfTimestamp span = new TmfTimestamp(startTime.getAdjustment(endTime), SCALE, 0);
//            return span;
//        }
//
//        public void setStartTime(TmfTimestamp ts) {
//            startTime = ts.synchronize(0, SCALE);
//            startSeconds = (int) (startTime.getValue() / NANOSECOND_SCALE);
//            startNanosec = (int) (startTime.getValue() % NANOSECOND_SCALE);
//        }
//
//        public void setEndTime(TmfTimestamp ts) {
//            endTime = ts.synchronize(0, SCALE);
//            endSeconds = (int) (endTime.getValue() / NANOSECOND_SCALE);
//            endNanosec = (int) (endTime.getValue() % NANOSECOND_SCALE);
//        }
//
//        public void setCurrentTime(TmfTimestamp ts) {
//            currentTime = ts.synchronize(0, SCALE);
//            currentSeconds = (int) (currentTime.getValue() / NANOSECOND_SCALE);
//            currentNanosec = (int) (currentTime.getValue() % NANOSECOND_SCALE);
//        }
//
//        // ====================================================================
//        // Operators
//        // ====================================================================
//
//        /**
//         * <b><u>setContent</u></b>
//         * <p>
//         * <li>validates that [startTime <= currentTime <= endTime] is respected
//         * <li>sets the start/current/end time and update the spinners
//         * </li>
//         * <p>
//         * 
//         * @param range
//         * @param current
//         */
//        public void setContent(TmfTimeRange range, TmfTimestamp current) {
//
//            // Extract the time range
//            TmfTimestamp start = range.getStartTime();
//            TmfTimestamp end = range.getEndTime();
//
//            // Assume start time is OK
//            setStartTime(start);
//
//            // Make sure end time >= start time
//            if (end.compareTo(start, false) < 0) {
//                end = start;
//            }
//            setEndTime(end);
//
//            // Make sure [start time <= current time <= end time]
//            // If not: current = min(max(start, current), end);
//            if (current.compareTo(start, false) < 0) {
//                current = start;
//            }
//            if (current.compareTo(end, false) > 0) {
//                current = end;
//            }
//            setCurrentTime(current);
//
//            // And configure the spinners
//            updateSpinners();
//        }
//
//        /**
//         * <b><u>setValue</u></b>
//         * <p>
//         * <li>validates that [startTime <= currentTime <= endTime] is respected
//         * <li>sets the current time and the spinners
//         * </li>
//         * <p>
//         * 
//         * @param range
//         * @param current
//         */
//        public void setValue(TmfTimestamp current) {
//
//            // Make sure [start time <= current time <= end time]
//            // If not: current = min(max(start, current), end);
//            if (current.compareTo(startTime, false) < 0) {
//                current = startTime;
//            }
//            if (current.compareTo(endTime, false) > 0) {
//                current = endTime;
//            }
//            setCurrentTime(current);
//
//            // And configure the spinners
//            updateSpinners();
//        }
//
//        /**
//         * Update the spinners with the new current time value
//         * Perform the update on the UI thread
//         */
//        public void updateSpinners() {
//
//            seconds.getDisplay().asyncExec(new Runnable() {
//                public void run() {
//                    if (!seconds.isDisposed() && !nanosec.isDisposed()) {
//                        // If we are on the start second, ensure that [currentNS >= startNS]
//                        // If the currentSeconds > startSeconds, set startns to -1 so we can
//                        // "underflow"
//                        int startns = -1;
//                        if (currentSeconds <= startSeconds) {
//                            currentSeconds = startSeconds;
//                            startns = startNanosec;
//                            if (currentNanosec < startns) {
//                                currentNanosec = startns;
//                            }
//                        }
//
//                        // If we are on the end second, ensure that [currentNS <= endNS]
//                        // If the currentSeconds < endSeconds, set endns to MAX so we can
//                        // "overflow"
//                        int endns = NANOSECOND_SCALE;
//                        if (currentSeconds >= endSeconds) {
//                            currentSeconds = endSeconds;
//                            endns = endNanosec;
//                            if (currentNanosec > endns) {
//                                currentNanosec = endns;
//                            }
//                        }
//
//                        // Refresh the spinners (value, range, increments, ...)
//                        // To ensure that the spinners are properly set, the range has to be > 0 
//                        seconds.setValues(currentSeconds, startSeconds - 1, endSeconds + 1, 0, 1, 10);
//                        nanosec.setValues(currentNanosec, startns - 1, endns + 1, 0, 1, 1000000);
//
//                        // If start == end (i.e. no range), disable the spinner
//                        // (if start == end, the spinner widget range is set to [0..100] by default)
//                        seconds.setEnabled(startSeconds != endSeconds);
//                        nanosec.setEnabled(startns != endns);
//                    }
//                }
//            });
//        }
////    };
////
////    // ========================================================================
////    // LTTngTimeFrameView
////    // ========================================================================
////
////    // The event log timestamp characteristics
////    private TmfTimestamp  fLogStartTime = new TmfTimestamp();
////    private TmfTimestamp  fLogEndTime   = new TmfTimestamp();
////
////    @SuppressWarnings("unused")
////    private TmfTimestamp  fCurrentTime  = new TmfTimestamp();
////    
////    private TmfTimeRange fLogTimeRange = new TmfTimeRange(fLogStartTime, fLogEndTime);
////    private TmfTimeRange fLogSpan      = new TmfTimeRange(fLogStartTime, fLogEndTime);
////    private byte fScale = 0;
////
////    private static final int SLIDER_RANGE = 10000;
////
////    private SpinnerGroup fStartGroup;
////    private SpinnerGroup fEndGroup;
////    private SpinnerGroup fRangeGroup;
////    private SpinnerGroup fCurrentGroup;
////
////    // The slider
////    private Slider fSlider;
////
////    // The event log
////    TmfTrace fEventLog;
////
////    /**
////     * Constructor
////     */
////    public TimeFrameView() {
////
////    }
////
////    /* (non-Javadoc)
////     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
////     */
////    public void createPartControl(Composite parent) {
////
////        // Set the view layout
////        GridLayout layout = new GridLayout(4, true);
////        parent.setLayout(layout);
////
////        fStartGroup   = new SpinnerGroup(parent, Labels.TimeFrameView_StartTime,   fLogTimeRange, fLogStartTime);
////        fEndGroup     = new SpinnerGroup(parent, Labels.TimeFrameView_EndTime,     fLogTimeRange, fLogEndTime);
////        fRangeGroup   = new SpinnerGroup(parent, Labels.TimeFrameView_TimeRange,   fLogTimeRange, fLogEndTime);
////        fCurrentGroup = new SpinnerGroup(parent, Labels.TimeFrameView_CurrentTime, fLogTimeRange, fLogStartTime);
////
////        // Create the slider
////        createSlider(parent);
////    }
////
////    /* (non-Javadoc)
////     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
////     */
////    public void setFocus() {
////        // TODO Auto-generated method stub
////    }
////
////    // ========================================================================
////    // Slider Handling
////    // ========================================================================
////
////    /**
////     * @param parent
////     */
////    private void createSlider(Composite parent) {
////        fSlider = new Slider(parent, SWT.FILL);
////        fSlider.setMaximum(SLIDER_RANGE + fSlider.getThumb());
////
////        GridData gridData = new GridData(SWT.LEFT, SWT.TOP, true, false);
////        gridData.horizontalAlignment = SWT.FILL;
////        gridData.horizontalSpan = 4;
////        fSlider.setLayoutData(gridData);
////
////        fSlider.addSelectionListener(this);
////    }
////
////    /**
////     * @param range
////     * @param current
////     */
////    private void updateSlider(TmfTimeRange range, TmfTimestamp current) {
////
////        // Determine the new relative position
////        long total    = range.getStartTime().getAdjustment(range.getEndTime());
////        long relative = range.getStartTime().getAdjustment(current);
////
////        // Set the slider value
////        long position = (total > 0) ? (relative * SLIDER_RANGE / total) : 0;
////        fSlider.setSelection((int) position);
////    }
////
////    /* (non-Javadoc)
////     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
////     */
////    public void widgetDefaultSelected(SelectionEvent e) {
////        // TODO Auto-generated method stub
////    }
////
////    /* (non-Javadoc)
////     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
////     */
////    public void widgetSelected(SelectionEvent e) {
////
////        // Get the relative position
////        int ratio = fSlider.getSelection();
////
////        TmfTimestamp span = fCurrentGroup.getSpan();
////        long value = span.getValue() * ratio / SLIDER_RANGE;
////
////        TmfTimestamp start = fCurrentGroup.getStartTime();
////        TmfTimestamp current = new TmfTimestamp(start.getValue() + value, start.getScale(), 0);
////
////        fCurrentGroup.setValue(current);
////    }
////
////    // ========================================================================
////    // TMF Signal Handling
////    // ========================================================================
////
////    /**
////     * @param signal
////     */
////    @TmfSignalHandler
////    public void traceSelected(TmfTraceSelectedSignal signal) {
////
////        // Update the trace reference
////        fEventLog = signal.getTrace();
////
////        // Update the time frame
////        fLogTimeRange = fEventLog.getTimeRange();
////        fLogStartTime = fLogTimeRange.getStartTime();
////        fLogEndTime   = fLogTimeRange.getEndTime();
////        fScale        = fLogStartTime.getScale();
////
////        // Update the widgets
////        fStartGroup.setContent(fLogTimeRange, fLogStartTime);
////        fEndGroup.setContent(fLogTimeRange, fLogEndTime);
////        fCurrentGroup.setContent(fLogTimeRange, fLogStartTime);
////
////        fCurrentTime = fLogStartTime;
////
////        TmfTimestamp delta = new TmfTimestamp(fLogStartTime.getAdjustment(fLogEndTime), fScale, 0);
////        fLogSpan = new TmfTimeRange(new TmfTimestamp(0, fScale, 0), delta);
////        fRangeGroup.setContent(fLogSpan, delta);
////    }
////
////    /**
////     * @param signal
////     */
////    @TmfSignalHandler
////    public void traceUpdated(TmfTraceUpdatedSignal signal) {
////
////        // Update the time frame
////    	fLogTimeRange = signal.getTrace().getTimeRange();
////        fLogStartTime = fLogTimeRange.getStartTime();
////        fLogEndTime   = fLogTimeRange.getEndTime();
////        fScale        = fLogStartTime.getScale();
////
////        // Update the widgets
////        fStartGroup.setContent(fLogTimeRange, fStartGroup.getCurrentTime());
////        fEndGroup.setContent(fLogTimeRange, fLogEndTime); // fEndGroup.getCurrentTime());
////        fCurrentGroup.setContent(fLogTimeRange, fCurrentGroup.getCurrentTime());
////
////        TmfTimestamp delta = new TmfTimestamp(fLogStartTime.getAdjustment(fLogEndTime), fScale, 0);
////        fLogSpan = new TmfTimeRange(new TmfTimestamp(0, fScale, 0), delta);
////        fRangeGroup.setContent(fLogSpan, delta);
////    }
////
//////    /**
//////     * @param signal
//////     */
//////    @TmfSignalHandler
//////    public void currentTimeUpdated(TmfCurrentTimeSignal signal) {
//////
//////        // Update the time frame
//////    	fLogTimeRange = signal.getTrace().getTimeRange();
//////        fLogStartTime = fLogTimeRange.getStartTime();
//////        fLogEndTime   = fLogTimeRange.getEndTime();
//////        fScale        = fLogStartTime.getScale();
//////
//////        // Update the widgets
//////        fStartGroup.setContent(fLogTimeRange, fStartGroup.getCurrentTime());
//////        fEndGroup.setContent(fLogTimeRange, fLogEndTime); // fEndGroup.getCurrentTime());
//////        fCurrentGroup.setContent(fLogTimeRange, fCurrentGroup.getCurrentTime());
//////
//////        TmfTimestamp delta = new TmfTimestamp(fLogStartTime.getAdjustment(fLogEndTime), fScale, 0);
//////        fLogSpan = new TmfTimeRange(new TmfTimestamp(0, fScale, 0), delta);
//////        fRangeGroup.setContent(fLogSpan, delta);
//////    }
////
//////  /* (non-Javadoc)
//////  * @see org.eclipse.linuxtools.tmf.ui.viewer.TmfViewer#handleEvent(org.eclipse.linuxtools.tmf.ui.viewer.TmfViewerEvent)
//////  */
////// public void handleEvent(ITmfEventLogEvent event) {
//////
//////     // A new trace has been selected
//////     if (event instanceof TmfTraceSelectedEvent) {
//////         TmfTraceSelectedEvent e = (TmfTraceSelectedEvent) event;
//////         traceSelected(e.getEventLog());
//////         return;
//////     }
//////
//////     // The trace time range has been updated
//////     if (event instanceof TmfLogUpdateEvent) {
//////         TmfLogUpdateEvent e = (TmfLogUpdateEvent) event;
//////         traceUpdated(e.getEventLog());
//////         return;
//////     }
//////
////////     if (event instanceof TmfCurrentTimeEvent) {
////////         TmfCurrentTimeEvent e = (TmfCurrentTimeEvent) event;
////////         fCurrentGroup.setContent(fLogTimeRange, e.getCurrentTime());
////////     }
////// }
////
////    /**
////     * One of the spinners has been updated. Synchronize the other widgets.
////     */
////    private void synchTimeFrameWidgets(SpinnerGroup trigger) {
////
////        // Collect the data
////        TmfTimestamp startTime   = fStartGroup.getCurrentTime();
////        TmfTimestamp endTime     = fEndGroup.getCurrentTime();
////        TmfTimestamp timeRange   = fRangeGroup.getCurrentTime();
////        TmfTimestamp currentTime = fCurrentGroup.getCurrentTime();
////
////        // If startTime was set beyond endTime, adjust endTime and interval
////        if (trigger == fStartGroup) {
////            if (startTime.compareTo(endTime, false) > 0) {
////                endTime = startTime;
////            }
////        }
////
////        // If endTime was set beyond startTime, adjust startTime and interval
////        if (trigger == fEndGroup) {
////            if (endTime.compareTo(startTime, false) < 0) {
////                startTime = endTime;
////            }
////        }
////
////        // If timeRange was set, adjust endTime
////        if (trigger == fRangeGroup) {
////            long start = startTime.getValue();
////            long span  = timeRange.getValue();
////            TmfTimestamp ts = new TmfTimestamp(start + span, startTime.getScale(), 0);
////            if (ts.compareTo(fLogEndTime, false) > 0) {
////                ts = fLogEndTime.synchronize(fLogEndTime.getValue(), startTime.getScale());
////            }
////            endTime = ts;
////        }
////
////        // Compute the new time range
////        TmfTimeRange subrange = new TmfTimeRange(startTime, endTime);
////        TmfTimestamp  interval = new TmfTimestamp(startTime.getAdjustment(endTime), startTime.getScale(), 0);
////
////        // Update the spinner groups
////        fStartGroup.setContent(fLogTimeRange, startTime);
////        fEndGroup.setContent(fLogTimeRange, endTime);
////        fRangeGroup.setContent(fLogSpan, interval);
////        fCurrentGroup.setContent(subrange, currentTime);
////
////        updateSlider(subrange, currentTime);
////
//////        // Notify other views
//////        if (!fCurrentTime.equals(currentTime)) {
//////            fCurrentTime = currentTime;
//////            dispatchEvent(new TmfCurrentTimeEvent(currentTime));
//////        }
////    }
//
//}
