#!/bin/bash
###############################################################################
# Copyright (c) 2016 Ericsson
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
###############################################################################

# A simple script that sets the rcp to use the stable update site. This is
# meant to be used when the master branch is being branched into a stable
# branch.

PATH_TO_PRODUCT_FILE="../rcp/org.eclipse.tracecompass.rcp.product/tracing.product"

OUTPUT=$(cat $PATH_TO_PRODUCT_FILE  | grep "<repository.*stable.*")
if [[ -z "$OUTPUT" ]];
then
sed -i -E  s/\(\<repository.*\)master\(.*$\)/\\1stable\\2/g $PATH_TO_PRODUCT_FILE
	CHECK_OUTPUT=$(cat $PATH_TO_PRODUCT_FILE | grep "<repository.*stable.*")
	if [[ -z "$CHECK_OUTPUT" ]];
	then
		echo "Failed!"
	else
		echo "Success!"
	fi
else
	echo "Stable update site already set?"
fi

