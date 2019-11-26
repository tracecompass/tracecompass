/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.parsers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.tmf.ui.editors.TmfTableColumnUtils;
import org.eclipse.tracecompass.internal.tmf.ui.project.operations.TmfWorkspaceModifyOperation;
import org.eclipse.tracecompass.tmf.core.TmfProjectNature;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.PlatformUI;

/**
 * Custom parser utility methods.
 *
 * @author Patrick Tasse
 *
 */
public class CustomParserUtils {

    private CustomParserUtils() {
        // Do nothing, private constructor
    }

    /**
     * Perform required cleanup when a custom parser is modified or deleted.
     *
     * @param traceTypeId
     *            the trace type id
     */
    public static void cleanup(@NonNull final String traceTypeId) {

        /*
         * Close all editors and delete supplementary files of traces with this trace type.
         */
        TmfWorkspaceModifyOperation operation = new TmfWorkspaceModifyOperation() {
            @Override
            public void execute(IProgressMonitor monitor) throws CoreException {
                for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
                    if (project.hasNature(TmfProjectNature.ID)) {
                        TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);
                        final TmfTraceFolder tracesFolder = projectElement.getTracesFolder();
                        if (tracesFolder != null) {
                            for (final TmfTraceElement trace : tracesFolder.getTraces()) {
                                if (monitor.isCanceled()) {
                                    throw new OperationCanceledException();
                                }
                                if (traceTypeId.equals(trace.getTraceType())) {
                                    Display.getDefault().syncExec(trace::closeEditors);
                                    trace.deleteSupplementaryResources();
                                    trace.refreshSupplementaryFolder();
                                }
                            }
                        }
                    }
                }

                /*
                 * Clear the column settings for this trace type. Must be done after closing the editors.
                 */
                TmfTableColumnUtils.clearColumnOrder(traceTypeId);
                TmfTableColumnUtils.clearColumnWidth(traceTypeId);
                TmfTableColumnUtils.clearColumnResizable(traceTypeId);
            }
        };
        try {
            PlatformUI.getWorkbench().getProgressService().run(true, true, operation);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (InvocationTargetException e) {
            TraceUtils.displayErrorMsg(e.toString(), e.getTargetException().toString());
        }
    }
}
