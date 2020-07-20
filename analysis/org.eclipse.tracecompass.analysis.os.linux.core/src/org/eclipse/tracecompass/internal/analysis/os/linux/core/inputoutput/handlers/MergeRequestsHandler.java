/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.handlers;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.DiskWriteModel;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.InputOutputStateProvider;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.IoOperationType;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.Request;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.handlers.KernelEventHandler;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * elv_merge_request event handler
 *
 * TODO: This event is part of an addons module to lttng. Update this when it is
 * mainlined in the kernel
 *
 * @author Houssem Daoud
 */
public class MergeRequestsHandler extends KernelEventHandler {

    private final InputOutputStateProvider fStateProvider;

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     * @param sp
     *            The state provider calling this handler
     */
    public MergeRequestsHandler(IKernelAnalysisEventLayout layout, InputOutputStateProvider sp) {
        super(layout);
        fStateProvider = sp;
    }

    @Override
    public void handleEvent(@NonNull ITmfStateSystemBuilder ss, @NonNull ITmfEvent event) throws AttributeNotFoundException {
        ITmfEventField content = event.getContent();
        long ts = event.getTimestamp().getValue();

        int phydisk = ((Long) content.getField(getLayout().fieldBlockDeviceId()).getValue()).intValue();
        Long baseRequestSector = NonNullUtils.checkNotNull((Long) content.getField(getLayout().fieldBlockRqSector()).getValue());
        Long mergedRequestSector = NonNullUtils.checkNotNull((Long) content.getField(getLayout().fieldBlockNextRqSector()).getValue());
        DiskWriteModel disk = fStateProvider.getDisk(phydisk);

        Request baseRequest = disk.getWaitingRequest(baseRequestSector);
        if (baseRequest == null) {
            baseRequest = new Request(disk, baseRequestSector, IoOperationType.OTHER);
        }
        Request mergedRequest = disk.getWaitingRequest(mergedRequestSector);
        if (mergedRequest == null) {
            mergedRequest = new Request(disk, mergedRequestSector, IoOperationType.OTHER);
        }
        disk.mergeRequests(ts, baseRequest, mergedRequest);
    }

}
