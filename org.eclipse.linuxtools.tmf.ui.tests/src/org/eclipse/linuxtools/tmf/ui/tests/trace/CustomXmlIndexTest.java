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
 *   Marc-Andre Laperle - Adapted to CustomXmlTrace
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.trace;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlTrace;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.core.trace.indexer.ITmfTraceIndexer;

/**
 * Test suite for indexing using a CustomXmlTrace.
 *
 * @author Marc-Andre Laperle
 */
public class CustomXmlIndexTest extends AbstractCustomTraceIndexTest {

    private static final String TRACE_DIRECTORY = TmfTraceManager.getTemporaryDirPath() + File.separator + "dummyXmlTrace";
    private static final String TRACE_PATH = TRACE_DIRECTORY + File.separator + "test.xml";
    private static final String DEFINITION_PATH = "tracesets" + File.separator + "xml" + File.separator + "testDefinition.xml";

    private static CustomXmlTraceDefinition createDefinition() {
        CustomXmlTraceDefinition[] definitions = CustomXmlTraceDefinition.loadAll(new File(DEFINITION_PATH).toString());
        return definitions[0];
    }

    @Override
    protected String getTraceDirectory() {
        return TRACE_DIRECTORY;
    }

    @Override
    protected TestTrace createTrace() throws Exception {
        CustomXmlTraceDefinition definition = createDefinition();
        final File file = new File(TRACE_PATH);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));) {
            writer.write("<trace>");
            for (int i = 0; i < NB_EVENTS; ++i) {
                SimpleDateFormat f = new SimpleDateFormat(TIMESTAMP_FORMAT);
                String eventStr = "<element time=\"" + f.format(new Date(i)) + "\">message</element>\n";
                writer.write(eventStr);
            }
            writer.write("</trace>");
        }

        return new TestXmlTrace(file.toString(), definition, BLOCK_SIZE);
    }

    private class TestXmlTrace extends CustomXmlTrace implements TestTrace {
        public TestXmlTrace(String path, CustomXmlTraceDefinition createDefinition, int blockSize) throws TmfTraceException {
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