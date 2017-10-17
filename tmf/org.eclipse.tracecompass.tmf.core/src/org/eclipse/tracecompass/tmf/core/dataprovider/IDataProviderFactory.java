/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.dataprovider;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Implementation of this interface can be contributed using the
 * org.eclipse.tracecompass.tmf.core.dataprovider extension point and is used to
 * create {@link ITmfTreeDataProvider}
 *
 * @author Simon Delisle
 * @since 3.2
 */
public interface IDataProviderFactory {

    /**
     * Create a {@link ITmfTreeDataProvider} for the given trace. If this factory
     * does not know how to handle the given trace it will return null
     *
     * @param trace
     *            A trace
     * @return {@link ITmfTreeDataProvider} that can be use for the given trace
     */
    @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(@NonNull ITmfTrace trace);

}
