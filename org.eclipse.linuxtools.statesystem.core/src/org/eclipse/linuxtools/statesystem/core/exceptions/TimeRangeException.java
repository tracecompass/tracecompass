/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.statesystem.core.exceptions;

/**
 * Generic exception for when the user specifies an invalid time stamp. Usually
 * timestamps must be within the range of the trace or state history being
 * queried.
 *
 * For insertions, it's forbidden to insert new states "in the past" (before where
 * the cursor is), so this exception is also thrown in that case.
 *
 * @author Alexandre Montplaisir
 * @since 3.0
 */
public class TimeRangeException extends RuntimeException {

    private static final long serialVersionUID = -4067685227260254532L;

}
