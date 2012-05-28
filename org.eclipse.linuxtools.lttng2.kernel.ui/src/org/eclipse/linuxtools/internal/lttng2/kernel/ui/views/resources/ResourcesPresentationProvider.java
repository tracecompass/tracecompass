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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.StateValues;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.Messages;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources.ResourcesEntry.Type;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateSystemQuerier;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.swt.graphics.RGB;

public class ResourcesPresentationProvider extends TimeGraphPresentationProvider {

    private enum State {
        UNKNOWN     (new RGB(100, 100, 100)),
        IDLE        (new RGB(200, 200, 200)),
        BUSY        (new RGB(0, 200, 0)),
        INTERRUPTED (new RGB(200, 100, 100)),
        RAISED      (new RGB(200, 200, 0)),
        ACTIVE      (new RGB(200, 150, 100));

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
                    return State.BUSY.ordinal();
                } else if (status == StateValues.CPU_STATUS_IRQ) {
                    return State.INTERRUPTED.ordinal();
                }
            } else if (resourcesEvent.getType() == Type.IRQ || resourcesEvent.getType() == Type.SOFT_IRQ) {
                int cpu = resourcesEvent.getValue();
                if (cpu == StateValues.SOFT_IRQ_RAISED) {
                    return State.RAISED.ordinal();
                }
                return State.ACTIVE.ordinal();
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
                    return State.BUSY.toString();
                } else if (status == StateValues.CPU_STATUS_IRQ) {
                    return State.INTERRUPTED.toString();
                }
            } else if (resourcesEvent.getType() == Type.IRQ || resourcesEvent.getType() == Type.SOFT_IRQ) {
                int cpu = resourcesEvent.getValue();
                if (cpu == StateValues.SOFT_IRQ_RAISED) {
                    return State.RAISED.toString();
                }
                return State.ACTIVE.toString();
            } else {
                return null;
            }
        }
        return State.UNKNOWN.toString();
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event) {
        
        Map<String, String> retMap = new HashMap<String, String>();
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
                    // In interrupted state get the IRQ or SOFT_IRQ that caused the interruption
                    ResourcesEntry entry = (ResourcesEntry) event.getEntry();
                    IStateSystemQuerier ssq = entry.getTrace().getStateSystem();
                    int cpu = entry.getId();
                    
                    IStateSystemQuerier ss = entry.getTrace().getStateSystem();
                    try {
                        int resultQuark = 0;
                        String attributeName = null;
                        
                        // First check for IRQ
                        List<ITmfStateInterval> fullState = ss.queryFullState(event.getTime());
                        List<Integer> irqQuarks = ss.getQuarks(Attributes.RESOURCES, Attributes.IRQS, "*"); //$NON-NLS-1$

                        for (int curQuark : irqQuarks) {
                            if (fullState.get(curQuark).getStateValue().unboxInt() == cpu) {
                                resultQuark = curQuark;
                                attributeName = Messages.ResourcesView_attributeIrqName;
                                break;
                            }
                        }
                        
                        // If not found check for SOFT_IRQ
                        if (attributeName == null) {
                            List<Integer> softIrqQuarks = ssq.getQuarks(Attributes.RESOURCES, Attributes.SOFT_IRQS, "*"); //$NON-NLS-1$
                            for (int curQuark : softIrqQuarks) {
                                if (fullState.get(curQuark).getStateValue().unboxInt() == cpu) {
                                    resultQuark = curQuark;
                                    attributeName = Messages.ResourcesView_attributeSoftIrqName;
                                    break;
                                }
                            }   
                        }

                        if (attributeName != null) {
                            // A IRQ or SOFT_IRQ was found
                            ITmfStateInterval value = ssq.querySingleState(event.getTime(), resultQuark);
                            if (!value.getStateValue().isNull()) {
                                int irq = Integer.parseInt(ssq.getAttributeName(resultQuark));
                                retMap.put(attributeName, String.valueOf(irq));
                            }
                        }
                    } catch (AttributeNotFoundException e) {
                        e.printStackTrace();
                    } catch (TimeRangeException e) {
                        e.printStackTrace();
                    } catch (StateValueTypeException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return retMap;
    }

}
