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

package org.eclipse.linuxtools.internal.lttng.ui.views.timeframe;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
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
    private static final int  NS_PER_SECOND = 1000 * 1000 * 1000;
    private static final byte NS_SCALING_FACTOR = -9;

    // Labels
    private static final String SECONDS_LABEL = "sec"; //$NON-NLS-1$
    private static final String NANOSEC_LABEL = "ns"; //$NON-NLS-1$

    // Widgets
    private Group group;
    private Spinner seconds;
    private Spinner nanosec;

    // The valid time range - start time
    private ITmfTimestamp startTime;
    private int startSeconds;
    private int startNanosec;

    // The valid time range - end time
    private ITmfTimestamp endTime;
    private int endSeconds;
    private int endNanosec;

    // The current time value
    private ITmfTimestamp currentTime;
    private int currentSeconds;
    private int currentNanosec;
    
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
    public SpinnerGroup(TimeFrameView owner, Composite parent, String groupName, TmfTimeRange range, ITmfTimestamp current) {

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
            @Override
			public void modifyText(ModifyEvent e) {
                currentSeconds = seconds.getSelection();
                refreshCurrentTime();
            }
        });
        seconds.setBounds(5, 25, 110, 25);

        Label label = new Label(group, SWT.LEFT);
        label.setText(SECONDS_LABEL);
        label.setBounds(120, 28, 25, 22);

        nanosec = new Spinner(group, SWT.BORDER);
        nanosec.addModifyListener(new ModifyListener() {
            @Override
			public void modifyText(ModifyEvent e) {
                currentNanosec = nanosec.getSelection();
                // Correct for nanosec underflow
                if (currentNanosec < 0) {
                    currentSeconds--;
                    currentNanosec = NS_PER_SECOND - 1;
                }
                // Correct for nanosec overflow
                if (currentNanosec >= NS_PER_SECOND) {
                    currentSeconds++;
                    currentNanosec = 0;
                }
                refreshCurrentTime();
            }
        });
        nanosec.setBounds(150, 25, 110, 25);

        label = new Label(group, SWT.LEFT);
        label.setText(NANOSEC_LABEL);
        label.setBounds(265, 28, 25, 22);

        setContent(range, current);
    }

    private void refreshCurrentTime() {
        long newCurrentTime = ((long) currentSeconds) * NS_PER_SECOND + currentNanosec;
        TmfTimestamp ts = new TmfTimestamp(newCurrentTime, NS_SCALING_FACTOR, 0);
        currentTime = ts;
//        fOwner.synchTimeFrameWidgets(this);
    }

    // ====================================================================
    // Get/Set
    // ====================================================================

    public ITmfTimestamp getStartTime() {
        return startTime;
    }

    public ITmfTimestamp getEndTime() {
        return endTime;
    }

    public ITmfTimestamp getCurrentTime() {
        return currentTime;
    }

    public TmfTimestamp getSpan() {
        TmfTimestamp span = (TmfTimestamp) startTime.getDelta(endTime);
        return span;
    }

    public TmfTimeRange getTimeRange() {
        TmfTimeRange range = new TmfTimeRange(startTime, endTime);
        return range;
    }

    public void setStartTime(ITmfTimestamp ts) {
    	try {
    		startTime = (TmfTimestamp) ts.getDelta(new TmfTimestamp(0, NS_SCALING_FACTOR));
    		startSeconds = (int) (startTime.getValue() / NS_PER_SECOND);
    		startNanosec = (int) (startTime.getValue() % NS_PER_SECOND);
    	}
    	catch (ArithmeticException e) {
    	}
    }

    public void setEndTime(ITmfTimestamp ts) {
    	try {
    	    endTime = (TmfTimestamp) ts.getDelta(new TmfTimestamp(0, NS_SCALING_FACTOR));
    		endSeconds = (int) (endTime.getValue() / NS_PER_SECOND);
    		endNanosec = (int) (endTime.getValue() % NS_PER_SECOND);
    	}
    	catch (ArithmeticException e) {
    	}
    }

    public void setCurrentTime(ITmfTimestamp ts) {
    	try {
    	    currentTime = (TmfTimestamp) ts.getDelta(new TmfTimestamp(0, NS_SCALING_FACTOR));
    		currentSeconds = (int) (currentTime.getValue() / NS_PER_SECOND);
    		currentNanosec = (int) (currentTime.getValue() % NS_PER_SECOND);
    	}
    	catch (ArithmeticException e) {
    	}
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
    public void setContent(TmfTimeRange range, ITmfTimestamp current) {

    	if (range != null) {
        	// Extract the time range
            ITmfTimestamp start = range.getStartTime();
            ITmfTimestamp end   = range.getEndTime();

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
    public void setValue(ITmfTimestamp current) {

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
    	// Ignore update if disposed
    	if (seconds.isDisposed()) return;
    	
        seconds.getDisplay().asyncExec(new Runnable() {
			@Override
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
		            int endns = NS_PER_SECOND;
		            if (currentSeconds >= endSeconds) {
		                currentSeconds = endSeconds;
		                endns = endNanosec;
		                if (currentNanosec > endns) {
		                    currentNanosec = endns;
		                }
		            }

		            // Refresh the spinners (value, range, increments, ...)
					// To ensure that the spinners are properly set, the range has to be > 0 
//					seconds.setValues(currentSeconds, startSeconds - 1, endSeconds + 1, 0, 1, 10);
//					nanosec.setValues(currentNanosec, startns - 1, endns + 1, 0, 1, 1000000);
					seconds.setValues(currentSeconds, startSeconds, endSeconds, 0, 1, 10);
					nanosec.setValues(currentNanosec, startns, endns, 0, 100000, 10000000);

		            // If start == end (i.e. no range), disable the spinner
		            // (if start == end, the spinner widget range is set to [0..100] by default)
		            seconds.setEnabled(startSeconds != endSeconds);
		            nanosec.setEnabled(startns != endns);
				}
			}
        });
    }
}