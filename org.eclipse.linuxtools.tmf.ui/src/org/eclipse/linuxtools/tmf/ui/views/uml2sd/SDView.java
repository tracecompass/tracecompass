/**********************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd;

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.BaseMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Frame;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.SyncMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.SyncMessageReturn;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ConfigureMinMax;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.FirstPage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.KeyBindingsManager;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.LastPage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.MoveToMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.NextPage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.OpenSDFiltersDialog;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.OpenSDFindDialog;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.OpenSDPagesDialog;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.PrevPage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.Print;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ShowNodeEnd;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ShowNodeStart;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.Zoom;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.Zoom.ZoomType;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.IExtendedFilterProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.IExtendedFindProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDAdvancedPagingProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDCollapseProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDExtendedActionBarProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDFilterProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDFindProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDGraphNodeSupporter;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDPagingProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDPropertiesProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.load.IUml2SDLoader;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.load.LoadersManager;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;

/**
 * <p>
 * This class is a generic sequence diagram view implementation.
 * </p>

 * @version 1.0
 * @author sveyrier
 */
public class SDView extends ViewPart {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Name of menu separator for view modes
     * @since 2.0
     */
    public static final String UML2SD_VIEW_MODES_SEPARATOR = "UML2SD_VIEW_MODES"; //$NON-NLS-1$
    /**
     * Name of menu separator for working set
     * @since 2.0
     */
    public static final String UML2SD_WORKING_SET_SEPARATOR = "UML2SD_WORKING_SET"; //$NON-NLS-1$
    /**
     * Name of menu separator for sorting
     * @since 2.0
     */
    public static final String UML2SD_SORTING_SEPARATOR = "UML2SD_SORTING"; //$NON-NLS-1$
    /**
     * Name of menu separator for filtering
     * @since 2.0
     */
    public static final String UML2SD_FILTERING_SEPARATOR = "UML2SD_FILTERING"; //$NON-NLS-1$
    /**
     * Name of menu separator for view layout
     * @since 2.0
     */
    public static final String UML2SD_VIEW_LAYOUT_SEPARATOR = "UML2SD_VIEW_LAYOUT"; //$NON-NLS-1$
    /**
     * Name of menu separator for link editor
     * @since 2.0
     */
    public static final String UML2SD_LINK_EDITOR_SEPARATOR = "UML2SD_LINK_EDITOR"; //$NON-NLS-1$
    /**
     * Name of menu separator for other commands
     * @since 2.0
     */
    public static final String UML2SD_OTHER_COMMANDS_SEPARATOR = "UML2SD_OTHER_COMMANDS"; //$NON-NLS-1$
    /**
     * Name of menu separator for other plug-in commands
     * @since 2.0
     */
    public static final String UML2SD_OTHER_PLUGINS_COMMANDS_SEPARATOR = "UML2SD_OTHER_PLUGINS_COMMANDS"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The sequence diagram widget.
     */
    private SDWidget fSdWidget = null;
    /**
     * The time compression bar.
     */
    private TimeCompressionBar fTimeCompressionBar = null;
    /**
     * The sequence diagram find provider implementation.
     */
    private ISDFindProvider fSdFindProvider = null;
    /**
     * The sequence diagram paging provider implementation.
     */
    private ISDPagingProvider fSdPagingProvider = null;
    /**
     * The sequence diagram filter provider implementation.
     */
    private ISDFilterProvider fSdFilterProvider = null;
    /**
     * The extended sequence diagram filter provider implementation.
     */
    private IExtendedFilterProvider fSdExFilterProvider = null;
    /**
     * The extended sequence diagram find provider implementation.
     */
    private IExtendedFindProvider fSdExFindProvider = null;
    /**
     * The extended sequence diagram action bar provider implementation.
     */
    private ISDExtendedActionBarProvider fSdExtendedActionBarProvider = null;
    /**
     * The sequence diagram property provider implementation.
     */
    private ISDPropertiesProvider fSdPropertiesProvider = null;
    /**
     * Button for executing the next page action.
     */
    private NextPage fNextPageButton = null;
    /**
     * Button for executing the previous page action.
     */
    private PrevPage fPrevPageButton = null;
    /**
     * Button for executing the first page page action.
     */
    private FirstPage fFirstPageButton = null;
    /**
     * Button for executing the last page action.
     */
    private LastPage fLastPageButton = null;
    /**
     * The menu manager reference.
     */
    private MenuManager fMenuMgr = null;
    /**
     * Flag to indicate whether view needs initialization or not.
     */
    private boolean fNeedInit = true;
    /**
     * WaitCursor is the cursor to be displayed when long tasks are running
     */
    private Cursor fWaitCursor;

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(Composite c) {
        Composite parent = new Composite(c, SWT.NONE);
        GridLayout parentLayout = new GridLayout();
        parentLayout.numColumns = 2;
        parentLayout.marginWidth = 0;
        parentLayout.marginHeight = 0;
        parent.setLayout(parentLayout);

        GridData timeLayoutdata = new GridData(GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
        timeLayoutdata.widthHint = 10;
        GridData seqDiagLayoutData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
        fTimeCompressionBar = new TimeCompressionBar(parent, SWT.NONE);
        fTimeCompressionBar.setLayoutData(timeLayoutdata);
        fSdWidget = new SDWidget(parent, SWT.NONE);
        fSdWidget.setLayoutData(seqDiagLayoutData);
        fSdWidget.setSite(this);
        fSdWidget.setTimeBar(fTimeCompressionBar);

        // Add this view to the key bindings manager
        KeyBindingsManager.getInstance().add(this.getSite().getId());

        createCoolbarContent();

        hookContextMenu();

        fTimeCompressionBar.setVisible(false);
        parent.layout(true);

        Print print = new Print(this);
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.PRINT.getId(), print);

        fNeedInit = restoreLoader();
    }

    /**
     * Load a blank page that is supposed to explain that a kind of interaction must be chosen.
     */
    protected void loadBlank() {
        IUml2SDLoader loader = new BlankUml2SdLoader();
        loader.setViewer(this);
        setContentDescription(loader.getTitleString());
    }

    @Override
    public void setFocus() {
        if (fSdWidget != null) {
            // update actions for key bindings
            KeyBindingsManager.getInstance().setSdView(this);
            fSdWidget.setFocus();
        }
        if (isViewReady() && fNeedInit) {
            fNeedInit = restoreLoader();
        }
    }

    @Override
    public void dispose() {
        KeyBindingsManager.getInstance().remove(this.getSite().getId());
        super.dispose();
    }

    /**
     * Returns the SD widget.
     *
     * @return The SD widget.
     */
    public SDWidget getSDWidget() {
        return fSdWidget;
    }

    /**
     * Set the find provider for the opened sequence diagram viewer<br>
     * If the provider is not set, the find menu item will not be available in the viewer<br>
     * A find provider is called back when the user perform a find action<br>
     * The find provider is responsible to move the sequence diagram to the GraphNode which match the
     * find criteria as well as to highlight the GraphNode
     *
     * @param provider the search provider
     */
    public void setSDFindProvider(ISDFindProvider provider) {
        fSdFindProvider = provider;
        fSdExFindProvider = null;
        createCoolbarContent();
        if (provider != null) {
            KeyBindingsManager.getInstance().setFindEnabled(true);
        }
        else {
            KeyBindingsManager.getInstance().setFindEnabled(false);
        }
    }

    /**
     * Set the find provider for the opened sequence diagram viewer<br>
     * If the provider is not set, the find menu item will not be available in
     * the viewer<br>
     * A find provider is called back when the user perform a find action<br>
     * If the extended find provider is set, it replaces the regular find
     * provider (sdFindProvider).<br>
     *
     * @param provider
     *            The provider to set
     */
    public void setExtendedFindProvider(IExtendedFindProvider provider) {
        fSdExFindProvider = provider;
        fSdFindProvider = null;
        createCoolbarContent();
        if (provider != null) {
            KeyBindingsManager.getInstance().setFindEnabled(true);
        }
        else {
            KeyBindingsManager.getInstance().setFindEnabled(false);
        }
    }

    /**
     * Returns the extended find provider
     *
     * @return extended find provider.
     */
    public IExtendedFindProvider getExtendedFindProvider() {
        return fSdExFindProvider;
    }

    /**
     * Resets all providers.
     */
    public void resetProviders() {
        KeyBindingsManager.getInstance().setFindEnabled(false);
        fSdFindProvider = null;
        fSdExFindProvider = null;
        fSdFilterProvider = null;
        fSdExFilterProvider = null;
        fSdPagingProvider = null;
        fSdExtendedActionBarProvider = null;
        fSdPropertiesProvider = null;
        if ((fSdWidget != null) && (!fSdWidget.isDisposed())) {
            fSdWidget.setCollapseProvider(null);
        }
    }

    /**
     * Set the filter provider for the opened sequence diagram viewer<br>
     * If the provider is not set, the filter menu item will not be available in the viewer<br>
     * A filter provider is called back when the user perform a filter action<br>
     *
     * @param provider the filter provider
     */
    public void setSDFilterProvider(ISDFilterProvider provider) {
        fSdFilterProvider = provider;
        // Both systems can be used now, commenting out next statement
        createCoolbarContent();
    }

    /**
     * Sets the extended filter provider for the opened sequence diagram viewer.
     *
     * @param provider
     *            The provider to set
     */
    public void setExtendedFilterProvider(IExtendedFilterProvider provider) {
        fSdExFilterProvider = provider;
        // Both systems can be used now, commenting out next statement
        createCoolbarContent();
    }

    /**
     * Returns the extended find provider.
     *
     * @return The extended find provider.
     */
    public IExtendedFilterProvider getExtendedFilterProvider() {
        return fSdExFilterProvider;
    }

    /**
     * Register the given provider to support Drag and Drop collapsing. This provider is
     * responsible of updating the Frame.
     *
     * @param provider - the provider to register
     */
    public void setCollapsingProvider(ISDCollapseProvider provider) {
        if ((fSdWidget != null) && (!fSdWidget.isDisposed())) {
            fSdWidget.setCollapseProvider(provider);
        }
    }

    /**
     * Set the page provider for the opened sequence diagram viewer<br>
     * If the sequence diagram provided (see setFrame) need to be split in many parts, a paging provider must be
     * provided in order to handle page change requested by the user<br>
     * Set a page provider will create the next and previous page buttons in the viewer coolBar
     *
     * @param provider the paging provider
     */
    public void setSDPagingProvider(ISDPagingProvider provider) {
        fSdPagingProvider = provider;
        createCoolbarContent();
    }

    /**
     * Returns the current page provider for the view
     *
     * @return the paging provider
     */
    public ISDPagingProvider getSDPagingProvider() {
        return fSdPagingProvider;
    }

    /**
     * Returns the current find provider for the view
     *
     * @return the find provider
     */
    public ISDFindProvider getSDFindProvider() {
        return fSdFindProvider;
    }

    /**
     * Returns the current filter provider for the view
     *
     * @return the filter provider
     */
    public ISDFilterProvider getSDFilterProvider() {
        return fSdFilterProvider;
    }

    /**
     * Set the extended action bar provider for the opened sequence diagram viewer<br>
     * This allow to add programmatically actions in the coolbar and/or in the drop-down menu
     *
     * @param provider the search provider
     */
    public void setSDExtendedActionBarProvider(ISDExtendedActionBarProvider provider) {
        fSdExtendedActionBarProvider = provider;
        createCoolbarContent();
    }

    /**
     * Returns the current extended action bar provider for the view
     *
     * @return the extended action bar provider
     */
    public ISDExtendedActionBarProvider getSDExtendedActionBarProvider() {
        return fSdExtendedActionBarProvider;
    }

    /**
     * Set the properties view provider for the opened sequence diagram viewer
     *
     * @param provider the properties provider
     */
    public void setSDPropertiesProvider(ISDPropertiesProvider provider) {
        fSdPropertiesProvider = provider;
    }

    /**
     * Returns the current extended action bar provider for the view.
     *
     * @return the extended action bar provider
     */
    public ISDPropertiesProvider getSDPropertiesProvider() {
        return fSdPropertiesProvider;
    }

    /**
     * Sets the sdWidget.
     *
     * @param sdWidget
     *          A sdWidget to set
     * @since 2.0
     */
    protected void setSDWidget(SDWidget sdWidget) {
        fSdWidget = sdWidget;
    }

    /**
     * Sets the time compression bar.
     *
     * @param timeCompressionbar
     *          A sdWidget to set
     * @since 2.0
     */
    protected void setTimeBar(TimeCompressionBar timeCompressionbar) {
        fTimeCompressionBar = timeCompressionbar;
    }

    /**
     * Sets the initialization flag.
     *
     * @param needInit
     *          flag value to set
     * @since 2.0
     */
    protected void setNeedInit(boolean needInit) {
        fNeedInit = needInit;
    }

    /**
     * Creates the basic sequence diagram menu
     */
    protected void hookContextMenu() {
        fMenuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        fMenuMgr.setRemoveAllWhenShown(true);
        fMenuMgr.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                fillContextMenu(manager);
            }
        });
        Menu menu = fMenuMgr.createContextMenu(fSdWidget.getViewControl());
        fSdWidget.getViewControl().setMenu(menu);
        getSite().registerContextMenu(fMenuMgr, fSdWidget.getSelectionProvider());
    }

    /**
     * Returns the context menu manager
     *
     * @return the menu manager
     */
    public MenuManager getMenuManager() {
        return fMenuMgr;
    }

    /**
     * Fills the basic sequence diagram menu and define the dynamic menu item insertion point
     *
     * @param manager the menu manager
     */
    protected void fillContextMenu(IMenuManager manager) {
        manager.add(new Separator("Additions")); //$NON-NLS-1$
        if (getSDWidget() != null && getSDWidget().getCurrentGraphNode() != null) {
            ISelectionProvider selProvider = fSdWidget.getSelectionProvider();
            ISelection sel = selProvider.getSelection();
            int nbMessage = 0;
            Iterator<?> it = ((StructuredSelection) sel).iterator();
            while (it.hasNext()) {
                Object node = it.next();
                if (node instanceof BaseMessage) {
                    nbMessage++;
                }
            }
            if (nbMessage != 1) {
                return;
            }
            GraphNode node = getSDWidget().getCurrentGraphNode();
            if ((node instanceof SyncMessageReturn) && (((SyncMessageReturn) node).getMessage() != null)) {
                Action goToMessage = new MoveToMessage(this);
                goToMessage.setText(Messages.SequenceDiagram_GoToMessage);
                manager.add(goToMessage);
            }
            if ((node instanceof SyncMessage) && (((SyncMessage) node).getMessageReturn() != null)) {
                Action goToMessage = new MoveToMessage(this);
                goToMessage.setText(Messages.SequenceDiagram_GoToMessageReturn);
                manager.add(goToMessage);
            }
        }
        manager.add(new Separator("MultiSelectAdditions")); //$NON-NLS-1$
    }

    /**
     * Enables/Disables an action with given name.
     *
     * @param actionName The action name
     * @param state true or false
     */
    public void setEnableAction(String actionName, boolean state) {
        IActionBars bar = getViewSite().getActionBars();
        if (bar != null) {
            IContributionItem item = bar.getMenuManager().find(actionName);
            if ((item != null) && (item instanceof ActionContributionItem)) {
                IAction action = ((ActionContributionItem) item).getAction();
                if (action != null) {
                    action.setEnabled(state);
                }
                item.setVisible(state);
                bar.updateActionBars();
            }
        }
    }

    /**
     * Creates the coolBar icon depending on the actions supported by the Sequence Diagram provider<br>
     * - Navigation buttons are displayed if ISDPovider.HasPaging return true<br>
     * - Navigation buttons are enabled depending on the value return by ISDPovider.HasNext and HasPrev<br>
     *
     * @see ISDGraphNodeSupporter Action support definition
     * @see SDView#setSDFilterProvider(ISDFilterProvider)
     * @see SDView#setSDFindProvider(ISDFindProvider)
     * @see SDView#setSDPagingProvider(ISDPagingProvider)
     */
    protected void createCoolbarContent() {
        IActionBars bar = getViewSite().getActionBars();

        bar.getMenuManager().removeAll();
        bar.getToolBarManager().removeAll();

        createMenuGroup();

        Zoom resetZoom = new Zoom(this, ZoomType.ZOOM_RESET);
        bar.getMenuManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, resetZoom);
        bar.getToolBarManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, resetZoom);

        Zoom noZoom = new Zoom(this, ZoomType.ZOOM_NONE);
        noZoom.setChecked(true);
        bar.getMenuManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, noZoom);
        bar.getToolBarManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, noZoom);

        Zoom zoomIn = new Zoom(this, ZoomType.ZOOM_IN);
        bar.getMenuManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, zoomIn);
        bar.getToolBarManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, zoomIn);

        Zoom zoomOut = new Zoom(this, ZoomType.ZOOM_OUT);
        bar.getMenuManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, zoomOut);
        bar.getToolBarManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, zoomOut);

        MenuManager navigation = new MenuManager(Messages.SequenceDiagram_Navigation);

        ShowNodeStart showNodeStart = new ShowNodeStart(this);
        showNodeStart.setText(Messages.SequenceDiagram_ShowNodeStart);

        showNodeStart.setId("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ShowNodeStart");//$NON-NLS-1$
        showNodeStart.setActionDefinitionId("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ShowNodeStart");//$NON-NLS-1$
        navigation.add(showNodeStart);

        ShowNodeEnd showNodeEnd = new ShowNodeEnd(this);
        showNodeEnd.setText(Messages.SequenceDiagram_ShowNodeEnd);

        showNodeEnd.setId("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ShowNodeEnd");//$NON-NLS-1$
        showNodeEnd.setActionDefinitionId("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ShowNodeEnd");//$NON-NLS-1$
        navigation.add(showNodeEnd);

        bar.getMenuManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, navigation);

        ConfigureMinMax minMax = new ConfigureMinMax(this);
        minMax.setText(Messages.SequenceDiagram_ConfigureMinMax);
        minMax.setId("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ConfigureMinMax");//$NON-NLS-1$
        bar.getMenuManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, minMax);

        if ((fSdWidget.getFrame() != null) && (fSdWidget.getFrame().hasTimeInfo())) {
            minMax.setEnabled(true);
        } else {
            minMax.setEnabled(false);
        }

        // Do we need to display a paging item
        if (fSdPagingProvider != null) {
            fNextPageButton = new NextPage(this);
            bar.getToolBarManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, fNextPageButton);
            fNextPageButton.setEnabled(fSdPagingProvider.hasNextPage());
            bar.getMenuManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, fNextPageButton);

            fPrevPageButton = new PrevPage(this);
            bar.getToolBarManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, fPrevPageButton);
            fPrevPageButton.setEnabled(fSdPagingProvider.hasPrevPage());
            bar.getMenuManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, fPrevPageButton);

            fFirstPageButton = new FirstPage(this);
            bar.getToolBarManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, fFirstPageButton);
            fFirstPageButton.setEnabled(fSdPagingProvider.hasPrevPage());
            bar.getMenuManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, fFirstPageButton);

            fLastPageButton = new LastPage(this);
            bar.getToolBarManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, fLastPageButton);
            fLastPageButton.setEnabled(fSdPagingProvider.hasNextPage());
            bar.getMenuManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, fLastPageButton);
        }

        if (fSdExFilterProvider != null) {
            Action action = fSdExFilterProvider.getFilterAction();
            if (action != null) {
                if (action.getId() == null)
                 {
                    action.setId("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.extendedFilter"); //$NON-NLS-1$
                }
                if (action.getImageDescriptor() == null) {
                    action.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_FILTERS));
                }
                if (action.getText() == null || action.getText().length() == 0) {
                    action.setText(Messages.SequenceDiagram_EditFilters);
                }
                bar.getMenuManager().prependToGroup(UML2SD_FILTERING_SEPARATOR, action);
                bar.getToolBarManager().prependToGroup(UML2SD_FILTERING_SEPARATOR, action);
            }
        }
        // Both systems can be used now: commenting out else keyword
        if (fSdFilterProvider != null) {
            bar.getMenuManager().appendToGroup(UML2SD_FILTERING_SEPARATOR, new OpenSDFiltersDialog(this, fSdFilterProvider));
        }
        if (fSdPagingProvider instanceof ISDAdvancedPagingProvider) {
            IContributionItem sdPaging = bar.getMenuManager().find(OpenSDPagesDialog.ID);
            if (sdPaging != null) {
                bar.getMenuManager().remove(sdPaging);
                sdPaging = null;
            }
            bar.getMenuManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, new OpenSDPagesDialog(this, (ISDAdvancedPagingProvider) fSdPagingProvider));
            updatePagesMenuItem(bar);
        }

        if (fSdExFindProvider != null) {
            Action action = fSdExFindProvider.getFindAction();
            if (action != null) {
                if (action.getId() == null) {
                    action.setId("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.extendedFind"); //$NON-NLS-1$
                }
                if (action.getImageDescriptor() == null) {
                    action.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_SEARCH_SEQ));
                }
                if (action.getText() == null) {
                    action.setText(Messages.SequenceDiagram_Find + "..."); //$NON-NLS-1$
                }
                bar.getMenuManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, action);
                bar.getToolBarManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, action);
            }
        } else if (fSdFindProvider != null) {
            bar.getMenuManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, new OpenSDFindDialog(this));
            bar.getToolBarManager().appendToGroup(UML2SD_OTHER_COMMANDS_SEPARATOR, new OpenSDFindDialog(this));
        }

        if (fSdExtendedActionBarProvider != null) {
            fSdExtendedActionBarProvider.supplementCoolbarContent(bar);
        }

        bar.updateActionBars();
    }

    /**
     * Updates the view coolbar buttons state according to the value return by: -
     * ISDExtendedActionBarProvider.hasNextPage()<br>
     * - ISDExtendedActionBarProvider.hasPrevPage()<br>
     *
     */
    public void updateCoolBar() {
        if (fSdPagingProvider != null) {
            IActionBars bar = getViewSite().getActionBars();
            if (bar == null) {
                return;
            }
            IToolBarManager barManager = bar.getToolBarManager();
            if (barManager == null) {
                return;
            }
            IContributionItem nextPage = barManager.find(NextPage.ID);
            if (nextPage instanceof ActionContributionItem) {
                IAction nextPageAction = ((ActionContributionItem) nextPage).getAction();
                if (nextPageAction instanceof NextPage) {
                    ((NextPage) nextPageAction).setEnabled(fSdPagingProvider.hasNextPage());
                }
            }

            IContributionItem prevPage = barManager.find(PrevPage.ID);
            if (prevPage instanceof ActionContributionItem) {
                IAction prevPageAction = ((ActionContributionItem) prevPage).getAction();
                if (prevPageAction instanceof PrevPage) {
                    ((PrevPage) prevPageAction).setEnabled(fSdPagingProvider.hasPrevPage());
                }
            }

            IContributionItem firstPage = barManager.find(FirstPage.ID);
            if (firstPage instanceof ActionContributionItem) {
                IAction firstPageAction = ((ActionContributionItem) firstPage).getAction();
                if (firstPageAction instanceof FirstPage) {
                    ((FirstPage) firstPageAction).setEnabled(fSdPagingProvider.hasPrevPage());
                }
            }

            IContributionItem lastPage = barManager.find(LastPage.ID);
            if (lastPage instanceof ActionContributionItem) {
                IAction lastPageAction = ((ActionContributionItem) lastPage).getAction();
                if (lastPageAction instanceof LastPage) {
                    ((LastPage) lastPageAction).setEnabled(fSdPagingProvider.hasNextPage());
                }
            }

            updatePagesMenuItem(bar);
        }
    }

    /**
     * Enables or disables the Pages... menu item, depending on the number of pages
     *
     * @param bar the bar containing the action
     */
    protected void updatePagesMenuItem(IActionBars bar) {
        if (fSdPagingProvider instanceof ISDAdvancedPagingProvider) {
            IMenuManager menuManager = bar.getMenuManager();
            ActionContributionItem contributionItem = (ActionContributionItem) menuManager.find(OpenSDPagesDialog.ID);
            IAction openSDPagesDialog = null;
            if (contributionItem != null) {
                openSDPagesDialog = contributionItem.getAction();
            }

            if (openSDPagesDialog instanceof OpenSDPagesDialog) {
                openSDPagesDialog.setEnabled(((ISDAdvancedPagingProvider) fSdPagingProvider).pagesCount() > 1);
            }
        }
    }

    /**
     * The frame to render (the sequence diagram)
     *
     * @param frame the frame to display
     */
    public void setFrame(Frame frame) {
        setFrame(frame, true);
    }

    /**
     * The frame to render (the sequence diagram)
     *
     * @param frame the frame to display
     * @param resetPosition boolean Flag whether to reset the position or not.
     */
    protected void setFrame(Frame frame, boolean resetPosition) {
        if (getSDWidget() == null) {
            return;
        }

        if (frame == null) {
            loadBlank();
            return;
        }

        IUml2SDLoader loader = LoadersManager.getInstance().getCurrentLoader(getViewSite().getId(), this);
        if (loader == null) {
            return;
        }

        if (loader.getTitleString() != null) {
            setContentDescription(loader.getTitleString());
        }

        getSDWidget().setFrame(frame, resetPosition);

        if (fTimeCompressionBar != null) {
            fTimeCompressionBar.setFrame(frame);
        }
        updateCoolBar();
        if (fTimeCompressionBar != null) {
            if (!frame.hasTimeInfo()) {
                Composite parent = fTimeCompressionBar.getParent();
                fTimeCompressionBar.setVisible(false);
                parent.layout(true);
            } else {
                Composite parent = fTimeCompressionBar.getParent();
                fTimeCompressionBar.setVisible(true);
                parent.layout(true);
            }
        }
        IContributionItem shortKeysMenu = getViewSite().getActionBars().getMenuManager().find("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers");//$NON-NLS-1$
        MenuManager shortKeys = (MenuManager) shortKeysMenu;
        if (shortKeys != null) {
            IContributionItem[] items = shortKeys.getItems();
            for (int i = 0; i < items.length; i++) {
                if (items[i] instanceof ActionContributionItem) {
                    IAction action = ((ActionContributionItem) items[i]).getAction();
                    if (action != null) {
                        action.setEnabled(true);
                    }
                }
            }
        }
        createCoolbarContent();
    }

    /**
     * Activate or deactivate the short key command given in parameter (see plugin.xml)
     *
     * @param id the command id defined in the plugin.xml
     * @param value the state value
     */
    public void setEnableCommand(String id, boolean value) {
        IContributionItem shortKeysMenu = getViewSite().getActionBars().getMenuManager().find("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers");//$NON-NLS-1$
        MenuManager shortKeys = (MenuManager) shortKeysMenu;
        if (shortKeys == null) {
            return;
        }
        IContributionItem item = shortKeys.find(id);
        if ((item != null) && (item instanceof ActionContributionItem)) {
            IAction action = ((ActionContributionItem) item).getAction();
            if (action != null) {
                action.setEnabled(value);
            }
        }
    }

    /**
     * Set the frame from an other thread than the one executing the main loop
     *
     * @param frame The frame to set (and display)
     */
    public void setFrameSync(final Frame frame) {
        if (getSDWidget() == null || getSDWidget().isDisposed()) {
            return;
        }
        getSDWidget().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                if (getSDWidget() == null || getSDWidget().isDisposed() ||
                        ((fTimeCompressionBar != null) && fTimeCompressionBar.isDisposed())) {
                    return;
                }
                setFrame(frame);
            }
        });

    }

    /**
     * Ensure an object is visible from an other thread than the one executing the main loop
     *
     * @param sm The node to make visible in view
     */
    public void ensureVisibleSync(final GraphNode sm) {
        getSDWidget().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                if (getSDWidget() == null || getSDWidget().isDisposed()) {
                    return;
                }
                getSDWidget().ensureVisible(sm);
            }
        });
    }

    /**
     * Set the frame and ensure an object is visible from an other thread than the one executing the main loop
     *
     * @param sm The node to make visible in view
     * @param frame Frame The frame to set
     */
    public void setFrameAndEnsureVisibleSync(final Frame frame, final GraphNode sm) {
        if (getSDWidget() == null || getSDWidget().isDisposed()) {
            return;
        }
        getSDWidget().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                if (getSDWidget() == null || getSDWidget().isDisposed()) {
                    return;
                }
                setFrameAndEnsureVisible(frame, sm);
            }
        });
    }

    /**
     * Set the frame and ensure an object is visible
     *
     * @param sm The node to make visible in view
     * @param frame Frame The frame to set
     */
    public void setFrameAndEnsureVisible(Frame frame, GraphNode sm) {
        getSDWidget().clearSelection();
        setFrame(frame, false);
        getSDWidget().ensureVisible(sm);
    }

    /**
     * Set the frame and ensure an object is visible from an other thread than the one executing the main loop
     *
     * @param frame The frame to set.
     * @param x The x coordinate to make visible.
     * @param y The y coordinate to make visible.
     */
    public void setFrameAndEnsureVisibleSync(final Frame frame, final int x, final int y) {
        if (getSDWidget() == null || getSDWidget().isDisposed()) {
            return;
        }

        getSDWidget().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                setFrameAndEnsureVisible(frame, x, y);
            }
        });
    }

    /**
     * Set the frame and ensure an object is visible
     *
     * @param frame The frame to set.
     * @param x The x coordinate to make visible.
     * @param y The y coordinate to make visible.
     */
    public void setFrameAndEnsureVisible(Frame frame, int x, int y) {
        getSDWidget().clearSelection();
        setFrame(frame, false);
        getSDWidget().ensureVisible(x, y);
        getSDWidget().redraw();
    }

    /**
     * Toggle between default and wait cursors from an other thread than the one executing the main loop
     *
     * @param wait <code>true</code> for wait cursor else <code>false</code> for default cursor.
     */
    public void toggleWaitCursorAsync(final boolean wait) {
        if (getSDWidget() == null || getSDWidget().isDisposed()) {
            return;
        }

        getSDWidget().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (getSDWidget() == null || getSDWidget().isDisposed()) {
                    return;
                }
                if (wait) {
                    if (fWaitCursor != null && !fWaitCursor.isDisposed()) {
                        fWaitCursor.dispose();
                    }
                    fWaitCursor = new Cursor(getSDWidget().getDisplay(), SWT.CURSOR_WAIT);
                    getSDWidget().setCursor(fWaitCursor);
                    getSDWidget().getDisplay().update();
                } else {
                    if (fWaitCursor != null && !fWaitCursor.isDisposed()) {
                        fWaitCursor.dispose();
                    }
                    fWaitCursor = null;
                    getSDWidget().setCursor(null);
                    getSDWidget().getDisplay().update();
                }
            }
        });
    }

    /**
     * Return the time compression bar widget
     *
     * @return the time compression bar
     */
    public TimeCompressionBar getTimeCompressionBar() {
        return fTimeCompressionBar;
    }

    /**
     * Returns the current Frame (the sequence diagram container)
     *
     * @return the current frame
     */
    public Frame getFrame() {
        if (getSDWidget() != null) {
            return getSDWidget().getFrame();
        }
        return null;
    }

    /**
     * Gets the initialization flag.
     * @return the value of the initialization flag.
     * @since 2.0
     */
    protected boolean isNeedInit() {
        return fNeedInit;
    }

    /**
     * Restores the loader for the view based on the view ID.
     *
     * @return boolean <code>true</code> if initialization is needed else <code>false</code>.
     */
    protected boolean restoreLoader() {
        String id = getViewSite().getId();
        if (id == null) {
            return true;
        }
        IUml2SDLoader loader = LoadersManager.getInstance().getCurrentLoader(id, this);
        if ((loader != null)) {
            loader.setViewer(this);
            return false;
        }
        loadBlank();
        return true;
    }

    /**
     * Checks if current view is ready to be used.
     *
     * @return boolean <code>true</code> if view is ready else <code>false</code>.
     */
    protected boolean isViewReady() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page == null) {
            return false;
        }

        IViewReference[] ref = page.getViewReferences();
        for (int i = 0; i < ref.length; i++) {
            if (ref[i].getView(false) == this) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates the menu group.
     */
    protected void createMenuGroup() {
        IActionBars bar = getViewSite().getActionBars();
        if (bar == null) {
            return;
        }
        bar.getToolBarManager().add(new Separator(UML2SD_VIEW_MODES_SEPARATOR));
        bar.getToolBarManager().add(new Separator(UML2SD_WORKING_SET_SEPARATOR));
        bar.getToolBarManager().add(new Separator(UML2SD_SORTING_SEPARATOR));
        bar.getToolBarManager().add(new Separator(UML2SD_FILTERING_SEPARATOR));
        bar.getToolBarManager().add(new Separator(UML2SD_VIEW_LAYOUT_SEPARATOR));
        bar.getToolBarManager().add(new Separator(UML2SD_LINK_EDITOR_SEPARATOR));
        bar.getToolBarManager().add(new Separator(UML2SD_OTHER_COMMANDS_SEPARATOR));
        bar.getToolBarManager().add(new Separator(UML2SD_OTHER_PLUGINS_COMMANDS_SEPARATOR));
        bar.getMenuManager().add(new Separator(UML2SD_VIEW_MODES_SEPARATOR));
        bar.getMenuManager().add(new Separator(UML2SD_WORKING_SET_SEPARATOR));
        bar.getMenuManager().add(new Separator(UML2SD_SORTING_SEPARATOR));
        bar.getMenuManager().add(new Separator(UML2SD_FILTERING_SEPARATOR));
        bar.getMenuManager().add(new Separator(UML2SD_VIEW_LAYOUT_SEPARATOR));
        bar.getMenuManager().add(new Separator(UML2SD_LINK_EDITOR_SEPARATOR));
        bar.getMenuManager().add(new Separator(UML2SD_OTHER_COMMANDS_SEPARATOR));
        bar.getMenuManager().add(new Separator(UML2SD_OTHER_PLUGINS_COMMANDS_SEPARATOR));
    }

    @Override
    public Object getAdapter(Class adapter) {
        Object obj = super.getAdapter(adapter);
        if (fSdPropertiesProvider != null && adapter.equals(IPropertySheetPage.class)) {
            return fSdPropertiesProvider.getPropertySheetEntry();
        }

        return obj;
    }

    /**
     * Loader for a blank sequence diagram.
     *
     * @version 1.0
     */
    public static class BlankUml2SdLoader implements IUml2SDLoader {
        @Override
        public void setViewer(SDView viewer) {
            // Nothing to do
            Frame f = new Frame();
            f.setName(""); //$NON-NLS-1$
            viewer.setFrame(f);
        }

        @Override
        public String getTitleString() {
            return ""; //$NON-NLS-1$
        }

        @Override
        public void dispose() {
        }
    }
}
