#!/usr/bin/env bash
#*******************************************************************************
# Copyright (c) 2020 Ericsson
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v20.html
#*******************************************************************************

if [ -f "$1" ]; then
    sed -i s#}\"\"{#},\\n{#g $1
    sed -i s#\"\"#\"},\\n{\"#g $1
    sed -i s#\"{#\[{#g $1
    sed -i s#}\"#}\]#g $1
else
    echo "$1 file not found!"
    exit 1
fi
     


