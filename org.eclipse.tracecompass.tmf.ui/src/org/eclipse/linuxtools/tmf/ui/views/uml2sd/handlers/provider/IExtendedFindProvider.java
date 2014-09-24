/**********************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation, Ericsson
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

import org.eclipse.jface.action.Action;

/**
 * Interface for providing an extended find provider.
 *
 * Sequence Diagram loaders which implement this interface provide an action for finding in the sequence diagram.
 *
 * Action provider are associated to a Sequence Diagram view by calling <code>SDView.setExtendedFindProvider()</code>.<br>
 *
 * Note that either provider implementing ISDFindProvider or IExtendedFindProvider can be active in the SDView.<br>
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public interface IExtendedFindProvider {

    /**
     * Returns an extended find action.
     *
     * @return an extended find action
     */
    Action getFindAction();
}
