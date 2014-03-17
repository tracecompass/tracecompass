/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.ui.IViewReference;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests perspectives to make sure they have all the views
 *
 * @author Matthew Khouzam
 */
public abstract class AbstractPerspectiveChecker {

    private static SWTWorkbenchBot fBot;
    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    /**
     * The perspective id
     */
    protected String fPerspectiveId;
    /**
     * the view id collection
     */
    protected Collection<String> fViewIds;

    /** Test Class setup */
    @BeforeClass
    public static void beforeInit() {
        SWTBotUtil.failIfUIThread();

        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 50000; /* 50 second timeout */
        fLogger.addAppender(new NullAppender());
        fBot = new SWTWorkbenchBot();

        SWTBotUtil.closeView("welcome", fBot);

    }

    /**
     * Gets the perspective and checks if all the views specified in the list
     * are present in the perspective
     */
    @Test
    public void testPerspectiveForViews() {
        SWTBotUtil.switchToPerspective(fPerspectiveId);
        SWTBotUtil.waitForJobs();
        for (final String viewID : fViewIds) {
            List<SWTBotView> view = fBot.views(new BaseMatcher<String>() {

                @Override
                public boolean matches(Object item) {
                    if (!(item instanceof IViewReference)) {
                        return false;
                    }
                    IViewReference reference = (IViewReference) item;
                    return reference.getId().equals(viewID);
                }

                @Override
                public void describeTo(Description description) {
                }

            });
            assertEquals("view " + viewID + "is present", 1, view.size());
        }
    }

    /**
     * Gets the perspective and checks if all the views of that perspective are
     * in the list
     */
    @Test
    public void testPerspectiveComplete() {
        SWTBotUtil.switchToPerspective(fPerspectiveId);
        SWTBotUtil.waitForJobs();
        for (SWTBotView view : fBot.views()) {
            assertTrue("view " + view.toString() + "is present", fViewIds.contains(view.getViewReference().getId()));
        }
    }

}
