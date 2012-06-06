/*****************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation, 2009, 2010, 2011, 2012 Ericsson.
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
 *
 *****************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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

    // Calender Time format, using Epoch reference or Relative time
    // format(default
    private boolean calendarTimeFormat = false;
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

    /** ctor */
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
     * @param input the input of this time graph viewer, or <code>null</code> if none
     */
    public void setInput(ITimeGraphEntry[] input) {
        if (null != _stateCtrl) {
            if (null == input) {
                input = new ITimeGraphEntry[0];
            }
            setTimeRange(input);
            _verticalScrollBar.setEnabled(true);
            setTopIndex(0);
            _selectedTime = 0;
            refreshAllData(input);
        }
    }

    public void refresh() {
        setInput(_stateCtrl.getTraces());
    }

    public void controlMoved(ControlEvent e) {
    }

    public void controlResized(ControlEvent e) {
        resizeControls();
    }

    // called from the display order in the API
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

    protected String getViewTypeStr() {
        return "viewoption.threads"; //$NON-NLS-1$
    }

    int getMarginWidth(int idx) {
        return 0;
    }

    int getMarginHeight(int idx) {
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

        _stateCtrl = createTimeGraphControl();

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

    public void dispose() {
        saveOptions();
        _stateCtrl.dispose();
        _dataViewer.dispose();
        _colors.dispose();
    }

    protected TimeGraphControl createTimeGraphControl() {
        return new TimeGraphControl(_dataViewer, _colors);
    }

    public void resizeControls() {
        Rectangle r = _dataViewer.getClientArea();
        if (r.isEmpty())
            return;

        int width = r.width;
        if (_nameWidth > width - _minNameWidth)
            _nameWidth = width - _minNameWidth;
        if (_nameWidth < _minNameWidth)
            _nameWidth = _minNameWidth;
        adjustVerticalScrollBar();
    }

    /** Tries to set most convenient time range for display. */
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

    public void setTimeBounds() {
        //_time0_ = _beginTime - (long) ((_endTime - _beginTime) * 0.02);
        _time0_ = _beginTime;
        if (_time0_ < 0)
            _time0_ = 0;
        // _time1_ = _time0_ + (_endTime - _time0_) * 1.05;
        _time1_ = _endTime;
        // _time0_ = Math.floor(_time0_);
        // _time1_ = Math.ceil(_time1_);
        if (!_timeRangeFixed) {
            _time0 = _time0_;
            _time1 = _time1_;
        }
        if (_time1 - _time0 < _minTimeInterval) {
            _time1 = _time0 + _minTimeInterval;
        }
    }

    /**
     * @param traces
     * @param start
     * @param end
     */
    void updateInternalData(ITimeGraphEntry[] traces, long start, long end) {
        if (null == traces)
            traces = new ITimeGraphEntry[0];
        if ((start == 0 && end == 0) || start < 0 || end < 0) {
            // Start and end time are unspecified and need to be determined from
            // individual processes
            setTimeRange(traces);
        } else {
            _beginTime = start;
            _endTime = end;
        }

        refreshAllData(traces);
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

    public void setFocus() {
        if (null != _stateCtrl)
            _stateCtrl.setFocus();
    }

    public boolean isInFocus() {
        return _stateCtrl.isInFocus();
    }

    public ITimeGraphEntry getSelection() {
        return _stateCtrl.getSelectedTrace();
    }

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
        width = _stateCtrl.getClientArea().width;
        if (_nameWidth > width - 6)
            _nameWidth = width - 6;
        if (_nameWidth < 6)
            _nameWidth = 6;
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.widgets.ITimeDataProvider
     * #setStartFinishTimeNotify(long, long)
     */
    @Override
    public void setStartFinishTimeNotify(long time0, long time1) {
        setStartFinishTime(time0, time1);
        notifyRangeListeners(time0, time1);
    }


    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.widgets.ITimeDataProvider#notifyStartFinishTime()
     */
    @Override
    public void notifyStartFinishTime() {
        notifyRangeListeners(_time0, _time1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.widgets.ITimeDataProvider
     * #setStartFinishTime(long, long)
     */
    @Override
    public void setStartFinishTime(long time0, long time1) {
        _time0 = time0;
        if (_time0 < _time0_)
            _time0 = _time0_;
        if (_time0 > _time1_)
            _time0 = _time1_;
        _time1 = time1;
        if (_time1 < _time0_)
            _time1 = _time0_;
        if (_time1 > _time1_)
            _time1 = _time1_;
        if (_time1 - _time0 < _minTimeInterval)
            _time1 = Math.min(_time1_, _time0 + _minTimeInterval);
        _timeRangeFixed = true;
        _stateCtrl.adjustScrolls();
        _stateCtrl.redraw();
        _timeScaleCtrl.redraw();
    }

    public void setTimeBounds(long beginTime, long endTime) {
        _beginTime = beginTime;
        _endTime = endTime;
        _time0_ = beginTime;
        _time1_ = endTime;
        _stateCtrl.adjustScrolls();
    }

    @Override
    public void resetStartFinishTime() {
        setStartFinishTimeNotify(_time0_, _time1_);
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
            _time1 = _time0 + _minTimeInterval;
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

    public void selectNextEvent() {
        _stateCtrl.selectNextEvent();
        adjustVerticalScrollBar();
    }

    public void selectPrevEvent() {
        _stateCtrl.selectPrevEvent();
        adjustVerticalScrollBar();
    }

    public void selectNextItem() {
        _stateCtrl.selectNextTrace();
        adjustVerticalScrollBar();
    }

    public void selectPrevItem() {
        _stateCtrl.selectPrevTrace();
        adjustVerticalScrollBar();
    }

    public void showLegend() {
        if (_dataViewer == null || _dataViewer.isDisposed())
            return;

        TimeGraphLegend.open(_dataViewer.getShell(), fTimeGraphProvider);
    }

    public void zoomIn() {
        _stateCtrl.zoomIn();
    }

    public void zoomOut() {
        _stateCtrl.zoomOut();
    }

    private String getPreferenceString(String string) {
        return getViewTypeStr() + "." + string; //$NON-NLS-1$
    }

    public void addSelectionListener(ITimeGraphSelectionListener listener) {
        fSelectionListeners.add(listener);
    }

    public void removeSelectionListener(ITimeGraphSelectionListener listener) {
        fSelectionListeners.remove(listener);
    }

    private void notifySelectionListeners(ITimeGraphEntry selection) {
        TimeGraphSelectionEvent event = new TimeGraphSelectionEvent(this, selection);

        for (ITimeGraphSelectionListener listener : fSelectionListeners) {
            listener.selectionChanged(event);
        }
    }

    public void addTimeListener(ITimeGraphTimeListener listener) {
        fTimeListeners.add(listener);
    }

    public void removeTimeListener(ITimeGraphTimeListener listener) {
        fTimeListeners.remove(listener);
    }

    private void notifyTimeListeners(long time) {
        TimeGraphTimeEvent event = new TimeGraphTimeEvent(this, time);

        for (ITimeGraphTimeListener listener : fTimeListeners) {
            listener.timeSelected(event);
        }
    }

    public void addRangeListener(ITimeGraphRangeListener listener) {
        fRangeListeners.add(listener);
    }

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

    public void setSelectedTime(long time, boolean ensureVisible, Object source) {
        if (this == source) {
            return;
        }

        setSelectedTimeNotify(time, ensureVisible);
    }

    public void setSelectedEvent(ITimeEvent event, Object source) {
        if (event == null || source == this) {
            return;
        }
        _selectedEntry = event.getEntry();
        _stateCtrl.selectItem(_selectedEntry, false);

        setSelectedTimeInt(event.getTime(), true, true);
        adjustVerticalScrollBar();
    }

    public void setSelectedTraceTime(ITimeGraphEntry trace, long time, Object source) {
        if (trace == null || source == this) {
            return;
        }
        _selectedEntry = trace;
        _stateCtrl.selectItem(trace, false);

        setSelectedTimeInt(time, true, true);
    }

    public void setSelection(ITimeGraphEntry trace) {
        _selectedEntry = trace;
        _stateCtrl.selectItem(trace, false);
        adjustVerticalScrollBar();
    }

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

    public void setTimeCalendarFormat(boolean toAbsoluteCaltime) {
        calendarTimeFormat = toAbsoluteCaltime;
    }

    @Override
    public boolean isCalendarFormat() {
        return calendarTimeFormat;
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        if (borderWidth > -1) {
            this.borderWidth = borderWidth;
            GridLayout gl = (GridLayout)_dataViewer.getLayout();
            gl.marginHeight = borderWidth;
        }
    }

    public int getHeaderHeight() {
        return timeScaleHeight;
    }

    public void setHeaderHeight(int headerHeight) {
        if (headerHeight > -1) {
            this.timeScaleHeight = headerHeight;
            _timeScaleCtrl.setHeight(headerHeight);
        }
    }

    public int getItemHeight() {
        if (_stateCtrl != null) {
            return _stateCtrl.getItemHeight();
        }
        return 0;
    }

    public void setItemHeight(int rowHeight) {
        if (_stateCtrl != null) {
            _stateCtrl.setItemHeight(rowHeight);
        }
    }

    public void setMinimumItemWidth(int width) {
        if (_stateCtrl != null) {
            _stateCtrl.setMinimumItemWidth(width);
        }
    }

    public void setNameWidthPref(int width) {
        _nameWidthPref = width;
        if (width == 0) {
            _minNameWidth = 0;
            _nameWidth = 0;
        }
    }

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
     */
    TimeGraphControl getTimeGraphControl() {
        return _stateCtrl;
    }

    /**
     * Returns the time graph scale associated with this viewer.
     *
     * @return the time graph scale
     */
    TimeGraphScale getTimeGraphScale() {
        return _timeScaleCtrl;
    }

    /**
     * Get the selection provider
     * 
     * @return the selection provider
     */
    public ISelectionProvider getSelectionProvider() {
        return _stateCtrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.ITimeAnalysisViewer
     * #waitCursor(boolean)
     */
    public void waitCursor(boolean waitInd) {
        _stateCtrl.waitCursor(waitInd);
    }

    public ScrollBar getHorizontalBar() {
        return _stateCtrl.getHorizontalBar();
    }

    public Slider getVerticalBar() {
        return _verticalScrollBar;
    }

    public void setTopIndex(int index) {
        _stateCtrl.setTopIndex(index);
        adjustVerticalScrollBar();
    }

    public int getTopIndex() {
        return _stateCtrl.getTopIndex();
    }

    public void setExpandedState(ITimeGraphEntry entry, boolean expanded) {
        _stateCtrl.setExpandedState(entry, expanded);
        adjustVerticalScrollBar();
    }

    public int getExpandedElementCount() {
        return _stateCtrl.getExpandedElementCount();
    }

    public ITimeGraphEntry[] getExpandedElements() {
        return _stateCtrl.getExpandedElements();
    }

    public void addTreeListener(ITimeGraphTreeListener listener) {
        _stateCtrl.addTreeListener(listener);
    }

    public void removeTreeListener(ITimeGraphTreeListener listener) {
        _stateCtrl.removeTreeListener(listener);
    }

    public Action getResetScaleAction() {
        if (resetScale == null) {
            // resetScale
            resetScale = new Action() {
                @Override
                public void run() {
                    resetStartFinishTime();
                }
            };
            resetScale.setText(Messages.TmfTimeGraphViewer_ResetScaleActionNameText);
            resetScale.setToolTipText(Messages.TmfTimeGraphViewer_ResetScaleActionToolTipText);
            resetScale.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_HOME_MENU));
        } 
        return resetScale;
    }

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
    
    

}
