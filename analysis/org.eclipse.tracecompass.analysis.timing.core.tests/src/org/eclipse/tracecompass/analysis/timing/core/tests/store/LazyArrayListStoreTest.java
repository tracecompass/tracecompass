/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.tests.store;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.analysis.timing.core.store.LazyArrayListStore;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;

/**
 * Unit tests for intersecting elements in an LazyArrayListStore
 *
 * @author Matthew Khouzam
 */
public class LazyArrayListStoreTest extends AbstractTestSegmentStore {

    @Override
    protected ISegmentStore<@NonNull ISegment> getSegmentStore() {
        return new LazyArrayListStore<>();
    }
}