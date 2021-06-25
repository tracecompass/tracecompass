/*******************************************************************************
 * Copyright (c) 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.annotations;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.IOutputAnnotationProvider;
import org.eclipse.tracecompass.tmf.core.trace.AbstractTmfTraceAdapterFactory;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Output annotation provider factory for trace level annotations for lost events.
 */
public class LostEventsOutputAnnotationProviderFactory extends AbstractTmfTraceAdapterFactory {

    @Override
    protected <T> @Nullable T getTraceAdapter(ITmfTrace trace, @Nullable Class<T> adapterType) {
        if (null != adapterType && IOutputAnnotationProvider.class.equals(adapterType)) {
            IOutputAnnotationProvider adapter = new LostEventsOutputAnnotationProvider(trace);
            return adapterType.cast(adapter);
        }
        return null;
    }

    @Override
    public Class<?>[] getAdapterList() {
        return new Class[] {
                IOutputAnnotationProvider.class
        };
    }
}
