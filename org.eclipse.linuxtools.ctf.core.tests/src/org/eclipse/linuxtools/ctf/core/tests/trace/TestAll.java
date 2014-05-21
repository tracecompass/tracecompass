/*******************************************************************************
 * Copyright (c) 2013-2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.trace;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The class <code>TestAll</code> builds a suite that can be used to run all of
 * the tests within its package as well as within any subpackages of its
 * package.
 *
 * @author Matthew Khouzam
 * @version $Revision: 1.0 $
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    CTFTraceCallsitePerformanceTest.class,
    CTFTraceReaderTest.class,
    CTFTraceTest.class,
    CTFTraceGrowingTest.class,
    IOstructgenTest.class,
    MetadataTest.class,
    CTFStreamInputPacketIndexEntryTest.class,
    CTFStreamInputPacketIndexTest.class,
    CTFStreamInputReaderTest.class,
    CTFStreamInputReaderTimestampComparatorTest.class,
    CTFStreamInputTest.class,
    CTFStreamTest.class,
    UtilsTest.class
})
public class TestAll {

}
