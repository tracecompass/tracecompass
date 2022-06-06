/**********************************************************************
 * Copyright (c) 2022 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore;

import java.util.List;

import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.VirtualTableCell;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.VirtualTableLine;

/**
 * Segment store table line model
 *
 * @author Kyrollos Bekhet
 */
public class SegmentStoreTableLine extends VirtualTableLine {

    public SegmentStoreTableLine(List<VirtualTableCell> cellData, long index) {
        super(index, cellData);
    }

}
