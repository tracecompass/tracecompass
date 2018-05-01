/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.table;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.VirtualTableQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;

/**
 * This interface represents a virtual table data provider. It returns a
 * response that will be used by viewers. Response encapsulates a status and a
 * virtual table model
 *
 * @author Yonni Chen
 * @param <M>
 *            Model that represent the column for a table, returned by fetchTree
 * @param <L>
 *            Model that represent a line in a table, returned by fetchLines
 * @since 4.0
 */
public interface ITmfVirtualTableDataProvider<M extends ITmfTreeDataModel, L extends IVirtualTableLine> extends ITmfTreeDataProvider<M> {

    /**
     * This methods computes a virtual table model. Then, it returns a
     * {@link TmfModelResponse} that contains the model.
     *
     * @param filter
     *            A query filter that contains a list of desired columns, a starting
     *            index and a number of requested lines
     * @param monitor
     *            A ProgressMonitor to cancel task
     *
     * @return A {@link TmfModelResponse} instance that encapsulate an
     *         {@link ITmfVirtualTableModel}
     */
    TmfModelResponse<ITmfVirtualTableModel<L>> fetchLines(VirtualTableQueryFilter filter, @Nullable IProgressMonitor monitor);
}