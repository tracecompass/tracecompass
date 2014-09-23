/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial implementation and API
 *   Marc-Andre Laperle - Initial implementation and API
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands;

/**
 * Instruction to send to relayd
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public interface IBaseCommand {

    /**
     * gets the numerical value of the command
     *
     * @return the numerical value of the command
     */
    int getCommand();
}