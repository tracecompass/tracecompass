/*******************************************************************************
 * Copyright (c) 2017 Ericsson
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
 * The content presentation option enum
 *
 * @author Bernd Hufmann
 */
enum ContentPresentation {
    /** Show by thread */
    BY_THREAD,
    /** Aggregate threads */
    AGGREGATE_THREADS;

    public static ContentPresentation fromName(String name) {
        if (name.equals(ContentPresentation.BY_THREAD.name())) {
            return ContentPresentation.BY_THREAD;
        } else if (name.equals(ContentPresentation.AGGREGATE_THREADS.name())) {
            return ContentPresentation.AGGREGATE_THREADS;
        }
        return ContentPresentation.AGGREGATE_THREADS;
    }
}
