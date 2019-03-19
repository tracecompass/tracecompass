/*******************************************************************************
 * Copyright (c) 2017, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers;

import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfUIPreferences;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphTooltipHandler;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.primitives.Longs;

/**
 * Abstract tool tip handler.
 *
 * @since 3.2
 * @author Loic Prieur-Drevon - extracted from {@link TimeGraphTooltipHandler}
 */
public abstract class TmfAbstractToolTipHandler {
    private static final int MAX_SHELL_WIDTH = 750;
    private static final int MAX_SHELL_HEIGHT = 700;
    private static final int MOUSE_DEADZONE = 5;
    private static final String TIME_PREFIX = "time://"; //$NON-NLS-1$
    private static final Pattern TIME_PATTERN = Pattern.compile("\\s*time\\:\\/\\/(\\d+).*"); //$NON-NLS-1$

    private static final String UNCATEGORIZED = ""; //$NON-NLS-1$
    private static final int OFFSET = 16;
    private static Point fScrollBarSize = null;
    private Composite fTipComposite;
    private Shell fTipShell;
    private Rectangle fInitialDeadzone;
    private Table<String, String, HyperLink> fModel = HashBasedTable.create();

    private static synchronized boolean isBrowserAvailable(Composite parent) {
        boolean isBrowserAvailable = Activator.getDefault().getPreferenceStore().getBoolean(ITmfUIPreferences.USE_BROWSER_TOOLTIPS);
        if (isBrowserAvailable) {
            try {
                getScrollbarSize(parent);

                Browser browser = new Browser(parent, SWT.NONE);
                browser.dispose();
                isBrowserAvailable = true;
            } catch (SWTError er) {
                isBrowserAvailable = false;
            }
        }
        return isBrowserAvailable;
    }

    private static synchronized Point getScrollbarSize(Composite parent) {
        if (fScrollBarSize == null) {
            // Don't move these lines below the new Browser() line
            Slider sliderV = new Slider(parent, SWT.VERTICAL);
            Slider sliderH = new Slider(parent, SWT.HORIZONTAL);
            int width = sliderV.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
            int height = sliderH.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
            Point scrollBarSize = new Point(width, height);
            sliderV.dispose();
            sliderH.dispose();
            fScrollBarSize = scrollBarSize;
        }
        return fScrollBarSize;
    }

    /**
     * Important note: this is being added to a display filter, this may leak,
     * make sure it is removed when not needed.
     */
    private final Listener fListener = this::disposeIfExited;
    private final Listener fFocusLostListener = event -> {
        Shell tipShell = fTipShell;
        // Don't dispose if the tooltip is clicked.
        if (tipShell != null && event.display.getActiveShell() != tipShell) {
            tipShell.dispose();
        }
    };

    /**
     * Dispose the shell if we exit the range.
     *
     * @param e
     *            The event which occurred
     */
    private void disposeIfExited(Event e) {
        if (!(e.widget instanceof Control)) {
            return;
        }
        Control control = (Control) e.widget;
        if (control != null && !control.isDisposed()) {
            Point pt = control.toDisplay(e.x, e.y);
            Shell tipShell = fTipShell;
            if (tipShell != null && !tipShell.isDisposed()) {
                Rectangle bounds = tipShell.getBounds();
                bounds.x -= OFFSET;
                bounds.y -= OFFSET;
                bounds.height += 2 * OFFSET;
                bounds.width += 2 * OFFSET;
                if (!bounds.contains(pt) && !fInitialDeadzone.contains(pt)) {
                    tipShell.dispose();
                }
            }
        }
    }

    /**
     * Callback for the mouse-over tooltip
     *
     * @param control
     *            The control object to use
     */
    public void activateHoverHelp(final Control control) {

        control.addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseHover(MouseEvent event) {
                // Is application not in focus?
                // -OR- a mouse button is pressed
                if (Display.getDefault().getFocusControl() == null
                        || (event.stateMask & SWT.BUTTON_MASK) != 0
                        || (event.stateMask & SWT.KEY_MASK) != 0) {
                    return;
                }
                Point pt = new Point(event.x, event.y);
                Control timeGraphControl = (Control) event.widget;
                Point ptInDisplay = control.toDisplay(event.x, event.y);
                fInitialDeadzone = new Rectangle(ptInDisplay.x - MOUSE_DEADZONE, ptInDisplay.y - MOUSE_DEADZONE, 2 * MOUSE_DEADZONE, 2 * MOUSE_DEADZONE);
                createTooltipShell(timeGraphControl.getShell(), control, event, pt);
                if (fTipComposite.getChildren().length == 0) {
                    // avoid displaying empty tool tips.
                    return;
                }
                Point tipPosition = control.toDisplay(pt);
                setHoverLocation(fTipShell, tipPosition);
                fTipShell.setVisible(true);
                // Register Display filters.
                Display display = Display.getDefault();
                display.addFilter(SWT.MouseMove, fListener);
                display.addFilter(SWT.FocusOut, fFocusLostListener);
            }
        });
    }

    /**
     * Create the tooltip shell.
     *
     * @param parent
     *            the parent shell
     * @param control
     *            the underlying control
     * @param event
     *            the mouse event to react to
     * @param pt
     *            the mouse hover position in the control's coordinates
     */
    private void createTooltipShell(Shell parent, Control control, MouseEvent event, Point pt) {
        final Display display = parent.getDisplay();
        if (fTipShell != null && !fTipShell.isDisposed()) {
            fTipShell.dispose();
        }
        fModel.clear();
        fTipShell = new Shell(parent, SWT.ON_TOP | SWT.TOOL | SWT.RESIZE | SWT.NO_SCROLL);
        // Deregister display filters on dispose
        fTipShell.addDisposeListener(e -> e.display.removeFilter(SWT.MouseMove, fListener));
        fTipShell.addDisposeListener(e -> e.display.removeFilter(SWT.FocusOut, fFocusLostListener));
        fTipShell.addListener(SWT.Deactivate, e -> {
            if (!fTipShell.isDisposed()) {
                fTipShell.dispose();
            }
        });
        fTipShell.setLayout(new FillLayout());
        fTipShell.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

        fTipComposite = new Composite(fTipShell, SWT.NO_FOCUS);
        fTipComposite.setLayout(new FillLayout());
        fill(control, event, pt);

        ITooltipContent content = null;
        if (isBrowserAvailable(fTipComposite)) {
            content = new BrowserContent(fTipComposite);
        } else {
            content = new DefaultContent(fTipComposite);
        }
        content.setInput(fModel);
        content.create();
        Point p = content.computePreferredSize();
        Rectangle t = fTipShell.computeTrim(0, 0, p.x, p.y);
        fTipShell.setSize(Math.min(t.width, MAX_SHELL_WIDTH), Math.min(t.height, MAX_SHELL_HEIGHT));
    }

    private static void setHoverLocation(Shell shell, Point position) {
        Rectangle displayBounds = shell.getDisplay().getBounds();
        Rectangle shellBounds = shell.getBounds();
        if (position.x + shellBounds.width + OFFSET > displayBounds.width && position.x - shellBounds.width - OFFSET >= 0) {
            shellBounds.x = position.x - shellBounds.width - OFFSET;
        } else {
            shellBounds.x = Math.max(Math.min(position.x + OFFSET, displayBounds.width - shellBounds.width), 0);
        }
        if (position.y + shellBounds.height + OFFSET > displayBounds.height && position.y - shellBounds.height - OFFSET >= 0) {
            shellBounds.y = position.y - shellBounds.height - OFFSET;
        } else {
            shellBounds.y = Math.max(Math.min(position.y + OFFSET, displayBounds.height - shellBounds.height), 0);
        }
        shell.setBounds(shellBounds);
    }

    /**
     * Getter for the current underlying tip {@link Composite}
     *
     * @return the current underlying tip {@link Composite}
     */
    protected Composite getTipComposite() {
        return fTipComposite;
    }

    /**
     * Method to call to add tuples : name, value to the tooltip. It has
     * the possibility to add a time value for creating a hyperlink in the
     * tooltip.
     *
     * @param category
     *            the category of the item (used for grouping)
     * @param name
     *            name of the line
     * @param label
     *            label to display representing the time or line value
     * @param time
     *            time in nanoseconds, if time value else null
     * @since 5.0
     */
    protected void addItem(String category, String name, String label, Long time) {
        fModel.put(category == null ? UNCATEGORIZED : category, name, new HyperLink(label, time));
    }

    /**
     * Method to call to add tuples : name, value to the tooltip.
     *
     * @param name
     *            name of the line
     * @param value
     *            line value
     * @deprecated use {@link #addItem(String, String, String, Long)} to
     *             have categories and/or time
     */
    @Deprecated
    protected void addItem(String name, String value) {
        addItem(null, name, value, null);
    }

    /**
     * Abstract method to override within implementations. Call
     * {@link TmfAbstractToolTipHandler#addItem(String, String)} to populate the
     * tool tip.
     *
     * @param control
     *            the underlying control
     * @param event
     *            the mouse event to react to
     * @param pt
     *            the mouse hover position in the control's coordinates
     */
    protected abstract void fill(Control control, MouseEvent event, Point pt);

    // Stopgap interface
    private static class HyperLink {
        private String fLabel;
        // TODO: use a real interface
        private @Nullable Object fLink;

        public HyperLink(String label, @Nullable Object link) {
            fLabel = label;
            fLink = link;
        }

        /**
         * Get the label
         *
         * @return the label
         */
        public String getLabel() {
            return fLabel;
        }

        /**
         * Get the link
         *
         * @return the link, can be null
         */
        public Object getLink() {
            return fLink;
        }

        @SuppressWarnings("nls")
        @Override
        public String toString() {
            Object link = getLink();
            String label = getLabel();
            if (link == null) {
                return label;
            }
            return "<a href=" + TIME_PREFIX + link + ">" + label + "</a>";
        }
    }

    private interface ITooltipContent {
        void create();
        void setInput(Table<String, String, HyperLink> model);
        Point computePreferredSize();

        default void setupControl(Control control) {
            control.setForeground(control.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
            control.setBackground(control.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        }
    }

    private class BrowserContent extends AbstractContent {
        private static final int MARGIN = 10;
        private static final int CHAR_WIDTH = 9;
        private static final int LINE_HEIGHT = 18;

        public BrowserContent(Composite parent) {
            super(parent);
        }

        @Override
        public void create() {
            Composite parent = getParent();
            Table<String, String, HyperLink> model = getModel();
            if (parent == null || model.size() == 0) {
                return;
            }
            setupControl(parent);
            ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
            scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            scrolledComposite.setExpandVertical(true);
            scrolledComposite.setExpandHorizontal(true);

            Browser browser = new Browser(scrolledComposite, SWT.NONE);
            browser.setJavascriptEnabled(false);
            browser.addLocationListener(new LocationListener() {
                @Override
                public void changing(LocationEvent ev) {
                    Matcher matcher = TIME_PATTERN.matcher(ev.location);
                    if (matcher.find()) {
                        String time = matcher.group(1);
                        Long val = Longs.tryParse(time);
                        if (val != null) {
                            TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(ev.getSource(), TmfTimestamp.fromNanos(val)));
                        }
                        ev.doit = false;
                    }
                }
                @Override
                public void changed(LocationEvent ev) {
                    // Ignore
                }
            });
            setupControl(browser);

            String toolTipHtml = toHtml();
            browser.setText(toolTipHtml);
            scrolledComposite.setContent(browser);
            Point preferredSize = computePreferredSize();
            Point scrollBarSize = getScrollbarSize(scrolledComposite);
            scrolledComposite.setMinSize(Math.min(preferredSize.x > scrollBarSize.x ? preferredSize.x - scrollBarSize.x : preferredSize.x, MAX_SHELL_WIDTH - scrollBarSize.x), 0);
        }

        @Override
        public Point computePreferredSize() {
            Table<String, String, HyperLink> model = getModel();
            int elementCount = model.size();
            int longestString = 0;
            int longestValueString = 0;
            Set<String> rowKeySet = model.rowKeySet();
            for (String row : rowKeySet) {
                Set<@NonNull Entry<String, HyperLink>> entrySet = model.row(row).entrySet();
                for (Entry<String, HyperLink> entry : entrySet) {
                    longestString = Math.max(longestString, entry.getKey().length() + 2);
                    longestValueString = Math.max(longestValueString, + entry.getValue().getLabel().length() + 2);
                }
            }
            int noCat = rowKeySet.size();
            if (model.containsRow(UNCATEGORIZED)) {
                // don't count UNCATEGORIZED because it's not drawn as header
                noCat -= (noCat > 0 ? 1 : 0);
            }
            int w = (longestString + longestValueString) * CHAR_WIDTH + 2 * 2 * MARGIN;
            int h = elementCount * LINE_HEIGHT + noCat * LINE_HEIGHT + 2 * MARGIN;
            return new Point(w, h);
        }

        @SuppressWarnings("nls")
        private String toHtml() {
            Table<String, String, HyperLink> model = getModel();
            StringBuilder toolTipContent = new StringBuilder();
            toolTipContent.append("<head>\n" +
                    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                    "<style>\n" +
                    ".collapsible {\n" +
                    "  background-color: #777;\n" +
                    "  color: white;\n" +
//                    "  cursor: pointer;\n" + // Add when enabling JavaScript
                    "  padding: 0px;\n" +
                    "  width: 100%;\n" +
                    "  border: none;\n" +
                    "  text-align: left;\n" +
                    "  outline: none;\n" +
                    "  font-size: 12px;\n" +
                    "}\n" +
                    "\n" +
                    ".active, .collapsible:hover {\n" +
                    "  background-color: #555;\n" +
                    "}\n" +
                    "\n" +
                    ".content {\n" +
                    "  padding: 0px 0px;\n" +
                    "  display: block;\n" +
                    "  overflow: hidden;\n" +
                    "  background-color: #f1f1f1;\n" +
                    "}\n" +
                    ".tab {\n" +
                    "  font-size: 14px;\n" +
                    "}\n" +
                    ".paddingBetweenCols {\n" +
                    "  padding:0px 10px 0px 10px;\n" +
                    "}\n" +
                    ".bodystyle {\n" +
                    "  padding:0px 0px;\n" +
                    "}\n" +
                    "</style>\n" +
                    "</head>");
            toolTipContent.append("<body class=\"bodystyle\">"); //$NON-NLS-1$

            Set<String> rowKeySet = model.rowKeySet();
            for (String row : rowKeySet) {
                if (!row.equals(UNCATEGORIZED)) {
                    toolTipContent.append("<button class=\"collapsible\">").append(row).append("</button>");
                }
                toolTipContent.append("<div class=\"content\">");
                toolTipContent.append("<table class=\"tab\">");
                Set<@NonNull Entry<String, HyperLink>> entrySet = model.row(row).entrySet();
                for (Entry<String, HyperLink> entry : entrySet) {
                    toolTipContent.append("<tr>");
                    toolTipContent.append("<td class=\"paddingBetweenCols\">");
                    toolTipContent.append(entry.getKey());
                    toolTipContent.append("</td>");
                    toolTipContent.append("<td class=\"paddingBetweenCols\">");
                    toolTipContent.append(entry.getValue());
                    toolTipContent.append("</td>");
                    toolTipContent.append("</tr>");
                }
                toolTipContent.append("</table></div>");
            }
            /* Add when enabling JavaScript
            toolTipContent.append("\n" +
                    "<script>\n" +
                    "var coll = document.getElementsByClassName(\"collapsible\");\n" +
                    "var i;\n" +
                    "\n" +
                    "for (i = 0; i < coll.length; i++) {\n" +
                    "  coll[i].addEventListener(\"click\", function() {\n" +
                    "    this.classList.toggle(\"active\");\n" +
                    "    var content = this.nextElementSibling;\n" +
                    "    if (content.style.display === \"block\") {\n" +
                    "      content.style.display = \"none\";\n" +
                    "    } else {\n" +
                    "      content.style.display = \"block\";\n" +
                    "    }\n" +
                    "  });\n" +
                    "}\n" +
                    "</script>");
            */
            toolTipContent.append("</body>"); //$NON-NLS-1$
            return toolTipContent.toString();
        }
    }

    private class DefaultContent extends AbstractContent {
        public DefaultContent(Composite parent) {
            super(parent);
        }

        @Override
        public void create() {
            Composite parent = getParent();
            Table<String, String, HyperLink> model = getModel();
            if (parent == null || model.size() == 0) {
                return;
            }
            setupControl(parent);
            parent.setLayout(new GridLayout());

            ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
            scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            scrolledComposite.setExpandVertical(true);
            scrolledComposite.setExpandHorizontal(true);
            setupControl(scrolledComposite);

            Composite composite = new Composite(scrolledComposite, SWT.NONE);
            composite.setLayout(new GridLayout(3, false));
            setupControl(composite);
            Set<String> rowKeySet = model.rowKeySet();
            for (String row : rowKeySet) {
                Set<@NonNull Entry<String, HyperLink>> entrySet = model.row(row).entrySet();
                for (Entry<String, HyperLink> entry : entrySet) {
                    Label nameLabel = new Label(composite, SWT.NO_FOCUS);
                    nameLabel.setText(entry.getKey());
                    setupControl(nameLabel);
                    Label separator = new Label(composite, SWT.NO_FOCUS | SWT.SEPARATOR | SWT.VERTICAL);
                    GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
                    gd.heightHint = nameLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
                    separator.setLayoutData(gd);
                    setupControl(separator);
                    Label valueLabel = new Label(composite, SWT.NO_FOCUS);
                    valueLabel.setText(entry.getValue().getLabel());
                    setupControl(valueLabel);
                }
            }
            scrolledComposite.setContent(composite);
            Point preferredSize = computePreferredSize();
            Point scrollBarSize = getScrollbarSize(composite);
            scrolledComposite.setMinSize(preferredSize.x > scrollBarSize.x ? preferredSize.x - scrollBarSize.x : preferredSize.x, preferredSize.y > scrollBarSize.y ? preferredSize.y - scrollBarSize.y : preferredSize.y);
        }
    }

    private abstract class AbstractContent implements ITooltipContent {
        private Composite fParent = null;
        private Table<String, String, HyperLink> fContentModel = null;

        public AbstractContent(Composite parent) {
            fParent = parent;
        }

        @Override
        public void setInput(Table<String, String, HyperLink> model) {
            fContentModel = model;
        }

        @NonNull
        protected Table<String, String, HyperLink> getModel() {
            Table<String, String, HyperLink> model = fContentModel;
            if (model == null) {
                model = HashBasedTable.create();
            }
            return model;
        }

        @Override
        public Point computePreferredSize() {
            Composite parent = fParent;
            if (parent == null) {
                return new Point(0, 0);
            }
            return parent.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        }

        protected Composite getParent() {
            return fParent;
        }
    }

}
