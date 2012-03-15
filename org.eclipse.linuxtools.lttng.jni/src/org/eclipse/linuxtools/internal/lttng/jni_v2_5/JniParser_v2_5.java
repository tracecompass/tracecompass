package org.eclipse.linuxtools.internal.lttng.jni_v2_5;
/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *******************************************************************************/

import org.eclipse.linuxtools.lttng.jni.JniParser;

/**
 * <b><u>JniParser_v2_5</u></b>
 * <p>
 * JniParser version to support Lttng traceformat of version 2.5<br>
 * This class extend abstract class JniParser with (possibly) version specific implementation.<br>
 * <p>
 */
public class JniParser_v2_5 extends JniParser {
	
	/*
	 * Forbid access to the default constructor
	 */
	protected JniParser_v2_5() {
		super();
    }
}
