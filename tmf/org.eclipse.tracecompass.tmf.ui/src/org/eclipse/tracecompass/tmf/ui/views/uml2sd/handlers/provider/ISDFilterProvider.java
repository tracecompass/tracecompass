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

import java.util.List;

import org.eclipse.tracecompass.tmf.ui.views.uml2sd.dialogs.FilterCriteria;

/**
 * Interface for providing a filter provider.
 *
 * Sequence Diagram loaders which implement this class provide the actions filtering the sequence diagram.<br>
 * This interface also allow the implementor to set which graph nodes are supporting filtering (thanks to
 * ISDGraphNodeSupporter extension).<br>
 *
 * Action provider are associated to a Sequence Diagram view by calling <code>SDView.setSDFilterProvider</code><br>
 *
 * Filters to be applied to be managed by the loader in an ArrayList of FilterCriteria.<br>
 *
 * @version 1.0
 * @author sveyrier
 */
public interface ISDFilterProvider extends ISDGraphNodeSupporter {

    /**
     * Called when the Filter dialog box OK button is pressed
     *
     * @param filters user selection made in the dialog box
     * @return true if the filter applied
     */
    boolean filter(List<FilterCriteria> filters);

}
