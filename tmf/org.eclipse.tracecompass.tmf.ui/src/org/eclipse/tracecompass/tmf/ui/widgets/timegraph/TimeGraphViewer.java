/*****************************************************************************
 * Copyright (c) 2007, 2017 Intel Corporation, Ericsson, others
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

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.internal.tmf.ui.dialogs.AddBookmarkDialog;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentInfo;
import org.eclipse.tracecompass.tmf.ui.views.ITmfTimeAligned;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.ShowFilterDialogAction;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TimeGraphLegend;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.MarkerEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.IMarkerAxisListener;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.ITimeDataProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeDataProviderCyclesConverter;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphColorScheme;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphMarkerAxis;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphScale;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphTooltipHandler;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;
import org.eclipse.ui.PlatformUI;

/**
 * Generic time graph viewer implementation
 *
 * @author Patrick Tasse, and others
 */
public class TimeGraphViewer extends Viewer implements ITimeDataProvider, IMarkerAxisListener, SelectionListener {

    /** Constant indicating that all levels of the time graph should be expanded */
    public static final int ALL_LEVELS = AbstractTreeViewer.ALL_LEVELS;

    private static final int DEFAULT_NAME_WIDTH = 200;
    private static final int MIN_NAME_WIDTH = 3;
    private static final int MAX_NAME_WIDTH = 1000;
    private static final int DEFAULT_HEIGHT = 22;
    private static final String HIDE_ARROWS_KEY = "hide.arrows"; //$NON-NLS-1$
    private static final long DEFAULT_FREQUENCY = 1000000000L;
    private static final int H_SCROLLBAR_MAX = Integer.MAX_VALUE - 1;

    private static final ImageDescriptor ADD_BOOKMARK = Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_ADD_BOOKMARK);
    private static final ImageDescriptor NEXT_BOOKMARK = Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_NEXT_BOOKMARK);
    private static final ImageDescriptor PREVIOUS_BOOKMARK = Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_PREVIOUS_BOOKMARK);
    private static final ImageDescriptor REMOVE_BOOKMARK = Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_REMOVE_BOOKMARK);

    private long fMinTimeInterval;
    private ITimeGraphEntry fSelectedEntry;
    private long fBeginTime = SWT.DEFAULT; // The user-specified bounds start time
    private long fEndTime = SWT.DEFAULT; // The user-specified bounds end time
    private long fTime0 = SWT.DEFAULT; // The current window start time
    private long fTime1 = SWT.DEFAULT; // The current window end time
    private long fSelectionBegin = SWT.DEFAULT;
    private long fSelectionEnd = SWT.DEFAULT;
    private long fTime0Bound = SWT.DEFAULT; // The bounds start time
    private long fTime1Bound = SWT.DEFAULT; // The bounds end time
    private long fTime0ExtSynch = SWT.DEFAULT;
    private long fTime1ExtSynch = SWT.DEFAULT;
    private boolean fTimeRangeFixed;
    private int fNameWidthPref = DEFAULT_NAME_WIDTH;
    private int fMinNameWidth = MIN_NAME_WIDTH;
    private int fNameWidth;
    private int[] fWeights;
    private Composite fDataViewer;

    private TimeGraphControl fTimeGraphCtrl;
    private Tree fTree;
    private TimeGraphScale fTimeScaleCtrl;
    private TimeGraphMarkerAxis fMarkerAxisCtrl;
    private Slider fHorizontalScrollBar;
    private Slider fVerticalScrollBar;
    private @NonNull TimeGraphColorScheme fColorScheme = new TimeGraphColorScheme();
    private Object fInputElement;
    private ITimeGraphContentProvider fTimeGraphContentProvider;
    private ITimeGraphPresentationProvider fTimeGraphProvider;
    private ITableLabelProvider fLabelProvider;
    private @NonNull ITimeDataProvider fTimeDataProvider = this;
    private TimeGraphTooltipHandler fToolTipHandler;

    private List<ITimeGraphSelectionListener> fSelectionListeners = new ArrayList<>();
    private List<ITimeGraphTimeListener> fTimeListeners = new ArrayList<>();
    private List<ITimeGraphRangeListener> fRangeListeners = new ArrayList<>();
    private List<ITimeGraphBookmarkListener> fBookmarkListeners = new ArrayList<>();

    // Time format, using Epoch reference, Relative time format(default),
    // Number, or Cycles
    private TimeFormat fTimeFormat = TimeFormat.RELATIVE;
    // Clock frequency to use for Cycles time format
    private long fClockFrequency = DEFAULT_FREQUENCY;
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
    private ShowFilterDialogAction fShowFilterDialogAction;
    private Action fToggleBookmarkAction;
    private Action fNextMarkerAction;
    private Action fPreviousMarkerAction;
    private MenuManager fMarkersMenu;

    /** The list of bookmarks */
    private final List<IMarkerEvent> fBookmarks = new ArrayList<>();

    /** The list of marker categories */
    private final List<String> fMarkerCategories = new ArrayList<>();

    /** The set of hidden marker categories */
    private final Set<String> fHiddenMarkerCategories = new HashSet<>();

    /** The set of skipped marker categories */
    private final Set<String> fSkippedMarkerCategories = new HashSet<>();

    /** The list of markers */
    private final List<IMarkerEvent> fMarkers = new ArrayList<>();

    private ListenerNotifier fListenerNotifier;

    private Composite fTimeAlignedComposite;

    private class ListenerNotifier extends Thread {
        private static final long DELAY = 400L;
        private static final long POLLING_INTERVAL = 10L;
        private long fLastUpdateTime = Long.MAX_VALUE;
        private boolean fSelectionChanged = false;
        private boolean fTimeRangeUpdated = false;
        private boolean fTimeSelected = false;

        @Override
        public void run() {
            while ((System.currentTimeMillis() - fLastUpdateTime) < DELAY) {
                try {
                    Thread.sleep(POLLING_INTERVAL);
                } catch (Exception e) {
                    return;
                }
            }
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (fListenerNotifier != ListenerNotifier.this) {
                        return;
                    }
                    fListenerNotifier = null;
                    if (ListenerNotifier.this.isInterrupted() || fDataViewer.isDisposed()) {
                        return;
                    }
                    if (fSelectionChanged) {
                        fireSelectionChanged(fSelectedEntry);
                    }
                    if (fTimeRangeUpdated) {
                        fireTimeRangeUpdated(fTime0, fTime1);
                    }
                    if (fTimeSelected) {
                        fireTimeSelected(fSelectionBegin, fSelectionEnd);
                    }
                }
            });
        }

        public void selectionChanged() {
            fSelectionChanged = true;
            fLastUpdateTime = System.currentTimeMillis();
        }

        public void timeRangeUpdated() {
            fTimeRangeUpdated = true;
            fLastUpdateTime = System.currentTimeMillis();
        }

        public void timeSelected() {
            fTimeSelected = true;
            fLastUpdateTime = System.currentTimeMillis();
        }

        public boolean hasSelectionChanged() {
            return fSelectionChanged;
        }

        public boolean hasTimeRangeUpdated() {
            return fTimeRangeUpdated;
        }

        public boolean hasTimeSelected() {
            return fTimeSelected;
        }
    }

    private final static class MarkerComparator implements Comparator<IMarkerEvent> {
        @Override
        public int compare(IMarkerEvent o1, IMarkerEvent o2) {
            int res = Long.compare(o1.getTime(), o2.getTime());
            if (res != 0) {
                return res;
            }
            return Long.compare(o1.getDuration(), o2.getDuration());
        }
    }

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
        fTimeGraphContentProvider = new TimeGraphContentProvider();
    }

    /**
     * Sets the timegraph content provider used by this timegraph viewer.
     *
     * @param timeGraphContentProvider
     *            the timegraph content provider
     */
    public void setTimeGraphContentProvider(ITimeGraphContentProvider timeGraphContentProvider) {
        fTimeGraphContentProvider = timeGraphContentProvider;
    }

    /**
     * Gets the timegraph content provider used by this timegraph viewer.
     *
     * @return the timegraph content provider
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
        fToolTipHandler = new TimeGraphTooltipHandler(fTimeGraphProvider, fTimeDataProvider);
        fToolTipHandler.activateHoverHelp(fTimeGraphCtrl);
    }

    /**
     * Sets the tree label provider used for the name space
     *
     * @param labelProvider the tree label provider
     * @since 2.3
     */
    public void setTimeGraphLabelProvider(ITableLabelProvider labelProvider) {
        fLabelProvider = labelProvider;
        if (fTimeGraphCtrl != null) {
            fTimeGraphCtrl.setLabelProvider(labelProvider);
        }
    }

    /**
     * Sets the tree columns for this time graph combo's filter dialog.
     *
     * @param columnNames the tree column names
     * @since 1.2
     */
    public void setFilterColumns(String[] columnNames) {
        getShowFilterDialogAction().getFilterDialog().setColumnNames(columnNames);
    }

    /**
     * Sets the tree content provider used by the filter dialog
     *
     * @param contentProvider the tree content provider
     * @since 1.2
     */
    public void setFilterContentProvider(ITreeContentProvider contentProvider) {
        getShowFilterDialogAction().getFilterDialog().setContentProvider(contentProvider);
    }

    /**
     * Sets the tree label provider used by the filter dialog
     *
     * @param labelProvider the tree label provider
     * @since 1.2
     */
    public void setFilterLabelProvider(ITableLabelProvider labelProvider) {
        getShowFilterDialogAction().getFilterDialog().setLabelProvider(labelProvider);
    }

    @Override
    public void setInput(Object inputElement) {
        Object oldInput = fInputElement;
        fTimeGraphContentProvider.inputChanged(this, oldInput, inputElement);
        fInputElement = inputElement;
        ITimeGraphEntry[] input = fTimeGraphContentProvider.getElements(inputElement);
        fListenerNotifier = null;
        if (fTimeGraphCtrl != null) {
            setTimeRange(input);
            setTopIndex(0);
            fSelectionBegin = SWT.DEFAULT;
            fSelectionEnd = SWT.DEFAULT;
            updateMarkerActions();
            fSelectedEntry = null;
            refreshAllData(input);
        }
    }

    @Override
    public Object getInput() {
        return fInputElement;
    }

    /**
     * Sets (or clears if null) the list of links to display on this combo
     *
     * @param links
     *            the links to display in this time graph combo
     */
    public void setLinks(List<ILinkEvent> links) {
        if (fTimeGraphCtrl != null) {
            fTimeGraphCtrl.refreshArrows(links);
        }
    }

    @Override
    public void refresh() {
        ITimeGraphEntry[] input = fTimeGraphContentProvider.getElements(fInputElement);
        setTimeRange(input);
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
        fSelectionBegin = SWT.DEFAULT;
        fSelectionEnd = SWT.DEFAULT;
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
        fDataViewer = new Composite(parent, style) {
            @Override
            public void redraw() {
                fTree.redraw();
                fTimeScaleCtrl.redraw();
                fTimeGraphCtrl.redraw();
                fMarkerAxisCtrl.redraw();
                super.redraw();
            }
        };
        fDataViewer.addDisposeListener((e) -> {
            if (fMarkersMenu != null) {
                fMarkersMenu.dispose();
            }
        });
        GridLayout gl = new GridLayout(2, false);
        gl.marginHeight = fBorderWidth;
        gl.marginWidth = 0;
        gl.verticalSpacing = 0;
        gl.horizontalSpacing = 0;
        fDataViewer.setLayout(gl);

        fTimeAlignedComposite = new Composite(fDataViewer, style) {
            @Override
            public void redraw() {
                fDataViewer.redraw();
                super.redraw();
            }
        };
        GridLayout gl2 = new GridLayout(2, false);
        gl2.marginHeight = fBorderWidth;
        gl2.marginWidth = 0;
        gl2.verticalSpacing = 0;
        gl2.horizontalSpacing = 0;
        fTimeAlignedComposite.setLayout(gl2);
        fTimeAlignedComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        fTree = new Tree(fTimeAlignedComposite, SWT.NO_SCROLL);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
        gd.widthHint = fNameWidth;
        fTree.setLayoutData(gd);
        fTree.setHeaderVisible(true);
        // add a default column
        TreeColumn column = new TreeColumn(fTree, SWT.LEFT);
        column.setResizable(false);

        /*
         * Bug in Linux. The tree header height is 0 in constructor, so we need
         * to reset it later when the control is painted. This work around used
         * to be done on control resized but the header height was not
         * initialized on the initial resize on GTK3.
         */
        fTree.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                int headerHeight = fTree.getHeaderHeight();
                if (headerHeight > 0) {
                    fTree.removePaintListener(this);
                    setHeaderHeight(headerHeight);
                }
            }
        });

        fTimeScaleCtrl = new TimeGraphScale(fTimeAlignedComposite, fColorScheme);
        fTimeScaleCtrl.setTimeProvider(fTimeDataProvider);
        fTimeScaleCtrl.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        fTimeScaleCtrl.setHeight(fTimeScaleHeight);
        fTimeScaleCtrl.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseScrolled(MouseEvent e) {
                if (e.count == 0) {
                    return;
                }
                if ((e.stateMask & SWT.CTRL) != 0) {
                    fTimeGraphCtrl.zoom(e.count > 0);
                } else {
                    fTimeGraphCtrl.horizontalScroll(e.count > 0);
                }
            }
        });

        fTimeGraphCtrl = createTimeGraphControl(fTimeAlignedComposite, fColorScheme);

        fTimeGraphCtrl.setTimeProvider(this);
        fTimeGraphCtrl.setLabelProvider(fLabelProvider);
        fTimeGraphCtrl.setTree(fTree);
        fTimeGraphCtrl.setTimeGraphScale(fTimeScaleCtrl);
        fTimeGraphCtrl.addSelectionListener(this);
        fTimeGraphCtrl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        fTimeGraphCtrl.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseScrolled(MouseEvent e) {
                if (e.count == 0) {
                    return;
                }
                /*
                 * On some platforms the mouse scroll event is sent to the
                 * control that has focus even if it is not under the cursor.
                 * Handle the event only if not over the time graph control.
                 */
                Point ctrlParentCoords = fTimeAlignedComposite.toControl(fTimeGraphCtrl.toDisplay(e.x, e.y));
                Point scrollBarParentCoords = fDataViewer.toControl(fTimeGraphCtrl.toDisplay(e.x, e.y));
                if (fTimeGraphCtrl.getBounds().contains(ctrlParentCoords)) {
                    /* the time graph control handles the event */
                    adjustVerticalScrollBar();
                } else if (fTimeScaleCtrl.getBounds().contains(ctrlParentCoords)
                        || fMarkerAxisCtrl.getBounds().contains(ctrlParentCoords)
                        || fHorizontalScrollBar.getBounds().contains(scrollBarParentCoords)) {
                    if ((e.stateMask & SWT.CTRL) != 0) {
                        fTimeGraphCtrl.zoom(e.count > 0);
                    } else {
                        fTimeGraphCtrl.horizontalScroll(e.count > 0);
                    }
                } else {
                    /* over the vertical scroll bar or outside of the viewer */
                    setTopIndex(getTopIndex() - e.count);
                }
            }
        });
        fTimeGraphCtrl.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == '.') {
                    boolean extend = (e.stateMask & SWT.SHIFT) != 0;
                    if (extend) {
                        extendToNextMarker();
                    } else {
                        selectNextMarker();
                    }
                } else if (e.keyCode == ',') {
                    boolean extend = (e.stateMask & SWT.SHIFT) != 0;
                    if (extend) {
                        extendToPrevMarker();
                    } else {
                        selectPrevMarker();
                    }
                }
                adjustVerticalScrollBar();
            }
        });

        fMarkerAxisCtrl = createTimeGraphMarkerAxis(fTimeAlignedComposite, fColorScheme, this);
        fMarkerAxisCtrl.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1));
        fMarkerAxisCtrl.addMarkerAxisListener(this);
        fMarkerAxisCtrl.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseScrolled(MouseEvent e) {
                if (e.count == 0) {
                    return;
                }
                if ((e.stateMask & SWT.CTRL) != 0) {
                    fTimeGraphCtrl.zoom(e.count > 0);
                } else {
                    fTimeGraphCtrl.horizontalScroll(e.count > 0);
                }
            }
        });

        fVerticalScrollBar = new Slider(fDataViewer, SWT.VERTICAL | SWT.NO_FOCUS);
        fVerticalScrollBar.setLayoutData(new GridData(SWT.DEFAULT, SWT.FILL, false, true, 1, 1));
        fVerticalScrollBar.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setTopIndex(fVerticalScrollBar.getSelection());
            }
        });

        fHorizontalScrollBar = new Slider(fDataViewer, SWT.HORIZONTAL | SWT.NO_FOCUS);
        fHorizontalScrollBar.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        fHorizontalScrollBar.addListener(SWT.MouseWheel, new Listener() {
            @Override
            public void handleEvent(Event event) {
                // don't handle the immediately following SWT.Selection event
                event.doit = false;
                if (event.count == 0) {
                    return;
                }
                if ((event.stateMask & SWT.CTRL) != 0) {
                    fTimeGraphCtrl.zoom(event.count > 0);
                } else {
                    fTimeGraphCtrl.horizontalScroll(event.count > 0);
                }
            }
        });
        fHorizontalScrollBar.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                int start = fHorizontalScrollBar.getSelection();
                long time0 = getTime0();
                long time1 = getTime1();
                long timeMin = getMinTime();
                long timeMax = getMaxTime();
                long delta = timeMax - timeMin;

                long range = time1 - time0;
                time0 = timeMin + Math.round(delta * ((double) start / H_SCROLLBAR_MAX));
                time1 = time0 + range;

                setStartFinishTimeNotify(time0, time1);
            }
        });

        Composite filler = new Composite(fDataViewer, SWT.NONE);
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
        gd.heightHint = fHorizontalScrollBar.getSize().y;
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
        adjustHorizontalScrollBar();
        adjustVerticalScrollBar();

        fDataViewer.addDisposeListener((e) -> {
            saveOptions();
            fColorScheme.dispose();
        });

        return fDataViewer;
    }

    /**
     * Dispose the time graph viewer.
     */
    public void dispose() {
        fDataViewer.dispose();
    }

    /**
     * Create a new time graph control.
     *
     * @param parent
     *            The parent composite
     * @param colors
     *            The color scheme
     * @return The new TimeGraphControl
     */
    protected TimeGraphControl createTimeGraphControl(Composite parent,
            TimeGraphColorScheme colors) {
        return new TimeGraphControl(parent, colors);
    }

    /**
     * Create a new time graph marker axis.
     *
     * @param parent
     *            The parent composite object
     * @param colorScheme
     *            The color scheme to use
     * @param timeProvider
     *            The time data provider
     * @return The new TimeGraphMarkerAxis
     * @since 2.0
     */
    protected TimeGraphMarkerAxis createTimeGraphMarkerAxis(Composite parent,
            @NonNull TimeGraphColorScheme colorScheme, @NonNull ITimeDataProvider timeProvider) {
        return new TimeGraphMarkerAxis(parent, colorScheme, timeProvider);
    }

    /**
     * Resize the controls
     */
    public void resizeControls() {
        Rectangle r = fDataViewer.getClientArea();
        if (r.isEmpty()) {
            return;
        }

        if (fWeights != null) {
            setWeights(fWeights);
            fWeights = null;
        }
        int width = r.width;
        if (fNameWidth > width - fMinNameWidth) {
            fNameWidth = width - fMinNameWidth;
        }
        if (fNameWidth < fMinNameWidth) {
            fNameWidth = fMinNameWidth;
        }
        GridData gd = (GridData) fTree.getLayoutData();
        gd.widthHint = fNameWidth;
        if (fTree.getColumnCount() == 1) {
            fTree.getColumn(0).setWidth(fNameWidth);
        }
        adjustHorizontalScrollBar();
        adjustVerticalScrollBar();
    }

    /**
     * Recalculate the time bounds based on the time graph entries,
     * if the user-specified bound is set to SWT.DEFAULT.
     *
     * @param entries
     *            The root time graph entries in the model
     */
    public void setTimeRange(ITimeGraphEntry entries[]) {
        fTime0Bound = (fBeginTime != SWT.DEFAULT ? fBeginTime : fEndTime);
        fTime1Bound = (fEndTime != SWT.DEFAULT ? fEndTime : fBeginTime);
        if (fBeginTime != SWT.DEFAULT && fEndTime != SWT.DEFAULT) {
            return;
        }
        if (entries == null || entries.length == 0) {
            return;
        }
        if (fTime0Bound == SWT.DEFAULT) {
            fTime0Bound = Long.MAX_VALUE;
        }
        if (fTime1Bound == SWT.DEFAULT) {
            fTime1Bound = Long.MIN_VALUE;
        }
        for (ITimeGraphEntry entry : entries) {
            setTimeRange(entry);
        }
        if (fTime0Bound > fTime1Bound) {
            fTime0Bound = SWT.DEFAULT;
            fTime1Bound = SWT.DEFAULT;
        }
    }

    private void setTimeRange(ITimeGraphEntry entry) {
        if (fBeginTime == SWT.DEFAULT && entry.hasTimeEvents() && entry.getStartTime() != SWT.DEFAULT) {
            fTime0Bound = Math.min(entry.getStartTime(), fTime0Bound);
        }
        if (fEndTime == SWT.DEFAULT && entry.hasTimeEvents() && entry.getEndTime() != SWT.DEFAULT) {
            fTime1Bound = Math.max(entry.getEndTime(), fTime1Bound);
        }
        if (entry.hasChildren()) {
            for (ITimeGraphEntry child : entry.getChildren()) {
                setTimeRange(child);
            }
        }
    }

    /**
     * Set the time bounds to the provided values.
     *
     * @param beginTime
     *            The bounds begin time, or SWT.DEFAULT to use the input bounds
     * @param endTime
     *            The bounds end time, or SWT.DEFAULT to use the input bounds
     */
    public void setTimeBounds(long beginTime, long endTime) {
        fBeginTime = beginTime;
        fEndTime = endTime;
        fTime0Bound = (fBeginTime != SWT.DEFAULT ? fBeginTime : fEndTime);
        fTime1Bound = (fEndTime != SWT.DEFAULT ? fEndTime : fBeginTime);
        if (fTime0Bound > fTime1Bound) {
            // only possible if both are not default
            fBeginTime = endTime;
            fEndTime = beginTime;
            fTime0Bound = fBeginTime;
            fTime1Bound = fEndTime;
        }
        adjustHorizontalScrollBar();
    }

    /**
     * Recalculate the current time window when bounds have changed.
     */
    public void setTimeBounds() {
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
        fMarkerAxisCtrl.redraw();
        updateMarkerActions();
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
    @Override
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

    /**
     * Sets the relative horizontal weight of each part of the time graph
     * viewer. The first number is the name space weight, and the second number
     * is the time space weight.
     *
     * @param weights
     *            The array of relative weights of each part of the viewer
     * @since 2.3
     */
    public void setWeights(final int[] weights) {
        if (weights.length != 2) {
            return;
        }
        int width = fTimeAlignedComposite.getSize().x;
        if (width == 0) {
            /* the weigths will be applied when the control is resized */
            fWeights = Arrays.copyOf(weights, weights.length);
            return;
        }
        setNameSpace(width * weights[0] / (weights[0] + weights[1]));
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
        GridData gd = (GridData) fTree.getLayoutData();
        gd.widthHint = fNameWidth;
        if (fTree.getColumnCount() == 1) {
            fTree.getColumn(0).setWidth(fNameWidth);
        }
        fTimeAlignedComposite.layout();
        fTree.redraw();
        fTimeGraphCtrl.redraw();
        fTimeScaleCtrl.redraw();
        fMarkerAxisCtrl.redraw();
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

    @Override
    public long getSelectionBegin() {
        return fSelectionBegin;
    }

    @Override
    public long getSelectionEnd() {
        return fSelectionEnd;
    }

    @Override
    public void setStartFinishTimeNotify(long time0, long time1) {
        setStartFinishTimeInt(time0, time1);
        notifyRangeListeners();
    }

    @Override
    public void notifyStartFinishTime() {
        notifyRangeListeners();
    }

    @Override
    public void setStartFinishTime(long time0, long time1) {
        /* if there is a pending time range, ignore this one */
        if (fListenerNotifier != null && fListenerNotifier.hasTimeRangeUpdated()) {
            return;
        }
        setStartFinishTimeInt(time0, time1);
        updateExtSynchValues();
    }

    private void setStartFinishTimeInt(long time0, long time1) {
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
        adjustHorizontalScrollBar();
        fTimeGraphCtrl.redraw();
        fTimeScaleCtrl.redraw();
        fMarkerAxisCtrl.redraw();
    }

    @Override
    public void resetStartFinishTime() {
        setStartFinishTimeNotify(fTime0Bound, fTime1Bound);
        fTimeRangeFixed = false;
    }

    /**
     * @since 2.0
     */
    @Override
    public void resetStartFinishTime(boolean notify) {
        if (notify) {
            setStartFinishTimeNotify(fTime0Bound, fTime1Bound);
        } else {
            setStartFinishTime(fTime0Bound, fTime1Bound);
        }
        fTimeRangeFixed = false;
    }

    @Override
    public void setSelectedTimeNotify(long time, boolean ensureVisible) {
        setSelectedTimeInt(time, ensureVisible, true);
    }

    @Override
    public void setSelectedTime(long time, boolean ensureVisible) {
        /* if there is a pending time selection, ignore this one */
        if (fListenerNotifier != null && fListenerNotifier.hasTimeSelected()) {
            return;
        }
        setSelectedTimeInt(time, ensureVisible, false);
    }

    private void setSelectedTimeInt(long time, boolean ensureVisible, boolean doNotify) {
        setSelectionRangeInt(time, time, ensureVisible, doNotify);
    }

    /**
     * @since 1.2
     */
    @Override
    public void setSelectionRangeNotify(long beginTime, long endTime, boolean ensureVisible) {
        setSelectionRangeInt(beginTime, endTime, ensureVisible, true);
    }

    /**
     * @since 1.2
     */
    @Override
    public void setSelectionRange(long beginTime, long endTime, boolean ensureVisible) {
        /* if there is a pending time selection, ignore this one */
        if (fListenerNotifier != null && fListenerNotifier.hasTimeSelected()) {
            return;
        }
        setSelectionRangeInt(beginTime, endTime, ensureVisible, false);
    }

    private void setSelectionRangeInt(long beginTime, long endTime, boolean ensureVisible, boolean doNotify) {
        long time0 = fTime0;
        long time1 = fTime1;
        long selectionBegin = fSelectionBegin;
        long selectionEnd = fSelectionEnd;
        fSelectionBegin = Math.max(fTime0Bound, Math.min(fTime1Bound, beginTime));
        fSelectionEnd = Math.max(fTime0Bound, Math.min(fTime1Bound, endTime));
        boolean changed = (selectionBegin != fSelectionBegin || selectionEnd != fSelectionEnd);

        if (ensureVisible) {
            ensureVisible(selectionBegin != fSelectionBegin ? fSelectionBegin : fSelectionEnd);
        }

        fTimeGraphCtrl.redraw();
        fTimeScaleCtrl.redraw();
        fMarkerAxisCtrl.redraw();
        updateMarkerActions();

        if ((time0 != fTime0) || (time1 != fTime1)) {
            notifyRangeListeners();
        }

        if (doNotify && changed) {
            notifyTimeListeners();
        }
    }

    private void ensureVisible(long time) {
        long timeMid = (fTime1 - fTime0) / 2;
        if (time < fTime0) {
            long dt = fTime0 - time + timeMid;
            fTime0 -= dt;
            fTime1 -= dt;
        } else if (time > fTime1) {
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
        if (fTime1 - fTime0 < fMinTimeInterval) {
            fTime1 = Math.min(fTime1Bound, fTime0 + fMinTimeInterval);
        }
        adjustHorizontalScrollBar();
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        if (fSelectedEntry != getSelection()) {
            fSelectedEntry = getSelection();
            notifySelectionListeners();
        }
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (fSelectedEntry != getSelection()) {
            fSelectedEntry = getSelection();
            notifySelectionListeners();
        }
    }

    /**
     * Callback for when the next event is selected
     *
     * @param extend
     *            true to extend selection range, false for single selection
     * @since 1.0
     */
    public void selectNextEvent(boolean extend) {
        fTimeGraphCtrl.selectNextEvent(extend);
        adjustVerticalScrollBar();
    }

    /**
     * Callback for when the previous event is selected
     *
     * @param extend
     *            true to extend selection range, false for single selection
     * @since 1.0
     */
    public void selectPrevEvent(boolean extend) {
        fTimeGraphCtrl.selectPrevEvent(extend);
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

    private void notifySelectionListeners() {
        if (fListenerNotifier == null) {
            fListenerNotifier = new ListenerNotifier();
            fListenerNotifier.start();
        }
        fListenerNotifier.selectionChanged();
    }

    private void fireSelectionChanged(ITimeGraphEntry selection) {
        TimeGraphSelectionEvent event = new TimeGraphSelectionEvent(this, selection);

        for (ITimeGraphSelectionListener listener : fSelectionListeners) {
            listener.selectionChanged(event);
        }

        ISelection structuredSelection = (selection == null) ? StructuredSelection.EMPTY : new StructuredSelection(selection);
        fireSelectionChanged(new SelectionChangedEvent(this, structuredSelection));
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

    private void notifyTimeListeners() {
        if (fListenerNotifier == null) {
            fListenerNotifier = new ListenerNotifier();
            fListenerNotifier.start();
        }
        fListenerNotifier.timeSelected();
    }

    private void fireTimeSelected(long startTime, long endTime) {
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

    private void notifyRangeListeners() {
        if (fListenerNotifier == null) {
            fListenerNotifier = new ListenerNotifier();
            fListenerNotifier.start();
        }
        fListenerNotifier.timeRangeUpdated();
    }

    private void fireTimeRangeUpdated(long startTime, long endTime) {
        // Check if the time has actually changed from last notification
        if (startTime != fTime0ExtSynch || endTime != fTime1ExtSynch) {
            // Notify Time Scale Selection Listeners
            TimeGraphRangeUpdateEvent event = new TimeGraphRangeUpdateEvent(this, startTime, endTime);

            for (ITimeGraphRangeListener listener : fRangeListeners) {
                listener.timeRangeUpdated(event);
            }

            // update external synch values
            updateExtSynchValues();
        }
    }

    /**
     * Add a bookmark listener
     *
     * @param listener
     *            The listener to add
     * @since 2.0
     */
    public void addBookmarkListener(ITimeGraphBookmarkListener listener) {
        fBookmarkListeners.add(listener);
    }

    /**
     * Remove a bookmark listener
     *
     * @param listener
     *            The listener to remove
     * @since 2.0
     */
    public void removeBookmarkListener(ITimeGraphBookmarkListener listener) {
        fBookmarkListeners.remove(listener);
    }

    private void fireBookmarkAdded(IMarkerEvent bookmark) {
        TimeGraphBookmarkEvent event = new TimeGraphBookmarkEvent(this, bookmark);

        for (ITimeGraphBookmarkListener listener : fBookmarkListeners) {
            listener.bookmarkAdded(event);
        }
    }

    private void fireBookmarkRemoved(IMarkerEvent bookmark) {
        TimeGraphBookmarkEvent event = new TimeGraphBookmarkEvent(this, bookmark);

        for (ITimeGraphBookmarkListener listener : fBookmarkListeners) {
            listener.bookmarkRemoved(event);
        }
    }

    /**
     * Set the bookmarks list.
     *
     * @param bookmarks
     *            The bookmarks list, or null
     * @since 2.0
     */
    public void setBookmarks(List<IMarkerEvent> bookmarks) {
        fBookmarks.clear();
        if (bookmarks != null) {
            fBookmarks.addAll(bookmarks);
        }
        updateMarkerList();
        updateMarkerActions();
    }

    /**
     * Get the bookmarks list.
     *
     * @return The bookmarks list
     * @since 2.0
     */
    public List<IMarkerEvent> getBookmarks() {
        return Collections.unmodifiableList(fBookmarks);
    }

    /**
     * Set the list of marker categories.
     *
     * @param categories
     *            The list of marker categories, or null
     * @since 2.0
     */
    public void setMarkerCategories(List<String> categories) {
        fMarkerCategories.clear();
        if (categories != null) {
            fMarkerCategories.addAll(categories);
        }
        fMarkerCategories.add(IMarkerEvent.BOOKMARKS);
        fMarkerAxisCtrl.setMarkerCategories(fMarkerCategories);
    }

    /**
     * @since 2.0
     */
    @Override
    public void setMarkerCategoryVisible(String category, boolean visible) {
        boolean changed = false;
        if (visible) {
            changed = fHiddenMarkerCategories.remove(category);
        } else {
            changed = fHiddenMarkerCategories.add(category);
        }
        if (changed) {
            updateMarkerList();
            updateMarkerActions();
            getControl().redraw();
        }
    }

    /**
     * Set the markers list.
     *
     * @param markers
     *            The markers list, or null
     * @since 2.0
     */
    public void setMarkers(List<IMarkerEvent> markers) {
        fMarkers.clear();
        if (markers != null) {
            fMarkers.addAll(markers);
        }
        updateMarkerList();
        updateMarkerActions();
    }

    /**
     * Get the markers list.
     *
     * @return The markers list, or null
     * @since 2.0
     */
    public List<IMarkerEvent> getMarkers() {
        return Collections.unmodifiableList(fMarkers);
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

    @Override
    public void setSelection(ISelection selection, boolean reveal) {
        /* if there is a pending selection, ignore this one */
        if (fListenerNotifier != null && fListenerNotifier.hasSelectionChanged()) {
            return;
        }
        Object element = selection;
        if (selection instanceof IStructuredSelection) {
            element = ((IStructuredSelection) selection).getFirstElement();
        }
        if (!(element instanceof ITimeGraphEntry)) {
            return;
        }
        ITimeGraphEntry entry = (ITimeGraphEntry) element;
        fSelectedEntry = entry;
        fTimeGraphCtrl.selectItem(entry, false, reveal);
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

        setStartFinishTimeInt(time0, time1);

        // update notification time values since we are now in synch with the
        // external application
        updateExtSynchValues();
    }

    /**
     * update the cache values used to identify the need to send a time window
     * update to external registered listeners
     */
    private void updateExtSynchValues() {
        // last time notification cache
        fTime0ExtSynch = fTime0;
        fTime1ExtSynch = fTime1;
    }

    @Override
    public TimeFormat getTimeFormat() {
        return fTimeFormat;
    }

    /**
     * @param tf
     *            the {@link TimeFormat} used to display timestamps
     */
    public void setTimeFormat(TimeFormat tf) {
        this.fTimeFormat = tf;
        if (tf == TimeFormat.CYCLES) {
            fTimeDataProvider = new TimeDataProviderCyclesConverter(this, fClockFrequency);
        } else {
            fTimeDataProvider = this;
        }
        fTimeScaleCtrl.setTimeProvider(fTimeDataProvider);
        if (fToolTipHandler != null) {
            fToolTipHandler.setTimeProvider(fTimeDataProvider);
        }
    }

    /**
     * Sets the clock frequency. Used when the time format is set to CYCLES.
     *
     * @param clockFrequency
     *            the clock frequency in Hz
     */
    public void setClockFrequency(long clockFrequency) {
        fClockFrequency = clockFrequency;
        if (fTimeFormat == TimeFormat.CYCLES) {
            fTimeDataProvider = new TimeDataProviderCyclesConverter(this, fClockFrequency);
            fTimeScaleCtrl.setTimeProvider(fTimeDataProvider);
            if (fToolTipHandler != null) {
                fToolTipHandler.setTimeProvider(fTimeDataProvider);
            }
        }
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

    @Override
    public Control getControl() {
        return fDataViewer;
    }

    /**
     * Returns the time graph control associated with this viewer.
     *
     * @return the time graph control
     */
    public TimeGraphControl getTimeGraphControl() {
        return fTimeGraphCtrl;
    }

    /**
     * Returns the tree control associated with this viewer. The tree is only
     * used for column handling of the name space and contains no tree items.
     *
     * @return the tree control
     * @since 2.3
     */
    public Tree getTree() {
        return fTree;
    }

    /**
     * Sets the columns for this time graph viewer's name space.
     *
     * @param columnNames
     *            the column names
     * @since 2.3
     */
    public void setColumns(String[] columnNames) {
        fTimeGraphCtrl.setColumns(columnNames);
    }

    /**
     * Returns the time graph scale associated with this viewer.
     *
     * @return the time graph scale
     */
    public TimeGraphScale getTimeGraphScale() {
        return fTimeScaleCtrl;
    }

    /**
     * Returns the composite containing all the controls that are time aligned,
     * i.e. TimeGraphScale, TimeGraphControl.
     *
     * @return the time based composite
     * @since 1.0
     */
    public Composite getTimeAlignedComposite() {
        return fTimeAlignedComposite;
    }

    /**
     * Return the x coordinate corresponding to a time
     *
     * @param time
     *            the time
     * @return the x coordinate corresponding to the time
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
    public Slider getHorizontalBar() {
        return fHorizontalScrollBar;
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
     * Sets the auto-expand level to be used for new entries discovered when
     * calling {@link #setInput(Object)} or {@link #refresh()}. The value 0
     * means that there is no auto-expand; 1 means that top-level entries are
     * expanded, but not their children; 2 means that top-level entries are
     * expanded, and their children, but not grand-children; and so on.
     * <p>
     * The value {@link #ALL_LEVELS} means that all subtrees should be expanded.
     * </p>
     *
     * @param level
     *            non-negative level, or <code>ALL_LEVELS</code> to expand all
     *            levels of the tree
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
     */
    public int getAutoExpandLevel() {
        return fTimeGraphCtrl.getAutoExpandLevel();
    }

    /**
     * Get the expanded state of an entry.
     *
     * @param entry
     *            The entry
     * @return true if the entry is expanded, false if collapsed
     * @since 1.1
     */
    public boolean getExpandedState(ITimeGraphEntry entry) {
        return fTimeGraphCtrl.getExpandedState(entry);
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
     * Set the expanded state of a given list of entries
     *
     * @param entries
     *            The list of entries
     * @param expanded
     *            True if expanded, false if collapsed
     * @since 3.1
     */
    public void setExpandedState(Iterable<ITimeGraphEntry> entries, boolean expanded) {
        fTimeGraphCtrl.setExpandedState(entries, expanded);
        adjustVerticalScrollBar();
    }

    /**
     * Collapses all nodes of the viewer's tree, starting with the root.
     */
    public void collapseAll() {
        fTimeGraphCtrl.collapseAll();
        adjustVerticalScrollBar();
    }

    /**
     * Expands all entries of the viewer's tree, starting with the root.
     */
    public void expandAll() {
        fTimeGraphCtrl.expandAll();
        adjustVerticalScrollBar();
    }

    /**
     * Select an entry and reveal it
     *
     * @param entry
     *            The entry to select
     * @since 2.0
     */
    public void selectAndReveal(@NonNull ITimeGraphEntry entry) {
        final ITimeGraphEntry parent = entry.getParent();
        if (parent != null) {
            fTimeGraphCtrl.setExpandedState(parent, true);
        }
        fSelectedEntry = entry;
        fTimeGraphCtrl.selectItem(entry, false);
        adjustVerticalScrollBar();
    }

    /**
     * Get the number of expanded (visible) time graph entries. This includes
     * leafs and does not include filtered-out entries.
     *
     * @return The number of expanded (visible) time graph entries
     */
    public int getExpandedElementCount() {
        return fTimeGraphCtrl.getExpandedElementCount();
    }

    /**
     * Get the expanded (visible) time graph entries. This includes leafs and
     * does not include filtered-out entries.
     *
     * @return The array of expanded (visible) time graph entries
     */
    public ITimeGraphEntry[] getExpandedElements() {
        return fTimeGraphCtrl.getExpandedElements();
    }

    /**
     * Get the collapsed (visible or not) time graph entries.
     *
     * @return The array of collapsed time graph entries
     * @since 3.1
     */
    public @NonNull Set<@NonNull ITimeGraphEntry> getAllCollapsedElements() {
        @NonNull Set<@NonNull ITimeGraphEntry> collapsedEntries = new HashSet<>();
        ITimeGraphEntry[] elements = fTimeGraphContentProvider.getElements(getInput());
        for (ITimeGraphEntry entry : elements) {
            if (entry != null) {
                getAllCollapsedElements(entry, collapsedEntries);
            }
        }
        return collapsedEntries;
    }

    /**
     * Get all collapsed entries. This method is called recursively to add collapse
     * entries to the given set
     *
     * @param entry
     *            The current entry
     * @param collapsedEntries
     *            The set of collapsed entries
     */
    private void getAllCollapsedElements(@NonNull ITimeGraphEntry entry, @NonNull Set<@NonNull ITimeGraphEntry> collapsedEntries) {
        if (entry.hasChildren() && !fTimeGraphCtrl.getExpandedState(entry)) {
            collapsedEntries.add(entry);
        }
        for (ITimeGraphEntry child : entry.getChildren()) {
            getAllCollapsedElements(child, collapsedEntries);
        }
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
     * Add a viewer filter listener
     *
     * @param listener
     *            The listener to add
     * @since 3.2
     */
    public void addViewerFilterListener(ITimeGraphViewerFilterListener listener) {
        fTimeGraphCtrl.addViewerFilterListener(listener);
    }

    /**
     * Remove a viewer filter listener
     *
     * @param listener
     *            The listener to remove
     * @since 3.2
     */
    public void removeViewerFilterListener(ITimeGraphViewerFilterListener listener) {
        fTimeGraphCtrl.removeViewerFilterListener(listener);
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
                public void runWithEvent(Event event) {
                    boolean extend = (event.stateMask & SWT.SHIFT) != 0;
                    selectNextEvent(extend);
                }
            };

            fNextEventAction.setText(Messages.TmfTimeGraphViewer_NextStateChangeActionNameText);
            fNextEventAction.setToolTipText(Messages.TmfTimeGraphViewer_NextStateChangeActionToolTipText);
            fNextEventAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_NEXT_STATE_CHANGE));
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
                public void runWithEvent(Event event) {
                    boolean extend = (event.stateMask & SWT.SHIFT) != 0;
                    selectPrevEvent(extend);
                }
            };

            fPrevEventAction.setText(Messages.TmfTimeGraphViewer_PreviousStateChangeActionNameText);
            fPrevEventAction.setToolTipText(Messages.TmfTimeGraphViewer_PreviousStateChangeActionToolTipText);
            fPrevEventAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_PREV_STATE_CHANGE));
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
     */
    public Action getFollowArrowFwdAction() {
        if (fFollowArrowFwdAction == null) {
            fFollowArrowFwdAction = new Action() {
                @Override
                public void runWithEvent(Event event) {
                    boolean extend = (event.stateMask & SWT.SHIFT) != 0;
                    fTimeGraphCtrl.followArrowFwd(extend);
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
     */
    public Action getFollowArrowBwdAction() {
        if (fFollowArrowBwdAction == null) {
            fFollowArrowBwdAction = new Action() {
                @Override
                public void runWithEvent(Event event) {
                    boolean extend = (event.stateMask & SWT.SHIFT) != 0;
                    fTimeGraphCtrl.followArrowBwd(extend);
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

    /**
     * Get the show filter dialog action.
     *
     * @return The Action object
     * @since 1.2
     */
    public ShowFilterDialogAction getShowFilterDialogAction() {
        if (fShowFilterDialogAction == null) {
            fShowFilterDialogAction = new ShowFilterDialogAction(this);
        }
        return fShowFilterDialogAction;
    }

    /**
     * Get the toggle bookmark action.
     *
     * @return The Action object
     * @since 2.0
     */
    public Action getToggleBookmarkAction() {
        if (fToggleBookmarkAction == null) {
            fToggleBookmarkAction = new Action() {
                @Override
                public void runWithEvent(Event event) {
                    IMarkerEvent selectedBookmark = getBookmarkAtSelection();
                    if (selectedBookmark == null) {
                        final long time = Math.min(fSelectionBegin, fSelectionEnd);
                        final long duration = Math.max(fSelectionBegin, fSelectionEnd) - time;
                        final AddBookmarkDialog dialog = new AddBookmarkDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), null);
                        if (dialog.open() == Window.OK) {
                            final String label = dialog.getValue();
                            final RGBA rgba = dialog.getColorValue();
                            IMarkerEvent bookmark = new MarkerEvent(null, time, duration, IMarkerEvent.BOOKMARKS, rgba, label, true);
                            fBookmarks.add(bookmark);
                            updateMarkerList();
                            updateMarkerActions();
                            getControl().redraw();
                            fireBookmarkAdded(bookmark);
                        }
                    } else {
                        fBookmarks.remove(selectedBookmark);
                        updateMarkerList();
                        updateMarkerActions();
                        getControl().redraw();
                        fireBookmarkRemoved(selectedBookmark);
                    }
                }
            };
            fToggleBookmarkAction.setText(Messages.TmfTimeGraphViewer_BookmarkActionAddText);
            fToggleBookmarkAction.setToolTipText(Messages.TmfTimeGraphViewer_BookmarkActionAddText);
            fToggleBookmarkAction.setImageDescriptor(ADD_BOOKMARK);
        }
        return fToggleBookmarkAction;
    }

    /**
     * Get the next marker action.
     *
     * @return The Action object
     * @since 2.0
     */
    public Action getNextMarkerAction() {
        if (fNextMarkerAction == null) {
            fNextMarkerAction = new Action(Messages.TmfTimeGraphViewer_NextMarkerActionText, IAction.AS_DROP_DOWN_MENU) {
                @Override
                public void runWithEvent(Event event) {
                    boolean extend = (event.stateMask & SWT.SHIFT) != 0;
                    if (extend) {
                        extendToNextMarker();
                    } else {
                        selectNextMarker();
                    }
                }
            };
            fNextMarkerAction.setToolTipText(Messages.TmfTimeGraphViewer_NextMarkerActionText);
            fNextMarkerAction.setImageDescriptor(NEXT_BOOKMARK);
            fNextMarkerAction.setMenuCreator(new IMenuCreator () {
                Menu menu = null;
                @Override
                public void dispose() {
                    if (menu != null) {
                        menu.dispose();
                        menu = null;
                    }
                }

                @Override
                public Menu getMenu(Control parent) {
                    if (menu != null) {
                        menu.dispose();
                    }
                    menu = new Menu(parent);
                    for (String category : fMarkerCategories) {
                        final Action action = new Action(category, IAction.AS_CHECK_BOX) {
                            @Override
                            public void runWithEvent(Event event) {
                                if (isChecked()) {
                                    fSkippedMarkerCategories.remove(getText());
                                } else {
                                    fSkippedMarkerCategories.add(getText());
                                }
                                updateMarkerActions();
                            }
                        };
                        action.setEnabled(!fHiddenMarkerCategories.contains(category));
                        action.setChecked(action.isEnabled() && !fSkippedMarkerCategories.contains(category));
                        new ActionContributionItem(action).fill(menu, -1);
                    }
                    return menu;
                }

                @Override
                public Menu getMenu(Menu parent) {
                    return null;
                }
            });
        }
        return fNextMarkerAction;
    }

    /**
     * Get the previous marker action.
     *
     * @return The Action object
     * @since 2.0
     */
    public Action getPreviousMarkerAction() {
        if (fPreviousMarkerAction == null) {
            fPreviousMarkerAction = new Action() {
                @Override
                public void runWithEvent(Event event) {
                    boolean extend = (event.stateMask & SWT.SHIFT) != 0;
                    if (extend) {
                        extendToPrevMarker();
                    } else {
                        selectPrevMarker();
                    }
                }
            };
            fPreviousMarkerAction.setText(Messages.TmfTimeGraphViewer_PreviousMarkerActionText);
            fPreviousMarkerAction.setToolTipText(Messages.TmfTimeGraphViewer_PreviousMarkerActionText);
            fPreviousMarkerAction.setImageDescriptor(PREVIOUS_BOOKMARK);
        }
        return fPreviousMarkerAction;
    }

    /**
     * Get the show markers menu.
     *
     * @return The menu manager object
     * @since 2.0
     */
    public MenuManager getMarkersMenu() {
        if (fMarkersMenu == null) {
            fMarkersMenu = new MenuManager(Messages.TmfTimeGraphViewer_ShowMarkersMenuText);
            fMarkersMenu.setRemoveAllWhenShown(true);
            fMarkersMenu.addMenuListener(new IMenuListener() {
                @Override
                public void menuAboutToShow(IMenuManager manager) {
                    for (String category : fMarkerCategories) {
                        final Action action = new Action(category, IAction.AS_CHECK_BOX) {
                            @Override
                            public void runWithEvent(Event event) {
                                setMarkerCategoryVisible(getText(), isChecked());
                            }
                        };
                        action.setChecked(!fHiddenMarkerCategories.contains(category));
                        manager.add(action);
                    }
                }
            });
        }
        return fMarkersMenu;
    }

    /**
     * Select the next marker that begins at or after the current selection
     * begin time. Markers that begin at the same time are ordered by end time.
     */
    private void selectNextMarker() {
        List<IMarkerEvent> markers = getTimeGraphControl().getMarkers();
        if (markers == null) {
            return;
        }
        for (IMarkerEvent marker : markers) {
            final long time = Math.min(fSelectionBegin, fSelectionEnd);
            final long duration = Math.max(fSelectionBegin, fSelectionEnd) - time;
            if ((marker.getTime() > time ||
                    (marker.getTime() == time && marker.getDuration() > duration))
                    && !fSkippedMarkerCategories.contains(marker.getCategory())) {
                setSelectionRangeNotify(marker.getTime(), marker.getTime() + marker.getDuration(), false);
                ensureVisible(marker.getTime());
                notifyRangeListeners();
                fTimeGraphCtrl.updateStatusLine();
                return;
            }
        }
    }

    /**
     * Select the previous marker that begins at or before the current selection
     * begin time. Markers that begin at the same time are ordered by end time.
     */
    private void selectPrevMarker() {
        List<IMarkerEvent> markers = getTimeGraphControl().getMarkers();
        if (markers == null) {
            return;
        }
        final long time = Math.min(fSelectionBegin, fSelectionEnd);
        final long duration = Math.max(fSelectionBegin, fSelectionEnd) - time;
        for (int i = markers.size() - 1; i >= 0; i--) {
            IMarkerEvent marker = markers.get(i);
            if ((marker.getTime() < time ||
                    (marker.getTime() == time && marker.getDuration() < duration))
                    && !fSkippedMarkerCategories.contains(marker.getCategory())) {
                setSelectionRangeNotify(marker.getTime(), marker.getTime() + marker.getDuration(), false);
                ensureVisible(marker.getTime());
                notifyRangeListeners();
                fTimeGraphCtrl.updateStatusLine();
                return;
            }
        }
    }

    /**
     * Extend the selection to the closest next marker end time.
     */
    private void extendToNextMarker() {
        List<IMarkerEvent> markers = getTimeGraphControl().getMarkers();
        if (markers == null) {
            return;
        }
        IMarkerEvent nextMarker = null;
        for (IMarkerEvent marker : markers) {
            if (marker.getTime() + marker.getDuration() > fSelectionEnd
                    && !fSkippedMarkerCategories.contains(marker.getCategory())
                    && (nextMarker == null || marker.getTime() + marker.getDuration() < nextMarker.getTime() + nextMarker.getDuration())) {
                nextMarker = marker;
            }
        }
        if (nextMarker != null) {
            setSelectionRangeNotify(fSelectionBegin, nextMarker.getTime() + nextMarker.getDuration(), true);
            fTimeGraphCtrl.updateStatusLine();
        }
    }

    /**
     * Extend the selection to the closest previous marker start time.
     */
    private void extendToPrevMarker() {
        List<IMarkerEvent> markers = getTimeGraphControl().getMarkers();
        if (markers == null) {
            return;
        }
        for (int i = markers.size() - 1; i >= 0; i--) {
            IMarkerEvent marker = markers.get(i);
            if (marker.getTime() < fSelectionEnd
                    && !fSkippedMarkerCategories.contains(marker.getCategory())) {
                setSelectionRangeNotify(fSelectionBegin, marker.getTime(), true);
                fTimeGraphCtrl.updateStatusLine();
                return;
            }
        }
    }

    private IMarkerEvent getBookmarkAtSelection() {
        final long time = Math.min(fSelectionBegin, fSelectionEnd);
        final long duration = Math.max(fSelectionBegin, fSelectionEnd) - time;
        for (IMarkerEvent bookmark : fBookmarks) {
            if (bookmark.getTime() == time && bookmark.getDuration() == duration) {
                return bookmark;
            }
        }
        return null;
    }

    private void updateMarkerActions() {
        boolean enabled = fTime0Bound != SWT.DEFAULT || fTime1Bound != SWT.DEFAULT;
        if (fToggleBookmarkAction != null) {
            if (getBookmarkAtSelection() != null) {
                fToggleBookmarkAction.setText(Messages.TmfTimeGraphViewer_BookmarkActionRemoveText);
                fToggleBookmarkAction.setToolTipText(Messages.TmfTimeGraphViewer_BookmarkActionRemoveText);
                fToggleBookmarkAction.setImageDescriptor(REMOVE_BOOKMARK);
            } else {
                fToggleBookmarkAction.setText(Messages.TmfTimeGraphViewer_BookmarkActionAddText);
                fToggleBookmarkAction.setToolTipText(Messages.TmfTimeGraphViewer_BookmarkActionAddText);
                fToggleBookmarkAction.setImageDescriptor(ADD_BOOKMARK);
            }
            fToggleBookmarkAction.setEnabled(enabled);
        }
        List<IMarkerEvent> markers = getTimeGraphControl().getMarkers();
        if (markers == null) {
            markers = Collections.emptyList();
        }
        if (fPreviousMarkerAction != null) {
            fPreviousMarkerAction.setEnabled(enabled && !markers.isEmpty());
        }
        if (fNextMarkerAction != null) {
            fNextMarkerAction.setEnabled(enabled && !markers.isEmpty());
        }
    }

    private void updateMarkerList() {
        List<IMarkerEvent> markers = new ArrayList<>();
        for (IMarkerEvent marker : fMarkers) {
            if (!fHiddenMarkerCategories.contains(marker.getCategory())) {
                markers.add(marker);
            }
        }
        if (!fHiddenMarkerCategories.contains(IMarkerEvent.BOOKMARKS)) {
            markers.addAll(fBookmarks);
        }
        Collections.sort(markers, new MarkerComparator());
        fTimeGraphCtrl.setMarkers(markers);
        fMarkerAxisCtrl.setMarkers(markers);
    }

    private void adjustHorizontalScrollBar() {
        long time0 = getTime0();
        long time1 = getTime1();
        long timeMin = getMinTime();
        long timeMax = getMaxTime();
        long delta = timeMax - timeMin;
        int timePos = 0;
        int thumb = H_SCROLLBAR_MAX;
        if (delta != 0) {
            // Thumb size (page size)
            thumb = Math.max(1, (int) (H_SCROLLBAR_MAX * ((double) (time1 - time0) / delta)));
            // At the beginning of visible window
            timePos = (int) (H_SCROLLBAR_MAX * ((double) (time0 - timeMin) / delta));
        }
        fHorizontalScrollBar.setValues(timePos, 0, H_SCROLLBAR_MAX, thumb, Math.max(1, thumb / 4), Math.max(2, thumb));
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
     * @see org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl#addTimeGraphEntryMenuListener(org.eclipse.swt.events.MenuDetectListener)
     */
    public void addTimeGraphEntryMenuListener(MenuDetectListener listener) {
        fTimeGraphCtrl.addTimeGraphEntryMenuListener(listener);
    }

    /**
     * @param listener
     *            a {@link MenuDetectListener}
     * @see org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl#removeTimeGraphEntryMenuListener(org.eclipse.swt.events.MenuDetectListener)
     */
    public void removeTimeGraphEntryMenuListener(MenuDetectListener listener) {
        fTimeGraphCtrl.removeTimeGraphEntryMenuListener(listener);
    }

    /**
     * @param listener
     *            a {@link MenuDetectListener}
     * @see org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl#addTimeEventMenuListener(org.eclipse.swt.events.MenuDetectListener)
     */
    public void addTimeEventMenuListener(MenuDetectListener listener) {
        fTimeGraphCtrl.addTimeEventMenuListener(listener);
    }

    /**
     * @param listener
     *            a {@link MenuDetectListener}
     * @see org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl#removeTimeEventMenuListener(org.eclipse.swt.events.MenuDetectListener)
     */
    public void removeTimeEventMenuListener(MenuDetectListener listener) {
        fTimeGraphCtrl.removeTimeEventMenuListener(listener);
    }

    /**
     * Add a new viewer filter object
     *
     * @param filter
     *            The filter object to be attached to the view
     */
    public void addFilter(@NonNull ViewerFilter filter) {
        fTimeGraphCtrl.addFilter(filter);
        refresh();
    }

    /**
     * Change the viewer filter object
     *
     * @param filter
     *            The filter object to be attached to the view
     * @since 3.2
     */
    public void changeFilter(@NonNull ViewerFilter filter) {
        fTimeGraphCtrl.changeFilter(filter);
        refresh();
    }

    /**
     * Remove the viewer filter object
     *
     * @param filter
     *            The filter object to be attached to the view
     */
    public void removeFilter(@NonNull ViewerFilter filter) {
        fTimeGraphCtrl.removeFilter(filter);
        refresh();
    }

    /**
     * Returns this viewer's filters.
     *
     * @return an array of viewer filters
     * @since 1.2
     */
    public @NonNull ViewerFilter[] getFilters() {
        return fTimeGraphCtrl.getFilters();
    }

    /**
     * Sets the filters, replacing any previous filters, and triggers
     * refiltering of the elements.
     *
     * @param filters
     *            an array of viewer filters, or null
     * @since 1.2
     */
    public void setFilters(@NonNull ViewerFilter[] filters) {
        fTimeGraphCtrl.setFilters(filters);
        refresh();
    }

    /**
     * Return the time alignment information
     *
     * @return the time alignment information
     *
     * @see ITmfTimeAligned
     *
     * @since 1.0
     */
    public TmfTimeViewAlignmentInfo getTimeViewAlignmentInfo() {
        return fTimeGraphCtrl.getTimeViewAlignmentInfo();
    }

    /**
     * Return the available width for the time-axis.
     *
     * @see ITmfTimeAligned
     *
     * @param requestedOffset
     *            the requested offset
     * @return the available width for the time-axis
     *
     * @since 1.0
     */
    public int getAvailableWidth(int requestedOffset) {
        int totalWidth = fTimeAlignedComposite.getSize().x;
        return Math.min(totalWidth, Math.max(0, totalWidth - requestedOffset));
    }

    /**
     * Perform the alignment operation.
     *
     * @param offset
     *            the alignment offset
     * @param width
     *            the alignment width
     *
     * @see ITmfTimeAligned
     *
     * @since 1.0
     */
    public void performAlign(int offset, int width) {
        fTimeGraphCtrl.performAlign(offset);
        int alignmentWidth = width;
        int size = fTimeAlignedComposite.getSize().x;
        GridLayout layout = (GridLayout) fTimeAlignedComposite.getLayout();
        int marginSize = size - alignmentWidth - offset;
        layout.marginRight = Math.max(0, marginSize);
        fTimeAlignedComposite.layout();
    }
}
