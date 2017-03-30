/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.markers;

import org.eclipse.tracecompass.tmf.ui.markers.PeriodicMarkerEventSource.Reference;

/**
 * An adapter interface for traces, which allows the trace to provide a
 * reference for periodic markers.
 *
 * @since 2.4
 */
public interface IMarkerReferenceProvider {

    /**
     * Get the reference for the specified reference id
     *
     * @param referenceId
     *            the reference id
     * @return a reference
     */
    Reference getReference(String referenceId);
}
