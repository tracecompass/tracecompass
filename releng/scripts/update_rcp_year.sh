#!/bin/sh
###############################################################################
# Copyright (c) 2019, 2022 Ericsson
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
###############################################################################

# Update the user visible copyright year in the RCP.
#
# Usage  ./update_rcp_year.sh

newYear=`date +%Y`

echo Changing copyright year to $newYear

#Update year in product file
sed -i -e "s/\([0-9]\{4\}\)\sEricsson/$newYear Ericsson/g" ../../rcp/org.eclipse.tracecompass.rcp.product/tracing.product

#Update year in legacy/staging product files
sed -i -e "s/\([0-9]\{4\}\)\sEricsson/$newYear Ericsson/g" ../../rcp/org.eclipse.tracecompass.rcp.product/*/tracing.product

#Update branding plugin.xml
sed -i -e "s/\([0-9]\{4\}\)\sEricsson/$newYear Ericsson/g" ../../rcp/org.eclipse.tracecompass.rcp.branding/plugin.xml

#Update branding about.properties
sed -i -e "s/\([0-9]\{4\}\)\sEricsson/$newYear Ericsson/g" ../../rcp/org.eclipse.tracecompass.rcp.branding/about.properties

