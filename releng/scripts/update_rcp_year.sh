#!/bin/sh
###############################################################################
# Copyright (c) 2019 Ericsson
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
###############################################################################

# Update the user visible copyright year in the RCP.
#
# Usage  ./update_rcp_year.sh

newYear=`date +%Y`

echo Changing copyright year to $newYear

#Update year in product file
sed -i -e "s/\([0-9]\{4\}\)\sEricsson/$newYear Ericsson/g" ../../rcp/org.eclipse.tracecompass.rcp.product/tracing.product

#Update year in legacy product file
sed -i -e "s/\([0-9]\{4\}\)\sEricsson/$newYear Ericsson/g" ../../rcp/org.eclipse.tracecompass.rcp.product/legacy/tracing.product

#Update branding plugin.xml
sed -i -e "s/\([0-9]\{4\}\)\sEricsson/$newYear Ericsson/g" ../../rcp/org.eclipse.tracecompass.rcp.branding/plugin.xml

#Update branding about.properties
sed -i -e "s/\([0-9]\{4\}\)\sEricsson/$newYear Ericsson/g" ../../rcp/org.eclipse.tracecompass.rcp.branding/about.properties

