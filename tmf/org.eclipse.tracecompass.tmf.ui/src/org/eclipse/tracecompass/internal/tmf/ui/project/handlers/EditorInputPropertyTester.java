/*******************************************************************************
 * Copyright (c) 2015 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.tracecompass.internal.tmf.ui.editors.ITmfEventsEditorConstants;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.ui.IFileEditorInput;

/**
 * Property tester for editor inputs
 */
public class EditorInputPropertyTester extends PropertyTester {

    private static final String IS_EXPERIMENT_EDITOR_INPUT = "isExperimentEditorInput"; //$NON-NLS-1$

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

        if (IS_EXPERIMENT_EDITOR_INPUT.equals(property)) {
            if (receiver instanceof IFileEditorInput) {
                IFileEditorInput editorInput = (IFileEditorInput) receiver;
                IFile file = editorInput.getFile();
                if (file != null) {
                    try {
                        final String traceTypeId = file.getPersistentProperty(TmfCommonConstants.TRACETYPE);
                        if (traceTypeId != null && ITmfEventsEditorConstants.EXPERIMENT_INPUT_TYPE_CONSTANTS.contains(traceTypeId)) {
                            return true;
                        }
                    } catch (CoreException e) {
                        // Ignore
                    }
                }
            }
        }
        return false;
    }
}
