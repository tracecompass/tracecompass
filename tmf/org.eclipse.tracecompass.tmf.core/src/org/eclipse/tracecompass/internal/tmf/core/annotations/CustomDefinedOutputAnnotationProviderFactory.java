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
 * Output annotation provider factory for trace level annotations for custom annotations (frames).
 */
public class CustomDefinedOutputAnnotationProviderFactory extends AbstractTmfTraceAdapterFactory {

    @Override
    protected @Nullable <T> T getTraceAdapter(ITmfTrace trace, @Nullable Class<T> adapterType) {
        if (null != adapterType && IOutputAnnotationProvider.class.equals(adapterType)) {
            return adapterType.cast(new CustomDefinedOutputAnnotationProvider());
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
