/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Delisle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.tests.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.DownloadTraceHttpHelper;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.TraceDownloadStatus;
import org.eclipse.tracecompass.tmf.ui.tests.TmfUITestPlugin;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the {@link DownloadTraceHttpHelper} class
 *
 * @author Simon Delisle
 */
public class DownloadTraceHttpHelperTest {

    private static String fTestTrace1Url = "http://archive.eclipse.org/tracecompass/test-traces/tmf/syslog";
    private static String fTestTrace2Url = "http://archive.eclipse.org/tracecompass/test-traces/tmf/syslog_collapse";
    private static String fTraceArchiveUrl = "http://archive.eclipse.org/tracecompass/test-traces/tmf/syslogs.zip";
    private static String fDestinationDirectory;

    /**
     * Setup class
     *
     * @throws Exception
     *             if an exception occurs
     */
    @BeforeClass
    public static void beforeClass() throws Exception {
        fDestinationDirectory = TmfUITestPlugin.getDefault().getStateLocation().append("httpDownloadTestDirectory").toOSString();
    }

    /**
     * Cleanup after each test
     *
     * @throws IOException
     *             if an exception occurs
     */
    @After
    public void afterTest() throws IOException {
        File destFile = new File(fDestinationDirectory);
        FileUtils.deleteDirectory(destFile);
    }

    /**
     * Test the download and import operation for a trace file
     */
    @Test
    public void testTraceDownload() {
        TraceDownloadStatus status = DownloadTraceHttpHelper.downloadTrace(fTestTrace1Url, fDestinationDirectory);
        assumeFalse(status.isTimeout());
        assertTrue(status.isOk());
        validateSingleDownload(status.getDownloadedFile(), "syslog");
    }

    /**
     * Test the download and import operation for a trace file
     */
    @Test
    public void testMutlipleTracesDownload() {
        List<String> tracesUrl = new ArrayList<>();
        tracesUrl.add(fTestTrace1Url);
        tracesUrl.add(fTestTrace2Url);

        TraceDownloadStatus multipleStatus = DownloadTraceHttpHelper.downloadTraces(tracesUrl, fDestinationDirectory);
        assumeFalse(multipleStatus.isTimeout());
        assertTrue(multipleStatus.isOk());

        List<File> downloadedTraces = new ArrayList<>();
        for (TraceDownloadStatus status : multipleStatus.getChildren()) {
            downloadedTraces.add(status.getDownloadedFile());
        }

        // Make sure that there is only two traces
        assertEquals(2, downloadedTraces.size());
        validateMultipleDownload(downloadedTraces);
    }

    /**
     * Test the download and import operation for a trace file
     */
    @Test
    public void testArchiveTraceDownload() {
        TraceDownloadStatus status = DownloadTraceHttpHelper.downloadTrace(fTraceArchiveUrl, fDestinationDirectory);
        assumeFalse(status.isTimeout());
        assertTrue(status.isOk());
        validateSingleDownload(status.getDownloadedFile(), "syslogs.zip");
    }

    private static void validateSingleDownload(File downloadedFile, String expectedFileName) {
        // Make sure that the name was correctly identify
        assertEquals(expectedFileName, downloadedFile.getName());

        // Check if the directory contains only one trace
        File dest = new File(fDestinationDirectory);
        File[] listFiles = dest.listFiles();
        assertEquals(1, listFiles.length);
        assertEquals(expectedFileName, listFiles[0].getName());
        assertTrue(listFiles[0].exists());
    }

    private static void validateMultipleDownload(List<File> downloadedFile) {
        // Check if the directory contains two traces
        File dest = new File(fDestinationDirectory);
        File[] listFiles = dest.listFiles();
        assertEquals(2, listFiles.length);
        for (File file : listFiles) {
            assertTrue(file.exists());
        }
    }

}
