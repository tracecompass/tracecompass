/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types;

/**
 * Lami 'disk partition' value.
 *
 * A disk partition is something like "sda2".
 *
 * @author Philippe Proulx
 */
class LamiDiskPartition extends LamiString {

    public LamiDiskPartition(String value) {
        super(value);
    }

}
