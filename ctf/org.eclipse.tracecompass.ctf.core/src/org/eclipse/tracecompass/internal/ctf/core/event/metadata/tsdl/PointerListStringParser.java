/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl;

import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;

/**
 * A parser of pointer lists... like x.y.z
 *
 * @author Matthew Khouzam - Initial API and implementation
 */
public final class PointerListStringParser implements ICommonTreeParser {

    /**
     * Instance
     */
    public static final PointerListStringParser INSTANCE = new PointerListStringParser();

    private PointerListStringParser() {
    }

    /**
     * Creates the string representation of a type specifier.
     *
     * @param pointers
     *            A TYPE_SPECIFIER node.
     * @param param
     *            unused
     *
     * @return A StringBuilder to which will be appended the string.
     */
    @Override
    public StringBuilder parse(CommonTree pointers, ICommonTreeParserParameter param) {
        StringBuilder sb = new StringBuilder();
        List<CommonTree> pointerList = pointers.getChildren();
        if (pointers.getChildCount() == 0) {
            return sb;
        }

        for (CommonTree pointer : pointerList) {

            sb.append(" *"); //$NON-NLS-1$
            if (pointer.getChildCount() > 0) {
                sb.append(" const"); //$NON-NLS-1$
            }
        }
        return sb;
    }
}
