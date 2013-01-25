/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial generation with CodePro tools
 *   Alexandre Montplaisir - Clean up, consolidate redundant tests
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTimestamp;
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

        CtfTmfTimestamp result = new CtfTmfTimestamp(timestamp);

        assertNotNull(result);
        //assertEquals("00:00:00.000 000 001", result.toString()); //$NON-NLS-1$
        assertEquals(0, result.getPrecision());
        assertEquals(-9, result.getScale());
        assertEquals(1L, result.getValue());
    }
}
