/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.types;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.scope.LexicalScope;

/**
 * Scoped defintion. a defintion where you can lookup various datatypes
 *
 * TODO: replace by default methods and an interface when java 8 is upon us
 *
 * @author Matthew Khouzam
 * @since 3.1
 */
@NonNullByDefault
public abstract class ScopedDefinition extends Definition implements IDefinitionScope {

    /**
     * Constructor
     *
     * @param declaration
     *            the event declaration
     * @param definitionScope
     *            the definition is in a scope, (normally a struct) what is it?
     * @param fieldName
     *            the name of the definition. (it is a field in the parent
     *            scope)
     */
    public ScopedDefinition(IDeclaration declaration, @Nullable IDefinitionScope definitionScope, String fieldName) {
        super(declaration, definitionScope, fieldName);
    }

    /**
     * Constructor This one takes the scope and thus speeds up definition
     * creation
     *
     * @param declaration
     *            the parent declaration
     * @param definitionScope
     *            the parent scope
     * @param fieldName
     *            the field name
     * @param scope
     *            the lexical scope
     * @since 3.1
     */
    public ScopedDefinition(StructDeclaration declaration, @Nullable IDefinitionScope definitionScope, String fieldName, LexicalScope scope) {
        super(declaration, definitionScope, fieldName, scope);
    }

    /**
     * Lookup an array in a struct. If the name returns a non-array (like an
     * int) then the method returns null
     *
     * @param name
     *            the name of the array
     * @return the array or null.
     */
    public @Nullable AbstractArrayDefinition lookupArrayDefinition(String name) {
        Definition def = lookupDefinition(name);
        return (AbstractArrayDefinition) ((def instanceof AbstractArrayDefinition) ? def : null);
    }

    /**
     * Lookup an array in a struct. If the name returns a non-array (like an
     * int) then the method returns null
     *
     * @param name
     *            the name of the array
     * @return the array or null.
     * @deprecated use {@link ScopedDefinition#lookupArrayDefinition(String)}
     */
    @Deprecated
    @Nullable
    public ArrayDefinition lookupArray(String name) {
        Definition def = lookupDefinition(name);
        return (ArrayDefinition) ((def instanceof ArrayDefinition) ? def : null);
    }

    /**
     * Lookup an enum in a struct. If the name returns a non-enum (like an int)
     * then the method returns null
     *
     * @param name
     *            the name of the enum
     * @return the enum or null if a definition is not found or it does not
     *         match the desired datatype.
     */
    @Nullable
    public EnumDefinition lookupEnum(String name) {
        Definition def = lookupDefinition(name);
        return (EnumDefinition) ((def instanceof EnumDefinition) ? def : null);
    }

    /**
     * Lookup an integer in a struct. If the name returns a non-integer (like an
     * float) then the method returns null
     *
     * @param name
     *            the name of the integer
     * @return the integer or null if a definition is not found or it does not
     *         match the desired datatype.
     */
    @Nullable
    public IntegerDefinition lookupInteger(String name) {
        Definition def = lookupDefinition(name);
        return (IntegerDefinition) ((def instanceof IntegerDefinition) ? def : null);
    }

    /**
     * Lookup a sequence in a struct. If the name returns a non-sequence (like
     * an int) then the method returns null
     *
     * @param name
     *            the name of the sequence
     * @return the sequence or null if a definition is not found or it does not
     *         match the desired datatype.
     * @since 3.0
     * @deprecated use {@link ScopedDefinition#lookupArrayDefinition(String)}
     */
    @Deprecated
    @Nullable
    public SequenceDefinition lookupSequence(String name) {
        Definition def = lookupDefinition(name);
        return (SequenceDefinition) ((def instanceof SequenceDefinition) ? def : null);
    }

    /**
     * Lookup a string in a struct. If the name returns a non-string (like an
     * int) then the method returns null
     *
     * @param name
     *            the name of the string
     * @return the string or null if a definition is not found or it does not
     *         match the desired datatype.
     */
    @Nullable
    public StringDefinition lookupString(String name) {
        Definition def = lookupDefinition(name);
        return (StringDefinition) ((def instanceof StringDefinition) ? def : null);
    }

    /**
     * Lookup a struct in a struct. If the name returns a non-struct (like an
     * int) then the method returns null
     *
     * @param name
     *            the name of the struct
     * @return the struct or null if a definition is not found or it does not
     *         match the desired datatype.
     */
    @Nullable
    public StructDefinition lookupStruct(String name) {
        Definition def = lookupDefinition(name);
        return (StructDefinition) ((def instanceof StructDefinition) ? def : null);
    }

    /**
     * Lookup a variant in a struct. If the name returns a non-variant (like an
     * int) then the method returns null
     *
     * @param name
     *            the name of the variant
     * @return the variant or null if a definition is not found or it does not
     *         match the desired datatype.
     */
    @Nullable
    public VariantDefinition lookupVariant(String name) {
        Definition def = lookupDefinition(name);
        return (VariantDefinition) ((def instanceof VariantDefinition) ? def : null);
    }
}