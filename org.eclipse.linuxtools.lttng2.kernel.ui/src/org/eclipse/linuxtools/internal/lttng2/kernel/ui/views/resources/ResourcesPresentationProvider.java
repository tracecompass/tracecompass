/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.StateValues;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.Messages;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources.ResourcesEntry.Type;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.Resolution;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;
import org.eclipse.swt.graphics.RGB;

/**
 * Presentation provider for the Resource view, based on the generic TMF
 * presentation provider.
 *
 * @author Patrick Tasse
 */
public class ResourcesPresentationProvider extends TimeGraphPresentationProvider {

    private enum State {
        UNKNOWN         (new RGB(100, 100, 100)),
        IDLE            (new RGB(200, 200, 200)),
        USERMODE        (new RGB(0, 200, 0)),
        SYSCALL         (new RGB(0, 0, 200)),
        IRQ             (new RGB(200, 100, 100)),
        SOFT_IRQ        (new RGB(200, 150, 100)),
        IRQ_ACTIVE      (new RGB(200, 100, 100)),
        SOFT_IRQ_RAISED (new RGB(200, 200, 0)),
        SOFT_IRQ_ACTIVE (new RGB(200, 150, 100));

        public final RGB rgb;

        private State (RGB rgb) {
            this.rgb = rgb;
        }
    }

    @Override
    public String getStateTypeName() {
        return Messages.ResourcesView_stateTypeName;
    }

    @Override
    public StateItem[] getStateTable() {
        StateItem[] stateTable = new StateItem[State.values().length];
        for (int i = 0; i < stateTable.length; i++) {
            State state = State.values()[i];
            stateTable[i] = new StateItem(state.rgb, state.toString());
        }
        return stateTable;
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        if (event instanceof ResourcesEvent) {
            ResourcesEvent resourcesEvent = (ResourcesEvent) event;
            if (resourcesEvent.getType() == Type.CPU) {
                int status = resourcesEvent.getValue();
                if (status == StateValues.CPU_STATUS_IDLE) {
                    return State.IDLE.ordinal();
                } else if (status == StateValues.CPU_STATUS_RUN_USERMODE) {
                    return State.USERMODE.ordinal();
                } else if (status == StateValues.CPU_STATUS_RUN_SYSCALL) {
                    return State.SYSCALL.ordinal();
                } else if (status == StateValues.CPU_STATUS_IRQ) {
                    return State.IRQ.ordinal();
                } else if (status == StateValues.CPU_STATUS_SOFTIRQ) {
                    return State.SOFT_IRQ.ordinal();
                }
            } else if (resourcesEvent.getType() == Type.IRQ) {
                return State.IRQ_ACTIVE.ordinal();
            } else if (resourcesEvent.getType() == Type.SOFT_IRQ) {
                int cpu = resourcesEvent.getValue();
                if (cpu == StateValues.SOFT_IRQ_RAISED) {
                    return State.SOFT_IRQ_RAISED.ordinal();
                }
                return State.SOFT_IRQ_ACTIVE.ordinal();
            } else {
                return -1; // NULL
            }
        }
        return State.UNKNOWN.ordinal();
    }

    @Override
    public String getEventName(ITimeEvent event) {
        if (event instanceof ResourcesEvent) {
            ResourcesEvent resourcesEvent = (ResourcesEvent) event;
            if (resourcesEvent.getType() == Type.CPU) {
                int status = resourcesEvent.getValue();
                if (status == StateValues.CPU_STATUS_IDLE) {
                    return State.IDLE.toString();
                } else if (status == StateValues.CPU_STATUS_RUN_USERMODE) {
                    return State.USERMODE.toString();
                } else if (status == StateValues.CPU_STATUS_RUN_SYSCALL) {
                    return State.SYSCALL.toString();
                } else if (status == StateValues.CPU_STATUS_IRQ) {
                    return State.IRQ.toString();
                } else if (status == StateValues.CPU_STATUS_SOFTIRQ) {
                    return State.SOFT_IRQ.toString();
                }
            } else if (resourcesEvent.getType() == Type.IRQ) {
                return State.IRQ_ACTIVE.toString();
            } else if (resourcesEvent.getType() == Type.SOFT_IRQ) {
                int cpu = resourcesEvent.getValue();
                if (cpu == StateValues.SOFT_IRQ_RAISED) {
                    return State.SOFT_IRQ_RAISED.toString();
                }
                return State.SOFT_IRQ_ACTIVE.toString();
            } else {
                return null;
            }
        }
        return State.UNKNOWN.toString();
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event, long hoverTime) {

        Map<String, String> retMap = new LinkedHashMap<String, String>();
        if (event instanceof ResourcesEvent) {

            ResourcesEvent resourcesEvent = (ResourcesEvent) event;

            // Check for IRQ or Soft_IRQ type
            if (resourcesEvent.getType().equals(Type.IRQ) || resourcesEvent.getType().equals(Type.SOFT_IRQ)) {

                // Get CPU of IRQ or SoftIRQ and provide it for the tooltip display
                int cpu = resourcesEvent.getValue();
                if (cpu >= 0) {
                    retMap.put(Messages.ResourcesView_attributeCpuName, String.valueOf(cpu));
                }
            }

            // Check for type CPU
            if (resourcesEvent.getType().equals(Type.CPU)) {
                int status = resourcesEvent.getValue();

                if (status == StateValues.CPU_STATUS_IRQ) {
                    // In IRQ state get the IRQ that caused the interruption
                    ResourcesEntry entry = (ResourcesEntry) event.getEntry();
                    ITmfStateSystem ssq = entry.getTrace().getStateSystem();
                    int cpu = entry.getId();

                    ITmfStateSystem ss = entry.getTrace().getStateSystem();
                    try {
                        List<ITmfStateInterval> fullState = ss.queryFullState(event.getTime());
                        List<Integer> irqQuarks = ss.getQuarks(Attributes.RESOURCES, Attributes.IRQS, "*"); //$NON-NLS-1$

                        for (int irqQuark : irqQuarks) {
                            if (fullState.get(irqQuark).getStateValue().unboxInt() == cpu) {
                                ITmfStateInterval value = ssq.querySingleState(event.getTime(), irqQuark);
                                if (!value.getStateValue().isNull()) {
                                    int irq = Integer.parseInt(ssq.getAttributeName(irqQuark));
                                    retMap.put(Messages.ResourcesView_attributeIrqName, String.valueOf(irq));
                                }
                                break;
                            }
                        }
                    } catch (AttributeNotFoundException e) {
                        e.printStackTrace();
                    } catch (TimeRangeException e) {
                        e.printStackTrace();
                    } catch (StateValueTypeException e) {
                        e.printStackTrace();
                    } catch (StateSystemDisposedException e) {
                        /* Ignored */
                    }
                } else if (status == StateValues.CPU_STATUS_SOFTIRQ) {
                    // In SOFT_IRQ state get the SOFT_IRQ that caused the interruption
                    ResourcesEntry entry = (ResourcesEntry) event.getEntry();
                    ITmfStateSystem ssq = entry.getTrace().getStateSystem();
                    int cpu = entry.getId();

                    ITmfStateSystem ss = entry.getTrace().getStateSystem();
                    try {
                        List<ITmfStateInterval> fullState = ss.queryFullState(event.getTime());
                        List<Integer> softIrqQuarks = ss.getQuarks(Attributes.RESOURCES, Attributes.SOFT_IRQS, "*"); //$NON-NLS-1$

                        for (int softIrqQuark : softIrqQuarks) {
                            if (fullState.get(softIrqQuark).getStateValue().unboxInt() == cpu) {
                                ITmfStateInterval value = ssq.querySingleState(event.getTime(), softIrqQuark);
                                if (!value.getStateValue().isNull()) {
                                    int softIrq = Integer.parseInt(ssq.getAttributeName(softIrqQuark));
                                    retMap.put(Messages.ResourcesView_attributeSoftIrqName, String.valueOf(softIrq));
                                }
                                break;
                            }
                        }
                    } catch (AttributeNotFoundException e) {
                        e.printStackTrace();
                    } catch (TimeRangeException e) {
                        e.printStackTrace();
                    } catch (StateValueTypeException e) {
                        e.printStackTrace();
                    } catch (StateSystemDisposedException e) {
                        /* Ignored */
                    }
                } else if (status == StateValues.CPU_STATUS_RUN_USERMODE || status == StateValues.CPU_STATUS_RUN_SYSCALL){
                    // In running state get the current tid
                    ResourcesEntry entry = (ResourcesEntry) event.getEntry();
                    ITmfStateSystem ssq = entry.getTrace().getStateSystem();

                    try {
                        retMap.put(Messages.ResourcesView_attributeHoverTime, Utils.formatTime(hoverTime, TimeFormat.ABSOLUTE, Resolution.NANOSEC));
                        int cpuQuark = entry.getQuark();
                        int currentThreadQuark = ssq.getQuarkRelative(cpuQuark, Attributes.CURRENT_THREAD);
                        ITmfStateInterval interval = ssq.querySingleState(hoverTime, currentThreadQuark);
                        if (!interval.getStateValue().isNull()) {
                            ITmfStateValue value = interval.getStateValue();
                            int currentThreadId = value.unboxInt();
                            retMap.put(Messages.ResourcesView_attributeTidName, Integer.toString(currentThreadId));
                            int execNameQuark = ssq.getQuarkAbsolute(Attributes.THREADS, Integer.toString(currentThreadId), Attributes.EXEC_NAME);
                            interval = ssq.querySingleState(hoverTime, execNameQuark);
                            if (!interval.getStateValue().isNull()) {
                                value = interval.getStateValue();
                                retMap.put(Messages.ResourcesView_attributeProcessName, value.unboxStr());
                            }
                            if (status == StateValues.CPU_STATUS_RUN_SYSCALL) {
                                int syscallQuark = ssq.getQuarkAbsolute(Attributes.THREADS, Integer.toString(currentThreadId), Attributes.SYSTEM_CALL);
                                interval = ssq.querySingleState(hoverTime, syscallQuark);
                                if (!interval.getStateValue().isNull()) {
                                    value = interval.getStateValue();
                                    retMap.put(Messages.ResourcesView_attributeSyscallName, value.unboxStr());
                                }
                            }
                        }
                    } catch (AttributeNotFoundException e) {
                        e.printStackTrace();
                    } catch (TimeRangeException e) {
                        e.printStackTrace();
                    } catch (StateValueTypeException e) {
                        e.printStackTrace();
                    } catch (StateSystemDisposedException e) {
                        /* Ignored */
                    }
                }
            }
        }

        return retMap;
    }

}
