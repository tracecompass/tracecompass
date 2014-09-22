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

import static org.junit.Assert.assertNotNull;

import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputReaderTimestampComparator;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StreamInputReaderTimestampComparatorTest</code> contains
 * tests for the class <code>{@link StreamInputReaderTimestampComparator}</code>
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class CTFStreamInputReaderTimestampComparatorTest {

    private StreamInputReaderTimestampComparator fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new StreamInputReaderTimestampComparator();
    }

    /**
     * Run the StreamInputReaderTimestampComparator() constructor test.
     */
    @Test
    public void testStreamInputReaderTimestampComparator_1() {
        assertNotNull(fixture);
    }

}
