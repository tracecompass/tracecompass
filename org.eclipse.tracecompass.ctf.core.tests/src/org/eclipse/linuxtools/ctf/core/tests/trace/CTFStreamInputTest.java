/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinition;
import org.eclipse.linuxtools.ctf.core.tests.shared.CtfTestTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFStream;
import org.eclipse.linuxtools.ctf.core.trace.CTFStreamInput;
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
     * @throws CTFReaderException
     */
    @Before
    public void setUp() throws CTFReaderException {
        assumeTrue(testTrace.exists());
        fixture = new CTFStreamInput(new CTFStream(testTrace.getTrace()), createFile());
        fixture.setTimestampEnd(1L);
    }

    @NonNull
    private static File createFile() {
        File path = new File(testTrace.getPath());
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
        String result = fixture.getScopePath().toString();
        assertNotNull(result);
    }

    /**
     * Run the Stream getStream() method test.
     */
    @Test
    public void testGetStream() {
        CTFStream result = fixture.getStream();
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
    public void testEquals1() throws CTFReaderException {
        s1 = new CTFStreamInput(new CTFStream(testTrace.getTrace()),
                createFile());
        assertFalse(s1.equals(null));
    }

    @Test
    public void testEquals2() throws CTFReaderException {
        s1 = new CTFStreamInput(new CTFStream(testTrace.getTrace()),
                createFile());
        assertFalse(s1.equals(new Long(23L)));

    }

    @Test
    public void testEquals3() throws CTFReaderException {
        s1 = new CTFStreamInput(new CTFStream(testTrace.getTrace()),
                createFile());
        assertEquals(s1, s1);

    }

    @Test
    public void testEquals4() throws CTFReaderException {
        s1 = new CTFStreamInput(new CTFStream(testTrace.getTrace()),
                createFile());
        s2 = new CTFStreamInput(new CTFStream(testTrace.getTrace()),
                createFile());
        assertEquals(s1, s2);
    }
}