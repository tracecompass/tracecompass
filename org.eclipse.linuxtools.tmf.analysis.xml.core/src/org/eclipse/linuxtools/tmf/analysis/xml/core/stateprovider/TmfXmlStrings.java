/*******************************************************************************
 * Copyright (c) 2014 Ecole Polytechnique
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial implementation
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider;

/**
 * This file defines all name in the XML Structure for the State Provider
 *
 * @author Florian Wininger
 */
@SuppressWarnings({ "javadoc", "nls" })
public interface TmfXmlStrings {

    /* XML Node Name */
    static final String STATE_PROVIDER = "stateProvider";
    static final String VALUE = "value";
    static final String LABEL = "label";

    /* XML Node Attribute */
    static final String ANALYSIS_ID = "analysisId";
    static final String NAME = "name";
    static final String VERSION = "version";

    /* XML head element */
    static final String HEAD = "head";
    static final String TRACETYPE = "tracetype";
    static final String ID = "id";

}