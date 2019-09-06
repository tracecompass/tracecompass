/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.preferences;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tracecompass.internal.tmf.remote.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.remote.ui.messages.RemoteMessages;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.ExtractRemoteProfilesOperation;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportConnectionNodeElement;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportProfileElement;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportProfilesWriter;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportTraceGroupElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.Messages;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageContentProvider;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageFilesElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageTraceElement;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.remote.core.proxy.TmfRemoteConnectionFactory;
import org.eclipse.tracecompass.tmf.ui.dialog.TmfFileDialogFactory;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * Preference page for profiles
 *
 * @author Patrick Tasse
 */
public class RemoteProfilesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    /** The ID of this preference page */
    public static final String ID = "org.eclipse.linuxtools.tmf.remote.ui.preferences.remoteprofiles"; //$NON-NLS-1$

    private static final String REMOTE_PROFILES_XML_FILE_NAME = "remote_profiles.xml"; //$NON-NLS-1$
    private static final String REMOTE_PROFILES_XML_FILE_PATH =
            Activator.getDefault().getStateLocation().addTrailingSeparator().append(REMOTE_PROFILES_XML_FILE_NAME).toOSString();

    private static final String REMOTE_PROFILES_LOCATION_PREF = "REMOTE_PROFILES_LOCATION"; //$NON-NLS-1$
    private static final String REMOTE_PROFILES_LOCATION_DIR_DEF = ""; //$NON-NLS-1$

    private static final String DEFAULT_ROOT_IMPORT_PATH = "/rootpath"; //$NON-NLS-1$
    private static final String DEFAULT_IMPORT_NAME = ""; //$NON-NLS-1$
    private static final String DEFAULT_FILE_PATTERN = ".*"; //$NON-NLS-1$
    private static final String TRACE_TYPE_AUTO_DETECT = Messages.ImportTraceWizard_AutoDetection;
    private static final String SSH_SCHEME = "ssh"; //$NON-NLS-1$

    private TreeViewer fTreeViewer;
    private List<RemoteImportProfileElement> fProfiles;
    private String fSelectedProfileName;
    private DetailsPanel fDetailsPanel;
    private Button fAddButton;
    private Button fRemoveButton;
    private Button fImportButton;
    private Button fExportButton;
    private Button fMoveUpButton;
    private Button fMoveDownButton;
    private Action fDeleteAction;
    private Action fCutAction;
    private Action fCopyAction;
    private Action fPasteAction;

    private static final String PROFILE_FILE_PATH;

    static {
        String profileFilePath = REMOTE_PROFILES_XML_FILE_PATH;

        // Get alternative location under the parent of Activator.getDefault().getStateLocation()
        IEclipsePreferences prefs = DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        String plugin = prefs.get(REMOTE_PROFILES_LOCATION_PREF, REMOTE_PROFILES_LOCATION_DIR_DEF);

        if (!plugin.isEmpty()) {
            // Alternative location
            IPath profileFolderPath = Activator.getDefault().getStateLocation().removeLastSegments(1).append(plugin);
            File profileFolder = profileFolderPath.toFile();
            // Create folder if it doesn't exist
            if (profileFolder.exists() || profileFolder.mkdir()) {
                profileFilePath = profileFolderPath.append(REMOTE_PROFILES_XML_FILE_NAME).toString();
            }
        }
        PROFILE_FILE_PATH = profileFilePath;
    }

    /**
     * Constructor
     */
    public RemoteProfilesPreferencePage() {
        // Do nothing
    }

    @Override
    public void init(IWorkbench workbench) {
        noDefaultAndApplyButton();
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(gd);

        GridLayout gl = new GridLayout(2, false);
        composite.setLayout(gl);

        PatternFilter patternFilter = new PatternFilter() {
            // show all children of matching profiles or profiles with matching connection node
            @Override
            protected boolean isLeafMatch(Viewer viewer, Object element) {
                TreeViewer treeViewer = (TreeViewer) viewer;
                TracePackageContentProvider contentProvider = (TracePackageContentProvider) treeViewer.getContentProvider();
                Object parentElement = element;
                while (!(parentElement instanceof RemoteImportProfileElement)) {
                    parentElement = contentProvider.getParent(parentElement);
                    if (parentElement instanceof TracePackageTraceElement) {
                        // don't show children of trace element
                        return false;
                    }
                }
                RemoteImportProfileElement profile = (RemoteImportProfileElement) parentElement;
                if (super.isLeafMatch(viewer, profile)) {
                    return true;
                }
                for (Object child : contentProvider.getChildren(profile)) {
                    if ((child instanceof RemoteImportConnectionNodeElement) && (super.isLeafMatch(viewer, child))) {
                        return true;
                    }
                }
                return false;
            }
        };

        final FilteredTree filteredTree = new FilteredTree(composite,
                SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, patternFilter, true);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 0;
        filteredTree.setLayoutData(gd);
        final TreeViewer treeViewer = filteredTree.getViewer();
        fTreeViewer = treeViewer;

        treeViewer.setContentProvider(new TracePackageContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof ArrayList) {
                    return ((ArrayList<?>) inputElement).toArray();
                }
                return super.getElements(inputElement);
            }

            @Override
            public boolean hasChildren(Object element) {
                if (element instanceof TracePackageTraceElement) {
                    return false;
                }
                return super.hasChildren(element);
            }
        });

        treeViewer.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof TracePackageTraceElement) {
                    for (TracePackageElement files : ((TracePackageTraceElement) element).getChildren()) {
                        if (files instanceof TracePackageFilesElement) {
                            return ((TracePackageFilesElement) files).getFileName();
                        }
                    }
                } else if (element instanceof TracePackageElement) {
                    return ((TracePackageElement) element).getText();
                }
                return super.getText(element);
            }
            @Override
            public Image getImage(Object element) {
                if (element instanceof TracePackageTraceElement) {
                    for (TracePackageElement files : ((TracePackageTraceElement) element).getChildren()) {
                        return files.getImage();
                    }
                } else if (element instanceof TracePackageElement) {
                    return ((TracePackageElement) element).getImage();
                }
                return super.getImage(element);
            }
        });

        treeViewer.addSelectionChangedListener(event -> {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            TracePackageElement element = (TracePackageElement) (selection.size() == 1 ? selection.getFirstElement() : null);
            fDetailsPanel.refreshDetailsPanel(element);
            enableButtons(selection);
            fSelectedProfileName = null;
            while (element != null) {
                if (element instanceof RemoteImportProfileElement) {
                    fSelectedProfileName = ((RemoteImportProfileElement) element).getProfileName();
                }
                element = element.getParent();
            }
        });

        createGlobalActions();
        createContextMenu();

        fProfiles = readProfiles(PROFILE_FILE_PATH, new NullProgressMonitor());

        treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
        treeViewer.setInput(fProfiles);
        treeViewer.expandAll();

        Composite buttonBar = createVerticalButtonBar(composite);
        gd = new GridData(SWT.CENTER, SWT.BEGINNING, false, false);
        gd.verticalIndent = filteredTree.getFilterControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y + gl.verticalSpacing;
        buttonBar.setLayoutData(gd);
        enableButtons((IStructuredSelection) treeViewer.getSelection());

        Composite details = new Composite(composite, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
        details.setLayoutData(gd);
        gl = new GridLayout(2, false);
        gl.marginWidth = 0;
        details.setLayout(gl);

        Label label = new Label(details, SWT.NONE);
        label.setText(RemoteMessages.RemoteProfilesPreferencePage_DetailsPanelLabel);
        gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1);
        label.setLayoutData(gd);

        fDetailsPanel = new DetailsPanel(details);

        validate();

        for (RemoteImportProfileElement profile : fProfiles) {
            if (profile.getProfileName().equals(fSelectedProfileName)) {
                fTreeViewer.setSelection(new StructuredSelection(profile));
            }
        }

        Dialog.applyDialogFont(composite);
        return composite;
    }

    /**
     * Get the remote profiles stored in the preferences
     *
     * @param monitor
     *            a progress monitor
     *
     * @return the list of remote profiles
     */
    public static List<RemoteImportProfileElement> getRemoteProfiles(IProgressMonitor monitor) {
        return readProfiles(PROFILE_FILE_PATH, monitor);
    }

    private static List<RemoteImportProfileElement> readProfiles(String path, IProgressMonitor monitor) {
        final ExtractRemoteProfilesOperation op = new ExtractRemoteProfilesOperation(path);
        op.run(monitor);
        List<RemoteImportProfileElement> profiles = new ArrayList<>();
        if (!op.getStatus().isOK()) {
            return profiles;
        }
        TracePackageElement[] resultElements = op.getResultElements();
        if (resultElements != null) {
            for (TracePackageElement element : resultElements) {
                if (element instanceof RemoteImportProfileElement) {
                    profiles.add((RemoteImportProfileElement) element);
                }
            }
        }
        return profiles;
    }

    private boolean writeProfiles(List<RemoteImportProfileElement> profiles, String path) {
        try {
            String contents = RemoteImportProfilesWriter.writeProfilesToXML(profiles.toArray(new TracePackageElement[0]));
            File file = new File(path);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(contents);
                return true;
            }
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            MessageDialog.openError(getShell(), RemoteMessages.RemoteProfilesPreferencePage_ErrorWritingProfile, e.getMessage());
        }
        return false;
    }

    private void createGlobalActions() {
        fDeleteAction = new Action(RemoteMessages.RemoteProfilesPreferencePage_DeleteAction) {
            @Override
            public void run() {
                final IStructuredSelection selection = (IStructuredSelection) fTreeViewer.getSelection();
                if (selection.size() == 0) {
                    return;
                }
                for (Object item : selection.toList()) {
                    removeElement(item);
                }
            }
        };
        fDeleteAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
        fDeleteAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_DELETE);
        fDeleteAction.setAccelerator(SWT.DEL);

        fCutAction = new Action(RemoteMessages.RemoteProfilesPreferencePage_CutAction) {
            @Override
            public void run() {
                final IStructuredSelection selection = (IStructuredSelection) fTreeViewer.getSelection();
                if (selection.size() != 1) {
                    return;
                }
                setClipboardContents(selection);
                Object item = selection.getFirstElement();
                removeElement(item);
            }
        };
        fCutAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
        fCutAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_CUT);
        fCutAction.setAccelerator(SWT.CTRL | 'X');

        fCopyAction = new Action(RemoteMessages.RemoteProfilesPreferencePage_CopyAction) {
            @Override
            public void run() {
                final IStructuredSelection selection = (IStructuredSelection) fTreeViewer.getSelection();
                if (selection.size() != 1) {
                    return;
                }
                setClipboardContents(new StructuredSelection(
                        copyElement(null, (TracePackageElement) selection.getFirstElement())));
            }
        };
        fCopyAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
        fCopyAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_COPY);
        fCopyAction.setAccelerator(SWT.CTRL | 'C');

        fPasteAction = new Action(RemoteMessages.RemoteProfilesPreferencePage_PasteAction) {
            @Override
            public void run() {
                final IStructuredSelection selection = (IStructuredSelection) fTreeViewer.getSelection();
                if (selection.size() > 1) {
                    return;
                }
                if (!validatePaste(selection.getFirstElement())) {
                    return;
                }
                IStructuredSelection data = getClipboardContents();
                data = getClipboardContents();
                if (data == null) {
                    return;
                }
                for (Object object : data.toArray()) {
                    if (object instanceof RemoteImportProfileElement) {
                        TracePackageElement element = copyElement(null, (TracePackageElement) object);
                        TracePackageElement target = (TracePackageElement) selection.getFirstElement();
                        if (target == null) {
                            fProfiles.add((RemoteImportProfileElement) element);
                        } else {
                            int index = fProfiles.indexOf(target);
                            fProfiles.add(index + 1, (RemoteImportProfileElement) element);
                        }
                        Object[] expanded = fTreeViewer.getExpandedElements();
                        fTreeViewer.refresh();
                        fTreeViewer.setExpandedElements(expanded);
                        fTreeViewer.expandToLevel(element, AbstractTreeViewer.ALL_LEVELS);
                        fTreeViewer.setSelection(new StructuredSelection(element));
                        validate();
                    } else if (object instanceof TracePackageElement && selection.getFirstElement() instanceof TracePackageElement) {
                        TracePackageElement element = copyElement(null, (TracePackageElement) object);
                        TracePackageElement target = (TracePackageElement) selection.getFirstElement();
                        if (target.getClass().equals(element.getClass())) {
                            int index = target.getParent().indexOf(target);
                            target.getParent().addChild(index + 1, element);
                            fTreeViewer.refresh(target.getParent());
                        } else {
                            target.addChild(0, element);
                            fTreeViewer.refresh(target);
                        }
                        fTreeViewer.expandToLevel(element, AbstractTreeViewer.ALL_LEVELS);
                        fTreeViewer.setSelection(new StructuredSelection(element));
                        validate();
                    }
                }
            }
        };
        fPasteAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
        fPasteAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_PASTE);
        fPasteAction.setAccelerator(SWT.CTRL | 'V');

        fTreeViewer.getTree().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.stateMask == 0 && e.keyCode == SWT.DEL) {
                    fDeleteAction.run();
                }
                if ((e.stateMask & SWT.CTRL) == SWT.CTRL) {
                    if (e.keyCode == 'x') {
                        fCutAction.run();
                    } else if (e.keyCode == 'c') {
                        fCopyAction.run();
                    } else if (e.keyCode == 'v') {
                        fPasteAction.run();
                    }
                }
            }
        });
    }

    private void createContextMenu() {
        MenuManager menuManager = new MenuManager();
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(manager -> fillContextMenu(manager));

        Menu contextMenu = menuManager.createContextMenu(fTreeViewer.getTree());
        fTreeViewer.getTree().setMenu(contextMenu);
    }

    private void fillContextMenu(IMenuManager manager) {
        final IStructuredSelection selection = (IStructuredSelection) fTreeViewer.getSelection();
        final List<Object> items = selection.toList();
        if (items.size() == 1) {
            Object item = items.get(0);
            if (item instanceof RemoteImportProfileElement) {
                final RemoteImportProfileElement profile = (RemoteImportProfileElement) item;
                manager.add(new Action(RemoteMessages.RemoteProfilesPreferencePage_NewConnectionNode) {
                    @Override
                    public void run() {
                        newConnectionNode(profile, null);
                    }
                });
            } else if (item instanceof RemoteImportConnectionNodeElement) {
                final RemoteImportConnectionNodeElement node = (RemoteImportConnectionNodeElement) item;
                manager.add(new Action(RemoteMessages.RemoteProfilesPreferencePage_NewTraceGroupAction) {
                    @Override
                    public void run() {
                        newTraceGroup(node, null);
                    }
                });
            } else if (item instanceof RemoteImportTraceGroupElement) {
                final RemoteImportTraceGroupElement traceGroup = (RemoteImportTraceGroupElement) item;
                manager.add(new Action(RemoteMessages.RemoteProfilesPreferencePage_NewTraceAction) {
                    @Override
                    public void run() {
                        newTrace(traceGroup, null);
                    }
                });
            }
        }
        manager.add(new Separator());
        manager.add(fDeleteAction);
        fDeleteAction.setEnabled(!items.isEmpty());
        manager.add(new Separator());
        manager.add(fCutAction);
        fCutAction.setEnabled(items.size() == 1);
        manager.add(fCopyAction);
        fCopyAction.setEnabled(items.size() == 1);
        manager.add(fPasteAction);
        fPasteAction.setEnabled(items.size() <= 1 && validatePaste(selection.getFirstElement()));
    }

    private Composite createVerticalButtonBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());

        fAddButton = createVerticalButton(composite, RemoteMessages.RemoteProfilesPreferencePage_AddButton);
        fAddButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) fTreeViewer.getSelection();
                if (selection.isEmpty()) {
                    newProfile(null);
                } else if (selection.getFirstElement() instanceof TracePackageElement) {
                    TracePackageElement previous = (TracePackageElement) selection.getFirstElement();
                    if (previous instanceof RemoteImportProfileElement) {
                        newProfile(previous);
                    } else if (previous instanceof RemoteImportConnectionNodeElement) {
                        newConnectionNode(previous.getParent(), previous);
                    } else if (previous instanceof RemoteImportTraceGroupElement) {
                        newTraceGroup(previous.getParent(), previous);
                    } else if (previous instanceof TracePackageTraceElement) {
                        newTrace(previous.getParent(), previous);
                    }
                }
            }
        });

        fRemoveButton = createVerticalButton(composite, RemoteMessages.RemoteProfilesPreferencePage_RemoveButton);
        fRemoveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) fTreeViewer.getSelection();
                for (Object item : selection.toList()) {
                    if (item instanceof RemoteImportProfileElement) {
                        fProfiles.remove(item);
                    } else if (item instanceof TracePackageElement) {
                        TracePackageElement element = (TracePackageElement) item;
                        element.getParent().removeChild(element);
                    }
                }
                fTreeViewer.refresh();
                validate();
            }
        });

        new Label(composite, SWT.NONE);

        fImportButton = createVerticalButton(composite, RemoteMessages.RemoteProfilesPreferencePage_ImportButton);
        fImportButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = TmfFileDialogFactory.create(Display.getCurrent().getActiveShell(), SWT.OPEN);
                dialog.setText(RemoteMessages.RemoteProfilesPreferencePage_ImportFileDialogTitle);
                dialog.setFilterExtensions(new String[] { "*.xml", "*" }); //$NON-NLS-1$ //$NON-NLS-2$
                String path = dialog.open();
                if (path != null) {
                    List<RemoteImportProfileElement> profiles = readProfiles(path, new NullProgressMonitor());
                    fProfiles.addAll(profiles);
                    fTreeViewer.refresh();
                    for (RemoteImportProfileElement profile : profiles) {
                        fTreeViewer.expandToLevel(profile, AbstractTreeViewer.ALL_LEVELS);
                    }
                    fTreeViewer.setSelection(new StructuredSelection(profiles));
                    validate();
                }
            }
        });

        fExportButton = createVerticalButton(composite, RemoteMessages.RemoteProfilesPreferencePage_ExportButton);
        fExportButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = TmfFileDialogFactory.create(Display.getCurrent().getActiveShell(), SWT.SAVE);
                dialog.setText(RemoteMessages.RemoteProfilesPreferencePage_ExportFileDialogTitle);
                dialog.setFilterExtensions(new String[] { "*.xml", "*" }); //$NON-NLS-1$ //$NON-NLS-2$
                String path = dialog.open();
                if (path != null) {
                    IStructuredSelection selection = (IStructuredSelection) fTreeViewer.getSelection();
                    List<RemoteImportProfileElement> profiles = new ArrayList<>();
                    for (Object element : selection.toList()) {
                        if (element instanceof RemoteImportProfileElement) {
                            profiles.add((RemoteImportProfileElement) element);
                        }
                    }
                    writeProfiles(profiles, path);
                }
            }
        });

        new Label(composite, SWT.NONE);

        fMoveUpButton = createVerticalButton(composite, RemoteMessages.RemoteProfilesPreferencePage_MoveUpButton);
        fMoveUpButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) fTreeViewer.getSelection();
                Object element = selection.getFirstElement();
                if (element instanceof RemoteImportProfileElement) {
                    int index = fProfiles.indexOf(element);
                    if (index > 0) {
                        RemoteImportProfileElement profile = fProfiles.remove(index);
                        fProfiles.add(index - 1, profile);
                        Object[] expanded = fTreeViewer.getExpandedElements();
                        fTreeViewer.refresh();
                        fTreeViewer.setExpandedElements(expanded);
                        enableButtons(selection);
                    }
                } else if (element instanceof TracePackageElement) {
                    TracePackageElement child = (TracePackageElement) element;
                    TracePackageElement parentElement = child.getParent();
                    int index = parentElement.indexOf(child);
                    if (index > 0) {
                        parentElement.removeChild(child);
                        parentElement.addChild(index - 1, child);
                        Object[] expanded = fTreeViewer.getExpandedElements();
                        fTreeViewer.refresh(parentElement);
                        fTreeViewer.setExpandedElements(expanded);
                        enableButtons(selection);
                    }
                }
            }
        });

        fMoveDownButton = createVerticalButton(composite, RemoteMessages.RemoteProfilesPreferencePage_MoveDownButton);
        fMoveDownButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) fTreeViewer.getSelection();
                Object element = selection.getFirstElement();
                if (element instanceof RemoteImportProfileElement) {
                    int index = fProfiles.indexOf(selection.getFirstElement());
                    if (index >= 0 && index < fProfiles.size() - 1) {
                        RemoteImportProfileElement profile = fProfiles.remove(index);
                        fProfiles.add(index + 1, profile);
                        Object[] expanded = fTreeViewer.getExpandedElements();
                        fTreeViewer.refresh();
                        fTreeViewer.setExpandedElements(expanded);
                        enableButtons(selection);
                    }
                } else if (element instanceof TracePackageElement) {
                    TracePackageElement child = (TracePackageElement) element;
                    TracePackageElement parentElement = child.getParent();
                    int index = parentElement.indexOf(child);
                    if (index >= 0 && index < parentElement.getChildren().length - 1) {
                        parentElement.removeChild(child);
                        parentElement.addChild(index + 1, child);
                        Object[] expanded = fTreeViewer.getExpandedElements();
                        fTreeViewer.refresh(parentElement);
                        fTreeViewer.setExpandedElements(expanded);
                        enableButtons(selection);
                    }
                }
            }
        });

        return composite;
    }

    private static Button createVerticalButton(Composite parent, String text) {
        Button button = new Button(parent, SWT.PUSH);
        button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        button.setText(text);
        return button;
    }

    private void enableButtons(IStructuredSelection selection) {
        boolean allProfiles = false;
        for (Object element : selection.toList()) {
            if (element instanceof RemoteImportProfileElement) {
                allProfiles = true;
            } else {
                allProfiles = false;
                break;
            }
        }
        fAddButton.setEnabled(selection.size() <= 1);
        fRemoveButton.setEnabled(!selection.isEmpty());
        fExportButton.setEnabled(allProfiles);
        int index = 0;
        int length = 0;
        if (selection.size() == 1) {
            Object item = selection.getFirstElement();
            if (item instanceof RemoteImportProfileElement) {
                index = fProfiles.indexOf(item);
                length = fProfiles.size();
            } else if (item instanceof TracePackageElement) {
                TracePackageElement element = (TracePackageElement) item;
                TracePackageElement parent = element.getParent();
                index = parent.indexOf(element);
                length = parent.getChildren().length;
            }
        }
        fMoveUpButton.setEnabled(index > 0);
        fMoveDownButton.setEnabled(index < length - 1);
    }

    private class DetailsPanel {

        private Composite fComposite;

        public DetailsPanel(Composite parent) {
            fComposite = new Composite(parent, SWT.BORDER);
            GridLayout gl = new GridLayout(2, false);
            fComposite.setLayout(gl);
            GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
            Combo combo = new Combo(fComposite, SWT.BORDER);
            combo.setText("*"); //$NON-NLS-1$
            gd.heightHint = 2 * gl.marginHeight + gl.verticalSpacing + 2 * (combo.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
            fComposite.setLayoutData(gd);
            combo.dispose();
        }

        public void refreshDetailsPanel(final TracePackageElement selection) {
            for (Control control : fComposite.getChildren()) {
                control.dispose();
            }

            if (selection instanceof RemoteImportProfileElement) {
                final RemoteImportProfileElement element = (RemoteImportProfileElement) selection;

                Label label = new Label(fComposite, SWT.NONE);
                label.setText(RemoteMessages.RemoteProfilesPreferencePage_ProfileNameLabel);
                final Text profileNameText = new Text(fComposite, SWT.BORDER);
                GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
                profileNameText.setLayoutData(gd);
                profileNameText.setText(element.getProfileName());
                profileNameText.addModifyListener(e -> {
                    element.setProfileName(profileNameText.getText().trim());
                    fTreeViewer.refresh(element, true);
                    validate();
                    fSelectedProfileName = element.getProfileName();
                });

            } else if (selection instanceof RemoteImportConnectionNodeElement) {
                final RemoteImportConnectionNodeElement element = (RemoteImportConnectionNodeElement) selection;

                Label label = new Label(fComposite, SWT.NONE);
                label.setText(RemoteMessages.RemoteProfilesPreferencePage_ConnectionNodeNameLabel);
                final Text nameText = new Text(fComposite, SWT.BORDER);
                nameText.setText(element.getName());
                GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
                nameText.setLayoutData(gd);
                nameText.addModifyListener(e -> {
                    element.setName(nameText.getText().trim());
                    fTreeViewer.refresh(element, true);
                    validate();
                });

                label = new Label(fComposite, SWT.NONE);
                label.setText(RemoteMessages.RemoteProfilesPreferencePage_ConnectionNodeURILabel);
                final Text uriText = new Text(fComposite, SWT.BORDER);
                uriText.setText(element.getURI());
                gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
                uriText.setLayoutData(gd);
                uriText.addModifyListener(e -> {
                    element.setURI(uriText.getText().trim());
                    fTreeViewer.refresh(element, true);
                    validate();
                });

            } else if (selection instanceof RemoteImportTraceGroupElement) {
                final RemoteImportTraceGroupElement element = (RemoteImportTraceGroupElement) selection;

                Label label = new Label(fComposite, SWT.NONE);
                label.setText(RemoteMessages.RemoteProfilesPreferencePage_RootPathLabel);
                final Text rootText = new Text(fComposite, SWT.BORDER);
                GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
                rootText.setLayoutData(gd);
                rootText.setText(element.getRootImportPath());
                rootText.addModifyListener(e -> {
                    element.setRootImportPath(rootText.getText().trim());
                    fTreeViewer.refresh(element, true);
                    validate();
                });

                // create label for alignment
                new Label(fComposite, SWT.NONE);
                final Button recursiveButton = new Button(fComposite, SWT.CHECK);
                gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
                recursiveButton.setLayoutData(gd);
                recursiveButton.setText(RemoteMessages.RemoteProfilesPreferencePage_RecursiveButton);
                recursiveButton.setSelection(element.isRecursive());
                recursiveButton.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        element.setRecursive(recursiveButton.getSelection());
                        fTreeViewer.refresh(element, true);
                    }
                });

            } else if (selection instanceof TracePackageTraceElement) {
                final TracePackageTraceElement element = (TracePackageTraceElement) selection;

                Label label = new Label(fComposite, SWT.NONE);
                label.setText(RemoteMessages.RemoteProfilesPreferencePage_FilePatternLabel);
                final Text fileNameText = new Text(fComposite, SWT.BORDER);
                for (TracePackageElement child : element.getChildren()) {
                    if (child instanceof TracePackageFilesElement) {
                        TracePackageFilesElement files = (TracePackageFilesElement) child;
                        fileNameText.setText(files.getFileName());
                    }
                }
                GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
                fileNameText.setLayoutData(gd);
                fileNameText.addModifyListener(e -> {
                    for (TracePackageElement child : element.getChildren()) {
                        if (child instanceof TracePackageFilesElement) {
                            TracePackageFilesElement files = (TracePackageFilesElement) child;
                            files.setFileName(fileNameText.getText().trim());
                        }
                    }
                    fTreeViewer.refresh(element, true);
                    validate();
                });

                label = new Label(fComposite, SWT.NONE);
                label.setText(RemoteMessages.RemoteProfilesPreferencePage_TraceTypeLabel);
                final Combo combo = new Combo(fComposite, SWT.BORDER | SWT.READ_ONLY);
                String[] availableTraceTypes = TmfTraceType.getAvailableTraceTypes();
                String[] traceTypeList = new String[availableTraceTypes.length + 1];
                traceTypeList[0] = TRACE_TYPE_AUTO_DETECT;
                System.arraycopy(availableTraceTypes, 0, traceTypeList, 1, availableTraceTypes.length);
                combo.setItems(traceTypeList);
                combo.select(0);
                for (int i = 1; i < traceTypeList.length; i++) {
                    String traceType = traceTypeList[i];
                    String traceTypeId = TmfTraceType.getTraceTypeId(traceType);
                    if (traceTypeId.equals(element.getTraceType())) {
                        combo.select(i);
                        break;
                    }
                }
                gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
                combo.setLayoutData(gd);
                combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (combo.getSelectionIndex() == 0) {
                            element.setTraceType(null);
                        } else {
                            String traceType = combo.getText();
                            String traceTypeId = TmfTraceType.getTraceTypeId(traceType);
                            element.setTraceType(traceTypeId);
                        }
                    }
                });

            }

            fComposite.layout();
        }
    }

    private RemoteImportProfileElement newProfile(TracePackageElement previous) {
        RemoteImportProfileElement profile = new RemoteImportProfileElement(null, RemoteMessages.RemoteProfilesPreferencePage_DefaultProfileName);
        RemoteImportConnectionNodeElement node = new RemoteImportConnectionNodeElement(profile, RemoteMessages.RemoteProfilesPreferencePage_DefaultConnectionNodeName, RemoteMessages.RemoteProfilesPreferencePage_DefaultConnectionNodeURI);
        RemoteImportTraceGroupElement traceGroup = new RemoteImportTraceGroupElement(node, DEFAULT_ROOT_IMPORT_PATH);
        TracePackageTraceElement trace = new TracePackageTraceElement(traceGroup, DEFAULT_IMPORT_NAME, null);
        new TracePackageFilesElement(trace, DEFAULT_FILE_PATTERN);
        int index = previous == null ? fProfiles.size() : fProfiles.indexOf(previous) + 1;
        fProfiles.add(index, profile);
        newElementAdded(profile);
        return profile;
    }

    private RemoteImportConnectionNodeElement newConnectionNode(TracePackageElement parent, TracePackageElement previous) {
        RemoteImportConnectionNodeElement node = new RemoteImportConnectionNodeElement(null, RemoteMessages.RemoteProfilesPreferencePage_DefaultConnectionNodeName, RemoteMessages.RemoteProfilesPreferencePage_DefaultConnectionNodeURI);
        RemoteImportTraceGroupElement traceGroup = new RemoteImportTraceGroupElement(node, DEFAULT_ROOT_IMPORT_PATH);
        TracePackageTraceElement trace = new TracePackageTraceElement(traceGroup, DEFAULT_IMPORT_NAME, null);
        new TracePackageFilesElement(trace, DEFAULT_FILE_PATTERN);
        int index = previous == null ? parent.getChildren().length : parent.indexOf(previous) + 1;
        parent.addChild(index, node);
        newElementAdded(node);
        return node;
    }

    private RemoteImportTraceGroupElement newTraceGroup(TracePackageElement parent, TracePackageElement previous) {
        RemoteImportTraceGroupElement traceGroup = new RemoteImportTraceGroupElement(null, DEFAULT_ROOT_IMPORT_PATH);
        TracePackageTraceElement trace = new TracePackageTraceElement(traceGroup, DEFAULT_IMPORT_NAME, null);
        new TracePackageFilesElement(trace, DEFAULT_FILE_PATTERN);
        int index = previous == null ? parent.getChildren().length : parent.indexOf(previous) + 1;
        parent.addChild(index, traceGroup);
        newElementAdded(traceGroup);
        return traceGroup;
    }

    private TracePackageTraceElement newTrace(TracePackageElement parent, TracePackageElement previous) {
        TracePackageTraceElement trace = new TracePackageTraceElement(null, DEFAULT_IMPORT_NAME, null);
        new TracePackageFilesElement(trace, DEFAULT_FILE_PATTERN);
        int index = previous == null ? parent.getChildren().length : parent.indexOf(previous) + 1;
        parent.addChild(index, trace);
        newElementAdded(trace);
        return trace;
    }

    private void newElementAdded(TracePackageElement element) {
        if (element.getParent() != null) {
            fTreeViewer.refresh(element.getParent());
        } else {
            fTreeViewer.refresh();
        }
        fTreeViewer.expandToLevel(element, AbstractTreeViewer.ALL_LEVELS);
        fTreeViewer.setSelection(new StructuredSelection(element));
        validate();
    }

    private void removeElement(Object item) {
        if (item instanceof RemoteImportProfileElement) {
            fProfiles.remove(item);
            fTreeViewer.refresh();
            validate();
        } else if (item instanceof TracePackageElement) {
            TracePackageElement element = (TracePackageElement) item;
            TracePackageElement parent = element.getParent();
            parent.removeChild(element);
            fTreeViewer.refresh(parent);
            validate();
        }
    }

    private TracePackageElement copyElement(TracePackageElement parent, TracePackageElement element) {
        TracePackageElement copy = null;
        if (element instanceof RemoteImportProfileElement) {
            RemoteImportProfileElement source = (RemoteImportProfileElement) element;
            copy = new RemoteImportProfileElement(parent, source.getProfileName());
        } else if (element instanceof RemoteImportConnectionNodeElement) {
            RemoteImportConnectionNodeElement source = (RemoteImportConnectionNodeElement) element;
            copy = new RemoteImportConnectionNodeElement(parent, source.getName(), source.getURI());
        } else if (element instanceof RemoteImportTraceGroupElement) {
            RemoteImportTraceGroupElement source = (RemoteImportTraceGroupElement) element;
            copy = new RemoteImportTraceGroupElement(parent, source.getRootImportPath());
            ((RemoteImportTraceGroupElement) copy).setRecursive(source.isRecursive());
        } else if (element instanceof TracePackageTraceElement) {
            TracePackageTraceElement source = (TracePackageTraceElement) element;
            copy = new TracePackageTraceElement(parent, source.getImportName(), source.getTraceType());
        } else if (element instanceof TracePackageFilesElement) {
            TracePackageFilesElement source = (TracePackageFilesElement) element;
            copy = new TracePackageFilesElement(parent, source.getFileName());
        }
        for (TracePackageElement child : element.getChildren()) {
            copyElement(copy, child);
        }
        return copy;
    }

    private static boolean validatePaste(Object target) {
        IStructuredSelection data = getClipboardContents();
        if (data == null || data.isEmpty()) {
            return false;
        }
        for (Object item : data.toArray()) {
            if (item instanceof RemoteImportConnectionNodeElement) {
                if (!(target instanceof RemoteImportConnectionNodeElement ||
                        target instanceof RemoteImportProfileElement)) {
                    return false;
                }
            } else if (item instanceof RemoteImportTraceGroupElement) {
                if (!(target instanceof RemoteImportTraceGroupElement ||
                        target instanceof RemoteImportConnectionNodeElement)) {
                    return false;
                }
            } else if (item instanceof TracePackageTraceElement) {
                if (!(target instanceof TracePackageTraceElement ||
                        target instanceof RemoteImportTraceGroupElement)) {
                    return false;
                }
            } else if (item instanceof RemoteImportProfileElement) {
                if (!(target instanceof RemoteImportProfileElement ||
                        target == null)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private static void setClipboardContents(IStructuredSelection data) {
        LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
        transfer.setSelection(data);
        Clipboard clipboard = new Clipboard(PlatformUI.getWorkbench().getDisplay());
        clipboard.setContents(new Object[] { new Object() }, new Transfer[] { transfer });
        clipboard.dispose();
    }

    private static IStructuredSelection getClipboardContents() {
        Clipboard clipboard = new Clipboard(PlatformUI.getWorkbench().getDisplay());
        IStructuredSelection data = (IStructuredSelection) clipboard.getContents(LocalSelectionTransfer.getTransfer());
        clipboard.dispose();
        return data;
    }

    private void validate() {
        setValid(false);
        Set<String> profileNames = new HashSet<>();
        for (RemoteImportProfileElement profile : fProfiles) {
            if (profile.getProfileName().length() == 0) {
                setErrorMessage(RemoteMessages.RemoteProfilesPreferencePage_EmptyProfileNameError);
                return;
            }
            String prefix = profile.getProfileName() + ": "; //$NON-NLS-1$
            if (!profileNames.add(profile.getProfileName())) {
                setErrorMessage(prefix + RemoteMessages.RemoteProfilesPreferencePage_DuplicateProfileNameError);
                return;
            }
            Set<String> nodeNames = new HashSet<>();
            int nodeCount = 0;
            for (TracePackageElement profileChild : profile.getChildren()) {
                if (profileChild instanceof RemoteImportConnectionNodeElement) {
                    nodeCount++;
                    RemoteImportConnectionNodeElement node = (RemoteImportConnectionNodeElement) profileChild;
                    if (node.getName().length() == 0) {
                        setErrorMessage(prefix + RemoteMessages.RemoteProfilesPreferencePage_EmptyNodeNameError);
                        return;
                    }
                    // validate node name against the OS since the name will be used as folder name at the destination
                    if (!ResourcesPlugin.getWorkspace().validateName(node.getName(), IResource.FOLDER).isOK()) {
                        setErrorMessage(prefix + RemoteMessages.RemoteProfilesPreferencePage_InvalidNodeName);
                        return;
                    }
                    if (!nodeNames.add(node.getName())) {
                        setErrorMessage(prefix + RemoteMessages.RemoteProfilesPreferencePage_DuplicateConnectionNodeNameError);
                        return;
                    }

                    if (node.getURI().length() == 0) {
                        setErrorMessage(prefix + RemoteMessages.RemoteProfilesPreferencePage_EmptyNodeURIError);
                        return;
                    }
                    try {
                        URI uri = URIUtil.fromString(node.getURI());
                        IRemoteServicesManager manager = TmfRemoteConnectionFactory.getService(IRemoteServicesManager.class);
                        if (manager == null || manager.getConnectionType(uri) == null) {
                            setErrorMessage(prefix + RemoteMessages.RemoteProfilesPreferencePage_UnsupportedURISchemeError);
                            return;
                        }
                        if (uri.getScheme().equals(SSH_SCHEME)) {
                            if (uri.getHost() == null) {
                                setErrorMessage(prefix + RemoteMessages.RemoteProfilesPreferencePage_InvalidHostOrPortError);
                                return;
                            }
                            if (uri.getUserInfo() == null) {
                                setErrorMessage(prefix + RemoteMessages.RemoteProfilesPreferencePage_MissingUserInfoError);
                                return;
                            }
                        }
                    } catch (URISyntaxException e) {
                        setErrorMessage(prefix + RemoteMessages.RemoteProfilesPreferencePage_InvalidNodeURIError);
                        return;
                    }
                    int traceGroupCount = 0;
                    for (TracePackageElement nodeChild : node.getChildren()) {
                        if (nodeChild instanceof RemoteImportTraceGroupElement) {
                            traceGroupCount++;
                            RemoteImportTraceGroupElement traceGroup = (RemoteImportTraceGroupElement) nodeChild;
                            if (traceGroup.getRootImportPath().length() == 0) {
                                setErrorMessage(prefix + RemoteMessages.RemoteProfilesPreferencePage_EmptyRootPathError);
                                return;
                            }
                            int traceCount = 0;
                            for (TracePackageElement traceGroupChild : traceGroup.getChildren()) {
                                if (traceGroupChild instanceof TracePackageTraceElement) {
                                    traceCount++;
                                    TracePackageTraceElement trace = (TracePackageTraceElement) traceGroupChild;

                                    for (TracePackageElement traceChild : trace.getChildren()) {
                                        if (traceChild instanceof TracePackageFilesElement) {
                                            TracePackageFilesElement files = (TracePackageFilesElement) traceChild;
                                            if (files.getFileName().length() == 0) {
                                                setErrorMessage(prefix + RemoteMessages.RemoteProfilesPreferencePage_EmptyFilePatternError);
                                                return;
                                            }
                                            try {
                                                Pattern.compile(files.getFileName());
                                            } catch (PatternSyntaxException e) {
                                                setErrorMessage(prefix + RemoteMessages.RemoteProfilesPreferencePage_InvalidFilePatternError);
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                            if (traceCount == 0) {
                                setErrorMessage(prefix + RemoteMessages.RemoteProfilesPreferencePage_MissingTraceError);
                                return;
                            }
                        }
                    }
                    if (traceGroupCount == 0) {
                        setErrorMessage(prefix + RemoteMessages.RemoteProfilesPreferencePage_MissingTraceGroupError);
                        return;
                    }
                }
            }
            if (nodeCount == 0) {
                setErrorMessage(prefix + RemoteMessages.RemoteProfilesPreferencePage_MissingConnectionNodeError);
                return;
            }
        }
        setValid(true);
        setErrorMessage(null);
    }

    @Override
    public boolean performOk() {
        return writeProfiles(fProfiles, PROFILE_FILE_PATH);
    }

    /**
     * Set the selected profile name
     *
     * @param profileName the selected profile name
     */
    public void setSelectedProfile(String profileName) {
        fSelectedProfileName = profileName;
        if (fTreeViewer != null && !fTreeViewer.getTree().isDisposed()) {
            for (RemoteImportProfileElement profile : fProfiles) {
                if (profile.getProfileName().equals(profileName)) {
                    fTreeViewer.setSelection(new StructuredSelection(profile));
                }
            }
        }
    }

    /**
     * Return the selected profile name
     *
     * @return the selected profile name or null
     */
    public String getSelectedProfile() {
        return fSelectedProfileName;
    }

}
