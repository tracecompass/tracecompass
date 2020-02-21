/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial implementation and API
 *   Marc-Andre Laperle - Initial implementation and API
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands;


/**
 * Command sent, needs a getBytes to stream the data
 *
 * @author Matthew Khouzam
 */
public interface IRelayCommand {

    /**
     * Gets a byte array of the command so that it may be streamed
     *
     * @return the byte array of the command
     */
    byte[] serialize();
}