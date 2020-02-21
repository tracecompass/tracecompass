/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.remote.ui.tests.fetch;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportConnectionNodeElement;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportProfileElement;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportProfilesReader;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportProfilesWriter;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportTraceGroupElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageFilesElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageTraceElement;
import org.junit.Test;

/**
 * Test writing profiles from XML
 */
public class RemoteImportProfilesWriterTest extends
        AbstractRemoteImportProfilesIOTest {

    private static final String ENCODING = "UTF-8"; //$NON-NLS-1$
    private static final String LINE_SEPARATOR_PROPERTY = "line.separator";

    /**
     * Test writing a profiles file.
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testValidateValid() throws Exception {
        String writtenXML = RemoteImportProfilesWriter.writeProfilesToXML(generateElements());
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                writtenXML.getBytes(ENCODING));
        RemoteImportProfilesReader.validate(inputStream);

        File expectedFile = getProfilesFile(VALID_PROFILE_PATH);
        String expectedContent = new String(
                Files.readAllBytes(Paths.get(expectedFile.toURI())), ENCODING);

        // On windows, \r\n will be written to the XML. Replace them with \n for
        // proper comparison.
        String sysLineSeparator = System.getProperty(LINE_SEPARATOR_PROPERTY);
        writtenXML = writtenXML.replace(sysLineSeparator, "\n");

        assertEquals(expectedContent, writtenXML);
    }

    private static TracePackageElement[] generateElements() {
        RemoteImportProfileElement profileElement = new RemoteImportProfileElement(
                null, "myProfile"); //$NON-NLS-1$
        RemoteImportConnectionNodeElement nodeElement = new RemoteImportConnectionNodeElement(profileElement,
                "myhost", "ssh://user@127.0.0.1:22"); //$NON-NLS-1$//$NON-NLS-2$
        RemoteImportTraceGroupElement traceGroupElement = new RemoteImportTraceGroupElement(
                nodeElement, "/home/user/traces/test/test_with_lttng"); //$NON-NLS-1$
        traceGroupElement.setRecursive(true);
        // Profile 1 > Node 1 > Group 1
        TracePackageTraceElement traceElement = new TracePackageTraceElement(
                traceGroupElement,
                "test.log.(group1)", "org.eclipse.tracecompass.tmf.remote.ui.test.tracetype1"); //$NON-NLS-1$//$NON-NLS-2$
        new TracePackageFilesElement(traceElement, ".*test\\.log\\.(\\d+)"); //$NON-NLS-1$
        traceElement = new TracePackageTraceElement(
                traceGroupElement,
                "TestLog.(group1)", "org.eclipse.tracecompass.tmf.remote.ui.test.tracetype2"); //$NON-NLS-1$//$NON-NLS-2$
        new TracePackageFilesElement(traceElement, ".*TestLog\\.(\\d+)"); //$NON-NLS-1$

        // Profile 1 > Node 1 > Group 2
        traceGroupElement = new RemoteImportTraceGroupElement(nodeElement,
                "/home/user/traces/test/"); //$NON-NLS-1$
        traceGroupElement.setRecursive(false);
        traceElement = new TracePackageTraceElement(traceGroupElement,
                "lttng/(group1)", "org.eclipse.linuxtools.tmf.ui.type.ctf"); //$NON-NLS-1$//$NON-NLS-2$
        new TracePackageFilesElement(traceElement, "lttng/(.*)"); //$NON-NLS-1$

        // Profile 1 > Node 2 > Group 1
        nodeElement = new RemoteImportConnectionNodeElement(profileElement,"myhost3", "ssh://user@127.0.0.1:22"); //$NON-NLS-1$//$NON-NLS-2$
        traceGroupElement = new RemoteImportTraceGroupElement(nodeElement, "/home"); //$NON-NLS-1$
        traceGroupElement.setRecursive(false);
        traceElement = new TracePackageTraceElement(traceGroupElement, "", "trace.type"); //$NON-NLS-1$//$NON-NLS-2$
        new TracePackageFilesElement(traceElement, ".*"); //$NON-NLS-1$

        // Profile 2 > Node 1 > Group 1
        RemoteImportProfileElement profileElement2 = new RemoteImportProfileElement(
                null, "myProfile2"); //$NON-NLS-1$
        RemoteImportConnectionNodeElement nodeElement2 = new RemoteImportConnectionNodeElement(profileElement2,
                "myhost2", "ssh://user@142.111.222.333:22"); //$NON-NLS-1$//$NON-NLS-2$
        traceGroupElement = new RemoteImportTraceGroupElement(nodeElement2,
                "/home/user/traces/test/"); //$NON-NLS-1$
        traceGroupElement.setRecursive(false);
        traceElement = new TracePackageTraceElement(traceGroupElement,
                "lttng/(group1)", "org.eclipse.linuxtools.tmf.ui.type.ctf"); //$NON-NLS-1$//$NON-NLS-2$
        new TracePackageFilesElement(traceElement, "lttng/(.*)"); //$NON-NLS-1$

        return new TracePackageElement[] { profileElement, profileElement2 };
    }
}
