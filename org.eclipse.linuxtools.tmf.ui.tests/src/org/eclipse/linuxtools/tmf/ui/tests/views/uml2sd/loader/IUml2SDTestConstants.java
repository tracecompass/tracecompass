/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.loader;

/**
 * Common constants for the TMF UML2SD test cases
 * @author Bernd Hufmann
 */
public interface IUml2SDTestConstants {

    /**
     * Timeout for waiting of jobs to finish (in milliseconds).
     */
    final static public int WAIT_FOR_JOBS_DELAY = 1000;
    /**
     * Timeout for waiting for GUI display to refresh (in milliseconds).
     */
    final static public int GUI_REFESH_DELAY = 1000;
    /**
     * Initial delay before indexing (in milliseconds).
     */
    final static public int INITIAL_INDEX_DELAY = 1000;
    /**
     * Delay after broadcasting a TMF signal (in milliseconds)
     */
    final static public int BROADCAST_DELAY = 2000;
    /**
     * Total number of pages of test trace.
     */
    final static public int TOTAL_NUMBER_OF_PAGES  = 9;
    /**
     * Number of messages per page (as defined for loader class)
     */
    final static public int MAX_MESSEAGES_PER_PAGE = 10000;
    /**
     * Number of messages of last page of the test trace.
     */
    final static public int NUM_MESSAGES_OF_LAST_PAGE = 32;
    /**
     * Default number of lifelines of test trace.
     */
    final static public int DEFAULT_NUM_LIFELINES = 2;
    /**
     * Number of lifelines of test trace when all lifelines are visible.
     */
    final static public int NUM_OF_ALL_LIFELINES = 3;
    /**
     * Page number of test trace where all lifelines are visible.
     */
    final static public int PAGE_OF_ALL_LIFELINES = 4;
    /**
     * Time scale of test trace.
     */
    final static public byte TIME_SCALE = -9;

    /**
     * Master player name (property of test trace)
     */
    final static public String MASTER_PLAYER_NAME = "Master";
    /**
     * First player name (property of test trace)
     */
    final static public String FIRST_PLAYER_NAME = "player1";
    /**
     * Second player name (property of test trace)
     */
    final static public String SECOND_PLAYER_NAME = "player2";

}
