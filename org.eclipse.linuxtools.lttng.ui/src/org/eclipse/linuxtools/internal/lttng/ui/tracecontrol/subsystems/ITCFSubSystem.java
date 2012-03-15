/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Bernd Hufmann - Adapted for Lttng
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.subsystems;

import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * A subsystem can implement this interface to indicate that it can share TCF connection with
 * other subsystems on same host.
 */
public interface ITCFSubSystem extends ISubSystem {

}
