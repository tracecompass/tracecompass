/**********************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.Criteria;

/**
 * Interface for providing a find provider.
 *
 * Sequence Diagram loaders which implement this class provide the actions for finding the sequence diagram. This
 * interface also allow the implementor to set which action/feature are supported.<br>
 *
 * Action provider are associated to a Sequence Diagram view by calling <code>SDView.setSDFindProvider()</code>.<br>
 *
 * Note that either provider implementing ISDFindProvider or IExtendedFindProvider can be active in the SDView.<br>
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public interface ISDFindProvider extends ISDGraphNodeSupporter {

    /**
     * Called when the Find dialog box OK button is pressed
     *
     * @param toApply user selection made in the dialog box
     * @return true if the find got a non empty result
     */
    public boolean find(Criteria toApply);

    /**
     * Called when dialog is closed
     */
    public void cancel();

}
