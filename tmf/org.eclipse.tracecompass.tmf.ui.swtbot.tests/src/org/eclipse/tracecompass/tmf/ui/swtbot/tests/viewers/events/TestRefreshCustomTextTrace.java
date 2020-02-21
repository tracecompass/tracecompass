/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.viewers.events;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;

/**
 * Test refreshing a custom text trace after new content was added.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class TestRefreshCustomTextTrace extends TestRefreshTextTrace {

    private static final String TRACE_LOCATION = TmfTraceManager.getTemporaryDirPath() + File.separator + "test.txt";
    private static final String DEFINITION_PATH = "testfiles" + File.separator + "txt" + File.separator + "testTxtDefinition.xml";
    private static final String TRACE_TYPE_CUSTOM_TEXT = "custom.txt.trace:Custom Text:testtxt";
    private static final String TIMESTAMP_FORMAT = "dd/MM/yyyy HH:mm:ss:SSS";

    private static final long SECOND_TO_MILLISECOND = 1000;
    private static final long SECOND_TO_NANOSECOND = 1000000000;

    private long fNbWrittenEvents = 0;

    @Override
    protected String createTrace(long nbEvents) throws Exception {
        createDefinition();
        writeToTrace(nbEvents, false);

        return TRACE_LOCATION;
    }

    private static void createDefinition() throws URISyntaxException, IOException {
        File file = getBundleFile(TmfCoreTestPlugin.getDefault().getBundle(), new Path(DEFINITION_PATH));
        CustomTxtTraceDefinition[] definitions = CustomTxtTraceDefinition.loadAll(file.toString());
        for (CustomTxtTraceDefinition text : definitions) {
            text.save();
        }
    }

    private static File getBundleFile(Bundle bundle, IPath relativePath) throws URISyntaxException, IOException {
        return new File(FileLocator.toFileURL(FileLocator.find(bundle, relativePath, null)).toURI());
    }

    @Override
    protected void appendToTrace(long nbEvents) throws IOException {
        writeToTrace(nbEvents, true);
    }

    private void writeToTrace(long nbEvents, boolean append) throws IOException {
        final File file = new File(TRACE_LOCATION);
        try (FileWriter writer = new FileWriter(file, append)) {
            for (int i = 0; i < nbEvents; ++i) {
                SimpleDateFormat f = new SimpleDateFormat(TIMESTAMP_FORMAT);
                String eventStr = f.format(new Date(fNbWrittenEvents * SECOND_TO_MILLISECOND)) + " hello world\n";
                writer.write(eventStr);
                fNbWrittenEvents++;
            }
        }
    }

    @Override
    protected long getExpectedEndTimeStamp() {
        return (fNbWrittenEvents - 1) * SECOND_TO_NANOSECOND;
    }

    @Override
    protected String getTraceType() {
        return TRACE_TYPE_CUSTOM_TEXT;
    }

    @Override
    protected long getNbWrittenEvents() {
        return fNbWrittenEvents;
    }
}
