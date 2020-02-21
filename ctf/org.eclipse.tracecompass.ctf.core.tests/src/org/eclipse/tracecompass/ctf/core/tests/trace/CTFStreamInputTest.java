/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfTestTraceUtils;
import org.eclipse.tracecompass.ctf.core.trace.CTFStreamInput;
import org.eclipse.tracecompass.ctf.core.trace.ICTFStream;
import org.eclipse.tracecompass.internal.ctf.core.trace.CTFStream;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StreamInputTest</code> contains tests for the class
 * <code>{@link CTFStreamInput}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class CTFStreamInputTest {

    private static final CtfTestTrace testTrace = CtfTestTrace.KERNEL;

    private CTFStreamInput fixture;

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFException
     */
    @Before
    public void setUp() throws CTFException {
        fixture = new CTFStreamInput(new CTFStream(CtfTestTraceUtils.getTrace(testTrace)), createFile());
        fixture.setTimestampEnd(1L);
    }

    private static @NonNull File createFile() throws CTFException {
        File path = new File(CtfTestTraceUtils.getTrace(testTrace).getPath());
        final File[] listFiles = path.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.contains("hann")) {
                    return true;
                }
                return false;
            }
        });
        assertNotNull(listFiles);
        final File returnFile = listFiles[0];
        assertNotNull(returnFile);
        return returnFile;
    }

    /**
     * Run the StreamInput(Stream,FileChannel,File) constructor test.
     */
    @Test
    public void testStreamInput() {
        assertNotNull(fixture);
    }

    /**
     * Run the String getFilename() method test.
     */
    @Test
    public void testGetFilename() {
        String result = fixture.getFilename();
        assertNotNull(result);
    }

    /**
     * Run the String getPath() method test.
     */
    @Test
    public void testGetPath() {
        String result = fixture.getScopePath().getPath();
        assertNotNull(result);
    }

    /**
     * Run the Stream getStream() method test.
     */
    @Test
    public void testGetStream() {
        ICTFStream result = fixture.getStream();
        assertNotNull(result);
    }

    /**
     * Run the long getTimestampEnd() method test.
     */
    @Test
    public void testGetTimestampEnd() {
        long result = fixture.getTimestampEnd();
        assertTrue(0L < result);
    }

    /**
     * Run the Definition lookupDefinition(String) method test.
     */
    @Test
    public void testLookupDefinition() {
        IDefinition result = fixture.lookupDefinition("id");
        assertNull(result);
    }

    /**
     * Run the void setTimestampEnd(long) method test.
     */
    @Test
    public void testSetTimestampEnd() {
        fixture.setTimestampEnd(1L);
        assertEquals(fixture.getTimestampEnd(), 1L);
    }

    CTFStreamInput s1;
    CTFStreamInput s2;

    @Test
    public void testEquals1() throws CTFException {
        s1 = new CTFStreamInput(new CTFStream(CtfTestTraceUtils.getTrace(testTrace)),
                createFile());
        assertFalse(s1.equals(null));
    }

    @Test
    public void testEquals2() throws CTFException {
        s1 = new CTFStreamInput(new CTFStream(CtfTestTraceUtils.getTrace(testTrace)),
                createFile());
        assertFalse(s1.equals(Long.valueOf(23)));

    }

    @Test
    public void testEquals3() throws CTFException {
        s1 = new CTFStreamInput(new CTFStream(CtfTestTraceUtils.getTrace(testTrace)),
                createFile());
        assertEquals(s1, s1);

    }

    @Test
    public void testEquals4() throws CTFException {
        s1 = new CTFStreamInput(new CTFStream(CtfTestTraceUtils.getTrace(testTrace)),
                createFile());
        s2 = new CTFStreamInput(new CTFStream(CtfTestTraceUtils.getTrace(testTrace)),
                createFile());
        assertEquals(s1, s2);
    }
}