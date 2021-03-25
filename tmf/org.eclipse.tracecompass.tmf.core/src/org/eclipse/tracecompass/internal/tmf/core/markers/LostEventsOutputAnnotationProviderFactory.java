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

package org.eclipse.tracecompass.internal.tmf.core.markers;

import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.IOutputAnnotationProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.LostEventOutputAnnotationProvider;
import org.eclipse.tracecompass.tmf.core.trace.AbstractTmfTraceAdapterFactory;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Output annotation provider factory for trace level annotations for lost events.
 */
public class LostEventsOutputAnnotationProviderFactory extends AbstractTmfTraceAdapterFactory {

    @Override
    protected <T> T getTraceAdapter(ITmfTrace trace, Class<T> adapterType) {
        if (IOutputAnnotationProvider.class.equals(adapterType)) {
            IOutputAnnotationProvider adapter = new LostEventOutputAnnotationProvider(trace);
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
