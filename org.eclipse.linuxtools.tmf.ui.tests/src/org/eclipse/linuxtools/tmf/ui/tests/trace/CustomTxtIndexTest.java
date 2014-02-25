/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Adapted for TMF Trace Model 1.0
 *   Alexandre Montplaisir - Port to JUnit4
 *   Marc-Andre Laperle - Adapted to CustomTxtTrace
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.trace;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTrace;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.linuxtools.tmf.core.trace.indexer.ITmfTraceIndexer;

/**
 * Test suite for indexing using a CustomTxtTrace.
 *
 * @author Marc-Andre Laperle
 */
public class CustomTxtIndexTest extends AbstractCustomTraceIndexTest {

    private static final String TRACE_DIRECTORY = System.getProperty("java.io.tmpdir") + File.separator + "dummyTxtTrace";
    private static final String TRACE_PATH = TRACE_DIRECTORY + File.separator + "test.txt";
    private static final String DEFINITION_PATH = "tracesets" + File.separator + "txt" + File.separator + "testTxtDefinition.xml";

    private static CustomTxtTraceDefinition createDefinition() {
        CustomTxtTraceDefinition[] definitions = CustomTxtTraceDefinition.loadAll(new File(DEFINITION_PATH).toString());
        return definitions[0];
    }

    @Override
    protected String getTraceDirectory() {
        return TRACE_DIRECTORY;
    }

    @Override
    protected TestTrace createTrace() throws Exception {
        CustomTxtTraceDefinition definition = createDefinition();
        final File file = new File(TRACE_PATH);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));) {
            for (int i = 0; i < NB_EVENTS; ++i) {
                SimpleDateFormat f = new SimpleDateFormat(TIMESTAMP_FORMAT);
                String eventStr = f.format(new Date(i)) + " hello world\n";
                writer.write(eventStr);
            }
        }

        return new TestTxtTrace(file.toString(), definition, BLOCK_SIZE);
    }

    private class TestTxtTrace extends CustomTxtTrace implements TestTrace {
        public TestTxtTrace(String path, CustomTxtTraceDefinition createDefinition, int blockSize) throws TmfTraceException {
            super(null, createDefinition, path, blockSize);
        }

        @Override
        protected ITmfTraceIndexer createIndexer(int interval) {
            return new TestIndexer(this, interval);
        }

        @Override
        public TestIndexer getIndexer() {
            return (TestIndexer) super.getIndexer();
        }
    }
}