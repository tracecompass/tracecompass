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
import java.util.Map;

import org.eclipse.linuxtools.internal.ctf.core.event.io.BitBuffer;

/**
 * <b><u>VariantDefinition</u></b>
 */
public class VariantDefinition extends Definition implements IDefinitionScope {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private VariantDeclaration declaration;

    private EnumDefinition tagDefinition;
    private HashMap<String, Definition> definitions = new HashMap<String, Definition>();
    private String currentField;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public VariantDefinition(VariantDeclaration declaration,
            IDefinitionScope definitionScope, String fieldName) {
        super(definitionScope, fieldName);

        this.declaration = declaration;

        Definition tagDef = definitionScope.lookupDefinition(declaration.getTag());
        /*
         * if (tagDef == null) { throw new
         * Exception("Variant tag field not found"); }
         *
         * if (!(tagDef instanceof EnumDefinition)) { throw new
         * Exception("Variant tag field not enum"); }
         */
        this.tagDefinition = (EnumDefinition) tagDef;

        for (Map.Entry<String, IDeclaration> field : declaration.getFields().entrySet()) {
            Definition fieldDef = field.getValue().createDefinition(this,
                    field.getKey());
            definitions.put(field.getKey(), fieldDef);
        }
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    public VariantDeclaration getDeclaration() {
        return declaration;
    }

    public void setDeclaration(VariantDeclaration declaration) {
        this.declaration = declaration;
    }

    public EnumDefinition getTagDefinition() {
        return tagDefinition;
    }

    public void setTagDefinition(EnumDefinition tagDefinition) {
        this.tagDefinition = tagDefinition;
    }

    public HashMap<String, Definition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(HashMap<String, Definition> definitions) {
        this.definitions = definitions;
    }

    public void setCurrentField(String currentField) {
        this.currentField = currentField;
    }

    @Override
    public String getPath() {
        return path;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void read(BitBuffer input) {
        currentField = tagDefinition.getValue();

        Definition field = definitions.get(currentField);

        field.read(input);
    }

    @Override
    public Definition lookupDefinition(String lookupPath) {
        return definitions.get(lookupPath);
    }

    public String getCurrentFieldName() {
        return currentField;
    }

    public Definition getCurrentField() {
        return definitions.get(currentField);
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

}
