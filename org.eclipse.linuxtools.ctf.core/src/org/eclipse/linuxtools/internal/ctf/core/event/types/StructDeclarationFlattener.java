/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.event.types;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.ISimpleDatatypeDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;

/**
 * A fixed size struct declaration is a declaration of a structure that has no
 * variant or sequence fields. This will accelerate reading of the trace.
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public final class StructDeclarationFlattener {

    private StructDeclarationFlattener() {}

    /**
     * Flatten a {@link StructDeclaration}, if it can be (which means if it
     * contains only fixed-size elements).
     *
     * This does not modify the declaration passed in parameter, you need to use
     * the return value.
     *
     * @param sd
     *            The initial StructDeclaration
     * @return The flattened struct. Or if it couldn't be flattened, the 'sd'
     *         struct itself
     */
    public static StructDeclaration tryFlattenStruct(@NonNull StructDeclaration sd) {
        if (canBeFlattened(sd)) {
            return newFlattenedStruct(sd);
        }
        return sd;
    }

    /**
     * Check if this struct is fixed size
     *
     * @param sd
     *            the struct
     * @return if the struct is of fixed size
     */
    private static boolean canBeFlattened(@NonNull StructDeclaration sd) {
        for (String field : sd.getFieldsList()) {
            IDeclaration dec = sd.getField(field);
            if (!isFixedSize(dec)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isFixedSize(IDeclaration dec) {
        if (dec instanceof ISimpleDatatypeDeclaration) {
            return true;
        }
        if (dec instanceof ArrayDeclaration) {
            return isFixedSize(((ArrayDeclaration) dec).getElementType());
        }
        if (dec instanceof StructDeclaration) {
            StructDeclaration sDec = ((StructDeclaration) dec);
            return canBeFlattened(sDec);
        }
        return false;
    }

    private static StructDeclaration newFlattenedStruct(@NonNull StructDeclaration sd) {
        StructDeclaration flatStruct = new StructDeclaration(sd.getAlignment());
        for (String name : sd.getFieldsList()) {
            depthFirstAdd(name, flatStruct, sd.getField(name));
        }
        return flatStruct;
    }

    private static void depthFirstAdd(String path, StructDeclaration flatStruct, IDeclaration dec) {
        if (dec instanceof ISimpleDatatypeDeclaration) {
            flatStruct.addField(path, dec);
        } else if (dec instanceof ArrayDeclaration) {
            ArrayDeclaration ad = (ArrayDeclaration) dec;
            int lastIndexOf = path.lastIndexOf('.');

            String name = (lastIndexOf > 0) ? path.substring(lastIndexOf) : path;
            if (((ArrayDeclaration) dec).isString()) {
                flatStruct.addField(path, dec);
            } else {
                for (int i = 0; i < ad.getLength(); i++) {
                    depthFirstAdd(path + '.' + name + '[' + i + ']', flatStruct, ad.getElementType());
                }
            }
        } else if (dec instanceof StructDeclaration) {
            StructDeclaration sDec = ((StructDeclaration) dec);
            for (String name : sDec.getFieldsList()) {
                depthFirstAdd(path + '.' + name, flatStruct, sDec.getField(name));
            }
        }
    }

}
