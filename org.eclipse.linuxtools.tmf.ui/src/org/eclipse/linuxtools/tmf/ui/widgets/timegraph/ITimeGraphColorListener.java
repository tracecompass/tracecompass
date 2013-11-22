/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph;

/**
 * A time graph state color change listener
 *
 * @author Geneviève Bastien
 */
public interface ITimeGraphColorListener {

    /**
     * Notify the listener that the presentation provider's color may have
     * changed and they need to be reloaded
     *
     * @param stateItems
     *            The new state table
     */
    void colorSettingsChanged(StateItem[] stateItems);

}
