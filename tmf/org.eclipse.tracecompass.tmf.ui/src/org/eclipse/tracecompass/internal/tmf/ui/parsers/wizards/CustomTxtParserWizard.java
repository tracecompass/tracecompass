/*******************************************************************************
 * Copyright (c) 2010, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.parsers.wizards;

import java.util.function.Function;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.internal.tmf.ui.parsers.CustomParserUtils;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTrace;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfTraceIndexer;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.TmfCheckpointIndexer;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard for custom text trace parsers.
 *
 * @author Patrick Tasse
 */
public class CustomTxtParserWizard extends Wizard implements INewWizard {

    CustomTxtParserInputWizardPage inputPage;
    CustomParserOutputWizardPage outputPage;
    private ISelection selection;
    CustomTxtTraceDefinition fDefinition;
    String initialCategoryName;
    String initialDefinitionName;

    /**
     * Default constructor
     */
    public CustomTxtParserWizard() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param definition
     *            The trace definition
     */
    public CustomTxtParserWizard(CustomTxtTraceDefinition definition) {
        super();
        this.fDefinition = definition;
        if (definition != null) {
            initialCategoryName = definition.categoryName;
            initialDefinitionName = definition.definitionName;
        }
        setWindowTitle(Messages.CustomTxtParserInputWizardPage_windowTitle);
    }

    @Override
    public boolean performFinish() {
        CustomTraceDefinition def = outputPage.getDefinition();
        if (fDefinition != null) {
            if (!initialCategoryName.equals(def.categoryName) || !initialDefinitionName.equals(def.definitionName)) {
                CustomTxtTraceDefinition.delete(initialCategoryName, initialDefinitionName);
            }
            CustomParserUtils.cleanup(CustomTxtTrace.buildTraceTypeId(initialCategoryName, initialDefinitionName));
        }
        def.save();
        CustomParserUtils.cleanup(CustomTxtTrace.buildTraceTypeId(def.categoryName, def.definitionName));
        return true;
    }

    @Override
    public void addPages() {
        String traceType = "CustomTxtTrace"; //$NON-NLS-1$
        Function<TraceParams, ITmfTrace> builder = (TraceParams tp) -> {
            CustomTraceDefinition definition = tp.getDefinition();
            if (definition instanceof CustomTxtTraceDefinition) {
                CustomTxtTrace trace;
                try {
                    trace = new CustomTxtTrace(null, (CustomTxtTraceDefinition) definition, tp.getFile().getAbsolutePath(), tp.getCacheSize()) {
                        @Override
                        protected ITmfTraceIndexer createIndexer(int interval) {
                            return new TmfCheckpointIndexer(this, interval);
                        }
                    };
                    trace.getIndexer().buildIndex(0, TmfTimeRange.ETERNITY, false);
                    return trace;
                } catch (TmfTraceException e) {
                    Activator.getDefault().logError("Error creating" + traceType + ". File:" + tp.getFile().getAbsolutePath() + e); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            return null;
        };
        inputPage = new CustomTxtParserInputWizardPage(selection, fDefinition);
        addPage(inputPage);
        outputPage = new CustomParserOutputWizardPage(inputPage, fDefinition, traceType, builder);
        addPage(outputPage);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection sel) {
        this.selection = sel;
    }

}
