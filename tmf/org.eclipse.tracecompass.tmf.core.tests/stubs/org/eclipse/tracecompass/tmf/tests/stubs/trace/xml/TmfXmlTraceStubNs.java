/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.trace.xml;

import static org.junit.Assert.fail;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;

/**
 * An XML development trace using a custom XML trace definition and schema.
 *
 * This class will typically be used to build custom traces to unit test more
 * complex functionalities like analyzes or to develop and test data-driven
 * analyzes.
 *
 * This class wraps a custom XML trace and rewrites the returned events in the
 * getNext() method so that event's fields are the ones defined in <field ... />
 * elements instead of those defined in the custom XML parser. This way, each
 * event can have a different set of fields. This class can, for example, mimic
 * a CTF trace.
 *
 * The timestamps of this trace will be in nanoseconds
 *
 * @author Geneviève Bastien
 */
public class TmfXmlTraceStubNs extends TmfXmlTraceStub {

    private static final String DEVELOPMENT_TRACE_PARSER_PATH = "TmfXmlDevelopmentTraceNs.xml"; //$NON-NLS-1$

    /**
     * Validate and initialize a {@link TmfXmlTraceStubNs} object
     *
     * @param absolutePath
     *            The absolute file path of the trace file
     * @return The trace
     */
    public static TmfXmlTraceStubNs setupTrace(IPath absolutePath) {
        TmfXmlTraceStubNs trace = new TmfXmlTraceStubNs();
        IStatus status = trace.validate(null, absolutePath.toOSString());
        if (!status.isOK()) {
            fail(status.getException().getMessage());
        }
        try {
            trace.initTrace(null, absolutePath.toOSString(), TmfEvent.class);
        } catch (TmfTraceException e) {
            trace.dispose();
            fail(e.getMessage());
        }
        return trace;
    }

    @Override
    protected @NonNull String getParserFileName() {
        return DEVELOPMENT_TRACE_PARSER_PATH;
    }

}
