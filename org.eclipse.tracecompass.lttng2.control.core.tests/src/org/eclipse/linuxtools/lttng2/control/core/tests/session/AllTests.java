/**********************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Guilliano Molaire - Initial API and implementation
 *********************************************************************/
package org.eclipse.linuxtools.lttng2.control.core.tests.session;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Run all the tests in the lttng2.core.session plugin.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    SessionConfigGeneratorTest.class
})
public class AllTests {

}
