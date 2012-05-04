/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
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
 */
@SuppressWarnings("nls")
public interface IUml2SDTestConstants {
    
    final static public int WAIT_FOR_JOBS_DELAY = 1000; 
    final static public int GUI_REFESH_DELAY = 1000;
    final static public int INITIAL_INDEX_DELAY = 1000;
    final static public int BROADCAST_DELAY = 2000;
    
    final static public int TOTAL_NUMBER_OF_PAGES  = 9;
    final static public int MAX_MESSEAGES_PER_PAGE = 10000;
    final static public int NUM_MESSAGES_OF_LAST_PAGE = 32;
    
    final static public int DEFAULT_NUM_LIFELINES = 2;
    final static public int NUM_OF_ALL_LIFELINES = 3;
    final static public int PAGE_OF_ALL_LIFELINES = 4;
    
    final static public byte TIME_SCALE = -9;
    
    final static public String MASTER_PLAYER_NAME = "Master";
    final static public String FIRST_PLAYER_NAME = "player1";
    final static public String SECOND_PLAYER_NAME = "player2";

}
