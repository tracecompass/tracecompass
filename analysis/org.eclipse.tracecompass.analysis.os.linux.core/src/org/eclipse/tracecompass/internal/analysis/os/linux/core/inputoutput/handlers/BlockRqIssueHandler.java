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
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.BlockIO;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.DiskWriteModel;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.InputOutputStateProvider;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.Request;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.handlers.KernelEventHandler;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * Request issue event handler
 *
 * @author Houssem Daoud
 */
public class BlockRqIssueHandler extends KernelEventHandler {

    private final InputOutputStateProvider fStateProvider;

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     * @param sp
     *            The state provider calling this handler
     */
    public BlockRqIssueHandler(IKernelAnalysisEventLayout layout, InputOutputStateProvider sp) {
        super(layout);
        fStateProvider = sp;
    }

    @Override
    public void handleEvent(@NonNull ITmfStateSystemBuilder ss, @NonNull ITmfEvent event) throws AttributeNotFoundException {
        ITmfEventField content = event.getContent();
        long ts = event.getTimestamp().getValue();

        Integer phydisk = ((Long) content.getField(getLayout().fieldBlockDeviceId()).getValue()).intValue();
        Long sector = NonNullUtils.checkNotNull((Long) content.getField(getLayout().fieldBlockSector()).getValue());
        Integer nrSector = ((Long) content.getField(getLayout().fieldBlockNrSector()).getValue()).intValue();
        Integer rwbs = content.getFieldValue(Integer.class, getLayout().fieldBlockRwbs());
        if (rwbs == null || nrSector == 0) {
            return;
        }

        DiskWriteModel disk = fStateProvider.getDisk(phydisk);
        Request request = disk.getWaitingRequest(sector);
        if (request == null) {
            BlockIO bio = new BlockIO(sector, nrSector, disk, rwbs);
            request = new Request(bio);
        }
        request.setNrSector(nrSector);
        request.setType(rwbs);
        disk.issueRequest(ts, request);
    }

}
