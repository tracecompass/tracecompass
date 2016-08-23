#!/bin/bash
###############################################################################
# Copyright (c) 2016 Ericsson
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
###############################################################################

# A simple script that sets the rcp to automatically update at startup by
# default. This sets the preference through plugin_customization.ini
# See https://bugs.eclipse.org/bugs/show_bug.cgi?id=499247

PATH_TO_PREFERENCE_FILE="../../rcp/org.eclipse.tracecompass.rcp.branding/plugin_customization.ini"

OUTPUT=$(cat $PATH_TO_PREFERENCE_FILE  | grep org.eclipse.equinox.p2.ui.sdk.scheduler | grep enabled)
if [[ -z "$OUTPUT" ]];
then
	echo "Setting Automatic update"
	echo "
# check for updates every time Trace Compass starts. This should only be done in stable releases.
# https://bugs.eclipse.org/bugs/show_bug.cgi?id=499247
org.eclipse.equinox.p2.ui.sdk.scheduler/enabled=true" >> $PATH_TO_PREFERENCE_FILE
	CHECK_OUTPUT=$(cat $PATH_TO_PREFERENCE_FILE | grep org.eclipse.equinox.p2.ui.sdk.scheduler | grep enabled)
	if [[ -z "$CHECK_OUTPUT" ]];
	then
		echo "Failed!"
	else
		echo "Success!"
	fi
else
	echo "Automatic update already set?"
fi

