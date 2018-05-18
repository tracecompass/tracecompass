/*******************************************************************************
 * Copyright (c) 2010, 2018 Ericsson and others.
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
 *   Matthew Khouzam - Add hide columns
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.events;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
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
import org.eclipse.swt.graphics.GC;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.core.filter.TmfCollapseFilter;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.internal.tmf.ui.commands.CopyToClipboardOperation;
import org.eclipse.tracecompass.internal.tmf.ui.commands.ExportToTextCommandHandler;
import org.eclipse.tracecompass.internal.tmf.ui.dialogs.AddBookmarkDialog;
import org.eclipse.tracecompass.tmf.core.component.ITmfEventProvider;
import org.eclipse.tracecompass.tmf.core.component.TmfComponent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.collapse.ITmfCollapsibleEvent;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfModelLookup;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfSourceLookup;
import org.eclipse.tracecompass.tmf.core.filter.FilterManager;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterMatchesNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterObjectNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterRootNode;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.resources.ITmfMarker;
import org.eclipse.tracecompass.tmf.core.signal.TmfEventFilterAppliedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfEventSearchAppliedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfEventSelectedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.tracecompass.tmf.core.util.Pair;
import org.eclipse.tracecompass.tmf.ui.TmfUiRefreshHandler;
import org.eclipse.tracecompass.tmf.ui.viewers.events.TmfEventsCache.CachedEvent;
import org.eclipse.tracecompass.tmf.ui.viewers.events.TmfEventsTableHeader.IEventsTableHeaderListener;
import org.eclipse.tracecompass.tmf.ui.viewers.events.columns.TmfEventTableColumn;
import org.eclipse.tracecompass.tmf.ui.views.colors.ColorSetting;
import org.eclipse.tracecompass.tmf.ui.views.colors.ColorSettingsManager;
import org.eclipse.tracecompass.tmf.ui.views.colors.IColorSettingsListener;
import org.eclipse.tracecompass.tmf.ui.widgets.rawviewer.TmfRawEventViewer;
import org.eclipse.tracecompass.tmf.ui.widgets.virtualtable.TmfVirtualTable;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.themes.ColorUtil;
import org.eclipse.ui.themes.IThemeManager;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * The generic TMF Events table
 *
 * This is a view that will list events that are read from a trace.
 *
 * @author Francois Chouinard
 * @author Patrick Tasse
 */
public class TmfEventsTable extends TmfComponent implements IGotoMarker, IColorSettingsListener, ISelectionProvider, IPropertyChangeListener {

    /**
     * Empty string array, used by {@link #getItemStrings}.
     */
    protected static final String @NonNull [] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Empty string
     */
    protected static final @NonNull String EMPTY_STRING = ""; //$NON-NLS-1$

    private static final boolean IS_LINUX = System.getProperty("os.name").contains("Linux") ? true : false; //$NON-NLS-1$ //$NON-NLS-2$
    private static final boolean IS_WIN32 = SWT.getPlatform().equals("win32"); //$NON-NLS-1$

    private static final String FONT_DEFINITION_ID = "org.eclipse.tracecompass.tmf.ui.font.eventtable"; //$NON-NLS-1$
    private static final String HIGHLIGHT_COLOR_DEFINITION_ID = "org.eclipse.tracecompass.tmf.ui.color.eventtable.highlight"; //$NON-NLS-1$

    private static final Image BOOKMARK_IMAGE = Activator.getDefault().getImageFromPath(
            "icons/elcl16/bookmark_obj.gif"); //$NON-NLS-1$
    private static final Image SEARCH_IMAGE = Activator.getDefault().getImageFromPath("icons/elcl16/search.gif"); //$NON-NLS-1$
    private static final Image SEARCH_MATCH_IMAGE = Activator.getDefault().getImageFromPath(
            "icons/elcl16/search_match.gif"); //$NON-NLS-1$
    private static final Image SEARCH_MATCH_BOOKMARK_IMAGE = Activator.getDefault().getImageFromPath(
            "icons/elcl16/search_match_bookmark.gif"); //$NON-NLS-1$
    private static final Image FILTER_IMAGE = Activator.getDefault().getImageFromPath("icons/elcl16/filter_items.gif"); //$NON-NLS-1$
    private static final Image FILTER_ADD_IMAGE = Activator.getDefault().getImageFromPath("icons/elcl16/filter_add.gif"); //$NON-NLS-1$
    private static final Image STOP_IMAGE = Activator.getDefault().getImageFromPath("icons/elcl16/stop.gif"); //$NON-NLS-1$
    private static final String SEARCH_HINT = Messages.TmfEventsTable_SearchHint;
    private static final int MAX_CACHE_SIZE = 1000;

    private static final int MARGIN_COLUMN_INDEX = 0;
    private static final int FILTER_SUMMARY_INDEX = 1;
    private static final int EVENT_COLUMNS_START_INDEX = MARGIN_COLUMN_INDEX + 1;

    private final ISchedulingRule fTimeSelectMutexRule = new ISchedulingRule() {
        @Override
        public boolean isConflicting(ISchedulingRule rule) {
            return (rule == this);
        }

        @Override
        public boolean contains(ISchedulingRule rule) {
            return (rule == this);
        }
    };

    private Job fTimeSelectJob = null;

    private final class ColumnListener extends ControlAdapter {
        /*
         * Make sure that the margin column is always first and keep the column
         * order variable up to date.
         */
        @Override
        public void controlMoved(ControlEvent e) {
            int[] order = fTable.getColumnOrder();
            if (order[0] != MARGIN_COLUMN_INDEX) {
                for (int i = order.length - 1; i > 0; i--) {
                    if (order[i] == MARGIN_COLUMN_INDEX) {
                        order[i] = order[i - 1];
                        order[i - 1] = MARGIN_COLUMN_INDEX;
                    }
                }
                fTable.setColumnOrder(order);
            }
            fColumnOrder = order;
            fTable.layout();
        }

        @Override
        public void controlResized(ControlEvent e) {
            TableColumn column = (TableColumn) e.widget;
            if (fPacking) {
                /* Don't update column width if resize due to packing */
                return;
            }
            if (column.getResizable() && !isExpanded(column)) {
                int index = (int) column.getData(Key.INDEX);
                fColumnSize[index] = column.getWidth();
                /* Turns off AutoFit */
                column.setData(Key.WIDTH, fColumnSize[index]);
            }
        }
    }

    private final class TableSelectionListener extends SelectionAdapter {
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
                } else {
                    fSelectedRank = -1;
                }
                if (fTable.getSelectionIndices().length == 1) {
                    fSelectedBeginRank = fSelectedRank;
                }
                if (e.item.getData(Key.TIMESTAMP) instanceof ITmfTimestamp) {
                    final ITmfTimestamp ts = NonNullUtils.checkNotNull((ITmfTimestamp) e.item.getData(Key.TIMESTAMP));
                    if (fTable.getSelectionIndices().length == 1) {
                        fSelectedBeginTimestamp = ts;
                    }
                    ITmfTimestamp selectedBeginTimestamp = fSelectedBeginTimestamp;
                    if (selectedBeginTimestamp != null) {
                        broadcast(new TmfSelectionRangeUpdatedSignal(TmfEventsTable.this, selectedBeginTimestamp, ts, fTrace));
                        if (fTable.getSelectionIndices().length == 2) {
                            updateStatusLine(ts.getDelta(selectedBeginTimestamp));
                        }
                    }
                } else {
                    if (fTable.getSelectionIndices().length == 1) {
                        fSelectedBeginTimestamp = null;
                    }
                }
            }
            if (e.item.getData() instanceof ITmfEvent) {
                broadcast(new TmfEventSelectedSignal(TmfEventsTable.this, (ITmfEvent) e.item.getData()));
                fireSelectionChanged(new SelectionChangedEvent(TmfEventsTable.this, new StructuredSelection(e.item.getData())));
            } else {
                fireSelectionChanged(new SelectionChangedEvent(TmfEventsTable.this, StructuredSelection.EMPTY));
            }
        }
    }

    private final class MouseDoubleClickListener extends MouseAdapter {
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
    }

    private final class RawSelectionListener implements Listener {
        @Override
        public void handleEvent(final Event e) {
            if (fTrace == null) {
                return;
            }
            long rank;
            if (e.data instanceof Long) {
                rank = (Long) e.data;
            } else if (e.data instanceof ITmfLocation) {
                rank = findRank((ITmfLocation) e.data);
            } else {
                return;
            }
            int index = (int) rank;
            if (fTable.getData(Key.FILTER_OBJ) != null) {
                // +1 for top filter status row
                index = fCache.getFilteredEventIndex(rank) + 1;
            }
            // +1 for header row
            fTable.setSelection(index + 1);
            fSelectedRank = rank;
            fSelectedBeginRank = fSelectedRank;
            updateStatusLine(null);
            final TableItem[] selection = fTable.getSelection();
            if ((selection != null) && (selection.length > 0)) {
                TableItem item = fTable.getSelection()[0];
                final TmfTimestamp ts = (TmfTimestamp) item.getData(Key.TIMESTAMP);
                if (ts != null) {
                    broadcast(new TmfSelectionRangeUpdatedSignal(TmfEventsTable.this, ts, ts, fTrace));
                }
                if (item.getData() instanceof ITmfEvent) {
                    broadcast(new TmfEventSelectedSignal(TmfEventsTable.this, (ITmfEvent) item.getData()));
                    fireSelectionChanged(new SelectionChangedEvent(TmfEventsTable.this, new StructuredSelection(item.getData())));
                } else {
                    fireSelectionChanged(new SelectionChangedEvent(TmfEventsTable.this, StructuredSelection.EMPTY));
                }
            }
        }

        private long findRank(final ITmfLocation selectedLocation) {
            final double selectedRatio = fTrace.getLocationRatio(selectedLocation);
            long low = 0;
            long high = fTrace.getNbEvents();
            long rank = high / 2;
            double ratio = -1;
            while (ratio != selectedRatio) {
                ITmfContext context = fTrace.seekEvent(rank);
                ratio = fTrace.getLocationRatio(context.getLocation());
                context.dispose();
                if (ratio < selectedRatio) {
                    low = rank;
                    rank = (rank + high) / 2;
                } else if (ratio > selectedRatio) {
                    high = rank;
                    rank = (rank + low) / 2;
                }
                if ((high - low) < 2) {
                    break;
                }
            }
            return rank;
        }
    }

    private final class SetDataListener implements Listener {
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
                /* -1 for top filter status row */
                index = index - 1;
            }

            final CachedEvent cachedEvent = fCache.getEvent(index);
            if (cachedEvent != null) {
                setItemData(item, cachedEvent, cachedEvent.rank);
                return;
            }

            // Else, fill the cache asynchronously (and off the UI thread)
            event.doit = false;
        }
    }

    private static final class PainItemListener implements Listener {
        @Override
        public void handleEvent(Event event) {
            TableItem item = (TableItem) event.item;

            // we promised to paint the table item's foreground
            GC gc = event.gc;
            Image image = item.getImage(event.index);
            if (image != null) {
                Rectangle imageBounds = item.getImageBounds(event.index);
                /*
                 * The image bounds don't match the default image position.
                 */
                if (IS_LINUX) {
                    gc.drawImage(image, imageBounds.x + 1, imageBounds.y + 3);
                } else {
                    gc.drawImage(image, imageBounds.x, imageBounds.y + 1);
                }
            }
            gc.setForeground(item.getForeground(event.index));
            gc.setFont(item.getFont(event.index));
            String text = item.getText(event.index);
            Rectangle textBounds = item.getTextBounds(event.index);
            /*
             * The text bounds don't match the default text position.
             */
            if (IS_LINUX) {
                gc.drawText(text, textBounds.x + 1, textBounds.y + 3, true);
            } else {
                gc.drawText(text, textBounds.x - 1, textBounds.y + 2, true);
            }
        }
    }

    private static final class EraseItemListener implements Listener {
        @Override
        public void handleEvent(Event event) {
            TableItem item = (TableItem) event.item;
            List<?> styleRanges = (List<?>) item.getData(Key.STYLE_RANGES);

            GC gc = event.gc;
            Color background = item.getBackground(event.index);
            /*
             * Paint the background if it is not the default system color. In
             * Windows, if you let the widget draw the background, it will not
             * show the item's background color if the item is selected or hot.
             * If there are no style ranges and the item background is the
             * default system color, we do not want to paint it or otherwise we
             * would override the platform theme (e.g. alternating colors).
             */
            if (styleRanges != null || !background.equals(item.getParent().getBackground())) {
                // we will paint the table item's background
                event.detail &= ~SWT.BACKGROUND;

                // paint the item's default background
                gc.setBackground(background);
                gc.fillRectangle(event.x, event.y, event.width, event.height);
            }

            /*
             * We will paint the table item's foreground. In Windows, if you
             * paint the background but let the widget draw the foreground, it
             * will override your background, unless the item is selected or
             * hot.
             */
            event.detail &= ~SWT.FOREGROUND;

            // paint the highlighted background for all style ranges
            if (styleRanges != null) {
                Rectangle textBounds = item.getTextBounds(event.index);
                String text = item.getText(event.index);
                for (Object o : styleRanges) {
                    if (o instanceof StyleRange) {
                        StyleRange styleRange = (StyleRange) o;
                        if (styleRange.data.equals(event.index)) {
                            int startIndex = styleRange.start;
                            int endIndex = startIndex + styleRange.length;
                            int startX = gc.textExtent(text.substring(0, startIndex)).x;
                            int endX = gc.textExtent(text.substring(0, endIndex)).x;
                            gc.setBackground(styleRange.background);
                            gc.fillRectangle(textBounds.x + startX, textBounds.y, (endX - startX), textBounds.height);
                        }
                    }
                }
            }
        }
    }

    private final class TooltipListener implements Listener {
        Shell tooltipShell = null;

        @Override
        public void handleEvent(final Event event) {
            switch (event.type) {
            case SWT.MouseHover:
                final TableItem item = fTable.getItem(new Point(event.x, event.y));
                if (item == null) {
                    return;
                }
                String text;
                if (fTable.indexOf(item) == 0) {
                    if (fHeaderState == HeaderState.SEARCH && item.getBounds(0).contains(event.x, event.y)) {
                        text = Messages.TmfEventsTable_AddAsFilterText;
                    } else {
                        return;
                    }
                } else {
                    final Long rank = (Long) item.getData(Key.RANK);
                    if (rank == null) {
                        return;
                    }
                    final String tooltipText = (String) item.getData(Key.BOOKMARK);
                    final Rectangle bounds = item.getImageBounds(0);
                    bounds.width = BOOKMARK_IMAGE.getBounds().width;
                    if (!bounds.contains(event.x, event.y)) {
                        return;
                    }
                    text = rank.toString() + (tooltipText != null ? ": " + tooltipText : EMPTY_STRING); //$NON-NLS-1$
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
                label.setForeground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
                label.setBackground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
                label.setText(text);
                label.addListener(SWT.MouseExit, this);
                label.addListener(SWT.MouseDown, this);
                label.addListener(SWT.MouseWheel, this);
                final Point size = tooltipShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                /*
                 * Bug in Linux. The coordinates of the event have an origin
                 * that excludes the table header but the method toDisplay()
                 * expects coordinates relative to an origin that includes the
                 * table header.
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
    }

    /**
     * The events table search/filter/data keys
     *
     * @author Patrick Tasse
     * @noimplement This interface only contains Event Table specific static
     *              definitions.
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

        /** Bookmark indicator */
        String BOOKMARK = "$bookmark"; //$NON-NLS-1$

        /** Event aspect represented by this column */
        String ASPECT = "$aspect"; //$NON-NLS-1$

        /**
         * Table item list of style ranges
         *
         * @since 1.0
         */
        String STYLE_RANGES = "$style_ranges"; //$NON-NLS-1$

        /**
         * The width of a table item
         *
         * @since 1.1
         */
        String WIDTH = "$width"; //$NON-NLS-1$

        /**
         * The position of the column
         *
         * @since 2.1
         */
        String INDEX = "$index"; //$NON-NLS-1$
    }

    /**
     * The events table search/filter state
     *
     * @version 1.0
     * @author Patrick Tasse
     */
    public static enum HeaderState {
        /**
         * No search filter is applied
         *
         * @since 2.0
         */
        NO_SEARCH,

        /** A search filter is applied */
        SEARCH
    }

    interface Direction {
        int FORWARD = +1;
        int BACKWARD = -1;
    }

    // ------------------------------------------------------------------------
    // Table data
    // ------------------------------------------------------------------------

    /** The header bar */
    private TmfEventsTableHeader fHeaderBar;

    /** The virtual event table */
    protected TmfVirtualTable fTable;

    private Composite fComposite;
    private SashForm fSashForm;
    private Composite fTableComposite;
    private TmfRawEventViewer fRawViewer;
    private ITmfTrace fTrace;
    private volatile boolean fPacking = false;
    private volatile boolean fPackDone = false;
    private volatile boolean fPackMarginDone = false;
    private HeaderState fHeaderState = HeaderState.NO_SEARCH;
    private long fSelectedRank = -1;
    private long fSelectedBeginRank = -1;
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
    private boolean fCollapseFilterEnabled = false;

    /**
     * List of selection change listeners (element type:
     * <code>ISelectionChangedListener</code>).
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
    private Color fHighlightColor;
    private Font fFont;
    private Font fBoldFont;

    private final List<TmfEventTableColumn> fColumns = new LinkedList<>();

    // Event cache
    private final TmfEventsCache fCache;
    private boolean fCacheUpdateBusy = false;
    private boolean fCacheUpdatePending = false;
    private boolean fCacheUpdateCompleted = false;
    private final Object fCacheUpdateSyncObj = new Object();

    // Keep track of column order, it is needed after table is disposed
    private int[] fColumnOrder;

    private boolean fDisposeOnClose;

    private Menu fHeaderMenu;

    private Menu fTablePopup;

    private Menu fRawTablePopup;

    private Point fLastMenuCursorLocation;
    private MenuManager fRawViewerPopupMenuManager;
    private MenuManager fTablePopupMenuManager;
    private MenuManager fHeaderPopupMenuManager;

    private boolean[] fColumnResizable;

    private int[] fColumnSize;

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
        this(parent, cacheSize, TmfTrace.BASE_ASPECTS);
    }

    /**
     * Standard constructor, where we define which columns to use.
     *
     * @param parent
     *            The parent composite UI object
     * @param cacheSize
     *            The size of the event table cache
     * @param aspects
     *            The event aspects to display in this table. One column per
     *            aspect will be created.
     *            <p>
     *            The iteration order of this collection will correspond to the
     *            initial ordering of the columns in the table.
     *            </p>
     */
    public TmfEventsTable(final Composite parent, int cacheSize,
            @NonNull Iterable<ITmfEventAspect<?>> aspects) {
        super("TmfEventsTable"); //$NON-NLS-1$

        fComposite = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.verticalSpacing = 0;
        fComposite.setLayout(gl);

        fSashForm = new SashForm(fComposite, SWT.HORIZONTAL);
        fSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Create a composite for the table and its header bar
        fTableComposite = new Composite(fSashForm, SWT.NONE);
        fTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.verticalSpacing = 0;
        fTableComposite.setLayout(gl);

        // Create an events table header bar
        fHeaderBar = new TmfEventsTableHeader(fTableComposite, SWT.NONE, new IEventsTableHeaderListener() {
            @Override
            public void filterSelected(ITmfFilter filter) {
                if (filter instanceof TmfFilterMatchesNode) {
                    TmfFilterMatchesNode matchFilter = (TmfFilterMatchesNode) filter;
                    for (TableColumn col : fTable.getColumns()) {
                        if (col.getData(Key.ASPECT) == matchFilter.getEventAspect()) {
                            col.setData(Key.FILTER_TXT, matchFilter.getRegex());
                        } else {
                            col.setData(Key.FILTER_TXT, null);
                        }
                    }
                    fTable.refresh();
                    fTable.redraw();
                }
            }

            @Override
            public void filterRemoved(ITmfFilter filter) {
                for (TableColumn col : fTable.getColumns()) {
                    col.setData(Key.FILTER_TXT, null);
                }
                removeFilter(filter);
            }
        });

        // Create a virtual table
        final int style = SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION;
        fTable = new TmfVirtualTable(fTableComposite, style);

        // Set the table layout
        final GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        fTable.setLayoutData(layoutData);

        // Some cosmetic enhancements
        fTable.setHeaderVisible(true);
        fTable.setLinesVisible(true);

        // Setup the columns
        for (ITmfEventAspect<?> aspect : aspects) {
            if (aspect != null) {
                fColumns.add(new TmfEventTableColumn(aspect));

            }
        }

        TmfMarginColumn collapseCol = new TmfMarginColumn();
        fColumns.add(MARGIN_COLUMN_INDEX, collapseCol);

        fHeaderMenu = new Menu(fTable);

        fColumnSize = new int[fColumns.size()];
        fColumnResizable = new boolean[fColumns.size()];
        int index = 0;
        // Create the UI columns in the table
        for (TmfEventTableColumn col : fColumns) {
            TableColumn column = fTable.newTableColumn(SWT.LEFT);
            column.setText(col.getHeaderName());
            column.setToolTipText(col.getHeaderTooltip());
            column.setData(Key.ASPECT, col.getEventAspect());
            column.setData(Key.INDEX, index);
            if (col instanceof TmfMarginColumn) {
                column.setResizable(false);
            } else {
                column.pack();
                column.setMoveable(true);
                column.setData(Key.WIDTH, SWT.DEFAULT);
                if (col.getEventAspect().isHiddenByDefault()) {
                    column.setWidth(0);
                    column.setResizable(false);
                }
            }
            column.addControlListener(new ColumnListener());
            fColumnSize[index] = SWT.DEFAULT;
            fColumnResizable[index] = column.getResizable();
            index++;
        }
        fColumnOrder = fTable.getColumnOrder();

        // Set the frozen row for header row
        fTable.setFrozenRowCount(1);

        // Create the header row cell editor
        createHeaderEditor();

        // Handle the table item selection
        fTable.addSelectionListener(new TableSelectionListener());

        int realCacheSize = Math.max(cacheSize, Display.getDefault().getBounds().height / fTable.getItemHeight());
        realCacheSize = Math.min(realCacheSize, MAX_CACHE_SIZE);
        fCache = new TmfEventsCache(realCacheSize, this);

        // Handle the table item requests
        fTable.addListener(SWT.SetData, new SetDataListener());

        fTable.addMenuDetectListener(event -> {
            fLastMenuCursorLocation = new Point(event.x, event.y);
            Point pt = fTable.getDisplay().map(null, fTable, fLastMenuCursorLocation);
            Rectangle clientArea = fTable.getClientArea();
            boolean header = clientArea.y <= pt.y && pt.y < (clientArea.y + fTable.getHeaderHeight());
            fTable.setMenu(header ? fHeaderMenu : fTablePopup);
        });

        fTable.addMouseListener(new MouseDoubleClickListener());

        final Listener tooltipListener = new TooltipListener();

        fTable.addListener(SWT.MouseHover, tooltipListener);
        fTable.addListener(SWT.Dispose, tooltipListener);
        fTable.addListener(SWT.KeyDown, tooltipListener);
        fTable.addListener(SWT.MouseMove, tooltipListener);
        fTable.addListener(SWT.MouseExit, tooltipListener);
        fTable.addListener(SWT.MouseDown, tooltipListener);
        fTable.addListener(SWT.MouseWheel, tooltipListener);

        fTable.addListener(SWT.EraseItem, new EraseItemListener());

        fTable.addListener(SWT.PaintItem, new PainItemListener());

        // Create resources
        createResources();

        initializeFonts();
        initializeColors();
        PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(this);

        ColorSettingsManager.addColorSettingsListener(this);

        fTable.setItemCount(1); // +1 for header row

        fRawViewer = new TmfRawEventViewer(fSashForm, SWT.H_SCROLL | SWT.V_SCROLL);

        fRawViewer.addSelectionListener(new RawSelectionListener());

        fSashForm.setWeights(new int[] { 1, 1 });
        fRawViewer.setVisible(false);

        createPopupMenu();

        fComposite.addDisposeListener((e) -> {
            internalDispose();
        });
    }

    private IAction createAutoFitAction(TableColumn column) {
        final IAction autoFitAction = new Action(Messages.TmfEventsTable_AutoFit, IAction.AS_CHECK_BOX) {
            @Override
            public void run() {
                boolean isChecked = isChecked();
                int index = (int) column.getData(Key.INDEX);
                if (isChecked) {
                    fPacking = true;
                    column.pack();
                    fPacking = false;
                    column.setData(Key.WIDTH, SWT.DEFAULT);
                    fColumnSize[index] = SWT.DEFAULT;
                } else {
                    fColumnSize[index] = column.getWidth();
                    column.setData(Key.WIDTH, fColumnSize[index]);
                }
            }
        };
        autoFitAction.setChecked(Objects.equals(column.getData(Key.WIDTH), SWT.DEFAULT));
        return autoFitAction;
    }

    private IAction createShowColumnAction(final TableColumn column) {
        final IAction columnMenuAction = new Action(column.getText(), IAction.AS_CHECK_BOX) {
            @Override
            public void run() {
                boolean isChecked = isChecked();
                int index = (int) column.getData(Key.INDEX);
                if (isChecked) {
                    int width = (int) column.getData(Key.WIDTH);
                    column.setResizable(true);
                    if (width <= 0) {
                        fPacking = true;
                        column.pack();
                        fPacking = false;
                        column.setData(Key.WIDTH, SWT.DEFAULT);
                        fColumnSize[index] = SWT.DEFAULT;
                    } else {
                        column.setWidth(width);
                    }
                } else {
                    column.setResizable(false);
                    column.setWidth(0);
                }
                fColumnResizable[index] = isChecked;
                fTable.refresh();
            }
        };
        columnMenuAction.setChecked(column.getResizable());
        return columnMenuAction;
    }

    private IAction createShowAllAction() {
        return new Action(Messages.TmfEventsTable_ShowAll) {
            @Override
            public void run() {
                for (TableColumn column : fTable.getColumns()) {
                    int index = (int) column.getData(Key.INDEX);
                    if (index != MARGIN_COLUMN_INDEX) {
                        final int width = (int) column.getData(Key.WIDTH);
                        column.setResizable(true);
                        if (width <= 0) {
                            fPacking = true;
                            column.pack();
                            fPacking = false;
                            column.setData(Key.WIDTH, SWT.DEFAULT);
                            fColumnSize[index] = SWT.DEFAULT;
                        } else {
                            column.setWidth(width);
                        }
                        fColumnResizable[index] = true;
                    }
                }
                fTable.refresh();
            }
        };
    }

    private IAction createResetAllAction() {
        return new Action(Messages.TmfEventsTable_ResetAll) {
            @Override
            public void run() {
                for (int index = 0; index < fColumnOrder.length; index++) {
                    fColumnOrder[index] = index;
                }
                fTable.setColumnOrder(fColumnOrder);
                for (TableColumn column : fTable.getColumns()) {
                    int index = (int) column.getData(Key.INDEX);
                    if (index != MARGIN_COLUMN_INDEX) {
                        ITmfEventAspect<?> aspect = (ITmfEventAspect<?>) column.getData(Key.ASPECT);
                        if (aspect.isHiddenByDefault()) {
                            column.setWidth(0);
                            column.setResizable(false);
                        } else {
                            fPacking = true;
                            column.pack();
                            fPacking = false;
                            column.setResizable(true);
                        }
                        column.setData(Key.WIDTH, SWT.DEFAULT);
                        fColumnSize[index] = SWT.DEFAULT;
                        fColumnResizable[index] = column.getResizable();
                    }
                }
                fTable.refresh();
            }
        };
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Create a pop-up menu.
     */
    private void createPopupMenu() {
        final IAction copyAction = new Action(Messages.TmfEventsTable_CopyToClipboardActionText) {
            @Override
            public void run() {
                ITmfTrace trace = fTrace;
                if (trace == null || (fSelectedRank == -1 && fSelectedBeginRank == -1)) {
                    return;
                }

                List<TmfEventTableColumn> columns = new ArrayList<>();
                for (int i : fTable.getColumnOrder()) {
                    TableColumn column = fTable.getColumns()[i];
                    // Omit the margin column and hidden columns
                    if (isVisibleEventColumn(column)) {
                        columns.add(fColumns.get(i));
                    }
                }

                long start = Math.min(fSelectedBeginRank, fSelectedRank);
                long end = Math.max(fSelectedBeginRank, fSelectedRank);
                final ITmfFilter filter = (ITmfFilter) fTable.getData(Key.FILTER_OBJ);
                IRunnableWithProgress operation = new CopyToClipboardOperation(trace, filter, columns, start, end);
                try {
                    PlatformUI.getWorkbench().getProgressService().busyCursorWhile(operation);
                } catch (InvocationTargetException e) {
                    Activator.getDefault().logError("Invocation target exception copying to clipboard ", e); //$NON-NLS-1$
                } catch (InterruptedException e) {
                    /* ignored */
                }
            }
        };

        final IAction showTableAction = new Action(Messages.TmfEventsTable_ShowTableActionText) {
            @Override
            public void run() {
                fTableComposite.setVisible(true);
                fSashForm.layout();
            }
        };

        final IAction hideTableAction = new Action(Messages.TmfEventsTable_HideTableActionText) {
            @Override
            public void run() {
                fTableComposite.setVisible(false);
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
                if (!(data instanceof ITmfSourceLookup)) {
                    return;
                }
                ITmfSourceLookup event = (ITmfSourceLookup) data;
                ITmfCallsite cs = event.getCallsite();
                if (cs == null) {
                    return;
                }
                Long lineNo = cs.getLineNo();
                if (lineNo == null) {
                    /* Not enough information to provide a full callsite */
                    return;
                }

                String fileName = cs.getFileName();
                final String trimmedPath = fileName.replaceAll("\\.\\./", EMPTY_STRING); //$NON-NLS-1$
                File fileToOpen = new File(trimmedPath);

                try {
                    if (fileToOpen.exists() && fileToOpen.isFile()) {
                        /*
                         * The path points to a "real" file, attempt to open
                         * that
                         */
                        IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
                        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

                        IEditorPart editor = IDE.openEditorOnFileStore(page, fileStore);
                        if (editor instanceof ITextEditor) {
                            /*
                             * Calculate the "document offset" corresponding to
                             * the line number, then seek there.
                             */
                            ITextEditor textEditor = (ITextEditor) editor;
                            int lineNumber = lineNo.intValue();
                            IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());

                            IRegion region = document.getLineInformation(lineNumber - 1);
                            if (region != null) {
                                textEditor.selectAndReveal(region.getOffset(), region.getLength());
                            }
                        }

                    } else {
                        /*
                         * The file was not found on disk, attempt to find it in
                         * the workspace instead.
                         */
                        IMarker marker = null;
                        final ArrayList<IFile> files = new ArrayList<>();
                        IPath p = new Path(trimmedPath);
                        ResourcesPlugin.getWorkspace().getRoot().accept(new IResourceVisitor() {
                            @Override
                            public boolean visit(IResource resource) throws CoreException {
                                if (resource instanceof IFile && resource.getFullPath().toString().endsWith(p.lastSegment())) {
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
                            marker.setAttribute(IMarker.LINE_NUMBER, lineNo.intValue());
                            IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), marker);
                            marker.delete();
                        } else if (files.isEmpty()) {
                            displayException(new FileNotFoundException('\'' + cs.toString() + '\'' + '\n' + Messages.TmfEventsTable_OpenSourceCodeNotFound));
                        }
                    }
                } catch (BadLocationException | CoreException e) {
                    displayException(e);
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
                            } catch (CoreException e) {
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
                Object handlerServiceObject = activePage.getActiveEditor().getSite().getService(IHandlerService.class);
                IHandlerService handlerService = (IHandlerService) handlerServiceObject;
                Object cmdServiceObject = activePage.getActiveEditor().getSite().getService(ICommandService.class);
                ICommandService cmdService = (ICommandService) cmdServiceObject;
                try {
                    HashMap<String, Object> parameters = new HashMap<>();
                    Command command = cmdService.getCommand(ExportToTextCommandHandler.COMMAND_ID);
                    ParameterizedCommand cmd = ParameterizedCommand.generateCommand(command, parameters);

                    IEvaluationContext context = handlerService.getCurrentState();
                    List<TmfEventTableColumn> exportColumns = new ArrayList<>();
                    for (int i : fTable.getColumnOrder()) {
                        TableColumn column = fTable.getColumns()[i];
                        // Omit the margin column and hidden columns
                        if (isVisibleEventColumn(column)) {
                            exportColumns.add(fColumns.get(i));
                        }
                    }
                    context.addVariable(ExportToTextCommandHandler.TMF_EVENT_TABLE_COLUMNS_ID, exportColumns);

                    handlerService.executeCommandInContext(cmd, null, context);
                } catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
                    displayException(e);
                }
            }
        };

        final IAction addAsFilterAction = new Action(Messages.TmfEventsTable_AddAsFilterText) {
            @Override
            public void run() {
                applySearchAsFilter();
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

        final IAction synchronizeAction = new Action(Messages.TmfEventsTable_SynchronizeActionText, IAction.AS_CHECK_BOX) {
            @Override
            public void run() {
                TmfTraceManager.getInstance().updateTraceContext(NonNullUtils.checkNotNull(fTrace),
                        builder -> builder.setSynchronized(isChecked()));
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

        fHeaderPopupMenuManager = new MenuManager();
        fHeaderPopupMenuManager.setRemoveAllWhenShown(true);
        fHeaderPopupMenuManager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                final Point point = fTable.toControl(fLastMenuCursorLocation);
                TableColumn selectedColumn = fTable.getColumn(point);
                if (selectedColumn != null && selectedColumn.getResizable()) {
                    fHeaderPopupMenuManager.add(createAutoFitAction(selectedColumn));
                    fHeaderPopupMenuManager.add(new Separator());
                }
                for (int index : fTable.getColumnOrder()) {
                    TableColumn column = fTable.getColumns()[index];
                    if (column.getData(Key.WIDTH) != null) {
                        fHeaderPopupMenuManager.add(createShowColumnAction(column));
                    }
                }
                fHeaderPopupMenuManager.add(new Separator());
                fHeaderPopupMenuManager.add(createShowAllAction());
                fHeaderPopupMenuManager.add(createResetAllAction());
            }
        });

        fTablePopupMenuManager = new MenuManager();
        fTablePopupMenuManager.setRemoveAllWhenShown(true);
        fTablePopupMenuManager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(final IMenuManager manager) {
                if (fTable.getSelectionIndices().length == 1 && fTable.getSelectionIndices()[0] == 0) {
                    // Right-click on header row
                    if (fHeaderState == HeaderState.SEARCH) {
                        fTablePopupMenuManager.add(addAsFilterAction);
                    }
                    return;
                }
                final Point point = fTable.toControl(fLastMenuCursorLocation);
                final TableItem item = fTable.getSelection().length > 0 ? fTable.getSelection()[0] : null;
                if (item != null) {
                    final Rectangle imageBounds = item.getImageBounds(0);
                    imageBounds.width = BOOKMARK_IMAGE.getBounds().width;
                    if (point.x <= (imageBounds.x + imageBounds.width)) {
                        // Right-click on left margin
                        final Long rank = (Long) item.getData(Key.RANK);
                        if ((rank != null) && (fBookmarksFile != null)) {
                            if (fBookmarksMap.containsKey(rank)) {
                                fTablePopupMenuManager.add(new ToggleBookmarkAction(
                                        Messages.TmfEventsTable_RemoveBookmarkActionText, rank));
                            } else {
                                fTablePopupMenuManager.add(new ToggleBookmarkAction(
                                        Messages.TmfEventsTable_AddBookmarkActionText, rank));
                            }
                        }
                        return;
                    }
                }

                // Right-click on table
                if (fSelectedRank != -1 && fSelectedBeginRank != -1) {
                    fTablePopupMenuManager.add(copyAction);
                    fTablePopupMenuManager.add(new Separator());
                }
                if (fTable.isVisible() && fRawViewer.isVisible()) {
                    fTablePopupMenuManager.add(hideTableAction);
                    fTablePopupMenuManager.add(hideRawAction);
                } else if (!fTable.isVisible()) {
                    fTablePopupMenuManager.add(showTableAction);
                } else if (!fRawViewer.isVisible()) {
                    fTablePopupMenuManager.add(showRawAction);
                }
                fTablePopupMenuManager.add(exportToTextAction);
                fTablePopupMenuManager.add(new Separator());

                if (item != null) {
                    final Object data = item.getData();
                    Separator separator = null;
                    if (data instanceof ITmfSourceLookup) {
                        ITmfSourceLookup event = (ITmfSourceLookup) data;
                        if (event.getCallsite() != null) {
                            fTablePopupMenuManager.add(openCallsiteAction);
                            separator = new Separator();
                        }
                    }

                    if (data instanceof ITmfModelLookup) {
                        ITmfModelLookup event = (ITmfModelLookup) data;
                        if (event.getModelUri() != null) {
                            fTablePopupMenuManager.add(openModelAction);
                            separator = new Separator();
                        }

                        if (separator != null) {
                            fTablePopupMenuManager.add(separator);
                        }
                    }
                }

                /*
                 * Only show collapse filter if at least one trace can be
                 * collapsed.
                 */
                boolean isCollapsible = false;
                if (fTrace != null) {
                    for (ITmfTrace trace : TmfTraceManager.getTraceSet(fTrace)) {
                        Class<? extends ITmfEvent> eventClass = trace.getEventType();
                        isCollapsible = ITmfCollapsibleEvent.class.isAssignableFrom(eventClass);
                        if (isCollapsible) {
                            break;
                        }
                    }
                }

                if (isCollapsible && !fCollapseFilterEnabled) {
                    fTablePopupMenuManager.add(collapseAction);
                    fTablePopupMenuManager.add(new Separator());
                }

                fTablePopupMenuManager.add(clearFiltersAction);
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
                    fTablePopupMenuManager.add(subMenu);
                }
                fTablePopupMenuManager.add(new Separator());

                ITmfTrace trace = fTrace;
                if (trace != null) {
                    synchronizeAction.setChecked(TmfTraceManager.getInstance().getTraceContext(trace).isSynchronized());
                    fTablePopupMenuManager.add(synchronizeAction);
                }

                appendToTablePopupMenu(fTablePopupMenuManager, item);
            }
        });

        fRawViewerPopupMenuManager = new MenuManager();
        fRawViewerPopupMenuManager.setRemoveAllWhenShown(true);
        fRawViewerPopupMenuManager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(final IMenuManager manager) {
                if (fTable.isVisible() && fRawViewer.isVisible()) {
                    fRawViewerPopupMenuManager.add(hideTableAction);
                    fRawViewerPopupMenuManager.add(hideRawAction);
                } else if (!fTable.isVisible()) {
                    fRawViewerPopupMenuManager.add(showTableAction);
                } else if (!fRawViewer.isVisible()) {
                    fRawViewerPopupMenuManager.add(showRawAction);
                }
                appendToRawPopupMenu(fRawViewerPopupMenuManager);
            }
        });

        fHeaderMenu = fHeaderPopupMenuManager.createContextMenu(fTable);

        fTablePopup = fTablePopupMenuManager.createContextMenu(fTable);
        fTable.setMenu(fTablePopup);

        fRawTablePopup = fRawViewerPopupMenuManager.createContextMenu(fRawViewer);
        fRawViewer.setMenu(fRawTablePopup);
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
        fComposite.dispose();
    }

    private void internalDispose() {
        stopSearchThread();
        stopFilterThread();
        PlatformUI.getWorkbench().getThemeManager().removePropertyChangeListener(this);
        ColorSettingsManager.removeColorSettingsListener(this);
        fCache.clear();
        if ((fTrace != null) && fDisposeOnClose) {
            fTrace.dispose();
        }
        fResourceManager.dispose();
        if (fRawViewerPopupMenuManager != null) {
            fRawViewerPopupMenuManager.dispose();
        }
        if (fHeaderPopupMenuManager != null) {
            fHeaderPopupMenuManager.dispose();
        }
        if (fTablePopupMenuManager != null) {
            fTablePopupMenuManager.dispose();
        }

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

        // Get the actual ITmfEvent from the CachedEvent
        ITmfEvent tmfEvent = event;
        if (event instanceof CachedEvent) {
            tmfEvent = ((CachedEvent) event).event;
        }
        item.setText(itemStrings);
        item.setData(tmfEvent);
        item.setData(Key.TIMESTAMP, tmfEvent.getTimestamp());
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
                    if (marker != null) {
                        parts.add(marker.getAttribute(IMarker.MESSAGE));
                    }
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
            if (searchFilter.matches(tmfEvent)) {
                searchMatch = true;
            } else {
                searchNoMatch = true;
            }
        }

        final ColorSetting colorSetting = ColorSettingsManager.getColorSetting(tmfEvent);
        if (searchNoMatch) {
            item.setForeground(colorSetting.getDimmedForegroundColor());
            item.setBackground(colorSetting.getDimmedBackgroundColor());
        } else {
            item.setForeground(colorSetting.getForegroundColor());
            item.setBackground(colorSetting.getBackgroundColor());
        }
        item.setFont(fFont);

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

        List<StyleRange> styleRanges = new ArrayList<>();
        for (int index = 0; index < fTable.getColumns().length; index++) {
            TableColumn column = fTable.getColumns()[index];
            String regex = null;
            if (fHeaderState == HeaderState.SEARCH) {
                if (searchMatch) {
                    regex = (String) column.getData(Key.SEARCH_TXT);
                }
            } else {
                regex = (String) column.getData(Key.FILTER_TXT);
            }
            if (regex != null) {
                String text = item.getText(index);
                try {
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(text);
                    while (matcher.find()) {
                        int start = matcher.start();
                        int length = matcher.end() - start;
                        Color foreground = colorSetting.getForegroundColor();
                        Color background = fHighlightColor;
                        StyleRange styleRange = new StyleRange(start, length, foreground, background);
                        styleRange.data = index;
                        styleRanges.add(styleRange);
                    }
                } catch (PatternSyntaxException e) {
                    /* ignored */
                }
            }
        }
        if (styleRanges.isEmpty()) {
            item.setData(Key.STYLE_RANGES, null);
        } else {
            item.setData(Key.STYLE_RANGES, styleRanges);
        }
        item.getParent().redraw();

        if ((itemStrings[MARGIN_COLUMN_INDEX] != null) && !itemStrings[MARGIN_COLUMN_INDEX].isEmpty()) {
            packMarginColumn();
        }
        TmfUiRefreshHandler.getInstance().queueUpdate(this, () -> {
            if (fTable.isDisposed()) {
                return;
            }
            packColumns();
        });
    }

    /**
     * Set the item data of the header row.
     *
     * @param item
     *            The item to use as table header
     */
    protected void setHeaderRowItemData(final TableItem item) {
        if (fHeaderState == HeaderState.NO_SEARCH) {
            item.setImage(SEARCH_IMAGE);
        } else if (fHeaderState == HeaderState.SEARCH) {
            item.setImage(FILTER_ADD_IMAGE);
        }
        item.setForeground(fGrayColor);
        // Ignore collapse and image column
        for (int i = EVENT_COLUMNS_START_INDEX; i < fTable.getColumns().length; i++) {
            final TableColumn column = fTable.getColumns()[i];
            final String filter = (String) column.getData(Key.SEARCH_TXT);
            if (filter == null) {
                item.setText(i, SEARCH_HINT);
                item.setForeground(i, fGrayColor);
                item.setFont(i, fFont);
            } else {
                item.setText(i, filter);
                item.setForeground(i, fGreenColor);
                item.setFont(i, fBoldFont);
            }
        }
        if (!fPackMarginDone) {
            packMarginColumn();
            fPackMarginDone = true;
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
        item.setData(Key.STYLE_RANGES, null);
        item.setForeground(null);
        item.setBackground(null);
        item.setFont(fFont);
    }

    /**
     * Create an editor for the header.
     */
    private void createHeaderEditor() {
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

                    // Margin column selected
                    if (item.getBounds(0).contains(point)) {
                        if (fHeaderState == HeaderState.SEARCH) {
                            applySearchAsFilter();
                        }
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

                    /*
                     * The control that will be the editor must be a child of
                     * the Table
                     */
                    final Text newEditor = (Text) fTable.createTableEditorControl(Text.class);
                    final String headerString = (String) column.getData(Key.SEARCH_TXT);
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
                                if ((e.stateMask & SWT.CTRL) != 0) {
                                    applySearchAsFilter();
                                }
                                /*
                                 * Set focus on the table so that the next
                                 * carriage return goes to the next result
                                 */
                                TmfEventsTable.this.getTable().setFocus();
                            } else if (e.character == SWT.ESC) {
                                tableEditor.getEditor().dispose();
                                TmfEventsTable.this.getTable().setFocus();
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
            private boolean updateHeader(final String regex) {
                if (regex.length() > 0) {
                    try {
                        Pattern.compile(regex);
                        if (regex.equals(column.getData(Key.SEARCH_TXT))) {
                            tableEditor.getEditor().dispose();
                            return false;
                        }
                        final TmfFilterMatchesNode filter = new TmfFilterMatchesNode(null);
                        ITmfEventAspect<?> aspect = (ITmfEventAspect<?>) column.getData(Key.ASPECT);
                        filter.setEventAspect(aspect);
                        filter.setRegex(regex);
                        column.setData(Key.SEARCH_OBJ, filter);
                        column.setData(Key.SEARCH_TXT, regex);
                    } catch (final PatternSyntaxException ex) {
                        tableEditor.getEditor().dispose();
                        MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                ex.getDescription(), ex.getMessage());
                        return false;
                    }
                } else {
                    if (column.getData(Key.SEARCH_TXT) == null) {
                        tableEditor.getEditor().dispose();
                        return false;
                    }
                    column.setData(Key.SEARCH_OBJ, null);
                    column.setData(Key.SEARCH_TXT, null);
                }
                return true;
            }

            private void applyHeader() {
                stopSearchThread();
                final TmfFilterRootNode filter = new TmfFilterRootNode();
                for (final TableColumn col : fTable.getColumns()) {
                    final Object filterObj = col.getData(Key.SEARCH_OBJ);
                    if (filterObj instanceof ITmfFilterTreeNode) {
                        filter.addChild((ITmfFilterTreeNode) filterObj);
                    }
                }
                if (filter.getChildrenCount() > 0) {
                    fHeaderState = HeaderState.SEARCH;
                    fTable.setData(Key.SEARCH_OBJ, filter);
                    fTable.refresh();
                    searchNext();
                    fireSearchApplied(filter);
                } else {
                    fHeaderState = HeaderState.NO_SEARCH;
                    fTable.setData(Key.SEARCH_OBJ, null);
                    fTable.refresh();
                    fireSearchApplied(null);
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
                        fHeaderState = HeaderState.NO_SEARCH;
                        stopSearchThread();
                        for (final TableColumn column : fTable.getColumns()) {
                            column.setData(Key.SEARCH_OBJ, null);
                            column.setData(Key.SEARCH_TXT, null);
                            column.setData(Key.FILTER_TXT, null);
                        }
                        fTable.setData(Key.SEARCH_OBJ, null);
                        fTable.refresh();
                        fireSearchApplied(null);
                    } else {
                        for (final TableColumn column : fTable.getColumns()) {
                            column.setData(Key.FILTER_TXT, null);
                        }
                        fTable.refresh();
                    }
                } else if (e.character == SWT.CR) {
                    if ((e.stateMask & SWT.CTRL) != 0) {
                        if (fHeaderState == HeaderState.SEARCH) {
                            applySearchAsFilter();
                        }
                    } else if ((e.stateMask & SWT.SHIFT) == 0) {
                        searchNext();
                    } else {
                        searchPrevious();
                    }
                }
            }
        });
    }

    /**
     * Apply the current search condition as a new filter.
     *
     * @since 2.0
     */
    protected void applySearchAsFilter() {
        Object searchObj = fTable.getData(Key.SEARCH_OBJ);
        if (searchObj instanceof ITmfFilter) {
            ITmfFilter filter = (ITmfFilter) searchObj;
            fTable.setData(Key.SEARCH_OBJ, null);
            fireSearchApplied(null);
            fHeaderState = HeaderState.NO_SEARCH;
            for (final TableColumn col : fTable.getColumns()) {
                col.setData(Key.FILTER_TXT, col.getData(Key.SEARCH_TXT));
                col.setData(Key.SEARCH_TXT, null);
                col.setData(Key.SEARCH_OBJ, null);
            }
            applyFilter(filter);
        }
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
     * Apply a filter. It is added to the existing filters.
     *
     * @param filter
     *            The filter to apply
     */
    protected void applyFilter(ITmfFilter filter) {
        stopFilterThread();
        stopSearchThread();
        fFilterMatchCount = 0;
        fFilterCheckCount = 0;
        ITmfFilterTreeNode rootFilter = (ITmfFilterTreeNode) fTable.getData(Key.FILTER_OBJ);
        if (rootFilter == null) {
            rootFilter = new TmfFilterRootNode();
        }
        if (filter instanceof TmfFilterRootNode) {
            TmfFilterRootNode parentFilter = (TmfFilterRootNode) filter;
            for (ITmfFilterTreeNode child : parentFilter.getChildren()) {
                rootFilter.addChild(child);
            }
        } else if (filter instanceof TmfCollapseFilter) {
            fCollapseFilterEnabled = true;
        } else if (filter instanceof ITmfFilterTreeNode) {
            rootFilter.addChild((ITmfFilterTreeNode) filter);
        } else {
            rootFilter.addChild(new TmfFilterObjectNode(filter));
        }
        fCache.applyFilter(rootFilter, fCollapseFilterEnabled);
        fHeaderBar.addFilter(filter);
        fTable.clearAll();
        fTable.setData(Key.FILTER_OBJ, rootFilter);
        /* +1 for header row, +2 for top and bottom filter status rows */
        fTable.setItemCount(3);
        startFilterThread();
        fireFilterApplied(rootFilter);
    }

    /**
     * Remove a filter. Any other existing filters remain applied.
     *
     * @param filter
     *            The filter to remove
     * @since 2.0
     */
    protected void removeFilter(ITmfFilter filter) {
        ITmfFilterTreeNode rootFilter = (ITmfFilterTreeNode) fTable.getData(Key.FILTER_OBJ);
        if (rootFilter == null) {
            return;
        }
        stopFilterThread();
        stopSearchThread();
        fFilterMatchCount = 0;
        fFilterCheckCount = 0;
        if (filter instanceof TmfCollapseFilter) {
            fCollapseFilterEnabled = false;
        } else if (filter instanceof ITmfFilterTreeNode) {
            rootFilter.removeChild((ITmfFilterTreeNode) filter);
        } else {
            for (ITmfFilterTreeNode child : rootFilter.getChildren()) {
                if (child instanceof TmfFilterObjectNode) {
                    if (((TmfFilterObjectNode) child).getFilter().equals(filter)) {
                        rootFilter.removeChild(child);
                        break;
                    }
                }
            }
        }
        if (!rootFilter.hasChildren() && !fCollapseFilterEnabled) {
            clearFilters();
            return;
        }
        fCache.applyFilter(rootFilter, fCollapseFilterEnabled);
        fHeaderBar.removeFilter(filter);
        fTable.clearAll();
        fTable.setData(Key.FILTER_OBJ, rootFilter);
        /* +1 for header row, +2 for top and bottom filter status rows */
        fTable.setItemCount(3);
        startFilterThread();
        fireFilterApplied(rootFilter);

        // Set original width
        fTable.getColumns()[MARGIN_COLUMN_INDEX].setWidth(0);
        packMarginColumn();
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
        fHeaderBar.clearFilters();
        fCollapseFilterEnabled = false;
        fTable.clearAll();
        for (final TableColumn column : fTable.getColumns()) {
            column.setData(Key.FILTER_OBJ, null);
            column.setData(Key.FILTER_TXT, null);
        }
        fTable.setData(Key.FILTER_OBJ, null);
        if (fTrace != null) {
            /* +1 for header row */
            fTable.setItemCount((int) fTrace.getNbEvents() + 1);
        } else {
            /* +1 for header row */
            fTable.setItemCount(1);
        }
        fFilterMatchCount = 0;
        fFilterCheckCount = 0;
        if (fSelectedRank >= 0) {
            /* +1 for header row */
            fTable.setSelection((int) fSelectedRank + 1);
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
        private TmfCollapseFilter collapseFilter = null;
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
            if (fCollapseFilterEnabled) {
                collapseFilter = new TmfCollapseFilter();
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
                        if (collapseFilter == null || collapseFilter.matches(event)) {
                            final long rank = fFilterCheckCount;
                            final int index = (int) fFilterMatchCount;
                            fFilterMatchCount++;
                            fCache.storeEvent(event, rank, index);
                        } else if (collapseFilter != null) {
                            fCache.updateCollapsedEvent((int) fFilterMatchCount - 1);
                        }
                        refresh = true;
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
                    /*
                     * +1 for header row, +2 for top and bottom filter status
                     * rows
                     */
                    fTable.setItemCount((int) fFilterMatchCount + 3);
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
                /* -1 for header row, +1 for next event */
                startIndex = selectionIndex;
            } else {
                /*
                 * header row is selected, start at top event
                 */
                /* -1 for header row */
                startIndex = Math.max(0, fTable.getTopIndex() - 1);
            }
            final ITmfFilterTreeNode eventFilter = (ITmfFilterTreeNode) fTable.getData(Key.FILTER_OBJ);
            if (eventFilter != null) {
                // -1 for top filter status row
                startIndex = Math.max(0, startIndex - 1);
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
                /* -1 for header row, -1 for previous event */
                startIndex = selectionIndex - 2;
            } else {
                /*
                 * Header row is selected, start at precedent of top event
                 */
                /* -1 for header row, -1 for previous event */
                startIndex = fTable.getTopIndex() - 2;
            }
            final ITmfFilterTreeNode eventFilter = (ITmfFilterTreeNode) fTable.getData(Key.FILTER_OBJ);
            if (eventFilter != null) {
                /* -1 for top filter status row */
                startIndex = startIndex - 1;
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
            final ITmfTrace trace = fTrace;
            if (trace == null) {
                return Status.OK_STATUS;
            }
            final Display display = Display.getDefault();
            if (startIndex < 0) {
                rank = (int) trace.getNbEvents() - 1;
                /*
                 * -1 for header row, -3 for header and top and bottom filter
                 * status rows
                 */
            } else if (startIndex >= (fTable.getItemCount() - (eventFilter == null ? 1 : 3))) {
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
                        if (rank > (trace.getNbEvents() - 1)) {
                            rank = 0;
                        }
                    } else {
                        rank--;
                        if (rank < 0) {
                            rank = (int) trace.getNbEvents() - 1;
                        }
                    }
                }
            }
            final int startRank = (int) rank;
            boolean wrapped = false;
            while (!monitor.isCanceled() && (foundRank == -1)) {
                int nbRequested = (direction == Direction.FORWARD ? Integer.MAX_VALUE : Math.min((int) rank + 1, trace.getCacheSize()));
                if (direction == Direction.BACKWARD) {
                    rank = Math.max(0, rank - trace.getCacheSize() + 1);
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
                ((ITmfEventProvider) trace).sendRequest(request);
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
                            rank = (int) trace.getNbEvents() - 1;
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
            /* +1 for header row, +1 for top filter status row */
            final int selection = index + 1 + (eventFilter != null ? +1 : 0);

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
                    fSelectedBeginRank = fSelectedRank;
                    fRawViewer.selectAndReveal(fSelectedRank);
                    if (foundTimestamp != null) {
                        broadcast(new TmfSelectionRangeUpdatedSignal(TmfEventsTable.this, foundTimestamp, foundTimestamp, fTrace));
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
            if (request != null) {
                request.cancel();
            }
            synchronized (fSearchSyncObj) {
                fSearchThread = null;
            }
        }

    }

    /**
     * Create the resources.
     */
    private void createResources() {
        fGrayColor = fResourceManager.createColor(ColorUtil.blend(fTable.getBackground().getRGB(), fTable.getForeground().getRGB()));
        fGreenColor = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
    }

    /**
     * Initialize the fonts.
     */
    private void initializeFonts() {
        FontRegistry fontRegistry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry();
        fFont = fontRegistry.get(FONT_DEFINITION_ID);
        fBoldFont = fontRegistry.getBold(FONT_DEFINITION_ID);
        fTable.setFont(fFont);
        /* Column header font cannot be set. See Bug 63038 */
    }

    /**
     * Initialize the colors.
     */
    private void initializeColors() {
        ColorRegistry colorRegistry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
        fHighlightColor = colorRegistry.get(HIGHLIGHT_COLOR_DEFINITION_ID);
    }

    /**
     * @since 1.0
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ((IThemeManager.CHANGE_CURRENT_THEME.equals(event.getProperty())) ||
                (FONT_DEFINITION_ID.equals(event.getProperty()))) {
            initializeFonts();
            fTable.refresh();
        }
        if ((IThemeManager.CHANGE_CURRENT_THEME.equals(event.getProperty())) ||
                (HIGHLIGHT_COLOR_DEFINITION_ID.equals(event.getProperty()))) {
            initializeColors();
            fTable.refresh();
        }
    }

    /**
     * Pack the columns.
     *
     * @return true if this is the first call to pack columns
     *
     * @since 2.0
     */
    protected boolean packColumns() {
        boolean packDone = fPackDone;
        fTable.setRedraw(false);
        try {
            int horizontalPos = fTable.getHorizontalBar().getSelection();
            TableColumn tableColumns[] = fTable.getColumns();
            for (int i = 0; i < tableColumns.length; i++) {
                final TableColumn column = tableColumns[i];
                if (Objects.equals(column.getData(Key.WIDTH), SWT.DEFAULT)) {
                    packSingleColumn(i, column);
                }
            }
            if (!IS_WIN32) {
                /* Bug 410369: Cannot programmatically scroll on Windows */
                fTable.getHorizontalBar().setSelection(horizontalPos);
            }
        } finally {
            // Make sure that redraw is always enabled.
            fTable.setRedraw(true);
        }
        fPackDone = true;
        return !packDone;
    }

    private void packMarginColumn() {
        TableColumn[] columns = fTable.getColumns();
        if (columns.length > 0) {
            packSingleColumn(0, columns[0]);
        }
    }

    private void packSingleColumn(int i, final TableColumn column) {
        if (i != MARGIN_COLUMN_INDEX && !column.getResizable()) {
            return;
        }
        int minWidth = column.getWidth();
        fPacking = true;
        column.pack();
        /*
         * Workaround for Linux which doesn't consider the image width of
         * search/filter row in TableColumn.pack() after having executed
         * TableItem.setImage(null) for other rows than search/filter row.
         */
        if (IS_LINUX && (i == MARGIN_COLUMN_INDEX) && fCollapseFilterEnabled) {
            column.setWidth(column.getWidth() + SEARCH_IMAGE.getBounds().width);
        }

        if (column.getWidth() < minWidth) {
            column.setWidth(minWidth);
        }
        fPacking = false;
    }

    /**
     * Returns true if the column is a visible event column.
     *
     * @param column
     *            the column
     * @return false if the column is the margin column or hidden, true
     *         otherwise
     */
    private static boolean isVisibleEventColumn(TableColumn column) {
        if (column.getData(Key.ASPECT) == TmfMarginColumn.MARGIN_ASPECT) {
            return false;
        }
        if (!column.getResizable() && column.getWidth() == 0) {
            return false;
        }
        return true;
    }

    /**
     * Returns true if the column is expanded to take extra available space.
     * This is the last non-zero-width visible column in the column order on
     * Linux. This column's width should not be persisted.
     *
     * @param column
     *            the column
     * @return true if the column is expanded.
     */
    private static boolean isExpanded(TableColumn column) {
        if (IS_LINUX) {
            Table table = column.getParent();
            int[] order = table.getColumnOrder();
            for (int i = order.length - 1; i >= 0; i--) {
                TableColumn col = table.getColumn(order[i]);
                if (col == column) {
                    return true;
                }
                if (col.getWidth() > 0) {
                    return false;
                }
            }
        }
        return false;
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
    private String[] getItemStrings(List<TmfEventTableColumn> columns, ITmfEvent event) {
        if (event == null) {
            return EMPTY_STRING_ARRAY;
        }
        synchronized (columns) {
            String[] itemStrings = new String[columns.size()];
            TableColumn[] tableColumns = fTable.getColumns();
            for (int i = 0; i < columns.size(); i++) {
                TmfEventTableColumn column = columns.get(i);
                ITmfEvent passedEvent = event;
                if (!(column instanceof TmfMarginColumn) && (event instanceof CachedEvent)) {
                    /*
                     * Make sure that the event object from the trace is passed to all columns but
                     * the TmfMarginColumn
                     */
                    passedEvent = ((CachedEvent) event).event;
                }
                // Check if column is hidden but not a margin column.
                TableColumn tableColumn = tableColumns[fColumns.indexOf(column)];
                if (passedEvent == null || (!tableColumn.getResizable() && tableColumn.getWidth() == 0)) {
                    itemStrings[i] = EMPTY_STRING;
                } else {
                    String s = column.getItemString(passedEvent);
                    s = s.replaceAll("\\n", " "); //$NON-NLS-1$//$NON-NLS-2$
                    s = s.replaceAll("\\r", " "); //$NON-NLS-1$//$NON-NLS-2$
                    itemStrings[i] = s;
                }
            }
            return itemStrings;
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
     */
    public String[] getItemStrings(ITmfEvent event) {
        List<TmfEventTableColumn> columns = new ArrayList<>();
        for (int i : fTable.getColumnOrder()) {
            columns.add(fColumns.get(i));
        }
        return getItemStrings(columns, event);
    }

    /**
     * Returns an array of zero-relative integers that map the creation order of
     * the receiver's columns to the order in which they are currently being
     * displayed.
     * <p>
     * Specifically, the indices of the returned array represent the current
     * visual order of the columns, and the contents of the array represent the
     * creation order of the columns.
     *
     * @return the current visual order of the receiver's columns
     * @since 1.0
     */
    public int[] getColumnOrder() {
        return fColumnOrder;
    }

    /**
     * Get column widths
     *
     * @return the current visual widths of the receiver's columns
     * @since 2.1
     */
    public int[] getColumnWidth() {
        return fColumnSize;
    }

    /**
     * Get whether the columns are resizable
     *
     * @return an array stating if each column is resizable
     * @since 2.1
     */
    public boolean[] getColumnResizable() {
        return fColumnResizable;
    }

    /**
     * Sets the order that the columns in the receiver should be displayed in to
     * the given argument which is described in terms of the zero-relative
     * ordering of when the columns were added.
     * <p>
     * Specifically, the contents of the array represent the original position
     * of each column at the time its creation.
     *
     * @param order
     *            the new order to display the columns
     * @since 1.0
     */
    public void setColumnOrder(int[] order) {
        if (order == null || order.length != fTable.getColumns().length) {
            return;
        }
        fTable.setColumnOrder(order);
        fColumnOrder = fTable.getColumnOrder();
    }

    /**
     * Sets the column width and resizability
     *
     * @param width
     *            an array of widths
     * @param resizable
     *            an array of bools saying if a column is resizable or not
     * @since 2.1
     */
    public void setColumnWidth(int[] width, boolean[] resizable) {
        int length = fTable.getColumns().length;
        if (width == null || resizable == null || resizable.length != length || width.length != length) {
            return;
        }
        if (width.length > 0 && width[0] == 0) {
            /*
             * When width of margin column is 0 instead of SWT.DEFAULT, it is an old
             * setting. Reset all widths to SWT.DEFAULT to initially enable AutoFit.
             */
            Arrays.fill(width, SWT.DEFAULT);
        }
        int i = 0;
        for (TableColumn column : fTable.getColumns()) {
            if (i != MARGIN_COLUMN_INDEX) {
                column.setData(Key.WIDTH, width[i]);
                column.setResizable(resizable[i]);
                if (column.getResizable() && width[i] > 0) {
                    column.setWidth(width[i]);
                } else if (width[i] == 0){
                    column.setWidth(0);
                }
            }
            i++;
        }
        fColumnSize = width;
        fColumnResizable = resizable;
    }

    /**
     * Notify this table that is got the UI focus.
     */
    public void setFocus() {
        fTable.setFocus();
    }

    /**
     * Registers context menus with a site for extension. This method can be
     * called for part sites so that context menu contributions can be added.
     *
     * @param site
     *            the site that the context menus will be registered for
     *
     * @since 1.2
     */
    public void registerContextMenus(IWorkbenchPartSite site) {
        if (site instanceof IEditorSite) {
            IEditorSite editorSite = (IEditorSite) site;
            // Don't use the editor input when adding contributions, otherwise
            // we get too many unwanted things.
            editorSite.registerContextMenu(fTablePopupMenuManager, this, false);
        }
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
        fDisposeOnClose = disposeOnClose;

        // Perform the updates on the UI thread
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                fSelectedRank = -1;
                fSelectedBeginRank = -1;
                fTable.removeAll();
                fCache.setTrace(trace); // Clear the cache
                if (trace != null) {
                    if (!fTable.isDisposed()) {
                        if (fTable.getData(Key.FILTER_OBJ) == null) {
                            // +1 for header row
                            fTable.setItemCount((int) trace.getNbEvents() + 1);
                        } else {
                            stopFilterThread();
                            fFilterMatchCount = 0;
                            fFilterCheckCount = 0;
                            /*
                             * +1 for header row, +2 for top and bottom filter
                             * status rows
                             */
                            fTable.setItemCount(3);
                            startFilterThread();
                        }
                    }
                }
                fRawViewer.setTrace(trace);
            }
        });
    }

    /**
     * Assign the status line manager
     *
     * @param statusLineManager
     *            The status line manager, or null to disable status line
     *            messages
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
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (!fTable.isDisposed()) {
                    fTable.refresh();
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

    /**
     * Callback for when populating the table is complete.
     */
    protected void populateCompleted() {
        // Nothing by default;
    }

    // ------------------------------------------------------------------------
    // ISelectionProvider
    // ------------------------------------------------------------------------

    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        selectionChangedListeners.add(listener);
    }

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

    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        selectionChangedListeners.remove(listener);
    }

    @Override
    public void setSelection(ISelection selection) {
        // not implemented
    }

    /**
     * Notifies any selection changed listeners that the viewer's selection has
     * changed. Only listeners registered at the time this method is called are
     * notified.
     *
     * @param event
     *            a selection changed event
     *
     * @see ISelectionChangedListener#selectionChanged
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
                for (int i : fTable.getColumnOrder()) {
                    TableColumn column = fTable.getColumns()[i];
                    // Omit the margin column and hidden columns
                    if (isVisibleEventColumn(column)) {
                        if (defaultMessage.length() > 0) {
                            defaultMessage.append(", "); //$NON-NLS-1$
                        }
                        defaultMessage.append(tableItem.getText(i));
                    }
                }
                final AddBookmarkDialog dialog = new AddBookmarkDialog(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), defaultMessage.toString());
                if (dialog.open() == Window.OK) {
                    final String message = dialog.getValue();
                    try {
                        final Long rank = (Long) tableItem.getData(Key.RANK);
                        final String location = NLS.bind(Messages.TmfMarker_LocationRank, rank.toString());
                        final ITmfTimestamp timestamp = (ITmfTimestamp) tableItem.getData(Key.TIMESTAMP);
                        final long[] id = new long[1];
                        ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                            @Override
                            public void run(IProgressMonitor monitor) throws CoreException {
                                final IMarker bookmark = bookmarksFile.createMarker(IMarker.BOOKMARK);
                                bookmark.setAttribute(IMarker.MESSAGE, message.toString());
                                bookmark.setAttribute(IMarker.LOCATION, location);
                                bookmark.setAttribute(ITmfMarker.MARKER_RANK, rank.toString());
                                bookmark.setAttribute(ITmfMarker.MARKER_TIME, Long.toString(timestamp.toNanos()));
                                bookmark.setAttribute(ITmfMarker.MARKER_COLOR, dialog.getColorValue().toString());
                                id[0] = bookmark.getId();
                            }
                        }, null);
                        fBookmarksMap.put(rank, id[0]);
                        fTable.refresh();
                    } catch (final CoreException e) {
                        displayException(e);
                    }
                }
            }
        }

    }

    /**
     * Add one or more bookmarks to this event table.
     *
     * @param bookmarks
     *            The bookmarks to add
     * @since 2.0
     */
    public void addBookmark(final IMarker... bookmarks) {
        for (IMarker bookmark : bookmarks) {
            /* try location as an integer for backward compatibility */
            long rank = bookmark.getAttribute(IMarker.LOCATION, -1);
            if (rank == -1) {
                String rankString = bookmark.getAttribute(ITmfMarker.MARKER_RANK, (String) null);
                if (rankString != null) {
                    try {
                        rank = Long.parseLong(rankString);
                    } catch (NumberFormatException e) {
                        Activator.getDefault().logError("Invalid marker rank", e); //$NON-NLS-1$
                    }
                }
            }
            if (rank != -1) {
                fBookmarksMap.put(rank, bookmark.getId());
            }
        }
        fTable.refresh();
    }

    /**
     * Remove one or more bookmarks from this event table.
     *
     * @param bookmarks
     *            The bookmarks to remove
     * @since 2.0
     */
    public void removeBookmark(final IMarker... bookmarks) {
        for (IMarker bookmark : bookmarks) {
            for (final Entry<Long, Long> entry : fBookmarksMap.entries()) {
                if (entry.getValue().equals(bookmark.getId())) {
                    fBookmarksMap.remove(entry.getKey(), entry.getValue());
                    break;
                }
            }
        }
        fTable.refresh();
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
            IMarker[] bookmarks = bookmarksFile.findMarkers(IMarker.BOOKMARK, false, IResource.DEPTH_ZERO);
            addBookmark(bookmarks);
        } catch (final CoreException e) {
            displayException(e);
        }
    }

    @Override
    public void gotoMarker(final IMarker marker) {
        ITmfTimestamp tsBegin = null;
        ITmfTimestamp tsEnd = null;
        /* try location as an integer for backward compatibility */
        long rank = marker.getAttribute(IMarker.LOCATION, -1);
        if (rank == -1) {
            String rankString = marker.getAttribute(ITmfMarker.MARKER_RANK, (String) null);
            try {
                rank = Long.parseLong(rankString);
            } catch (NumberFormatException e) {
                /* ignored */
            }
        }
        try {
            String timeString = marker.getAttribute(ITmfMarker.MARKER_TIME, (String) null);
            long time = Long.parseLong(timeString);
            tsBegin = TmfTimestamp.fromNanos(time);
            String durationString = marker.getAttribute(ITmfMarker.MARKER_DURATION, (String) null);
            long duration = Long.parseLong(durationString);
            tsEnd = TmfTimestamp.fromNanos(time + duration);
        } catch (NumberFormatException e) {
            /* ignored */
        }
        if (rank == -1 && tsBegin != null) {
            final ITmfContext context = fTrace.seekEvent(tsBegin);
            rank = context.getRank();
            context.dispose();
        }
        if (rank != -1) {
            int index = (int) rank;
            if (fTable.getData(Key.FILTER_OBJ) != null) {
                // +1 for top filter status row
                index = fCache.getFilteredEventIndex(rank) + 1;
            } else if (rank >= fTable.getItemCount()) {
                fPendingGotoRank = rank;
            }
            fSelectedRank = rank;
            fSelectedBeginRank = fSelectedRank;
            fTable.setSelection(index + 1); // +1 for header row
            updateStatusLine(null);
            if (tsBegin != null) {
                if (tsEnd != null) {
                    broadcast(new TmfSelectionRangeUpdatedSignal(TmfEventsTable.this, tsBegin, tsEnd, fTrace));
                } else {
                    broadcast(new TmfSelectionRangeUpdatedSignal(TmfEventsTable.this, tsBegin, tsBegin, fTrace));
                }
            }
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
                        /* +1 for header row */
                        fTable.setItemCount((int) fTrace.getNbEvents() + 1);
                        /* +1 for header row */
                        if ((fPendingGotoRank != -1) && ((fPendingGotoRank + 1) < fTable.getItemCount())) {
                            /* +1 for header row */
                            fTable.setSelection((int) fPendingGotoRank + 1);
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
     * Handler for the selection range signal.
     *
     * @param signal
     *            The incoming signal
     * @since 1.0
     */
    @TmfSignalHandler
    public void selectionRangeUpdated(final TmfSelectionRangeUpdatedSignal signal) {
        ITmfTrace trace = fTrace;
        if ((signal.getSource() != this) && (trace != null) && (!fTable.isDisposed())) {

            Job timeSelectJob;
            synchronized (fTimeSelectMutexRule) {
                timeSelectJob = fTimeSelectJob;
                if (timeSelectJob != null) {
                    timeSelectJob.cancel();
                }

                TmfTraceContext ctx = TmfTraceManager.getInstance().getTraceContext(trace);
                ITmfTimestamp ts = ctx.getSelectionRange().getStartTime();
                ITmfTimestamp tf = ctx.getSelectionRange().getEndTime();
                /*
                 * Run in separate thread to not block UI thread for too long.
                 */
                timeSelectJob = new Job("Events table selection job") { //$NON-NLS-1$

                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        if (fTrace == null) {
                            return Status.OK_STATUS;
                        }

                        final Pair<Long, Long> selection = getSelectedRanks(monitor);

                        if (monitor.isCanceled() || (selection == null)) {
                            return Status.CANCEL_STATUS;
                        }
                        updateDisplayWithSelection(selection.getFirst().longValue(), selection.getSecond().longValue());
                        return Status.OK_STATUS;
                    }

                    /**
                     * Verify if the event is within the trace range and adjust if
                     * necessary.
                     * @param monitor
                     *                a progress monitor
                     * @return A pair of rank representing the selected area
                     **/
                    @Nullable
                    private Pair<Long, Long> getSelectedRanks(IProgressMonitor monitor) {

                        /* Clamp the timestamp value to fit inside of the trace */
                        ITmfTimestamp timestampBegin = ts;
                        if (timestampBegin.compareTo(fTrace.getStartTime()) < 0) {
                            timestampBegin = fTrace.getStartTime();
                        }
                        if (timestampBegin.compareTo(fTrace.getEndTime()) > 0) {
                            timestampBegin = fTrace.getEndTime();
                        }

                        ITmfTimestamp timestampEnd = tf;
                        if (timestampEnd.compareTo(fTrace.getStartTime()) < 0) {
                            timestampEnd = fTrace.getStartTime();
                        }
                        if (timestampEnd.compareTo(fTrace.getEndTime()) > 0) {
                            timestampEnd = fTrace.getEndTime();
                        }

                        ITmfTimestamp tb;
                        ITmfTimestamp te;
                        long rankBegin;
                        long rankEnd;
                        ITmfContext contextBegin;
                        ITmfContext contextEnd;
                        if (monitor.isCanceled()) {
                            return null;
                        }

                        /* Adjust the rank of the selection to the right range */
                        if (timestampBegin.compareTo(timestampEnd) > 0) {
                            te = timestampEnd;
                            contextEnd = fTrace.seekEvent(te);
                            rankEnd = contextEnd.getRank();
                            contextEnd.dispose();

                            if (monitor.isCanceled()) {
                                return null;
                            }
                            /*
                             * To include all events at the begin time, seek at the
                             * next nanosecond and then use the previous rank
                             */
                            tb = timestampBegin.normalize(1, ITmfTimestamp.NANOSECOND_SCALE);
                            if (tb.compareTo(fTrace.getEndTime()) <= 0) {
                                contextBegin = fTrace.seekEvent(tb);
                                rankBegin = contextBegin.getRank();
                                contextBegin.dispose();
                            } else {
                                rankBegin = ITmfContext.UNKNOWN_RANK;
                            }
                            rankBegin = (rankBegin == ITmfContext.UNKNOWN_RANK ? fTrace.getNbEvents() : rankBegin) - 1;
                            /*
                             * If no events in selection range, select only the next
                             * event
                             */
                            rankBegin = rankBegin >= rankEnd ? rankBegin : rankEnd;
                        } else {
                            tb = timestampBegin;
                            contextBegin = fTrace.seekEvent(tb);
                            rankBegin = contextBegin.getRank();
                            contextBegin.dispose();
                            if (monitor.isCanceled()) {
                                return null;
                            }
                            /*
                             * To include all events at the end time, seek at the
                             * next nanosecond and then use the previous rank
                             */
                            te = timestampEnd.normalize(1, ITmfTimestamp.NANOSECOND_SCALE);
                            if (te.compareTo(fTrace.getEndTime()) <= 0) {
                                contextEnd = fTrace.seekEvent(te);
                                rankEnd = contextEnd.getRank();
                                contextEnd.dispose();
                            } else {
                                rankEnd = ITmfContext.UNKNOWN_RANK;
                            }
                            rankEnd = (rankEnd == ITmfContext.UNKNOWN_RANK ? fTrace.getNbEvents() : rankEnd) - 1;
                            /*
                             * If no events in selection range, select only the next
                             * event
                             */
                            rankEnd = rankEnd >= rankBegin ? rankEnd : rankBegin;
                        }
                        return new Pair<>(Long.valueOf(rankBegin), Long.valueOf(rankEnd));
                    }

                    private void updateDisplayWithSelection(final long rankBegin, final long rankEnd) {
                        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                // Return if table is disposed
                                if (fTable.isDisposed()) {
                                    return;
                                }
                                if (fSelectedBeginRank == rankBegin && fSelectedRank == rankEnd) {
                                    return;
                                }

                                fSelectedRank = rankEnd;
                                long toReveal = fSelectedBeginRank != rankBegin ? rankBegin : rankEnd;
                                fSelectedBeginRank = rankBegin;
                                int indexBegin = (int) rankBegin;
                                int indexEnd = (int) rankEnd;

                                if (fTable.getData(Key.FILTER_OBJ) != null) {
                                    /* +1 for top filter status row */
                                    indexBegin = fCache.getFilteredEventIndex(rankBegin) + 1;
                                    indexEnd = rankEnd == rankBegin ? indexBegin : fCache.getFilteredEventIndex(rankEnd) + 1;
                                }
                                /* +1 for header row */
                                fTable.setSelectionRange(indexBegin + 1, indexEnd + 1);
                                fRawViewer.selectAndReveal(toReveal);
                                updateStatusLine(null);
                            }
                        });
                    }
                };
                timeSelectJob.setSystem(true);
                /*
                 *  Make subsequent jobs not run concurrently so that they are
                 *  executed in order.
                 */
                timeSelectJob.setRule(fTimeSelectMutexRule);
                timeSelectJob.schedule();
                fTimeSelectJob = timeSelectJob;
            }
        }
    }

    // ------------------------------------------------------------------------
    // Error handling
    // ------------------------------------------------------------------------

    /**
     * Display an exception in a message box
     *
     * @param e
     *            the exception
     */
    private static void displayException(final Exception e) {
        final MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        mb.setText(e.getClass().getSimpleName());
        mb.setMessage(e.getMessage());
        mb.open();
    }

    /**
     * Refresh the table
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

        private static final @NonNull ITmfEventAspect<String> MARGIN_ASPECT = new ITmfEventAspect<String>() {

            @Override
            public String getName() {
                return EMPTY_STRING;
            }

            @Override
            public String resolve(ITmfEvent event) {
                if (!(event instanceof CachedEvent) || ((CachedEvent) event).repeatCount == 0) {
                    return EMPTY_STRING;
                }
                return "+" + ((CachedEvent) event).repeatCount; //$NON-NLS-1$
            }

            @Override
            public String getHelpText() {
                return EMPTY_STRING;
            }
        };

        /**
         * Constructor
         */
        public TmfMarginColumn() {
            super(MARGIN_ASPECT);
        }
    }
}