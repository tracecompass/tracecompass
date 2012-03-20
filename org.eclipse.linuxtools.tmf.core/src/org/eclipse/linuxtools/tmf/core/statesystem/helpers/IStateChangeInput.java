/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statesystem.helpers;

import org.eclipse.linuxtools.tmf.core.statesystem.StateSystem;

/**
 * This is the interface used to define the "state change input", which is the
 * main type of input that goes in the state system.
 * 
 * Usually a state change input, also called "state provider" is the piece of
 * the pipeline which converts trace events to state changes.
 * 
 * @author alexmont
 * 
 */
public interface IStateChangeInput extends Runnable {

    /**
     * Return the start time of this "state change input", which is normally the
     * start time of the originating trace (or it can be the time of the first
     * state-changing event).
     * 
     * @return The start time
     */
    public long getStartTime();

    /**
     * Assign the target state system where this SCI will insert its state
     * changes. Because of dependencies issues, this can normally not be done at
     * the constructor.
     * 
     * This needs to be called before .run()!
     * 
     * @param ss
     */
    public void assignTargetStateSystem(StateSystem ss);

    /**
     * Return the State System that was assigned to this SCI.
     * 
     * @return The target state system
     */
    public StateSystem getStateSystem();

}
