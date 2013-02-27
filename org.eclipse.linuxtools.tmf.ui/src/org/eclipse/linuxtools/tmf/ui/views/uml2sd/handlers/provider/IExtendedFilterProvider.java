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

import org.eclipse.jface.action.Action;

/**
 * Interface for providing an extended filter provider.
 *
 * Sequence Diagram loaders which implement this interface provide the action for filtering the sequence diagram.<br>
 *
 * Action provider are associated to a Sequence Diagram view by calling <code>SDView.setExtendedFilterProvider</code><br>
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public interface IExtendedFilterProvider {

    /**
     * Returns a filter action implementation.
     *
     * @return a filter action implementation
     */
    public Action getFilterAction();

}
