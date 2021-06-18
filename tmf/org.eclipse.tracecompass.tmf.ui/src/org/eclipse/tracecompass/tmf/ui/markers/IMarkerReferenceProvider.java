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

import org.eclipse.tracecompass.tmf.ui.markers.PeriodicMarkerEventSource.Reference;

/**
 * An adapter interface for traces, which allows the trace to provide a
 * reference for periodic markers.
 *
 * @since 3.0
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
