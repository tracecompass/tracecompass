/**********************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.uml2sd.load;

import org.eclipse.tracecompass.tmf.ui.views.uml2sd.SDView;

/**
 * The interface all UML2SD loaders must implement.
 *
 * @version 1.0
 * @author sveyrier
 */
public interface IUml2SDLoader {

    /**
     * Set the viewer object to the loader that has been reloaded at the beginning
     * of a new workbench session
     *
     * @param viewer The sequence diagram view
     */
    void setViewer(SDView viewer);

    /**
     * Returns title string for the UML2SD View when this loader is the one
     *
     * @return the string convenient for this loader
     */
    String getTitleString();

    /**
     * When another loader becomes the one the previous one is replaced It's time clean-up
     * if needed (listeners to be removed for example)
     */
    void dispose();

}
