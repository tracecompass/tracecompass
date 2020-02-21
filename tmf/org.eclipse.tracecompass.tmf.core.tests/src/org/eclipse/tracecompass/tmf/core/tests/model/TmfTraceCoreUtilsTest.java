/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.model;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.Path;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceCoreUtils;
import org.junit.Test;

/**
 * Test suite for the TmfCoalescedEventRequest class.
 */
public class TmfTraceCoreUtilsTest {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").contains("Windows") ? true : false; //$NON-NLS-1$ //$NON-NLS-2$
    private static final char BACKSLASH_PUA = 0xF000 + '\\';
    private static final char COLON_PUA = 0xF000 + ':';

    /**
     * Test {@link TmfTraceCoreUtils#validateName(String)}
     */
    @Test
    public void testValidateName() {
        // @see org.eclipse.core.internal.resources.OS for invalid patterns
        if (IS_WINDOWS) {
            assertEquals("myName", TmfTraceCoreUtils.validateName("myName"));
            assertEquals("myName_", TmfTraceCoreUtils.validateName("myName_"));
            assertEquals("my Name", TmfTraceCoreUtils.validateName("my Name"));
            assertEquals("my_Name", TmfTraceCoreUtils.validateName("my\\Name"));
            assertEquals("_", TmfTraceCoreUtils.validateName("."));
            assertEquals("._", TmfTraceCoreUtils.validateName(".."));
            assertEquals("myName_", TmfTraceCoreUtils.validateName("myName:"));
            assertEquals("my_Name", TmfTraceCoreUtils.validateName("my/Name"));
            assertEquals("my_Name", TmfTraceCoreUtils.validateName("my\\Name"));
            assertEquals("my_Name", TmfTraceCoreUtils.validateName("my/Name"));
            assertEquals("myName_", TmfTraceCoreUtils.validateName("myName."));
            assertEquals("myName_", TmfTraceCoreUtils.validateName("myName*"));
            assertEquals("myName_", TmfTraceCoreUtils.validateName("myName>"));
            assertEquals("myName_", TmfTraceCoreUtils.validateName("myName<"));
            assertEquals("my_Name", TmfTraceCoreUtils.validateName("my?Name"));
            assertEquals("my_Name", TmfTraceCoreUtils.validateName("my\"Name"));
            assertEquals("my_Name", TmfTraceCoreUtils.validateName("my|Name"));
            assertEquals("_com1", TmfTraceCoreUtils.validateName("com1"));
            assertEquals("_clock$", TmfTraceCoreUtils.validateName("clock$"));
            return;
        }
        assertEquals("myName", TmfTraceCoreUtils.validateName("myName"));
        assertEquals("myName_", TmfTraceCoreUtils.validateName("myName_"));
        assertEquals("my\\ Name", TmfTraceCoreUtils.validateName("my\\ Name"));
        assertEquals("_.", TmfTraceCoreUtils.validateName("."));
        assertEquals("_..", TmfTraceCoreUtils.validateName(".."));
        assertEquals("myName_", TmfTraceCoreUtils.validateName("myName\0"));
        assertEquals("my_Name", TmfTraceCoreUtils.validateName("my/Name"));
    }

    /**
     * Test {@link TmfTraceCoreUtils#newSafePath(String)} and {@link TmfTraceCoreUtils#safePathToString(String)}
     */
    public void testSafePath() {
        String path1 = "my:name:is/santa";
        Path safePath1 = newSafePath(path1);
        assertEquals(safePath1.toPortableString(), TmfTraceCoreUtils.newSafePath(path1).toPortableString());

        String path2 = "my\\name\\is/santa";
        Path safePath2 = newSafePath(path2);
        assertEquals(safePath2.toPortableString(), TmfTraceCoreUtils.newSafePath(path2).toPortableString());

        assertEquals(path1, TmfTraceCoreUtils.safePathToString(safePath1.toPortableString()));
        assertEquals(path2, TmfTraceCoreUtils.safePathToString(safePath2.toPortableString()));
    }

    private static Path newSafePath(String path) {
        return new Path(path.replace('\\', BACKSLASH_PUA).replace(':', COLON_PUA));
    }
}
