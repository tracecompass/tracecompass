/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.tracecontrol.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceChannel;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceChannels;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * <b><u>UstTraceChannelConfigurationPage</u></b>
 * <p>
 *  Wizard page implementation to configure the UST trace channels.
 * </p>
 */
public class UstTraceChannelConfigurationPage extends WizardPage implements ITraceChannelConfigurationPage {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private TraceChannels fChannels;
    private Composite fContainer;
    private Text fChannelTimerText;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * 
     * @param channels
     */
    protected UstTraceChannelConfigurationPage(TraceChannels channels) {
        super("UstTraceChannelConfigurationPage"); //$NON-NLS-1$
        fChannels = channels;
        setTitle(Messages.ChannelConfigPage_PageTitle);
    }

 
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
 
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        fContainer = new Composite(parent, SWT.NULL);
        fContainer.setLayout(new GridLayout());
        setControl(fContainer);

        Composite headerComposite = new Composite(fContainer, SWT.FILL);
        GridLayout headerLayout = new GridLayout(4, true);
        headerLayout.marginHeight = 0;
        headerLayout.marginWidth = 0;
        headerComposite.setLayout(headerLayout);
        headerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        Label label = new Label(headerComposite, SWT.LEFT);
        label.setText(Messages.ChannelConfigPage_ChannelTimer + ":"); //$NON-NLS-1$
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        
        fChannelTimerText = new Text(headerComposite, SWT.LEFT);
        fChannelTimerText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
        fChannelTimerText.setToolTipText(Messages.ChannelConfigPage_ChannelTimerTooltip);
        
        TraceChannel chan = fChannels.get(TraceChannel.UST_TRACE_CHANNEL_NAME);
        if (chan.getTimer() == TraceChannel.UNKNOWN_VALUE) {
            fChannelTimerText.setText(TraceChannel.UNKNOWN_STRING);    
        }
        else { 
            fChannelTimerText.setText(String.valueOf(chan.getTimer()));
        }
        
        fChannelTimerText.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent e) {
                e.doit = e.text.matches("[0-9]*"); //$NON-NLS-1$
            }
        });
        
        fChannelTimerText.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
                String valueString = fChannelTimerText.getText();
                TraceChannel chan = fChannels.get(TraceChannel.UST_TRACE_CHANNEL_NAME);
                if (valueString.length() == 0) {
                    valueString = "0"; //$NON-NLS-1$
                }
                else if(TraceChannel.UNKNOWN_STRING.equals(valueString)) {
                    chan.setTimer(TraceChannel.UNKNOWN_VALUE);
                }
                else {
                    chan.setTimer(Integer.parseInt(valueString));
                }
            }
        });
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.ui.wizards.ITraceChannelConfigurationPage#getTraceChannels()
     */
    @Override
    public TraceChannels getTraceChannels() {
        return fChannels;
    }
}