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
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.viewers.events;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * SWTBot test suite for testing of the TMF events table.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        CallsiteEventsInTableTest.class,
        CollapseEventsInTableTest.class,
        ColorsViewTest.class,
        ColumnHeaderMenuTest.class,
        CopyToClipboardTest.class,
        FilterColorEditorTest.class,
        FilterViewerTest.class,
        FontEventEditorTest.class,
        MovableColumnEventsEditorTest.class,
        SDViewTest.class,
        TestTraceOffsetting.class,
        TmfAlignTimeAxisTest.class

})
public class AllTests {
}
