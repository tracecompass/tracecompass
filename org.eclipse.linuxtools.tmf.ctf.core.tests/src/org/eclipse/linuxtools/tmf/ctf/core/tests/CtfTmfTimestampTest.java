/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial generation with CodePro tools
 *   Alexandre Montplaisir - Clean up, consolidate redundant tests
 *   Patrick Tasse - Fix for local time zone
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ctf.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfTimestamp;
import org.junit.Test;

/**
 * The class <code>CtfTmfTimestampTest</code> contains tests for the class
 * <code>{@link CtfTmfTimestamp}</code>.
 *
 * @author ematkho
 * @version 1.0
 */
public class CtfTmfTimestampTest {

    /**
     * Run the CtfTmfTimestamp(long) constructor test.
     */
    @Test
    public void testCtfTmfTimestamp() {
        long timestamp = 1L;
        DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
        Date d = new Date(timestamp / 1000000);

        CtfTmfTimestamp result = new CtfTmfTimestamp(timestamp);

        assertNotNull(result);
        assertEquals(df.format(d) + " 000 001", result.toString());
        assertEquals(0, result.getPrecision());
        assertEquals(-9, result.getScale());
        assertEquals(1L, result.getValue());
    }
}
