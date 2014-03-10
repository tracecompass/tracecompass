/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.analysis.xml.ui;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This file defines all names in the XML Structure for UI elements
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings({ "javadoc", "nls" })
@NonNullByDefault
public interface TmfXmlUiStrings {

    /* XML generic Element attribute names */
    static final String STATE_PROVIDER_VIEW = "stateSystemView";

    /* View elements and attributes */
    static final String SSV_LINE = "line";
    static final String ENTRY_ELEMENT = "entry";

    /* Elements and attributes of view entries */
    static final String PATH = "path";
    static final String DISPLAY_ELEMENT = "display";
    static final String PARENT_ELEMENT = "parent";
    static final String NAME_ELEMENT = "name";
    static final String ID_ELEMENT = "id";

    /* Generic strings for the XML module */
    static final String XML_OUTPUT_DATA = "xmlOutputData";

}