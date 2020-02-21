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

package org.eclipse.tracecompass.internal.ctf.core.event.metadata;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.core.event.metadata.DeclarationScope;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.VariantDeclaration;

/**
 * Common tree parser with scopes
 *
 * @author Matthew Khouzam
 *
 */
@NonNullByDefault
public abstract class AbstractScopedCommonTreeParser implements ICommonTreeParser {

    /**
     * Register a declaration to the current scope
     *
     * @param declaration
     *            the declaration to register
     * @param identifier
     *            the declaration's name
     * @param scope the scope to register
     * @throws ParseException
     *             if something already has that name
     */
    protected void registerType(IDeclaration declaration, String identifier , DeclarationScope scope) throws ParseException {
        if (declaration instanceof EnumDeclaration) {
            if (scope.lookupEnum(identifier) == null) {
                scope.registerEnum(identifier, (EnumDeclaration) declaration);
            }
        } else if (declaration instanceof VariantDeclaration) {
            scope.registerVariant(identifier, (VariantDeclaration) declaration);
        }
    }

}
