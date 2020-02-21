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

import org.eclipse.tracecompass.tmf.ui.views.uml2sd.core.Lifeline;

/**
 * Interface for providing a collapse provider.
 *
 * Sequence diagram loaders which want to support Drag and Drop collapsing in the sequence diagram must implement this
 * interface and register this implementation using <code>SDView.setCollapsingProvider();</code>
 *
 * @version 1.0
 * @author sveyrier
 */
public interface ISDCollapseProvider {

    /**
     * Called back when the sequence diagram is requesting 2 lifelines collapsing
     *
     * @param lifeline1 - One of the lifeline to collapse
     * @param lifeline2 - The other lifeline to collapse with
     */
    void collapseTwoLifelines(Lifeline lifeline1, Lifeline lifeline2);

}
