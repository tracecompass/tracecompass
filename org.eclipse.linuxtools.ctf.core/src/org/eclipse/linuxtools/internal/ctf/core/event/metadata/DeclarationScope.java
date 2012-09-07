/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial Design and Grammar
 * Contributors: Simon Marchi    - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.event.metadata;

import java.util.HashMap;

import org.eclipse.linuxtools.ctf.core.event.types.EnumDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDeclaration;
import org.eclipse.linuxtools.internal.ctf.core.event.metadata.exceptions.ParseException;

/**
 * <b><u>DeclarationScope</u></b>
 * <p>
 * A DeclarationScope keeps track of the various CTF declarations for a given
 * scope.
 */
public class DeclarationScope {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private DeclarationScope parentScope = null;

    private final HashMap<String, StructDeclaration> structs = new HashMap<String, StructDeclaration>();
    private final HashMap<String, EnumDeclaration> enums = new HashMap<String, EnumDeclaration>();
    private final HashMap<String, VariantDeclaration> variants = new HashMap<String, VariantDeclaration>();
    private final HashMap<String, IDeclaration> types = new HashMap<String, IDeclaration>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Creates a declaration scope with no parent.
     */
    public DeclarationScope() {
    }

    /**
     * Creates a declaration scope with the specified parent.
     *
     * @param parentScope
     *            The parent of the newly created scope.
     */
    public DeclarationScope(DeclarationScope parentScope) {
        this.parentScope = parentScope;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Returns the parent of the current scope.
     *
     * @return The parent scope.
     */
    public DeclarationScope getParentScope() {
        return parentScope;
    }

    // ------------------------------------------------------------------------
    // Registration operations
    // ------------------------------------------------------------------------

    /**
     * Registers a type declaration.
     *
     * @param name
     *            The name of the type.
     * @param declaration
     *            The type declaration.
     * @throws ParseException
     *             if a type with the same name has already been defined.
     */
    public void registerType(String name, IDeclaration declaration)
            throws ParseException {
        /* Check if the type has been defined in the current scope */
        if (types.containsKey(name)) {
            throw new ParseException("Type " + name //$NON-NLS-1$
                    + " has already been defined."); //$NON-NLS-1$
        }

        /* Add it to the register. */
        types.put(name, declaration);
    }

    /**
     * Registers a struct declaration.
     *
     * @param name
     *            The name of the struct.
     * @param declaration
     *            The declaration of the struct.
     * @throws ParseException
     *             if a struct with the same name has already been registered.
     */
    public void registerStruct(String name, StructDeclaration declaration)
            throws ParseException {
        /* Check if the struct has been defined in the current scope. */
        if (structs.containsKey(name)) {
            throw new ParseException("struct " + name //$NON-NLS-1$
                    + " has already been defined."); //$NON-NLS-1$
        }

        /* Add it to the register. */
        structs.put(name, declaration);

        /* It also defined a new type, so add it to the type declarations. */
        String struct_prefix = "struct "; //$NON-NLS-1$
        registerType(struct_prefix + name, declaration);
    }

    /**
     * Registers an enum declaration.
     *
     * @param name
     *            The name of the enum.
     * @param declaration
     *            The declaration of the enum.
     * @throws ParseException
     *             if an enum with the same name has already been registered.
     */
    public void registerEnum(String name, EnumDeclaration declaration)
            throws ParseException {
        /* Check if the enum has been defined in the current scope. */
        if (lookupEnum(name) != null) {
            throw new ParseException("enum " + name //$NON-NLS-1$
                    + " has already been defined."); //$NON-NLS-1$
        }

        /* Add it to the register. */
        enums.put(name, declaration);

        /* It also defined a new type, so add it to the type declarations. */
        String enum_prefix = "enum "; //$NON-NLS-1$
        registerType(enum_prefix + name, declaration);
    }

    /**
     * Registers a variant declaration.
     *
     * @param name
     *            The name of the variant.
     * @param declaration
     *            The declaration of the variant.
     * @throws ParseException
     *             if a variant with the same name has already been registered.
     */
    public void registerVariant(String name, VariantDeclaration declaration)
            throws ParseException {
        /* Check if the variant has been defined in the current scope. */
        if (lookupVariant(name) != null) {
            throw new ParseException("variant " + name //$NON-NLS-1$
                    + " has already been defined."); //$NON-NLS-1$
        }

        /* Add it to the register. */
        variants.put(name, declaration);

        /* It also defined a new type, so add it to the type declarations. */
        String variant_prefix = "variant "; //$NON-NLS-1$
        registerType(variant_prefix + name, declaration);
    }

    // ------------------------------------------------------------------------
    // Lookup operations
    // ------------------------------------------------------------------------

    /**
     * Looks up a type declaration in the current scope.
     *
     * @param name
     *            The name of the type to search for.
     * @return The type declaration, or null if no type with that name has been
     *         defined.
     */
    public IDeclaration lookupType(String name) {
        return types.get(name);
    }

    /**
     * Looks up a type declaration in the current scope and recursively in the
     * parent scopes.
     *
     * @param name
     *            The name of the type to search for.
     * @return The type declaration, or null if no type with that name has been
     *         defined.
     */
    public IDeclaration rlookupType(String name) {
        IDeclaration declaration = lookupType(name);
        if (declaration != null) {
            return declaration;
        } else if (parentScope != null) {
            return parentScope.rlookupType(name);
        } else {
            return null;
        }
    }

    /**
     * Looks up a struct declaration.
     *
     * @param name
     *            The name of the struct to search for.
     * @return The struct declaration, or null if no struct with that name has
     *         been defined.
     */
    public StructDeclaration lookupStruct(String name) {
        return structs.get(name);
    }

    /**
     * Looks up a struct declaration in the current scope and recursively in the
     * parent scopes.
     *
     * @param name
     *            The name of the struct to search for.
     * @return The struct declaration, or null if no struct with that name has
     *         been defined.
     */
    public StructDeclaration rlookupStruct(String name) {
        StructDeclaration declaration = lookupStruct(name);
        if (declaration != null) {
            return declaration;
        } else if (parentScope != null) {
            return parentScope.rlookupStruct(name);
        } else {
            return null;
        }
    }

    /**
     * Looks up a enum declaration.
     *
     * @param name
     *            The name of the enum to search for.
     * @return The enum declaration, or null if no enum with that name has been
     *         defined.
     */
    public EnumDeclaration lookupEnum(String name) {
        return enums.get(name);
    }

    /**
     * Looks up an enum declaration in the current scope and recursively in the
     * parent scopes.
     *
     * @param name
     *            The name of the enum to search for.
     * @return The enum declaration, or null if no enum with that name has been
     *         defined.
     */
    public EnumDeclaration rlookupEnum(String name) {
        EnumDeclaration declaration = lookupEnum(name);
        if (declaration != null) {
            return declaration;
        } else if (parentScope != null) {
            return parentScope.rlookupEnum(name);
        } else {
            return null;
        }
    }

    /**
     * Looks up a variant declaration.
     *
     * @param name
     *            The name of the variant to search for.
     * @return The variant declaration, or null if no variant with that name has
     *         been defined.
     */
    public VariantDeclaration lookupVariant(String name) {
        return variants.get(name);
    }

    /**
     * Looks up a variant declaration in the current scope and recursively in
     * the parent scopes.
     *
     * @param name
     *            The name of the variant to search for.
     * @return The variant declaration, or null if no variant with that name has
     *         been defined.
     */
    public VariantDeclaration rlookupVariant(String name) {
        VariantDeclaration declaration = lookupVariant(name);
        if (declaration != null) {
            return declaration;
        } else if (parentScope != null) {
            return parentScope.rlookupVariant(name);
        } else {
            return null;
        }
    }


    /**
     * Get all the type names of this scope.
     *
     * @return The type names
     */
    public String[] getTypeNames() {
        String[] keys = new String[types.keySet().size()];
        return types.keySet().toArray(keys);
    }

    /**
     * Replace a type with a new one.
     *
     * @param name
     *            The name of the type
     * @param newType
     *            The type
     * @throws ParseException
     *             If the type does not exist.
     */
    public void replaceType(String name, IDeclaration newType) throws ParseException{
        if (types.containsKey(name)) {
            types.put(name, newType);
        } else {
            throw new ParseException("Trace does not contain type: " + name); //$NON-NLS-1$
        }
    }

}
