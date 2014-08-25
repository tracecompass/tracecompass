/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation, replaced Table by TmfVirtualTable
 *   Patrick Tasse - Factored out from events view,
 *                   Filter implementation (inspired by www.eclipse.org/mat)
 *   Ansgar Radermacher - Support navigation to model URIs (Bug 396956)
 *   Bernd Hufmann - Updated call site and model URI implementation
 *   Alexandre Montplaisir - Update to new column API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.events;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.tmf.core.filter.TmfCollapseFilter;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.internal.tmf.ui.commands.ExportToTextCommandHandler;
import org.eclipse.linuxtools.internal.tmf.ui.dialogs.MultiLineInputDialog;
import org.eclipse.linuxtools.tmf.core.component.ITmfEventProvider;
import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.collapse.ITmfCollapsibleEvent;
import org.eclipse.linuxtools.tmf.core.event.lookup.ITmfCallsite;
import org.eclipse.linuxtools.tmf.core.event.lookup.ITmfModelLookup;
import org.eclipse.linuxtools.tmf.core.event.lookup.ITmfSourceLookup;
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;
import org.eclipse.linuxtools.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterAndNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterMatchesNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfEventFilterAppliedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfEventSearchAppliedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsCache.CachedEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.events.columns.TmfEventTableColumn;
import org.eclipse.linuxtools.tmf.ui.viewers.events.columns.TmfEventTableFieldColumn;
import org.eclipse.linuxtools.tmf.ui.views.colors.ColorSetting;
import org.eclipse.linuxtools.tmf.ui.views.colors.ColorSettingsManager;
import org.eclipse.linuxtools.tmf.ui.views.colors.IColorSettingsListener;
import org.eclipse.linuxtools.tmf.ui.views.filter.FilterManager;
import org.eclipse.linuxtools.tmf.ui.widgets.rawviewer.TmfRawEventViewer;
import org.eclipse.linuxtools.tmf.ui.widgets.virtualtable.TmfVirtualTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.themes.ColorUtil;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

/**
 * The generic TMF Events table
 *
 * This is a view that will list events that are read from a trace.
 *
 * @author Francois Chouinard
 * @author Patrick Tasse
 * @since 2.0
 */
public class TmfEventsTable extends TmfComponent implements IGotoMarker, IColorSettingsListener, ISelectionProvider {

    /**
     * Empty string array, used by {@link #getItemStrings}.
     * @since 3.0
     */
    protected static final @NonNull String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Empty string
     * @since 3.1
     */
    protected static final @NonNull String EMPTY_STRING = ""; //$NON-NLS-1$

    private static final boolean IS_LINUX = System.getProperty("os.name").contains("Linux") ? true : false; //$NON-NLS-1$ //$NON-NLS-2$

    private static final Image BOOKMARK_IMAGE = Activator.getDefault().getImageFromPath(
            "icons/elcl16/bookmark_obj.gif"); //$NON-NLS-1$
    private static final Image SEARCH_IMAGE = Activator.getDefault().getImageFromPath("icons/elcl16/search.gif"); //$NON-NLS-1$
    private static final Image SEARCH_MATCH_IMAGE = Activator.getDefault().getImageFromPath(
            "icons/elcl16/search_match.gif"); //$NON-NLS-1$
    private static final Image SEARCH_MATCH_BOOKMARK_IMAGE = Activator.getDefault().getImageFromPath(
            "icons/elcl16/search_match_bookmark.gif"); //$NON-NLS-1$
    private static final Image FILTER_IMAGE = Activator.getDefault()
            .getImageFromPath("icons/elcl16/filter_items.gif"); //$NON-NLS-1$
    private static final Image STOP_IMAGE = Activator.getDefault().getImageFromPath("icons/elcl16/stop.gif"); //$NON-NLS-1$
    private static final String SEARCH_HINT = Messages.TmfEventsTable_SearchHint;
    private static final String FILTER_HINT = Messages.TmfEventsTable_FilterHint;
    private static final int MAX_CACHE_SIZE = 1000;

    private static final int MARGIN_COLUMN_INDEX = 0;
    private static final int FILTER_SUMMARY_INDEX = 1;
    private static final int EVENT_COLUMNS_START_INDEX = MARGIN_COLUMN_INDEX + 1;

    /**
     * Default set of columns to use for trace types that do not specify
     * anything
     * @since 3.1
     */
    public static final Collection<TmfEventTableColumn> DEFAULT_COLUMNS = ImmutableList.of(
            TmfEventTableColumn.BaseColumns.TIMESTAMP,
            TmfEventTableColumn.BaseColumns.SOURCE,
            TmfEventTableColumn.BaseColumns.EVENT_TYPE,
            TmfEventTableColumn.BaseColumns.REFERENCE,
            TmfEventTableColumn.BaseColumns.CONTENTS
            );

    /**
     * The events table search/filter keys
     *
     * @version 1.0
     * @author Patrick Tasse
     */
    public interface Key {
        /** Search text */
        String SEARCH_TXT = "$srch_txt"; //$NON-NLS-1$

        /** Search object */
        String SEARCH_OBJ = "$srch_obj"; //$NON-NLS-1$

        /** Filter text */
        String FILTER_TXT = "$fltr_txt"; //$NON-NLS-1$

        /** Filter object */
        String FILTER_OBJ = "$fltr_obj"; //$NON-NLS-1$

        /** Timestamp */
        String TIMESTAMP = "$time"; //$NON-NLS-1$

        /** Rank */
        String RANK = "$rank"; //$NON-NLS-1$

        /** Field ID */
        String FIELD_ID = "$field_id"; //$NON-NLS-1$

        /** Bookmark indicator */
        String BOOKMARK = "$bookmark"; //$NON-NLS-1$
    }

    /**
     * The events table search/filter state
     *
     * @version 1.0
     * @author Patrick Tasse
     */
    public static enum HeaderState {
        /** A search is being run */
        SEARCH,

        /** A filter is applied */
        FILTER
    }

    interface Direction {
        int FORWARD = +1;
        int BACKWARD = -1;
    }

    // ------------------------------------------------------------------------
    // Table data
    // ------------------------------------------------------------------------

    /** The virtual event table */
    protected TmfVirtualTable fTable;

    private Composite fComposite;
    private SashForm fSashForm;
    private TmfRawEventViewer fRawViewer;
    private ITmfTrace fTrace;
    volatile private boolean fPackDone = false;
    private HeaderState fHeaderState = HeaderState.SEARCH;
    private long fSelectedRank = 0;
    private ITmfTimestamp fSelectedBeginTimestamp = null;
    private IStatusLineManager fStatusLineManager = null;

    // Filter data
    private long fFilterMatchCount;
    private long fFilterCheckCount;
    private FilterThread fFilterThread;
    private boolean fFilterThreadResume = false;
    private final Object fFilterSyncObj = new Object();
    private SearchThread fSearchThread;
    private final Object fSearchSyncObj = new Object();

    /**
     * List of selection change listeners (element type: <code>ISelectionChangedListener</code>).
     *
     * @see #fireSelectionChanged
     */
    private ListenerList selectionChangedListeners = new ListenerList();

    // Bookmark map <Rank, MarkerId>
    private Multimap<Long, Long> fBookmarksMap = HashMultimap.create();
    private IFile fBookmarksFile;
    private long fPendingGotoRank = -1;

    // SWT resources
    private LocalResourceManager fResourceManager = new LocalResourceManager(JFaceResources.getResources());
    private Color fGrayColor;
    private Color fGreenColor;
    private Font fBoldFont;

    private final List<TmfEventTableColumn> fColumns = new LinkedList<>();

    // Event cache
    private final TmfEventsCache fCache;
    private boolean fCacheUpdateBusy = false;
    private boolean fCacheUpdatePending = false;
    private boolean fCacheUpdateCompleted = false;
    private final Object fCacheUpdateSyncObj = new Object();

    private boolean fDisposeOnClose;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Basic constructor, using the default set of columns
     *
     * @param parent
     *            The parent composite UI object
     * @param cacheSize
     *            The size of the event table cache
     */
    public TmfEventsTable(final Composite parent, final int cacheSize) {
        this(parent, cacheSize, DEFAULT_COLUMNS);
    }

    /**
     * Legacy constructor, using ColumnData to define columns
     *
     * @param parent
     *            The parent composite UI object
     * @param cacheSize
     *            The size of the event table cache
     * @param columnData
     *            Unused
     * @deprecated Deprecated constructor, use
     *             {@link #TmfEventsTable(Composite, int, Collection)}
     */
    @Deprecated
    public TmfEventsTable(final Composite parent, int cacheSize,
            final org.eclipse.linuxtools.tmf.ui.widgets.virtualtable.ColumnData[] columnData) {
        /*
         * We'll do a "best-effort" to keep trace types still using this API to
         * keep working, by defining a TmfEventTableFieldColumn for each
         * ColumnData they passed.
         */
        this(parent, cacheSize, convertFromColumnData(columnData));
    }

    @Deprecated
    private static Collection<TmfEventTableColumn> convertFromColumnData(
            org.eclipse.linuxtools.tmf.ui.widgets.virtualtable.ColumnData[] columnData) {

        ImmutableList.Builder<TmfEventTableColumn> builder = new ImmutableList.Builder<>();
        for (org.eclipse.linuxtools.tmf.ui.widgets.virtualtable.ColumnData col : columnData) {
            String header = col.header;
            if (header != null) {
                builder.add(new TmfEventTableFieldColumn(header));
            }
        }
        return builder.build();
    }

    /**
     * Standard constructor, where we define which columns to use.
     *
     * @param parent
     *            The parent composite UI object
     * @param cacheSize
     *            The size of the event table cache
     * @param columns
     *            The columns to use in this table.
     *            <p>
     *            The iteration order of this collection will correspond to the
     *            initial ordering of this series of columns in the table.
     *            </p>
     * @since 3.1
     */
    public TmfEventsTable(final Composite parent, int cacheSize,
            Collection<? extends TmfEventTableColumn> columns) {
        super("TmfEventsTable"); //$NON-NLS-1$

        fComposite = new Composite(parent, SWT.NONE);
        final GridLayout gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.verticalSpacing = 0;
        fComposite.setLayout(gl);

        fSashForm = new SashForm(fComposite, SWT.HORIZONTAL);
        fSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Create a virtual table
        final int style = SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION;
        fTable = new TmfVirtualTable(fSashForm, style);

        // Set the table layout
        final GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        fTable.setLayoutData(layoutData);

        // Some cosmetic enhancements
        fTable.setHeaderVisible(true);
        fTable.setLinesVisible(true);

        // Setup the columns
        if (columns != null) {
            fColumns.addAll(columns);
        }

        TmfMarginColumn collapseCol = new TmfMarginColumn();
        fColumns.add(MARGIN_COLUMN_INDEX, collapseCol);

        // Create the UI columns in the table
        for (TmfEventTableColumn col : fColumns) {
            TableColumn column = fTable.newTableColumn(SWT.LEFT);
            column.setText(col.getHeaderName());
            column.setToolTipText(col.getHeaderTooltip());
            column.setData(Key.FIELD_ID, col.getFilterFieldId());
            column.pack();
            if (col instanceof TmfMarginColumn) {
                column.setResizable(false);
            }
        }

        // Set the frozen row for header row
        fTable.setFrozenRowCount(1);

        // Create the header row cell editor
        createHeaderEditor();

        // Handle the table item selection
        fTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (e.item == null) {
                    return;
                }
                updateStatusLine(null);
                if (fTable.getSelectionIndices().length > 0) {
                    if (e.item.getData(Key.RANK) instanceof Long) {
                        fSelectedRank = (Long) e.item.getData(Key.RANK);
                        fRawViewer.selectAndReveal((Long) e.item.getData(Key.RANK));
                    }
                    if (e.item.getData(Key.TIMESTAMP) instanceof ITmfTimestamp) {
                        final ITmfTimestamp ts = (ITmfTimestamp) e.item.getData(Key.TIMESTAMP);
                        if (fTable.getSelectionIndices().length == 1) {
                            fSelectedBeginTimestamp = ts;
                        }
                        if (fSelectedBeginTimestamp != null) {
                            if (fSelectedBeginTimestamp.compareTo(ts) <= 0) {
                                broadcast(new TmfTimeSynchSignal(TmfEventsTable.this, fSelectedBeginTimestamp, ts));
                                if (fTable.getSelectionIndices().length == 2) {
                                    updateStatusLine(ts.getDelta(fSelectedBeginTimestamp));
                                }
                            } else {
                                broadcast(new TmfTimeSynchSignal(TmfEventsTable.this, ts, fSelectedBeginTimestamp));
                                updateStatusLine(fSelectedBeginTimestamp.getDelta(ts));
                            }
                        }
                    } else {
                        if (fTable.getSelectionIndices().length == 1) {
                            fSelectedBeginTimestamp = null;
                        }
                    }
                }
                if (e.item.getData() != null) {
                    fireSelectionChanged(new SelectionChangedEvent(TmfEventsTable.this, new StructuredSelection(e.item.getData())));
                } else {
                    fireSelectionChanged(new SelectionChangedEvent(TmfEventsTable.this, StructuredSelection.EMPTY));
                }
            }
        });

        int realCacheSize = Math.max(cacheSize, Display.getDefault().getBounds().height / fTable.getItemHeight());
        realCacheSize = Math.min(realCacheSize, MAX_CACHE_SIZE);
        fCache = new TmfEventsCache(realCacheSize, this);

        // Handle the table item requests
        fTable.addListener(SWT.SetData, new Listener() {

            @Override
            public void handleEvent(final Event event) {

                final TableItem item = (TableItem) event.item;
                int index = event.index - 1; // -1 for the header row

                if (event.index == 0) {
                    setHeaderRowItemData(item);
                    return;
                }

                if (fTable.getData(Key.FILTER_OBJ) != null) {
                    if ((event.index == 1) || (event.index == (fTable.getItemCount() - 1))) {
                        setFilterStatusRowItemData(item);
                        return;
                    }
                    index = index - 1; // -1 for top filter status row
                }

                final CachedEvent cachedEvent = fCache.getEvent(index);
                if (cachedEvent != null) {
                    setItemData(item, cachedEvent, cachedEvent.rank);
                    return;
                }

                // Else, fill the cache asynchronously (and off the UI thread)
                event.doit = false;
            }
        });

        fTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(final MouseEvent event) {
                if (event.button != 1) {
                    return;
                }
                // Identify the selected row
                final Point point = new Point(event.x, event.y);
                final TableItem item = fTable.getItem(point);
                if (item != null) {
                    final Rectangle imageBounds = item.getImageBounds(0);
                    imageBounds.width = BOOKMARK_IMAGE.getBounds().width;
                    if (imageBounds.contains(point)) {
                        final Long rank = (Long) item.getData(Key.RANK);
                        if (rank != null) {
                            toggleBookmark(rank);
                        }
                    }
                }
            }
        });

        final Listener tooltipListener = new Listener () {
            Shell tooltipShell = null;
            @Override
            public void handleEvent(final Event event) {
                switch (event.type) {
                    case SWT.MouseHover:
                        final TableItem item = fTable.getItem(new Point(event.x, event.y));
                        if (item == null) {
                            return;
                        }
                        final Long rank = (Long) item.getData(Key.RANK);
                        if (rank == null) {
                            return;
                        }
                        final String tooltipText = (String) item.getData(Key.BOOKMARK);
                        final Rectangle bounds = item.getImageBounds(0);
                        bounds.width = BOOKMARK_IMAGE.getBounds().width;
                        if (!bounds.contains(event.x,event.y)) {
                            return;
                        }
                        if ((tooltipShell != null) && !tooltipShell.isDisposed()) {
                            tooltipShell.dispose();
                        }
                        tooltipShell = new Shell(fTable.getShell(), SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
                        tooltipShell.setBackground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
                        final FillLayout layout = new FillLayout();
                        layout.marginWidth = 2;
                        tooltipShell.setLayout(layout);
                        final Label label = new Label(tooltipShell, SWT.WRAP);
                        String text = rank.toString() + (tooltipText != null ? ": " + tooltipText : EMPTY_STRING); //$NON-NLS-1$
                        label.setForeground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
                        label.setBackground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
                        label.setText(text);
                        label.addListener(SWT.MouseExit, this);
                        label.addListener(SWT.MouseDown, this);
                        label.addListener(SWT.MouseWheel, this);
                        final Point size = tooltipShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                        /*
                         * Bug in Linux.  The coordinates of the event have an origin that excludes the table header but
                         * the method toDisplay() expects coordinates relative to an origin that includes the table header.
                         */
                        int y = event.y;
                        if (IS_LINUX) {
                            y += fTable.getHeaderHeight();
                        }
                        Point pt = fTable.toDisplay(event.x, y);
                        pt.x += BOOKMARK_IMAGE.getBounds().width;
                        pt.y += item.getBounds().height;
                        tooltipShell.setBounds(pt.x, pt.y, size.x, size.y);
                        tooltipShell.setVisible(true);
                        break;
                    case SWT.Dispose:
                    case SWT.KeyDown:
                    case SWT.MouseMove:
                    case SWT.MouseExit:
                    case SWT.MouseDown:
                    case SWT.MouseWheel:
                        if (tooltipShell != null) {
                            tooltipShell.dispose();
                            tooltipShell = null;
                        }
                        break;
                    default:
                        break;
                }
            }
        };

        fTable.addListener(SWT.MouseHover, tooltipListener);
        fTable.addListener(SWT.Dispose, tooltipListener);
        fTable.addListener(SWT.KeyDown, tooltipListener);
        fTable.addListener(SWT.MouseMove, tooltipListener);
        fTable.addListener(SWT.MouseExit, tooltipListener);
        fTable.addListener(SWT.MouseDown, tooltipListener);
        fTable.addListener(SWT.MouseWheel, tooltipListener);

        // Create resources
        createResources();

        ColorSettingsManager.addColorSettingsListener(this);

        fTable.setItemCount(1); // +1 for header row

        fRawViewer = new TmfRawEventViewer(fSashForm, SWT.H_SCROLL | SWT.V_SCROLL);

        fRawViewer.addSelectionListener(new Listener() {
            @Override
            public void handleEvent(final Event e) {
                if (e.data instanceof Long) {
                    final long rank = (Long) e.data;
                    int index = (int) rank;
                    if (fTable.getData(Key.FILTER_OBJ) != null) {
                        index = fCache.getFilteredEventIndex(rank) + 1; // +1 for top filter status row
                    }
                    fTable.setSelection(index + 1); // +1 for header row
                    fSelectedRank = rank;
                    updateStatusLine(null);
                } else if (e.data instanceof ITmfLocation) {
                    // DOES NOT WORK: rank undefined in context from seekLocation()
                    // ITmfLocation<?> location = (ITmfLocation<?>) e.data;
                    // TmfContext context = fTrace.seekLocation(location);
                    // fTable.setSelection((int) context.getRank());
                    return;
                } else {
                    return;
                }
                final TableItem[] selection = fTable.getSelection();
                if ((selection != null) && (selection.length > 0)) {
                    final TmfTimestamp ts = (TmfTimestamp) fTable.getSelection()[0].getData(Key.TIMESTAMP);
                    if (ts != null) {
                        broadcast(new TmfTimeSynchSignal(TmfEventsTable.this, ts));
                    }
                }
            }
        });

        fSashForm.setWeights(new int[] { 1, 1 });
        fRawViewer.setVisible(false);

        createPopupMenu();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Create a pop-up menu.
     */
    protected void createPopupMenu() {
        final IAction showTableAction = new Action(Messages.TmfEventsTable_ShowTableActionText) {
            @Override
            public void run() {
                fTable.setVisible(true);
                fSashForm.layout();
            }
        };

        final IAction hideTableAction = new Action(Messages.TmfEventsTable_HideTableActionText) {
            @Override
            public void run() {
                fTable.setVisible(false);
                fSashForm.layout();
            }
        };

        final IAction showRawAction = new Action(Messages.TmfEventsTable_ShowRawActionText) {
            @Override
            public void run() {
                fRawViewer.setVisible(true);
                fSashForm.layout();
                final int index = fTable.getSelectionIndex();
                if (index >= 1) {
                    fRawViewer.selectAndReveal(index - 1);
                }
            }
        };

        final IAction hideRawAction = new Action(Messages.TmfEventsTable_HideRawActionText) {
            @Override
            public void run() {
                fRawViewer.setVisible(false);
                fSashForm.layout();
            }
        };

        final IAction openCallsiteAction = new Action(Messages.TmfEventsTable_OpenSourceCodeActionText) {
            @Override
            public void run() {
                final TableItem items[] = fTable.getSelection();
                if (items.length != 1) {
                    return;
                }
                final TableItem item = items[0];

                final Object data = item.getData();
                if (data instanceof ITmfSourceLookup) {
                    ITmfSourceLookup event = (ITmfSourceLookup) data;
                    ITmfCallsite cs = event.getCallsite();
                    if (cs == null || cs.getFileName() == null) {
                        return;
                    }
                    IMarker marker = null;
                    try {
                        String fileName = cs.getFileName();
                        final String trimmedPath = fileName.replaceAll("\\.\\./", EMPTY_STRING); //$NON-NLS-1$
                        final ArrayList<IFile> files = new ArrayList<>();
                        ResourcesPlugin.getWorkspace().getRoot().accept(new IResourceVisitor() {
                            @Override
                            public boolean visit(IResource resource) throws CoreException {
                                if (resource instanceof IFile && resource.getFullPath().toString().endsWith(trimmedPath)) {
                                    files.add((IFile) resource);
                                }
                                return true;
                            }
                        });
                        IFile file = null;
                        if (files.size() > 1) {
                            ListDialog dialog = new ListDialog(getTable().getShell());
                            dialog.setContentProvider(ArrayContentProvider.getInstance());
                            dialog.setLabelProvider(new LabelProvider() {
                                @Override
                                public String getText(Object element) {
                                    return ((IFile) element).getFullPath().toString();
                                }
                            });
                            dialog.setInput(files);
                            dialog.setTitle(Messages.TmfEventsTable_OpenSourceCodeSelectFileDialogTitle);
                            dialog.setMessage(Messages.TmfEventsTable_OpenSourceCodeSelectFileDialogTitle + '\n' + cs.toString());
                            dialog.open();
                            Object[] result = dialog.getResult();
                            if (result != null && result.length > 0) {
                                file = (IFile) result[0];
                            }
                        } else if (files.size() == 1) {
                            file = files.get(0);
                        }
                        if (file != null) {
                            marker = file.createMarker(IMarker.MARKER);
                            marker.setAttribute(IMarker.LINE_NUMBER, Long.valueOf(cs.getLineNumber()).intValue());
                            IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), marker);
                            marker.delete();
                        } else if (files.size() == 0){
                            displayException(new FileNotFoundException('\'' + cs.toString() + '\'' + '\n' + Messages.TmfEventsTable_OpenSourceCodeNotFound));
                        }
                    } catch (CoreException e) {
                        displayException(e);
                    }
                }
            }
        };

        final IAction openModelAction = new Action(Messages.TmfEventsTable_OpenModelActionText) {
            @Override
            public void run() {

                final TableItem items[] = fTable.getSelection();
                if (items.length != 1) {
                    return;
                }
                final TableItem item = items[0];

                final Object eventData = item.getData();
                if (eventData instanceof ITmfModelLookup) {
                    String modelURI = ((ITmfModelLookup) eventData).getModelUri();

                    if (modelURI != null) {
                        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

                        IFile file = null;
                        final URI uri = URI.createURI(modelURI);
                        if (uri.isPlatformResource()) {
                            IPath path = new Path(uri.toPlatformString(true));
                            file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
                        } else if (uri.isFile() && !uri.isRelative()) {
                            file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(
                                    new Path(uri.toFileString()));
                        }

                        if (file != null) {
                            try {
                                /*
                                 * create a temporary validation marker on the
                                 * model file, remove it afterwards thus,
                                 * navigation works with all model editors
                                 * supporting the navigation to a marker
                                 */
                                IMarker marker = file.createMarker(EValidator.MARKER);
                                marker.setAttribute(EValidator.URI_ATTRIBUTE, modelURI);
                                marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);

                                IDE.openEditor(activePage, marker, OpenStrategy.activateOnOpen());
                                marker.delete();
                            }
                            catch (CoreException e) {
                                displayException(e);
                            }
                        } else {
                            displayException(new FileNotFoundException('\'' + modelURI + '\'' + '\n' + Messages.TmfEventsTable_OpenModelUnsupportedURI));
                        }
                    }
                }
            }
        };

        final IAction exportToTextAction = new Action(Messages.TmfEventsTable_Export_to_text) {
            @Override
            public void run() {
                IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                IHandlerService handlerService = (IHandlerService) activePage.getActiveEditor().getSite().getService(IHandlerService.class);
                ICommandService cmdService = (ICommandService) activePage.getActiveEditor().getSite().getService(ICommandService.class);
                try {
                    HashMap<String, Object> parameters = new HashMap<>();
                    Command command = cmdService.getCommand(ExportToTextCommandHandler.COMMAND_ID);
                    ParameterizedCommand cmd = ParameterizedCommand.generateCommand(command, parameters);

                    IEvaluationContext context = handlerService.getCurrentState();
                    // Omit the margin column
                    List<TmfEventTableColumn> exportColumns = fColumns.subList(EVENT_COLUMNS_START_INDEX, fColumns.size());
                    context.addVariable(ExportToTextCommandHandler.TMF_EVENT_TABLE_COLUMNS_ID, exportColumns);

                    handlerService.executeCommandInContext(cmd, null, context);
                } catch (ExecutionException e) {
                    displayException(e);
                } catch (NotDefinedException e) {
                    displayException(e);
                } catch (NotEnabledException e) {
                    displayException(e);
                } catch (NotHandledException e) {
                    displayException(e);
                }
            }
        };

        final IAction showSearchBarAction = new Action(Messages.TmfEventsTable_ShowSearchBarActionText) {
            @Override
            public void run() {
                fHeaderState = HeaderState.SEARCH;
                fTable.refresh();
            }
        };

        final IAction showFilterBarAction = new Action(Messages.TmfEventsTable_ShowFilterBarActionText) {
            @Override
            public void run() {
                fHeaderState = HeaderState.FILTER;
                fTable.refresh();
            }
        };

        final IAction clearFiltersAction = new Action(Messages.TmfEventsTable_ClearFiltersActionText) {
            @Override
            public void run() {
                clearFilters();
            }
        };

        final IAction collapseAction = new Action(Messages.TmfEventsTable_CollapseFilterMenuName) {
            @Override
            public void run() {
                applyFilter(new TmfCollapseFilter());
            }
        };

        class ToggleBookmarkAction extends Action {
            Long fRank;

            public ToggleBookmarkAction(final String text, final Long rank) {
                super(text);
                fRank = rank;
            }

            @Override
            public void run() {
                toggleBookmark(fRank);
            }
        }

        final MenuManager tablePopupMenu = new MenuManager();
        tablePopupMenu.setRemoveAllWhenShown(true);
        tablePopupMenu.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(final IMenuManager manager) {
                if (fTable.getSelectionIndex() == 0) {
                    // Right-click on header row
                    if (fHeaderState == HeaderState.FILTER) {
                        tablePopupMenu.add(showSearchBarAction);
                    } else {
                        tablePopupMenu.add(showFilterBarAction);
                    }
                    return;
                }
                final Point point = fTable.toControl(Display.getDefault().getCursorLocation());
                final TableItem item = fTable.getSelection().length > 0 ? fTable.getSelection()[0] : null;
                if (item != null) {
                    final Rectangle imageBounds = item.getImageBounds(0);
                    imageBounds.width = BOOKMARK_IMAGE.getBounds().width;
                    if (point.x <= (imageBounds.x + imageBounds.width)) {
                        // Right-click on left margin
                        final Long rank = (Long) item.getData(Key.RANK);
                        if ((rank != null) && (fBookmarksFile != null)) {
                            if (fBookmarksMap.containsKey(rank)) {
                                tablePopupMenu.add(new ToggleBookmarkAction(
                                        Messages.TmfEventsTable_RemoveBookmarkActionText, rank));
                            } else {
                                tablePopupMenu.add(new ToggleBookmarkAction(
                                        Messages.TmfEventsTable_AddBookmarkActionText, rank));
                            }
                        }
                        return;
                    }
                }

                // Right-click on table
                if (fTable.isVisible() && fRawViewer.isVisible()) {
                    tablePopupMenu.add(hideTableAction);
                    tablePopupMenu.add(hideRawAction);
                } else if (!fTable.isVisible()) {
                    tablePopupMenu.add(showTableAction);
                } else if (!fRawViewer.isVisible()) {
                    tablePopupMenu.add(showRawAction);
                }
                tablePopupMenu.add(exportToTextAction);
                tablePopupMenu.add(new Separator());

                if (item != null) {
                    final Object data = item.getData();
                    Separator separator = null;
                    if (data instanceof ITmfSourceLookup) {
                        ITmfSourceLookup event = (ITmfSourceLookup) data;
                        if (event.getCallsite() != null) {
                            tablePopupMenu.add(openCallsiteAction);
                            separator = new Separator();
                        }
                    }

                    if (data instanceof ITmfModelLookup) {
                        ITmfModelLookup event = (ITmfModelLookup) data;
                        if (event.getModelUri() != null) {
                            tablePopupMenu.add(openModelAction);
                            separator = new Separator();
                        }

                        if (separator != null) {
                            tablePopupMenu.add(separator);
                        }
                    }
                }

                // only show collapse filter if at least one trace can be collapsed
                boolean isCollapsible = false;
                if (fTrace != null) {
                    ITmfTrace traces[] = TmfTraceManager.getTraceSet(fTrace);
                    for (ITmfTrace trace : traces) {
                        Class <? extends ITmfEvent> eventClass = trace.getEventType();
                        isCollapsible = ITmfCollapsibleEvent.class.isAssignableFrom(eventClass);
                        if (isCollapsible) {
                            break;
                        }
                    }
                }

                if (isCollapsible && !(fTable.getData(Key.FILTER_OBJ) instanceof TmfCollapseFilter)) {
                    tablePopupMenu.add(collapseAction);
                    tablePopupMenu.add(new Separator());
                }

                tablePopupMenu.add(clearFiltersAction);
                final ITmfFilterTreeNode[] savedFilters = FilterManager.getSavedFilters();
                if (savedFilters.length > 0) {
                    final MenuManager subMenu = new MenuManager(Messages.TmfEventsTable_ApplyPresetFilterMenuName);
                    for (final ITmfFilterTreeNode node : savedFilters) {
                        if (node instanceof TmfFilterNode) {
                            final TmfFilterNode filter = (TmfFilterNode) node;
                            subMenu.add(new Action(filter.getFilterName()) {
                                @Override
                                public void run() {
                                    applyFilter(filter);
                                }
                            });
                        }
                    }
                    tablePopupMenu.add(subMenu);
                }
                appendToTablePopupMenu(tablePopupMenu, item);
            }
        });

        final MenuManager rawViewerPopupMenu = new MenuManager();
        rawViewerPopupMenu.setRemoveAllWhenShown(true);
        rawViewerPopupMenu.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(final IMenuManager manager) {
                if (fTable.isVisible() && fRawViewer.isVisible()) {
                    rawViewerPopupMenu.add(hideTableAction);
                    rawViewerPopupMenu.add(hideRawAction);
                } else if (!fTable.isVisible()) {
                    rawViewerPopupMenu.add(showTableAction);
                } else if (!fRawViewer.isVisible()) {
                    rawViewerPopupMenu.add(showRawAction);
                }
                appendToRawPopupMenu(tablePopupMenu);
            }
        });

        Menu menu = tablePopupMenu.createContextMenu(fTable);
        fTable.setMenu(menu);

        menu = rawViewerPopupMenu.createContextMenu(fRawViewer);
        fRawViewer.setMenu(menu);
    }


    /**
     * Append an item to the event table's pop-up menu.
     *
     * @param tablePopupMenu
     *            The menu manager
     * @param selectedItem
     *            The item to append
     */
    protected void appendToTablePopupMenu(final MenuManager tablePopupMenu, final TableItem selectedItem) {
        // override to append more actions
    }

    /**
     * Append an item to the raw viewer's pop-up menu.
     *
     * @param rawViewerPopupMenu
     *            The menu manager
     */
    protected void appendToRawPopupMenu(final MenuManager rawViewerPopupMenu) {
        // override to append more actions
    }

    @Override
    public void dispose() {
        stopSearchThread();
        stopFilterThread();
        ColorSettingsManager.removeColorSettingsListener(this);
        fComposite.dispose();
        if ((fTrace != null) && fDisposeOnClose) {
            fTrace.dispose();
        }
        fResourceManager.dispose();
        fRawViewer.dispose();
        super.dispose();
    }

    /**
     * Assign a layout data object to this view.
     *
     * @param layoutData
     *            The layout data to assign
     */
    public void setLayoutData(final Object layoutData) {
        fComposite.setLayoutData(layoutData);
    }

    /**
     * Get the virtual table contained in this event table.
     *
     * @return The TMF virtual table
     */
    public TmfVirtualTable getTable() {
        return fTable;
    }

    /**
     * @param columnData
     *            columnData
     * @deprecated The column headers are now set at the constructor, this
     *             shouldn't be called anymore.
     */
    @Deprecated
    protected void setColumnHeaders(final org.eclipse.linuxtools.tmf.ui.widgets.virtualtable.ColumnData [] columnData) {
        /* No-op */
    }

    /**
     * Set a table item's data.
     *
     * @param item
     *            The item to set
     * @param event
     *            Which trace event to link with this entry
     * @param rank
     *            Which rank this event has in the trace/experiment
     */
    protected void setItemData(final TableItem item, final ITmfEvent event, final long rank) {
        String[] itemStrings = getItemStrings(fColumns, event);
        item.setText(itemStrings);
        item.setData(event);
        item.setData(Key.TIMESTAMP, new TmfTimestamp(event.getTimestamp()));
        item.setData(Key.RANK, rank);

        final Collection<Long> markerIds = fBookmarksMap.get(rank);
        if (!markerIds.isEmpty()) {
            Joiner joiner = Joiner.on("\n -").skipNulls(); //$NON-NLS-1$
            List<Object> parts = new ArrayList<>();
            if (markerIds.size() > 1) {
                parts.add(Messages.TmfEventsTable_MultipleBookmarksToolTip);
            }
            try {
                for (long markerId : markerIds) {
                    final IMarker marker = fBookmarksFile.findMarker(markerId);
                    parts.add(marker.getAttribute(IMarker.MESSAGE));
                }
            } catch (CoreException e) {
                displayException(e);
            }
            item.setData(Key.BOOKMARK, joiner.join(parts));
        } else {
            item.setData(Key.BOOKMARK, null);
        }

        boolean searchMatch = false;
        boolean searchNoMatch = false;
        final ITmfFilter searchFilter = (ITmfFilter) fTable.getData(Key.SEARCH_OBJ);
        if (searchFilter != null) {
            if (searchFilter.matches(event)) {
                searchMatch = true;
            } else {
                searchNoMatch = true;
            }
        }

        final ColorSetting colorSetting = ColorSettingsManager.getColorSetting(event);
        if (searchNoMatch) {
            item.setForeground(colorSetting.getDimmedForegroundColor());
            item.setBackground(colorSetting.getDimmedBackgroundColor());
        } else {
            item.setForeground(colorSetting.getForegroundColor());
            item.setBackground(colorSetting.getBackgroundColor());
        }

        if (searchMatch) {
            if (!markerIds.isEmpty()) {
                item.setImage(SEARCH_MATCH_BOOKMARK_IMAGE);
            } else {
                item.setImage(SEARCH_MATCH_IMAGE);
            }
        } else if (!markerIds.isEmpty()) {
            item.setImage(BOOKMARK_IMAGE);
        } else {
            item.setImage((Image) null);
        }

        if ((itemStrings[MARGIN_COLUMN_INDEX] != null) && !itemStrings[MARGIN_COLUMN_INDEX].isEmpty()) {
            packMarginColumn();
        }
    }

    /**
     * Set the item data of the header row.
     *
     * @param item
     *            The item to use as table header
     */
    protected void setHeaderRowItemData(final TableItem item) {
        String txtKey = null;
        if (fHeaderState == HeaderState.SEARCH) {
            item.setImage(SEARCH_IMAGE);
            txtKey = Key.SEARCH_TXT;
        } else if (fHeaderState == HeaderState.FILTER) {
            item.setImage(FILTER_IMAGE);
            txtKey = Key.FILTER_TXT;
        }
        item.setForeground(fGrayColor);
        // Ignore collapse and image column
        for (int i = EVENT_COLUMNS_START_INDEX; i < fTable.getColumns().length; i++) {
            final TableColumn column = fTable.getColumns()[i];
            final String filter = (String) column.getData(txtKey);
            if (filter == null) {
                if (fHeaderState == HeaderState.SEARCH) {
                    item.setText(i, SEARCH_HINT);
                } else if (fHeaderState == HeaderState.FILTER) {
                    item.setText(i, FILTER_HINT);
                }
                item.setForeground(i, fGrayColor);
                item.setFont(i, fTable.getFont());
            } else {
                item.setText(i, filter);
                item.setForeground(i, fGreenColor);
                item.setFont(i, fBoldFont);
            }
        }
    }

    /**
     * Set the item data of the "filter status" row.
     *
     * @param item
     *            The item to use as filter status row
     */
    protected void setFilterStatusRowItemData(final TableItem item) {
        for (int i = 0; i < fTable.getColumns().length; i++) {
            if (i == MARGIN_COLUMN_INDEX) {
                if ((fTrace == null) || (fFilterCheckCount == fTrace.getNbEvents())) {
                    item.setImage(FILTER_IMAGE);
                } else {
                    item.setImage(STOP_IMAGE);
                }
            }

            if (i == FILTER_SUMMARY_INDEX) {
                item.setText(FILTER_SUMMARY_INDEX, fFilterMatchCount + "/" + fFilterCheckCount); //$NON-NLS-1$
            } else {
                item.setText(i, EMPTY_STRING);
            }
        }
        item.setData(null);
        item.setData(Key.TIMESTAMP, null);
        item.setData(Key.RANK, null);
        item.setForeground(null);
        item.setBackground(null);
    }

    /**
     * Create an editor for the header.
     */
    protected void createHeaderEditor() {
        final TableEditor tableEditor = fTable.createTableEditor();
        tableEditor.horizontalAlignment = SWT.LEFT;
        tableEditor.verticalAlignment = SWT.CENTER;
        tableEditor.grabHorizontal = true;
        tableEditor.minimumWidth = 50;

        // Handle the header row selection
        fTable.addMouseListener(new MouseAdapter() {
            int columnIndex;
            TableColumn column;
            TableItem item;

            @Override
            public void mouseDown(final MouseEvent event) {
                if (event.button != 1) {
                    return;
                }
                // Identify the selected row
                final Point point = new Point(event.x, event.y);
                item = fTable.getItem(point);

                // Header row selected
                if ((item != null) && (fTable.indexOf(item) == 0)) {

                    // Icon selected
                    if (item.getImageBounds(0).contains(point)) {
                        if (fHeaderState == HeaderState.SEARCH) {
                            fHeaderState = HeaderState.FILTER;
                        } else if (fHeaderState == HeaderState.FILTER) {
                            fHeaderState = HeaderState.SEARCH;
                        }
                        fTable.setSelection(0);
                        fTable.refresh();
                        return;
                    }

                    // Identify the selected column
                    columnIndex = -1;
                    for (int i = 0; i < fTable.getColumns().length; i++) {
                        final Rectangle rect = item.getBounds(i);
                        if (rect.contains(point)) {
                            columnIndex = i;
                            break;
                        }
                    }

                    if (columnIndex == -1) {
                        return;
                    }

                    column = fTable.getColumns()[columnIndex];

                    String txtKey = null;
                    if (fHeaderState == HeaderState.SEARCH) {
                        txtKey = Key.SEARCH_TXT;
                    } else if (fHeaderState == HeaderState.FILTER) {
                        txtKey = Key.FILTER_TXT;
                    }

                    // The control that will be the editor must be a child of the Table
                    final Text newEditor = (Text) fTable.createTableEditorControl(Text.class);
                    final String headerString = (String) column.getData(txtKey);
                    if (headerString != null) {
                        newEditor.setText(headerString);
                    }
                    newEditor.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(final FocusEvent e) {
                            final boolean changed = updateHeader(newEditor.getText());
                            if (changed) {
                                applyHeader();
                            }
                        }
                    });
                    newEditor.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyPressed(final KeyEvent e) {
                            if (e.character == SWT.CR) {
                                updateHeader(newEditor.getText());
                                applyHeader();

                                // Set focus on the table so that the next carriage return goes to the next result
                                TmfEventsTable.this.getTable().setFocus();
                            } else if (e.character == SWT.ESC) {
                                tableEditor.getEditor().dispose();
                            }
                        }
                    });
                    newEditor.selectAll();
                    newEditor.setFocus();
                    tableEditor.setEditor(newEditor, item, columnIndex);
                }
            }

            /*
             * returns true is value was changed
             */
            private boolean updateHeader(final String text) {
                String objKey = null;
                String txtKey = null;
                if (fHeaderState == HeaderState.SEARCH) {
                    objKey = Key.SEARCH_OBJ;
                    txtKey = Key.SEARCH_TXT;
                } else if (fHeaderState == HeaderState.FILTER) {
                    objKey = Key.FILTER_OBJ;
                    txtKey = Key.FILTER_TXT;
                }
                if (text.trim().length() > 0) {
                    try {
                        final String regex = TmfFilterMatchesNode.regexFix(text);
                        Pattern.compile(regex);
                        if (regex.equals(column.getData(txtKey))) {
                            tableEditor.getEditor().dispose();
                            return false;
                        }
                        final TmfFilterMatchesNode filter = new TmfFilterMatchesNode(null);
                        String fieldId = (String) column.getData(Key.FIELD_ID);
                        if (fieldId == null) {
                            fieldId = column.getText();
                        }
                        filter.setField(fieldId);
                        filter.setRegex(regex);
                        column.setData(objKey, filter);
                        column.setData(txtKey, regex);
                    } catch (final PatternSyntaxException ex) {
                        tableEditor.getEditor().dispose();
                        MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                ex.getDescription(), ex.getMessage());
                        return false;
                    }
                } else {
                    if (column.getData(txtKey) == null) {
                        tableEditor.getEditor().dispose();
                        return false;
                    }
                    column.setData(objKey, null);
                    column.setData(txtKey, null);
                }
                return true;
            }

            private void applyHeader() {
                if (fHeaderState == HeaderState.SEARCH) {
                    stopSearchThread();
                    final TmfFilterAndNode filter = new TmfFilterAndNode(null);
                    for (final TableColumn col : fTable.getColumns()) {
                        final Object filterObj = col.getData(Key.SEARCH_OBJ);
                        if (filterObj instanceof ITmfFilterTreeNode) {
                            filter.addChild((ITmfFilterTreeNode) filterObj);
                        }
                    }
                    if (filter.getChildrenCount() > 0) {
                        fTable.setData(Key.SEARCH_OBJ, filter);
                        fTable.refresh();
                        searchNext();
                        fireSearchApplied(filter);
                    } else {
                        fTable.setData(Key.SEARCH_OBJ, null);
                        fTable.refresh();
                        fireSearchApplied(null);
                    }
                } else if (fHeaderState == HeaderState.FILTER) {
                    final TmfFilterAndNode filter = new TmfFilterAndNode(null);
                    for (final TableColumn col : fTable.getColumns()) {
                        final Object filterObj = col.getData(Key.FILTER_OBJ);
                        if (filterObj instanceof ITmfFilterTreeNode) {
                            filter.addChild((ITmfFilterTreeNode) filterObj);
                        }
                    }
                    if (filter.getChildrenCount() > 0) {
                        applyFilter(filter);
                    } else {
                        clearFilters();
                    }
                }

                tableEditor.getEditor().dispose();
            }
        });

        fTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                e.doit = false;
                if (e.character == SWT.ESC) {
                    stopFilterThread();
                    stopSearchThread();
                    fTable.refresh();
                } else if (e.character == SWT.DEL) {
                    if (fHeaderState == HeaderState.SEARCH) {
                        stopSearchThread();
                        for (final TableColumn column : fTable.getColumns()) {
                            column.setData(Key.SEARCH_OBJ, null);
                            column.setData(Key.SEARCH_TXT, null);
                        }
                        fTable.setData(Key.SEARCH_OBJ, null);
                        fTable.refresh();
                        fireSearchApplied(null);
                    } else if (fHeaderState == HeaderState.FILTER) {
                        clearFilters();
                    }
                } else if (e.character == SWT.CR) {
                    if ((e.stateMask & SWT.SHIFT) == 0) {
                        searchNext();
                    } else {
                        searchPrevious();
                    }
                }
            }
        });
    }

    /**
     * Send an event indicating a filter has been applied.
     *
     * @param filter
     *            The filter that was just applied
     */
    protected void fireFilterApplied(final ITmfFilter filter) {
        broadcast(new TmfEventFilterAppliedSignal(this, fTrace, filter));
    }

    /**
     * Send an event indicating that a search has been applied.
     *
     * @param filter
     *            The search filter that was just applied
     */
    protected void fireSearchApplied(final ITmfFilter filter) {
        broadcast(new TmfEventSearchAppliedSignal(this, fTrace, filter));
    }

    /**
     * Start the filtering thread.
     */
    protected void startFilterThread() {
        synchronized (fFilterSyncObj) {
            final ITmfFilterTreeNode filter = (ITmfFilterTreeNode) fTable.getData(Key.FILTER_OBJ);
            if (fFilterThread == null || fFilterThread.filter != filter) {
                if (fFilterThread != null) {
                    fFilterThread.cancel();
                    fFilterThreadResume = false;
                }
                fFilterThread = new FilterThread(filter);
                fFilterThread.start();
            } else {
                fFilterThreadResume = true;
            }
        }
    }

    /**
     * Stop the filtering thread.
     */
    protected void stopFilterThread() {
        synchronized (fFilterSyncObj) {
            if (fFilterThread != null) {
                fFilterThread.cancel();
                fFilterThread = null;
                fFilterThreadResume = false;
            }
        }
    }

    /**
     * Apply a filter.
     *
     * @param filter
     *            The filter to apply
     * @since 1.1
     */
    protected void applyFilter(ITmfFilter filter) {
        stopFilterThread();
        stopSearchThread();
        fFilterMatchCount = 0;
        fFilterCheckCount = 0;
        fCache.applyFilter(filter);
        fTable.clearAll();
        fTable.setData(Key.FILTER_OBJ, filter);
        fTable.setItemCount(3); // +1 for header row, +2 for top and bottom filter status rows
        startFilterThread();
        fireFilterApplied(filter);
    }

    /**
     * Clear all currently active filters.
     */
    protected void clearFilters() {
        if (fTable.getData(Key.FILTER_OBJ) == null) {
            return;
        }
        stopFilterThread();
        stopSearchThread();
        fCache.clearFilter();
        fTable.clearAll();
        for (final TableColumn column : fTable.getColumns()) {
            column.setData(Key.FILTER_OBJ, null);
            column.setData(Key.FILTER_TXT, null);
        }
        fTable.setData(Key.FILTER_OBJ, null);
        if (fTrace != null) {
            fTable.setItemCount((int) fTrace.getNbEvents() + 1); // +1 for header row
        } else {
            fTable.setItemCount(1); // +1 for header row
        }
        fFilterMatchCount = 0;
        fFilterCheckCount = 0;
        if (fSelectedRank >= 0) {
            fTable.setSelection((int) fSelectedRank + 1); // +1 for header row
        } else {
            fTable.setSelection(0);
        }
        fireFilterApplied(null);
        updateStatusLine(null);

        // Set original width
        fTable.getColumns()[MARGIN_COLUMN_INDEX].setWidth(0);
        packMarginColumn();
    }

    /**
     * Wrapper Thread object for the filtering thread.
     */
    protected class FilterThread extends Thread {
        private final ITmfFilterTreeNode filter;
        private TmfEventRequest request;
        private boolean refreshBusy = false;
        private boolean refreshPending = false;
        private final Object syncObj = new Object();

        /**
         * Constructor.
         *
         * @param filter
         *            The filter this thread will be processing
         */
        public FilterThread(final ITmfFilterTreeNode filter) {
            super("Filter Thread"); //$NON-NLS-1$
            this.filter = filter;
        }

        @Override
        public void run() {
            if (fTrace == null) {
                return;
            }
            final int nbRequested = (int) (fTrace.getNbEvents() - fFilterCheckCount);
            if (nbRequested <= 0) {
                return;
            }
            request = new TmfEventRequest(ITmfEvent.class, TmfTimeRange.ETERNITY,
                    (int) fFilterCheckCount, nbRequested, ExecutionType.BACKGROUND) {
                @Override
                public void handleData(final ITmfEvent event) {
                    super.handleData(event);
                    if (request.isCancelled()) {
                        return;
                    }
                    boolean refresh = false;
                    if (filter.matches(event)) {
                        final long rank = fFilterCheckCount;
                        final int index = (int) fFilterMatchCount;
                        fFilterMatchCount++;
                        fCache.storeEvent(event, rank, index);
                        refresh = true;
                    } else {
                        if (filter instanceof TmfCollapseFilter) {
                            fCache.updateCollapsedEvent((int) fFilterMatchCount - 1);
                        }
                    }

                    if (refresh || (fFilterCheckCount % 100) == 0) {
                        refreshTable();
                    }
                    fFilterCheckCount++;
                }
            };
            ((ITmfEventProvider) fTrace).sendRequest(request);
            try {
                request.waitForCompletion();
            } catch (final InterruptedException e) {
            }
            refreshTable();
            synchronized (fFilterSyncObj) {
                fFilterThread = null;
                if (fFilterThreadResume) {
                    fFilterThreadResume = false;
                    fFilterThread = new FilterThread(filter);
                    fFilterThread.start();
                }
            }
        }

        /**
         * Refresh the filter.
         */
        public void refreshTable() {
            synchronized (syncObj) {
                if (refreshBusy) {
                    refreshPending = true;
                    return;
                }
                refreshBusy = true;
            }
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (request.isCancelled()) {
                        return;
                    }
                    if (fTable.isDisposed()) {
                        return;
                    }
                    fTable.setItemCount((int) fFilterMatchCount + 3); // +1 for header row, +2 for top and bottom filter status rows
                    fTable.refresh();
                    synchronized (syncObj) {
                        refreshBusy = false;
                        if (refreshPending) {
                            refreshPending = false;
                            refreshTable();
                        }
                    }
                }
            });
        }

        /**
         * Cancel this filtering thread.
         */
        public void cancel() {
            if (request != null) {
                request.cancel();
            }
        }
    }

    /**
     * Go to the next item of a search.
     */
    protected void searchNext() {
        synchronized (fSearchSyncObj) {
            if (fSearchThread != null) {
                return;
            }
            final ITmfFilterTreeNode searchFilter = (ITmfFilterTreeNode) fTable.getData(Key.SEARCH_OBJ);
            if (searchFilter == null) {
                return;
            }
            final int selectionIndex = fTable.getSelectionIndex();
            int startIndex;
            if (selectionIndex > 0) {
                startIndex = selectionIndex; // -1 for header row, +1 for next event
            } else {
                // header row is selected, start at top event
                startIndex = Math.max(0, fTable.getTopIndex() - 1); // -1 for header row
            }
            final ITmfFilterTreeNode eventFilter = (ITmfFilterTreeNode) fTable.getData(Key.FILTER_OBJ);
            if (eventFilter != null) {
                startIndex = Math.max(0, startIndex - 1); // -1 for top filter status row
            }
            fSearchThread = new SearchThread(searchFilter, eventFilter, startIndex, fSelectedRank, Direction.FORWARD);
            fSearchThread.schedule();
        }
    }

    /**
     * Go to the previous item of a search.
     */
    protected void searchPrevious() {
        synchronized (fSearchSyncObj) {
            if (fSearchThread != null) {
                return;
            }
            final ITmfFilterTreeNode searchFilter = (ITmfFilterTreeNode) fTable.getData(Key.SEARCH_OBJ);
            if (searchFilter == null) {
                return;
            }
            final int selectionIndex = fTable.getSelectionIndex();
            int startIndex;
            if (selectionIndex > 0) {
                startIndex = selectionIndex - 2; // -1 for header row, -1 for previous event
            } else {
                // header row is selected, start at precedent of top event
                startIndex = fTable.getTopIndex() - 2; // -1 for header row, -1 for previous event
            }
            final ITmfFilterTreeNode eventFilter = (ITmfFilterTreeNode) fTable.getData(Key.FILTER_OBJ);
            if (eventFilter != null) {
                startIndex = startIndex - 1; // -1 for top filter status row
            }
            fSearchThread = new SearchThread(searchFilter, eventFilter, startIndex, fSelectedRank, Direction.BACKWARD);
            fSearchThread.schedule();
        }
    }

    /**
     * Stop the search thread.
     */
    protected void stopSearchThread() {
        fPendingGotoRank = -1;
        synchronized (fSearchSyncObj) {
            if (fSearchThread != null) {
                fSearchThread.cancel();
                fSearchThread = null;
            }
        }
    }

    /**
     * Wrapper for the search thread.
     */
    protected class SearchThread extends Job {

        private ITmfFilterTreeNode searchFilter;
        private ITmfFilterTreeNode eventFilter;
        private int startIndex;
        private int direction;
        private long rank;
        private long foundRank = -1;
        private TmfEventRequest request;
        private ITmfTimestamp foundTimestamp = null;

        /**
         * Constructor.
         *
         * @param searchFilter
         *            The search filter
         * @param eventFilter
         *            The event filter
         * @param startIndex
         *            The index at which we should start searching
         * @param currentRank
         *            The current rank
         * @param direction
         *            In which direction should we search, forward or backwards
         */
        public SearchThread(final ITmfFilterTreeNode searchFilter,
                final ITmfFilterTreeNode eventFilter, final int startIndex,
                final long currentRank, final int direction) {
            super(Messages.TmfEventsTable_SearchingJobName);
            this.searchFilter = searchFilter;
            this.eventFilter = eventFilter;
            this.startIndex = startIndex;
            this.rank = currentRank;
            this.direction = direction;
        }

        @Override
        protected IStatus run(final IProgressMonitor monitor) {
            if (fTrace == null) {
                return Status.OK_STATUS;
            }
            final Display display = Display.getDefault();
            if (startIndex < 0) {
                rank = (int) fTrace.getNbEvents() - 1;
            } else if (startIndex >= (fTable.getItemCount() - (eventFilter == null ? 1 : 3))) { // -1 for header row, -2 for top and bottom filter status rows
                rank = 0;
            } else {
                int idx = startIndex;
                while (foundRank == -1) {
                    final CachedEvent event = fCache.peekEvent(idx);
                    if (event == null) {
                        break;
                    }
                    rank = event.rank;
                    if (searchFilter.matches(event.event) && ((eventFilter == null) || eventFilter.matches(event.event))) {
                        foundRank = event.rank;
                        foundTimestamp = event.event.getTimestamp();
                        break;
                    }
                    if (direction == Direction.FORWARD) {
                        idx++;
                    } else {
                        idx--;
                    }
                }
                if (foundRank == -1) {
                    if (direction == Direction.FORWARD) {
                        rank++;
                        if (rank > (fTrace.getNbEvents() - 1)) {
                            rank = 0;
                        }
                    } else {
                        rank--;
                        if (rank < 0) {
                            rank = (int) fTrace.getNbEvents() - 1;
                        }
                    }
                }
            }
            final int startRank = (int) rank;
            boolean wrapped = false;
            while (!monitor.isCanceled() && (foundRank == -1) && (fTrace != null)) {
                int nbRequested = (direction == Direction.FORWARD ? Integer.MAX_VALUE : Math.min((int) rank + 1, fTrace.getCacheSize()));
                if (direction == Direction.BACKWARD) {
                    rank = Math.max(0, rank - fTrace.getCacheSize() + 1);
                }
                request = new TmfEventRequest(ITmfEvent.class, TmfTimeRange.ETERNITY,
                        (int) rank, nbRequested, ExecutionType.BACKGROUND) {
                    long currentRank = rank;

                    @Override
                    public void handleData(final ITmfEvent event) {
                        super.handleData(event);
                        if (searchFilter.matches(event) && ((eventFilter == null) || eventFilter.matches(event))) {
                            foundRank = currentRank;
                            foundTimestamp = event.getTimestamp();
                            if (direction == Direction.FORWARD) {
                                done();
                                return;
                            }
                        }
                        currentRank++;
                    }
                };
                ((ITmfEventProvider) fTrace).sendRequest(request);
                try {
                    request.waitForCompletion();
                    if (request.isCancelled()) {
                        return Status.OK_STATUS;
                    }
                } catch (final InterruptedException e) {
                    synchronized (fSearchSyncObj) {
                        fSearchThread = null;
                    }
                    return Status.OK_STATUS;
                }
                if (foundRank == -1) {
                    if (direction == Direction.FORWARD) {
                        if (rank == 0) {
                            synchronized (fSearchSyncObj) {
                                fSearchThread = null;
                            }
                            return Status.OK_STATUS;
                        }
                        nbRequested = (int) rank;
                        rank = 0;
                        wrapped = true;
                    } else {
                        rank--;
                        if (rank < 0) {
                            rank = (int) fTrace.getNbEvents() - 1;
                            wrapped = true;
                        }
                        if ((rank <= startRank) && wrapped) {
                            synchronized (fSearchSyncObj) {
                                fSearchThread = null;
                            }
                            return Status.OK_STATUS;
                        }
                    }
                }
            }
            int index = (int) foundRank;
            if (eventFilter != null) {
                index = fCache.getFilteredEventIndex(foundRank);
            }
            final int selection = index + 1 + (eventFilter != null ? +1 : 0); // +1 for header row, +1 for top filter status row

            display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (monitor.isCanceled()) {
                        return;
                    }
                    if (fTable.isDisposed()) {
                        return;
                    }
                    fTable.setSelection(selection);
                    fSelectedRank = foundRank;
                    fRawViewer.selectAndReveal(fSelectedRank);
                    if (foundTimestamp != null) {
                        broadcast(new TmfTimeSynchSignal(TmfEventsTable.this, foundTimestamp));
                    }
                    fireSelectionChanged(new SelectionChangedEvent(TmfEventsTable.this, getSelection()));
                    synchronized (fSearchSyncObj) {
                        fSearchThread = null;
                    }
                    updateStatusLine(null);
                }
            });
            return Status.OK_STATUS;
        }

        @Override
        protected void canceling() {
            request.cancel();
            synchronized (fSearchSyncObj) {
                fSearchThread = null;
            }
        }
    }

    /**
     * Create the resources.
     */
    protected void createResources() {
        fGrayColor = fResourceManager.createColor(ColorUtil.blend(fTable.getBackground().getRGB(), fTable
                .getForeground().getRGB()));
        fGreenColor = fTable.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
        fBoldFont = fResourceManager.createFont(FontDescriptor.createFrom(fTable.getFont()).setStyle(SWT.BOLD));
    }

    /**
     * Pack the columns.
     */
    protected void packColumns() {
        if (fPackDone) {
            return;
        }
        fTable.setRedraw(false);
        try {
            TableColumn tableColumns[] = fTable.getColumns();
            for (int i = 0; i < tableColumns.length; i++) {
                final TableColumn column = tableColumns[i];
                packSingleColumn(i, column);
            }
        } finally {
            // Make sure that redraw is always enabled.
            fTable.setRedraw(true);
        }
        fPackDone = true;
    }


    private void packMarginColumn() {
        TableColumn[] columns = fTable.getColumns();
        if (columns.length > 0) {
            packSingleColumn(0, columns[0]);
        }
    }

    private void packSingleColumn(int i, final TableColumn column) {
        final int headerWidth = column.getWidth();
        column.pack();
        // Workaround for Linux which doesn't consider the image width of
        // search/filter row in TableColumn.pack() after having executed
        // TableItem.setImage((Image)null) for other rows than search/filter row.
        boolean isCollapseFilter = fTable.getData(Key.FILTER_OBJ) instanceof TmfCollapseFilter;
        if (IS_LINUX && (i == 0) && isCollapseFilter) {
            column.setWidth(column.getWidth() + SEARCH_IMAGE.getBounds().width);
        }

        if (column.getWidth() < headerWidth) {
            column.setWidth(headerWidth);
        }
    }

    /**
     * Get the array of item strings (e.g., what to display in each cell of the
     * table row) corresponding to the columns and trace event passed in
     * parameter. The order of the Strings in the returned array will correspond
     * to the iteration order of 'columns'.
     *
     * <p>
     * To ensure consistent results, make sure only call this within a scope
     * synchronized on 'columns'! If the order of 'columns' changes right after
     * this method is called, the returned value won't be ordered correctly
     * anymore.
     */
    private static String[] getItemStrings(List<TmfEventTableColumn> columns, ITmfEvent event) {
        if (event == null) {
            return EMPTY_STRING_ARRAY;
        }
        synchronized (columns) {
            List<String> itemStrings = new ArrayList<>(columns.size());
            for (TmfEventTableColumn column : columns) {
                ITmfEvent passedEvent = event;
                if (!(column instanceof TmfMarginColumn) && (event instanceof CachedEvent)) {
                    // Make sure that the event object from the trace is passed
                    // to all columns but the TmfMarginColumn
                    passedEvent = ((CachedEvent) event).event;
                }
                if (passedEvent == null) {
                    itemStrings.add(EMPTY_STRING);
                } else {
                    itemStrings.add(column.getItemString(passedEvent));
                }

            }
            return itemStrings.toArray(new String[0]);
        }
    }

    /**
     * Get the contents of the row in the events table corresponding to an
     * event. The order of the elements corresponds to the current order of the
     * columns.
     *
     * @param event
     *            The event printed in this row
     * @return The event row entries
     * @since 3.0
     */
    public String[] getItemStrings(ITmfEvent event) {
        return getItemStrings(fColumns, event);
    }

    /**
     * Notify this table that is got the UI focus.
     */
    public void setFocus() {
        fTable.setFocus();
    }

    /**
     * Assign a new trace to this event table.
     *
     * @param trace
     *            The trace to assign to this event table
     * @param disposeOnClose
     *            true if the trace should be disposed when the table is
     *            disposed
     */
    public void setTrace(final ITmfTrace trace, final boolean disposeOnClose) {
        if ((fTrace != null) && fDisposeOnClose) {
            fTrace.dispose();
        }
        fTrace = trace;
        fPackDone = false;
        fSelectedRank = 0;
        fDisposeOnClose = disposeOnClose;

        // Perform the updates on the UI thread
        fTable.getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                fTable.removeAll();
                fCache.setTrace(fTrace); // Clear the cache
                if (fTrace != null) {
                    if (!fTable.isDisposed() && (fTrace != null)) {
                        if (fTable.getData(Key.FILTER_OBJ) == null) {
                            fTable.setItemCount((int) fTrace.getNbEvents() + 1); // +1 for header row
                        } else {
                            stopFilterThread();
                            fFilterMatchCount = 0;
                            fFilterCheckCount = 0;
                            fTable.setItemCount(3); // +1 for header row, +2 for top and bottom filter status rows
                            startFilterThread();
                        }
                    }
                }
                fRawViewer.setTrace(fTrace);
            }
        });
    }

    /**
     * Assign the status line manager
     *
     * @param statusLineManager
     *            The status line manager, or null to disable status line messages
     * @since 2.1
     */
    public void setStatusLineManager(IStatusLineManager statusLineManager) {
        if (fStatusLineManager != null && statusLineManager == null) {
            fStatusLineManager.setMessage(EMPTY_STRING);
        }
        fStatusLineManager = statusLineManager;
    }

    private void updateStatusLine(ITmfTimestamp delta) {
        if (fStatusLineManager != null) {
            if (delta != null) {
                fStatusLineManager.setMessage("\u0394: " + delta); //$NON-NLS-1$
            } else {
                fStatusLineManager.setMessage(null);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Event cache
    // ------------------------------------------------------------------------

    /**
     * Notify that the event cache has been updated
     *
     * @param completed
     *            Also notify if the populating of the cache is complete, or
     *            not.
     */
    public void cacheUpdated(final boolean completed) {
        synchronized (fCacheUpdateSyncObj) {
            if (fCacheUpdateBusy) {
                fCacheUpdatePending = true;
                fCacheUpdateCompleted = completed;
                return;
            }
            fCacheUpdateBusy = true;
        }
        // Event cache is now updated. Perform update on the UI thread
        if (!fTable.isDisposed()) {
            fTable.getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (!fTable.isDisposed()) {
                        fTable.refresh();
                        packColumns();
                    }
                    if (completed) {
                        populateCompleted();
                    }
                    synchronized (fCacheUpdateSyncObj) {
                        fCacheUpdateBusy = false;
                        if (fCacheUpdatePending) {
                            fCacheUpdatePending = false;
                            cacheUpdated(fCacheUpdateCompleted);
                        }
                    }
                }
            });
        }
    }

    /**
     * Callback for when populating the table is complete.
     */
    protected void populateCompleted() {
        // Nothing by default;
    }

    // ------------------------------------------------------------------------
    // ISelectionProvider
    // ------------------------------------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        selectionChangedListeners.add(listener);
    }

    /**
     * @since 2.0
     */
    @Override
    public ISelection getSelection() {
        if (fTable == null || fTable.isDisposed()) {
            return StructuredSelection.EMPTY;
        }
        List<Object> list = new ArrayList<>(fTable.getSelection().length);
        for (TableItem item : fTable.getSelection()) {
            if (item.getData() != null) {
                list.add(item.getData());
            }
        }
        return new StructuredSelection(list);
    }

    /**
     * @since 2.0
     */
    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        selectionChangedListeners.remove(listener);
    }

    /**
     * @since 2.0
     */
    @Override
    public void setSelection(ISelection selection) {
        // not implemented
    }

    /**
     * Notifies any selection changed listeners that the viewer's selection has changed.
     * Only listeners registered at the time this method is called are notified.
     *
     * @param event a selection changed event
     *
     * @see ISelectionChangedListener#selectionChanged
     * @since 2.0
     */
    protected void fireSelectionChanged(final SelectionChangedEvent event) {
        Object[] listeners = selectionChangedListeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
            SafeRunnable.run(new SafeRunnable() {
                @Override
                public void run() {
                    l.selectionChanged(event);
                }
            });
        }
    }

    // ------------------------------------------------------------------------
    // Bookmark handling
    // ------------------------------------------------------------------------

    /**
     * Add a bookmark to this event table.
     *
     * @param bookmarksFile
     *            The file to use for the bookmarks
     */
    public void addBookmark(final IFile bookmarksFile) {
        fBookmarksFile = bookmarksFile;
        final TableItem[] selection = fTable.getSelection();
        if (selection.length > 0) {
            final TableItem tableItem = selection[0];
            if (tableItem.getData(Key.RANK) != null) {
                final StringBuffer defaultMessage = new StringBuffer();
                for (int i = 0; i < fTable.getColumns().length; i++) {
                    if (i > 0) {
                        defaultMessage.append(", "); //$NON-NLS-1$
                    }
                    defaultMessage.append(tableItem.getText(i));
                }
                final InputDialog dialog = new MultiLineInputDialog(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        Messages.TmfEventsTable_AddBookmarkDialogTitle,
                        Messages.TmfEventsTable_AddBookmarkDialogMessage,
                        defaultMessage.toString());
                if (dialog.open() == Window.OK) {
                    final String message = dialog.getValue();
                    try {
                        final IMarker bookmark = bookmarksFile.createMarker(IMarker.BOOKMARK);
                        if (bookmark.exists()) {
                            bookmark.setAttribute(IMarker.MESSAGE, message.toString());
                            final Long rank = (Long) tableItem.getData(Key.RANK);
                            final int location = rank.intValue();
                            bookmark.setAttribute(IMarker.LOCATION, Integer.valueOf(location));
                            fBookmarksMap.put(rank, bookmark.getId());
                            fTable.refresh();
                        }
                    } catch (final CoreException e) {
                        displayException(e);
                    }
                }
            }
        }

    }

    /**
     * Remove a bookmark from this event table.
     *
     * @param bookmark
     *            The bookmark to remove
     */
    public void removeBookmark(final IMarker bookmark) {
        for (final Entry<Long, Long> entry : fBookmarksMap.entries()) {
            if (entry.getValue().equals(bookmark.getId())) {
                fBookmarksMap.remove(entry.getKey(), entry.getValue());
                fTable.refresh();
                return;
            }
        }
    }

    private void toggleBookmark(final Long rank) {
        if (fBookmarksFile == null) {
            return;
        }
        if (fBookmarksMap.containsKey(rank)) {
            final Collection<Long> markerIds = fBookmarksMap.removeAll(rank);
            fTable.refresh();
            try {
                for (long markerId : markerIds) {
                    final IMarker bookmark = fBookmarksFile.findMarker(markerId);
                    if (bookmark != null) {
                        bookmark.delete();
                    }
                }
            } catch (final CoreException e) {
                displayException(e);
            }
        } else {
            addBookmark(fBookmarksFile);
        }
    }

    /**
     * Refresh the bookmarks assigned to this trace, from the contents of a
     * bookmark file.
     *
     * @param bookmarksFile
     *            The bookmark file to use
     */
    public void refreshBookmarks(final IFile bookmarksFile) {
        fBookmarksFile = bookmarksFile;
        if (bookmarksFile == null) {
            fBookmarksMap.clear();
            fTable.refresh();
            return;
        }
        try {
            fBookmarksMap.clear();
            for (final IMarker bookmark : bookmarksFile.findMarkers(IMarker.BOOKMARK, false, IResource.DEPTH_ZERO)) {
                final int location = bookmark.getAttribute(IMarker.LOCATION, -1);
                if (location != -1) {
                    final long rank = location;
                    fBookmarksMap.put(rank, bookmark.getId());
                }
            }
            fTable.refresh();
        } catch (final CoreException e) {
            displayException(e);
        }
    }

    @Override
    public void gotoMarker(final IMarker marker) {
        final int rank = marker.getAttribute(IMarker.LOCATION, -1);
        if (rank != -1) {
            int index = rank;
            if (fTable.getData(Key.FILTER_OBJ) != null) {
                index = fCache.getFilteredEventIndex(rank) + 1; // +1 for top filter status row
            } else if (rank >= fTable.getItemCount()) {
                fPendingGotoRank = rank;
            }
            fSelectedRank = rank;
            fTable.setSelection(index + 1); // +1 for header row
            updateStatusLine(null);
        }
    }

    // ------------------------------------------------------------------------
    // Listeners
    // ------------------------------------------------------------------------

    @Override
    public void colorSettingsChanged(final ColorSetting[] colorSettings) {
        fTable.refresh();
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handler for the trace updated signal
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void traceUpdated(final TmfTraceUpdatedSignal signal) {
        if ((signal.getTrace() != fTrace) || fTable.isDisposed()) {
            return;
        }
        // Perform the refresh on the UI thread
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (!fTable.isDisposed() && (fTrace != null)) {
                    if (fTable.getData(Key.FILTER_OBJ) == null) {
                        fTable.setItemCount((int) fTrace.getNbEvents() + 1); // +1 for header row
                        if ((fPendingGotoRank != -1) && ((fPendingGotoRank + 1) < fTable.getItemCount())) { // +1 for header row
                            fTable.setSelection((int) fPendingGotoRank + 1); // +1 for header row
                            fPendingGotoRank = -1;
                            updateStatusLine(null);
                        }
                    } else {
                        startFilterThread();
                    }
                }
                if (!fRawViewer.isDisposed() && (fTrace != null)) {
                    fRawViewer.refreshEventCount();
                }
            }
        });
    }

    /**
     * Handler for the time synch signal.
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void currentTimeUpdated(final TmfTimeSynchSignal signal) {
        if ((signal.getSource() != this) && (fTrace != null) && (!fTable.isDisposed())) {

            // Create a request for one event that will be queued after other ongoing requests. When this request is completed
            // do the work to select the actual event with the timestamp specified in the signal. This procedure prevents
            // the method fTrace.getRank() from interfering and delaying ongoing requests.
            final TmfEventRequest subRequest = new TmfEventRequest(ITmfEvent.class,
                    TmfTimeRange.ETERNITY, 0, 1, ExecutionType.FOREGROUND) {

                TmfTimestamp ts = new TmfTimestamp(signal.getBeginTime());

                @Override
                public void handleData(final ITmfEvent event) {
                    super.handleData(event);
                }

                @Override
                public void handleCompleted() {
                    super.handleCompleted();
                    if (fTrace == null) {
                        return;
                    }

                    // Verify if the event is within the trace range and adjust if necessary
                    ITmfTimestamp timestamp = ts;
                    if (timestamp.compareTo(fTrace.getStartTime(), true) == -1) {
                        timestamp = fTrace.getStartTime();
                    }
                    if (timestamp.compareTo(fTrace.getEndTime(), true) == 1) {
                        timestamp = fTrace.getEndTime();
                    }

                    // Get the rank of the selected event in the table
                    final ITmfContext context = fTrace.seekEvent(timestamp);
                    final long rank = context.getRank();
                    context.dispose();
                    fSelectedRank = rank;

                    fTable.getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            // Return if table is disposed
                            if (fTable.isDisposed()) {
                                return;
                            }

                            int index = (int) rank;
                            if (fTable.isDisposed()) {
                                return;
                            }
                            if (fTable.getData(Key.FILTER_OBJ) != null) {
                                index = fCache.getFilteredEventIndex(rank) + 1; // +1 for top filter status row
                            }
                            fTable.setSelection(index + 1); // +1 for header row
                            fRawViewer.selectAndReveal(rank);
                            updateStatusLine(null);
                        }
                    });
                }
            };

            ((ITmfEventProvider) fTrace).sendRequest(subRequest);
        }
    }

    // ------------------------------------------------------------------------
    // Error handling
    // ------------------------------------------------------------------------

    /**
     * Display an exception in a message box
     *
     * @param e the exception
     */
    private static void displayException(final Exception e) {
        final MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        mb.setText(e.getClass().getSimpleName());
        mb.setMessage(e.getMessage());
        mb.open();
    }

    /**
     * @since 2.0
     */
    public void refresh() {
        fCache.clear();
        fTable.refresh();
        fTable.redraw();
    }

    /**
    * Margin column for images and special text (e.g. collapse count)
    */
   private static final class TmfMarginColumn extends TmfEventTableColumn {

       private static final @NonNull String HEADER = EMPTY_STRING;

       /**
        * Constructor
        */
       public TmfMarginColumn() {
           super(HEADER);
       }

       @Override
       public String getItemString(ITmfEvent event) {
           if (!(event instanceof CachedEvent) || ((CachedEvent) event).repeatCount == 0) {
               return EMPTY_STRING;
           }
           return "+" + ((CachedEvent) event).repeatCount; //$NON-NLS-1$
       }

       @Override
       public String getFilterFieldId() {
           return null;
       }
   }

}
