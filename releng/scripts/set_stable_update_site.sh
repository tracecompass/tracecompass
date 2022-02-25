#!/bin/bash
###############################################################################
# Copyright (c) 2016, 2022 Ericsson
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
###############################################################################

# A simple script that sets the rcp to use the stable update site. This is
# meant to be used when the master branch is being branched into a stable
# branch.

files=("../../rcp/org.eclipse.tracecompass.rcp.product/tracing.product" "../../rcp/org.eclipse.tracecompass.rcp.product/staging/tracing.product")

for file in "${files[@]}"
do
    OUTPUT=$(cat $file | grep "<repository.*stable.*")
    if [[ -z "$OUTPUT" ]];
    then
    sed -i -E  s/\(\<repository.*\)master\(.*$\)/\\1stable\\2/g $file
	    CHECK_OUTPUT=$(cat $file | grep "<repository.*stable.*")
	    if [[ -z "$CHECK_OUTPUT" ]];
	    then
		    echo "Failed!"
	    else
		    echo "Success!"
	    fi
    else
	    echo "Stable update site already set?"
    fi
done
