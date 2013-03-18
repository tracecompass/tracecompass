/*******************************************************************************
 * Copyright (c) 2011-2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.dialogs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.Criteria;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDFilterProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDGraphNodeSupporter;
import org.junit.Test;

/**
 *  Test cases to test Criteria class.
 */
public class CriteriaTest {

    /**
     * Test default constructor.
     */
    @Test
    public void testCriteria() {
        Criteria criteria = new Criteria();
        assertFalse("testCriteria", criteria.isAsyncMessageReturnSelected());
        assertFalse("testCriteria", criteria.isAsyncMessageSelected());
        assertFalse("testCriteria", criteria.isCaseSenstiveSelected());
        assertFalse("testCriteria", criteria.isLifeLineSelected());
        assertFalse("testCriteria", criteria.isStopSelected());
        assertFalse("testCriteria", criteria.isSyncMessageReturnSelected());
        assertFalse("testCriteria", criteria.isSyncMessageSelected());
        assertNull("testCriteria",  criteria.getExpression());
        assertNull("testCriteria",  criteria.getPattern());
    }

    /**
     * Test copy constructor.
     */
    @Test
    public void testCriteriaCriteria() {
        Criteria criteria = new Criteria();
        criteria.setExpression("test");
        criteria.setLifeLineSelected(true);
        criteria.setSyncMessageSelected(true);

        Criteria copy = new Criteria(criteria);

        assertEquals("testCriteriaCriteria", criteria.isAsyncMessageReturnSelected(), copy.isAsyncMessageReturnSelected());
        assertEquals("testCriteriaCriteria", criteria.isAsyncMessageSelected(), copy.isAsyncMessageSelected());
        assertEquals("testCriteriaCriteria", criteria.isCaseSenstiveSelected(), copy.isCaseSenstiveSelected());
        assertEquals("testCriteriaCriteria", criteria.isLifeLineSelected(), copy.isLifeLineSelected());
        assertEquals("testCriteriaCriteria", criteria.isStopSelected(), copy.isStopSelected());
        assertEquals("testCriteriaCriteria", criteria.isSyncMessageReturnSelected(), copy.isSyncMessageReturnSelected());
        assertEquals("testCriteriaCriteria", criteria.isSyncMessageSelected(), copy.isSyncMessageSelected());
        assertEquals("testCriteriaCriteria", criteria.getExpression(), copy.getExpression());
        assertEquals("testCriteriaCriteria", criteria.getPattern().pattern(), copy.getPattern().pattern());
    }

    /**
     * Test accessor methods.
     */
    @Test
    public void testAccessors() {
        Criteria criteria = new Criteria();
        criteria.setAsyncMessageReturnSelected(true);
        criteria.setAsyncMessageSelected(true);
        criteria.setCaseSenstiveSelected(true);
        criteria.setLifeLineSelected(true);
        criteria.setStopSelected(true);
        criteria.setSyncMessageReturnSelected(true);
        criteria.setSyncMessageSelected(true);
        criteria.setExpression("test.*");

        // set true to all flags
        assertTrue("testAccessors", criteria.isAsyncMessageReturnSelected());
        assertTrue("testAccessors", criteria.isAsyncMessageSelected());
        assertTrue("testAccessors", criteria.isCaseSenstiveSelected());
        assertTrue("testAccessors", criteria.isLifeLineSelected());
        assertTrue("testAccessors", criteria.isStopSelected());
        assertTrue("testAccessors", criteria.isSyncMessageReturnSelected());
        assertTrue("testAccessors", criteria.isSyncMessageSelected());
        assertEquals("testAccessors",  "test.*", criteria.getExpression());
        assertNotNull("testAccessors",  criteria.getPattern());
        assertEquals("testAccessors", "test.*", criteria.getPattern().pattern());
        assertEquals("testAccessors", 0, criteria.getPattern().flags() & Pattern.CASE_INSENSITIVE);

        // set false to all flags
        criteria.setAsyncMessageReturnSelected(false);
        criteria.setAsyncMessageSelected(false);
        criteria.setCaseSenstiveSelected(false);
        criteria.setLifeLineSelected(false);
        criteria.setStopSelected(false);
        criteria.setSyncMessageReturnSelected(false);
        criteria.setSyncMessageSelected(false);

        assertFalse("testAccessors", criteria.isAsyncMessageReturnSelected());
        assertFalse("testAccessors", criteria.isAsyncMessageSelected());
        assertFalse("testAccessors", criteria.isCaseSenstiveSelected());
        assertFalse("testAccessors", criteria.isLifeLineSelected());
        assertFalse("testAccessors", criteria.isStopSelected());
        assertFalse("testAccessors", criteria.isSyncMessageReturnSelected());
        assertFalse("testAccessors", criteria.isSyncMessageSelected());
        assertEquals("testAccessors",  "test.*", criteria.getExpression());
        assertNotNull("testAccessors",  criteria.getPattern());
        assertEquals("testAccessors", "test.*", criteria.getPattern().pattern());
        assertEquals("testAccessors", Pattern.CASE_INSENSITIVE, criteria.getPattern().flags() & Pattern.CASE_INSENSITIVE);
    }

    /**
     * Test compartTo method.
     */
    @Test
    public void testCompareTo() {
        Criteria criteria = new Criteria();
        criteria.setExpression("test");
        criteria.setLifeLineSelected(true);
        criteria.setSyncMessageSelected(true);

        Criteria copy = new Criteria(criteria);
        assertTrue("testCompareTo", criteria.compareTo(copy));
        assertTrue("testCompareTo", copy.compareTo(criteria));
        assertTrue("testCompareTo", criteria.compareTo(criteria));

        copy.setExpression(null);
        assertFalse("testCompareTo", criteria.compareTo(copy));
        assertFalse("testCompareTo", copy.compareTo(criteria));

        criteria.setExpression(null);
        assertTrue("testCompareTo", criteria.compareTo(copy));
        assertTrue("testCompareTo", copy.compareTo(criteria));

        criteria.setExpression("test");
        copy.setExpression("test.*[12345]");
        assertFalse("testCompareTo", criteria.compareTo(copy));
        assertFalse("testCompareTo", copy.compareTo(criteria));

        copy.setExpression("test");
        copy.setAsyncMessageReturnSelected(true);
        assertFalse("testCompareTo", criteria.compareTo(copy));
        assertFalse("testCompareTo", copy.compareTo(criteria));
    }

    /**
     * Test save to disk.
     */
    @Test
    public void testSave() {
        DialogSettings settings = new DialogSettings("mysettings");

        Criteria criteria = new Criteria();
        criteria.setExpression("test");
        criteria.setLifeLineSelected(true);
        criteria.setSyncMessageSelected(true);

        // Save the criteria to the dialog settings
        criteria.save(settings);

        // Check if all values are saved as expected
        assertEquals("testSave", "test", settings.get("expression"));
        assertEquals("testSave", "false", settings.get("isCaseSenstiveSelected"));
        assertEquals("testSave", "false", settings.get("isAsyncMessageReturnSelected"));
        assertEquals("testSave", "false", settings.get("isAsyncMessageSelected"));
        assertEquals("testSave", "true", settings.get("isLifeLineSelected"));
        assertEquals("testSave", "false", settings.get("isStopSelected"));
        assertEquals("testSave", "false", settings.get("isSyncMessageReturnSelected"));
        assertEquals("testSave", "true", settings.get("isSyncMessageSelected"));
    }

    /**
     * Test restore from disk.
     */
    @Test
    public void testLoad() {
        DialogSettings settings = new DialogSettings("mysettings");

        Criteria criteria = new Criteria();
        criteria.setExpression("test");
        criteria.setLifeLineSelected(true);
        criteria.setSyncMessageSelected(true);

        // Save the criteria to the dialog settings
        criteria.save(settings);

        // Load the criteria from the dialog settings
        Criteria copy = new Criteria();
        copy.load(settings);

        assertTrue("testCompareTo", criteria.compareTo(copy));
        assertTrue("testCompareTo", copy.compareTo(criteria));
    }

    /**
     * Test graph node summary usage.
     */
    @Test
    public void testGetGraphNodeSummary() {

        // Create a dummy provider
        ISDFilterProvider provider = new ISDFilterProvider() {

            @Override
            public boolean isNodeSupported(int nodeType) {
                return true;
            }

            @Override
            public String getNodeName(int nodeType, String loaderClassName) {
                // not clear about purpose loaderClassName
                switch (nodeType) {
                case ISDGraphNodeSupporter.LIFELINE:
                    return "MyLifeline";
                case ISDGraphNodeSupporter.SYNCMESSAGE:
                    return "MySyncMessage";
                case ISDGraphNodeSupporter.SYNCMESSAGERETURN:
                    return "MySyncMessageReturn";
                case ISDGraphNodeSupporter.ASYNCMESSAGE:
                    return "MyAsyncMessage";
                case ISDGraphNodeSupporter.ASYNCMESSAGERETURN:
                    return "MyAsyncMessageReturn";
                case ISDGraphNodeSupporter.STOP:
                    return "MyStop";
                default:
                    return "";
                }
            }

            @Override
            public boolean filter(List<?> filters) {
                return false;
            }
        };

        Criteria criteria = new Criteria();
        criteria.setExpression("BALL_.*");
        criteria.setAsyncMessageReturnSelected(true);
        criteria.setAsyncMessageSelected(true);
        criteria.setLifeLineSelected(true);
        criteria.setStopSelected(true);
        criteria.setSyncMessageReturnSelected(true);
        criteria.setSyncMessageSelected(true);

        // Test summary when provider is available
        String summary = criteria.getGraphNodeSummary(provider, null);
        assertEquals("testGetGraphNodeSummary", "[MyLifeline or MySyncMessage or MySyncMessageReturn or MyAsyncMessage or MyAsyncMessageReturn or MyStop]", summary);

        // Test default summary when no provider is provided
        summary = criteria.getGraphNodeSummary(null, null);
        assertEquals("testGetGraphNodeSummary", "[Lifeline or Synchronous message or Synchronous message return or Asynchronous message or Asynchronous message return or Stop]", summary);

    }

    /**
     * Test matches algorithm.
     */
    @Test
    public void testMatches() {
        Criteria criteria = new Criteria();
        criteria.setExpression("BALL_.*");

        /*
         *  Note that method matches uses the Pattern class. Test
         *  only case sensitive/case insensitive case. All other regular
         *  expression cases are covered by Pattern class.
         */

        // case insensitive
        assertTrue("testMatches", criteria.matches("BALL_REQUEST"));
        assertTrue("testMatches", criteria.matches("BALL_REPLY"));
        assertTrue("testMatches", criteria.matches("BALL_R"));
        assertTrue("testMatches", criteria.matches("ball_request"));
        assertTrue("testMatches", criteria.matches("ball_request"));
        assertFalse("testMatches", criteria.matches("NOBALL_REQUEST"));
        assertFalse("testMatches", criteria.matches("BLABLA"));

        // case sensitive
        criteria.setCaseSenstiveSelected(true);
        assertTrue("testMatches", criteria.matches("BALL_REQUEST"));
        assertTrue("testMatches", criteria.matches("BALL_REPLY"));
        assertTrue("testMatches", criteria.matches("BALL_R"));
        assertFalse("testMatches", criteria.matches("ball_request"));
        assertFalse("testMatches", criteria.matches("ball_request"));
        assertFalse("testMatches", criteria.matches("NOBALL_REQUEST"));
        assertFalse("testMatches", criteria.matches("BLABLA"));
    }

}
