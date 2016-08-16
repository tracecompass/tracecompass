/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.timing.ui.flamegraph;

import java.util.Comparator;

/**
 * Comparator to compare by thread name.
 *
 * @author Bernd Hufmann
 *
 */
class ThreadNameComparator implements Comparator<FlamegraphDepthEntry> {
    @Override
    public int compare(FlamegraphDepthEntry o1, FlamegraphDepthEntry o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
