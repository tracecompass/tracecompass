/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.types;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.scope.ILexicalScope;
import org.eclipse.tracecompass.internal.ctf.core.Activator;

/**
 * A CTF structure declaration.
 *
 * A structure is similar to a C structure, it is a compound data type that
 * contains other datatypes in fields.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong> If
 * multiple threads access an <tt>StructDeclaration</tt> instance concurrently,
 * and at least one of the threads modifies the list structurally, by calling
 * {@link #addField(String, IDeclaration)} it <i>must</i> be synchronized
 * externally. This is typically not the case though as it would mean modifying
 * the TSDL/Metadata while reading events.
 * <p>
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class StructDeclaration extends Declaration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** Field names */
    private @NonNull String[] fFieldNames;
    /** Field declarations */
    private @NonNull IDeclaration[] fFields;

    /** maximum bit alignment */
    private long fMaxAlign;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The struct declaration, add fields later
     *
     * @param align
     *            the minimum alignment of the struct. (if a struct is 8bit
     *            aligned and has a 32 bit aligned field, the struct becomes 32
     *            bit aligned.
     */
    public StructDeclaration(long align) {
        fMaxAlign = Math.max(align, 1);
        fFieldNames = new @NonNull String[0];
        fFields = new @NonNull IDeclaration[0];
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Get current alignment
     *
     * @return the alignment of the struct and all its fields
     */
    public long getMaxAlign() {
        return fMaxAlign;
    }

    /**
     * Query if the struct has a given field
     *
     * @param name
     *            the name of the field, scopeless please
     * @return does the field exist?
     */
    public boolean hasField(String name) {
        return Arrays.asList(fFieldNames).contains(name);
    }

    /**
     * Get the field declaration corresponding to a field name.
     *
     * @param fieldName
     *            The field name
     * @return The declaration of the field, or null if there is no such field.
     */
    @Nullable
    public IDeclaration getField(String fieldName) {
        final int indexOf = Arrays.asList(fFieldNames).indexOf(fieldName);
        if (indexOf == -1) {
            return null;
        }
        return fFields[indexOf];
    }

    /**
     * Gets the field list.
     *
     * @return the field list.
     */
    public @NonNull Iterable<@NonNull String> getFieldsList() {
        return Arrays.asList(fFieldNames);
    }

    @Override
    public long getAlignment() {
        return this.fMaxAlign;
    }

    @Override
    public int getMaximumSize() {
        long maxSize = 0;
        for (IDeclaration field : fFields) {
            maxSize += field.getMaximumSize();
        }
        return (int) Math.min(maxSize, Integer.MAX_VALUE);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public StructDefinition createDefinition(IDefinitionScope definitionScope,
            String fieldName, BitBuffer input) throws CTFException {
        alignRead(input);
        final Definition[] myFields = new Definition[fFields.length];
        StructDefinition structDefinition = null;
        if (definitionScope == null) {
            InternalDef localDefinitionScope = new InternalDef(null, null);
            structDefinition = new StructDefinition(this, localDefinitionScope, fieldName, myFields);
            localDefinitionScope.setDefinition(structDefinition);
        } else {
            structDefinition = new StructDefinition(this, definitionScope, fieldName, myFields);
        }
        fillStruct(input, myFields, structDefinition);
        return structDefinition;
    }

    /**
     * Create a definition from this declaration. This is a faster constructor
     * as it has a lexical scope and this does not need to look it up.
     *
     * @param definitionScope
     *            the definition scope, the parent where the definition will be
     *            placed
     * @param fieldScope
     *            the scope of the definition
     * @param input
     *            a bitbuffer to read from
     * @return a reference to the definition
     * @throws CTFException
     *             error in reading
     * @since 1.0
     */
    public StructDefinition createDefinition(IDefinitionScope definitionScope,
            ILexicalScope fieldScope, @NonNull BitBuffer input) throws CTFException {
        alignRead(input);
        final Definition[] myFields = new Definition[fFields.length];

        StructDefinition structDefinition = new StructDefinition(this, definitionScope,
                fieldScope, fieldScope.getName(), Arrays.asList(fFieldNames), myFields);
        fillStruct(input, myFields, structDefinition);
        return structDefinition;
    }

    /**
     * Add a field to the struct, will not add a field that is already declared
     *
     * @param name
     *            the name of the field, scopeless
     * @param declaration
     *            the declaration of the field
     */
    public void addField(@NonNull String name, @NonNull IDeclaration declaration) {
        if (hasField(name)) {
            Activator.log(IStatus.WARNING, "Struct already contains a field named " + name); //$NON-NLS-1$
            return;
        }
        /* extend by one */
        final int length = fFieldNames.length;
        @NonNull String[] names = Arrays.copyOf(fFieldNames, length + 1);
        @NonNull IDeclaration[] fields = Arrays.copyOf(fFields, length + 1);
        /* set the value */
        names[length] = name;
        fields[length] = declaration;
        fFieldNames = names;
        fFields = fields;
        fMaxAlign = Math.max(fMaxAlign, declaration.getAlignment());
    }

    private void fillStruct(@NonNull BitBuffer input, final IDefinition[] myFields, StructDefinition structDefinition) throws CTFException {
        final @NonNull String[] fieldNames = fFieldNames;
        final @NonNull IDeclaration[] fields = fFields;
        for (int i = 0; i < fields.length; i++) {
            /* We should not have inserted null keys... */
            myFields[i] = fields[i].createDefinition(structDefinition, fieldNames[i], input);
        }
    }

    /**
     * Special constructor for fields
     *
     * @param eventHeaderDef
     *            the event header, used for scopes
     * @param definitionScope
     *            the definition scope, in this case, the trace
     * @param fields
     *            event fields
     * @param input
     *            the input {@link BitBuffer}
     * @return the fields definition
     * @throws CTFException
     *             something went wrong
     * @since 1.1
     */
    public StructDefinition createFieldDefinition(ICompositeDefinition eventHeaderDef, IDefinitionScope definitionScope, ILexicalScope fields, @NonNull BitBuffer input) throws CTFException {
        alignRead(input);
        final Definition[] myFields = new Definition[fFields.length];
        IDefinitionScope merged = definitionScope;
        if (eventHeaderDef != null) {
            merged = new InternalDef(definitionScope, eventHeaderDef);
        }
        StructDefinition structDefinition = new StructDefinition(this, merged,
                fields, fields.getName(), Arrays.asList(fFieldNames), myFields);
        if (merged instanceof InternalDef) {
            InternalDef internalDef = (InternalDef) merged;
            internalDef.setDefinition(structDefinition);
        }
        fillStruct(input, myFields, structDefinition);
        return structDefinition;
    }

    private static final Pattern EVENT_HEADER = Pattern.compile(ILexicalScope.EVENT_HEADER.getPath().replaceAll("\\.", "\\\\.") + "\\."); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

    static class InternalDef implements IDefinitionScope {

        private final ICompositeDefinition fEventHeaderDef;
        private final IDefinitionScope fTraceDef;
        private StructDefinition fDefinition;

        public InternalDef(IDefinitionScope definitionScope, ICompositeDefinition eventHeaderDef) {
            fTraceDef = definitionScope;
            fEventHeaderDef = eventHeaderDef;
        }

        @Override
        public ILexicalScope getScopePath() {
            return ILexicalScope.EVENT;
        }

        @Override
        public IDefinition lookupDefinition(String lookupPath) {
            IDefinition lookupDefinition = null;
            if (fTraceDef != null) {
                lookupDefinition = fTraceDef.lookupDefinition(lookupPath);
            }
            if (lookupDefinition == null && fEventHeaderDef != null) {
                String[] paths = EVENT_HEADER.split(lookupPath);
                if (paths.length > 1) {
                    String[] childLookup = paths[1].split("\\."); //$NON-NLS-1$
                    return getRecursiveDef(fEventHeaderDef.getDefinition(childLookup[0]), childLookup, 1);
                }
                if (fDefinition != null) {
                    return fDefinition.lookupDefinition(lookupPath);
                }
            }
            return lookupDefinition;
        }

        public IDefinition lookupDefinitionBreakLoop(String lookupPath) {
            IDefinition lookupDefinition = null;
            if (fTraceDef != null) {
                lookupDefinition = fTraceDef.lookupDefinition(lookupPath);
            }
            if (lookupDefinition == null) {
                if (fEventHeaderDef != null) {
                    String[] paths = EVENT_HEADER.split(lookupPath);
                    if (paths.length > 1) {
                        String[] childLookup = paths[1].split("\\."); //$NON-NLS-1$
                        return getRecursiveDef(fEventHeaderDef.getDefinition(childLookup[0]), childLookup, 1);
                    }
                }
            }
            return lookupDefinition;
        }

        private IDefinition getRecursiveDef(Definition definition, String[] childLookup, int i) {
            if (i == childLookup.length) {
                return definition;
            }
            if (definition instanceof ICompositeDefinition) {
                ICompositeDefinition compositeDefinition = (ICompositeDefinition) definition;
                return getRecursiveDef(compositeDefinition.getDefinition(childLookup[i]), childLookup, i + 1);
            }
            return null;
        }

        public void setDefinition(StructDefinition definition) {
            fDefinition = definition;
        }

    }

    @Override
    public String toString() {
        /* Only used for debugging */
        StringBuilder sb = new StringBuilder();
        sb.append("[declaration] struct["); //$NON-NLS-1$
        for (int i = 0; i < fFields.length; i++) {
            sb.append(fFieldNames[i]).append(':').append(fFields[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        for (int i = 0; i < fFields.length; i++) {
            result = prime * result + fFieldNames[i].hashCode();
            result = prime * result + fFields[i].hashCode();
        }
        result = (prime * result) + (int) (fMaxAlign ^ (fMaxAlign >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof StructDeclaration)) {
            return false;
        }
        StructDeclaration other = (StructDeclaration) obj;
        if (fFields.length != other.fFields.length) {
            return false;
        }

        List<String> localFieldNames = Arrays.asList(fFieldNames);
        List<IDeclaration> localDecs = Arrays.asList(fFields);
        List<String> otherFieldNames = Arrays.asList(other.fFieldNames);
        List<IDeclaration> otherDecs = Arrays.asList(other.fFields);

        // check fields in order
        for (int i = 0; i < fFields.length; i++) {
            if ((!localFieldNames.get(i).equals(otherFieldNames.get(i))) ||
                    (!otherDecs.get(i).equals(localDecs.get(i)))) {
                return false;
            }
        }
        return (fMaxAlign == other.fMaxAlign);
    }

    @Override
    public boolean isBinaryEquivalent(IDeclaration obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof StructDeclaration)) {
            return false;
        }
        StructDeclaration other = (StructDeclaration) obj;
        if (fFields.length != other.fFields.length) {
            return false;
        }
        List<IDeclaration> localDecs = Arrays.asList(fFields);
        List<IDeclaration> otherDecs = Arrays.asList(other.fFields);
        for (int i = 0; i < fFields.length; i++) {
            if (!otherDecs.get(i).isBinaryEquivalent(localDecs.get(i))) {
                return false;
            }
        }
        return (fMaxAlign == other.fMaxAlign);
    }

}
