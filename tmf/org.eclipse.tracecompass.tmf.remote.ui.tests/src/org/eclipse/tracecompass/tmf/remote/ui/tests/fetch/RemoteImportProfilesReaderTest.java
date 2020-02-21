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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportConnectionNodeElement;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportProfileElement;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportProfilesReader;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportTraceGroupElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageFilesElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageTraceElement;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Test reading profiles from xml
 */
public class RemoteImportProfilesReaderTest extends AbstractRemoteImportProfilesIOTest {

    private static final Path INVALID_EMPTY_PROFILE_PATH = new Path(
            "resources/invalid_profile_empty_profile.xml"); //$NON-NLS-1$
    private static final Path INVALID_MISSING_FILES_PROFILE_PATH = new Path(
            "resources/invalid_profile_missing_files.xml"); //$NON-NLS-1$
    private static final Path INVALID_MISSING_ROOT_PROFILE_PATH = new Path(
            "resources/invalid_profile_missing_root.xml"); //$NON-NLS-1$

    /**
     * Test a valid profiles file.
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testValidateValid() throws Exception {
        validatePath(VALID_PROFILE_PATH);
    }

    /**
     * Test a profiles file with an empty profile element.
     *
     * @throws Exception
     *             on error
     */
    @Test(expected = SAXException.class)
    public void testValidateInvalidEmptyProfile() throws Exception {
        validatePath(INVALID_EMPTY_PROFILE_PATH);
    }

    /**
     * Test a profiles file missing a files element under the trace element.
     *
     * @throws Exception
     *             on error
     */
    @Test(expected = SAXException.class)
    public void testValidateInvalidMissingFiles() throws Exception {
        validatePath(INVALID_MISSING_FILES_PROFILE_PATH);
    }

    /**
     * Test a profiles file missing a root directory.
     *
     * @throws Exception
     *             on error
     */
    @Test(expected = SAXException.class)
    public void testValidateInvalidMissingRoot() throws Exception {
        validatePath(INVALID_MISSING_ROOT_PROFILE_PATH);
    }

    /**
     * Test loading elements from a profiles file.
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testLoadElements() throws Exception {
        TracePackageElement[] elements = loadElementsFromPath(VALID_PROFILE_PATH);
        assertEquals("profile element count", 2, elements.length); //$NON-NLS-1$
        TracePackageElement element = elements[0];
        assertTrue(element instanceof RemoteImportProfileElement);

        RemoteImportProfileElement profileElement = (RemoteImportProfileElement) element;
        assertEquals("myProfile", profileElement.getProfileName()); //$NON-NLS-1$

        Image image = profileElement.getImage();
        assertNotNull(image);
        image.dispose();

        assertEquals("profile children count", 2, profileElement.getChildren().length); //$NON-NLS-1$

        // First node
        element = getElementOfClass(RemoteImportConnectionNodeElement.class, profileElement.getChildren()).get(0);
        assertTrue(element instanceof RemoteImportConnectionNodeElement);
        RemoteImportConnectionNodeElement nodeElement = (RemoteImportConnectionNodeElement) element;
        assertEquals("myhost", nodeElement.getName()); //$NON-NLS-1$
        assertEquals("ssh://user@127.0.0.1:22", nodeElement.getURI()); //$NON-NLS-1$
        image = nodeElement.getImage();
        assertNotNull(image);

        element = getElementOfClass(RemoteImportTraceGroupElement.class, nodeElement.getChildren()).get(0);
        assertTrue(element instanceof RemoteImportTraceGroupElement);
        RemoteImportTraceGroupElement traceGroupElement = (RemoteImportTraceGroupElement) element;
        assertEquals("/home/user/traces/test/test_with_lttng", traceGroupElement.getRootImportPath()); //$NON-NLS-1$
        assertTrue(traceGroupElement.isRecursive());
        image = traceGroupElement.getImage();
        assertNotNull(image);

        element = getElementOfClass(TracePackageTraceElement.class, traceGroupElement.getChildren()).get(0);
        assertTrue(element instanceof TracePackageTraceElement);
        TracePackageTraceElement traceElement = (TracePackageTraceElement) element;
        assertEquals("test.log.(group1)", traceElement.getText()); //$NON-NLS-1$
        assertEquals("org.eclipse.tracecompass.tmf.remote.ui.test.tracetype1", traceElement.getTraceType()); //$NON-NLS-1$

        element = getElementOfClass(TracePackageFilesElement.class, traceElement.getChildren()).get(0);
        assertTrue(element instanceof TracePackageFilesElement);
        TracePackageFilesElement traceFilesElement = (TracePackageFilesElement) element;
        assertEquals(".*test\\.log\\.(\\d+)", traceFilesElement.getFileName()); //$NON-NLS-1$
        image = traceFilesElement.getImage();
        assertNotNull(image);

        // Second node
        element = getElementOfClass(RemoteImportConnectionNodeElement.class, profileElement.getChildren()).get(1);
        assertTrue(element instanceof RemoteImportConnectionNodeElement);
        nodeElement = (RemoteImportConnectionNodeElement) element;
        assertEquals("myhost3", nodeElement.getName()); //$NON-NLS-1$
        assertEquals("ssh://user@127.0.0.1:22", nodeElement.getURI()); //$NON-NLS-1$
        image = nodeElement.getImage();
        assertNotNull(image);

        element = getElementOfClass(RemoteImportTraceGroupElement.class, nodeElement.getChildren()).get(0);
        assertTrue(element instanceof RemoteImportTraceGroupElement);
        traceGroupElement = (RemoteImportTraceGroupElement) element;
        assertEquals("/home", traceGroupElement.getRootImportPath()); //$NON-NLS-1$
        assertFalse(traceGroupElement.isRecursive());
        image = traceGroupElement.getImage();
        assertNotNull(image);

        element = getElementOfClass(TracePackageTraceElement.class, traceGroupElement.getChildren()).get(0);
        assertTrue(element instanceof TracePackageTraceElement);
        traceElement = (TracePackageTraceElement) element;
        assertEquals("", traceElement.getText()); //$NON-NLS-1$
        assertEquals("trace.type", traceElement.getTraceType()); //$NON-NLS-1$

        element = getElementOfClass(TracePackageFilesElement.class, traceElement.getChildren()).get(0);
        assertTrue(element instanceof TracePackageFilesElement);
        traceFilesElement = (TracePackageFilesElement) element;
        assertEquals(".*", traceFilesElement.getFileName()); //$NON-NLS-1$
        image = traceFilesElement.getImage();
        assertNotNull(image);

    }

    private static void validatePath(IPath profilePath) throws Exception {
        try (FileInputStream inputStream = new FileInputStream(
                getProfilesFile(profilePath))) {
            RemoteImportProfilesReader.validate(inputStream);
        }
    }

    private static TracePackageElement[] loadElementsFromPath(IPath profilePath)
            throws Exception {
        try (FileInputStream inputStream = new FileInputStream(
                getProfilesFile(profilePath))) {
            return RemoteImportProfilesReader.loadElementsFromProfiles(inputStream);
        }
    }

    private static <T extends TracePackageElement> List<T> getElementOfClass(
            Class<T> clazz, TracePackageElement[] elements) {
        List<T> result = new ArrayList<>();
        for (TracePackageElement element : elements) {
            if (clazz.isInstance(element)) {
                result.add(clazz.cast(element));
            }
        }
        return result;
    }
}
