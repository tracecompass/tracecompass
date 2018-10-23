/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Mathieu Rail - Added functionality for getting a module's requirements
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.internal.tmf.core.analysis.TmfAnalysisModuleSourceConfigElement;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.osgi.framework.Bundle;

/**
 * Analysis module helper for modules provided by a plugin's configuration
 * elements.
 *
 * @author Geneviève Bastien
 */
public class TmfAnalysisModuleHelperConfigElement implements IAnalysisModuleHelper {

    /** Note: This comparator is not symmetric so it cannot be used by regular sorting algorithms */
    private static final @NonNull Comparator<@NonNull ApplicableClass> APPLICABLE_CLASS_COMPARATOR = new Comparator<@NonNull ApplicableClass>() {

        @Override
        public int compare(@NonNull ApplicableClass o1, @NonNull ApplicableClass o2) {
            if (o1.fClass.equals(o2.fClass)) {
                // Classes are the same
                return 0;
            }
            // Otherwise, if one class is assignable from the other, the most generic one is smaller
            if (o1.fClass.isAssignableFrom(o2.fClass)) {
                return -1;
            }
            if (o2.fClass.isAssignableFrom(o1.fClass)) {
                return 1;
            }
            return 0;
        }

    };

    /** Class that stores whether the analysis applies to a certain trace type */
    private static class ApplicableClass {
        private final Class<?> fClass;
        private final boolean fApplies;

        public ApplicableClass(Class<?> clazz, boolean applies) {
            fClass = clazz;
            fApplies = applies;
        }
    }

    private final IConfigurationElement fCe;
    private @Nullable List<ApplicableClass> fApplicableClasses = null;

    /**
     * Constructor
     *
     * @param ce
     *            The source {@link IConfigurationElement} of this module helper
     */
    public TmfAnalysisModuleHelperConfigElement(IConfigurationElement ce) {
        fCe = ce;
    }

    // ----------------------------------------
    // Wrappers to {@link IAnalysisModule} methods
    // ----------------------------------------

    @Override
    public String getId() {
        String id = fCe.getAttribute(TmfAnalysisModuleSourceConfigElement.ID_ATTR);
        if (id == null) {
            throw new IllegalStateException();
        }
        return id;
    }

    @Override
    public String getName() {
        String name = fCe.getAttribute(TmfAnalysisModuleSourceConfigElement.NAME_ATTR);
        if (name == null) {
            throw new IllegalStateException();
        }
        return name;
    }

    @Override
    public boolean isAutomatic() {
        return Boolean.parseBoolean(fCe.getAttribute(TmfAnalysisModuleSourceConfigElement.AUTOMATIC_ATTR));
    }

    /**
     * @since 1.0
     */
    @Override
    public boolean appliesToExperiment() {
        return Boolean.parseBoolean(fCe.getAttribute(TmfAnalysisModuleSourceConfigElement.APPLIES_EXP_ATTR));
    }

    @Override
    public String getHelpText() {
        /*
         * FIXME: No need to externalize this. A better solution will be found
         * soon and this string is just temporary
         */
        return "The trace must be opened to get the help message"; //$NON-NLS-1$
    }

    @Override
    public String getIcon() {
        return fCe.getAttribute(TmfAnalysisModuleSourceConfigElement.ICON_ATTR);
    }

    @Override
    public Bundle getBundle() {
        return getBundle(fCe);
    }

    private static Bundle getBundle(IConfigurationElement element) {
        return ContributorFactoryOSGi.resolve(element.getContributor());
    }

    private List<ApplicableClass> fillApplicableClasses() {
        List<IConfigurationElement> ces = new ArrayList<>();
        /*
         * Get the module's applying tracetypes, first from the extension point itself
         */
        IConfigurationElement[] tracetypeCE = fCe.getChildren(TmfAnalysisModuleSourceConfigElement.TRACETYPE_ELEM);
        ces.addAll(Arrays.asList(tracetypeCE));
        /* Then those in their separate extension */
        tracetypeCE = Platform.getExtensionRegistry().getConfigurationElementsFor(TmfAnalysisModuleSourceConfigElement.TMF_ANALYSIS_TYPE_ID);
        String id = getId();
        for (IConfigurationElement element : tracetypeCE) {
            String elementName = element.getName();
            if (elementName.equals(TmfAnalysisModuleSourceConfigElement.TRACETYPE_ELEM)) {
                String analysisId = element.getAttribute(TmfAnalysisModuleSourceConfigElement.ID_ATTR);
                if (id.equals(analysisId)) {
                    ces.add(element);
                }
            }
        }
        Map<Class<?>, ApplicableClass> classMap = new HashMap<>();
        /*
         * Convert the configuration element to applicable classes and keep only the
         * latest one for a class
         */
        ces.forEach(ce -> {
            ApplicableClass traceTypeApplies = parseTraceTypeElement(ce);
            if (traceTypeApplies != null) {
                classMap.put(traceTypeApplies.fClass, traceTypeApplies);
            }
        });
        List<ApplicableClass> applicableClasses = new ArrayList<>(classMap.values());
        return sortApplicableClasses(applicableClasses);
    }

    private static List<ApplicableClass> sortApplicableClasses(List<ApplicableClass> applicableClasses) {
        if (applicableClasses.isEmpty()) {
            return Collections.emptyList();
        }
        List<ApplicableClass> sorted = new ArrayList<>(applicableClasses.size());
        sorted.add(applicableClasses.get(0));
        for (int i = 1; i < applicableClasses.size(); i++) {
            int pos = i;
            ApplicableClass current = applicableClasses.get(i);
            for (int j = 0; j < sorted.size(); j++) {
                int cmp = APPLICABLE_CLASS_COMPARATOR.compare(Objects.requireNonNull(current), Objects.requireNonNull(sorted.get(j)));
                if (cmp < 0) {
                    pos = j;
                } else if (cmp > 0) {
                    pos = j + 1;
                }
            }
            sorted.add(pos, current);
        }
        return sorted;
    }

    private static ApplicableClass parseTraceTypeElement(IConfigurationElement element) {
        try {
            Class<?> applyclass = getBundle(element).loadClass(element.getAttribute(TmfAnalysisModuleSourceConfigElement.CLASS_ATTR));
            String classAppliesVal = element.getAttribute(TmfAnalysisModuleSourceConfigElement.APPLIES_ATTR);
            boolean classApplies = true;
            if (classAppliesVal != null) {
                classApplies = Boolean.parseBoolean(classAppliesVal);
            }
            if (classApplies) {
                return new ApplicableClass(applyclass, true);
            }
            return new ApplicableClass(applyclass, false);
        } catch (ClassNotFoundException | InvalidRegistryObjectException e) {
            Activator.logError("Error in applies to trace", e); //$NON-NLS-1$
        }
        return null;
    }


    private List<ApplicableClass> getApplicableClasses() {
        List<ApplicableClass> applicableClasses = fApplicableClasses;
        if (applicableClasses == null) {
            applicableClasses = fillApplicableClasses();
            fApplicableClasses = applicableClasses;
        }
        return applicableClasses;
    }

    private @Nullable Boolean appliesToTraceClass(Class<? extends ITmfTrace> traceclass) {
        Boolean applies = null;
        for (ApplicableClass clazz : getApplicableClasses()) {
            if (clazz.fApplies) {
                if (clazz.fClass.isAssignableFrom(traceclass)) {
                    applies = true;
                }
            } else {
                /*
                 * If the trace type does not apply, reset the applies variable to false
                 */
                if (clazz.fClass.isAssignableFrom(traceclass)) {
                    applies = false;
                }
            }
        }
        return applies;
    }

    @Override
    public boolean appliesToTraceType(Class<? extends ITmfTrace> traceclass) {
        Boolean applies = appliesToTraceClass(traceclass);

        /* Check if it applies to an experiment */
        if (applies == null && TmfExperiment.class.isAssignableFrom(traceclass)) {
            return appliesToExperiment();
        }
        return applies == null ? false : applies;
    }

    @Override
    public Iterable<Class<? extends ITmfTrace>> getValidTraceTypes() {
        Set<Class<? extends ITmfTrace>> traceTypes = new HashSet<>();

        for (TraceTypeHelper tth : TmfTraceType.getTraceTypeHelpers()) {
            if (appliesToTraceType(tth.getTraceClass())) {
                traceTypes.add(tth.getTraceClass());
            }
        }

        return traceTypes;
    }

    @Override
    public Iterable<TmfAbstractAnalysisRequirement> getAnalysisRequirements() {
        IAnalysisModule module = createModule();
        if (module != null) {
            Iterable<@NonNull TmfAbstractAnalysisRequirement> requirements = module.getAnalysisRequirements();
            module.dispose();
            return requirements;
        }
        return Collections.emptySet();

    }

    // ---------------------------------------
    // Functionalities
    // ---------------------------------------

    private IAnalysisModule createModule() {
        IAnalysisModule module = null;
        try {
            module = (IAnalysisModule) fCe.createExecutableExtension(TmfAnalysisModuleSourceConfigElement.ANALYSIS_MODULE_ATTR);
            module.setName(getName());
            module.setId(getId());
        } catch (CoreException e) {
            Activator.logError("Error getting analysis modules from configuration files", e); //$NON-NLS-1$
        }
        return module;
    }

    @Override
    public IAnalysisModule newModule(ITmfTrace trace) throws TmfAnalysisException {

        /* Check if it applies to trace itself */
        Boolean applies = appliesToTraceClass(trace.getClass());
        /*
         * If the trace is an experiment, check if this module would apply to an
         * experiment should it apply to one of its traces.
         */
        if (applies == null && (trace instanceof TmfExperiment) && appliesToExperiment()) {
            for (ITmfTrace expTrace : TmfTraceManager.getTraceSet(trace)) {
                if (appliesToTraceClass(expTrace.getClass()) == Boolean.TRUE) {
                    applies = true;
                    break;
                }
            }
        }

        if (applies != Boolean.TRUE) {
            return null;
        }

        IAnalysisModule module = createModule();
        if (module == null) {
            return null;
        }

        module.setAutomatic(isAutomatic());

        /* Get the module's parameters */
        final IConfigurationElement[] parametersCE = fCe.getChildren(TmfAnalysisModuleSourceConfigElement.PARAMETER_ELEM);
        for (IConfigurationElement element : parametersCE) {
            String paramName = element.getAttribute(TmfAnalysisModuleSourceConfigElement.NAME_ATTR);
            if (paramName == null) {
                continue;
            }
            module.addParameter(paramName);
            String defaultValue = element.getAttribute(TmfAnalysisModuleSourceConfigElement.DEFAULT_VALUE_ATTR);
            if (defaultValue != null) {
                module.setParameter(paramName, defaultValue);
            }
        }
        if (module.setTrace(trace)) {
            TmfAnalysisManager.analysisModuleCreated(module);
        } else {
            module.dispose();
            module = null;
        }

        return module;

    }

    @Override
    public String getHelpText(@NonNull ITmfTrace trace) {
        IAnalysisModule module = createModule();
        if (module != null) {
            String ret = module.getHelpText(trace);
            module.dispose();
            return ret;
        }
        return getHelpText();

    }
}
