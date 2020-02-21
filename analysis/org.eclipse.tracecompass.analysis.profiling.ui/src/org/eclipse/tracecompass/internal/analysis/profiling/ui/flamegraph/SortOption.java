/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.profiling.ui.flamegraph;

/**
 * The sort option enum
 *
 * @author Bernd Hufmann
 */
enum SortOption {
    /** Sort by thread name*/
    BY_NAME,
    /** Sort by thread name reverse */
    BY_NAME_REV,
    /** Sort by thread ID */
    BY_ID,
    /** Sort by thread ID reverse*/
    BY_ID_REV;

    public static SortOption fromName(String name) {
        if (name.equals(SortOption.BY_NAME.name())) {
            return SortOption.BY_NAME;
        } else if (name.equals(SortOption.BY_NAME_REV.name())) {
            return SortOption.BY_NAME_REV;
        } else if (name.equals(SortOption.BY_ID.name())) {
            return SortOption.BY_ID;
        } else if (name.equals(SortOption.BY_ID_REV.name())) {
            return SortOption.BY_ID_REV;
        }
        return SortOption.BY_NAME;
    }
}
