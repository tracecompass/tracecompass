#!/bin/bash

if [ $EUID -ne 0 ]
then
	echo "Must be run as root"
	exit
fi

if [ $# -ne 2 ]
then
	echo "Need 2 arguments, the output folder and the size in MB.";
	exit
fi

path=$1
size=$2

if [ -d $path ]; then
	echo "Directory already exists, aborting."
	exit 1
fi

lttng create -o "$path"

lttng enable-event -k -a

lttng start

echo "Trace started. Do something to generate events."

while [ $(du --summarize "$path" | cut -f 1) -lt $(($size * 1024)) ]
do
	sleep 1
done

lttng stop

lttng destroy

echo "Final size : " $(du -sh $path)

if [ -n "$SUDO_USER" ]; then
	user=$SUDO_USER
	group=$(id -ng $user)
	
	while true; do
		read -p "Do 'chown -Rv $user:$group $path' ? [y/n]" yesno
		case $yesno in
			[Yy]* )
				chown -Rv $user:$group $path
				break;
				;;
			[Nn]* )
				break;
				;;
		esac
	done
fi

