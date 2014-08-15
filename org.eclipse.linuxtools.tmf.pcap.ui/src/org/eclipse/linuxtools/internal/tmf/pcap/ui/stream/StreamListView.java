/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.pcap.ui.stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.internal.tmf.pcap.core.analysis.StreamListAnalysis;
import org.eclipse.linuxtools.internal.tmf.pcap.core.event.PcapEvent;
import org.eclipse.linuxtools.internal.tmf.pcap.core.event.TmfPacketStream;
import org.eclipse.linuxtools.internal.tmf.pcap.core.event.TmfPacketStreamBuilder;
import org.eclipse.linuxtools.internal.tmf.pcap.core.protocol.TmfProtocol;
import org.eclipse.linuxtools.internal.tmf.pcap.core.signal.TmfPacketStreamSelectedSignal;
import org.eclipse.linuxtools.internal.tmf.pcap.core.trace.PcapTrace;
import org.eclipse.linuxtools.internal.tmf.pcap.ui.Activator;
import org.eclipse.linuxtools.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterAndNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterContainsNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterOrNode;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.project.model.TraceUtils;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.linuxtools.tmf.ui.views.filter.FilterManager;
import org.eclipse.linuxtools.tmf.ui.views.filter.FilterView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Class that represents the Stream List View. Such a view lists all the
 * available streams from the current experiment. <br>
 * <br>
 * TODO Switch to TmfUiRefreshHandler once the behavior is fixed
 *
 * FIXME analysis is leaking ressource. Someone I will not name told me not to worry about it since
 * AnalysisModule will not be autocloseable later.
 *
 * @author Vincent Perot
 */
public class StreamListView extends TmfView {

    /**
     * The Stream List View ID.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.pcap.ui.view.stream.list"; //$NON-NLS-1$

    private static final String[] COLUMN_NAMES =
    { Messages.StreamListView_ID,
            Messages.StreamListView_EndpointA,
            Messages.StreamListView_EndpointB,
            Messages.StreamListView_TotalPackets,
            Messages.StreamListView_TotalBytes,
            Messages.StreamListView_PacketsAtoB,
            Messages.StreamListView_BytesAtoB,
            Messages.StreamListView_PacketsBtoA,
            Messages.StreamListView_BytesBtoA,
            Messages.StreamListView_StartTime,
            Messages.StreamListView_StopTime,
            Messages.StreamListView_Duration,
            Messages.StreamListView_BPSAtoB,
            Messages.StreamListView_BPSBtoA
    };

    private static final int[] COLUMN_SIZES =
        { 75,
        350,
        350,
        110,
        110,
        110,
        110,
        110,
        110,
        180,
        180,
        110,
        110,
        110 };

    private static final String KEY_PROTOCOL = "$protocol$"; //$NON-NLS-1$
    private static final String KEY_STREAM = "$stream$"; //$NON-NLS-1$

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private static final long WAIT_TIME = 1000;

    private @Nullable CTabFolder fTabFolder;
    private @Nullable Map<TmfProtocol, Table> fTableMap;

    private @Nullable TmfPacketStream fCurrentStream;
    private @Nullable ITmfTrace fCurrentTrace;

    private volatile boolean fStopThread;

    /**
     * Constructor of the StreamListView class.
     */
    public StreamListView() {
        super(ID);
    }

    /**
     * Handler called when an trace is opened.
     *
     * @param signal
     *            Contains the information about the selection.
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        fCurrentTrace = signal.getTrace();
        resetView();
        queryAnalysis();
    }

    /**
     * Handler called when an trace is closed. Checks if the trace is the
     * current trace and update the view accordingly.
     *
     * @param signal
     *            Contains the information about the selection.
     */
    @TmfSignalHandler
    public void traceClosed(TmfTraceClosedSignal signal) {
        if (fCurrentTrace == signal.getTrace()) {
            fCurrentTrace = null;
            resetView();
        }
    }

    /**
     * Handler called when an trace is selected. Checks if the trace has changed
     * and requests the selected trace if it has not yet been cached.
     *
     * @param signal
     *            Contains the information about the selection.
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        if (fCurrentTrace != signal.getTrace()) {
            fCurrentTrace = signal.getTrace();
            resetView();
            queryAnalysis();
        }
    }

    private void queryAnalysis() {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                ITmfTrace trace = fCurrentTrace;
                if (trace == null || (!(trace instanceof PcapTrace))) {
                    return;
                }
                StreamListAnalysis analysis = trace.getAnalysisModuleOfClass(StreamListAnalysis.class, StreamListAnalysis.ID);
                if (analysis == null) {
                    return;
                }
                while (!analysis.isFinished() && !fStopThread) {
                    updateUI();
                    try {
                        Thread.sleep(WAIT_TIME);
                    } catch (InterruptedException e) {
                        String message = e.getMessage();
                        if (message == null) {
                            message = EMPTY_STRING;
                        }
                        Activator.logError(message, e);
                        return;
                    }
                }
                // Update UI one more time (daft punk)
                if (!fStopThread) {
                    updateUI();
                }

            }
        });

        fStopThread = false;
        thread.start();
    }

    private void resetView() {

        // Stop thread if needed
        fStopThread = true;

        // Remove all content in tables
        final Display display = Display.getDefault();
        if (display == null || display.isDisposed()) {
            return;
        }
        display.asyncExec(new Runnable() {

            @Override
            public void run() {
                if (display.isDisposed()) {
                    return;
                }
                Map<TmfProtocol, Table> tableMap = fTableMap;
                if (tableMap == null) {
                    return;
                }
                for (TmfProtocol protocol : tableMap.keySet()) {
                    if (!(tableMap.get(protocol).isDisposed())) {
                        tableMap.get(protocol).removeAll();
                    }
                }
            }
        });
    }

    private void updateUI() {
        final Display display = Display.getDefault();
        if (display == null || display.isDisposed()) {
            return;
        }
        display.asyncExec(new Runnable() {

            @Override
            public void run() {
                if (display.isDisposed()) {
                    return;
                }
                ITmfTrace trace = fCurrentTrace;
                if (trace == null) {
                    return;
                }

                StreamListAnalysis analysis = trace.getAnalysisModuleOfClass(StreamListAnalysis.class, StreamListAnalysis.ID);
                if (analysis == null) {
                    return;
                }

                Map<TmfProtocol, Table> tables = fTableMap;
                if (tables == null) {
                    return;
                }
                for (TmfProtocol p : tables.keySet()) {
                    @SuppressWarnings("null")
                    @NonNull TmfProtocol protocol = p;
                    TmfPacketStreamBuilder builder = analysis.getBuilder(protocol);
                    if (builder != null && !(tables.get(protocol).isDisposed())) {
                        for (TmfPacketStream stream : builder.getStreams()) {

                            TableItem item;
                            if (stream.getID() < tables.get(protocol).getItemCount()) {
                                item = tables.get(protocol).getItem(stream.getID());
                            } else {
                                item = new TableItem(tables.get(protocol), SWT.NONE);
                            }
                            item.setText(0, String.valueOf(stream.getID()));
                            item.setText(1, stream.getFirstEndpoint().toString());
                            item.setText(2, stream.getSecondEndpoint().toString());
                            item.setText(3, String.valueOf(stream.getNbPackets()));
                            item.setText(4, String.valueOf(stream.getNbBytes()));
                            item.setText(5, String.valueOf(stream.getNbPacketsAtoB()));
                            item.setText(6, String.valueOf(stream.getNbBytesAtoB()));
                            item.setText(7, String.valueOf(stream.getNbPacketsBtoA()));
                            item.setText(8, String.valueOf(stream.getNbBytesBtoA()));
                            item.setText(9, stream.getStartTime().toString());
                            item.setText(10, stream.getStopTime().toString());
                            item.setText(11, String.format("%.3f", stream.getDuration())); //$NON-NLS-1$
                            item.setText(12, String.format("%.3f", stream.getBPSAtoB())); //$NON-NLS-1$
                            item.setText(13, String.format("%.3f", stream.getBPSBtoA())); //$NON-NLS-1$
                            item.setData(KEY_STREAM, stream);
                        }
                    }
                }
            }

        });
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        // Initialize
        fTableMap = new HashMap<>();
        fCurrentTrace = getActiveTrace();
        fCurrentStream = null;

        // Add a tab folder
        fTabFolder = new CTabFolder(parent, SWT.NONE);
        fTabFolder.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(@Nullable SelectionEvent e) {
                Map<TmfProtocol, Table> tables = fTableMap;
                if (tables == null || e == null) {
                    return;
                }
                TmfProtocol protocol = (TmfProtocol) e.item.getData(KEY_PROTOCOL);
                tables.get(protocol).deselectAll();
                fCurrentStream = null;
            }

        });

        // Add items and tables for each protocol
        for (TmfProtocol protocol : TmfProtocol.getAllProtocols()) {
            if (protocol.supportsStream()) {
                CTabItem item = new CTabItem(fTabFolder, SWT.NONE);
                item.setText(protocol.getName());
                item.setData(KEY_PROTOCOL, protocol);
                Table table = new Table(fTabFolder, SWT.NONE);
                table.setHeaderVisible(true);
                table.setLinesVisible(true);

                // Add columns to table
                for (int i = 0; i < COLUMN_NAMES.length || i < COLUMN_SIZES.length; i++) {
                    TableColumn column = new TableColumn(table, SWT.NONE);
                    column.setText(COLUMN_NAMES[i]);
                    column.setWidth(COLUMN_SIZES[i]);
                }
                item.setControl(table);
                table.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(@Nullable SelectionEvent e) {
                        if (e == null) {
                            return;
                        }
                        fCurrentStream = (TmfPacketStream) e.item.getData(KEY_STREAM);
                    }

                });

                Map<TmfProtocol, Table> tables = fTableMap;
                if (tables == null) {
                    return;
                }

                tables.put(protocol, table);

                // Add right click menu
                Menu menu = new Menu(table);
                MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
                menuItem.setText(Messages.StreamListView_FollowStream);
                menuItem.addListener(SWT.Selection, new Listener() {

                    @Override
                    public void handleEvent(@Nullable Event event) {
                        TmfSignal signal = new TmfPacketStreamSelectedSignal(this, 0, fCurrentStream);
                        TmfSignalManager.dispatchSignal(signal);
                    }
                });
                menuItem = new MenuItem(menu, SWT.PUSH);
                menuItem.setText(Messages.StreamListView_Clear);
                menuItem.addListener(SWT.Selection, new Listener() {

                    @Override
                    public void handleEvent(@Nullable Event event) {
                        TmfSignal signal = new TmfPacketStreamSelectedSignal(this, 0, null);
                        TmfSignalManager.dispatchSignal(signal);

                    }
                });
                menuItem = new MenuItem(menu, SWT.PUSH);
                menuItem.setText(Messages.StreamListView_ExtractAsFilter);
                menuItem.addListener(SWT.Selection, new Listener() {

                    @Override
                    public void handleEvent(@Nullable Event event) {
                        // Generate filter.
                        ITmfFilterTreeNode filter = generateFilter();

                        // Update view and XML
                        updateFilters(filter);

                    }

                    private void updateFilters(@Nullable ITmfFilterTreeNode filter) {
                        if (filter == null) {
                            return;
                        }

                        // Update XML
                        List<ITmfFilterTreeNode> newFilters = new ArrayList<>();
                        ITmfFilterTreeNode[] oldFilters = FilterManager.getSavedFilters();
                        for (int i = 0; i < oldFilters.length; i++) {
                            newFilters.add(oldFilters[i]);
                        }
                        if (!(newFilters.contains(filter))) {
                            newFilters.add(filter);
                            FilterManager.setSavedFilters(newFilters.toArray(new ITmfFilterTreeNode[newFilters.size()]));
                        }

                        // Update Filter View
                        try {
                            final IWorkbench wb = PlatformUI.getWorkbench();
                            final IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();
                            IViewPart view = activePage.showView(FilterView.ID);
                            FilterView filterView = (FilterView) view;
                            filterView.addFilter(filter);
                        } catch (final PartInitException e) {
                            TraceUtils.displayErrorMsg(Messages.StreamListView_ExtractAsFilter, "Error opening view " + FilterView.ID + e.getMessage()); //$NON-NLS-1$
                            Activator.logError("Error opening view " + FilterView.ID, e); //$NON-NLS-1$
                            return;
                        }

                    }

                    private @Nullable ITmfFilterTreeNode generateFilter() {
                        TmfPacketStream stream = fCurrentStream;
                        if (stream == null) {
                            return null;
                        }

                        // First stage - root
                        String name = Messages.StreamListView_FilterName_Stream + ' ' + stream.getProtocol().getShortName() + ' ' + stream.getFirstEndpoint()
                                + " <--> " + stream.getSecondEndpoint(); //$NON-NLS-1$
                        TmfFilterNode root = new TmfFilterNode(name);

                        // Second stage - and
                        TmfFilterAndNode and = new TmfFilterAndNode(root);

                        // Third stage - protocol + or
                        TmfFilterContainsNode protocolFilter = new TmfFilterContainsNode(and);
                        protocolFilter.setField(stream.getProtocol().getName());
                        protocolFilter.setValue(EMPTY_STRING);
                        TmfFilterOrNode or = new TmfFilterOrNode(and);

                        // Fourth stage - and
                        TmfFilterAndNode andA = new TmfFilterAndNode(or);
                        TmfFilterAndNode andB = new TmfFilterAndNode(or);

                        // Fourth stage - endpoints
                        TmfFilterContainsNode endpointAAndA = new TmfFilterContainsNode(andA);
                        endpointAAndA.setField(PcapEvent.EVENT_FIELD_PACKET_SOURCE);
                        endpointAAndA.setValue(stream.getFirstEndpoint());
                        TmfFilterContainsNode endpointBAndA = new TmfFilterContainsNode(andA);
                        endpointBAndA.setField(PcapEvent.EVENT_FIELD_PACKET_DESTINATION);
                        endpointBAndA.setValue(stream.getSecondEndpoint());
                        TmfFilterContainsNode endpointAAndB = new TmfFilterContainsNode(andB);
                        endpointAAndB.setField(PcapEvent.EVENT_FIELD_PACKET_SOURCE);
                        endpointAAndB.setValue(stream.getSecondEndpoint());
                        TmfFilterContainsNode endpointBAndB = new TmfFilterContainsNode(andB);
                        endpointBAndB.setField(PcapEvent.EVENT_FIELD_PACKET_DESTINATION);
                        endpointBAndB.setValue(stream.getFirstEndpoint());

                        return root;
                    }
                });
                table.setMenu(menu);
            }
        }

        // Ask the analysis for data.
        queryAnalysis();
    }

    @Override
    public void setFocus() {
        CTabFolder tabFolder = fTabFolder;
        if (tabFolder != null && !(tabFolder.isDisposed())) {
            tabFolder.setFocus();
        }
    }

}
