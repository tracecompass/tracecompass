/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Externalized messages for this package
 *
 * @author Geneviève Bastien
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.messages"; //$NON-NLS-1$

    /** Message for a state undefined */
    public static @Nullable String TmfXmlFsm_StateUndefined;
    /** Message for a missing script state value children id attribute */
    public static @Nullable String TmfXmlStateValue_MissingScriptChildrenID;
    /** Message for a null script */
    public static @Nullable String TmfXmlStateValue_ScriptNullException;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
