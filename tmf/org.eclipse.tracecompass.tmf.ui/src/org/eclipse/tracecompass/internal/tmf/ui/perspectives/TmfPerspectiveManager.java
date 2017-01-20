/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Patrick Tasse - extracted code from org.eclipse.debug.ui plug-in
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.ui.perspectives;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfUIPreferences;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceTypeUIUtils;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

/**
 * The perspective manager manages the 'perspective' setting defined in the
 * 'tracetypeui' extension point. Specifically it:
 * <ul>
 * <li>manages the user preference to allow perspective switch
 * <li>changes to the associated perspective when a trace is opened
 * </ul>
 */
public class TmfPerspectiveManager {

    private static TmfPerspectiveManager fInstance;

    /**
     * Flag used to indicate that the user is already being prompted to
     * switch perspectives. This flag allows us to not open multiple
     * prompts at the same time.
     */
    private boolean fPrompting;

    /**
     * Constructor
     */
    public TmfPerspectiveManager() {
        TmfSignalManager.register(this);
    }

    /**
     * Initializes the perspective manager.
     */
    public static synchronized void init() {
        if (fInstance == null) {
            fInstance = new TmfPerspectiveManager();
        }
    }

    /**
     * Disposes the perspective manager.
     */
    public static synchronized void dispose() {
        if (fInstance != null) {
            TmfSignalManager.deregister(fInstance);
            fInstance = null;
        }
    }

    /**
     * Signal handler for the traceOpened signal.
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public synchronized void traceOpened(final TmfTraceOpenedSignal signal) {
        Display.getDefault().asyncExec(() -> {
            String id = null;
            /*
             * For experiments, switch only if all traces have the same
             * associated perspective id.
             */
            for (ITmfTrace trace : TmfTraceManager.getTraceSet(signal.getTrace())) {
                String perspectiveId = TmfTraceTypeUIUtils.getPerspectiveId(trace);
                if (id != null && !id.equals(perspectiveId)) {
                    return;
                }
                id = perspectiveId;
            }
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (id != null && window != null && shouldSwitchPerspective(window, id, ITmfUIPreferences.SWITCH_TO_PERSPECTIVE)) {
                switchToPerspective(window, id);
            }
        });
    }

    /**
     * Returns whether or not the user wishes to switch to the specified
     * perspective when a launch occurs.
     *
     * @param perspectiveName
     *            the name of the perspective that will be presented to the user
     *            for confirmation if they've asked to be prompted about
     *            perspective switching
     * @param message
     *            a message to be presented to the user. This message is
     *            expected to contain a slot for the perspective name to be
     *            inserted ("{0}").
     * @param preferenceKey
     *            the preference key of the perspective switching preference
     * @return whether or not the user wishes to switch to the specified
     *         perspective automatically
     */
    private boolean shouldSwitchPerspective(IWorkbenchWindow window, String perspectiveId, String preferenceKey) {
        if (isCurrentPerspective(window, perspectiveId)) {
            return false;
        }
        IPerspectiveDescriptor perspective = PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
        String perspectiveName = (perspective == null) ? null : perspective.getLabel();
        if (perspectiveName == null) {
            return false;
        }
        String switchPerspective = Activator.getDefault().getPreferenceStore().getString(preferenceKey);
        if (MessageDialogWithToggle.ALWAYS.equals(switchPerspective)) {
            return true;
        } else if (MessageDialogWithToggle.NEVER.equals(switchPerspective)) {
            return false;
        }

        Shell shell= window.getShell();
        if (shell == null || fPrompting) {
            return false;
        }
        fPrompting = true;
        MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(
                shell,
                Messages.TmfPerspectiveManager_SwitchPerspectiveDialogTitle,
                MessageFormat.format(Messages.TmfPerspectiveManager_SwitchPerspectiveDialogMessage, perspectiveName),
                null,
                false,
                Activator.getDefault().getPreferenceStore(),
                preferenceKey);
        boolean answer = (dialog.getReturnCode() == IDialogConstants.YES_ID);
        synchronized (this) {
            fPrompting= false;
            notifyAll();
        }
        if (isCurrentPerspective(window, perspectiveId)) {
            answer = false;
        }
        return answer;
    }

    /**
     * Returns whether the given perspective identifier matches the identifier
     * of the current perspective.
     *
     * @param window
     *            the workbench window
     *
     * @param perspectiveId
     *            the identifier
     * @return whether the given perspective identifier matches the identifier
     *         of the current perspective
     */
    private static boolean isCurrentPerspective(IWorkbenchWindow window, String perspectiveId) {
        boolean isCurrent= false;
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                IPerspectiveDescriptor perspectiveDescriptor = page.getPerspective();
                if (perspectiveDescriptor != null) {
                    isCurrent= perspectiveId.equals(perspectiveDescriptor.getId());
                }
            }
        }
        return isCurrent;
    }

    /**
     * Switches to the specified perspective.
     *
     * @param window
     *            the workbench window
     * @param id
     *            perspective identifier
     */
    protected void switchToPerspective(IWorkbenchWindow window, String id) {
        try {
            window.getWorkbench().showPerspective(id, window);
        } catch (WorkbenchException e) {
            TraceUtils.displayErrorMsg(
                    Messages.TmfPerspectiveManager_SwitchPerspectiveErrorTitle,
                    Messages.TmfPerspectiveManager_SwitchPerspectiveErrorMessage,
                    e);
        }
    }
}
