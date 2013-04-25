/*****************************************************************************
 * Copyright (c) 2007, 2013 Intel Corporation, Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Intel Corporation - Initial API and implementation
 *   Ruslan A. Scherbakov, Intel - Initial API and implementation
 *   Alexander N. Alexeev, Intel - Add monitors statistics support
 *   Alvaro Sanchez-Leon - Adapted for TMF
 *   Patrick Tasse - Refactoring
 *****************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.dialogs.TimeGraphLegend;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.ITimeDataProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphColorScheme;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphScale;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphTooltipHandler;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Slider;

/**
 * Generic time graph viewer implementation
 *
 * @version 1.0
 * @author Patrick Tasse, and others
 */
public class TimeGraphViewer implements ITimeDataProvider, SelectionListener {

    /** vars */
    private long _minTimeInterval;
    private long _selectedTime;
    private ITimeGraphEntry _selectedEntry;
    private long _beginTime;
    private long _endTime;
    private long _time0;
    private long _time1;
    private long _time0_;
    private long _time1_;
    private long _time0_extSynch = 0;
    private long _time1_extSynch = 0;
    private boolean _timeRangeFixed;
    private int _nameWidthPref = 200;
    private int _minNameWidth = 6;
    private int _nameWidth;
    private Composite _dataViewer;

    private TimeGraphControl _stateCtrl;
    private TimeGraphScale _timeScaleCtrl;
    private Slider _verticalScrollBar;
    private TimeGraphTooltipHandler _threadTip;
    private TimeGraphColorScheme _colors;
    private ITimeGraphPresentationProvider fTimeGraphProvider;

    ArrayList<ITimeGraphSelectionListener> fSelectionListeners = new ArrayList<ITimeGraphSelectionListener>();
    ArrayList<ITimeGraphTimeListener> fTimeListeners = new ArrayList<ITimeGraphTimeListener>();
    ArrayList<ITimeGraphRangeListener> fRangeListeners = new ArrayList<ITimeGraphRangeListener>();

    // Time format, using Epoch reference, Relative time format(default) or Number
    private TimeFormat timeFormat = TimeFormat.RELATIVE;
    private int borderWidth = 0;
    private int timeScaleHeight = 22;

    private Action resetScale;
    private Action showLegendAction;
    private Action nextEventAction;
    private Action prevEventAction;
    private Action nextItemAction;
    private Action previousItemAction;
    private Action zoomInAction;
    private Action zoomOutAction;

    /**
     * Standard constructor
     *
     * @param parent
     *            The parent UI composite object
     * @param style
     *            The style to use
     */
    public TimeGraphViewer(Composite parent, int style) {
        createDataViewer(parent, style);
    }

    /**
     * Sets the timegraph provider used by this timegraph viewer.
     *
     * @param timeGraphProvider the timegraph provider
     */
    public void setTimeGraphProvider(ITimeGraphPresentationProvider timeGraphProvider) {
        fTimeGraphProvider = timeGraphProvider;
        _stateCtrl.setTimeGraphProvider(timeGraphProvider);
        _threadTip = new TimeGraphTooltipHandler(_dataViewer.getShell(), fTimeGraphProvider, this);
        _threadTip.activateHoverHelp(_stateCtrl);
    }

    /**
     * Sets or clears the input for this time graph viewer.
     * The input array should only contain top-level elements.
     *
     * @param input The input of this time graph viewer, or <code>null</code> if none
     */
    public void setInput(ITimeGraphEntry[] input) {
        ITimeGraphEntry[] realInput = input;

        if (_stateCtrl != null) {
            if (realInput == null) {
                realInput = new ITimeGraphEntry[0];
            }
            setTimeRange(realInput);
            _verticalScrollBar.setEnabled(true);
            setTopIndex(0);
            _selectedTime = 0;
            _selectedEntry = null;
            refreshAllData(realInput);
        }
    }

    /**
     * Refresh the view
     */
    public void refresh() {
        setTimeRange(_stateCtrl.getTraces());
        _verticalScrollBar.setEnabled(true);
        refreshAllData(_stateCtrl.getTraces());
    }

    /**
     * Callback for when the control is moved
     *
     * @param e
     *            The caller event
     */
    public void controlMoved(ControlEvent e) {
    }

    /**
     * Callback for when the control is resized
     *
     * @param e
     *            The caller event
     */
    public void controlResized(ControlEvent e) {
        resizeControls();
    }

    /**
     * Handler for when the model is updated. Called from the display order in
     * the API
     *
     * @param traces
     *            The traces in the model
     * @param start
     *            The start time
     * @param end
     *            The end time
     * @param updateTimeBounds
     *            Should we updated the time bounds too
     */
    public void modelUpdate(ITimeGraphEntry[] traces, long start,
            long end, boolean updateTimeBounds) {
        if (null != _stateCtrl) {
            //loadOptions();
            updateInternalData(traces, start, end);
            if (updateTimeBounds) {
                _timeRangeFixed = true;
                // set window to match limits
                setStartFinishTime(_time0_, _time1_);
            } else {
                _stateCtrl.redraw();
                _timeScaleCtrl.redraw();
            }
        }
    }

    /**
     * @return The string representing the view type
     */
    protected String getViewTypeStr() {
        return "viewoption.threads"; //$NON-NLS-1$
    }

    int getMarginWidth() {
        return 0;
    }

    int getMarginHeight() {
        return 0;
    }

    void loadOptions() {
        _minTimeInterval = 1;
        _selectedTime = -1;
        _nameWidth = Utils.loadIntOption(getPreferenceString("namewidth"), //$NON-NLS-1$
                _nameWidthPref, _minNameWidth, 1000);
    }

    void saveOptions() {
        Utils.saveIntOption(getPreferenceString("namewidth"), _nameWidth); //$NON-NLS-1$
    }

    /**
     * Create a data viewer.
     *
     * @param parent
     *            Parent composite
     * @param style
     *            Style to use
     * @return The new data viewer
     */
    protected Control createDataViewer(Composite parent, int style) {
        loadOptions();
        _colors = new TimeGraphColorScheme();
        _dataViewer = new Composite(parent, style) {
            @Override
            public void redraw() {
                _timeScaleCtrl.redraw();
                _stateCtrl.redraw();
                super.redraw();
            }
        };
        GridLayout gl = new GridLayout(2, false);
        gl.marginHeight = borderWidth;
        gl.marginWidth = 0;
        gl.verticalSpacing = 0;
        gl.horizontalSpacing = 0;
        _dataViewer.setLayout(gl);

        _timeScaleCtrl = new TimeGraphScale(_dataViewer, _colors);
        _timeScaleCtrl.setTimeProvider(this);
        _timeScaleCtrl.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        _timeScaleCtrl.setHeight(timeScaleHeight);

        _verticalScrollBar = new Slider(_dataViewer, SWT.VERTICAL | SWT.NO_FOCUS);
        _verticalScrollBar.setLayoutData(new GridData(SWT.DEFAULT, SWT.FILL, false, true, 1, 2));
        _verticalScrollBar.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setTopIndex(_verticalScrollBar.getSelection());
            }
        });
        _verticalScrollBar.setEnabled(false);

        _stateCtrl = createTimeGraphControl(_dataViewer, _colors);

        _stateCtrl.setTimeProvider(this);
        _stateCtrl.addSelectionListener(this);
        _stateCtrl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
        _stateCtrl.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseScrolled(MouseEvent e) {
                adjustVerticalScrollBar();
            }
        });
        _stateCtrl.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                adjustVerticalScrollBar();
            }
        });

        Composite filler = new Composite(_dataViewer, SWT.NONE);
        GridData gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
        gd.heightHint = _stateCtrl.getHorizontalBar().getSize().y;
        filler.setLayoutData(gd);
        filler.setLayout(new FillLayout());

        _stateCtrl.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent event) {
                resizeControls();
            }
        });
        resizeControls();
        _dataViewer.update();
        adjustVerticalScrollBar();
        return _dataViewer;
    }

    /**
     * Dispose the view.
     */
    public void dispose() {
        saveOptions();
        _stateCtrl.dispose();
        _dataViewer.dispose();
        _colors.dispose();
    }

    /**
     * Create a new time graph control.
     *
     * @param parent
     *            The parent composite
     * @param colors
     *            The color scheme
     * @return The new TimeGraphControl
     * @since 2.0
     */
    protected TimeGraphControl createTimeGraphControl(Composite parent,
            TimeGraphColorScheme colors) {
        return new TimeGraphControl(parent, colors);
    }

    /**
     * Resize the controls
     */
    public void resizeControls() {
        Rectangle r = _dataViewer.getClientArea();
        if (r.isEmpty()) {
            return;
        }

        int width = r.width;
        if (_nameWidth > width - _minNameWidth) {
            _nameWidth = width - _minNameWidth;
        }
        if (_nameWidth < _minNameWidth) {
            _nameWidth = _minNameWidth;
        }
        adjustVerticalScrollBar();
    }

    /**
     * Try to set most convenient time range for display.
     *
     * @param traces
     *            The traces in the model
     */
    public void setTimeRange(ITimeGraphEntry traces[]) {
        _endTime = 0;
        _beginTime = -1;
        for (int i = 0; i < traces.length; i++) {
            ITimeGraphEntry entry = traces[i];
            if (entry.getEndTime() >= entry.getStartTime() && entry.getEndTime() > 0) {
                if (_beginTime < 0 || entry.getStartTime() < _beginTime) {
                    _beginTime = entry.getStartTime();
                }
                if (entry.getEndTime() > _endTime) {
                    _endTime = entry.getEndTime();
                }
            }
        }

        if (_beginTime < 0) {
            _beginTime = 0;
        }
    }

    /**
     * Recalculate the time bounds
     */
    public void setTimeBounds() {
        //_time0_ = _beginTime - (long) ((_endTime - _beginTime) * 0.02);
        _time0_ = _beginTime;
        if (_time0_ < 0) {
            _time0_ = 0;
        }
        // _time1_ = _time0_ + (_endTime - _time0_) * 1.05;
        _time1_ = _endTime;
        // _time0_ = Math.floor(_time0_);
        // _time1_ = Math.ceil(_time1_);
        if (!_timeRangeFixed) {
            _time0 = _time0_;
            _time1 = _time1_;
        }
        if (_time1 - _time0 < _minTimeInterval) {
            _time1 = Math.min(_time1_, _time0 + _minTimeInterval);
        }
    }

    /**
     * @param traces
     * @param start
     * @param end
     */
    void updateInternalData(ITimeGraphEntry[] traces, long start, long end) {
        ITimeGraphEntry[] realTraces = traces;

        if (null == realTraces) {
            realTraces = new ITimeGraphEntry[0];
        }
        if ((start == 0 && end == 0) || start < 0 || end < 0) {
            // Start and end time are unspecified and need to be determined from
            // individual processes
            setTimeRange(realTraces);
        } else {
            _beginTime = start;
            _endTime = end;
        }

        refreshAllData(realTraces);
    }

    /**
     * @param traces
     */
    private void refreshAllData(ITimeGraphEntry[] traces) {
        setTimeBounds();
        if (_selectedTime < _beginTime) {
            _selectedTime = _beginTime;
        } else if (_selectedTime > _endTime) {
            _selectedTime = _endTime;
        }
        _stateCtrl.refreshData(traces);
        _timeScaleCtrl.redraw();
        adjustVerticalScrollBar();
    }

    /**
     * Callback for when this view is focused
     */
    public void setFocus() {
        if (null != _stateCtrl) {
            _stateCtrl.setFocus();
        }
    }

    /**
     * Get the current focus status of this view.
     *
     * @return If the view is currently focused, or not
     */
    public boolean isInFocus() {
        return _stateCtrl.isInFocus();
    }

    /**
     * Get the view's current selection
     *
     * @return The entry that is selected
     */
    public ITimeGraphEntry getSelection() {
        return _stateCtrl.getSelectedTrace();
    }

    /**
     * Get the index of the current selection
     *
     * @return The index
     */
    public int getSelectionIndex() {
        return _stateCtrl.getSelectedIndex();
    }

    @Override
    public long getTime0() {
        return _time0;
    }

    @Override
    public long getTime1() {
        return _time1;
    }

    @Override
    public long getMinTimeInterval() {
        return _minTimeInterval;
    }

    @Override
    public int getNameSpace() {
        return _nameWidth;
    }

    @Override
    public void setNameSpace(int width) {
        _nameWidth = width;
        int w = _stateCtrl.getClientArea().width;
        if (_nameWidth > w - 6) {
            _nameWidth = w - 6;
        }
        if (_nameWidth < 6) {
            _nameWidth = 6;
        }
        _stateCtrl.adjustScrolls();
        _stateCtrl.redraw();
        _timeScaleCtrl.redraw();
    }

    @Override
    public int getTimeSpace() {
        int w = _stateCtrl.getClientArea().width;
        return w - _nameWidth;
    }

    @Override
    public long getSelectedTime() {
        return _selectedTime;
    }

    @Override
    public long getBeginTime() {
        return _beginTime;
    }

    @Override
    public long getEndTime() {
        return _endTime;
    }

    @Override
    public long getMaxTime() {
        return _time1_;
    }

    @Override
    public long getMinTime() {
        return _time0_;
    }

    @Override
    public void setStartFinishTimeNotify(long time0, long time1) {
        setStartFinishTime(time0, time1);
        notifyRangeListeners(time0, time1);
    }

    @Override
    public void notifyStartFinishTime() {
        notifyRangeListeners(_time0, _time1);
    }

    @Override
    public void setStartFinishTime(long time0, long time1) {
        _time0 = time0;
        if (_time0 < _time0_) {
            _time0 = _time0_;
        }
        if (_time0 > _time1_) {
            _time0 = _time1_;
        }
        _time1 = time1;
        if (_time1 < _time0_) {
            _time1 = _time0_;
        }
        if (_time1 > _time1_) {
            _time1 = _time1_;
        }
        if (_time1 - _time0 < _minTimeInterval) {
            _time1 = Math.min(_time1_, _time0 + _minTimeInterval);
        }
        _timeRangeFixed = true;
        _stateCtrl.adjustScrolls();
        _stateCtrl.redraw();
        _timeScaleCtrl.redraw();
    }

    /**
     * Set the time bounds to the provided values
     *
     * @param beginTime
     *            The start time of the window
     * @param endTime
     *            The end time
     */
    public void setTimeBounds(long beginTime, long endTime) {
        _beginTime = beginTime;
        _endTime = endTime;
        _time0_ = beginTime;
        _time1_ = endTime;
        _stateCtrl.adjustScrolls();
    }

    @Override
    public void resetStartFinishTime() {
        setStartFinishTime(_time0_, _time1_);
        _timeRangeFixed = false;
    }

    @Override
    public void setSelectedTimeNotify(long time, boolean ensureVisible) {
        setSelectedTimeInt(time, ensureVisible, true);
    }

    @Override
    public void setSelectedTime(long time, boolean ensureVisible) {
        setSelectedTimeInt(time, ensureVisible, false);
    }

    private void setSelectedTimeInt(long time, boolean ensureVisible, boolean doNotify) {
        long time0 = _time0;
        long time1 = _time1;
        if (ensureVisible) {
            long timeSpace = (long) ((_time1 - _time0) * .02);
            long timeMid = (long) ((_time1 - _time0) * .5);
            if (time < _time0 + timeSpace) {
                long dt = _time0 - time + timeMid;
                _time0 -= dt;
                _time1 -= dt;
            } else if (time > _time1 - timeSpace) {
                long dt = time - _time1 + timeMid;
                _time0 += dt;
                _time1 += dt;
            }
            if (_time0 < _time0_) {
                _time1 = Math.min(_time1_, _time1 + (_time0_ - _time0));
                _time0 = _time0_;
            } else if (_time1 > _time1_) {
                _time0 = Math.max(_time0_, _time0 - (_time1 - _time1_));
                _time1 = _time1_;
            }
        }
        if (_time1 - _time0 < _minTimeInterval) {
            _time1 = Math.min(_time1_, _time0 + _minTimeInterval);
        }
        _stateCtrl.adjustScrolls();
        _stateCtrl.redraw();
        _timeScaleCtrl.redraw();


        boolean notifySelectedTime = (time != _selectedTime);
        _selectedTime = time;

        if (doNotify && ((time0 != _time0) || (time1 != _time1))) {
            notifyRangeListeners(_time0, _time1);
        }

        if (doNotify && notifySelectedTime) {
            notifyTimeListeners(_selectedTime);
        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        if (_selectedEntry != getSelection()) {
            _selectedEntry = getSelection();
            notifySelectionListeners(_selectedEntry);
        }
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (_selectedEntry != getSelection()) {
            _selectedEntry = getSelection();
            notifySelectionListeners(_selectedEntry);
        }
    }

    /**
     * Callback for when the next event is selected
     */
    public void selectNextEvent() {
        _stateCtrl.selectNextEvent();
        adjustVerticalScrollBar();
    }

    /**
     * Callback for when the previous event is selected
     */
    public void selectPrevEvent() {
        _stateCtrl.selectPrevEvent();
        adjustVerticalScrollBar();
    }

    /**
     * Callback for when the next item is selected
     */
    public void selectNextItem() {
        _stateCtrl.selectNextTrace();
        adjustVerticalScrollBar();
    }

    /**
     * Callback for when the previous item is selected
     */
    public void selectPrevItem() {
        _stateCtrl.selectPrevTrace();
        adjustVerticalScrollBar();
    }

    /**
     * Callback for the show legend action
     */
    public void showLegend() {
        if (_dataViewer == null || _dataViewer.isDisposed()) {
            return;
        }

        TimeGraphLegend.open(_dataViewer.getShell(), fTimeGraphProvider);
    }

    /**
     * Callback for the Zoom In action
     */
    public void zoomIn() {
        _stateCtrl.zoomIn();
    }

    /**
     * Callback for the Zoom Out action
     */
    public void zoomOut() {
        _stateCtrl.zoomOut();
    }

    private String getPreferenceString(String string) {
        return getViewTypeStr() + "." + string; //$NON-NLS-1$
    }

    /**
     * Add a selection listener
     *
     * @param listener
     *            The listener to add
     */
    public void addSelectionListener(ITimeGraphSelectionListener listener) {
        fSelectionListeners.add(listener);
    }

    /**
     * Remove a selection listener
     *
     * @param listener
     *            The listener to remove
     */
    public void removeSelectionListener(ITimeGraphSelectionListener listener) {
        fSelectionListeners.remove(listener);
    }

    private void notifySelectionListeners(ITimeGraphEntry selection) {
        TimeGraphSelectionEvent event = new TimeGraphSelectionEvent(this, selection);

        for (ITimeGraphSelectionListener listener : fSelectionListeners) {
            listener.selectionChanged(event);
        }
    }

    /**
     * Add a time listener
     *
     * @param listener
     *            The listener to add
     */
    public void addTimeListener(ITimeGraphTimeListener listener) {
        fTimeListeners.add(listener);
    }

    /**
     * Remove a time listener
     *
     * @param listener
     *            The listener to remove
     */
    public void removeTimeListener(ITimeGraphTimeListener listener) {
        fTimeListeners.remove(listener);
    }

    private void notifyTimeListeners(long time) {
        TimeGraphTimeEvent event = new TimeGraphTimeEvent(this, time);

        for (ITimeGraphTimeListener listener : fTimeListeners) {
            listener.timeSelected(event);
        }
    }

    /**
     * Add a range listener
     *
     * @param listener
     *            The listener to add
     */
    public void addRangeListener(ITimeGraphRangeListener listener) {
        fRangeListeners.add(listener);
    }

    /**
     * Remove a range listener
     *
     * @param listener
     *            The listener to remove
     */
    public void removeRangeListener(ITimeGraphRangeListener listener) {
        fRangeListeners.remove(listener);
    }

    private void notifyRangeListeners(long startTime, long endTime) {
        // Check if the time has actually changed from last notification
        if (startTime != _time0_extSynch || endTime != _time1_extSynch) {
            // Notify Time Scale Selection Listeners
            TimeGraphRangeUpdateEvent event = new TimeGraphRangeUpdateEvent(this, startTime, endTime);

            for (ITimeGraphRangeListener listener : fRangeListeners) {
                listener.timeRangeUpdated(event);
            }

            // update external synch timers
            updateExtSynchTimers();
        }
    }

    /**
     * Callback to set a selected event in the view
     *
     * @param event
     *            The event that was selected
     * @param source
     *            The source of this selection event
     */
    public void setSelectedEvent(ITimeEvent event, Object source) {
        if (event == null || source == this) {
            return;
        }
        _selectedEntry = event.getEntry();
        _stateCtrl.selectItem(_selectedEntry, false);

        setSelectedTimeInt(event.getTime(), true, true);
        adjustVerticalScrollBar();
    }

    /**
     * Set the seeked time of a trace
     *
     * @param trace
     *            The trace that was seeked
     * @param time
     *            The target time
     * @param source
     *            The source of this seek event
     */
    public void setSelectedTraceTime(ITimeGraphEntry trace, long time, Object source) {
        if (trace == null || source == this) {
            return;
        }
        _selectedEntry = trace;
        _stateCtrl.selectItem(trace, false);

        setSelectedTimeInt(time, true, true);
    }

    /**
     * Callback for a trace selection
     *
     * @param trace
     *            The trace that was selected
     */
    public void setSelection(ITimeGraphEntry trace) {
        _selectedEntry = trace;
        _stateCtrl.selectItem(trace, false);
        adjustVerticalScrollBar();
    }

    /**
     * Callback for a time window selection
     *
     * @param time0
     *            Start time of the range
     * @param time1
     *            End time of the range
     * @param source
     *            Source of the event
     */
    public void setSelectVisTimeWindow(long time0, long time1, Object source) {
        if (source == this) {
            return;
        }

        setStartFinishTime(time0, time1);

        // update notification time values since we are now in synch with the
        // external application
        updateExtSynchTimers();
    }

    /**
     * update the cache timers used to identify the need to send a time window
     * update to external registered listeners
     */
    private void updateExtSynchTimers() {
        // last time notification cache
        _time0_extSynch = _time0;
        _time1_extSynch = _time1;
    }

    /**
     * @since 2.0
     */
    @Override
    public TimeFormat getTimeFormat() {
        return timeFormat;
    }

    /**
     * @param tf the {@link TimeFormat} used to display timestamps
     * @since 2.0
     */
    public void setTimeFormat(TimeFormat tf) {
        this.timeFormat = tf;
    }

    /**
     * Retrieve the border width
     *
     * @return The width
     */
    public int getBorderWidth() {
        return borderWidth;
    }

    /**
     * Set the border width
     *
     * @param borderWidth
     *            The width
     */
    public void setBorderWidth(int borderWidth) {
        if (borderWidth > -1) {
            this.borderWidth = borderWidth;
            GridLayout gl = (GridLayout)_dataViewer.getLayout();
            gl.marginHeight = borderWidth;
        }
    }

    /**
     * Retrieve the height of the header
     *
     * @return The height
     */
    public int getHeaderHeight() {
        return timeScaleHeight;
    }

    /**
     * Set the height of the header
     *
     * @param headerHeight
     *            The height to set
     */
    public void setHeaderHeight(int headerHeight) {
        if (headerHeight > -1) {
            this.timeScaleHeight = headerHeight;
            _timeScaleCtrl.setHeight(headerHeight);
        }
    }

    /**
     * Retrieve the height of an item row
     *
     * @return The height
     */
    public int getItemHeight() {
        if (_stateCtrl != null) {
            return _stateCtrl.getItemHeight();
        }
        return 0;
    }

    /**
     * Set the height of an item row
     *
     * @param rowHeight
     *            The height to set
     */
    public void setItemHeight(int rowHeight) {
        if (_stateCtrl != null) {
            _stateCtrl.setItemHeight(rowHeight);
        }
    }

    /**
     * Set the minimum item width
     *
     * @param width
     *            The min width
     */
    public void setMinimumItemWidth(int width) {
        if (_stateCtrl != null) {
            _stateCtrl.setMinimumItemWidth(width);
        }
    }

    /**
     * Set the width for the name column
     *
     * @param width The width
     */
    public void setNameWidthPref(int width) {
        _nameWidthPref = width;
        if (width == 0) {
            _minNameWidth = 0;
            _nameWidth = 0;
        }
    }

    /**
     * Retrieve the configure width for the name column
     *
     * @param width
     *            Unused?
     * @return The width
     */
    public int getNameWidthPref(int width) {
        return _nameWidthPref;
    }

    /**
     * Returns the primary control associated with this viewer.
     *
     * @return the SWT control which displays this viewer's content
     */
    public Control getControl() {
        return _dataViewer;
    }

    /**
     * Returns the time graph control associated with this viewer.
     *
     * @return the time graph control
     * @since 2.0
     */
    public TimeGraphControl getTimeGraphControl() {
        return _stateCtrl;
    }

    /**
     * Returns the time graph scale associated with this viewer.
     *
     * @return the time graph scale
     * @since 2.0
     */
    public TimeGraphScale getTimeGraphScale() {
        return _timeScaleCtrl;
    }

    /**
     * Return the x coordinate corresponding to a time
     *
     * @param time the time
     * @return the x coordinate corresponding to the time
     *
     * @since 2.0
     */
    public int getXForTime(long time) {
        return _stateCtrl.getXForTime(time);
    }

    /**
     * Return the time corresponding to an x coordinate
     *
     * @param x the x coordinate
     * @return the time corresponding to the x coordinate
     *
     * @since 2.0
     */
    public long getTimeAtX(int x) {
        return _stateCtrl.getTimeAtX(x);
    }

    /**
     * Get the selection provider
     *
     * @return the selection provider
     */
    public ISelectionProvider getSelectionProvider() {
        return _stateCtrl;
    }

    /**
     * Wait for the cursor
     *
     * @param waitInd
     *            Wait indefinitely?
     */
    public void waitCursor(boolean waitInd) {
        _stateCtrl.waitCursor(waitInd);
    }

    /**
     * Get the horizontal scroll bar object
     *
     * @return The scroll bar
     */
    public ScrollBar getHorizontalBar() {
        return _stateCtrl.getHorizontalBar();
    }

    /**
     * Get the vertical scroll bar object
     *
     * @return The scroll bar
     */
    public Slider getVerticalBar() {
        return _verticalScrollBar;
    }

    /**
     * Set the given index as the top one
     *
     * @param index
     *            The index that will go to the top
     */
    public void setTopIndex(int index) {
        _stateCtrl.setTopIndex(index);
        adjustVerticalScrollBar();
    }

    /**
     * Retrieve the current top index
     *
     * @return The top index
     */
    public int getTopIndex() {
        return _stateCtrl.getTopIndex();
    }

    /**
     * Set the expanded state of an entry
     *
     * @param entry
     *            The entry to expand/collapse
     * @param expanded
     *            True for expanded, false for collapsed
     */
    public void setExpandedState(ITimeGraphEntry entry, boolean expanded) {
        _stateCtrl.setExpandedState(entry, expanded);
        adjustVerticalScrollBar();
    }

    /**
     * Collapses all nodes of the viewer's tree, starting with the root.
     *
     * @since 2.0
     */
    public void collapseAll() {
        _stateCtrl.collapseAll();
        adjustVerticalScrollBar();
    }

    /**
     * Expands all nodes of the viewer's tree, starting with the root.
     *
     * @since 2.0
     */
    public void expandAll() {
        _stateCtrl.expandAll();
        adjustVerticalScrollBar();
    }

    /**
     * Get the number of sub-elements when expanded
     *
     * @return The element count
     */
    public int getExpandedElementCount() {
        return _stateCtrl.getExpandedElementCount();
    }

    /**
     * Get the sub-elements
     *
     * @return The array of entries that are below this one
     */
    public ITimeGraphEntry[] getExpandedElements() {
        return _stateCtrl.getExpandedElements();
    }

    /**
     * Add a tree listener
     *
     * @param listener
     *            The listener to add
     */
    public void addTreeListener(ITimeGraphTreeListener listener) {
        _stateCtrl.addTreeListener(listener);
    }

    /**
     * Remove a tree listener
     *
     * @param listener
     *            The listener to remove
     */
    public void removeTreeListener(ITimeGraphTreeListener listener) {
        _stateCtrl.removeTreeListener(listener);
    }

    /**
     * Get the reset scale action.
     *
     * @return The Action object
     */
    public Action getResetScaleAction() {
        if (resetScale == null) {
            // resetScale
            resetScale = new Action() {
                @Override
                public void run() {
                    resetStartFinishTime();
                    notifyStartFinishTime();
                }
            };
            resetScale.setText(Messages.TmfTimeGraphViewer_ResetScaleActionNameText);
            resetScale.setToolTipText(Messages.TmfTimeGraphViewer_ResetScaleActionToolTipText);
            resetScale.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_HOME_MENU));
        }
        return resetScale;
    }

    /**
     * Get the show legend action.
     *
     * @return The Action object
     */
    public Action getShowLegendAction() {
        if (showLegendAction == null) {
            // showLegend
            showLegendAction = new Action() {
                @Override
                public void run() {
                    showLegend();
                }
            };
            showLegendAction.setText(Messages.TmfTimeGraphViewer_LegendActionNameText);
            showLegendAction.setToolTipText(Messages.TmfTimeGraphViewer_LegendActionToolTipText);
            showLegendAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_SHOW_LEGEND));
        }

        return showLegendAction;
    }

    /**
     * Get the the next event action.
     *
     * @return The action object
     */
    public Action getNextEventAction() {
        if (nextEventAction == null) {
            nextEventAction = new Action() {
                @Override
                public void run() {
                    selectNextEvent();
                }
            };

            nextEventAction.setText(Messages.TmfTimeGraphViewer_NextEventActionNameText);
            nextEventAction.setToolTipText(Messages.TmfTimeGraphViewer_NextEventActionToolTipText);
            nextEventAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_NEXT_EVENT));
        }

        return nextEventAction;
    }

    /**
     * Get the previous event action.
     *
     * @return The Action object
     */
    public Action getPreviousEventAction() {
        if (prevEventAction == null) {
            prevEventAction = new Action() {
                @Override
                public void run() {
                    selectPrevEvent();
                }
            };

            prevEventAction.setText(Messages.TmfTimeGraphViewer_PreviousEventActionNameText);
            prevEventAction.setToolTipText(Messages.TmfTimeGraphViewer_PreviousEventActionToolTipText);
            prevEventAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_PREV_EVENT));
        }

        return prevEventAction;
    }

    /**
     * Get the next item action.
     *
     * @return The Action object
     */
    public Action getNextItemAction() {
        if (nextItemAction == null) {

            nextItemAction = new Action() {
                @Override
                public void run() {
                    selectNextItem();
                }
            };
            nextItemAction.setText(Messages.TmfTimeGraphViewer_NextItemActionNameText);
            nextItemAction.setToolTipText(Messages.TmfTimeGraphViewer_NextItemActionToolTipText);
            nextItemAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_NEXT_ITEM));
        }
        return nextItemAction;
    }

    /**
     * Get the previous item action.
     *
     * @return The Action object
     */
    public Action getPreviousItemAction() {
        if (previousItemAction == null) {

            previousItemAction = new Action() {
                @Override
                public void run() {
                    selectPrevItem();
                }
            };
            previousItemAction.setText(Messages.TmfTimeGraphViewer_PreviousItemActionNameText);
            previousItemAction.setToolTipText(Messages.TmfTimeGraphViewer_PreviousItemActionToolTipText);
            previousItemAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_PREV_ITEM));
        }
        return previousItemAction;
    }

    /**
     * Get the zoom in action
     *
     * @return The Action object
     */
    public Action getZoomInAction() {
        if (zoomInAction == null) {
            zoomInAction = new Action() {
                @Override
                public void run() {
                    zoomIn();
                }
            };
            zoomInAction.setText(Messages.TmfTimeGraphViewer_ZoomInActionNameText);
            zoomInAction.setToolTipText(Messages.TmfTimeGraphViewer_ZoomInActionToolTipText);
            zoomInAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_ZOOM_IN_MENU));
        }
        return zoomInAction;
    }

    /**
     * Get the zoom out action
     *
     * @return The Action object
     */
    public Action getZoomOutAction() {
        if (zoomOutAction == null) {
            zoomOutAction = new Action() {
                @Override
                public void run() {
                    zoomOut();
                }
            };
            zoomOutAction.setText(Messages.TmfTimeGraphViewer_ZoomOutActionNameText);
            zoomOutAction.setToolTipText(Messages.TmfTimeGraphViewer_ZoomOutActionToolTipText);
            zoomOutAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_ZOOM_OUT_MENU));
        }
        return zoomOutAction;
    }


    private void adjustVerticalScrollBar() {
        int topIndex = _stateCtrl.getTopIndex();
        int countPerPage = _stateCtrl.countPerPage();
        int expandedElementCount = _stateCtrl.getExpandedElementCount();
        if (topIndex + countPerPage > expandedElementCount) {
            _stateCtrl.setTopIndex(Math.max(0, expandedElementCount - countPerPage));
        }

        int selection = _stateCtrl.getTopIndex();
        int min = 0;
        int max = Math.max(1, expandedElementCount - 1);
        int thumb = Math.min(max, Math.max(1, countPerPage - 1));
        int increment = 1;
        int pageIncrement = Math.max(1, countPerPage);
        _verticalScrollBar.setValues(selection, min, max, thumb, increment, pageIncrement);
    }

    /**
     * @param listener a {@link MenuDetectListener}
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphControl#addTimeGraphEntryMenuListener(org.eclipse.swt.events.MenuDetectListener)
     * @since 1.2
     */
    public void addTimeGraphEntryMenuListener(MenuDetectListener listener) {
        _stateCtrl.addTimeGraphEntryMenuListener(listener);
    }

    /**
     * @param listener a {@link MenuDetectListener}
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphControl#removeTimeGraphEntryMenuListener(org.eclipse.swt.events.MenuDetectListener)
     * @since 1.2
     */
    public void removeTimeGraphEntryMenuListener(MenuDetectListener listener) {
        _stateCtrl.removeTimeGraphEntryMenuListener(listener);
    }

    /**
     * @param listener a {@link MenuDetectListener}
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphControl#addTimeEventMenuListener(org.eclipse.swt.events.MenuDetectListener)
     * @since 1.2
     */
    public void addTimeEventMenuListener(MenuDetectListener listener) {
        _stateCtrl.addTimeEventMenuListener(listener);
    }

    /**
     * @param listener a {@link MenuDetectListener}
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphControl#removeTimeEventMenuListener(org.eclipse.swt.events.MenuDetectListener)
     * @since 1.2
     */
    public void removeTimeEventMenuListener(MenuDetectListener listener) {
        _stateCtrl.removeTimeEventMenuListener(listener);
    }

    /**
     * @param filter The filter object to be attached to the view
     * @since 2.0
     */
    public void addFilter(ViewerFilter filter) {
        _stateCtrl.addFilter(filter);
        refresh();
    }

    /**
     * @param filter The filter object to be attached to the view
     * @since 2.0
     */
    public void removeFilter(ViewerFilter filter) {
        _stateCtrl.removeFilter(filter);
        refresh();
    }

}
