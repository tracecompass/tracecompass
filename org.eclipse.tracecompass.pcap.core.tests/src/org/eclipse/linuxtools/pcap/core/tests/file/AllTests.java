/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.pcap.core.tests.file;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * File test suite
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        PcapFileOpenTest.class,
        PcapFileOpenFailTest.class,
        PcapFileReadTest.class,
        PcapFileEndiannessTest.class
})
public class AllTests {

}
