/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.markers;

import org.eclipse.tracecompass.tmf.core.markers.ITimeReference;
import org.eclipse.tracecompass.tmf.ui.markers.PeriodicMarkerEventSource.Reference;

/**
 * An adapter interface for traces, which allows the trace to provide a
 * reference for periodic markers.
 *
 * @since 3.0
 * @deprecated, use {@link org.eclipse.tracecompass.tmf.core.markers.ITimeReferenceProvider} instead
 */
@Deprecated
public interface IMarkerReferenceProvider extends org.eclipse.tracecompass.tmf.core.markers.ITimeReferenceProvider {

    /**
     * Get the reference for the specified reference id
     *
     * @param referenceId
     *            the reference id
     * @return a reference
     */
    Reference getReference(String referenceId);

    /**
     * @since 7.0
     */
    @Override
    default ITimeReference apply(String referenceId) {
        return getReference(referenceId);
    }
}
