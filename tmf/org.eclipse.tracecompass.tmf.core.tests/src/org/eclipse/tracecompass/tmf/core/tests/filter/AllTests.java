/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Patrick Tasse - Added filter node tests
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.filter;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Filter tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TmfFilterAndNodeTest.class,
    TmfFilterCompareNodeTest.class,
    TmfFilterContainsNodeTest.class,
    TmfFilterEqualsNodeTest.class,
    TmfFilterMatchesNodeTest.class,
    TmfFilterNodeTest.class,
    TmfFilterOrNodeTest.class,
    TmfFilterRootNodeTest.class,
    TmfFilterTraceTypeNodeTest.class,
    TmfCollapseFilterTest.class,
})
public class AllTests {

}
