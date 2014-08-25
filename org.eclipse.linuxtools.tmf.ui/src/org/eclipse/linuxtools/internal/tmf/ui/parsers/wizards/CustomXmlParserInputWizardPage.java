/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.parsers.wizards;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTraceDefinition;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlTrace;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlTraceDefinition.InputAttribute;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlTraceDefinition.InputElement;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Input wizard page for custom XML trace parsers.
 *
 * @author Patrick Tasse
 */
public class CustomXmlParserInputWizardPage extends WizardPage {

    private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"; //$NON-NLS-1$
    private static final String TIMESTAMP_FORMAT_BUNDLE = "org.eclipse.linuxtools.lttng.help"; //$NON-NLS-1$
    private static final String TIMESTAMP_FORMAT_PATH = "reference/api/org/eclipse/linuxtools/tmf/core/timestamp/TmfTimestampFormat.html"; //$NON-NLS-1$
    private static final Image ELEMENT_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/element_icon.gif"); //$NON-NLS-1$
    private static final Image ADD_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/add_button.gif"); //$NON-NLS-1$
    private static final Image ADD_NEXT_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/addnext_button.gif"); //$NON-NLS-1$
    private static final Image ADD_CHILD_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/addchild_button.gif"); //$NON-NLS-1$
    private static final Image ADD_MANY_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/addmany_button.gif"); //$NON-NLS-1$
    private static final Image DELETE_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/delete_button.gif"); //$NON-NLS-1$
    private static final Image MOVE_UP_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/moveup_button.gif"); //$NON-NLS-1$
    private static final Image MOVE_DOWN_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/movedown_button.gif"); //$NON-NLS-1$
    private static final Image HELP_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/help_button.gif"); //$NON-NLS-1$
    private static final Color COLOR_LIGHT_RED = new Color(Display.getDefault(), 255, 192, 192);
    private static final Color COLOR_TEXT_BACKGROUND = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
    private static final Color COLOR_WIDGET_BACKGROUND = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

    private final ISelection selection;
    private CustomXmlTraceDefinition definition;
    private String editCategoryName;
    private String editDefinitionName;
    private String defaultDescription;
    private ElementNode selectedElement;
    private Composite container;
    private Text categoryText;
    private Text logtypeText;
    private Text timeStampOutputFormatText;
    private Text timeStampPreviewText;
    private Button removeButton;
    private Button addChildButton;
    private Button addNextButton;
    private Button moveUpButton;
    private Button moveDownButton;
    private ScrolledComposite elementScrolledComposite;
    private TreeViewer treeViewer;
    private Composite elementContainer;
    private Text errorText;
    private StyledText inputText;
    private Font fixedFont;
    private UpdateListener updateListener;
    private Browser helpBrowser;
    private Element documentElement;

    // variables used recursively through element traversal
    private String timeStampValue;
    private String timeStampFormat;
    private boolean timeStampFound;
    private int logEntriesCount;
    private boolean logEntryFound;

    /**
     * Constructor
     *
     * @param selection
     *            Selection object
     * @param definition
     *            Trace definition
     */
    protected CustomXmlParserInputWizardPage(ISelection selection, CustomXmlTraceDefinition definition) {
        super("CustomXmlParserWizardPage"); //$NON-NLS-1$
        if (definition == null) {
            setTitle(Messages.CustomXmlParserInputWizardPage_titleNew);
            defaultDescription = Messages.CustomXmlParserInputWizardPage_descriptionNew;
        } else {
            setTitle(Messages.CustomXmlParserInputWizardPage_titleEdit);
            defaultDescription = Messages.CustomXmlParserInputWizardPage_descriptionEdit;
        }
        setDescription(defaultDescription);
        this.selection = selection;
        this.definition = definition;
        if (definition != null) {
            this.editCategoryName = definition.categoryName;
            this.editDefinitionName = definition.definitionName;
        }
    }

    @Override
    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout());

        updateListener = new UpdateListener();

        Composite headerComposite = new Composite(container, SWT.FILL);
        GridLayout headerLayout = new GridLayout(5, false);
        headerLayout.marginHeight = 0;
        headerLayout.marginWidth = 0;
        headerComposite.setLayout(headerLayout);
        headerComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label categoryLabel = new Label(headerComposite, SWT.NULL);
        categoryLabel.setText(Messages.CustomXmlParserInputWizardPage_category);

        categoryText = new Text(headerComposite, SWT.BORDER | SWT.SINGLE);
        categoryText.setLayoutData(new GridData(120, SWT.DEFAULT));

        Label timeStampFormatLabel = new Label(headerComposite, SWT.NULL);
        timeStampFormatLabel.setText(Messages.CustomXmlParserInputWizardPage_timestampFormat);

        timeStampOutputFormatText = new Text(headerComposite, SWT.BORDER | SWT.SINGLE);
        timeStampOutputFormatText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        timeStampOutputFormatText.setText(DEFAULT_TIMESTAMP_FORMAT);

        Button timeStampFormatHelpButton = new Button(headerComposite, SWT.PUSH);
        timeStampFormatHelpButton.setImage(HELP_IMAGE);
        timeStampFormatHelpButton.setToolTipText(Messages.CustomXmlParserInputWizardPage_timestampFormatHelp);
        timeStampFormatHelpButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Bundle plugin = Platform.getBundle(TIMESTAMP_FORMAT_BUNDLE);
                IPath path = new Path(TIMESTAMP_FORMAT_PATH);
                URL fileURL = FileLocator.find(plugin, path, null);
                try {
                    URL pageURL = FileLocator.toFileURL(fileURL);
                    openHelpShell(pageURL.toString());
                } catch (IOException e1) {
                }
            }
        });

        Label logtypeLabel = new Label(headerComposite, SWT.NULL);
        logtypeLabel.setText(Messages.CustomXmlParserInputWizardPage_logType);

        logtypeText = new Text(headerComposite, SWT.BORDER | SWT.SINGLE);
        logtypeText.setLayoutData(new GridData(120, SWT.DEFAULT));
        logtypeText.setFocus();

        Label timeStampPreviewLabel = new Label(headerComposite, SWT.NULL);
        timeStampPreviewLabel.setText(Messages.CustomXmlParserInputWizardPage_preview);

        timeStampPreviewText = new Text(headerComposite, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
        timeStampPreviewText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        timeStampPreviewText.setText("*no time stamp element or attribute*"); //$NON-NLS-1$

        createButtonBar();

        SashForm vSash = new SashForm(container, SWT.VERTICAL);
        vSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        vSash.setBackground(vSash.getDisplay().getSystemColor(SWT.COLOR_GRAY));

        SashForm hSash = new SashForm(vSash, SWT.HORIZONTAL);
        hSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        ScrolledComposite treeScrolledComposite = new ScrolledComposite(hSash, SWT.V_SCROLL | SWT.H_SCROLL);
        treeScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Composite treeContainer = new Composite(treeScrolledComposite, SWT.NONE);
        treeContainer.setLayout(new FillLayout());
        treeScrolledComposite.setContent(treeContainer);
        treeScrolledComposite.setExpandHorizontal(true);
        treeScrolledComposite.setExpandVertical(true);

        treeViewer = new TreeViewer(treeContainer, SWT.SINGLE | SWT.BORDER);
        treeViewer.setContentProvider(new InputElementTreeNodeContentProvider());
        treeViewer.setLabelProvider(new InputElementTreeLabelProvider());
        treeViewer.addSelectionChangedListener(new InputElementTreeSelectionChangedListener());
        treeContainer.layout();

        treeScrolledComposite
                .setMinSize(treeContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, treeContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);

        elementScrolledComposite = new ScrolledComposite(hSash, SWT.V_SCROLL);
        elementScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        elementContainer = new Composite(elementScrolledComposite, SWT.NONE);
        GridLayout gl = new GridLayout();
        gl.marginHeight = 1;
        gl.marginWidth = 0;
        elementContainer.setLayout(gl);
        elementScrolledComposite.setContent(elementContainer);
        elementScrolledComposite.setExpandHorizontal(true);
        elementScrolledComposite.setExpandVertical(true);

        if (definition == null) {
            definition = new CustomXmlTraceDefinition();
        }
        loadDefinition(definition);
        treeViewer.expandAll();
        elementContainer.layout();

        categoryText.addModifyListener(updateListener);
        logtypeText.addModifyListener(updateListener);
        timeStampOutputFormatText.addModifyListener(updateListener);

        elementScrolledComposite.setMinSize(elementContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x,
                elementContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y - 1);

        hSash.setWeights(new int[] { 1, 2 });

        if (definition.rootInputElement == null) {
            removeButton.setEnabled(false);
            addChildButton.setToolTipText(Messages.CustomXmlParserInputWizardPage_addDocumentElement);
            addNextButton.setEnabled(false);
            moveUpButton.setEnabled(false);
            moveDownButton.setEnabled(false);
        } else { // root is selected
            addNextButton.setEnabled(false);
        }

        Composite sashBottom = new Composite(vSash, SWT.NONE);
        GridLayout sashBottomLayout = new GridLayout(2, false);
        sashBottomLayout.marginHeight = 0;
        sashBottomLayout.marginWidth = 0;
        sashBottom.setLayout(sashBottomLayout);

        Label previewLabel = new Label(sashBottom, SWT.NULL);
        previewLabel.setText(Messages.CustomXmlParserInputWizardPage_previewInput);

        errorText = new Text(sashBottom, SWT.SINGLE | SWT.READ_ONLY);
        errorText.setBackground(COLOR_WIDGET_BACKGROUND);
        errorText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        errorText.setVisible(false);

        inputText = new StyledText(sashBottom, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        if (fixedFont == null) {
            if (System.getProperty("os.name").contains("Windows")) { //$NON-NLS-1$ //$NON-NLS-2$
                fixedFont = new Font(Display.getCurrent(), new FontData("Courier New", 10, SWT.NORMAL)); //$NON-NLS-1$
            } else {
                fixedFont = new Font(Display.getCurrent(), new FontData("Monospace", 10, SWT.NORMAL)); //$NON-NLS-1$
            }
        }
        inputText.setFont(fixedFont);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        gd.heightHint = inputText.computeSize(SWT.DEFAULT, inputText.getLineHeight() * 4).y;
        gd.widthHint = 800;
        inputText.setLayoutData(gd);
        inputText.setText(getSelectionText());
        inputText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                parseXmlInput(inputText.getText());
            }
        });
        inputText.addModifyListener(updateListener);

        vSash.setWeights(new int[] { hSash.computeSize(SWT.DEFAULT, SWT.DEFAULT).y, sashBottom.computeSize(SWT.DEFAULT, SWT.DEFAULT).y });

        setControl(container);
    }

    private void createButtonBar() {
        Composite buttonBar = new Composite(container, SWT.NONE);
        GridLayout buttonBarLayout = new GridLayout(6, false);
        buttonBarLayout.marginHeight = 0;
        buttonBarLayout.marginWidth = 0;
        buttonBar.setLayout(buttonBarLayout);

        removeButton = new Button(buttonBar, SWT.PUSH);
        removeButton.setImage(DELETE_IMAGE);
        removeButton.setToolTipText(Messages.CustomXmlParserInputWizardPage_removeElement);
        removeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (treeViewer.getSelection().isEmpty() || selectedElement == null) {
                    return;
                }
                removeElement();
                InputElement inputElement = (InputElement) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                if (inputElement == definition.rootInputElement) {
                    definition.rootInputElement = null;
                } else {
                    inputElement.parentElement.childElements.remove(inputElement);
                }
                treeViewer.refresh();
                validate();
                updatePreviews();
                removeButton.setEnabled(false);
                if (definition.rootInputElement == null) {
                    addChildButton.setEnabled(true);
                    addChildButton.setToolTipText(Messages.CustomXmlParserInputWizardPage_addDocumentEleemnt);
                } else {
                    addChildButton.setEnabled(false);
                }
                addNextButton.setEnabled(false);
                moveUpButton.setEnabled(false);
                moveDownButton.setEnabled(false);
            }
        });

        addChildButton = new Button(buttonBar, SWT.PUSH);
        addChildButton.setImage(ADD_CHILD_IMAGE);
        addChildButton.setToolTipText(Messages.CustomXmlParserInputWizardPage_addChildElement);
        addChildButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                InputElement inputElement = new InputElement("", false, CustomXmlTraceDefinition.TAG_IGNORE, 0, "", null); //$NON-NLS-1$ //$NON-NLS-2$
                if (definition.rootInputElement == null) {
                    definition.rootInputElement = inputElement;
                    inputElement.elementName = getChildNameSuggestion(null);
                } else if (treeViewer.getSelection().isEmpty()) {
                    return;
                } else {
                    InputElement parentInputElement = (InputElement) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                    parentInputElement.addChild(inputElement);
                    inputElement.elementName = getChildNameSuggestion(parentInputElement);
                }
                treeViewer.refresh();
                treeViewer.setSelection(new StructuredSelection(inputElement), true);
            }
        });

        addNextButton = new Button(buttonBar, SWT.PUSH);
        addNextButton.setImage(ADD_NEXT_IMAGE);
        addNextButton.setToolTipText(Messages.CustomXmlParserInputWizardPage_addNextElement);
        addNextButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                InputElement inputElement = new InputElement("", false, CustomXmlTraceDefinition.TAG_IGNORE, 0, "", null); //$NON-NLS-1$ //$NON-NLS-2$
                if (definition.rootInputElement == null) {
                    definition.rootInputElement = inputElement;
                    inputElement.elementName = getChildNameSuggestion(null);
                } else if (treeViewer.getSelection().isEmpty()) {
                    return;
                } else {
                    InputElement previousInputElement = (InputElement) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                    if (previousInputElement == definition.rootInputElement) {
                        return;
                    }
                    previousInputElement.addNext(inputElement);
                    inputElement.elementName = getChildNameSuggestion(inputElement.parentElement);
                }
                treeViewer.refresh();
                treeViewer.setSelection(new StructuredSelection(inputElement), true);
            }
        });

        Button feelingLuckyButton = new Button(buttonBar, SWT.PUSH);
        feelingLuckyButton.setImage(ADD_MANY_IMAGE);
        feelingLuckyButton.setToolTipText(Messages.CustomXmlParserInputWizardPage_feelingLucky);
        feelingLuckyButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                InputElement inputElement = null;
                if (definition.rootInputElement == null) {
                    if (getChildNameSuggestion(null).length() != 0) {
                        inputElement = new InputElement(getChildNameSuggestion(null), false, CustomXmlTraceDefinition.TAG_IGNORE, 0, "", null); //$NON-NLS-1$
                        definition.rootInputElement = inputElement;
                        feelingLucky(inputElement);
                    } else {
                        return;
                    }
                } else if (treeViewer.getSelection().isEmpty()) {
                    return;
                } else {
                    inputElement = (InputElement) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                    feelingLucky(inputElement);
                }
                treeViewer.refresh();
                treeViewer.setSelection(new StructuredSelection(inputElement), true);
                treeViewer.expandToLevel(inputElement, AbstractTreeViewer.ALL_LEVELS);
            }
        });

        moveUpButton = new Button(buttonBar, SWT.PUSH);
        moveUpButton.setImage(MOVE_UP_IMAGE);
        moveUpButton.setToolTipText(Messages.CustomXmlParserInputWizardPage_moveUp);
        moveUpButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (treeViewer.getSelection().isEmpty()) {
                    return;
                }
                InputElement inputElement = (InputElement) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                if (inputElement == definition.rootInputElement) {
                    return;
                }
                inputElement.moveUp();
                treeViewer.refresh();
                validate();
                updatePreviews();
            }
        });

        moveDownButton = new Button(buttonBar, SWT.PUSH);
        moveDownButton.setImage(MOVE_DOWN_IMAGE);
        moveDownButton.setToolTipText(Messages.CustomXmlParserInputWizardPage_moveDown);
        moveDownButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (treeViewer.getSelection().isEmpty()) {
                    return;
                }
                InputElement inputElement = (InputElement) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                if (inputElement == definition.rootInputElement) {
                    return;
                }
                inputElement.moveDown();
                treeViewer.refresh();
                validate();
                updatePreviews();
            }
        });
    }

    private void feelingLucky(InputElement inputElement) {
        while (true) {
            String attributeName = getAttributeNameSuggestion(inputElement);
            if (attributeName.length() == 0) {
                break;
            }
            InputAttribute attribute = new InputAttribute(attributeName, attributeName, 0, ""); //$NON-NLS-1$
            inputElement.addAttribute(attribute);
        }
        while (true) {
            String childName = getChildNameSuggestion(inputElement);
            if (childName.length() == 0) {
                break;
            }
            InputElement childElement = new InputElement(childName, false, CustomXmlTraceDefinition.TAG_IGNORE, 0, "", null); //$NON-NLS-1$
            inputElement.addChild(childElement);
            feelingLucky(childElement);
        }
    }

    private static class InputElementTreeNodeContentProvider implements ITreeContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            CustomXmlTraceDefinition def = (CustomXmlTraceDefinition) inputElement;
            if (def.rootInputElement != null) {
                return new Object[] { def.rootInputElement };
            }
            return new Object[0];
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            InputElement inputElement = (InputElement) parentElement;
            if (inputElement.childElements == null) {
                return new InputElement[0];
            }
            return inputElement.childElements.toArray();
        }

        @Override
        public boolean hasChildren(Object element) {
            InputElement inputElement = (InputElement) element;
            return (inputElement.childElements != null && inputElement.childElements.size() > 0);
        }

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public Object getParent(Object element) {
            InputElement inputElement = (InputElement) element;
            return inputElement.parentElement;
        }
    }

    private static class InputElementTreeLabelProvider extends ColumnLabelProvider {

        @Override
        public Image getImage(Object element) {
            return ELEMENT_IMAGE;
        }

        @Override
        public String getText(Object element) {
            InputElement inputElement = (InputElement) element;
            return (inputElement.elementName.trim().length() == 0) ? "?" : inputElement.elementName; //$NON-NLS-1$
        }
    }

    private class InputElementTreeSelectionChangedListener implements ISelectionChangedListener {
        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            if (selectedElement != null) {
                selectedElement.dispose();
            }
            if (!(event.getSelection().isEmpty()) && event.getSelection() instanceof IStructuredSelection) {
                IStructuredSelection sel = (IStructuredSelection) event.getSelection();
                InputElement inputElement = (InputElement) sel.getFirstElement();
                selectedElement = new ElementNode(elementContainer, inputElement);
                elementContainer.layout();
                elementScrolledComposite.setMinSize(elementContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x,
                        elementContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y - 1);
                container.layout();
                validate();
                updatePreviews();
                removeButton.setEnabled(true);
                addChildButton.setEnabled(true);
                addChildButton.setToolTipText(Messages.CustomXmlParserInputWizardPage_addChildElement);
                if (definition.rootInputElement == inputElement) {
                    addNextButton.setEnabled(false);
                } else {
                    addNextButton.setEnabled(true);
                }
                moveUpButton.setEnabled(true);
                moveDownButton.setEnabled(true);
            } else {
                removeButton.setEnabled(false);
                if (definition.rootInputElement == null) {
                    addChildButton.setEnabled(true);
                    addChildButton.setToolTipText(Messages.CustomXmlParserInputWizardPage_addDocumentElement);
                } else {
                    addChildButton.setEnabled(false);
                }
                addNextButton.setEnabled(false);
                moveUpButton.setEnabled(false);
                moveDownButton.setEnabled(false);
            }
        }
    }

    @Override
    public void dispose() {
        if (fixedFont != null) {
            fixedFont.dispose();
            fixedFont = null;
        }
        super.dispose();
    }

    private void loadDefinition(CustomXmlTraceDefinition def) {
        categoryText.setText(def.categoryName);
        logtypeText.setText(def.definitionName);
        timeStampOutputFormatText.setText(def.timeStampOutputFormat);
        treeViewer.setInput(def);

        if (def.rootInputElement != null) {
            treeViewer.setSelection(new StructuredSelection(def.rootInputElement));
        }
    }

    private String getName(InputElement inputElement) {
        String name = (inputElement.elementName.trim().length() == 0) ? "?" : inputElement.elementName.trim(); //$NON-NLS-1$
        if (inputElement.parentElement == null) {
            return name;
        }
        return getName(inputElement.parentElement) + " : " + name; //$NON-NLS-1$
    }

    private String getName(InputAttribute inputAttribute, InputElement inputElement) {
        String name = (inputAttribute.attributeName.trim().length() == 0) ? "?" : inputAttribute.attributeName.trim(); //$NON-NLS-1$
        return getName(inputElement) + " : " + name; //$NON-NLS-1$
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            validate();
            updatePreviews();
        }
        super.setVisible(visible);
    }

    /**
     * Get the global list of input names.
     *
     * @return The list of input names
     */
    public List<String> getInputNames() {
        return getInputNames(definition.rootInputElement);
    }

    /**
     * Get the list of input names for a given element.
     *
     * @param inputElement
     *            The element
     * @return The input names for this element
     */
    public List<String> getInputNames(InputElement inputElement) {
        List<String> inputs = new ArrayList<>();
        if (inputElement.inputName != null && !inputElement.inputName.equals(CustomXmlTraceDefinition.TAG_IGNORE)) {
            String inputName = inputElement.inputName;
            if (!inputs.contains(inputName)) {
                inputs.add(inputName);
            }
        }
        if (inputElement.attributes != null) {
            for (InputAttribute attribute : inputElement.attributes) {
                String inputName = attribute.inputName;
                if (!inputs.contains(inputName)) {
                    inputs.add(inputName);
                }
            }
        }
        if (inputElement.childElements != null) {
            for (InputElement childInputElement : inputElement.childElements) {
                for (String inputName : getInputNames(childInputElement)) {
                    if (!inputs.contains(inputName)) {
                        inputs.add(inputName);
                    }
                }
            }
        }
        return inputs;
    }

    private void removeElement() {
        selectedElement.dispose();
        selectedElement = null;
        elementContainer.layout();
        elementScrolledComposite.setMinSize(elementContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x,
                elementContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y - 1);
        container.layout();
    }

    private String getSelectionText() {
        InputStream inputStream = null;
        if (this.selection instanceof IStructuredSelection) {
            Object sel = ((IStructuredSelection) this.selection).getFirstElement();
            if (sel instanceof IFile) {
                IFile file = (IFile) sel;
                try {
                    inputStream = file.getContents();
                } catch (CoreException e) {
                    return ""; //$NON-NLS-1$
                }
            }
        }
        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));) {
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n"); //$NON-NLS-1$
                }
                parseXmlInput(sb.toString());
                return sb.toString();
            } catch (IOException e) {
                return ""; //$NON-NLS-1$
            }
        }
        return ""; //$NON-NLS-1$
    }

    private void parseXmlInput(final String string) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            // The following allows xml parsing without access to the dtd
            EntityResolver resolver = new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) {
                    String empty = ""; //$NON-NLS-1$
                    ByteArrayInputStream bais = new ByteArrayInputStream(empty.getBytes());
                    return new InputSource(bais);
                }
            };
            db.setEntityResolver(resolver);

            // The following catches xml parsing exceptions
            db.setErrorHandler(new ErrorHandler() {
                @Override
                public void error(SAXParseException saxparseexception) throws SAXException {
                }

                @Override
                public void warning(SAXParseException saxparseexception) throws SAXException {
                }

                @Override
                public void fatalError(SAXParseException saxparseexception) throws SAXException {
                    if (string.trim().length() != 0) {
                        errorText.setText(saxparseexception.getMessage());
                        errorText.setBackground(COLOR_LIGHT_RED);
                        errorText.setVisible(true);
                    }
                    throw saxparseexception;
                }
            });

            errorText.setVisible(false);
            Document doc = null;
            doc = db.parse(new ByteArrayInputStream(string.getBytes()));
            documentElement = doc.getDocumentElement();
        } catch (ParserConfigurationException e) {
            Activator.getDefault().logError("Error pasing XML input string: " + string, e); //$NON-NLS-1$
            documentElement = null;
        } catch (SAXException e) {
            documentElement = null;
        } catch (IOException e) {
            Activator.getDefault().logError("Error pasing XML input string: " + string, e); //$NON-NLS-1$
            documentElement = null;
        }
    }

    private void initValues() {
        timeStampValue = null;
        timeStampFormat = null;
        logEntriesCount = 0;
        logEntryFound = false;
    }

    private void updatePreviews() {
        if (inputText == null) {
            // early update during construction
            return;
        }
        inputText.setStyleRanges(new StyleRange[] {});
        if (selectedElement == null) {
            return;
        }

        initValues();

        selectedElement.updatePreview();

        if (timeStampValue != null && timeStampFormat != null) {
            try {
                TmfTimestampFormat timestampFormat = new TmfTimestampFormat(timeStampFormat);
                long timestamp = timestampFormat.parseValue(timeStampValue);
                timestampFormat = new TmfTimestampFormat(timeStampOutputFormatText.getText().trim());
                timeStampPreviewText.setText(timestampFormat.format(timestamp));
            } catch (ParseException e) {
                timeStampPreviewText.setText("*parse exception* [" + timeStampValue + "] <> [" + timeStampFormat + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } catch (IllegalArgumentException e) {
                timeStampPreviewText.setText("*parse exception* [Illegal Argument]"); //$NON-NLS-1$
            }
        } else {
            timeStampPreviewText.setText("*no matching time stamp*"); //$NON-NLS-1$
        }
    }

    private void openHelpShell(String url) {
        if (helpBrowser != null && !helpBrowser.isDisposed()) {
            helpBrowser.getShell().setActive();
            if (!helpBrowser.getUrl().equals(url)) {
                helpBrowser.setUrl(url);
            }
            return;
        }
        final Shell helpShell = new Shell(getShell(), SWT.SHELL_TRIM);
        helpShell.setLayout(new FillLayout());
        helpBrowser = new Browser(helpShell, SWT.NONE);
        helpBrowser.addTitleListener(new TitleListener() {
            @Override
            public void changed(TitleEvent event) {
                helpShell.setText(event.title);
            }
        });
        Rectangle r = container.getBounds();
        Point p = container.toDisplay(r.x, r.y);
        Rectangle trim = helpShell.computeTrim(p.x + (r.width - 750) / 2, p.y + (r.height - 400) / 2, 750, 400);
        helpShell.setBounds(trim);
        helpShell.open();
        helpBrowser.setUrl(url);
    }

    private class UpdateListener implements ModifyListener, SelectionListener {

        @Override
        public void modifyText(ModifyEvent e) {
            validate();
            updatePreviews();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            validate();
            updatePreviews();
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            validate();
            updatePreviews();
        }

    }

    private class ElementNode {
        private final InputElement inputElement;
        private final Group group;
        private List<Attribute> attributes = new ArrayList<>();
        private List<ElementNode> childElements = new ArrayList<>();
        private Text elementNameText;
        private Composite tagComposite;
        private Combo tagCombo;
        private Label tagLabel;
        private Text tagText;
        private Combo actionCombo;
        private Label previewLabel;
        private Text previewText;
        private Button logEntryButton;
        private Label fillerLabel;
        private Composite addAttributeComposite;
        private Button addAttributeButton;
        private Label addAttributeLabel;

        public ElementNode(Composite parent, InputElement inputElement) {
            this.inputElement = inputElement;

            group = new Group(parent, SWT.NONE);
            GridLayout gl = new GridLayout(2, false);
            gl.marginHeight = 0;
            group.setLayout(gl);
            group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            group.setText(getName(inputElement));

            Label label = new Label(group, SWT.NULL);
            label.setText(Messages.CustomXmlParserInputWizardPage_elementName);
            label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

            elementNameText = new Text(group, SWT.BORDER | SWT.SINGLE);
            GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gd.widthHint = 0;
            elementNameText.setLayoutData(gd);
            elementNameText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    ElementNode.this.inputElement.elementName = elementNameText.getText().trim();
                    group.setText(getName(ElementNode.this.inputElement));
                }
            });
            elementNameText.setText(inputElement.elementName);
            elementNameText.addModifyListener(updateListener);

            if (inputElement.parentElement != null) {
                previewLabel = new Label(group, SWT.NULL);
                previewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
                previewLabel.setText(Messages.CustomXmlParserInputWizardPage_preview);

                previewText = new Text(group, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
                gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
                gd.widthHint = 0;
                previewText.setLayoutData(gd);
                previewText.setText(Messages.CustomXmlParserInputWizardPage_noMatchingElement);
                previewText.setBackground(COLOR_WIDGET_BACKGROUND);

                logEntryButton = new Button(group, SWT.CHECK);
                logEntryButton.setText(Messages.CustomXmlParserInputWizardPage_logEntry);
                logEntryButton.setSelection(inputElement.logEntry);
                logEntryButton.addSelectionListener(new SelectionListener() {
                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                    }

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        InputElement parentElem = ElementNode.this.inputElement.parentElement;
                        while (parentElem != null) {
                            parentElem.logEntry = false;
                            parentElem = parentElem.parentElement;
                        }
                    }
                });
                logEntryButton.addSelectionListener(updateListener);

                tagComposite = new Composite(group, SWT.FILL);
                GridLayout tagLayout = new GridLayout(4, false);
                tagLayout.marginWidth = 0;
                tagLayout.marginHeight = 0;
                tagComposite.setLayout(tagLayout);
                tagComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

                tagCombo = new Combo(tagComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
                tagCombo.setItems(new String[] { CustomXmlTraceDefinition.TAG_IGNORE, CustomTraceDefinition.TAG_TIMESTAMP,
                        CustomTraceDefinition.TAG_MESSAGE, CustomTraceDefinition.TAG_OTHER });
                tagCombo.setVisibleItemCount(tagCombo.getItemCount());
                tagCombo.addSelectionListener(new SelectionListener() {
                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                    }

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        tagText.removeModifyListener(updateListener);
                        switch (tagCombo.getSelectionIndex()) {
                        case 0: // Ignore
                            tagLabel.setVisible(false);
                            tagText.setVisible(false);
                            actionCombo.setVisible(false);
                            break;
                        case 1: // Time Stamp
                            tagLabel.setText(Messages.CustomXmlParserInputWizardPage_format);
                            tagLabel.setVisible(true);
                            tagText.setVisible(true);
                            tagText.addModifyListener(updateListener);
                            actionCombo.setVisible(true);
                            break;
                        case 2: // Message
                            tagLabel.setVisible(false);
                            tagText.setVisible(false);
                            actionCombo.setVisible(true);
                            break;
                        case 3: // Other
                            tagLabel.setText(Messages.CustomXmlParserInputWizardPage_tagName);
                            tagLabel.setVisible(true);
                            if (tagText.getText().trim().length() == 0) {
                                tagText.setText(elementNameText.getText().trim());
                            }
                            tagText.setVisible(true);
                            tagText.addModifyListener(updateListener);
                            actionCombo.setVisible(true);
                            break;
                        default:
                            break;
                        }
                        tagComposite.layout();
                        validate();
                        updatePreviews();
                    }
                });

                tagLabel = new Label(tagComposite, SWT.NULL);
                tagLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

                tagText = new Text(tagComposite, SWT.BORDER | SWT.SINGLE);
                gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
                gd.widthHint = 0;
                tagText.setLayoutData(gd);

                actionCombo = new Combo(tagComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
                actionCombo.setItems(new String[] { Messages.CustomXmlParserInputWizardPage_set, Messages.CustomXmlParserInputWizardPage_append,
                        Messages.CustomXmlParserInputWizardPage_appendWith });
                actionCombo.select(inputElement.inputAction);
                actionCombo.addSelectionListener(updateListener);

                if (inputElement.inputName.equals(CustomXmlTraceDefinition.TAG_IGNORE)) {
                    tagCombo.select(0);
                    tagLabel.setVisible(false);
                    tagText.setVisible(false);
                    actionCombo.setVisible(false);
                } else if (inputElement.inputName.equals(CustomTraceDefinition.TAG_TIMESTAMP)) {
                    tagCombo.select(1);
                    tagLabel.setText(Messages.CustomXmlParserInputWizardPage_format);
                    tagText.setText(inputElement.inputFormat);
                    tagText.addModifyListener(updateListener);
                } else if (inputElement.inputName.equals(CustomTraceDefinition.TAG_MESSAGE)) {
                    tagCombo.select(2);
                    tagLabel.setVisible(false);
                    tagText.setVisible(false);
                } else {
                    tagCombo.select(3);
                    tagLabel.setText(Messages.CustomXmlParserInputWizardPage_tagName);
                    tagText.setText(inputElement.inputName);
                    tagText.addModifyListener(updateListener);
                }
            }

            if (inputElement.attributes != null) {
                for (InputAttribute inputAttribute : inputElement.attributes) {
                    Attribute attribute = new Attribute(group, this, inputAttribute, attributes.size() + 1);
                    attributes.add(attribute);
                }
            }

            createAddButton();
        }

        private void updatePreview() {
            Element element = getPreviewElement(inputElement);
            if (inputElement.parentElement != null) { // no preview text for
                                                      // document element
                previewText.setText(Messages.CustomXmlParserInputWizardPage_noMatchingElement);
                if (element != null) {
                    previewText.setText(CustomXmlTrace.parseElement(element, new StringBuffer()).toString());
                    if (logEntryButton.getSelection()) {
                        if (!logEntryFound) {
                            logEntryFound = true;
                            logEntriesCount++;
                        } else {
                            logEntryButton.setSelection(false); // remove nested
                                                                // log entry
                        }
                    }
                    if (tagCombo.getText().equals(CustomTraceDefinition.TAG_TIMESTAMP) && logEntriesCount <= 1) {
                        String value = previewText.getText().trim();
                        if (value.length() != 0) {
                            if (actionCombo.getSelectionIndex() == CustomTraceDefinition.ACTION_SET) {
                                timeStampValue = value;
                                timeStampFormat = tagText.getText().trim();
                            } else if (actionCombo.getSelectionIndex() == CustomTraceDefinition.ACTION_APPEND) {
                                if (timeStampValue != null) {
                                    timeStampValue += value;
                                    timeStampFormat += tagText.getText().trim();
                                } else {
                                    timeStampValue = value;
                                    timeStampFormat = tagText.getText().trim();
                                }
                            } else if (actionCombo.getSelectionIndex() == CustomTraceDefinition.ACTION_APPEND_WITH_SEPARATOR) {
                                if (timeStampValue != null) {
                                    timeStampValue += " | " + value; //$NON-NLS-1$
                                    timeStampFormat += " | " + tagText.getText().trim(); //$NON-NLS-1$
                                } else {
                                    timeStampValue = value;
                                    timeStampFormat = tagText.getText().trim();
                                }
                            }
                        }
                    }
                }
            }
            for (Attribute attribute : attributes) {
                if (element != null) {
                    String value = element.getAttribute(attribute.attributeNameText.getText().trim());
                    if (value.length() != 0) {
                        attribute.previewText.setText(value);
                        if (attribute.tagCombo.getText().equals(CustomTraceDefinition.TAG_TIMESTAMP) && logEntriesCount <= 1) {
                            if (attribute.actionCombo.getSelectionIndex() == CustomTraceDefinition.ACTION_SET) {
                                timeStampValue = value;
                                timeStampFormat = attribute.tagText.getText().trim();
                            } else if (attribute.actionCombo.getSelectionIndex() == CustomTraceDefinition.ACTION_APPEND) {
                                if (timeStampValue != null) {
                                    timeStampValue += value;
                                    timeStampFormat += attribute.tagText.getText().trim();
                                } else {
                                    timeStampValue = value;
                                    timeStampFormat = attribute.tagText.getText().trim();
                                }
                            } else if (attribute.actionCombo.getSelectionIndex() == CustomTraceDefinition.ACTION_APPEND_WITH_SEPARATOR) {
                                if (timeStampValue != null) {
                                    timeStampValue += " | " + value; //$NON-NLS-1$
                                    timeStampFormat += " | " + attribute.tagText.getText().trim(); //$NON-NLS-1$
                                } else {
                                    timeStampValue = value;
                                    timeStampFormat = attribute.tagText.getText().trim();
                                }
                            }
                        }
                    } else {
                        attribute.previewText.setText(Messages.CustomXmlParserInputWizardPage_noMatchingAttribute);
                    }
                } else {
                    attribute.previewText.setText(Messages.CustomXmlParserInputWizardPage_noMatchingElement);
                }
            }
            for (ElementNode child : childElements) {
                child.updatePreview();
            }
            if (logEntryButton != null && logEntryButton.getSelection()) {
                logEntryFound = false;
            }
        }

        private void createAddButton() {
            fillerLabel = new Label(group, SWT.NONE);

            addAttributeComposite = new Composite(group, SWT.NONE);
            addAttributeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            GridLayout addAttributeLayout = new GridLayout(2, false);
            addAttributeLayout.marginHeight = 0;
            addAttributeLayout.marginWidth = 0;
            addAttributeComposite.setLayout(addAttributeLayout);

            addAttributeButton = new Button(addAttributeComposite, SWT.PUSH);
            addAttributeButton.setImage(ADD_IMAGE);
            addAttributeButton.setToolTipText(Messages.CustomXmlParserInputWizardPage_addAttribute);
            addAttributeButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    removeAddButton();
                    String attributeName = getAttributeNameSuggestion(inputElement);
                    InputAttribute inputAttribute = new InputAttribute(attributeName, attributeName, 0, ""); //$NON-NLS-1$
                    attributes.add(new Attribute(group, ElementNode.this, inputAttribute, attributes.size() + 1));
                    createAddButton();
                    elementContainer.layout();
                    elementScrolledComposite.setMinSize(elementContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x,
                            elementContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y - 1);
                    group.getParent().layout();
                    validate();
                    updatePreviews();
                }
            });

            addAttributeLabel = new Label(addAttributeComposite, SWT.NULL);
            addAttributeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            addAttributeLabel.setText(Messages.CustomXmlParserInputWizardPage_newAttibute);
        }

        private void removeAddButton() {
            fillerLabel.dispose();
            addAttributeComposite.dispose();
        }

        private void removeAttribute(int attributeNumber) {
            int nb = attributeNumber;
            if (--nb < attributes.size()) {
                attributes.remove(nb).dispose();
                for (int i = nb; i < attributes.size(); i++) {
                    attributes.get(i).setAttributeNumber(i + 1);
                }
                elementContainer.layout();
                elementScrolledComposite.setMinSize(elementContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x,
                        elementContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y - 1);
                group.getParent().layout();
            }
        }

        private void dispose() {
            group.dispose();
        }

        private void extractInputs() {
            inputElement.elementName = elementNameText.getText().trim();
            if (inputElement.parentElement != null) {
                inputElement.logEntry = logEntryButton.getSelection();
                if (tagCombo.getText().equals(CustomTraceDefinition.TAG_OTHER)) {
                    inputElement.inputName = tagText.getText().trim();
                } else {
                    inputElement.inputName = tagCombo.getText();
                    if (tagCombo.getText().equals(CustomTraceDefinition.TAG_TIMESTAMP)) {
                        inputElement.inputFormat = tagText.getText().trim();
                    }
                }
                inputElement.inputAction = actionCombo.getSelectionIndex();
            }
            inputElement.attributes = new ArrayList<>(attributes.size());
            for (int i = 0; i < attributes.size(); i++) {
                Attribute attribute = attributes.get(i);
                InputAttribute inputAttribute = new InputAttribute();
                inputAttribute.attributeName = attribute.attributeNameText.getText().trim();
                if (attribute.tagCombo.getText().equals(CustomTraceDefinition.TAG_OTHER)) {
                    inputAttribute.inputName = attribute.tagText.getText().trim();
                } else {
                    inputAttribute.inputName = attribute.tagCombo.getText();
                    if (attribute.tagCombo.getText().equals(CustomTraceDefinition.TAG_TIMESTAMP)) {
                        inputAttribute.inputFormat = attribute.tagText.getText().trim();
                    }
                }
                inputAttribute.inputAction = attribute.actionCombo.getSelectionIndex();
                inputElement.addAttribute(inputAttribute);
            }
        }
    }

    private class Attribute {
        private ElementNode element;
        private int attributeNumber;

        // children of parent (must be disposed)
        private Composite labelComposite;
        private Composite attributeComposite;
        private Label filler;
        private Composite tagComposite;

        // children of labelComposite
        private Label attributeLabel;

        // children of attributeComposite
        private Text attributeNameText;
        private Text previewText;

        // children of tagComposite
        private Combo tagCombo;
        private Label tagLabel;
        private Text tagText;
        private Combo actionCombo;

        public Attribute(Composite parent, ElementNode element, InputAttribute inputAttribute, int attributeNumber) {
            this.element = element;
            this.attributeNumber = attributeNumber;

            labelComposite = new Composite(parent, SWT.FILL);
            GridLayout labelLayout = new GridLayout(2, false);
            labelLayout.marginWidth = 0;
            labelLayout.marginHeight = 0;
            labelComposite.setLayout(labelLayout);
            labelComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

            Button deleteButton = new Button(labelComposite, SWT.PUSH);
            deleteButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            deleteButton.setImage(DELETE_IMAGE);
            deleteButton.setToolTipText(Messages.CustomXmlParserInputWizardPage_removeAttribute);
            deleteButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Attribute.this.element.removeAttribute(Attribute.this.attributeNumber);
                    validate();
                    updatePreviews();
                }
            });

            attributeLabel = new Label(labelComposite, SWT.NULL);
            attributeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            attributeLabel.setText(Messages.CustomXmlParserInputWizardPage_attibute);

            attributeComposite = new Composite(parent, SWT.FILL);
            GridLayout attributeLayout = new GridLayout(4, false);
            attributeLayout.marginWidth = 0;
            attributeLayout.marginHeight = 0;
            attributeComposite.setLayout(attributeLayout);
            attributeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

            Label nameLabel = new Label(attributeComposite, SWT.NONE);
            nameLabel.setText(Messages.CustomXmlParserInputWizardPage_name);

            attributeNameText = new Text(attributeComposite, SWT.BORDER | SWT.SINGLE);
            attributeNameText.setLayoutData(new GridData(120, SWT.DEFAULT));
            attributeNameText.setText(inputAttribute.attributeName);
            attributeNameText.addModifyListener(updateListener);

            Label previewLabel = new Label(attributeComposite, SWT.NONE);
            previewLabel.setText(Messages.CustomXmlParserInputWizardPage_preview);

            previewText = new Text(attributeComposite, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
            GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gd.widthHint = 0;
            previewText.setLayoutData(gd);
            previewText.setText(Messages.CustomXmlParserInputWizardPage_noMatch);
            previewText.setBackground(COLOR_WIDGET_BACKGROUND);

            filler = new Label(parent, SWT.NULL);

            tagComposite = new Composite(parent, SWT.FILL);
            GridLayout tagLayout = new GridLayout(4, false);
            tagLayout.marginWidth = 0;
            tagLayout.marginHeight = 0;
            tagComposite.setLayout(tagLayout);
            tagComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

            tagCombo = new Combo(tagComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
            tagCombo.setItems(new String[] { CustomTraceDefinition.TAG_TIMESTAMP, CustomTraceDefinition.TAG_MESSAGE,
                    CustomTraceDefinition.TAG_OTHER });
            tagCombo.select(2); // Other
            tagCombo.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                }

                @Override
                public void widgetSelected(SelectionEvent e) {
                    tagText.removeModifyListener(updateListener);
                    switch (tagCombo.getSelectionIndex()) {
                    case 0: // Time Stamp
                        tagLabel.setText(Messages.CustomXmlParserInputWizardPage_format);
                        tagLabel.setVisible(true);
                        tagText.setVisible(true);
                        tagText.addModifyListener(updateListener);
                        break;
                    case 1: // Message
                        tagLabel.setVisible(false);
                        tagText.setVisible(false);
                        break;
                    case 2: // Other
                        tagLabel.setText(Messages.CustomXmlParserInputWizardPage_tagName);
                        tagLabel.setVisible(true);
                        if (tagText.getText().trim().length() == 0) {
                            tagText.setText(attributeNameText.getText().trim());
                        }
                        tagText.setVisible(true);
                        tagText.addModifyListener(updateListener);
                        break;
                    default:
                        break;
                    }
                    tagComposite.layout();
                    validate();
                    updatePreviews();
                }
            });

            tagLabel = new Label(tagComposite, SWT.NULL);
            tagLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

            tagText = new Text(tagComposite, SWT.BORDER | SWT.SINGLE);
            gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gd.widthHint = 0;
            tagText.setLayoutData(gd);
            tagText.setText(attributeNameText.getText());

            actionCombo = new Combo(tagComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
            actionCombo.setItems(new String[] { Messages.CustomXmlParserInputWizardPage_set, Messages.CustomXmlParserInputWizardPage_append,
                    Messages.CustomXmlParserInputWizardPage_appendWith });
            actionCombo.select(inputAttribute.inputAction);
            actionCombo.addSelectionListener(updateListener);

            if (inputAttribute.inputName.equals(CustomTraceDefinition.TAG_TIMESTAMP)) {
                tagCombo.select(0);
                tagLabel.setText(Messages.CustomXmlParserInputWizardPage_format);
                tagText.setText(inputAttribute.inputFormat);
                tagText.addModifyListener(updateListener);
            } else if (inputAttribute.inputName.equals(CustomTraceDefinition.TAG_MESSAGE)) {
                tagCombo.select(1);
                tagLabel.setVisible(false);
                tagText.setVisible(false);
            } else {
                tagCombo.select(2);
                tagLabel.setText(Messages.CustomXmlParserInputWizardPage_tagName);
                tagText.setText(inputAttribute.inputName);
                tagText.addModifyListener(updateListener);
            }
        }

        private void dispose() {
            labelComposite.dispose();
            attributeComposite.dispose();
            filler.dispose();
            tagComposite.dispose();
        }

        private void setAttributeNumber(int attributeNumber) {
            this.attributeNumber = attributeNumber;
            labelComposite.layout();
        }
    }

    private Element getPreviewElement(InputElement inputElement) {
        InputElement currentElement = inputElement;
        Element element = documentElement;
        if (element != null) {
            if (!documentElement.getNodeName().equals(definition.rootInputElement.elementName)) {
                return null;
            }
            ArrayList<String> elementNames = new ArrayList<>();
            while (currentElement != null) {
                elementNames.add(currentElement.elementName);
                currentElement = currentElement.parentElement;
            }
            for (int i = elementNames.size() - 1; --i >= 0;) {
                NodeList childList = element.getChildNodes();
                element = null;
                for (int j = 0; j < childList.getLength(); j++) {
                    Node child = childList.item(j);
                    if (child instanceof Element && child.getNodeName().equals(elementNames.get(i))) {
                        element = (Element) child;
                        break;
                    }
                }
                if (element == null) {
                    break;
                }
            }
            if (element != null) {
                return element;
            }
        }
        return null;
    }

    private String getChildNameSuggestion(InputElement inputElement) {
        if (inputElement == null) {
            if (documentElement != null) {
                return documentElement.getNodeName();
            }
        } else {
            Element element = getPreviewElement(inputElement);
            if (element != null) {
                NodeList childNodes = element.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node node = childNodes.item(i);
                    if (node instanceof Element) {
                        boolean unused = true;
                        if (inputElement.childElements != null) {
                            for (InputElement child : inputElement.childElements) {
                                if (child.elementName.equals(node.getNodeName())) {
                                    unused = false;
                                    break;
                                }
                            }
                        }
                        if (unused) {
                            return node.getNodeName();
                        }
                    }
                }
            }
        }
        return ""; //$NON-NLS-1$
    }

    private String getAttributeNameSuggestion(InputElement inputElement) {
        Element element = getPreviewElement(inputElement);
        if (element != null) {
            NamedNodeMap attributeMap = element.getAttributes();
            for (int i = 0; i < attributeMap.getLength(); i++) {
                Node node = attributeMap.item(i);
                boolean unused = true;
                if (inputElement.attributes != null) {
                    for (InputAttribute attribute : inputElement.attributes) {
                        if (attribute.attributeName.equals(node.getNodeName())) {
                            unused = false;
                            break;
                        }
                    }
                }
                if (unused) {
                    return node.getNodeName();
                }
            }
        }
        return ""; //$NON-NLS-1$
    }

    private void validate() {
        definition.categoryName = categoryText.getText().trim();
        definition.definitionName = logtypeText.getText().trim();
        definition.timeStampOutputFormat = timeStampOutputFormatText.getText().trim();

        if (selectedElement != null) {
            selectedElement.extractInputs();
            treeViewer.refresh();
        }

        StringBuffer errors = new StringBuffer();

        if (definition.categoryName.length() == 0) {
            errors.append(Messages.CustomXmlParserInputWizardPage_emptyCategoryError);
            categoryText.setBackground(COLOR_LIGHT_RED);
        } else if (definition.definitionName.length() == 0) {
            errors.append(Messages.CustomXmlParserInputWizardPage_emptyLogTypeError);
            logtypeText.setBackground(COLOR_LIGHT_RED);
        } else {
            categoryText.setBackground(COLOR_TEXT_BACKGROUND);
            logtypeText.setBackground(COLOR_TEXT_BACKGROUND);
            if (definition.categoryName.indexOf(':') != -1) {
                errors.append(Messages.CustomXmlParserInputWizardPage_invalidCategoryError);
                categoryText.setBackground(COLOR_LIGHT_RED);
            }
            if (definition.definitionName.indexOf(':') != -1) {
                errors.append(Messages.CustomXmlParserInputWizardPage_invalidLogTypeError);
                logtypeText.setBackground(COLOR_LIGHT_RED);
            }
            for (TraceTypeHelper helper : TmfTraceType.getTraceTypeHelpers()) {
                if (definition.categoryName.equals(helper.getCategoryName()) &&
                        definition.definitionName.equals(helper.getName()) &&
                        (editDefinitionName == null || !editDefinitionName.equals(definition.definitionName)) &&
                        (editCategoryName == null || !editCategoryName.equals(definition.categoryName))) {
                    errors.append(Messages.CustomXmlParserInputWizardPage_duplicatelogTypeError);
                    logtypeText.setBackground(COLOR_LIGHT_RED);
                    break;
                }
            }
        }

        if (definition.rootInputElement == null) {
            errors.append(Messages.CustomXmlParserInputWizardPage_noDocumentError);
        }

        if (definition.rootInputElement != null) {
            logEntryFound = false;
            timeStampFound = false;

            errors.append(validateElement(definition.rootInputElement));

            if ((definition.rootInputElement.attributes != null && definition.rootInputElement.attributes.size() != 0)
                    || (definition.rootInputElement.childElements != null && definition.rootInputElement.childElements.size() != 0)
                    || errors.length() == 0) {
                if (!logEntryFound) {
                    errors.append(Messages.CustomXmlParserInputWizardPage_missingLogEntryError);
                }

                if (timeStampFound) {
                    if (timeStampOutputFormatText.getText().trim().length() == 0) {
                        errors.append(Messages.CustomXmlParserInputWizardPage_missingTimestampFmtError);
                        timeStampOutputFormatText.setBackground(COLOR_LIGHT_RED);
                    } else {
                        try {
                            new TmfTimestampFormat(timeStampOutputFormatText.getText().trim());
                            timeStampOutputFormatText.setBackground(COLOR_TEXT_BACKGROUND);
                        } catch (IllegalArgumentException e) {
                            errors.append(Messages.CustomXmlParserInputWizardPage_invalidTimestampFmtError);
                            timeStampOutputFormatText.setBackground(COLOR_LIGHT_RED);
                        }
                    }
                } else {
                    timeStampPreviewText.setText(Messages.CustomXmlParserInputWizardPage_notimestamporAttributeError);
                }
            }
        } else {
            timeStampPreviewText.setText(Messages.CustomXmlParserInputWizardPage_notimestamporAttributeError);
        }

        if (errors.length() == 0) {
            setDescription(defaultDescription);
            setPageComplete(true);
        } else {
            setDescription(errors.toString());
            setPageComplete(false);
        }
    }

    /**
     * Clean up the specified XML element.
     *
     * @param inputElement
     *            The element to clean up
     * @return The validated element
     */
    public StringBuffer validateElement(InputElement inputElement) {
        StringBuffer errors = new StringBuffer();
        ElementNode elementNode = null;
        if (selectedElement != null && selectedElement.inputElement.equals(inputElement)) {
            elementNode = selectedElement;
        }
        if (inputElement == definition.rootInputElement) {
            if (inputElement.elementName.length() == 0) {
                errors.append(Messages.CustomXmlParserInputWizardPage_missingDocumentElementError);
                if (elementNode != null) {
                    elementNode.elementNameText.setBackground(COLOR_LIGHT_RED);
                }
            } else {
                if (elementNode != null) {
                    elementNode.elementNameText.setBackground(COLOR_TEXT_BACKGROUND);
                }
            }
        }
        if (inputElement != definition.rootInputElement) {
            if (inputElement.logEntry) {
                logEntryFound = true;
            }
            if (inputElement.inputName.equals(CustomTraceDefinition.TAG_TIMESTAMP)) {
                timeStampFound = true;
                if (inputElement.inputFormat.length() == 0) {
                    errors.append(Messages.CustomXmlParserInputWizardPage_timestampFormatPrompt
                            + " (" + Messages.CustomXmlParserInputWizardPage_timestampElementPrompt + " " + getName(inputElement) + "). "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    if (elementNode != null) {
                        elementNode.tagText.setBackground(COLOR_LIGHT_RED);
                    }
                } else {
                    try {
                        new TmfTimestampFormat(inputElement.inputFormat);
                        if (elementNode != null) {
                            elementNode.tagText.setBackground(COLOR_TEXT_BACKGROUND);
                        }
                    } catch (IllegalArgumentException e) {
                        errors.append(Messages.CustomXmlParserInputWizardPage_invalidTimestampFmtError
                                + " (" + Messages.CustomXmlParserInputWizardPage_timestampElementPrompt + " " + getName(inputElement) + "). "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        if (elementNode != null) {
                            elementNode.tagText.setBackground(COLOR_LIGHT_RED);
                        }
                    }
                }
            } else if (inputElement.inputName.length() == 0) {
                errors.append(Messages.CustomXmlParserInputWizardPage_missingInputElementNameError);
                if (elementNode != null) {
                    elementNode.tagText.setBackground(COLOR_LIGHT_RED);
                }
            } else {
                if (elementNode != null) {
                    elementNode.tagText.setBackground(COLOR_TEXT_BACKGROUND);
                }
            }
        }
        if (inputElement.attributes != null) {
            if (elementNode != null) {
                for (Attribute attribute : elementNode.attributes) {
                    attribute.attributeNameText.setBackground(COLOR_TEXT_BACKGROUND);
                }
            }
            for (int i = 0; i < inputElement.attributes.size(); i++) {
                InputAttribute attribute = inputElement.attributes.get(i);
                boolean duplicate = false;
                for (int j = i + 1; j < inputElement.attributes.size(); j++) {
                    InputAttribute otherAttribute = inputElement.attributes.get(j);
                    if (otherAttribute.attributeName.equals(attribute.attributeName)) {
                        duplicate = true;
                        if (elementNode != null) {
                            elementNode.attributes.get(j).attributeNameText.setBackground(COLOR_LIGHT_RED);
                        }
                    }
                }
                if (attribute.attributeName.length() == 0) {
                    errors.append(Messages.CustomXmlParserInputWizardPage_missingAttribute
                            + " (" + Messages.CustomXmlParserInputWizardPage_attributePrompt + " " + getName(inputElement) + ": ?). "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    if (elementNode != null) {
                        elementNode.attributes.get(i).attributeNameText.setBackground(COLOR_LIGHT_RED);
                    }
                } else if (duplicate) {
                    errors.append(Messages.CustomXmlParserInputWizardPage_duplicateAttributeError
                            + " (" + Messages.CustomXmlParserInputWizardPage_attributePrompt + " " + getName(attribute, inputElement) + "). "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    if (elementNode != null) {
                        elementNode.attributes.get(i).attributeNameText.setBackground(COLOR_LIGHT_RED);
                    }
                }
                if (attribute.inputName.equals(CustomTraceDefinition.TAG_TIMESTAMP)) {
                    timeStampFound = true;
                    if (attribute.inputFormat.length() == 0) {
                        errors.append(Messages.CustomXmlParserInputWizardPage_missingTimestampInFmtError
                                + " (" + Messages.CustomXmlParserInputWizardPage_attributePrompt + " " + getName(attribute, inputElement) + "). "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        if (elementNode != null) {
                            elementNode.attributes.get(i).tagText.setBackground(COLOR_LIGHT_RED);
                        }
                    } else {
                        try {
                            new TmfTimestampFormat(attribute.inputFormat);
                            if (elementNode != null) {
                                elementNode.attributes.get(i).tagText.setBackground(COLOR_TEXT_BACKGROUND);
                            }
                        } catch (IllegalArgumentException e) {
                            errors.append(Messages.CustomXmlParserInputWizardPage_invalidTimestampInFmtError
                                    + " (" + Messages.CustomXmlParserInputWizardPage_attributePrompt + " " + getName(attribute, inputElement) + "). "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            if (elementNode != null) {
                                elementNode.attributes.get(i).tagText.setBackground(COLOR_LIGHT_RED);
                            }
                        }
                    }
                } else if (attribute.inputName.length() == 0) {
                    errors.append(Messages.CustomXmlParserInputWizardPage_missingDataGroupNameError
                            + " (" + Messages.CustomXmlParserInputWizardPage_attributePrompt + " " + getName(attribute, inputElement) + "). "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    if (elementNode != null) {
                        elementNode.attributes.get(i).tagText.setBackground(COLOR_LIGHT_RED);
                    }
                } else {
                    if (elementNode != null) {
                        elementNode.attributes.get(i).tagText.setBackground(COLOR_TEXT_BACKGROUND);
                    }
                }
            }
        }
        if (inputElement.childElements != null) {
            for (InputElement child : inputElement.childElements) {
                ElementNode childElementNode = null;
                if (selectedElement != null && selectedElement.inputElement.equals(child)) {
                    childElementNode = selectedElement;
                }
                if (childElementNode != null) {
                    childElementNode.elementNameText.setBackground(COLOR_TEXT_BACKGROUND);
                }
            }
            for (int i = 0; i < inputElement.childElements.size(); i++) {
                InputElement child = inputElement.childElements.get(i);
                ElementNode childElementNode = null;
                if (selectedElement != null && selectedElement.inputElement.equals(child)) {
                    childElementNode = selectedElement;
                }
                if (child.elementName.length() == 0) {
                    errors.append(Messages.CustomXmlParserInputWizardPage_missingElementNameError
                            + " (" + Messages.CustomXmlParserInputWizardPage_attributePrompt + " " + getName(child) + "). "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    if (childElementNode != null) {
                        childElementNode.elementNameText.setBackground(COLOR_LIGHT_RED);
                    }
                } else {
                    boolean duplicate = false;
                    for (int j = i + 1; j < inputElement.childElements.size(); j++) {
                        InputElement otherChild = inputElement.childElements.get(j);
                        if (otherChild.elementName.equals(child.elementName)) {
                            duplicate = true;
                            ElementNode otherChildElementNode = null;
                            if (selectedElement != null && selectedElement.inputElement.equals(otherChild)) {
                                otherChildElementNode = selectedElement;
                            }
                            if (otherChildElementNode != null) {
                                otherChildElementNode.elementNameText.setBackground(COLOR_LIGHT_RED);
                            }
                        }
                    }
                    if (duplicate) {
                        errors.append(Messages.CustomXmlParserInputWizardPage_duplicateElementNameError
                                + " (" + Messages.CustomXmlParserInputWizardPage_attributePrompt + " " + getName(child) + "). "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        if (childElementNode != null) {
                            childElementNode.elementNameText.setBackground(COLOR_LIGHT_RED);
                        }
                    }
                }

                errors.append(validateElement(child));
            }
        }
        return errors;
    }

    /**
     * Get the trace definition.
     *
     * @return The trace definition
     */
    public CustomXmlTraceDefinition getDefinition() {
        return definition;
    }

    /**
     * Get the raw text input.
     *
     * @return The raw text input.
     */
    public char[] getInputText() {
        return inputText.getText().toCharArray();
    }
}
