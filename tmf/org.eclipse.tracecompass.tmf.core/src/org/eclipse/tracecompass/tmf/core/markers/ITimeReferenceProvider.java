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

package org.eclipse.tracecompass.tmf.core.markers;

import java.util.function.Function;

/**
 * Marker Reference Provider, allows one to extract a marker time reference from
 * a given id string
 *
 * @author Matthew Khouzam
 * @since 7.1
 */
public interface ITimeReferenceProvider extends Function<String, ITimeReference> {

    /**
     * Get the reference for the specified reference id
     *
     * @param referenceId
     *            the reference id
     * @return a reference
     */
    @Override
    ITimeReference apply(String referenceId);

}
