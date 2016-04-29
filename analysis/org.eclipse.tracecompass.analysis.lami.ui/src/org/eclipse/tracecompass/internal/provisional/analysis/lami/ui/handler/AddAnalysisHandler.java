/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.handler;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.LamiConfigUtils;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiAnalysis;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiAnalysisFactoryException;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiAnalysisFactoryFromConfigFile;
import org.eclipse.tracecompass.tmf.core.analysis.ondemand.OnDemandAnalysisManager;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfOnDemandAnalysesElement;
import org.eclipse.ui.PlatformUI;

/**
 * The command handler for the "Add External Analysis" menu option.
 *
 * @author Philippe Proulx
 */
public class AddAnalysisHandler extends AbstractHandler {

    private static void showErrorBox(@Nullable Shell shell, Throwable e) {
        Display.getDefault().asyncExec(() -> {
            MessageDialog.openError(shell,
                    Messages.AddAnalysisDialog_ErrorBoxTitle,
                    Messages.AddAnalysisDialog_ErrorBoxMessage + ":\n" + e.toString()); //$NON-NLS-1$
        });
    }

    @Override
    public @Nullable Object execute(@Nullable ExecutionEvent event) throws ExecutionException {
        final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        final AddAnalysisDialog dialog = new AddAnalysisDialog(shell, Messages.AddAnalysisDialog_Title,
                NAME_INPUT_VALIDATOR, COMMAND_INPUT_VALIDATOR);

        if (dialog.open() != Window.OK) {
            // User clicked Cancel, cancel the add operation
            return null;
        }

        Path configFilePath;

        try {
            configFilePath = LamiConfigUtils.createConfigFile(checkNotNull(dialog.getName().trim()),
                    checkNotNull(dialog.getCommand().trim()));
        } catch (IOException e) {
            showErrorBox(shell, e);
            return null;
        }

        try {
            final LamiAnalysis analysis = LamiAnalysisFactoryFromConfigFile.buildFromConfigFile(configFilePath, true, trace -> true);
            OnDemandAnalysisManager.getInstance().registerAnalysis(analysis);
        } catch (LamiAnalysisFactoryException e) {
            showErrorBox(shell, e);
            return null;
        }

        final Object elem = HandlerUtils.getSelectedModelElement();

        if (elem != null && elem instanceof TmfOnDemandAnalysesElement) {
            final TmfOnDemandAnalysesElement analysesElem = (TmfOnDemandAnalysesElement) elem;
            analysesElem.refresh();
        }

        return null;
    }

    private static final IInputValidator NAME_INPUT_VALIDATOR = text -> {
        if (text.trim().isEmpty()) {
            return Messages.AddAnalysisDialog_NameEmptyErrorMessage;
        }

        return null;
    };

    private static final IInputValidator COMMAND_INPUT_VALIDATOR = text -> {
        if (text.trim().isEmpty()) {
            return Messages.AddAnalysisDialog_CommandEmptyErrorMessage;
        }

        return null;
    };

}
