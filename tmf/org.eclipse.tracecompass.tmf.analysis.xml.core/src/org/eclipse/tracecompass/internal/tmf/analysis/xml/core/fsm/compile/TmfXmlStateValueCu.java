/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenActionStateChange;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenActionStateChange.StackAction;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValue;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueConstant;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueEventField;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueEventName;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValuePool;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueQuery;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueScript;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueSelf;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueStackPeek;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue.Type;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.w3c.dom.Element;

/**
 * The compilation unit for values from XML elements
 *
 * @author Geneviève Bastien
 * @author Florian Wininger
 * @author Jean-Christian Kouame
 */
public class TmfXmlStateValueCu implements IDataDrivenCompilationUnit {

    private final Supplier<DataDrivenValue> fGenerator;

    /**
     * Constructor
     *
     * Package-private because only classes from this package can build this
     *
     * @param generator
     *            The state value generator class for this state value
     */
    TmfXmlStateValueCu(Supplier<DataDrivenValue> generator) {
        fGenerator = generator;
    }

    /** The state value generator for query values */
    private static class StateValueQueryGenerator implements Supplier<DataDrivenValue> {

        private final List<TmfXmlStateValueCu> fList;
        private final @Nullable String fMappingGroupId;
        private final Type fForcedType;

        StateValueQueryGenerator(List<TmfXmlStateValueCu> list, @Nullable String mappingGroup, Type forcedType) {
            fList = list;
            fMappingGroupId = mappingGroup;
            fForcedType = forcedType;
        }

        @Override
        public DataDrivenValue get() {
            List<DataDrivenValue> generated = new ArrayList<>();
            for (TmfXmlStateValueCu subValue : fList) {
                generated.add(subValue.generate());
            }
            return new DataDrivenValueQuery(fMappingGroupId, fForcedType, generated);
        }

    }

    /** The value generator for stack peek values */
    private static class StateValueStackPeekGenerator implements Supplier<DataDrivenValue> {

        private final List<TmfXmlStateValueCu> fList;
        private final @Nullable String fMappingGroupId;
        private final Type fForcedType;

        StateValueStackPeekGenerator(List<TmfXmlStateValueCu> list, @Nullable String mappingGroup, Type forcedType) {
            fList = list;
            fMappingGroupId = mappingGroup;
            fForcedType = forcedType;
        }

        @Override
        public DataDrivenValue get() {
            List<DataDrivenValue> generated = new ArrayList<>();
            for (TmfXmlStateValueCu subValue : fList) {
                generated.add(subValue.generate());
            }
            return new DataDrivenValueStackPeek(fMappingGroupId, fForcedType, generated);
        }

    }

    /** The value generator for script values */
    private static class StateValueScriptGenerator implements Supplier<DataDrivenValue> {

        private final Map<String, TmfXmlStateValueCu> fMap;
        private final String fScript;
        private final String fScriptEngine;
        private final @Nullable String fMappingGroupId;
        private final Type fForcedType;

        StateValueScriptGenerator(Map<String, TmfXmlStateValueCu> list, String script, String scriptEngine, @Nullable String mappingGroup, Type forcedType) {
            fMap = list;
            fScript = script;
            fScriptEngine = scriptEngine;
            fMappingGroupId = mappingGroup;
            fForcedType = forcedType;
        }

        @Override
        public DataDrivenValue get() {
            Map<String, DataDrivenValue> values = new HashMap<>();
            fMap.entrySet().forEach(entry -> values.put(entry.getKey(), Objects.requireNonNull(entry.getValue()).generate()));
            return new DataDrivenValueScript(fMappingGroupId, fForcedType, values, fScript, fScriptEngine);
        }

    }

    @Override
    public DataDrivenValue generate() {
        return Objects.requireNonNull(fGenerator.get());
    }

    /**
     * Compile a stateAttribute XML element. It returns a list since some attributes
     * may link to location values that are replaced by the corresponding attribute
     * values.
     *
     * @param analysisData
     *            The analysis data already compiled
     * @param valueEl
     *            The XML element to compile
     * @return A list of value compilation units this element compiles to, or
     *         <code>null</code> if there was a compilation error
     */
    public static @Nullable List<TmfXmlStateValueCu> compileAttribute(AnalysisCompilationData analysisData, Element valueEl) {
        String type = valueEl.getAttribute(TmfXmlStrings.TYPE);
        Type forcedType = ITmfStateValue.Type.NULL;
        switch (type) {
        case TmfXmlStrings.TYPE_CONSTANT: {
            String name = getValueString(analysisData, valueEl);
            if (name == null || name.isEmpty()) {
                // TODO: Validation message here
                Activator.logError("The value of a constant attribute should not be null"); //$NON-NLS-1$
                return null;
            }
            TmfXmlStateValueCu tmfXmlStateValueCu = new TmfXmlStateValueCu(() -> new DataDrivenValueConstant(null, forcedType, name));
            return Collections.singletonList(tmfXmlStateValueCu);
        }
        case TmfXmlStrings.EVENT_FIELD: {
            String name = getValueString(analysisData, valueEl);
            if (name == null || name.isEmpty()) {
                // TODO: Validation message here
                Activator.logError("The value of an event field attribute should not be null"); //$NON-NLS-1$
                return null;
            }
            return Collections.singletonList(new TmfXmlStateValueCu(() -> new DataDrivenValueEventField(null, forcedType, name)));
        }
        case TmfXmlStrings.TYPE_LOCATION: {
            String name = getValueString(analysisData, valueEl);
            if (name == null || name.isEmpty()) {
                // TODO: Validation message here
                Activator.logError("The value of a location attribute should not be null"); //$NON-NLS-1$
                return null;
            }
            TmfXmlLocationCu location = analysisData.getLocation(name);
            if (location == null) {
                // TODO: Validation message here
                Activator.logError("Location " + name + " does not exist"); //$NON-NLS-1$ //$NON-NLS-2$
                return null;
            }
            return location.getValues();
        }
        case TmfXmlStrings.TYPE_QUERY: {
            List<Element> childElements = TmfXmlUtils.getChildElements(valueEl, TmfXmlStrings.STATE_ATTRIBUTE);
            List<TmfXmlStateValueCu> subAttribs = new ArrayList<>();
            for (Element subAttributeNode : childElements) {
                List<TmfXmlStateValueCu> subAttrib = TmfXmlStateValueCu.compileAttribute(analysisData, subAttributeNode);
                if (subAttrib == null) {
                    return null;
                }
                subAttribs.addAll(subAttrib);
            }
            return Collections.singletonList(new TmfXmlStateValueCu(new StateValueQueryGenerator(subAttribs, null, forcedType)));
        }
        case TmfXmlStrings.TYPE_EVENT_NAME:
            return Collections.singletonList(new TmfXmlStateValueCu(() -> new DataDrivenValueEventName(null)));
        case TmfXmlStrings.NULL:
            return Collections.singletonList(new TmfXmlStateValueCu(() -> new DataDrivenValueConstant(null, forcedType, null)));
        case TmfXmlStrings.TYPE_SELF:
            return Collections.singletonList(new TmfXmlStateValueCu(() -> new DataDrivenValueSelf(forcedType)));
        case TmfXmlStrings.TYPE_POOL:
            return Collections.singletonList(new TmfXmlStateValueCu(() -> new DataDrivenValuePool()));
        default:
            Activator.logError("Compiling state value: The XML element is not of the right type " + type); //$NON-NLS-1$
        }
        return null;
    }

    /**
     * Get the 'value' attribute of this element. It replaces defined values by the
     * corresponding string
     */
    private static @Nullable String getValueString(AnalysisCompilationData analysisContent, Element attribute) {
        return analysisContent.getStringValue(attribute.getAttribute(TmfXmlStrings.VALUE));
    }

    /**
     * Compile a stateValue XML element
     *
     * @param analysisData
     *            The analysis data already compiled
     * @param valueEl
     *            The XML element to compile
     * @return The value compilation unit or <code>null</code> if there were
     *         compilation errors
     */
    public static @Nullable TmfXmlStateValueCu compileValue(AnalysisCompilationData analysisData, Element valueEl) {
        String type = valueEl.getAttribute(TmfXmlStrings.TYPE);

        /*
         * Stack peek will have a separate treatment here, as the state value may have
         * sub attributes
         *
         * FIXME: This case should not be supported this way, have a special type for
         * stack peek and remove the peek stack action..
         */
        String stack = valueEl.getAttribute(TmfXmlStrings.ATTRIBUTE_STACK);
        StackAction stackAction = DataDrivenActionStateChange.StackAction.getTypeFromString(stack);
        if (stackAction == StackAction.PEEK) {
            type = TmfXmlStrings.STACK_PEEK;
        }

        // Verify mapping group data
        String mapGroupAttrib = valueEl.getAttribute(TmfXmlStrings.MAPPING_GROUP);
        TmfXmlMappingGroupCu mappingGroup = analysisData.getMappingGroup(mapGroupAttrib);
        // Make sure the mapping group exists
        if (!mapGroupAttrib.isEmpty() && mappingGroup == null) {
            // TODO: Validation message here
            Activator.logError("The mapping group " + mapGroupAttrib + " does not exist in this analysis"); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }
        String mappingGroupId = (mapGroupAttrib.isEmpty() ? null : mapGroupAttrib);

        /*
         * Forced type allows to convert the value to a certain type : For example, a
         * process's TID in an event field may arrive with a LONG format but we want to
         * store the data in an INT
         */
        String forcedTypeName = valueEl.getAttribute(TmfXmlStrings.FORCED_TYPE);
        ITmfStateValue.Type forcedType = forcedTypeName.isEmpty() ? ITmfStateValue.Type.NULL : TmfXmlUtils.getTmfStateValueByName(forcedTypeName);

        switch (type) {
        case TmfXmlStrings.TYPE_INT: {
            if (mappingGroup != null) {
                // TODO: Validation message here
                Activator.logWarning("state value is type int but a mappingGroup is specified"); //$NON-NLS-1$
            }
            String value = getValueString(analysisData, valueEl);
            try {
                int intValue = Integer.parseInt(value);
                return new TmfXmlStateValueCu(() -> new DataDrivenValueConstant(mappingGroupId, forcedType, intValue));
            } catch (NumberFormatException e) {
                Activator.logError("Compiling state value: value is not a parseable integer " + value); //$NON-NLS-1$
                return null;
            }
        }
        case TmfXmlStrings.TYPE_LONG: {
            if (mappingGroup != null) {
                // TODO: Validation message here
                Activator.logWarning("state value is type long but a mappingGroup is specified"); //$NON-NLS-1$
            }
            String value = getValueString(analysisData, valueEl);
            try {
                long longValue = Long.parseLong(value);
                return new TmfXmlStateValueCu(() -> new DataDrivenValueConstant(mappingGroupId, forcedType, longValue));
            } catch (NumberFormatException e) {
                Activator.logError("Compiling state value: value is not a parseable long " + value); //$NON-NLS-1$
                return null;
            }
        }
        case TmfXmlStrings.TYPE_STRING: {
            String value = getValueString(analysisData, valueEl);
            return new TmfXmlStateValueCu(() -> new DataDrivenValueConstant(mappingGroupId, forcedType, value));
        }
        case TmfXmlStrings.TYPE_NULL: {
            if (mappingGroup != null) {
                // TODO: Validation message here
                Activator.logWarning("state value is type null but a mappingGroup is specified"); //$NON-NLS-1$
            }
            return new TmfXmlStateValueCu(() -> new DataDrivenValueConstant(mappingGroupId, forcedType, null));
        }
        case TmfXmlStrings.EVENT_FIELD: {
            String name = getValueString(analysisData, valueEl);
            if (name == null || name.isEmpty()) {
                // TODO: Validation message here
                Activator.logError("The value of an event field attribute should not be null"); //$NON-NLS-1$
                return null;
            }
            return new TmfXmlStateValueCu(() -> new DataDrivenValueEventField(mappingGroupId, forcedType, name));
        }
        case TmfXmlStrings.TYPE_EVENT_NAME:
            return new TmfXmlStateValueCu(() -> new DataDrivenValueEventName(mappingGroupId));
        case TmfXmlStrings.TYPE_DELETE: {
            if (mappingGroup != null) {
                // TODO: Validation message here
                Activator.logWarning("state value is type delete but a mappingGroup is specified"); //$NON-NLS-1$
            }
            return new TmfXmlStateValueCu(() -> new DataDrivenValueConstant(mappingGroupId, forcedType, null));
        }
        case TmfXmlStrings.TYPE_QUERY: {
            List<Element> childElements = TmfXmlUtils.getChildElements(valueEl, TmfXmlStrings.STATE_ATTRIBUTE);
            List<TmfXmlStateValueCu> subAttribs = new ArrayList<>();
            for (Element subAttributeNode : childElements) {
                List<TmfXmlStateValueCu> subAttrib = TmfXmlStateValueCu.compileAttribute(analysisData, subAttributeNode);
                if (subAttrib == null) {
                    return null;
                }
                subAttribs.addAll(subAttrib);
            }
            return new TmfXmlStateValueCu(new StateValueQueryGenerator(subAttribs, mappingGroupId, forcedType));
        }
        case TmfXmlStrings.TYPE_SCRIPT: {
            List<Element> childElements = TmfXmlUtils.getChildElements(valueEl, TmfXmlStrings.STATE_VALUE);
            Map<String, TmfXmlStateValueCu> values = new HashMap<>();
            for (Element subAttributeNode : childElements) {
                // state values in a script should have an ID
                String valueId = subAttributeNode.getAttribute(TmfXmlStrings.ID);
                TmfXmlStateValueCu subAttrib = TmfXmlStateValueCu.compileValue(analysisData, subAttributeNode);
                if (subAttrib == null) {
                    return null;
                }
                values.put(valueId, subAttrib);
            }
            String script = getValueString(analysisData, valueEl);
            if (script == null) {
                Activator.logError("The script resolves to null"); //$NON-NLS-1$
                return null;
            }
            String scriptEngine = valueEl.getAttribute(TmfXmlStrings.SCRIPT_ENGINE);
            if (scriptEngine.isEmpty()) {
                scriptEngine = DataDrivenValueScript.DEFAULT_SCRIPT_ENGINE;
            }
            return new TmfXmlStateValueCu(new StateValueScriptGenerator(values, script, scriptEngine, mappingGroupId, forcedType));
        }
        case TmfXmlStrings.STACK_PEEK: {
            // A stack peek is like a query at the top of the stack
            List<Element> childElements = TmfXmlUtils.getChildElements(valueEl, TmfXmlStrings.STATE_ATTRIBUTE);
            if (childElements.isEmpty()) {
                // TODO: Validation message here
                Activator.logWarning("Compiling state value: Stack peek should have children state attributes"); //$NON-NLS-1$
            }
            List<TmfXmlStateValueCu> subAttribs = new ArrayList<>();
            for (Element subAttributeNode : childElements) {
                List<TmfXmlStateValueCu> subAttrib = TmfXmlStateValueCu.compileAttribute(analysisData, subAttributeNode);
                if (subAttrib == null) {
                    return null;
                }
                subAttribs.addAll(subAttrib);
            }
            return new TmfXmlStateValueCu(new StateValueStackPeekGenerator(subAttribs, mappingGroupId, forcedType));
        }
        default:
            Activator.logError("Compiling state value: The XML element is not of the right type " + type); //$NON-NLS-1$
        }
        return null;

    }

    /**
     * Compile a field XML element
     *
     * @param analysisData
     *            The analysis data already compiled
     * @param fieldEl
     *            The XML element to compile
     * @return The value compilation unit or <code>null</code> if there were
     *         compilation errors
     */
    public static @Nullable TmfXmlStateValueCu compileField(AnalysisCompilationData analysisData, Element fieldEl) {
        String name = analysisData.getStringValue(fieldEl.getAttribute(TmfXmlStrings.NAME));
        if (name == null || name.isEmpty()) {
            // TODO: Validation message here
            Activator.logError("The value of an event field attribute should not be null"); //$NON-NLS-1$
            return null;
        }
        return new TmfXmlStateValueCu(() -> new DataDrivenValueEventField(null, ITmfStateValue.Type.NULL, name));

    }

    /**
     * Compile a list of state value compilation unit as a single query value, to
     * support some use cases allowed by the XSD that could be well rewritten as a
     * query stateValue
     *
     * @param attributesCu
     *            The list of value compilation units mapping the query
     * @return The query value compilation unit
     */
    public static TmfXmlStateValueCu compileAsQuery(List<TmfXmlStateValueCu> attributesCu) {
        return new TmfXmlStateValueCu(new StateValueQueryGenerator(attributesCu, null, ITmfStateValue.Type.NULL));
    }

}
