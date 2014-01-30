/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTraceDefinition.Cardinality;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTraceDefinition.InputData;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTraceDefinition.InputLine;
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
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
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

/**
 * Input wizard page for custom text parsers.
 *
 * @author Patrick Tasse
 */
public class CustomTxtParserInputWizardPage extends WizardPage {

    private static final String DEFAULT_REGEX = "\\s*(.*\\S)"; //$NON-NLS-1$
    private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"; //$NON-NLS-1$
    private static final String SIMPLE_DATE_FORMAT_URL = "http://java.sun.com/javase/6/docs/api/java/text/SimpleDateFormat.html#skip-navbar_top"; //$NON-NLS-1$
    private static final String PATTERN_URL = "http://java.sun.com/javase/6/docs/api/java/util/regex/Pattern.html#sum"; //$NON-NLS-1$
    private static final Image LINE_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/line_icon.gif"); //$NON-NLS-1$
    private static final Image ADD_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/add_button.gif"); //$NON-NLS-1$
    private static final Image ADD_NEXT_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/addnext_button.gif"); //$NON-NLS-1$
    private static final Image ADD_CHILD_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/addchild_button.gif"); //$NON-NLS-1$
    private static final Image DELETE_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/delete_button.gif"); //$NON-NLS-1$
    private static final Image MOVE_UP_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/moveup_button.gif"); //$NON-NLS-1$
    private static final Image MOVE_DOWN_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/movedown_button.gif"); //$NON-NLS-1$
    private static final Image HELP_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/help_button.gif"); //$NON-NLS-1$
    private static final Color COLOR_BLACK = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
    private static final Color COLOR_LIGHT_GREEN = new Color(Display.getDefault(), 192, 255, 192);
    private static final Color COLOR_GREEN = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
    private static final Color COLOR_LIGHT_YELLOW = new Color(Display.getDefault(), 255, 255, 192);
    private static final Color COLOR_YELLOW = Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);
    private static final Color COLOR_LIGHT_MAGENTA = new Color(Display.getDefault(), 255, 192, 255);
    private static final Color COLOR_MAGENTA = Display.getDefault().getSystemColor(SWT.COLOR_MAGENTA);
    private static final Color COLOR_LIGHT_RED = new Color(Display.getDefault(), 255, 192, 192);
    private static final Color COLOR_TEXT_BACKGROUND = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
    private static final Color COLOR_WIDGET_BACKGROUND = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

    private final ISelection selection;
    private CustomTxtTraceDefinition definition;
    private String editDefinitionName;
    private String defaultDescription;
    private Line selectedLine;
    private Composite container;
    private Text logtypeText;
    private Text timestampOutputFormatText;
    private Text timestampPreviewText;
    private ScrolledComposite lineScrolledComposite;
    private TreeViewer treeViewer;
    private Composite lineContainer;
    private StyledText inputText;
    private Font fixedFont;
    private UpdateListener updateListener;
    private Browser helpBrowser;

    // variables used recursively through line traversal
    private String timeStampFormat;
    private boolean timestampFound;

    /**
     * Constructor
     *
     * @param selection
     *            The Selection object
     * @param definition
     *            The trace definition
     */
    protected CustomTxtParserInputWizardPage(ISelection selection,
            CustomTxtTraceDefinition definition) {
        super("CustomParserWizardPage"); //$NON-NLS-1$
        if (definition == null) {
            setTitle(Messages.CustomTxtParserInputWizardPage_windowTitleNew);
            defaultDescription = Messages.CustomTxtParserInputWizardPage_descriptionNew;
        } else {
            setTitle(Messages.CustomTxtParserInputWizardPage_windowTitleEdit);
            defaultDescription = Messages.CustomTxtParserInputWizardPage_desccriptionEdit;
        }
        setDescription(defaultDescription);
        this.selection = selection;
        this.definition = definition;
        if (definition != null) {
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

        Label logtypeLabel = new Label(headerComposite, SWT.NULL);
        logtypeLabel.setText(Messages.CustomTxtParserInputWizardPage_logType);

        logtypeText = new Text(headerComposite, SWT.BORDER | SWT.SINGLE);
        logtypeText.setLayoutData(new GridData(120, SWT.DEFAULT));

        Label timestampFormatLabel = new Label(headerComposite, SWT.NULL);
        timestampFormatLabel.setText(Messages.CustomTxtParserInputWizardPage_timestampFormat);

        timestampOutputFormatText = new Text(headerComposite, SWT.BORDER | SWT.SINGLE);
        timestampOutputFormatText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        timestampOutputFormatText.setText(DEFAULT_TIMESTAMP_FORMAT);

        Button dateFormatHelpButton = new Button(headerComposite, SWT.PUSH);
        dateFormatHelpButton.setImage(HELP_IMAGE);
        dateFormatHelpButton.setToolTipText(Messages.CustomTxtParserInputWizardPage_dateFormatHelp);
        dateFormatHelpButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openHelpShell(SIMPLE_DATE_FORMAT_URL);
            }
        });

        Label timestampPreviewLabel = new Label(headerComposite, SWT.NULL);
        timestampPreviewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 3, 1));
        timestampPreviewLabel.setText(Messages.CustomTxtParserInputWizardPage_preview);

        timestampPreviewText = new Text(headerComposite, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
        timestampPreviewText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        timestampPreviewText.setText(Messages.CustomTxtParserInputWizardPage_noMatchingTimestamp);

        Composite buttonBar = new Composite(container, SWT.NONE);
        GridLayout buttonBarLayout = new GridLayout(5, false);
        buttonBarLayout.marginHeight = 0;
        buttonBarLayout.marginWidth = 0;
        buttonBar.setLayout(buttonBarLayout);

        Button removeButton = new Button(buttonBar, SWT.PUSH);
        removeButton.setImage(DELETE_IMAGE);
        removeButton.setToolTipText(Messages.CustomTxtParserInputWizardPage_removeLine);
        removeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (treeViewer.getSelection().isEmpty() || selectedLine == null) {
                    return;
                }
                removeLine();
                InputLine inputLine = (InputLine) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                if (inputLine.parentInput == null) {
                    definition.inputs.remove(inputLine);
                } else {
                    inputLine.parentInput.childrenInputs.remove(inputLine);
                }
                treeViewer.refresh();
                validate();
                updatePreviews();
            }
        });
        Button addNextButton = new Button(buttonBar, SWT.PUSH);
        addNextButton.setImage(ADD_NEXT_IMAGE);
        addNextButton.setToolTipText(Messages.CustomTxtParserInputWizardPage_addNextLine);
        addNextButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent e) {
                InputLine inputLine = new InputLine(Cardinality.ZERO_OR_MORE, "", null); //$NON-NLS-1$
                if (((List<?>) treeViewer.getInput()).size() == 0) {
                    definition.inputs.add(inputLine);
                } else if (treeViewer.getSelection().isEmpty()) {
                    return;
                } else {
                    InputLine previousInputLine = (InputLine) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                    if (previousInputLine.parentInput == null) {
                        for (int i = 0; i < definition.inputs.size(); i++) {
                            if (definition.inputs.get(i).equals(previousInputLine)) {
                                definition.inputs.add(i + 1, inputLine);
                            }
                        }
                    } else {
                        previousInputLine.addNext(inputLine);
                    }
                }
                treeViewer.refresh();
                treeViewer.setSelection(new StructuredSelection(inputLine), true);
            }
        });
        Button addChildButton = new Button(buttonBar, SWT.PUSH);
        addChildButton.setImage(ADD_CHILD_IMAGE);
        addChildButton.setToolTipText(Messages.CustomTxtParserInputWizardPage_addChildLine);
        addChildButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                InputLine inputLine = new InputLine(Cardinality.ZERO_OR_MORE, "", null); //$NON-NLS-1$
                if (((List<?>) treeViewer.getInput()).size() == 0) {
                    definition.inputs.add(inputLine);
                } else if (treeViewer.getSelection().isEmpty()) {
                    return;
                } else {
                    InputLine parentInputLine = (InputLine) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                    parentInputLine.addChild(inputLine);
                }
                treeViewer.refresh();
                treeViewer.setSelection(new StructuredSelection(inputLine), true);
            }
        });
        Button moveUpButton = new Button(buttonBar, SWT.PUSH);
        moveUpButton.setImage(MOVE_UP_IMAGE);
        moveUpButton.setToolTipText(Messages.CustomTxtParserInputWizardPage_moveUp);
        moveUpButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (treeViewer.getSelection().isEmpty()) {
                    return;
                }
                InputLine inputLine = (InputLine) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                if (inputLine.parentInput == null) {
                    for (int i = 1; i < definition.inputs.size(); i++) {
                        if (definition.inputs.get(i).equals(inputLine)) {
                            definition.inputs.add(i - 1 , definition.inputs.remove(i));
                            break;
                        }
                    }
                } else {
                    inputLine.moveUp();
                }
                treeViewer.refresh();
                validate();
                updatePreviews();
            }
        });
        Button moveDownButton = new Button(buttonBar, SWT.PUSH);
        moveDownButton.setImage(MOVE_DOWN_IMAGE);
        moveDownButton.setToolTipText(Messages.CustomTxtParserInputWizardPage_moveDown);
        moveDownButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (treeViewer.getSelection().isEmpty()) {
                    return;
                }
                InputLine inputLine = (InputLine) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                if (inputLine.parentInput == null) {
                    for (int i = 0; i < definition.inputs.size() - 1; i++) {
                        if (definition.inputs.get(i).equals(inputLine)) {
                            definition.inputs.add(i + 1 , definition.inputs.remove(i));
                            break;
                        }
                    }
                } else {
                    inputLine.moveDown();
                }
                treeViewer.refresh();
                validate();
                updatePreviews();
            }
        });

        SashForm vSash = new SashForm(container, SWT.VERTICAL);
        vSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        vSash.setBackground(vSash.getDisplay().getSystemColor(SWT.COLOR_GRAY));

        SashForm hSash = new SashForm(vSash, SWT.HORIZONTAL);
        hSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        ScrolledComposite treeScrolledComposite = new ScrolledComposite(hSash, SWT.V_SCROLL | SWT.H_SCROLL);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 200;
        gd.widthHint = 200;
        treeScrolledComposite.setLayoutData(gd);
        Composite treeContainer = new Composite(treeScrolledComposite, SWT.NONE);
        treeContainer.setLayout(new FillLayout());
        treeScrolledComposite.setContent(treeContainer);
        treeScrolledComposite.setExpandHorizontal(true);
        treeScrolledComposite.setExpandVertical(true);

        treeViewer = new TreeViewer(treeContainer, SWT.SINGLE | SWT.BORDER);
        treeViewer.setContentProvider(new InputLineTreeNodeContentProvider());
        treeViewer.setLabelProvider(new InputLineTreeLabelProvider());
        treeViewer.addSelectionChangedListener(new InputLineTreeSelectionChangedListener());
        treeContainer.layout();

        treeScrolledComposite.setMinSize(treeContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, treeContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);

        lineScrolledComposite = new ScrolledComposite(hSash, SWT.V_SCROLL);
        lineScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        lineContainer = new Composite(lineScrolledComposite, SWT.NONE);
        GridLayout linesLayout = new GridLayout();
        linesLayout.marginHeight = 1;
        linesLayout.marginWidth = 0;
        lineContainer.setLayout(linesLayout);
        lineScrolledComposite.setContent(lineContainer);
        lineScrolledComposite.setExpandHorizontal(true);
        lineScrolledComposite.setExpandVertical(true);

        if (definition == null) {
            definition = new CustomTxtTraceDefinition();
            definition.inputs.add(new InputLine(Cardinality.ZERO_OR_MORE, DEFAULT_REGEX,
                    Arrays.asList(new InputData(CustomTraceDefinition.TAG_MESSAGE, CustomTraceDefinition.ACTION_SET))));
        }
        loadDefinition(definition);
        treeViewer.expandAll();
        lineContainer.layout();

        logtypeText.addModifyListener(updateListener);
        timestampOutputFormatText.addModifyListener(updateListener);

        lineScrolledComposite.setMinSize(lineContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, lineContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y-1);

        hSash.setWeights(new int[] {1, 2});

        Composite sashBottom = new Composite(vSash, SWT.NONE);
        GridLayout sashBottomLayout = new GridLayout(3, false);
        sashBottomLayout.marginHeight = 0;
        sashBottomLayout.marginWidth = 0;
        sashBottom.setLayout(sashBottomLayout);

        Label previewLabel = new Label(sashBottom, SWT.NULL);
        previewLabel.setText(Messages.CustomTxtParserInputWizardPage_previewInput);
        previewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button highlightAllButton = new Button(sashBottom, SWT.PUSH);
        highlightAllButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        highlightAllButton.setText(Messages.CustomTxtParserInputWizardPage_highlightAll);
        highlightAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updatePreviews(true);
            }
        });

        Button legendButton = new Button(sashBottom, SWT.PUSH);
        legendButton.setImage(HELP_IMAGE);
        legendButton.setToolTipText(Messages.CustomTxtParserInputWizardPage_previewLegend);
        legendButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        legendButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openLegend();
            }
        });

        inputText = new StyledText(sashBottom, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        if (fixedFont == null) {
            if (System.getProperty("os.name").contains("Windows")) { //$NON-NLS-1$  //$NON-NLS-2$
                fixedFont = new Font(Display.getCurrent(), new FontData("Courier New", 10, SWT.NORMAL)); //$NON-NLS-1$
            } else {
                fixedFont = new Font(Display.getCurrent(), new FontData("Monospace", 10, SWT.NORMAL)); //$NON-NLS-1$
            }
        }
        inputText.setFont(fixedFont);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
        gd.heightHint = inputText.computeSize(SWT.DEFAULT, inputText.getLineHeight() * 4).y;
        gd.widthHint = 800;
        inputText.setLayoutData(gd);
        inputText.setText(getSelectionText());
        inputText.addModifyListener(updateListener);

        vSash.setWeights(new int[] {hSash.computeSize(SWT.DEFAULT, SWT.DEFAULT).y, sashBottom.computeSize(SWT.DEFAULT, SWT.DEFAULT).y});

        setControl(container);

        validate();
        updatePreviews();
    }

    private static class InputLineTreeNodeContentProvider implements ITreeContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            return ((List<?>) inputElement).toArray();
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            InputLine inputLine = (InputLine) parentElement;
            if (inputLine.childrenInputs == null) {
                return new InputLine[0];
            }
            return inputLine.childrenInputs.toArray();
        }

        @Override
        public boolean hasChildren(Object element) {
            InputLine inputLine = (InputLine) element;
            return (inputLine.childrenInputs != null && inputLine.childrenInputs.size() > 0);
        }

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public Object getParent(Object element) {
            InputLine inputLine = (InputLine) element;
            return inputLine.parentInput;
        }
    }

    private class InputLineTreeLabelProvider extends ColumnLabelProvider {

        @Override
        public Image getImage(Object element) {
            return LINE_IMAGE;
        }

        @Override
        public String getText(Object element) {
            InputLine inputLine = (InputLine) element;
            if (inputLine.parentInput == null) {
                return "Root Line " + getName(inputLine) + " " + inputLine.cardinality.toString() + " : " + inputLine.getRegex(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            return "Line " + getName(inputLine) + " " + inputLine.cardinality.toString() + " : " + inputLine.getRegex(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    private class InputLineTreeSelectionChangedListener implements ISelectionChangedListener {
        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            if (selectedLine != null) {
                selectedLine.dispose();
            }
            if (!(event.getSelection().isEmpty()) && event.getSelection() instanceof IStructuredSelection) {
                IStructuredSelection sel = (IStructuredSelection) event.getSelection();
                InputLine inputLine = (InputLine) sel.getFirstElement();
                selectedLine = new Line(lineContainer, getName(inputLine), inputLine);
                lineContainer.layout();
                lineScrolledComposite.setMinSize(lineContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, lineContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y - 1);
                container.layout();
                validate();
                updatePreviews();
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

    private void loadDefinition(CustomTxtTraceDefinition def) {
        logtypeText.setText(def.definitionName);
        timestampOutputFormatText.setText(def.timeStampOutputFormat);
        treeViewer.setInput(def.inputs);
        if (def.inputs.size() > 0) {
            InputLine inputLine = def.inputs.get(0);
            treeViewer.setSelection(new StructuredSelection(inputLine));
        }
    }

    private String getName(InputLine inputLine) {
        if (inputLine.parentInput == null) {
            return Integer.toString(definition.inputs.indexOf(inputLine)+1);
        }
        return getName(inputLine.parentInput) + "." + Integer.toString(inputLine.parentInput.childrenInputs.indexOf(inputLine)+1); //$NON-NLS-1$
    }

    /**
     * Get the global list of input names.
     *
     * @return The list of input names
     */
    public List<String> getInputNames() {
        List<String> inputs = new ArrayList<>();
        for (InputLine inputLine : definition.inputs) {
            for (String inputName : getInputNames(inputLine)) {
                if (!inputs.contains(inputName)) {
                    inputs.add(inputName);
                }
            }
        }
        return inputs;
    }

    /**
     * Get the list of input names for the given input line.
     *
     * @param inputLine
     *            The input line
     * @return The list of input names
     */
    public List<String> getInputNames(InputLine inputLine) {
        List<String> inputs = new ArrayList<>();
        if (inputLine.columns != null) {
            for (InputData inputData : inputLine.columns) {
                String inputName = inputData.name;
                if (!inputs.contains(inputName)) {
                    inputs.add(inputName);
                }
            }
        }
        if (inputLine.childrenInputs != null) {
            for (InputLine childInputLine : inputLine.childrenInputs) {
                for (String inputName : getInputNames(childInputLine)) {
                    if (!inputs.contains(inputName)) {
                        inputs.add(inputName);
                    }
                }
            }
        }
        return inputs;
    }

    private void removeLine() {
        selectedLine.dispose();
        selectedLine = null;
        lineContainer.layout();
        lineScrolledComposite.setMinSize(lineContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, lineContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y-1);
        container.layout();
    }

    private String getSelectionText() {
        if (this.selection instanceof IStructuredSelection) {
            Object sel = ((IStructuredSelection) this.selection).getFirstElement();
            if (sel instanceof IFile) {
                IFile file = (IFile)sel;
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(file.getContents()));
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n"); //$NON-NLS-1$
                    }
                    return sb.toString();
                } catch (CoreException e) {
                    return ""; //$NON-NLS-1$
                } catch (IOException e) {
                    return ""; //$NON-NLS-1$
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }
        return ""; //$NON-NLS-1$
    }

    private void updatePreviews() {
        updatePreviews(false);
    }

    private void updatePreviews(boolean updateAll) {
        if (inputText == null) {
            // early update during construction
            return;
        }
        inputText.setStyleRanges(new StyleRange[] {});

        try (Scanner scanner = new Scanner(inputText.getText());) {
            scanner.useDelimiter("\n"); //$NON-NLS-1$

            int rawPos = 0;
            // skip starting delimiters
            String skip = scanner.findWithinHorizon("\\A\n+", 0); //$NON-NLS-1$
            if (skip != null) {
                rawPos += skip.length();
            }

            timeStampFormat = null;
            if (selectedLine != null) {
                for (InputGroup input : selectedLine.inputs) {
                    input.previewText.setText(Messages.CustomTxtParserInputWizardPage_noMathcingLine);
                }
            }

            Map<String, String> data = new HashMap<>();
            int rootLineMatches = 0;
            String firstEntryTimeStamp = null;
            String firstEntryTimeStampInputFormat = null;
            String log = null;
            event: while (scanner.hasNext()) {
                if (rootLineMatches > 0 && !updateAll) {
                    break;
                }
                if (log == null) {
                    log = scanner.next();
                }
                int length = log.length();
                for (InputLine rootInputLine : definition.inputs) {
                    Pattern pattern;
                    try {
                        pattern = rootInputLine.getPattern();
                    } catch (PatternSyntaxException e) {
                        continue;
                    }
                    Matcher matcher = pattern.matcher(log);
                    if (matcher.find()) {
                        rootLineMatches++;
                        inputText.setStyleRange(new StyleRange(rawPos, length,
                                COLOR_BLACK, COLOR_YELLOW, SWT.ITALIC));
                        data = new HashMap<>();
                        timeStampFormat = null;
                        updatePreviewLine(rootInputLine, matcher, data, rawPos, rootLineMatches);
                        if (rootLineMatches == 1) {
                            firstEntryTimeStamp = data.get(CustomTraceDefinition.TAG_TIMESTAMP);
                            firstEntryTimeStampInputFormat = timeStampFormat;
                        }
                        HashMap<InputLine, Integer> countMap = new HashMap<>();
                        InputLine currentInput = null;
                        if (rootInputLine.childrenInputs != null && rootInputLine.childrenInputs.size() > 0) {
                            currentInput = rootInputLine.childrenInputs.get(0);
                            countMap.put(currentInput, 0);
                        }
                        rawPos += length + 1; // +1 for \n
                        while (scanner.hasNext()) {
                            log = scanner.next();
                            length = log.length();
                            boolean processed = false;
                            if (currentInput == null) {
                                for (InputLine input : definition.inputs) {
                                    matcher = input.getPattern().matcher(log);
                                    if (matcher.find()) {
                                        continue event;
                                    }
                                }
                            } else {
                                if (countMap.get(currentInput) >= currentInput.getMinCount()) {
                                    List<InputLine> nextInputs = currentInput.getNextInputs(countMap);
                                    if (nextInputs.size() == 0 || nextInputs.get(nextInputs.size() - 1).getMinCount() == 0) {
                                        for (InputLine input : definition.inputs) {
                                            matcher = input.getPattern().matcher(log);
                                            if (matcher.find()) {
                                                continue event;
                                            }
                                        }
                                    }
                                    for (InputLine input : nextInputs) {
                                        matcher = input.getPattern().matcher(log);
                                        if (matcher.find()) {
                                            inputText.setStyleRange(new StyleRange(rawPos, length,
                                                    COLOR_BLACK, COLOR_LIGHT_YELLOW, SWT.ITALIC));
                                            currentInput = input;
                                            updatePreviewLine(currentInput, matcher, data, rawPos, rootLineMatches);
                                            if (countMap.get(currentInput) == null) {
                                                countMap.put(currentInput, 1);
                                            } else {
                                                countMap.put(currentInput, countMap.get(currentInput) + 1);
                                            }
                                            Iterator<InputLine> iter = countMap.keySet().iterator();
                                            while (iter.hasNext()) {
                                                InputLine inputLine = iter.next();
                                                if (inputLine.level > currentInput.level) {
                                                    iter.remove();
                                                }
                                            }
                                            if (currentInput.childrenInputs != null && currentInput.childrenInputs.size() > 0) {
                                                currentInput = currentInput.childrenInputs.get(0);
                                                countMap.put(currentInput, 0);
                                            } else {
                                                if (countMap.get(currentInput) >= currentInput.getMaxCount()) {
                                                    if (currentInput.getNextInputs(countMap).size() > 0) {
                                                        currentInput = currentInput.getNextInputs(countMap).get(0);
                                                        if (countMap.get(currentInput) == null) {
                                                            countMap.put(currentInput, 0);
                                                        }
                                                        iter = countMap.keySet().iterator();
                                                        while (iter.hasNext()) {
                                                            InputLine inputLine = iter.next();
                                                            if (inputLine.level > currentInput.level) {
                                                                iter.remove();
                                                            }
                                                        }
                                                    } else {
                                                        currentInput = null;
                                                    }
                                                }
                                            }
                                            processed = true;
                                            break;
                                        }
                                    }
                                }
                                if (!processed && currentInput != null) {
                                    matcher = currentInput.getPattern().matcher(log);
                                    if (matcher.find()) {
                                        inputText.setStyleRange(new StyleRange(rawPos, length,
                                                COLOR_BLACK, COLOR_LIGHT_YELLOW, SWT.ITALIC));
                                        updatePreviewLine(currentInput, matcher, data, rawPos, rootLineMatches);
                                        countMap.put(currentInput, countMap.get(currentInput) + 1);
                                        if (currentInput.childrenInputs != null && currentInput.childrenInputs.size() > 0) {
                                            currentInput = currentInput.childrenInputs.get(0);
                                            countMap.put(currentInput, 0);
                                        } else {
                                            if (countMap.get(currentInput) >= currentInput.getMaxCount()) {
                                                if (currentInput.getNextInputs(countMap).size() > 0) {
                                                    currentInput = currentInput.getNextInputs(countMap).get(0);
                                                    if (countMap.get(currentInput) == null) {
                                                        countMap.put(currentInput, 0);
                                                    }
                                                    Iterator<InputLine> iter = countMap.keySet().iterator();
                                                    while (iter.hasNext()) {
                                                        InputLine inputLine = iter.next();
                                                        if (inputLine.level > currentInput.level) {
                                                            iter.remove();
                                                        }
                                                    }
                                                } else {
                                                    currentInput = null;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            rawPos += length + 1; // +1 for \n
                        }

                        break;
                    }
                }
                rawPos += length + 1; // +1 for \n
                log = null;
            }

            if (rootLineMatches == 1) {
                firstEntryTimeStamp = data.get(CustomTraceDefinition.TAG_TIMESTAMP);
                firstEntryTimeStampInputFormat = timeStampFormat;
            }
            if (firstEntryTimeStamp == null) {
                timestampPreviewText.setText(Messages.CustomTxtParserInputWizardPage_noTimestampGroup);
                if (selectedLine != null) {
                    for (InputGroup group : selectedLine.inputs) {
                        if (group.tagCombo.getText().equals(CustomTraceDefinition.TAG_TIMESTAMP)) {
                            timestampPreviewText.setText(Messages.CustomTxtParserInputWizardPage_noMatchingTimestamp);
                            break;
                        }
                    }
                }
            } else {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(firstEntryTimeStampInputFormat);
                    Date date = dateFormat.parse(firstEntryTimeStamp);
                    dateFormat = new SimpleDateFormat(timestampOutputFormatText.getText().trim());
                    timestampPreviewText.setText(dateFormat.format(date));
                } catch (ParseException e) {
                    timestampPreviewText.setText("*parse exception* [" + firstEntryTimeStamp + "] <> [" + firstEntryTimeStampInputFormat + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                } catch (IllegalArgumentException e) {
                    timestampPreviewText.setText("*parse exception* [Illegal Argument]"); //$NON-NLS-1$
                }

            }
        }
    }

    private void updatePreviewLine(InputLine line, Matcher matcher, Map<String, String> data, int rawPos, int rootLineMatches) {
        for (int i = 0; i < line.columns.size(); i++) {
            InputData input = line.columns.get(i);
            if (i < matcher.groupCount() && matcher.group(i+1) != null) {
                if (line.parentInput == null) {
                    inputText.setStyleRange(new StyleRange(rawPos + matcher.start(i+1), matcher.end(i+1) - matcher.start(i+1),
                            COLOR_BLACK, COLOR_GREEN, SWT.BOLD));
                } else {
                    inputText.setStyleRange(new StyleRange(rawPos + matcher.start(i+1), matcher.end(i+1) - matcher.start(i+1),
                            COLOR_BLACK, COLOR_LIGHT_GREEN, SWT.BOLD));
                }
                String value = matcher.group(i+1).trim();
                if (selectedLine != null && selectedLine.inputLine.equals(line) && rootLineMatches == 1 &&
                        selectedLine.inputs.get(i).previewText.getText().equals(Messages.CustomTxtParserInputWizardPage_noMatchingLine)) {
                    selectedLine.inputs.get(i).previewText.setText(value);
                }
                if (value.length() == 0) {
                    continue;
                }
                if (input.action == CustomTraceDefinition.ACTION_SET) {
                    data.put(input.name, value);
                    if (input.name.equals(CustomTraceDefinition.TAG_TIMESTAMP)) {
                        timeStampFormat = input.format;
                    }
                } else if (input.action == CustomTraceDefinition.ACTION_APPEND) {
                    String s = data.get(input.name);
                    if (s != null) {
                        data.put(input.name, s + value);
                    } else {
                        data.put(input.name, value);
                    }
                    if (input.name.equals(CustomTraceDefinition.TAG_TIMESTAMP)) {
                        if (timeStampFormat != null) {
                            timeStampFormat += input.format;
                        } else {
                            timeStampFormat = input.format;
                        }
                    }
                } else if (input.action == CustomTraceDefinition.ACTION_APPEND_WITH_SEPARATOR) {
                    String s = data.get(input.name);
                    if (s != null) {
                        data.put(input.name, s + " | " + value); //$NON-NLS-1$
                    } else {
                        data.put(input.name, value);
                    }
                    if (input.name.equals(CustomTraceDefinition.TAG_TIMESTAMP)) {
                        if (timeStampFormat != null) {
                            timeStampFormat += " | " + input.format; //$NON-NLS-1$
                        } else {
                            timeStampFormat = input.format;
                        }
                    }
                }
            } else {
                if (selectedLine != null && selectedLine.inputLine.equals(line) && rootLineMatches == 1) {
                    if (selectedLine.inputs.get(i).previewText.getText().equals(Messages.CustomTxtParserInputWizardPage_noMatchingLine)) {
                        selectedLine.inputs.get(i).previewText.setText(Messages.CustomTxtParserInputWizardPage_noMatchingGroup);
                    }
                }
            }
        }
        // highlight the matching groups that have no corresponponding input
        for (int i = line.columns.size(); i < matcher.groupCount(); i++) {
            if (matcher.group(i+1) != null) {
                if (line.parentInput == null) {
                    inputText.setStyleRange(new StyleRange(rawPos + matcher.start(i+1), matcher.end(i+1) - matcher.start(i+1),
                            COLOR_BLACK, COLOR_MAGENTA));
                } else {
                    inputText.setStyleRange(new StyleRange(rawPos + matcher.start(i+1), matcher.end(i+1) - matcher.start(i+1),
                            COLOR_BLACK, COLOR_LIGHT_MAGENTA));
                }
            }
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
        helpBrowser.setBounds(0, 0, 600, 400);
        helpShell.pack();
        helpShell.open();
        helpBrowser.setUrl(url);
    }

    private void openLegend() {
        final String cg = Messages.CustomTxtParserInputWizardPage_capturedGroup;
        final String ucg = Messages.CustomTxtParserInputWizardPage_unidentifiedCaptureGroup;
        final String ut = Messages.CustomTxtParserInputWizardPage_uncapturedText;
        int line1start = 0;
        String line1 = Messages.CustomTxtParserInputWizardPage_nonMatchingLine;
        int line2start = line1start + line1.length();
        String line2 = Messages.CustomTxtParserInputWizardPage_matchingLineRoot + cg + ' ' + ucg + ' ' + ut + " \n";  //$NON-NLS-1$
        int line3start = line2start + line2.length();
        String line3 = Messages.CustomTxtParserInputWizardPage_matchingOtherLine + cg + ' ' + ucg + ' ' + ut + " \n"; //$NON-NLS-1$
        int line4start = line3start + line3.length();
        String line4 = Messages.CustomTxtParserInputWizardPage_matchingOtherLine + cg + ' ' + ucg + ' ' + ut + " \n"; //$NON-NLS-1$
        int line5start = line4start + line4.length();
        String line5 = Messages.CustomTxtParserInputWizardPage_nonMatchingLine;
        int line6start = line5start + line5.length();
        String line6 = Messages.CustomTxtParserInputWizardPage_matchingRootLine + cg + ' ' + ucg + ' ' + ut + " \n"; //$NON-NLS-1$

        final Shell legendShell = new Shell(getShell(), SWT.DIALOG_TRIM);
        legendShell.setLayout(new FillLayout());
        StyledText legendText = new StyledText(legendShell, SWT.MULTI);
        legendText.setFont(fixedFont);
        legendText.setText(line1 + line2 + line3 + line4 + line5 + line6);
        legendText.setStyleRange(new StyleRange(line2start, line2.length(), COLOR_BLACK, COLOR_YELLOW, SWT.ITALIC));
        legendText.setStyleRange(new StyleRange(line3start, line3.length(), COLOR_BLACK, COLOR_LIGHT_YELLOW, SWT.ITALIC));
        legendText.setStyleRange(new StyleRange(line4start, line4.length(), COLOR_BLACK, COLOR_LIGHT_YELLOW, SWT.ITALIC));
        legendText.setStyleRange(new StyleRange(line6start, line6.length(), COLOR_BLACK, COLOR_YELLOW, SWT.ITALIC));
        legendText.setStyleRange(new StyleRange(line2start + line2.indexOf(cg), cg.length(), COLOR_BLACK, COLOR_GREEN, SWT.BOLD));
        legendText.setStyleRange(new StyleRange(line2start + line2.indexOf(ucg), ucg.length(), COLOR_BLACK, COLOR_MAGENTA));
        legendText.setStyleRange(new StyleRange(line3start + line3.indexOf(cg), cg.length(), COLOR_BLACK, COLOR_LIGHT_GREEN, SWT.BOLD));
        legendText.setStyleRange(new StyleRange(line3start + line3.indexOf(ucg), ucg.length(), COLOR_BLACK, COLOR_LIGHT_MAGENTA));
        legendText.setStyleRange(new StyleRange(line4start + line4.indexOf(cg), cg.length(), COLOR_BLACK, COLOR_LIGHT_GREEN, SWT.BOLD));
        legendText.setStyleRange(new StyleRange(line4start + line4.indexOf(ucg), ucg.length(), COLOR_BLACK, COLOR_LIGHT_MAGENTA));
        legendText.setStyleRange(new StyleRange(line6start + line6.indexOf(cg), cg.length(), COLOR_BLACK, COLOR_GREEN, SWT.BOLD));
        legendText.setStyleRange(new StyleRange(line6start + line6.indexOf(ucg), ucg.length(), COLOR_BLACK, COLOR_MAGENTA));
        legendShell.setText(Messages.CustomTxtParserInputWizardPage_previewLegend);
        legendShell.pack();
        legendShell.open();
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

    private class Line {
        private static final String INFINITY_STRING = "\u221E"; //$NON-NLS-1$
        private InputLine inputLine;
        private Group group;
        private Composite labelComposite;
        private Text regexText;
        private Composite cardinalityContainer;
        private Combo cardinalityCombo;
        private Label cardinalityMinLabel;
        private Text cardinalityMinText;
        private Label cardinalityMaxLabel;
        private Text cardinalityMaxText;
        private Button infiniteButton;
        private List<InputGroup> inputs = new ArrayList<>();
        private Button addGroupButton;
        private Label addGroupLabel;

        public Line(Composite parent, String name, InputLine inputLine) {
            this.inputLine = inputLine;

            group = new Group(parent, SWT.NONE);
            group.setText(name);
            group.setLayout(new GridLayout(2, false));
            group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

            labelComposite = new Composite(group, SWT.FILL);
            GridLayout labelLayout = new GridLayout(1, false);
            labelLayout.marginWidth = 0;
            labelLayout.marginHeight = 0;
            labelComposite.setLayout(labelLayout);
            labelComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

            Label label = new Label(labelComposite, SWT.NULL);
            label.setText(Messages.CustomTxtParserInputWizardPage_regularExpression);

            Composite regexContainer = new Composite(group, SWT.NONE);
            GridLayout regexLayout = new GridLayout(2, false);
            regexLayout.marginHeight = 0;
            regexLayout.marginWidth = 0;
            regexContainer.setLayout(regexLayout);
            regexContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

            regexText = new Text(regexContainer, SWT.BORDER | SWT.SINGLE);
            GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gd.widthHint = 0;
            regexText.setLayoutData(gd);
            regexText.setText(inputLine.getRegex());
            regexText.addModifyListener(updateListener);

            Button regexHelpButton = new Button(regexContainer, SWT.PUSH);
            regexHelpButton.setImage(HELP_IMAGE);
            regexHelpButton.setToolTipText(Messages.CustomTxtParserInputWizardPage_regularExpressionHelp);
            regexHelpButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    openHelpShell(PATTERN_URL);
                }
            });

            label = new Label(group, SWT.NONE);
            label.setText(Messages.CustomTxtParserInputWizardPage_cardinality);
            label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

            cardinalityContainer = new Composite(group, SWT.NONE);
            GridLayout cardinalityLayout = new GridLayout(6, false);
            cardinalityLayout.marginHeight = 0;
            cardinalityLayout.marginWidth = 0;
            cardinalityContainer.setLayout(cardinalityLayout);
            cardinalityContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

            cardinalityCombo = new Combo(cardinalityContainer, SWT.DROP_DOWN | SWT.READ_ONLY);
            cardinalityCombo.setItems(new String[] {
                    Cardinality.ZERO_OR_MORE.toString(),
                    Cardinality.ONE_OR_MORE.toString(),
                    Cardinality.ZERO_OR_ONE.toString(),
                    Cardinality.ONE.toString(),
                    "(?,?)"}); //$NON-NLS-1$
            cardinalityCombo.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {}

                @Override
                public void widgetSelected(SelectionEvent e) {
                    switch (cardinalityCombo.getSelectionIndex()) {
                    case 4: // (?,?)
                        cardinalityMinLabel.setVisible(true);
                        cardinalityMinText.setVisible(true);
                        cardinalityMaxLabel.setVisible(true);
                        cardinalityMaxText.setVisible(true);
                        infiniteButton.setVisible(true);
                        break;
                    default:
                        cardinalityMinLabel.setVisible(false);
                        cardinalityMinText.setVisible(false);
                        cardinalityMaxLabel.setVisible(false);
                        cardinalityMaxText.setVisible(false);
                        infiniteButton.setVisible(false);
                        break;
                    }
                    cardinalityContainer.layout();
                    validate();
                    updatePreviews();
                }
            });

            cardinalityMinLabel = new Label(cardinalityContainer, SWT.NONE);
            cardinalityMinLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            cardinalityMinLabel.setText(Messages.CustomTxtParserInputWizardPage_min);
            cardinalityMinLabel.setVisible(false);

            cardinalityMinText = new Text(cardinalityContainer, SWT.BORDER | SWT.SINGLE);
            gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
            gd.widthHint = 20;
            cardinalityMinText.setLayoutData(gd);
            cardinalityMinText.setVisible(false);

            cardinalityMaxLabel = new Label(cardinalityContainer, SWT.NONE);
            cardinalityMaxLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            cardinalityMaxLabel.setText(Messages.CustomTxtParserInputWizardPage_max);
            cardinalityMaxLabel.setVisible(false);

            cardinalityMaxText = new Text(cardinalityContainer, SWT.BORDER | SWT.SINGLE);
            gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
            gd.widthHint = 20;
            cardinalityMaxText.setLayoutData(gd);
            cardinalityMaxText.setVisible(false);

            infiniteButton = new Button(cardinalityContainer, SWT.PUSH);
            infiniteButton.setText(INFINITY_STRING);
            infiniteButton.setVisible(false);
            infiniteButton.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected(SelectionEvent e) {
                    cardinalityMaxText.setText(INFINITY_STRING);
                }});

            if (inputLine.cardinality.equals(Cardinality.ZERO_OR_MORE)) {
                cardinalityCombo.select(0);
            } else if (inputLine.cardinality.equals(Cardinality.ONE_OR_MORE)) {
                cardinalityCombo.select(1);
            } else if (inputLine.cardinality.equals(Cardinality.ZERO_OR_ONE)) {
                cardinalityCombo.select(2);
            } else if (inputLine.cardinality.equals(Cardinality.ONE)) {
                cardinalityCombo.select(3);
            } else {
                cardinalityCombo.select(4);
                cardinalityMinLabel.setVisible(true);
                cardinalityMinText.setVisible(true);
                if (inputLine.getMinCount() >= 0) {
                    cardinalityMinText.setText(Integer.toString(inputLine.getMinCount()));
                }
                cardinalityMaxLabel.setVisible(true);
                cardinalityMaxText.setVisible(true);
                if (inputLine.getMaxCount() == Cardinality.INF) {
                    cardinalityMaxText.setText(INFINITY_STRING);
                } else if (inputLine.getMaxCount() >= 0) {
                    cardinalityMaxText.setText(Integer.toString(inputLine.getMaxCount()));
                }
                infiniteButton.setVisible(true);
            }

            VerifyListener digitsListener = new VerifyListener() {
            	@Override
                public void verifyText(VerifyEvent e) {
                    if (e.text.equals(INFINITY_STRING)) {
                        e.doit = e.widget == cardinalityMaxText && e.start == 0 && e.end == ((Text) e.widget).getText().length();
                    } else {
                        if (((Text) e.widget).getText().equals(INFINITY_STRING)) {
                            e.doit = e.start == 0 && e.end == ((Text) e.widget).getText().length();
                        }
                        for (int i = 0; i < e.text.length(); i++) {
                            if (!Character.isDigit(e.text.charAt(i))) {
                                e.doit = false;
                                break;
                            }
                        }
                    }
                }};

            cardinalityMinText.addModifyListener(updateListener);
            cardinalityMaxText.addModifyListener(updateListener);
            cardinalityMinText.addVerifyListener(digitsListener);
            cardinalityMaxText.addVerifyListener(digitsListener);

            if (inputLine.columns != null) {
                for (InputData inputData : inputLine.columns) {
                    InputGroup inputGroup = new InputGroup(group, this, inputs.size()+1);
                    if (inputData.name.equals(CustomTraceDefinition.TAG_TIMESTAMP)) {
                        inputGroup.tagCombo.select(0);
                        inputGroup.tagText.setText(inputData.format);
                        inputGroup.tagLabel.setText(Messages.CustomTxtParserInputWizardPage_format);
                        inputGroup.tagLabel.setVisible(true);
                        inputGroup.tagText.setVisible(true);
                        inputGroup.tagText.addModifyListener(updateListener);
                    } else if (inputData.name.equals(CustomTraceDefinition.TAG_MESSAGE)) {
                        inputGroup.tagCombo.select(1);
                    } else {
                        inputGroup.tagCombo.select(2);
                        inputGroup.tagText.setText(inputData.name);
                        inputGroup.tagLabel.setText(Messages.CustomTxtParserInputWizardPage_name);
                        inputGroup.tagLabel.setVisible(true);
                        inputGroup.tagText.setVisible(true);
                        inputGroup.tagText.addModifyListener(updateListener);
                    }
                    inputGroup.actionCombo.select(inputData.action);
                    inputs.add(inputGroup);
                }
            }

            createAddGroupButton();
        }

        private void createAddGroupButton() {
            addGroupButton = new Button(group, SWT.PUSH);
            addGroupButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            addGroupButton.setImage(ADD_IMAGE);
            addGroupButton.setToolTipText(Messages.CustomTxtParserInputWizardPage_addGroup);
            addGroupButton.addSelectionListener(new SelectionAdapter() {
            	@Override
                public void widgetSelected(SelectionEvent e) {
                    removeAddGroupButton();
                    inputs.add(new InputGroup(group, Line.this, inputs.size()+1));
                    createAddGroupButton();
                    lineContainer.layout();
                    lineScrolledComposite.setMinSize(lineContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, lineContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y-1);
                    group.getParent().layout();
                    validate();
                    updatePreviews();
                }
            });

            addGroupLabel = new Label(group, SWT.NULL);
            addGroupLabel.setText(Messages.CustomTxtParserInputWizardPage_newGroup);
        }

        private void removeAddGroupButton() {
            addGroupButton.dispose();
            addGroupLabel.dispose();
        }

        private void removeInput(int inputNumber) {
            int nb = inputNumber;
            if (--nb < inputs.size()) {
                inputs.remove(nb).dispose();
                for (int i = nb; i < inputs.size(); i++) {
                    inputs.get(i).setInputNumber(i+1);
                }
                lineContainer.layout();
                lineScrolledComposite.setMinSize(lineContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, lineContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y-1);
                group.getParent().layout();
            }
        }

        private void dispose() {
            group.dispose();
        }

        private void extractInputs() {
            inputLine.setRegex(selectedLine.regexText.getText());
            switch (cardinalityCombo.getSelectionIndex()) {
            case 0:
                inputLine.cardinality = Cardinality.ZERO_OR_MORE;
                break;
            case 1:
                inputLine.cardinality = Cardinality.ONE_OR_MORE;
                break;
            case 2:
                inputLine.cardinality = Cardinality.ZERO_OR_ONE;
                break;
            case 3:
                inputLine.cardinality = Cardinality.ONE;
                break;
            case 4: //(?,?)
                int min, max;
                try {
                    min = Integer.parseInt(cardinalityMinText.getText());
                } catch (NumberFormatException e) {
                    min = -1;
                }
                try {
                    if (cardinalityMaxText.getText().equals(INFINITY_STRING)) {
                        max = Cardinality.INF;
                    } else {
                        max = Integer.parseInt(cardinalityMaxText.getText());
                    }
                } catch (NumberFormatException e) {
                    max = -1;
                }
                inputLine.cardinality = new Cardinality(min, max);
                break;
            default:
                inputLine.cardinality = Cardinality.ZERO_OR_MORE;
                break;
            }
            inputLine.columns = new ArrayList<>(inputs.size());
            for (int i = 0; i < inputs.size(); i++) {
                InputGroup grp = inputs.get(i);
                InputData inputData = new InputData();
                if (grp.tagCombo.getText().equals(CustomTraceDefinition.TAG_OTHER)) {
                    inputData.name = grp.tagText.getText().trim();
                } else {
                    inputData.name = grp.tagCombo.getText();
                    if (grp.tagCombo.getText().equals(CustomTraceDefinition.TAG_TIMESTAMP)) {
                        inputData.format = grp.tagText.getText().trim();
                    }
                }
                inputData.action = grp.actionCombo.getSelectionIndex();
                inputLine.columns.add(inputData);
            }
        }
    }

    private class InputGroup {
        private Line line;
        private int inputNumber;

        // children of parent (must be disposed)
        private Composite labelComposite;
        private Composite tagComposite;
        private Label previewLabel;
        private Text previewText;

        // children of labelComposite
        private Label inputLabel;

        // children of tagComposite
        private Combo tagCombo;
        private Label tagLabel;
        private Text tagText;
        private Combo actionCombo;

        public InputGroup(Composite parent, Line line, int inputNumber) {
            this.line = line;
            this.inputNumber = inputNumber;

            labelComposite = new Composite(parent, SWT.FILL);
            GridLayout labelLayout = new GridLayout(2, false);
            labelLayout.marginWidth = 0;
            labelLayout.marginHeight = 0;
            labelComposite.setLayout(labelLayout);
            labelComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

            Button deleteButton = new Button(labelComposite, SWT.PUSH);
            deleteButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            deleteButton.setImage(DELETE_IMAGE);
            deleteButton.setToolTipText(Messages.CustomTxtParserInputWizardPage_removeGroup);
            deleteButton.addSelectionListener(new SelectionAdapter() {
            	@Override
                public void widgetSelected(SelectionEvent e) {
                    InputGroup.this.line.removeInput(InputGroup.this.inputNumber);
                    validate();
                    updatePreviews();
                }
            });

            inputLabel = new Label(labelComposite, SWT.NULL);
            inputLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            inputLabel.setText(Messages.CustomTxtParserInputWizardPage_group + inputNumber + ":"); //$NON-NLS-1$

            tagComposite = new Composite(parent, SWT.FILL);
            GridLayout tagLayout = new GridLayout(4, false);
            tagLayout.marginWidth = 0;
            tagLayout.marginHeight = 0;
            tagComposite.setLayout(tagLayout);
            tagComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

            tagCombo = new Combo(tagComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
            tagCombo.setItems(new String[] {CustomTraceDefinition.TAG_TIMESTAMP,
                                            CustomTraceDefinition.TAG_MESSAGE,
                                            CustomTraceDefinition.TAG_OTHER});
            tagCombo.select(1);
            tagCombo.addSelectionListener(new SelectionListener(){
            	@Override
                public void widgetDefaultSelected(SelectionEvent e) {}
            	@Override
                public void widgetSelected(SelectionEvent e) {
                    tagText.removeModifyListener(updateListener);
                    switch (tagCombo.getSelectionIndex()) {
                    case 0: //Time Stamp
                        tagLabel.setText(Messages.CustomTxtParserInputWizardPage_format);
                        tagLabel.setVisible(true);
                        tagText.setVisible(true);
                        tagText.addModifyListener(updateListener);
                        break;
                    case 1: //Message
                        tagLabel.setVisible(false);
                        tagText.setVisible(false);
                        break;
                    case 2: //Other
                        tagLabel.setText(Messages.CustomTxtParserInputWizardPage_name);
                        tagLabel.setVisible(true);
                        tagText.setVisible(true);
                        tagText.addModifyListener(updateListener);
                        break;
                    case 3: //Continue
                        tagLabel.setVisible(false);
                        tagText.setVisible(false);
                        break;
                    default:
                        break;
                    }
                    tagComposite.layout();
                    validate();
                    updatePreviews();
                }});

            tagLabel = new Label(tagComposite, SWT.NULL);
            tagLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            tagLabel.setVisible(false);

            tagText = new Text(tagComposite, SWT.BORDER | SWT.SINGLE);
            GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gd.widthHint = 0;
            tagText.setLayoutData(gd);
            tagText.setVisible(false);

            actionCombo = new Combo(tagComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
            actionCombo.setItems(new String[] {Messages.CustomTxtParserInputWizardPage_set, Messages.CustomTxtParserInputWizardPage_append, Messages.CustomTxtParserInputWizardPage_appendWith});
            actionCombo.select(0);
            actionCombo.addSelectionListener(updateListener);

            previewLabel = new Label(parent, SWT.NULL);
            previewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            previewLabel.setText(Messages.CustomTxtParserInputWizardPage_preview);

            previewText = new Text(parent, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
            gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gd.widthHint = 0;
            previewText.setLayoutData(gd);
            previewText.setText(Messages.CustomTxtParserInputWizardPage_noMatch);
            previewText.setBackground(COLOR_WIDGET_BACKGROUND);
        }

        private void dispose() {
            labelComposite.dispose();
            tagComposite.dispose();
            previewLabel.dispose();
            previewText.dispose();
        }

        private void setInputNumber(int inputNumber) {
            this.inputNumber = inputNumber;
            inputLabel.setText(Messages.CustomTxtParserInputWizardPage_group + inputNumber + ":"); //$NON-NLS-1$
            labelComposite.layout();
        }
    }

    private void validate() {

        definition.definitionName = logtypeText.getText().trim();
        definition.timeStampOutputFormat = timestampOutputFormatText.getText().trim();

        if (selectedLine != null) {
            selectedLine.extractInputs();
            treeViewer.refresh();
        }

        StringBuffer errors = new StringBuffer();

        if (definition.definitionName.length() == 0) {
            errors.append("Enter a name for the new log type. "); //$NON-NLS-1$
            logtypeText.setBackground(COLOR_LIGHT_RED);
        } else {
            logtypeText.setBackground(COLOR_TEXT_BACKGROUND);
            for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
                if (definition.definitionName.equals(def.definitionName) &&
                        (editDefinitionName == null || !editDefinitionName.equals(definition.definitionName))) {
                    errors.append("The log type name already exists. "); //$NON-NLS-1$
                    logtypeText.setBackground(COLOR_LIGHT_RED);
                    break;
                }
            }
        }

        timestampFound = false;
        for (int i = 0; i < definition.inputs.size(); i++) {

            InputLine inputLine = definition.inputs.get(i);
            String name = Integer.toString(i+1);
            errors.append(validateLine(inputLine, name));
        }
        if (timestampFound) {
            if (definition.timeStampOutputFormat.length() == 0) {
                errors.append("Enter the output format for the Time Stamp field. "); //$NON-NLS-1$
                timestampOutputFormatText.setBackground(COLOR_LIGHT_RED);
            } else {
                try {
                    new SimpleDateFormat(definition.timeStampOutputFormat);
                    timestampOutputFormatText.setBackground(COLOR_TEXT_BACKGROUND);
                } catch (IllegalArgumentException e) {
                    errors.append("Enter a valid output format for the Time Stamp field. "); //$NON-NLS-1$
                    timestampOutputFormatText.setBackground(COLOR_LIGHT_RED);
                }
            }

        } else {
            timestampOutputFormatText.setBackground(COLOR_TEXT_BACKGROUND);
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
     * Validate an input line.
     *
     * @param inputLine
     *            The line to clean up
     * @param name
     *            The name of the line
     * @return The cleaned up line
     */
    public StringBuffer validateLine(InputLine inputLine, String name) {
        StringBuffer errors = new StringBuffer();
        Line line = null;
        if (selectedLine != null && selectedLine.inputLine.equals(inputLine)) {
            line = selectedLine;
        }
        try {
            Pattern.compile(inputLine.getRegex());
            if (line != null) {
                line.regexText.setBackground(COLOR_TEXT_BACKGROUND);
            }
        } catch (PatternSyntaxException e) {
            errors.append("Enter a valid regular expression (Line "+name+"). "); //$NON-NLS-1$ //$NON-NLS-2$
            if (line != null) {
                line.regexText.setBackground(COLOR_LIGHT_RED);
            }
        }
        if (inputLine.getMinCount() == -1) {
            errors.append("Enter a minimum value for cardinality (Line "+name+"). "); //$NON-NLS-1$ //$NON-NLS-2$
            if (line != null) {
                line.cardinalityMinText.setBackground(COLOR_LIGHT_RED);
            }
        } else {
            if (line != null) {
                line.cardinalityMinText.setBackground(COLOR_TEXT_BACKGROUND);
            }
        }
        if (inputLine.getMaxCount() == -1) {
            errors.append("Enter a maximum value for cardinality (Line "+name+"). "); //$NON-NLS-1$ //$NON-NLS-2$
            if (line != null) {
                line.cardinalityMaxText.setBackground(COLOR_LIGHT_RED);
            }
        } else if (inputLine.getMinCount() > inputLine.getMaxCount()) {
            errors.append("Enter correct (min <= max) values for cardinality (Line "+name+"). "); //$NON-NLS-1$ //$NON-NLS-2$
            if (line != null) {
                line.cardinalityMinText.setBackground(COLOR_LIGHT_RED);
            }
            if (line != null) {
                line.cardinalityMaxText.setBackground(COLOR_LIGHT_RED);
            }
        } else {
            if (line != null) {
                line.cardinalityMaxText.setBackground(COLOR_TEXT_BACKGROUND);
            }
        }
        for (int i = 0; inputLine.columns != null && i < inputLine.columns.size(); i++) {
            InputData inputData = inputLine.columns.get(i);
            InputGroup group = null;
            if (line != null) {
                group = line.inputs.get(i);
            }
            if (inputData.name.equals(CustomTraceDefinition.TAG_TIMESTAMP)) {
                timestampFound = true;
                if (inputData.format.length() == 0) {
                    errors.append("Enter the input format for the Time Stamp (Line "+name+" Group "+(i+1)+"). "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    if (group != null) {
                        group.tagText.setBackground(COLOR_LIGHT_RED);
                    }
                } else {
                    try {
                        new SimpleDateFormat(inputData.format);
                        if (group != null) {
                            group.tagText.setBackground(COLOR_TEXT_BACKGROUND);
                        }
                    } catch (IllegalArgumentException e) {
                        errors.append("Enter a valid input format for the Time Stamp (Line "+name+" Group "+(i+1)+"). "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        if (group != null) {
                            group.tagText.setBackground(COLOR_LIGHT_RED);
                        }
                    }
                }
            } else if (inputData.name.length() == 0) {
                errors.append("Enter a name for the data group (Line "+name+" Group "+(i+1)+"). "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                if (group != null) {
                    group.tagText.setBackground(COLOR_LIGHT_RED);
                }
            } else {
                if (group != null) {
                    group.tagText.setBackground(COLOR_TEXT_BACKGROUND);
                }
            }
        }
        for (int i = 0; inputLine.childrenInputs != null && i < inputLine.childrenInputs.size(); i++) {
            errors.append(validateLine(inputLine.childrenInputs.get(i), name+"."+(i+1))); //$NON-NLS-1$
        }
        return errors;
    }

    /**
     * Get the trace definition.
     *
     * @return The trace definition
     */
    public CustomTxtTraceDefinition getDefinition() {
        return definition;
    }

    /**
     * Get the raw text of the input.
     *
     * @return The raw input text
     */
    public char[] getInputText() {
        return inputText.getText().toCharArray();
    }
}
