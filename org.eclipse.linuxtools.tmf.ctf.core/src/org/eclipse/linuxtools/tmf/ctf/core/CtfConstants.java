/*******************************************************************************
 * Copyright (c) 2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Ansgar Radermacher - support for model URI
 *   Patrick Tasse - context strings
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ctf.core;

/**
 * Set of constants used by the CTF adaptor classes
 *
 * @since 2.0
 */
@SuppressWarnings("nls")
public interface CtfConstants {

    /*
     * Context strings
     */

    /** Prefix for context information stored as CtfTmfEventfield */
    public static final String CONTEXT_FIELD_PREFIX = "context.";

    /** Key for ip field */
    public static final String IP_KEY = "_ip";

    /*
     * Custom attributes names (key within hash table)
     */

    /** Model URI for traces related to EMF models */
    public final static String MODEL_URI_KEY = "model.emf.uri";
}
