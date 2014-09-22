/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Etienne Bergeron - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.parser.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Master test suite for CTF parser.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    CtfLexerTest.class,
    CtfParserTest.class
})

public class AllCtfParserTests {

}
