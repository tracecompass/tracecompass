/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;

/**
 * This interface represents a tree data provider. It returns a computed model
 * that will be used by tree viewers.
 *
 * @author Yonni Chen
 * @param <T>
 *            Tree model extending {@link ITmfTreeDataModel}
 */
@FunctionalInterface
public interface ITmfTreeDataProvider<T extends ITmfTreeDataModel> {

    /**
     * This methods computes a tree model. Then, it returns a
     * {@link TmfModelResponse} that contains the model. Tree model will be used by
     * tree viewer to show entries as a tree or flat hierarchy
     *
     * @param filter
     *            A query filter that contains an array of time. Times are used for
     *            requesting data.
     * @param monitor
     *            A ProgressMonitor to cancel task
     * @return A {@link TmfModelResponse} instance
     */
    TmfModelResponse<List<T>> fetchTree(TimeQueryFilter filter, @Nullable IProgressMonitor monitor);
}
