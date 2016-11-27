/******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.parsers.custom;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceImportException;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.junit.Test;

/**
 * Common code for custom trace type extension points tests.
 */
public abstract class AbstractCustomTraceExtensionTest {

    /**
     * @return Get the extension Id that will provide the trace type to test.
     */
    protected abstract String getExtensionId();

    /**
     * @return Get the path of the trace to test with the trace type provided by
     *         an extension.
     */
    protected abstract String getTestTracePath();

    /**
     * Verifies that a trace type provided by an extension is present.
     */
    @Test
    public void testTraceTypePresence() {
        assertNotNull(TmfTraceType.getTraceType(getExtensionId()));
    }

    /**
     * Verifies that a trace type contributed by an extension can validate a valid trace.
     *
     * @throws TmfTraceImportException
     *             on error
     */
    @Test
    public void testValidate() throws TmfTraceImportException {
        final Predicate<TraceTypeHelper> predicateTracetypeIdEquals = (t) -> t.getTraceTypeId().equals(getExtensionId());
        @NonNull List<TraceTypeHelper> traceTypes = TmfTraceType.selectTraceType(getTestTracePath(), getExtensionId());
        String failureMessage = String.format("Could not find expected custom trace type %s in extensions", getExtensionId());
        assertTrue(failureMessage, traceTypes.stream().anyMatch(predicateTracetypeIdEquals));
    }
}
