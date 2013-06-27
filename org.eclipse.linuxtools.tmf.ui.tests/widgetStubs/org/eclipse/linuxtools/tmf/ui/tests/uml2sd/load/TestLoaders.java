/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.uml2sd.load;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.AsyncMessageReturn;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.BasicExecutionOccurrence;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.EllipsisMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.ExecutionOccurrence;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Frame;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.HotSpot;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Lifeline;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.LifelineCategories;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Stop;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.SyncMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.SyncMessageReturn;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.Criteria;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.FilterCriteria;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IImage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDFilterProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDFindProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDGraphNodeSupporter;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDPagingProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.load.IUml2SDLoader;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * Test loader class.
 */
@SuppressWarnings("javadoc")
public class TestLoaders implements IUml2SDLoader, ISDFindProvider, ISDFilterProvider, ISDPagingProvider, ISelectionListener {

    public SDView v;
    public int page;
    private List<GraphNode> findResults = new ArrayList<GraphNode>();
    private Criteria findCriteria;
    private int currentFindIndex = 0;

    private Frame savedFrame = null;

    public TestLoaders() {
        this("");
    }

    /**
     * Constructor
     *
     * @param name
     */
    public TestLoaders(String name) {
        page = 1;
    }

    @Override
    public void setViewer(SDView j) {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().addPostSelectionListener(this);
        v = j;
        v.setSDPagingProvider(this);
        v.setSDFindProvider(this);
        v.setSDFilterProvider(this);
        page = 1;
        createFrame();
    }

    @Override
    public boolean hasNextPage() {
        return page == 1;
    }

    @Override
    public boolean hasPrevPage() {
        return page == 2;
    }

    @Override
    public void prevPage() {
        page--;
        createFrame();
    }

    @Override
    public void nextPage() {
        page++;
        createFrame();
    }

    private void createFrame() {
        Frame testFrame = new Frame();
        if (page == 1) {
            testFrame.setName("Sequence Diagram - First Page");
            LifelineCategories tt[] = new LifelineCategories[2];
            tt[0] = new LifelineCategories();
            tt[1] = new LifelineCategories();
            tt[1].setName("Categorie 1");
            tt[1].setImage(new LocalImageImpl("obj16/node_obj.gif"));
            tt[0].setImage(new LocalImageImpl("obj16/class.gif"));
            tt[0].setName("Categorie 0");
            testFrame.setLifelineCategories(tt);
            Lifeline lifeline = new Lifeline();
            lifeline.setName("LifeLine 0");
            testFrame.addLifeLine(lifeline);
            EllipsisMessage mn = new EllipsisMessage();
            lifeline.getNewEventOccurrence();
            mn.setStartLifeline(lifeline);
            mn.setName("******************* EllipsisisMessage TEST ****************");
            testFrame.addMessage(mn);
            SyncMessage mn3 = new SyncMessage();
            mn3.setStartLifeline(lifeline);
            testFrame.addMessage(mn3);
            SyncMessage mn2 = new SyncMessage();
            lifeline.getNewEventOccurrence();
            lifeline.setCategory(0);
            mn2.setEndLifeline(lifeline);
            mn2.setName("*******************Sync TEST ****************");
            testFrame.addMessage(mn2);
            for (int i = 1; i < 300; i++) {
                lifeline = new Lifeline();
                lifeline.setName((new StringBuilder("LifeLine ")).append(i).toString());
                lifeline.setCategory(1);
                testFrame.addLifeLine(lifeline);
                SyncMessage m3 = new SyncMessage();
                testFrame.getLifeline(i - 1).getNewEventOccurrence();
                m3.setStartLifeline(testFrame.getLifeline(i - 1));
                m3.setEndLifeline(testFrame.getLifeline(i));
                m3.setName((new StringBuilder("Sync Message ")).append(i).toString());
                testFrame.addMessage(m3);
//                if (i == 11)
//                    m3.setTime(new TmfTimestamp(i - 400));
//                else if (i == 6)
//                    m3.setTime(new TmfTimestamp(i));
//                else
                    m3.setTime(new TmfTimestamp(i + 1));
            }

            for (int i = testFrame.lifeLinesCount() - 1; i > 0; i--) {
                SyncMessageReturn m = new SyncMessageReturn();
                testFrame.getLifeline(i).getNewEventOccurrence();
                m.setStartLifeline(testFrame.getLifeline(i));
                m.setEndLifeline(testFrame.getLifeline(i - 1));
                testFrame.addMessage(m);
                m.setName((new StringBuilder("Sync Message return ")).append(i).toString());
                if (i + 1 < testFrame.lifeLinesCount()) {
                    SyncMessage h = testFrame.getSyncMessage(i + 1);
                    m.setMessage(h);
                }
            }

            for (int i = 0; i < testFrame.lifeLinesCount(); i++) {
                if (i > 0) {
                    ExecutionOccurrence occ = new ExecutionOccurrence();
                    occ.setStartOccurrence(testFrame.getSyncMessage(i).getEventOccurrence() + 1);
                    occ.setEndOccurrence(testFrame.getSyncMessageReturn(testFrame.syncMessageReturnCount() - i).getEventOccurrence());
                    testFrame.getLifeline(i).addExecution(occ);
                    occ.setName("******************* Execution Occurance TEST ****************");
                }
            }

            Stop s = new Stop();
            s.setLifeline(testFrame.getLifeline(1));
            s.setEventOccurrence(testFrame.getLifeline(1).getNewEventOccurrence());
            testFrame.getLifeline(1).addNode(s);
            HotSpot gg = new HotSpot();
            gg.setImage(new LocalImageImpl("obj16/plus_obj.gif"));
            gg.setExecution((BasicExecutionOccurrence) testFrame.getLifeline(1).getExecutions().get(0));
            AsyncMessageReturn m = new AsyncMessageReturn();
            m.setStartLifeline(testFrame.getLifeline(1));
            m.setEndLifeline(testFrame.getLifeline(3));
            m.setStartOccurrence(2);
            m.setEndOccurrence(6);
            m.setStartTime(new TmfTimestamp(2));
            m.setEndTime(new TmfTimestamp(6));
            m.setName("*******************Async TEST ****************");
            testFrame.addMessage(m);
            v.setFrame(testFrame);
            v.getSDWidget().setReorderMode(true);
        } else {

//        if (page == 2) {
            testFrame.setName("Sequence Diagram");
            Lifeline lifeline = new Lifeline();
            lifeline.setName("LifeLine 0");
            testFrame.addLifeLine(lifeline);
            lifeline = new Lifeline();
            lifeline.setName("LifeLine 1");
            testFrame.addLifeLine(lifeline);
            for (int i = 1; i < 30; i++) {
                SyncMessage m3 = new SyncMessage();
                m3.autoSetStartLifeline(testFrame.getLifeline(0));
                m3.autoSetEndLifeline(testFrame.getLifeline(0));
                m3.setName((new StringBuilder("Message ")).append(i).toString());
                testFrame.addMessage(m3);
                SyncMessageReturn m = new SyncMessageReturn();
                m.autoSetStartLifeline(testFrame.getLifeline(0));
                m.autoSetEndLifeline(testFrame.getLifeline(0));
                testFrame.addMessage(m);
                m.setName((new StringBuilder("Message return ")).append(i).toString());
                ExecutionOccurrence occ = new ExecutionOccurrence();
                occ.setStartOccurrence(testFrame.getSyncMessage(i - 1).getEventOccurrence());
                occ.setEndOccurrence(testFrame.getSyncMessageReturn(i - 1).getEventOccurrence());
                testFrame.getLifeline(0).addExecution(occ);
            }
        }
        v.setFrame(testFrame);
    }

    @Override
    public boolean find(Criteria toSearch) {
        Frame frame = v.getFrame();

        if (frame == null) {
            return false;
        }
        if (findResults == null || findCriteria == null || !findCriteria.compareTo(toSearch)) {
            findResults = new ArrayList<GraphNode>();
            findCriteria = toSearch;
            if (findCriteria.isLifeLineSelected()) {
                for (int i = 0; i < frame.lifeLinesCount(); i++) {
                    if (findCriteria.matches(frame.getLifeline(i).getName())) {
                        findResults.add(frame.getLifeline(i));
                    }
                }

            }
            ArrayList<GraphNode> msgs = new ArrayList<GraphNode>();
            if (findCriteria.isSyncMessageSelected()) {
                for (int i = 0; i < frame.syncMessageCount(); i++) {
                    if (findCriteria.matches(frame.getSyncMessage(i).getName())) {
                        msgs.add(frame.getSyncMessage(i));
                    }
                }

                for (int i = 0; i < frame.syncMessageReturnCount(); i++) {
                    if (findCriteria.matches(frame.getSyncMessageReturn(i).getName())) {
                        msgs.add(frame.getSyncMessageReturn(i));
                    }
                }

            }
            // if(msgs.size() > 0) {
            // GraphNode temp[] = msgs.toArray(new GraphNode[0]);
            // Arrays.sort(temp, new DateComparator());
            // findResults.addAll(Arrays.asList(temp));
            // }

            msgs = new ArrayList<GraphNode>();
            if (findCriteria.isAsyncMessageSelected()) {
                for (int i = 0; i < frame.asyncMessageCount(); i++) {
                    if (findCriteria.matches(frame.getAsyncMessage(i).getName())) {
                        msgs.add(frame.getAsyncMessage(i));
                    }
                }

                for (int i = 0; i < frame.asyncMessageReturnCount(); i++) {
                    if (findCriteria.matches(frame.getAsyncMessageReturn(i).getName())) {
                        msgs.add(frame.getAsyncMessageReturn(i));
                    }
                }

            }
            // if(msgs.size() > 0) {
            // GraphNode temp[] = msgs.toArray(new GraphNode[0]);
            // Arrays.sort(temp, new DateComparator());
            // findResults.addAll(Arrays.asList(temp));
            // }

            List<GraphNode> selection = v.getSDWidget().getSelection();
            if (selection != null && selection.size() == 1) {
                currentFindIndex = findResults.indexOf(selection.get(0)) + 1;
            } else {
                currentFindIndex = 0;
            }
        } else {
            currentFindIndex++;
        }
        if (findResults.size() > currentFindIndex) {
            GraphNode current = findResults.get(currentFindIndex);
            v.getSDWidget().moveTo(current);
            return true;
        }
        // return notFoundYet(findCriteria); // search in other page
        return false;
    }

    @Override
    public void cancel() {
        findResults = null;
        findCriteria = null;
        currentFindIndex = 0;
    }

    public boolean isLifelineSupported() {
        return false;
    }

    public boolean isSyncMessageSupported() {
        return false;
    }

    public boolean isSyncMessageReturnSupported() {
        return false;
    }

    public boolean isAsyncMessageSupported() {
        return false;
    }

    public boolean isAsyncMessageReturnSupported() {
        return false;
    }

    public boolean isStopSupported() {
        return false;
    }

    public Action getFindAction() {
        return null;
    }

    @Override
    public boolean filter(List<FilterCriteria> filters) {

        if (savedFrame != null) {
            savedFrame = v.getFrame();
        }

        Frame frame = v.getFrame();

        if (frame == null) {
            return false;
        }

        if (filters.size() != 1) {
            return false;
        }

        FilterCriteria filterCriteria = filters.get(0);

        // One way is to set visiblity of the item, but this only works for messages and not
        // for lifelines! It's better to create a new frame without the filtered messages.
        boolean found = false;
        if (filterCriteria.getCriteria().isSyncMessageSelected()) {
            for (int i = 0; i < frame.syncMessageCount(); i++) {
                if (filterCriteria.getCriteria().matches(frame.getSyncMessage(i).getName())) {
                    frame.getSyncMessage(i).setVisible(false);
                    found = true;
                }
            }

            for (int i = 0; i < frame.syncMessageReturnCount(); i++) {
                if (filterCriteria.getCriteria().matches(frame.getSyncMessageReturn(i).getName())) {
                    frame.getSyncMessageReturn(i).setVisible(false);
                    found = true;
                }
            }
        }

        v.getSDWidget().redraw();
        return found;
    }

    public ArrayList<?> getCurrentFilters() {
        return null;
    }

    @Override
    public String getTitleString() {
        return "Test Loader";
    }

    @Override
    public void dispose() {
    }


    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        ISelection sel = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
        if (sel != null && (sel instanceof StructuredSelection)) {
            StructuredSelection stSel = (StructuredSelection) sel;
            if (stSel.getFirstElement() instanceof HotSpot) {
                // OpenToolBox gg = new OpenToolBox(v);
                // gg.run();
            }
        }
    }

    @Override
    public boolean isNodeSupported(int nodeType) {
        switch (nodeType) {
        case ISDGraphNodeSupporter.LIFELINE:
        case ISDGraphNodeSupporter.SYNCMESSAGE:
        case ISDGraphNodeSupporter.SYNCMESSAGERETURN:
        case ISDGraphNodeSupporter.ASYNCMESSAGE:
        case ISDGraphNodeSupporter.ASYNCMESSAGERETURN:
        case ISDGraphNodeSupporter.STOP:
            return true;

        default:
            break;
        }
        return false;
    }

    @Override
    public String getNodeName(int nodeType, String loaderClassName) {
        return null;
    }

    public static class LocalImageImpl implements IImage {
        protected Image img;

        public LocalImageImpl(String file) {
            img = null;
            img = getResourceImage(file);
        }

        public LocalImageImpl(Image img_) {
            img = null;
            img = img_;
        }

        public Image getResourceImage(String _name) {
            ImageDescriptor imgage;
            try {
                URL BASIC_URL = new URL("platform", "localhost", "plugin");
                URL url = new URL(BASIC_URL, (new StringBuilder("plugin/org.eclipse.linuxtools.tmf.ui/icons/")).append(_name).toString());
                imgage = ImageDescriptor.createFromURL(url);
                return imgage.createImage();
            } catch (Exception e) {
                System.err.println(e);
            }
            return null;
        }

        @Override
        public Object getImage() {
            return img;
        }

        @Override
        public void dispose() {
            if (img != null) {
                img.dispose();
            }
        }

    }

    @Override
    public void firstPage() {
        page = 0;
        createFrame();

    }

    @Override
    public void lastPage() {
        page = 2;
        createFrame();
    }
}