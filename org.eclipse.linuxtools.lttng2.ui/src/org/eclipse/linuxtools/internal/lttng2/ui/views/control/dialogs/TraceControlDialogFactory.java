package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;

import org.eclipse.ui.PlatformUI;


final public class TraceControlDialogFactory {

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
     * The create channel dialog (on domain level)
     */
    private ICreateChannelDialog fCreateChannelDialog;
    
    /**
     * The create session dialog.
     */
    private ICreateSessionDialog fCreateSessionDialog;
    
    /**
     * The enable events dialog.
     */
    private IEnableEventsDialog fEnableEventsDialog;
    
    /**
     * The get event info dialog.
     */
    private IGetEventInfoDialog fGetEventInfoDialog;
    
    /**
     * The confirmation dialog implementation
     */
    private IConfirmDialog fConfirmDialog;

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
    public synchronized static TraceControlDialogFactory getInstance() {
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
     * @return create channel dialog (on domain level)
     */
    public ICreateChannelDialog getCreateChannelDialog() {
        if (fCreateChannelDialog == null) {
            fCreateChannelDialog = new CreateChannelDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        }
        return fCreateChannelDialog;
    }

    /**
     * Sets a create channel dialog implementation (on domain level).
     * @param createChannelDialog - a create channel dialog implementation
     */
    public void setCreateChannelDialog(ICreateChannelDialog createChannelDialog) {
        fCreateChannelDialog = createChannelDialog;
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
     * Sets a get events info dialog implementation.
     * @param getEventInfoDialog - a get events info dialog implementation
     */
    public void setGetEventInfoDialog(IGetEventInfoDialog getEventInfoDialog) {
        fGetEventInfoDialog = getEventInfoDialog;
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
     * @param confirmDialog
     */
    public void setConfirmDialog(IConfirmDialog confirmDialog) {
        fConfirmDialog = confirmDialog;
    }
}


