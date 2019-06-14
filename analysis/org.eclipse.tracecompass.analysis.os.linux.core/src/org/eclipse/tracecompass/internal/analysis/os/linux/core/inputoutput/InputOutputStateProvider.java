/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.Activator;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.handlers.BlockFrontMergeHandler;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.handlers.BlockRqComplete;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.handlers.BlockRqInsertHandler;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.handlers.BlockRqIssueHandler;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.handlers.MergeRequestsHandler;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.handlers.StateDumpHandler;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.handlers.SysEntryHandler;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.handlers.SysExitHandler;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.handlers.KernelEventHandler;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableMap;

/**
 * State provider for the I/O analysis
 *
 * Attribute tree:
 *
 * <pre>
 * |- SYSTEM_CALLS
 * |  |- <TID> -> System Call Name
 * |- THREADS
 * |  |- <TID number>
 * |  |  |- BYTES_READ
 * |  |  |- BYTES_WRITTEN
 * |- Disks
 * |  |- <Disk number> -> Disk Name
 * |  |  |- SECTORS_READ
 * |  |  |- SECTORS_WRITTEN
 * |  |  |- WAITING_QUEUE -> Root for the Attribute pool for waiting queue
 * |  |  |  |- <slot #1> -> Status
 * |  |  |  |  |- CURRENT_REQUEST
 * |  |  |  |  |- REQUEST_SIZE
 * |  |  |  |  |- MERGED_IN
 * |  |  |  |- <slot #2>
 * |  |  |- WAITING_QUEUE_LENGTH
 * |  |  |- DRIVER_QUEUE -> Root for the Attribute pool for driver queue
 * |  |  |  |- <slot #1> -> Status
 * |  |  |  |  |- CURRENT_REQUEST
 * |  |  |  |  |- REQUEST_SIZE
 * |  |  |  |  |- ISSUED_FROM
 * |  |  |  |- <slot #2>
 * |  |  |- DRIVER_QUEUE_LENGTH
 * </pre>
 *
 * @author Houssem Daoud
 * @since 2.0
 */
public class InputOutputStateProvider extends AbstractTmfStateProvider {

    private static final int VERSION = 1;

    private final Map<Integer, DiskWriteModel> fDisks = new HashMap<>();
    private final Map<String, KernelEventHandler> fEventNames;
    private final IKernelAnalysisEventLayout fLayout;

    private final KernelEventHandler fSysEntryHandler;
    private final KernelEventHandler fSysExitHandler;

    /**
     * Instantiate a new state provider plugin.
     *
     * @param trace
     *            The kernel trace to apply this state provider to
     * @param layout
     *            The event layout to use for this state provider.
     */
    public InputOutputStateProvider(ITmfTrace trace, IKernelAnalysisEventLayout layout) {
        super(trace, "Input Output Analysis");//$NON-NLS-1$
        fLayout = layout;
        fEventNames = buildEventNames(layout);
        fSysEntryHandler = new SysEntryHandler(layout);
        fSysExitHandler = new SysExitHandler(layout);
    }

    private Map<String, KernelEventHandler> buildEventNames(IKernelAnalysisEventLayout layout) {
        ImmutableMap.Builder<String, KernelEventHandler> builder = ImmutableMap.builder();

        builder.put(layout.eventBlockRqInsert(), new BlockRqInsertHandler(layout, this));
        builder.put(layout.eventBlockRqIssue(), new BlockRqIssueHandler(layout, this));
        builder.put(layout.eventBlockRqComplete(), new BlockRqComplete(layout, this));
        builder.put(layout.eventBlockBioFrontmerge(), new BlockFrontMergeHandler(layout, this));
        builder.put(layout.eventBlockRqMerge(), new MergeRequestsHandler(layout, this));

        final String eventStatedumpBlockDevice = layout.eventStatedumpBlockDevice();
        if (eventStatedumpBlockDevice != null) {
            builder.put(eventStatedumpBlockDevice, new StateDumpHandler(layout, this));
        }

        return builder.build();
    }

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public InputOutputStateProvider getNewInstance() {
        return new InputOutputStateProvider(this.getTrace(), this.fLayout);
    }

    @Override
    protected void eventHandle(@Nullable ITmfEvent event) {

        if (event == null) {
            return;
        }

        final String eventName = event.getName();

        try {
            final ITmfStateSystemBuilder ss = NonNullUtils.checkNotNull(getStateSystemBuilder());
            /*
             * Feed event to the history system if it's known to cause a state
             * transition.
             */
            KernelEventHandler handler = fEventNames.get(eventName);
            if (handler == null) {
                if (isSyscallExit(eventName)) {
                    handler = fSysExitHandler;
                } else if (isSyscallEntry(eventName)) {
                    handler = fSysEntryHandler;
                }
            }
            if (handler != null) {
                handler.handleEvent(ss, event);
            }
        } catch (TimeRangeException | StateValueTypeException | AttributeNotFoundException e) {
            Activator.getDefault().logError("Exception while building the IO state system", e); //$NON-NLS-1$
        }

    }

    /**
     * Get a disk identified by a device ID
     *
     * @param deviceId
     *            The device ID of the block device
     * @return The disk corresponding to the device ID
     */
    public DiskWriteModel getDisk(int deviceId) {
        return fDisks.computeIfAbsent(deviceId, diskId -> new DiskWriteModel(diskId, checkNotNull(getStateSystemBuilder())));
    }

    private boolean isSyscallEntry(String eventName) {
        return (eventName.startsWith(fLayout.eventSyscallEntryPrefix())
                || eventName.startsWith(fLayout.eventCompatSyscallEntryPrefix()));
    }

    private boolean isSyscallExit(String eventName) {
        return (eventName.startsWith(fLayout.eventSyscallExitPrefix()) ||
                eventName.startsWith(fLayout.eventCompatSyscallExitPrefix()));
    }

    /**
     * Return the quark corresponding to the threads attributes
     *
     * @param ssb
     *            the state system builder
     * @return The quark of the {@link Attributes#THREADS} node
     */
    public static int getNodeThreads(ITmfStateSystemBuilder ssb) {
        return ssb.getQuarkAbsoluteAndAdd(Attributes.THREADS);
    }

    /**
     * Return the quark corresponding to the system call root attributes
     *
     * @param ssb
     *            the state system builder
     * @return The quark of the {@link Attributes#SYSTEM_CALLS_ROOT} node
     */
    public static int getNodeSyscalls(ITmfStateSystemBuilder ssb) {
        return ssb.getQuarkAbsoluteAndAdd(Attributes.SYSTEM_CALLS_ROOT);
    }

}
