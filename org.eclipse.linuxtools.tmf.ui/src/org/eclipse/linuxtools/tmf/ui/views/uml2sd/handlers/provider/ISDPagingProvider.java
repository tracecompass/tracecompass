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

/**
 * Paging provider interface.
 *
 * Sequence Diagram loaders which implement this class provide the actions for sequence diagram page navigation.<br>
 *
 * Action provider are associated to a Sequence Diagram view by calling <code>SDView.setSDPagingProvider()</code>.<br>
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public interface ISDPagingProvider {

    /**
     * Return true to enable the next page button in the coolBar, false otherwise
     *
     * @return true if a next page exists false otherwise
     */
    boolean hasNextPage();

    /**
     * Return true to enable the previous page button in the coolBar, false otherwise
     *
     * @return true if a previous page exists false otherwise
     */
    boolean hasPrevPage();

    /**
     * Called back when next page button is pressed in the coolBar
     */
    void nextPage();

    /**
     * Called back when previous page button is pressed in the coolBar
     */
    void prevPage();

    /**
     * Called back when first page button is pressed in the coolBar
     */
    void firstPage();

    /**
     * Called back when last page button is pressed in the coolBar
     */
    void lastPage();
}
