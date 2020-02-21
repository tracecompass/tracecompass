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

package org.eclipse.tracecompass.tmf.ui.views.uml2sd.handlers.provider;

import org.eclipse.ui.IActionBars;

/**
 * Interface for providing an extended action bar provider.
 *
 * Sequence Diagram loaders which implement this interface provide their own action to the action bar.
 *
 * Action provider are associated to a Sequence Diagram SDWidget calling SDViewer.setExtendedActionBarProvider()
 *
 * @version 1.0
 * @author sveyrier
 */
public interface ISDExtendedActionBarProvider {

    /**
     * The caller is supposed to add its own actions in the cool bar and the drop-down menu.<br>
     * See examples in SDView.createCoolbarContent().
     *
     * @param bar the bar
     */
    void supplementCoolbarContent(IActionBars bar);

}
