/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs;

import org.eclipse.ui.PlatformUI;

/**
 * <p>
 * Factory for generating dialog boxes. It allows to overwrite the dialog implementation.
 * Useful also for testing purposes.
 * </p>
 *
 * @author Bernd Hufmann
 *
 */
public final class TraceControlDialogFactory {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    /**
     * The factory instance.
     */
    private static TraceControlDialogFactory fInstance;

    /**
     * The new connection dialog reference.
     */
    private INewConnectionDialog fNewConnectionDialog;

    /**
     * The enable channel dialog
     */
    private IEnableChannelDialog fEnableChannelDialog;

    /**
     * The create session dialog.
     */
    private ICreateSessionDialog fCreateSessionDialog;

    /**
     * The command script selection dialog.
     */
    private ISelectCommandScriptDialog fCommandScriptDialog;

    /**
     * The command script selection dialog.
     */
    private ILoadDialog fLoadDialog;

    /**
     * The save dialog.
     */
    private ISaveDialog fSaveDialog;

    /**
     * The enable events dialog.
     */
    private IEnableEventsDialog fEnableEventsDialog;

    /**
     * The get event info dialog.
     */
    private IGetEventInfoDialog fGetEventInfoDialog;

    /**
     * The get event info dialog.
     */
    private GetLoggerInfoDialog fGetLoggerInfoDialog;

    /**
     * The confirmation dialog implementation.
     */
    private IConfirmDialog fConfirmDialog;

    /**
     * The add context dialog implementation.
     */
    private IAddContextDialog fAddContextDialog;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor for R4EUIDialogFactory.
     */
    private TraceControlDialogFactory() {
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @return TraceControlDialogFactory instance
     */
    public static synchronized TraceControlDialogFactory getInstance() {
        if (fInstance == null) {
            fInstance = new TraceControlDialogFactory();
        }
        return fInstance;
    }

    /**
     * @return new connection dialog
     */
    public INewConnectionDialog getNewConnectionDialog() {
        if (fNewConnectionDialog == null) {
            fNewConnectionDialog = new NewConnectionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        }
        return fNewConnectionDialog;
    }

    /**
     * Sets a new connection dialog implementation.
     * @param newConnectionDialog - new connection dialog implementation
     */
    public void setNewConnectionDialog(INewConnectionDialog newConnectionDialog) {
        fNewConnectionDialog = newConnectionDialog;
    }

    /**
     * @return enable channel dialog
     */
    public IEnableChannelDialog getEnableChannelDialog() {
        if (fEnableChannelDialog == null) {
            fEnableChannelDialog = new EnableChannelDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        }
        return fEnableChannelDialog;
    }

    /**
     * Sets a enable channel dialog implementation.
     * @param createEnableDialog - a create channel dialog implementation
     */
    public void setEnableChannelDialog(IEnableChannelDialog createEnableDialog) {
        fEnableChannelDialog = createEnableDialog;
    }

    /**
     * @return create session dialog implementation
     */
    public ICreateSessionDialog getCreateSessionDialog() {
        if (fCreateSessionDialog == null) {
            fCreateSessionDialog = new CreateSessionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        }
        return fCreateSessionDialog;
    }

    /**
     * @return command script selection dialog implementation
     */
    public ISelectCommandScriptDialog getCommandScriptDialog() {
        if (fCommandScriptDialog == null) {
            fCommandScriptDialog = new OpenCommandScriptDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        }
        return fCommandScriptDialog;
    }

    /**
     * @return command script selection dialog implementation
     */
    public ILoadDialog getLoadDialog() {
        if (fLoadDialog == null) {
            fLoadDialog = new LoadDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        }
        return fLoadDialog;
    }

    /**
     * Sets a load dialog implementation
     * @param loadDialog
     *            a load dialog implementation
     */
    public void setLoadDialog(ILoadDialog loadDialog) {
        fLoadDialog = loadDialog;
    }

    /**
     * @return save dialog implementation
     */
    public ISaveDialog getSaveDialog() {
        if (fSaveDialog == null) {
            fSaveDialog = new SaveDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        }
        return fSaveDialog;
    }

    /**
     * Sets a save dialog implementation
     * @param saveDialog
     *            a save dialog implementation
     */
    public void setSaveDialog(ISaveDialog saveDialog) {
        fSaveDialog = saveDialog;
    }

    /**
     * Sets a create session dialog implementation.
     * @param createSessionDialog - a create session implementation.
     */
    public void setCreateSessionDialog(ICreateSessionDialog createSessionDialog) {
        fCreateSessionDialog = createSessionDialog;
    }

    /**
     * @return enable events dialog implementation.
     */
    public IEnableEventsDialog getEnableEventsDialog() {
        if (fEnableEventsDialog == null) {
            fEnableEventsDialog = new EnableEventsDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        }
        return fEnableEventsDialog;
    }

    /**
     * Sets a enable events dialog implementation.
     * @param enableEventsDialog - a enable events dialog implementation.
     */
    public void setEnableEventsDialog(IEnableEventsDialog enableEventsDialog) {
        fEnableEventsDialog = enableEventsDialog;
    }

    /**
     * @return get events info dialog implementation.
     */
    public IGetEventInfoDialog getGetEventInfoDialog() {
        if (fGetEventInfoDialog == null) {
            fGetEventInfoDialog = new GetEventInfoDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        }
        return fGetEventInfoDialog;
    }

    /**
     * @return get loggers info dialog implementation.
     */
    public GetLoggerInfoDialog getGetLoggerInfoDialog() {
        if (fGetLoggerInfoDialog == null) {
            fGetLoggerInfoDialog = new GetLoggerInfoDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        }
        return fGetLoggerInfoDialog;
    }

    /**
     * Sets a get events info dialog implementation.
     * @param getEventInfoDialog - a get events info dialog implementation
     */
    public void setGetEventInfoDialog(IGetEventInfoDialog getEventInfoDialog) {
        fGetEventInfoDialog = getEventInfoDialog;
    }

    /**
     * Sets a get loggers info dialog implementation.
     * @param getLoggerInfoDialog - a get loggers info dialog implementation
     */
    public void setGetLoggerInfoDialog(GetLoggerInfoDialog getLoggerInfoDialog) {
        fGetLoggerInfoDialog = getLoggerInfoDialog;
    }

    /**
     * @return the confirmation dialog implementation
     */
    public IConfirmDialog getConfirmDialog() {
        if (fConfirmDialog == null) {
            fConfirmDialog = new ConfirmDialog();
        }
        return fConfirmDialog;
    }

    /**
     * Sets the confirmation dialog implementation
     * @param confirmDialog - a confirmation dialog implementation
     */
    public void setConfirmDialog(IConfirmDialog confirmDialog) {
        fConfirmDialog = confirmDialog;
    }

    /**
     * @return the add context dialog implementation
     */
    public IAddContextDialog getAddContextDialog() {
        if (fAddContextDialog == null) {
            fAddContextDialog = new AddContextDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        }
        return fAddContextDialog;
    }

    /**
     * Sets the add context dialog information
     * @param addContextDialog - a add context dialog implementation
     */
    public void setAddContextDialog(IAddContextDialog addContextDialog) {
        fAddContextDialog = addContextDialog;
    }

}

