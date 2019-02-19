/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs;

import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.Activator;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.trace.TmfXmlKernelTraceStub;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.statesystem.core.tests.shared.utils.IntervalInfo;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;

/**
 * Describe a test case for a linux unit test
 *
 * @author Geneviève Bastien
 */
public class LinuxTestCase {

    private final String fTraceFile;

    /**
     * Class to group a timestamp with a map of attributes and their expected
     * values
     */
    public static class PunctualInfo {

        private final long fTs;
        private final Map<String[], @Nullable Object> fValueMap;

        /**
         * Constructor
         *
         * @param ts
         *            Timestamp of this test data
         */
        public PunctualInfo(long ts) {
            fTs = ts;
            fValueMap = new HashMap<>();
        }

        /**
         * Get the timestamp this data is applied to
         *
         * @return The timestamp of this test data
         */
        public long getTimestamp() {
            return fTs;
        }

        /**
         * Get the test values
         *
         * @return The map of attribute path and values
         */
        public Map<String[], @Nullable Object> getValues() {
            return fValueMap;
        }

        /**
         * Add an attribute value to verify
         *
         * @param key
         *            The attribute path
         * @param value
         *            The value of this attribute at timestamp
         */
        public void addValue(String[] key, @Nullable Object value) {
            fValueMap.put(key, value);
        }
    }

    /**
     * Constructor
     *
     * @param filename
     *            The filename of the trace file
     */
    public LinuxTestCase(String filename) {
        fTraceFile = filename;
    }

    /**
     * Get the last part of the file name containing the test trace
     *
     * @return The name of the file
     */
    public String getTraceFileName() {
        return NonNullUtils.checkNotNull(FilenameUtils.getName(fTraceFile));
    }

    /**
     * Initializes the trace for this test case. This method will always create
     * a new trace. The caller must dispose of it the proper way.
     *
     * @return The {@link TmfXmlKernelTraceStub} created for this test case
     */
    public TmfXmlKernelTraceStub getKernelTrace() {
        TmfXmlKernelTraceStub trace = new TmfXmlKernelTraceStub();
        IPath filePath = Activator.getAbsoluteFilePath(fTraceFile);
        IStatus status = trace.validate(null, filePath.toOSString());
        if (!status.isOK()) {
            fail(status.getException().getMessage());
        }
        try {
            trace.initTrace(null, filePath.toOSString(), TmfEvent.class);
        } catch (TmfTraceException e) {
            fail(e.getMessage());
        }
        return trace;
    }

    /**
     * This method will return a set of attributes and their corresponding
     * expected intervals that will be tested with the actual intervals obtained
     * from the state system. It does not have to return all attributes of a
     * given state system, only the ones interesting for this test case.
     *
     * @return A set of {@link IntervalInfo} objects to verify
     */
    public Set<IntervalInfo> getTestIntervals() {
        return Collections.EMPTY_SET;
    }

    /**
     * This method will return a set of timestamps and their corresponding map
     * of attributes and state values. The attribute list does not have to
     * contain all attributes in the state system, only the ones that should be
     * tested.
     *
     * @return A set of {@link PunctualInfo} objects to verify
     */
    public Set<PunctualInfo> getPunctualTestData() {
        return Collections.EMPTY_SET;
    }

    @Override
    public String toString() {
        return getTraceFileName();
    }

}
