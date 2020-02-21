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

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * View for displaying a segment store analysis in a table.
 *
 * @author Geneviève Bastien
 * @since 1.2
 */
public class SegmentStoreTableView extends AbstractSegmentStoreTableView {

    /**
     * ID of this view
     */
    public static final String ID = "org.eclipse.tracecompass.analysis.timing.ui.segstore.table"; //$NON-NLS-1$

    @Override
    public void createPartControl(@Nullable Composite parent) {
        super.createPartControl(parent);
        IViewDescriptor descriptor = PlatformUI.getWorkbench().getViewRegistry().find(getViewId());
        if (descriptor != null) {
            setPartName(descriptor.getLabel());
        }
    }

    @Override
    protected @NonNull AbstractSegmentStoreTableViewer createSegmentStoreViewer(@NonNull TableViewer tableViewer) {
        // Set the title of this view
        String analysisId = NonNullUtils.nullToEmptyString(getViewSite().getSecondaryId());
        return new SegmentStoreTableViewer(tableViewer, analysisId);
    }
}
