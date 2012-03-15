/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.types;

import java.util.HashMap;
import java.util.ListIterator;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;

/**
 * <b><u>StructDefinition</u></b>
 */
public class StructDefinition extends Definition implements IDefinitionScope {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final StructDeclaration declaration;
    private final HashMap<String, Definition> definitions = new HashMap<String, Definition>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public StructDefinition(StructDeclaration declaration,
            IDefinitionScope definitionScope, String structFieldName) {
        super(definitionScope, structFieldName);

        this.declaration = declaration;

        for (String fName : declaration.getFieldsList()) {
            IDeclaration fieldDecl = declaration.getFields().get(fName);
            assert (fieldDecl != null);

            Definition def = fieldDecl.createDefinition(this, fName);
            definitions.put(fName, def);
        }
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    @Override
    public String getPath() {
        return path;
    }

    public HashMap<String, Definition> getDefinitions() {
        return definitions;
    }

    public StructDeclaration getDeclaration() {
        return declaration;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void read(BitBuffer input) {
        for (String fName : declaration.getFieldsList()) {
            Definition def = definitions.get(fName);
            assert (def != null);

            def.read(input);
        }
    }

    @Override
    public Definition lookupDefinition(String lookupPath) {
        /*
         * The fields are created in order of appearance, so if a variant or
         * sequence refers to a field that is after it, the field's definition
         * will not be there yet in the hashmap.
         */
        return definitions.get(lookupPath);
    }

    public ArrayDefinition lookupArray(String name) {
        Definition def = definitions.get(name);
        return (ArrayDefinition) ((def instanceof ArrayDefinition) ? def : null);
    }

    public EnumDefinition lookupEnum(String name) {
        Definition def = definitions.get(name);
        return (EnumDefinition) ((def instanceof EnumDefinition) ? def : null);
    }

    public IntegerDefinition lookupInteger(String name) {
        Definition def = definitions.get(name);
        return (IntegerDefinition) ((def instanceof IntegerDefinition) ? def
                : null);
    }

    public SequenceDefinition lookupSequence(String name) {
        Definition def = definitions.get(name);
        return (SequenceDefinition) ((def instanceof SequenceDefinition) ? def
                : null);
    }

    public StringDefinition lookupString(String name) {
        Definition def = definitions.get(name);
        return (StringDefinition) ((def instanceof StringDefinition) ? def
                : null);
    }

    public StructDefinition lookupStruct(String name) {
        Definition def = definitions.get(name);
        return (StructDefinition) ((def instanceof StructDefinition) ? def
                : null);
    }

    public VariantDefinition lookupVariant(String name) {
        Definition def = definitions.get(name);
        return (VariantDefinition) ((def instanceof VariantDefinition) ? def
                : null);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        int size = this.declaration.getFieldsList().size();
        int n = 0;

        if (size > 1) {
            builder.append("{ "); //$NON-NLS-1$
        }

        ListIterator<String> listIterator = this.declaration.getFieldsList().listIterator();

        while (listIterator.hasNext()) {
            String field = listIterator.next();
            builder.append(definitions.get(field).toString());
            n++;
            if (n != size) {
                builder.append(", "); //$NON-NLS-1$
            }
        }

        if (size > 1) {
            builder.append(" }"); //$NON-NLS-1$
        }

        return builder.toString();
    }
}
