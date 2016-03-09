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

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.scope.ILexicalScope;

/**
 * A CTF structure declaration.
 *
 * A structure is similar to a C structure, it is a compound data type that
 * contains other datatypes in fields. they are stored in an hashmap and indexed
 * by names which are strings.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class StructDeclaration extends Declaration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** linked list of field names. So fieldName->fieldValue */
    private final @NonNull Map<@NonNull String, IDeclaration> fFieldMap = new LinkedHashMap<>();

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
        return fFieldMap.containsKey(name);
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
        return fFieldMap.get(fieldName);
    }

    /**
     * Gets the field list. Very important since the map of fields does not
     * retain the order of the fields.
     *
     * @return the field list.
     */
    public @NonNull Iterable<@NonNull String> getFieldsList() {
        return fFieldMap.keySet();
    }

    @Override
    public long getAlignment() {
        return this.fMaxAlign;
    }

    @Override
    public int getMaximumSize() {
        int maxSize = 0;
        for (IDeclaration field : fFieldMap.values()) {
            maxSize += field.getMaximumSize();
        }
        return Math.min(maxSize, Integer.MAX_VALUE);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public StructDefinition createDefinition(IDefinitionScope definitionScope,
            String fieldName, BitBuffer input) throws CTFException {
        alignRead(input);
        final Definition[] myFields = new Definition[fFieldMap.size()];
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
        final Definition[] myFields = new Definition[fFieldMap.size()];

        StructDefinition structDefinition = new StructDefinition(this, definitionScope,
                fieldScope, fieldScope.getName(), fFieldMap.keySet(), myFields);
        fillStruct(input, myFields, structDefinition);
        return structDefinition;
    }

    /**
     * Add a field to the struct
     *
     * @param name
     *            the name of the field, scopeless
     * @param declaration
     *            the declaration of the field
     */
    public void addField(@NonNull String name, @NonNull IDeclaration declaration) {
        fFieldMap.put(name, declaration);
        fMaxAlign = Math.max(fMaxAlign, declaration.getAlignment());
    }

    private void fillStruct(@NonNull BitBuffer input, final Definition[] myFields, StructDefinition structDefinition) throws CTFException {
        Iterator<Map.Entry<String, IDeclaration>> iter = fFieldMap.entrySet().iterator();
        for (int i = 0; i < fFieldMap.size(); i++) {
            Map.Entry<String, IDeclaration> entry = iter.next();
            /* We should not have inserted null keys... */
            String key = checkNotNull(entry.getKey());
            myFields[i] = entry.getValue().createDefinition(structDefinition, key, input);
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
        final Definition[] myFields = new Definition[fFieldMap.size()];
        IDefinitionScope merged = definitionScope;
        if (eventHeaderDef != null) {
            merged = new InternalDef(definitionScope, eventHeaderDef);
        }
        StructDefinition structDefinition = new StructDefinition(this, merged,
                fields, fields.getName(), fFieldMap.keySet(), myFields);
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
        for (Entry<String, IDeclaration> field : fFieldMap.entrySet()) {
            sb.append(field.getKey()).append(':').append(field.getValue());
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        for (Entry<String, IDeclaration> field : fFieldMap.entrySet()) {
            result = prime * result + field.getKey().hashCode();
            result = prime * result + field.getValue().hashCode();
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
        if (fFieldMap.size() != other.fFieldMap.size()) {
            return false;
        }

        List<String> localFieldNames = new ArrayList<>();
        localFieldNames.addAll(fFieldMap.keySet());

        List<IDeclaration> localDecs = new ArrayList<>();
        localDecs.addAll(fFieldMap.values());

        List<String> otherFieldNames = new ArrayList<>();
        otherFieldNames.addAll(other.fFieldMap.keySet());

        List<IDeclaration> otherDecs = new ArrayList<>();
        otherDecs.addAll(other.fFieldMap.values());

        // check fields in order
        for (int i = 0; i < fFieldMap.size(); i++) {
            if ((!localFieldNames.get(i).equals(otherFieldNames.get(i))) ||
                    (!otherDecs.get(i).equals(localDecs.get(i)))) {
                return false;
            }
        }

        if (fMaxAlign != other.fMaxAlign) {
            return false;
        }
        return true;
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
        if (fFieldMap.size() != other.fFieldMap.size()) {
            return false;
        }
        List<IDeclaration> localDecs = new ArrayList<>();
        localDecs.addAll(fFieldMap.values());
        List<IDeclaration> otherDecs = new ArrayList<>();
        otherDecs.addAll(other.fFieldMap.values());
        for (int i = 0; i < fFieldMap.size(); i++) {
            if (!otherDecs.get(i).isBinaryEquivalent(localDecs.get(i))) {
                return false;
            }
        }

        if (fMaxAlign != other.fMaxAlign) {
            return false;
        }
        return true;
    }

}
