/*****************************************************************************
 * Copyright (c) 2007, 2014 Intel Corporation, Ericsson, others
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
 *   Geneviève Bastien - Add event links between entries
 *****************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.dialogs.TimeGraphLegend;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ILinkEvent;
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

    /** Constant indicating that all levels of the time graph should be expanded
     * @since 3.1 */
    public static final int ALL_LEVELS = AbstractTreeViewer.ALL_LEVELS;

    private static final int DEFAULT_NAME_WIDTH = 200;
    private static final int MIN_NAME_WIDTH = 6;
    private static final int MAX_NAME_WIDTH = 1000;
    private static final int DEFAULT_HEIGHT = 22;
    private static final long RECENTERING_MARGIN_FACTOR = 50;
    private static final String HIDE_ARROWS_KEY = "hide.arrows"; //$NON-NLS-1$

    private long fMinTimeInterval;
    private ITimeGraphEntry fSelectedEntry;
    private long fBeginTime;
    private long fEndTime;
    private long fTime0;
    private long fTime1;
    private long fSelectionBegin = 0;
    private long fSelectionEnd = 0;
    private long fTime0Bound;
    private long fTime1Bound;
    private long fTime0ExtSynch = 0;
    private long fTime1ExtSynch = 0;
    private boolean fTimeRangeFixed;
    private int fNameWidthPref = DEFAULT_NAME_WIDTH;
    private int fMinNameWidth = MIN_NAME_WIDTH;
    private int fNameWidth;
    private Composite fDataViewer;

    private TimeGraphControl fTimeGraphCtrl;
    private TimeGraphScale fTimeScaleCtrl;
    private Slider fVerticalScrollBar;
    private TimeGraphColorScheme fColorScheme;
    private Object fInputElement;
    private ITimeGraphContentProvider fTimeGraphContentProvider;
    private ITimeGraphPresentationProvider fTimeGraphProvider;

    private List<ITimeGraphSelectionListener> fSelectionListeners = new ArrayList<>();
    private List<ITimeGraphTimeListener> fTimeListeners = new ArrayList<>();
    private List<ITimeGraphRangeListener> fRangeListeners = new ArrayList<>();

    // Time format, using Epoch reference, Relative time format(default) or
    // Number
    private TimeFormat fTimeFormat = TimeFormat.RELATIVE;
    private int fBorderWidth = 0;
    private int fTimeScaleHeight = DEFAULT_HEIGHT;

    private Action fResetScaleAction;
    private Action fShowLegendAction;
    private Action fNextEventAction;
    private Action fPrevEventAction;
    private Action fNextItemAction;
    private Action fPreviousItemAction;
    private Action fZoomInAction;
    private Action fZoomOutAction;
    private Action fHideArrowsAction;
    private Action fFollowArrowFwdAction;
    private Action fFollowArrowBwdAction;

    /**
     * Standard constructor.
     * <p>
     * The default timegraph content provider accepts an ITimeGraphEntry[] as input element.
     *
     * @param parent
     *            The parent UI composite object
     * @param style
     *            The style to use
     */
    public TimeGraphViewer(Composite parent, int style) {
        createDataViewer(parent, style);
        fTimeGraphContentProvider = new ITimeGraphContentProvider() {
            @Override
            public ITimeGraphEntry[] getElements(Object inputElement) {
                if (inputElement instanceof ITimeGraphEntry[]) {
                    return (ITimeGraphEntry[]) inputElement;
                }
                return new ITimeGraphEntry[0];
            }
        };
    }

    /**
     * Sets the timegraph content provider used by this timegraph viewer.
     *
     * @param timeGraphContentProvider
     *            the timegraph content provider
     *
     * @since 3.0
     */
    public void setTimeGraphContentProvider(ITimeGraphContentProvider timeGraphContentProvider) {
        fTimeGraphContentProvider = timeGraphContentProvider;
    }

    /**
     * Gets the timegraph content provider used by this timegraph viewer.
     *
     * @return the timegraph content provider
     *
     * @since 3.0
     */
    public ITimeGraphContentProvider getTimeGraphContentProvider() {
        return fTimeGraphContentProvider;
    }

    /**
     * Sets the timegraph presentation provider used by this timegraph viewer.
     *
     * @param timeGraphProvider
     *            the timegraph provider
     */
    public void setTimeGraphProvider(ITimeGraphPresentationProvider timeGraphProvider) {
        fTimeGraphProvider = timeGraphProvider;
        fTimeGraphCtrl.setTimeGraphProvider(timeGraphProvider);
        TimeGraphTooltipHandler toolTipHandler = new TimeGraphTooltipHandler(fTimeGraphProvider, this);
        toolTipHandler.activateHoverHelp(fTimeGraphCtrl);
    }

    /**
     * Sets or clears the input for this time graph viewer.
     *
     * @param inputElement
     *            The input of this time graph viewer, or <code>null</code> if
     *            none
     *
     * @since 3.0
     */
    public void setInput(Object inputElement) {
        fInputElement = inputElement;
        ITimeGraphEntry[] input = fTimeGraphContentProvider.getElements(inputElement);

        if (fTimeGraphCtrl != null) {
            setTimeRange(input);
            fVerticalScrollBar.setEnabled(true);
            setTopIndex(0);
            fSelectionBegin = 0;
            fSelectionEnd = 0;
            fSelectedEntry = null;
            refreshAllData(input);
        }
    }

    /**
     * Gets the input for this time graph viewer.
     *
     * @return The input of this time graph viewer, or <code>null</code> if none
     *
     * @since 3.0
     */
    public Object getInput() {
        return fInputElement;
    }

    /**
     * Sets (or clears if null) the list of links to display on this combo
     *
     * @param links
     *            the links to display in this time graph combo
     * @since 2.1
     */
    public void setLinks(List<ILinkEvent> links) {
        if (fTimeGraphCtrl != null) {
            fTimeGraphCtrl.refreshArrows(links);
        }
    }

    /**
     * Refresh the view
     */
    public void refresh() {
        ITimeGraphEntry[] input = fTimeGraphContentProvider.getElements(fInputElement);
        setTimeRange(input);
        fVerticalScrollBar.setEnabled(true);
        refreshAllData(input);
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
        if (null != fTimeGraphCtrl) {
            updateInternalData(traces, start, end);
            if (updateTimeBounds) {
                fTimeRangeFixed = true;
                // set window to match limits
                setStartFinishTime(fTime0Bound, fTime1Bound);
            } else {
                fTimeGraphCtrl.redraw();
                fTimeScaleCtrl.redraw();
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
        fMinTimeInterval = 1;
        fSelectionBegin = -1;
        fSelectionEnd = -1;
        fNameWidth = Utils.loadIntOption(getPreferenceString("namewidth"), //$NON-NLS-1$
                fNameWidthPref, fMinNameWidth, MAX_NAME_WIDTH);
    }

    void saveOptions() {
        Utils.saveIntOption(getPreferenceString("namewidth"), fNameWidth); //$NON-NLS-1$
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
        fColorScheme = new TimeGraphColorScheme();
        fDataViewer = new Composite(parent, style) {
            @Override
            public void redraw() {
                fTimeScaleCtrl.redraw();
                fTimeGraphCtrl.redraw();
                super.redraw();
            }
        };
        GridLayout gl = new GridLayout(2, false);
        gl.marginHeight = fBorderWidth;
        gl.marginWidth = 0;
        gl.verticalSpacing = 0;
        gl.horizontalSpacing = 0;
        fDataViewer.setLayout(gl);

        fTimeScaleCtrl = new TimeGraphScale(fDataViewer, fColorScheme);
        fTimeScaleCtrl.setTimeProvider(this);
        fTimeScaleCtrl.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        fTimeScaleCtrl.setHeight(fTimeScaleHeight);

        fVerticalScrollBar = new Slider(fDataViewer, SWT.VERTICAL | SWT.NO_FOCUS);
        fVerticalScrollBar.setLayoutData(new GridData(SWT.DEFAULT, SWT.FILL, false, true, 1, 2));
        fVerticalScrollBar.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setTopIndex(fVerticalScrollBar.getSelection());
            }
        });
        fVerticalScrollBar.setEnabled(false);

        fTimeGraphCtrl = createTimeGraphControl(fDataViewer, fColorScheme);

        fTimeGraphCtrl.setTimeProvider(this);
        fTimeGraphCtrl.setTimeGraphScale(fTimeScaleCtrl);
        fTimeGraphCtrl.addSelectionListener(this);
        fTimeGraphCtrl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
        fTimeGraphCtrl.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseScrolled(MouseEvent e) {
                adjustVerticalScrollBar();
            }
        });
        fTimeGraphCtrl.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.character == '+') {
                    zoomIn();
                } else if (e.character == '-') {
                    zoomOut();
                }
                adjustVerticalScrollBar();
            }
        });

        Composite filler = new Composite(fDataViewer, SWT.NONE);
        GridData gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
        gd.heightHint = fTimeGraphCtrl.getHorizontalBar().getSize().y;
        filler.setLayoutData(gd);
        filler.setLayout(new FillLayout());

        fTimeGraphCtrl.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent event) {
                resizeControls();
            }
        });
        resizeControls();
        fDataViewer.update();
        adjustVerticalScrollBar();
        return fDataViewer;
    }

    /**
     * Dispose the view.
     */
    public void dispose() {
        saveOptions();
        fTimeGraphCtrl.dispose();
        fDataViewer.dispose();
        fColorScheme.dispose();
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
        Rectangle r = fDataViewer.getClientArea();
        if (r.isEmpty()) {
            return;
        }

        int width = r.width;
        if (fNameWidth > width - fMinNameWidth) {
            fNameWidth = width - fMinNameWidth;
        }
        if (fNameWidth < fMinNameWidth) {
            fNameWidth = fMinNameWidth;
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
        fEndTime = 0;
        fBeginTime = -1;
        for (int i = 0; i < traces.length; i++) {
            ITimeGraphEntry entry = traces[i];
            if (entry.getEndTime() >= entry.getStartTime() && entry.getEndTime() > 0) {
                if (fBeginTime < 0 || entry.getStartTime() < fBeginTime) {
                    fBeginTime = entry.getStartTime();
                }
                if (entry.getEndTime() > fEndTime) {
                    fEndTime = entry.getEndTime();
                }
            }
        }

        if (fBeginTime < 0) {
            fBeginTime = 0;
        }
    }

    /**
     * Recalculate the time bounds
     */
    public void setTimeBounds() {
        fTime0Bound = fBeginTime;
        if (fTime0Bound < 0) {
            fTime0Bound = 0;
        }
        fTime1Bound = fEndTime;
        if (!fTimeRangeFixed) {
            fTime0 = fTime0Bound;
            fTime1 = fTime1Bound;
        }
        fTime0 = Math.max(fTime0Bound, Math.min(fTime0, fTime1Bound));
        fTime1 = Math.max(fTime0Bound, Math.min(fTime1, fTime1Bound));
        if (fTime1 - fTime0 < fMinTimeInterval) {
            fTime1 = Math.min(fTime1Bound, fTime0 + fMinTimeInterval);
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
            fBeginTime = start;
            fEndTime = end;
        }

        refreshAllData(realTraces);
    }

    /**
     * @param traces
     */
    private void refreshAllData(ITimeGraphEntry[] traces) {
        setTimeBounds();
        if (fSelectionBegin < fBeginTime) {
            fSelectionBegin = fBeginTime;
        } else if (fSelectionBegin > fEndTime) {
            fSelectionBegin = fEndTime;
        }
        if (fSelectionEnd < fBeginTime) {
            fSelectionEnd = fBeginTime;
        } else if (fSelectionEnd > fEndTime) {
            fSelectionEnd = fEndTime;
        }
        fTimeGraphCtrl.refreshData(traces);
        fTimeScaleCtrl.redraw();
        adjustVerticalScrollBar();
    }

    /**
     * Callback for when this view is focused
     */
    public void setFocus() {
        if (null != fTimeGraphCtrl) {
            fTimeGraphCtrl.setFocus();
        }
    }

    /**
     * Get the current focus status of this view.
     *
     * @return If the view is currently focused, or not
     */
    public boolean isInFocus() {
        return fTimeGraphCtrl.isInFocus();
    }

    /**
     * Get the view's current selection
     *
     * @return The entry that is selected
     */
    public ITimeGraphEntry getSelection() {
        return fTimeGraphCtrl.getSelectedTrace();
    }

    /**
     * Get the index of the current selection
     *
     * @return The index
     */
    public int getSelectionIndex() {
        return fTimeGraphCtrl.getSelectedIndex();
    }

    @Override
    public long getTime0() {
        return fTime0;
    }

    @Override
    public long getTime1() {
        return fTime1;
    }

    @Override
    public long getMinTimeInterval() {
        return fMinTimeInterval;
    }

    @Override
    public int getNameSpace() {
        return fNameWidth;
    }

    @Override
    public void setNameSpace(int width) {
        fNameWidth = width;
        int w = fTimeGraphCtrl.getClientArea().width;
        if (fNameWidth > w - MIN_NAME_WIDTH) {
            fNameWidth = w - MIN_NAME_WIDTH;
        }
        if (fNameWidth < MIN_NAME_WIDTH) {
            fNameWidth = MIN_NAME_WIDTH;
        }
        fTimeGraphCtrl.adjustScrolls();
        fTimeGraphCtrl.redraw();
        fTimeScaleCtrl.redraw();
    }

    @Override
    public int getTimeSpace() {
        int w = fTimeGraphCtrl.getClientArea().width;
        return w - fNameWidth;
    }

    @Override
    public long getBeginTime() {
        return fBeginTime;
    }

    @Override
    public long getEndTime() {
        return fEndTime;
    }

    @Override
    public long getMaxTime() {
        return fTime1Bound;
    }

    @Override
    public long getMinTime() {
        return fTime0Bound;
    }

    /**
     * @since 2.1
     */
    @Override
    public long getSelectionBegin() {
        return fSelectionBegin;
    }

    /**
     * @since 2.1
     */
    @Override
    public long getSelectionEnd() {
        return fSelectionEnd;
    }

    @Override
    public void setStartFinishTimeNotify(long time0, long time1) {
        setStartFinishTime(time0, time1);
        notifyRangeListeners(fTime0, fTime1);
    }

    @Override
    public void notifyStartFinishTime() {
        notifyRangeListeners(fTime0, fTime1);
    }

    @Override
    public void setStartFinishTime(long time0, long time1) {
        fTime0 = time0;
        if (fTime0 < fTime0Bound) {
            fTime0 = fTime0Bound;
        }
        if (fTime0 > fTime1Bound) {
            fTime0 = fTime1Bound;
        }
        fTime1 = time1;
        if (fTime1 < fTime0Bound) {
            fTime1 = fTime0Bound;
        }
        if (fTime1 > fTime1Bound) {
            fTime1 = fTime1Bound;
        }
        if (fTime1 - fTime0 < fMinTimeInterval) {
            fTime1 = Math.min(fTime1Bound, fTime0 + fMinTimeInterval);
        }
        fTimeRangeFixed = true;
        fTimeGraphCtrl.adjustScrolls();
        fTimeGraphCtrl.redraw();
        fTimeScaleCtrl.redraw();
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
        if (endTime >= beginTime) {
            fBeginTime = beginTime;
            fEndTime = endTime;
            fTime0Bound = beginTime;
            fTime1Bound = endTime;
        } else {
            fBeginTime = 0;
            fEndTime = 0;
            fTime0Bound = 0;
            fTime1Bound = 0;
        }
        fTimeGraphCtrl.adjustScrolls();
    }

    @Override
    public void resetStartFinishTime() {
        setStartFinishTime(fTime0Bound, fTime1Bound);
        fTimeRangeFixed = false;
    }

    @Override
    public void setSelectedTimeNotify(long time, boolean ensureVisible) {
        setSelectedTimeInt(time, ensureVisible, true);
    }

    @Override
    public void setSelectedTime(long time, boolean ensureVisible) {
        setSelectedTimeInt(time, ensureVisible, false);
    }

    /**
     * @since 2.1
     */
    @Override
    public void setSelectionRangeNotify(long beginTime, long endTime) {
        boolean changed = (beginTime != fSelectionBegin || endTime != fSelectionEnd);
        fSelectionBegin = Math.max(fTime0Bound, Math.min(fTime1Bound, beginTime));
        fSelectionEnd = Math.max(fTime0Bound, Math.min(fTime1Bound, endTime));
        fTimeGraphCtrl.redraw();
        fTimeScaleCtrl.redraw();
        if (changed) {
            notifyTimeListeners(fSelectionBegin, fSelectionEnd);
        }
    }

    /**
     * @since 2.1
     */
    @Override
    public void setSelectionRange(long beginTime, long endTime) {
        fSelectionBegin = Math.max(fTime0Bound, Math.min(fTime1Bound, beginTime));
        fSelectionEnd = Math.max(fTime0Bound, Math.min(fTime1Bound, endTime));
        fTimeGraphCtrl.redraw();
        fTimeScaleCtrl.redraw();
    }

    private void setSelectedTimeInt(long time, boolean ensureVisible, boolean doNotify) {
        long time0 = fTime0;
        long time1 = fTime1;
        if (ensureVisible) {
            long timeSpace = (fTime1 - fTime0) / RECENTERING_MARGIN_FACTOR;
            long timeMid = (fTime1 - fTime0) / 2;
            if (time < fTime0 + timeSpace) {
                long dt = fTime0 - time + timeMid;
                fTime0 -= dt;
                fTime1 -= dt;
            } else if (time > fTime1 - timeSpace) {
                long dt = time - fTime1 + timeMid;
                fTime0 += dt;
                fTime1 += dt;
            }
            if (fTime0 < fTime0Bound) {
                fTime1 = Math.min(fTime1Bound, fTime1 + (fTime0Bound - fTime0));
                fTime0 = fTime0Bound;
            } else if (fTime1 > fTime1Bound) {
                fTime0 = Math.max(fTime0Bound, fTime0 - (fTime1 - fTime1Bound));
                fTime1 = fTime1Bound;
            }
        }
        if (fTime1 - fTime0 < fMinTimeInterval) {
            fTime1 = Math.min(fTime1Bound, fTime0 + fMinTimeInterval);
        }
        fTimeGraphCtrl.adjustScrolls();
        fTimeGraphCtrl.redraw();
        fTimeScaleCtrl.redraw();

        boolean notifySelectedTime = (time != fSelectionBegin || time != fSelectionEnd);
        fSelectionBegin = time;
        fSelectionEnd = time;

        if (doNotify && ((time0 != fTime0) || (time1 != fTime1))) {
            notifyRangeListeners(fTime0, fTime1);
        }

        if (doNotify && notifySelectedTime) {
            notifyTimeListeners(fSelectionBegin, fSelectionEnd);
        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        if (fSelectedEntry != getSelection()) {
            fSelectedEntry = getSelection();
            notifySelectionListeners(fSelectedEntry);
        }
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (fSelectedEntry != getSelection()) {
            fSelectedEntry = getSelection();
            notifySelectionListeners(fSelectedEntry);
        }
    }

    /**
     * Callback for when the next event is selected
     */
    public void selectNextEvent() {
        fTimeGraphCtrl.selectNextEvent();
        adjustVerticalScrollBar();
    }

    /**
     * Callback for when the previous event is selected
     */
    public void selectPrevEvent() {
        fTimeGraphCtrl.selectPrevEvent();
        adjustVerticalScrollBar();
    }

    /**
     * Callback for when the next item is selected
     */
    public void selectNextItem() {
        fTimeGraphCtrl.selectNextTrace();
        adjustVerticalScrollBar();
    }

    /**
     * Callback for when the previous item is selected
     */
    public void selectPrevItem() {
        fTimeGraphCtrl.selectPrevTrace();
        adjustVerticalScrollBar();
    }

    /**
     * Callback for the show legend action
     */
    public void showLegend() {
        if (fDataViewer == null || fDataViewer.isDisposed()) {
            return;
        }

        TimeGraphLegend.open(fDataViewer.getShell(), fTimeGraphProvider);
    }

    /**
     * Callback for the Zoom In action
     */
    public void zoomIn() {
        fTimeGraphCtrl.zoomIn();
    }

    /**
     * Callback for the Zoom Out action
     */
    public void zoomOut() {
        fTimeGraphCtrl.zoomOut();
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

    private void notifyTimeListeners(long startTime, long endTime) {
        TimeGraphTimeEvent event = new TimeGraphTimeEvent(this, startTime, endTime);

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
        if (startTime != fTime0ExtSynch || endTime != fTime1ExtSynch) {
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
        fSelectedEntry = event.getEntry();
        fTimeGraphCtrl.selectItem(fSelectedEntry, false);

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
        fSelectedEntry = trace;
        fTimeGraphCtrl.selectItem(trace, false);

        setSelectedTimeInt(time, true, true);
    }

    /**
     * Callback for a trace selection
     *
     * @param trace
     *            The trace that was selected
     */
    public void setSelection(ITimeGraphEntry trace) {
        fSelectedEntry = trace;
        fTimeGraphCtrl.selectItem(trace, false);
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
        fTime0ExtSynch = fTime0;
        fTime1ExtSynch = fTime1;
    }

    /**
     * @since 2.0
     */
    @Override
    public TimeFormat getTimeFormat() {
        return fTimeFormat;
    }

    /**
     * @param tf
     *            the {@link TimeFormat} used to display timestamps
     * @since 2.0
     */
    public void setTimeFormat(TimeFormat tf) {
        this.fTimeFormat = tf;
    }

    /**
     * Retrieve the border width
     *
     * @return The width
     */
    public int getBorderWidth() {
        return fBorderWidth;
    }

    /**
     * Set the border width
     *
     * @param borderWidth
     *            The width
     */
    public void setBorderWidth(int borderWidth) {
        if (borderWidth > -1) {
            this.fBorderWidth = borderWidth;
            GridLayout gl = (GridLayout) fDataViewer.getLayout();
            gl.marginHeight = borderWidth;
        }
    }

    /**
     * Retrieve the height of the header
     *
     * @return The height
     */
    public int getHeaderHeight() {
        return fTimeScaleHeight;
    }

    /**
     * Set the height of the header
     *
     * @param headerHeight
     *            The height to set
     */
    public void setHeaderHeight(int headerHeight) {
        if (headerHeight > -1) {
            this.fTimeScaleHeight = headerHeight;
            fTimeScaleCtrl.setHeight(headerHeight);
        }
    }

    /**
     * Retrieve the height of an item row
     *
     * @return The height
     */
    public int getItemHeight() {
        if (fTimeGraphCtrl != null) {
            return fTimeGraphCtrl.getItemHeight();
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
        if (fTimeGraphCtrl != null) {
            fTimeGraphCtrl.setItemHeight(rowHeight);
        }
    }

    /**
     * Set the minimum item width
     *
     * @param width
     *            The min width
     */
    public void setMinimumItemWidth(int width) {
        if (fTimeGraphCtrl != null) {
            fTimeGraphCtrl.setMinimumItemWidth(width);
        }
    }

    /**
     * Set the width for the name column
     *
     * @param width
     *            The width
     */
    public void setNameWidthPref(int width) {
        fNameWidthPref = width;
        if (width == 0) {
            fMinNameWidth = 0;
            fNameWidth = 0;
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
        return fNameWidthPref;
    }

    /**
     * Returns the primary control associated with this viewer.
     *
     * @return the SWT control which displays this viewer's content
     */
    public Control getControl() {
        return fDataViewer;
    }

    /**
     * Returns the time graph control associated with this viewer.
     *
     * @return the time graph control
     * @since 2.0
     */
    public TimeGraphControl getTimeGraphControl() {
        return fTimeGraphCtrl;
    }

    /**
     * Returns the time graph scale associated with this viewer.
     *
     * @return the time graph scale
     * @since 2.0
     */
    public TimeGraphScale getTimeGraphScale() {
        return fTimeScaleCtrl;
    }

    /**
     * Return the x coordinate corresponding to a time
     *
     * @param time
     *            the time
     * @return the x coordinate corresponding to the time
     *
     * @since 2.0
     */
    public int getXForTime(long time) {
        return fTimeGraphCtrl.getXForTime(time);
    }

    /**
     * Return the time corresponding to an x coordinate
     *
     * @param x
     *            the x coordinate
     * @return the time corresponding to the x coordinate
     *
     * @since 2.0
     */
    public long getTimeAtX(int x) {
        return fTimeGraphCtrl.getTimeAtX(x);
    }

    /**
     * Get the selection provider
     *
     * @return the selection provider
     */
    public ISelectionProvider getSelectionProvider() {
        return fTimeGraphCtrl;
    }

    /**
     * Wait for the cursor
     *
     * @param waitInd
     *            Wait indefinitely?
     */
    public void waitCursor(boolean waitInd) {
        fTimeGraphCtrl.waitCursor(waitInd);
    }

    /**
     * Get the horizontal scroll bar object
     *
     * @return The scroll bar
     */
    public ScrollBar getHorizontalBar() {
        return fTimeGraphCtrl.getHorizontalBar();
    }

    /**
     * Get the vertical scroll bar object
     *
     * @return The scroll bar
     */
    public Slider getVerticalBar() {
        return fVerticalScrollBar;
    }

    /**
     * Set the given index as the top one
     *
     * @param index
     *            The index that will go to the top
     */
    public void setTopIndex(int index) {
        fTimeGraphCtrl.setTopIndex(index);
        adjustVerticalScrollBar();
    }

    /**
     * Retrieve the current top index
     *
     * @return The top index
     */
    public int getTopIndex() {
        return fTimeGraphCtrl.getTopIndex();
    }

    /**
     * Sets the auto-expand level to be used when the input of the viewer is set
     * using {@link #setInput(Object)}. The value 0 means that there is no
     * auto-expand; 1 means that top-level elements are expanded, but not their
     * children; 2 means that top-level elements are expanded, and their
     * children, but not grand-children; and so on.
     * <p>
     * The value {@link #ALL_LEVELS} means that all subtrees should be expanded.
     * </p>
     * @param level
     *            non-negative level, or <code>ALL_LEVELS</code> to expand all
     *            levels of the tree
     * @since 3.1
     */
    public void setAutoExpandLevel(int level) {
        fTimeGraphCtrl.setAutoExpandLevel(level);
    }

    /**
     * Returns the auto-expand level.
     *
     * @return non-negative level, or <code>ALL_LEVELS</code> if all levels of
     *         the tree are expanded automatically
     * @see #setAutoExpandLevel
     * @since 3.1
     */
    public int getAutoExpandLevel() {
        return fTimeGraphCtrl.getAutoExpandLevel();
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
        fTimeGraphCtrl.setExpandedState(entry, expanded);
        adjustVerticalScrollBar();
    }

    /**
     * Collapses all nodes of the viewer's tree, starting with the root.
     *
     * @since 2.0
     */
    public void collapseAll() {
        fTimeGraphCtrl.collapseAll();
        adjustVerticalScrollBar();
    }

    /**
     * Expands all nodes of the viewer's tree, starting with the root.
     *
     * @since 2.0
     */
    public void expandAll() {
        fTimeGraphCtrl.expandAll();
        adjustVerticalScrollBar();
    }

    /**
     * Get the number of sub-elements when expanded
     *
     * @return The element count
     */
    public int getExpandedElementCount() {
        return fTimeGraphCtrl.getExpandedElementCount();
    }

    /**
     * Get the sub-elements
     *
     * @return The array of entries that are below this one
     */
    public ITimeGraphEntry[] getExpandedElements() {
        return fTimeGraphCtrl.getExpandedElements();
    }

    /**
     * Add a tree listener
     *
     * @param listener
     *            The listener to add
     */
    public void addTreeListener(ITimeGraphTreeListener listener) {
        fTimeGraphCtrl.addTreeListener(listener);
    }

    /**
     * Remove a tree listener
     *
     * @param listener
     *            The listener to remove
     */
    public void removeTreeListener(ITimeGraphTreeListener listener) {
        fTimeGraphCtrl.removeTreeListener(listener);
    }

    /**
     * Get the reset scale action.
     *
     * @return The Action object
     */
    public Action getResetScaleAction() {
        if (fResetScaleAction == null) {
            // resetScale
            fResetScaleAction = new Action() {
                @Override
                public void run() {
                    resetStartFinishTime();
                    notifyStartFinishTime();
                }
            };
            fResetScaleAction.setText(Messages.TmfTimeGraphViewer_ResetScaleActionNameText);
            fResetScaleAction.setToolTipText(Messages.TmfTimeGraphViewer_ResetScaleActionToolTipText);
            fResetScaleAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_HOME_MENU));
        }
        return fResetScaleAction;
    }

    /**
     * Get the show legend action.
     *
     * @return The Action object
     */
    public Action getShowLegendAction() {
        if (fShowLegendAction == null) {
            // showLegend
            fShowLegendAction = new Action() {
                @Override
                public void run() {
                    showLegend();
                }
            };
            fShowLegendAction.setText(Messages.TmfTimeGraphViewer_LegendActionNameText);
            fShowLegendAction.setToolTipText(Messages.TmfTimeGraphViewer_LegendActionToolTipText);
            fShowLegendAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_SHOW_LEGEND));
        }

        return fShowLegendAction;
    }

    /**
     * Get the the next event action.
     *
     * @return The action object
     */
    public Action getNextEventAction() {
        if (fNextEventAction == null) {
            fNextEventAction = new Action() {
                @Override
                public void run() {
                    selectNextEvent();
                }
            };

            fNextEventAction.setText(Messages.TmfTimeGraphViewer_NextEventActionNameText);
            fNextEventAction.setToolTipText(Messages.TmfTimeGraphViewer_NextEventActionToolTipText);
            fNextEventAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_NEXT_EVENT));
        }

        return fNextEventAction;
    }

    /**
     * Get the previous event action.
     *
     * @return The Action object
     */
    public Action getPreviousEventAction() {
        if (fPrevEventAction == null) {
            fPrevEventAction = new Action() {
                @Override
                public void run() {
                    selectPrevEvent();
                }
            };

            fPrevEventAction.setText(Messages.TmfTimeGraphViewer_PreviousEventActionNameText);
            fPrevEventAction.setToolTipText(Messages.TmfTimeGraphViewer_PreviousEventActionToolTipText);
            fPrevEventAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_PREV_EVENT));
        }

        return fPrevEventAction;
    }

    /**
     * Get the next item action.
     *
     * @return The Action object
     */
    public Action getNextItemAction() {
        if (fNextItemAction == null) {

            fNextItemAction = new Action() {
                @Override
                public void run() {
                    selectNextItem();
                }
            };
            fNextItemAction.setText(Messages.TmfTimeGraphViewer_NextItemActionNameText);
            fNextItemAction.setToolTipText(Messages.TmfTimeGraphViewer_NextItemActionToolTipText);
            fNextItemAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_NEXT_ITEM));
        }
        return fNextItemAction;
    }

    /**
     * Get the previous item action.
     *
     * @return The Action object
     */
    public Action getPreviousItemAction() {
        if (fPreviousItemAction == null) {

            fPreviousItemAction = new Action() {
                @Override
                public void run() {
                    selectPrevItem();
                }
            };
            fPreviousItemAction.setText(Messages.TmfTimeGraphViewer_PreviousItemActionNameText);
            fPreviousItemAction.setToolTipText(Messages.TmfTimeGraphViewer_PreviousItemActionToolTipText);
            fPreviousItemAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_PREV_ITEM));
        }
        return fPreviousItemAction;
    }

    /**
     * Get the zoom in action
     *
     * @return The Action object
     */
    public Action getZoomInAction() {
        if (fZoomInAction == null) {
            fZoomInAction = new Action() {
                @Override
                public void run() {
                    zoomIn();
                }
            };
            fZoomInAction.setText(Messages.TmfTimeGraphViewer_ZoomInActionNameText);
            fZoomInAction.setToolTipText(Messages.TmfTimeGraphViewer_ZoomInActionToolTipText);
            fZoomInAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_ZOOM_IN_MENU));
        }
        return fZoomInAction;
    }

    /**
     * Get the zoom out action
     *
     * @return The Action object
     */
    public Action getZoomOutAction() {
        if (fZoomOutAction == null) {
            fZoomOutAction = new Action() {
                @Override
                public void run() {
                    zoomOut();
                }
            };
            fZoomOutAction.setText(Messages.TmfTimeGraphViewer_ZoomOutActionNameText);
            fZoomOutAction.setToolTipText(Messages.TmfTimeGraphViewer_ZoomOutActionToolTipText);
            fZoomOutAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_ZOOM_OUT_MENU));
        }
        return fZoomOutAction;
    }

    /**
     * Get the hide arrows action
     *
     * @param dialogSettings
     *            The dialog settings section where the state should be stored,
     *            or null
     *
     * @return The Action object
     *
     * @since 2.1
     */
    public Action getHideArrowsAction(final IDialogSettings dialogSettings) {
        if (fHideArrowsAction == null) {
            fHideArrowsAction = new Action(Messages.TmfTimeGraphViewer_HideArrowsActionNameText, IAction.AS_CHECK_BOX) {
                @Override
                public void run() {
                    boolean hideArrows = fHideArrowsAction.isChecked();
                    fTimeGraphCtrl.hideArrows(hideArrows);
                    refresh();
                    if (dialogSettings != null) {
                        dialogSettings.put(HIDE_ARROWS_KEY, hideArrows);
                    }
                    if (fFollowArrowFwdAction != null) {
                        fFollowArrowFwdAction.setEnabled(!hideArrows);
                    }
                    if (fFollowArrowBwdAction != null) {
                        fFollowArrowBwdAction.setEnabled(!hideArrows);
                    }
                }
            };
            fHideArrowsAction.setToolTipText(Messages.TmfTimeGraphViewer_HideArrowsActionToolTipText);
            fHideArrowsAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_HIDE_ARROWS));
            if (dialogSettings != null) {
                boolean hideArrows = dialogSettings.getBoolean(HIDE_ARROWS_KEY);
                fTimeGraphCtrl.hideArrows(hideArrows);
                fHideArrowsAction.setChecked(hideArrows);
                if (fFollowArrowFwdAction != null) {
                    fFollowArrowFwdAction.setEnabled(!hideArrows);
                }
                if (fFollowArrowBwdAction != null) {
                    fFollowArrowBwdAction.setEnabled(!hideArrows);
                }
            }
        }
        return fHideArrowsAction;
    }

    /**
     * Get the follow arrow forward action.
     *
     * @return The Action object
     *
     * @since 2.1
     */
    public Action getFollowArrowFwdAction() {
        if (fFollowArrowFwdAction == null) {
            fFollowArrowFwdAction = new Action() {
                @Override
                public void run() {
                    fTimeGraphCtrl.followArrowFwd();
                    adjustVerticalScrollBar();
                }
            };
            fFollowArrowFwdAction.setText(Messages.TmfTimeGraphViewer_FollowArrowForwardActionNameText);
            fFollowArrowFwdAction.setToolTipText(Messages.TmfTimeGraphViewer_FollowArrowForwardActionToolTipText);
            fFollowArrowFwdAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_FOLLOW_ARROW_FORWARD));
            if (fHideArrowsAction != null) {
                fFollowArrowFwdAction.setEnabled(!fHideArrowsAction.isChecked());
            }
        }
        return fFollowArrowFwdAction;
    }

    /**
     * Get the follow arrow backward action.
     *
     * @return The Action object
     *
     * @since 2.1
     */
    public Action getFollowArrowBwdAction() {
        if (fFollowArrowBwdAction == null) {
            fFollowArrowBwdAction = new Action() {
                @Override
                public void run() {
                    fTimeGraphCtrl.followArrowBwd();
                    adjustVerticalScrollBar();
                }
            };
            fFollowArrowBwdAction.setText(Messages.TmfTimeGraphViewer_FollowArrowBackwardActionNameText);
            fFollowArrowBwdAction.setToolTipText(Messages.TmfTimeGraphViewer_FollowArrowBackwardActionToolTipText);
            fFollowArrowBwdAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_FOLLOW_ARROW_BACKWARD));
            if (fHideArrowsAction != null) {
                fFollowArrowBwdAction.setEnabled(!fHideArrowsAction.isChecked());
            }
        }
        return fFollowArrowBwdAction;
    }

    private void adjustVerticalScrollBar() {
        int topIndex = fTimeGraphCtrl.getTopIndex();
        int countPerPage = fTimeGraphCtrl.countPerPage();
        int expandedElementCount = fTimeGraphCtrl.getExpandedElementCount();
        if (topIndex + countPerPage > expandedElementCount) {
            fTimeGraphCtrl.setTopIndex(Math.max(0, expandedElementCount - countPerPage));
        }

        int selection = fTimeGraphCtrl.getTopIndex();
        int min = 0;
        int max = Math.max(1, expandedElementCount - 1);
        int thumb = Math.min(max, Math.max(1, countPerPage - 1));
        int increment = 1;
        int pageIncrement = Math.max(1, countPerPage);
        fVerticalScrollBar.setValues(selection, min, max, thumb, increment, pageIncrement);
    }

    /**
     * @param listener
     *            a {@link MenuDetectListener}
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphControl#addTimeGraphEntryMenuListener(org.eclipse.swt.events.MenuDetectListener)
     * @since 1.2
     */
    public void addTimeGraphEntryMenuListener(MenuDetectListener listener) {
        fTimeGraphCtrl.addTimeGraphEntryMenuListener(listener);
    }

    /**
     * @param listener
     *            a {@link MenuDetectListener}
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphControl#removeTimeGraphEntryMenuListener(org.eclipse.swt.events.MenuDetectListener)
     * @since 1.2
     */
    public void removeTimeGraphEntryMenuListener(MenuDetectListener listener) {
        fTimeGraphCtrl.removeTimeGraphEntryMenuListener(listener);
    }

    /**
     * @param listener
     *            a {@link MenuDetectListener}
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphControl#addTimeEventMenuListener(org.eclipse.swt.events.MenuDetectListener)
     * @since 1.2
     */
    public void addTimeEventMenuListener(MenuDetectListener listener) {
        fTimeGraphCtrl.addTimeEventMenuListener(listener);
    }

    /**
     * @param listener
     *            a {@link MenuDetectListener}
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphControl#removeTimeEventMenuListener(org.eclipse.swt.events.MenuDetectListener)
     * @since 1.2
     */
    public void removeTimeEventMenuListener(MenuDetectListener listener) {
        fTimeGraphCtrl.removeTimeEventMenuListener(listener);
    }

    /**
     * @param filter
     *            The filter object to be attached to the view
     * @since 2.0
     */
    public void addFilter(ViewerFilter filter) {
        fTimeGraphCtrl.addFilter(filter);
        refresh();
    }

    /**
     * @param filter
     *            The filter object to be attached to the view
     * @since 2.0
     */
    public void removeFilter(ViewerFilter filter) {
        fTimeGraphCtrl.removeFilter(filter);
        refresh();
    }

}
