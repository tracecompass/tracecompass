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
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTrace;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfTraceIndexer;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.TmfCheckpointIndexer;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard for custom XML trace parsers.
 *
 * @author Patrick Tasse
 */
public class CustomXmlParserWizard extends Wizard implements INewWizard {

    CustomXmlParserInputWizardPage inputPage;
    CustomParserOutputWizardPage outputPage;
    private ISelection selection;
    CustomXmlTraceDefinition fDefinition;
    String initialCategoryName;
    String initialDefinitionName;

    /**
     * Default constructor
     */
    public CustomXmlParserWizard() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param definition
     *            The trace definition
     */
    public CustomXmlParserWizard(CustomXmlTraceDefinition definition) {
        super();
        this.fDefinition = definition;
        if (definition != null) {
            initialCategoryName = definition.categoryName;
            initialDefinitionName = definition.definitionName;
        }
        setWindowTitle(Messages.CustomXmlParserInputWizardPage_windowTitle);
    }

    @Override
    public boolean performFinish() {
        CustomTraceDefinition def = outputPage.getDefinition();
        if (fDefinition != null) {
            if (!initialCategoryName.equals(def.categoryName) || !initialDefinitionName.equals(def.definitionName)) {
                CustomXmlTraceDefinition.delete(initialCategoryName, initialDefinitionName);
            }
            CustomParserUtils.cleanup(CustomXmlTrace.buildTraceTypeId(initialCategoryName, initialDefinitionName));
        }
        def.save();
        CustomParserUtils.cleanup(CustomTxtTrace.buildTraceTypeId(def.categoryName, def.definitionName));
        return true;
    }

    /**
     * Adding the page to the wizard.
     */

    @Override
    public void addPages() {
        String traceType = "CustomXmlTrace"; //$NON-NLS-1$
        Function<TraceParams, ITmfTrace> builder = (TraceParams tp) -> {
            CustomTraceDefinition definition = tp.getDefinition();
            if (definition instanceof CustomXmlTraceDefinition) {
                CustomXmlTrace trace;
                try {
                    trace = new CustomXmlTrace(null, (CustomXmlTraceDefinition) definition, tp.getFile().getAbsolutePath(), tp.getCacheSize()) {
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
        inputPage = new CustomXmlParserInputWizardPage(selection, fDefinition);
        addPage(inputPage);
        outputPage = new CustomParserOutputWizardPage(inputPage, fDefinition, traceType, builder); //$NON-NLS-1$
        addPage(outputPage);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection sel) {
        this.selection = sel;
    }

}
