/**********************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick-Jeffrey Pollo Guilbert, William Enright,
 *      William Tri-Khiem Truong - Initial API and implementation
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * <p>
 * Dialog box using profiles
 * </p>
 *
 * @author William Enright, Patrick-Jeffrey Pollo Guilbert, William Tri-Khiem
 *         Truong
 *
 *
 */
public class ProfileDialog extends TitleAreaDialog {

    private Composite fBase;
    private Composite fLabel;
    private CheckboxTreeViewer fCbtv;
    private Text descriptionText;
    private ArrayList<File> fSessionFiles = new ArrayList<>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param parentShell
     *            - a shell for the display of the dialog
     */
    public ProfileDialog(Shell parentShell) {
        super(parentShell);

    }

    @Override
    protected Control createDialogArea(Composite parent) {

        setTitle(Messages.TraceControl_BasicMode);
        setMessage(Messages.TraceControl_SelectBasicProfile);

        Composite parentComp = (Composite) super.createDialogArea(parent);

        fBase = new Composite(parentComp, SWT.NONE);

        // Creating composite for label group
        fLabel = new Composite(parentComp, SWT.NONE);
        fLabel.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).create());
        fLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        descriptionText = new Text(fLabel, SWT.READ_ONLY | SWT.BORDER | SWT.MULTI);
        descriptionText.setText(Messages.TraceControl_NoProfileSelected + "\n\n\n\n\n"); //$NON-NLS-1$
        descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        descriptionText.setBackground(new Color(null, 255, 255, 255));
        fBase.addHelpListener(new HelpListener() {

            @Override
            public void helpRequested(HelpEvent e) {
                try {
                    PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL("http://archive.eclipse.org/tracecompass/doc/org.eclipse.tracecompass.doc.user/LTTng-Tracer-Control.html#Control_View")); //$NON-NLS-1$
                } catch (PartInitException e1) {

                } catch (MalformedURLException e1) {

                }

            }
        });

        fBase.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).create());
        fBase.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        FilteredTree ft = new FilteredTree(fBase, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new PatternFilter(), true) {
            @Override
            protected TreeViewer doCreateTreeViewer(Composite aparent, int style) {
                fCbtv = new CheckboxTreeViewer(fBase);
                fCbtv.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
                fCbtv.setContentProvider(new BasicContentProvider());
                try {
                    fCbtv.setInput(initialize());
                } catch (IOException e) {

                }
                fCbtv.expandAll();
                return fCbtv;
            }
        };

        ft.setBounds(0, 0, 500, 500);
        fCbtv.addSelectionChangedListener(new ProfileSelectionChangedListener());

        return fBase;
    }

    // Updates text to be displayed in the textbox for profile description
    private void updateSelectionText(IStructuredSelection sel, boolean uncheck)
    {
        Object[] obj = fCbtv.getCheckedElements();
        obj.getClass();

        if (fCbtv.getCheckedElements().length == 0)
        {
            descriptionText.setText(Messages.NoSelectionDescription);
        }
        else if (fCbtv.getCheckedElements().length != 1 && fCbtv.getCheckedElements().length != 0)
        {
            descriptionText.setText(Messages.TraceControl_MultipleSelectionDescription);
        } else {

            TreeParent checkedElement;
            boolean commentFound = false;
            if (uncheck) {
                checkedElement = (TreeParent) obj[0];
            } else {
                checkedElement = (TreeParent) sel.getFirstElement();
            }

            TreeParent currentParent = checkedElement.getParent();
            String parentPath = currentParent.getName();

            if (fCbtv.getChecked(checkedElement))
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                try {
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    factory.setIgnoringComments(false);
                    String fullPath = parentPath + checkedElement.getName();
                    Document document = builder.parse(new File(fullPath));
                    document.getDocumentElement().normalize();
                    NodeList nl = document.getChildNodes();
                    for (int i = 0; i < nl.getLength(); i++) {
                        if (nl.item(i).getNodeType() == Node.COMMENT_NODE) {
                            commentFound = true;
                            Comment comment = (Comment) nl.item(i);
                            descriptionText.setText(comment.getData());
                        }
                    }

                    if (!commentFound) {
                        descriptionText.setText(Messages.TraceControl_SingleSelectionDescription);
                    }
                } catch (ParserConfigurationException e) {

                } catch (SAXException e) {

                } catch (IOException e) {

                }

            }

        }
    }

    private List<TreeParent> addCustomFolders() throws IOException
    {
        String basicPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        basicPath = basicPath + "resources/folders.txt"; //$NON-NLS-1$
        String thisLine = ""; //$NON-NLS-1$
        List<TreeParent> folderList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(basicPath))) {
            while ((thisLine = reader.readLine()) != null) {

                for (String retval : thisLine.split("\n")) { //$NON-NLS-1$
                    if (!retval.equals("")) { //$NON-NLS-1$
                        List<TreeParent> filesList = new ArrayList<>();
                        TreeParent newParent = new TreeParent(thisLine);
                        filesList = addFilesFromFolder(thisLine);

                        for (int i = 0; i < filesList.size(); i++)
                        {
                            newParent.addChild(new TreeParent(filesList.get(i).getName()));
                        }
                        folderList.add(newParent);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.printf(Messages.TraceControl_ReadingPathError, basicPath);
        }
        return folderList;
    }

    private List<TreeParent> addFilesFromFolder(String folderPath)
    {
        List<TreeParent> fileList = new ArrayList<>();
        File dir = new File(folderPath);
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir1, String name) {
                return name.toLowerCase().endsWith(Messages.TraceControl_LttngSuffix);
            }
        });
        if (files == null) {
            files = new File[0];
        }
        for (File f : files)
        {
            fSessionFiles.add(f);
            fileList.add(new TreeParent(f.getName()));
        }

        return fileList;

    }

    private TreeParent initialize() throws IOException {

        String absolutePath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        String defaultPath = absolutePath + "resources/"; //$NON-NLS-1$

        TreeParent root = new TreeParent("Root"); //$NON-NLS-1$

        List<TreeParent> sessionFilesList = new ArrayList<>();
        sessionFilesList = addFilesFromFolder(defaultPath);

        TreeParent defaultTP = new TreeParent(defaultPath);

        for (int i = 0; i < sessionFilesList.size(); i++)
        {
            defaultTP.addChild(sessionFilesList.get(i));
        }

        root.addChild(defaultTP);

        List<TreeParent> customList = new ArrayList<>();
        customList = addCustomFolders();

        for (TreeParent tp : customList)
        {
            root.addChild(tp);
        }

        return root;
    }

    private final class ProfileSelectionChangedListener implements ISelectionChangedListener {
        @Override
        public void selectionChanged(SelectionChangedEvent event) {

            if (!(event.getSelection().isEmpty()) && event.getSelection() instanceof IStructuredSelection)
            {
                IStructuredSelection sel = (IStructuredSelection) event.getSelection();

                updateSelectionText(sel, false);
                Object selectedObject = sel.getFirstElement();
                updateCheckedProfiles(sel, selectedObject);
            }
        }

        private void updateCheckedProfiles(IStructuredSelection sel, Object selectedObject) {
            // Check if all the other brothers are checked, if so, check parent
            // TreeObject is checked
            if (fCbtv.getChecked(selectedObject)) {
                ArrayList<TreeObject> allParents = ((TreeObject) selectedObject).getAllParents();
                for (TreeObject parentAbove : allParents) {
                    Boolean brothersChecked = true;
                    ArrayList<TreeObject> allChildren = ((TreeParent) parentAbove).getAllChildren();
                    for (TreeObject child : allChildren) {
                        // TreeParent is checked
                        if (!fCbtv.getChecked(child)) {
                            brothersChecked = false;
                        }
                    }
                    if (brothersChecked) {
                        fCbtv.setChecked(parentAbove, true);
                    }
                }
            }

            if (selectedObject.getClass() == TreeParent.class)
            {
                // TreeParent is checked, must check all children
                if (fCbtv.getChecked(selectedObject)) {
                    ArrayList<TreeObject> allChildren = ((TreeParent) selectedObject).getAllChildren();
                    for (TreeObject child : allChildren) {
                        fCbtv.setChecked(child, true);
                    }
                    fCbtv.expandToLevel(selectedObject, AbstractTreeViewer.ALL_LEVELS);
                }
                // TreeParent is unchecked, must uncheck children and parent,
                // but not brothers
                else {
                    ArrayList<TreeObject> allChildren = ((TreeParent) selectedObject).getAllChildren();
                    for (TreeObject child : allChildren) {
                        fCbtv.setChecked(child, false);
                        updateSelectionText(sel, true);
                    }
                    ArrayList<TreeObject> allParents = ((TreeObject) selectedObject).getAllParents();
                    for (TreeObject parentAbove : allParents) {
                        fCbtv.setChecked(parentAbove, false);
                        updateSelectionText(sel, true);
                    }

                }
            } else if (selectedObject.getClass() == TreeObject.class) {
                // TreeObject is checked
                if (fCbtv.getChecked(selectedObject)) {

                }
                // TreeObject is unchecked
                else {
                    ArrayList<TreeObject> allParents = ((TreeObject) selectedObject).getAllParents();
                    for (TreeObject parentAbove : allParents) {
                        fCbtv.setChecked(parentAbove, false);
                    }
                }
            }
        }
    }

    class TreeObject extends PlatformObject {
        private String name;
        private TreeParent parent;

        public TreeObject(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setParent(TreeParent parent) {
            this.parent = parent;
        }

        public TreeParent getParent() {
            return parent;
        }

        public ArrayList<TreeObject> getAllParents() {
            ArrayList<TreeObject> both = new ArrayList<>();
            TreeObject parentTemp = getParent();
            if (parentTemp != null) { // is not root, has more parents
                both.add(parentTemp);
                both.addAll(parentTemp.getAllParents());
            }

            return both;
        }

        @Override
        public String toString() {
            return getName();
        }

    }

    class TreeParent extends TreeObject {
        private ArrayList<TreeObject> children;

        public TreeParent(String name) {
            super(name);
            children = new ArrayList<>();
        }

        public void addChild(TreeObject child) {
            children.add(child);
            child.setParent(this);
        }

        public void removeChild(TreeObject child) {
            children.remove(child);
            child.setParent(null);
        }

        public TreeObject[] getChildren() {
            return children.toArray(new TreeObject[children.size()]);
        }

        public ArrayList<TreeObject> getAllChildren()
        {
            ArrayList<TreeObject> both = new ArrayList<>();
            for (Object child : getChildren()) {
                if (child.getClass() == TreeParent.class) { // is a parent
                    both.add((TreeObject) child);
                    both.addAll(((TreeParent) child).getAllChildren());
                } else {
                    both.add((TreeObject) child);
                }
            }
            return both;
        }

        public boolean hasChildren() {
            return children.size() > 0;
        }
    }

    class BasicContentProvider implements ITreeContentProvider {
        private TreeParent invisibleRoot;

        @Override
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public Object[] getElements(Object parent) {
            if (parent.equals(null)) {
                if (invisibleRoot == null) {
                    try {
                        invisibleRoot = initialize();
                    } catch (IOException e) {

                    }
                }
                return getChildren(invisibleRoot);
            }
            return getChildren(parent);
        }

        @Override
        public Object getParent(Object child) {
            if (child instanceof TreeObject) {
                return ((TreeObject) child).getParent();
            }
            return null;
        }

        @Override
        public Object[] getChildren(Object parent) {
            if (parent instanceof TreeParent) {
                return ((TreeParent) parent).getChildren();
            }
            return new Object[0];
        }

        @Override
        public boolean hasChildren(Object parent) {
            if (parent instanceof TreeParent) {
                return ((TreeParent) parent).hasChildren();
            }
            return false;
        }
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void okPressed()
    {
        Object[] checkedElements = fCbtv.getCheckedElements();

        if (checkedElements.length != 0) {
            ArrayList<File> temp = new ArrayList<>();
            for (Object checked : checkedElements) {
                String sessionName = ((TreeParent) checked).toString();
                if (sessionName.endsWith(Messages.ProfileDialog_LTTNG_Suffix)) {

                    for (File f : fSessionFiles) {
                        if (f.getAbsolutePath().endsWith(sessionName)) {
                            temp.add(f);
                        }
                    }
                }
            }
            fSessionFiles = temp;
        }

        super.okPressed();
    }

    /**
     * @return the list of checked files
     */
    public ArrayList<File> getCheckedFiles() {
        return fSessionFiles;
    }

}
