/******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.profiling.ui.callgraph;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density2.AbstractSegmentStoreDensityView;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density2.AbstractSegmentStoreDensityViewer;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.common.core.NonNullUtils;

/**
 * Call stack Density view displaying the call stack segments tree.
 *
 * @author Sonia Farrah
 */
public class CallGraphDensityView extends AbstractSegmentStoreDensityView {

    /** The view's ID */
    public static final @NonNull String ID = "org.eclipse.tracecompass.internal.analysis.timing.ui.callgraph.callgraphDensity"; //$NON-NLS-1$

    /**
     * Constructs a new density view.
     */
    public CallGraphDensityView() {
        super(ID);
    }

    @Override
    protected AbstractSegmentStoreTableViewer createSegmentStoreTableViewer(Composite parent) {
        return new CallGraphTableViewer(new TableViewer(parent, SWT.FULL_SELECTION | SWT.VIRTUAL)) {
            @Override
            protected void createProviderColumns() {
                super.createProviderColumns();
                Table t = (Table) getControl();
                t.setColumnOrder(new int[] { 2, 3, 0, 1 });
            }
        };
    }

    @Override
    protected AbstractSegmentStoreDensityViewer createSegmentStoreDensityViewer(Composite parent) {
        return new CallGraphDensityViewer(NonNullUtils.checkNotNull(parent));
    }
}