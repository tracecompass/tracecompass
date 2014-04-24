/**********************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.core.model;

/**
 * <p>
 *  Enumeration for the node connection state.
 * </p>
 *
 * @author Bernd Hufmann
 */
public enum TargetNodeState {
     /** State when disconnected */
    DISCONNECTED,
     /** State while disconnecting */
    DISCONNECTING,
     /** State when connected */
    CONNECTED,
     /** State while connecting */
    CONNECTING;
}
