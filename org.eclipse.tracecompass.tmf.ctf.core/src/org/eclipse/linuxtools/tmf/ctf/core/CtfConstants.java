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

import org.eclipse.core.runtime.QualifiedName;

/**
 * Set of constants used by the CTF adaptor classes
 *
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
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
    public final  String MODEL_URI_KEY = "model.emf.uri";

    /**
     * The host persistent property for the live session.
     *
     * @since 3.1
     */
    QualifiedName LIVE_HOST = new QualifiedName("org.eclipse.linuxtools.tmf.ctf.core", "live.host"); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * The port persistent property for the live session.
     *
     * @since 3.1
     */
    QualifiedName LIVE_PORT = new QualifiedName("org.eclipse.linuxtools.tmf.ctf.core", "live.port"); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * The live session name persistent property.
     *
     * @since 3.1
     */
    QualifiedName LIVE_SESSION_NAME = new QualifiedName("org.eclipse.linuxtools.tmf.ctf.core", "live.session.name"); //$NON-NLS-1$//$NON-NLS-2$;
}
