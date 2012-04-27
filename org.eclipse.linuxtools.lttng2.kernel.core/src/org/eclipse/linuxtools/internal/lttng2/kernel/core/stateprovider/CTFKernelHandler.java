/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider;

import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.statesystem.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.statesystem.StateHistorySystem;
import org.eclipse.linuxtools.tmf.core.statesystem.StateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;

/**
 * This is the reference "state provider" for LTTng 2.0 kernel traces.
 * 
 * @author alexmont
 * 
 */
class CTFKernelHandler implements Runnable {

    private final BlockingQueue<CtfTmfEvent> inQueue;
    private StateSystem ss;

    private CtfTmfEvent currentEvent;

    /*
     * We can keep handles to some Attribute Nodes so these don't need to be
     * re-found (re-hashed Strings etc.) every new event
     */
    Vector<Integer> currentCPUNodes;
    Vector<Integer> currentThreadNodes;

    /* Event names HashMap. TODO: This can be discarded once we move to Java 7 */
    private final HashMap<String, Integer> knownEventNames;

    CTFKernelHandler(BlockingQueue<CtfTmfEvent> eventsQueue) {
        assert (eventsQueue != null);
        this.inQueue = eventsQueue;
        currentCPUNodes = new Vector<Integer>();
        currentThreadNodes = new Vector<Integer>();

        knownEventNames = fillEventNames();
    }

    void assignStateSystem(StateSystem targetSS) {
        this.ss = targetSS;
    }

    StateSystem getStateSystem() {
        return ss;
    }

    @Override
    public void run() {
        if (ss == null) {
            System.err.println("Cannot run event manager without assigning a target state system first!"); //$NON-NLS-1$
            return;
        }
        CtfTmfEvent event;

        try {
            event = inQueue.take();
            while (event.getTimestampValue() != -1) {
                processEvent(event);
                event = inQueue.take();
            }
            /* We've received the last event, clean up */
            closeStateSystem();
            return;
        } catch (InterruptedException e) {
            /* We've been interrupted abnormally */
            System.out.println("Event handler interrupted!"); //$NON-NLS-1$
            e.printStackTrace();
        }
    }

    private void closeStateSystem() {
        /* Close the History system, if there is one */
        if (ss.getClass() == StateHistorySystem.class) {
            try {
                ((StateHistorySystem) ss).closeHistory(currentEvent.getTimestamp().getValue());
            } catch (TimeRangeException e) {
                /*
                 * Since we're using currentEvent.getTimestamp, this shouldn't
                 * cause any problem
                 */
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("nls")
    private void processEvent(CtfTmfEvent event) {
        currentEvent = event;
        ITmfEventField content = event.getContent();
        String eventName = event.getEventName();

        long ts = event.getTimestamp().getValue();
        int quark;
        ITmfStateValue value;
        Integer eventCpu = event.getCPU();
        Integer currentCPUNode, currentThreadNode, tidNode;

        /* Adjust the current nodes Vectors if we see a new CPU in an event */
        if (eventCpu >= currentCPUNodes.size()) {
            /* We need to add this node to the vector */
            for (Integer i = currentCPUNodes.size(); i < eventCpu + 1; i++) {
                quark = ss.getQuarkAbsoluteAndAdd("CPUs", i.toString());
                currentCPUNodes.add(quark);

                quark = ss.getQuarkAbsoluteAndAdd("Threads", "unknown");
                currentThreadNodes.add(quark);
            }
        }

        currentCPUNode = currentCPUNodes.get(eventCpu);
        currentThreadNode = currentThreadNodes.get(eventCpu);
        assert (currentCPUNode != null);
        assert (currentThreadNode != null);

        try {
            /*
             * Feed event to the history system if it's known to cause a state
             * transition See:
             * https://projectwiki.dorsal.polymtl.ca/index.php/State_transitions
             */
            switch (getEventIndex(eventName)) {

            case 1: // "exit_syscall":
                /* Fields: int64 ret */
                /* Pop "syscall" from the Exec_mode_stack */
                quark = ss.getQuarkRelativeAndAdd(currentThreadNode,
                        "Exec_mode_stack");
                try {
                    ss.popAttribute(ts, quark);
                } catch (AttributeNotFoundException e1) {
                    /*
                     * meh, can happen if we're missing events, we'll just
                     * silently ignore it.
                     */
                    System.err.println(event.getTimestamp()
                            + " Popping empty attribute: " + e1.getMessage()); //$NON-NLS-1$
                }
                break;

            case 2: // "irq_handler_entry":
                /* Fields: int32 irq, string name */
                Integer irqId = ((Long) content.getField("irq").getValue()).intValue();

                /* Push the IRQ to the CPU's IRQ_stack */
                quark = ss.getQuarkRelativeAndAdd(currentCPUNode, "IRQ_stack");
                value = TmfStateValue.newValueInt(irqId);
                ss.pushAttribute(ts, value, quark);

                /* Change the status of the running process to interrupted */
                quark = ss.getQuarkRelativeAndAdd(currentThreadNode, "Status");
                value = TmfStateValue.newValueInt(STATE_PROCESS_STATUS_WAIT_CPU);
                ss.modifyAttribute(ts, value, quark);
                break;

            case 3: // "irq_handler_exit":
                /* Fields: int32 irq, int32 ret */
                int stackDepth = 0;

                /* Pop the IRQ from the CPU's IRQ_stack */
                quark = ss.getQuarkRelativeAndAdd(currentCPUNode, "IRQ_stack");
                try {
                    ss.popAttribute(ts, quark);
                } catch (AttributeNotFoundException e1) {
                    System.err.print(event.getTimestamp()
                            + " Popping empty attribute: " + e1.getMessage());
                }

                /*
                 * If this was the last IRQ on the stack, set the process back
                 * to running
                 */
                /* 'quark' should still be valid */
                try {
                    stackDepth = ss.queryOngoingState(quark).unboxInt();
                } catch (StateValueTypeException e) {
                    /* IRQ_stack SHOULD be of int type, this shouldn't happen */
                    e.printStackTrace();
                }
                if (stackDepth == 0) {
                    quark = ss.getQuarkRelativeAndAdd(currentThreadNode,
                            "Status");
                    value = TmfStateValue.newValueInt(STATE_PROCESS_STATUS_RUN);
                    ss.modifyAttribute(ts, value, quark);
                }
                break;

            case 4: // "softirq_entry":
                /* Fields: int32 vec */
                break;

            case 5: // "softirq_exit":
                /* Fields: int32 vec */
                break;

            case 6: // "softirq_raise":
                /* Fields: int32 vec */
                break;

            case 7: // "sched_switch":
                /*
                 * Fields: string prev_comm, int32 prev_tid, int32 prev_prio,
                 * int64 prev_state, string next_comm, int32 next_tid, int32
                 * next_prio
                 */

                // prev_comm doesn't seem to get populated...
                String prevProcessName = (String) content.getField("prev_comm").getValue();
                Integer prevTid = ((Long) content.getField("prev_tid").getValue()).intValue();
                Long prevState = (Long) content.getField("prev_state").getValue();

                String nextProcessName = (String) content.getField("next_comm").getValue();
                Integer nextTid = ((Long) content.getField("next_tid").getValue()).intValue();

                /* Update the name of the process going out (if needed) */
                quark = ss.getQuarkRelativeAndAdd(currentThreadNode,
                        "Exec_name");
                value = TmfStateValue.newValueString(prevProcessName);
                ss.updateOngoingState(value, quark);

                /* Update the currentThreadNodes pointer */
                Integer newCurrentThreadNode = ss.getQuarkAbsoluteAndAdd(
                        "Threads", nextTid.toString());
                currentThreadNodes.set(eventCpu, newCurrentThreadNode);

                /* Set the status of the new scheduled process */
                quark = ss.getQuarkRelativeAndAdd(newCurrentThreadNode,
                        "Status");
                value = TmfStateValue.newValueInt(STATE_PROCESS_STATUS_RUN);
                ss.modifyAttribute(ts, value, quark);

                /* Set the exec name of the new process */
                quark = ss.getQuarkRelativeAndAdd(newCurrentThreadNode,
                        "Exec_name");
                value = TmfStateValue.newValueString(nextProcessName);
                ss.modifyAttribute(ts, value, quark);

                /* Set the status of the process that got scheduled out */
                quark = ss.getQuarkAbsoluteAndAdd("Threads",
                        prevTid.toString(), "Status");
                value = TmfStateValue.newValueInt(prevState.intValue());
                ss.modifyAttribute(ts, value, quark);

                /* Set the current scheduled process on the relevant CPU */
                quark = ss.getQuarkRelativeAndAdd(currentCPUNode,
                        "Current_thread");
                value = TmfStateValue.newValueInt(nextTid);
                ss.modifyAttribute(ts, value, quark);
                break;

            case 8: // "sched_process_fork":
                /*
                 * Fields: string parent_comm, int32 parent_tid, string
                 * child_comm, int32 child_tid
                 */

                // String parentProcessName = (String)
                // event.getFieldValue("parent_comm");
                String childProcessName = (String) content.getField("child_comm").getValue();
                // assert ( parentProcessName.equals(childProcessName) );

                Integer parentTid = ((Long) content.getField("parent_tid").getValue()).intValue();
                Integer childTid = ((Long) content.getField("child_tid").getValue()).intValue();

                tidNode = ss.getQuarkAbsoluteAndAdd("Threads",
                        childTid.toString());

                /*
                 * Add the new process with its known TID, PPID, and initial
                 * Exec_name
                 */
                quark = ss.getQuarkRelativeAndAdd(tidNode, "PPID");
                value = TmfStateValue.newValueInt(parentTid);
                ss.modifyAttribute(ts, value, quark);

                /* Set the new process' exec_name */
                quark = ss.getQuarkRelativeAndAdd(tidNode, "Exec_name");
                value = TmfStateValue.newValueString(childProcessName);
                ss.modifyAttribute(ts, value, quark);
                break;

            case 9: // "sched_process_exit":
                /* Fields: string comm, int32 tid, int32 prio */
                String processName = (String) content.getField("comm").getValue();
                Integer tid = ((Long) content.getField("tid").getValue()).intValue();

                /* Update the process' name, if we don't have it */
                quark = ss.getQuarkAbsoluteAndAdd("Threads", tid.toString(),
                        "Exec_name");
                value = TmfStateValue.newValueString(processName);
                ss.updateOngoingState(value, quark);

                /*
                 * Remove the process and all its sub-attributes from the
                 * current state
                 */
                quark = ss.getQuarkAbsoluteAndAdd("Threads", tid.toString());
                ss.removeAttribute(ts, quark);
                break;

            case 10: // "sched_process_free":
                /* Fields: string comm, int32 tid, int32 prio */
                break;

            // FIXME Not available with CTF. Use event context?
            // case LTT_EVENT_EXEC:
            // filename = new String((byte[]) event.getField(0));
            //
            // /* Change the Exec_name of the process */
            // quark = ss.getQuarkRelativePath(true, currentThreadNode,
            // "Exec_name");
            // ss.modifyAttribute(ts, filename, quark);
            // break;

            default:
                /* Other event types not covered by the main switch */

                if (eventName.startsWith("sys_")
                        || eventName.startsWith("compat_sys_")) {
                    /*
                     * This is a replacement for the old sys_enter event. Now
                     * syscall names are listed into the event type
                     */

                    /*
                     * Push the syscall name on the Exec_mode_stack of the
                     * relevant PID
                     */
                    quark = ss.getQuarkRelativeAndAdd(currentThreadNode,
                            "Exec_mode_stack");
                    value = TmfStateValue.newValueString(eventName);
                    ss.pushAttribute(ts, value, quark);
                }

                break;
            } // End of switch

            /*
             * Statistics
             */

            /* Nb of events total */
            try {
                quark = ss.getQuarkAbsoluteAndAdd("Stats", "Event_types",
                        eventName);
                ss.incrementAttribute(ts, quark);

                // /* Nb of events per CPU */
                // ss.incrementAttribute(ts,
                // ss.getAttributeQuarkAndAdd(currentCPUNode, "Stats",
                // "Event_types", eventType.toString()));
                //
                // /* Nb of events per process */
                // ss.incrementAttribute(ts,
                // ss.getAttributeQuarkAndAdd(currentThreadNode, "Stats",
                // "Event_types", eventType.toString()));

            } catch (StateValueTypeException sve) {
                /*
                 * Here's hoping we don't have string values in statistics
                 * attributes...
                 */
                sve.printStackTrace();
            }

            // end of big non-indented try
        } catch (AttributeNotFoundException ae) {
            /*
             * This would indicate a problem with the logic of the manager here,
             * so it shouldn't happen.
             */
            ae.printStackTrace();

        } catch (TimeRangeException tre) {
            /*
             * This would happen if the events in the trace aren't ordered
             * chronologically, which should never be the case ...
             */
            System.err.println("TimeRangeExcpetion caught in the state system's event manager.");
            System.err.println("Are the events in the trace correctly ordered?");
            tre.printStackTrace();

        } catch (StateValueTypeException sve) {
            /*
             * This would happen if we were trying to push/pop attributes not of
             * type integer. Which, once again, should never happen.
             */
            sve.printStackTrace();
        }

    }

    @SuppressWarnings("nls")
    private static HashMap<String, Integer> fillEventNames() {
        /*
         * TODO Replace with straight strings in the switch/case once we move to
         * Java 7
         */
        /*
         * This is still, imo, cleaner than the wtf-were-they-thinking Java
         * Enums
         */
        HashMap<String, Integer> map = new HashMap<String, Integer>();

        map.put("exit_syscall", 1);
        map.put("irq_handler_entry", 2);
        map.put("irq_handler_exit", 3);
        map.put("softirq_entry", 4);
        map.put("softirq_exit", 5);
        map.put("softirq_raise", 6);
        map.put("sched_switch", 7);
        map.put("sched_process_fork", 8);
        map.put("sched_process_exit", 9);
        map.put("sched_process_free", 10);

        return map;
    }

    private int getEventIndex(String eventName) {
        Integer ret = knownEventNames.get(eventName);
        return (ret != null) ? ret : -1;
    }

    /* Process status */
    private final static int STATE_PROCESS_STATUS_WAIT_CPU = 1;
    private final static int STATE_PROCESS_STATUS_RUN = 2;
}
