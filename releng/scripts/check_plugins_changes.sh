#!/bin/bash
###############################################################################
# Copyright (c) 2016 Ericsson
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
###############################################################################

# A script that checks every plugin for modifications since last release
# and prompt to bump the micro version if necessary.
#
# Usage  ./check_plugins_changes.sh [ignoredCommit1,ignoredCommit2,...]
#
# Where ignoredcommit is a 7 characters commit hash which will not be
# considered in the diffs (useful for big commits that didn't affect code)
#
# For example ./check_plugins_changes.sh 1325468,1325469

IFS=', ' read -r -a IGNORED_COMMITS <<< "$1"

PREV_RELEASE_VERSION=$(git tag -l | tail -1 | cut -c 2-)
echo "Baseline version detected: $PREV_RELEASE_VERSION. If this is wrong, stop the script (Ctrl-C)"
read -rsp $'Press any key to continue...\n' -n1 key

# Stats for the summary
num_new_plugins=0
num_bumped_already=0
num_bumped=0
num_not_bumped=0
num_no_bump_needed=0

ALL_PLUGIN_PATHS=($(dirname $(dirname $(find ../.. -name "MANIFEST.MF"))))

#For each plugin
for plugin_path in "${ALL_PLUGIN_PATHS[@]}"; do

	commit_summary=$(git log  --oneline --max-count=1 -- $plugin_path)
	commit_id=$(echo $commit_summary | awk '{print $1}')

	# Check if the commit we are about to consider should be ignored
	# and choose a better one if that's the case.
	check_ignored_commit=1
	while [ $check_ignored_commit -eq 1 ]; do
		check_ignored_commit=0
		for ignored_commit in "${IGNORED_COMMITS[@]}"; do
			if [ "$ignored_commit" = "$commit_id" ]
			then
				echo Ignoring commit $commit_id
				commit_summary=$(git log $commit_id~1  --oneline --max-count=1 -- "$plugin_path")
				commit_id=$(echo $commit_summary | awk '{print $1}')
				echo New commit: $commit_id
				check_ignored_commit=1
			fi
		done
	done

	manifest_diff=$(git diff v$PREV_RELEASE_VERSION -- $plugin_path/META-INF/MANIFEST.MF)
	is_new_file=$(echo "$manifest_diff" | grep "\-\-\- /dev/null")

	# We don't need to do anything for a new pluging that wasn't there
	# before, the initial version is always good
	if [[ -n "$is_new_file" ]]
	then
		echo "new plugin   $plugin_path"
		num_new_plugins=$((num_new_plugins+1))
		continue
	fi

	old_version=$(echo "$manifest_diff" | grep "\-Bundle-Version" | cut -c 2-)
	cur_version=$(grep Bundle-Version "$plugin_path/META-INF/MANIFEST.MF")

	plugin_diff=$(git diff v$PREV_RELEASE_VERSION $commit_id -- "$plugin_path")
	# Is the plugin bump needed? Check if the last commit of the plugin matches the previous release tag
        # or if there was no difference (aside from the ignored commits)
	tags_containing=$(git tag --contains $commit_id)
	if [[ -z "$plugin_diff" || ($tags_containing == *"$PREV_RELEASE_VERSION"*) ]]
	then
		echo "no update needed  $plugin_path   ($cur_version)"
		num_no_bump_needed=$((num_no_bump_needed+1))
		continue
	fi

	# Is the plugin already bumped? Check if versions are different
	if [[ -n "$old_version" && ("$cur_version" != "$old_version") ]]
	then
		echo "bumped already   $plugin_path   ($old_version -> $cur_version)"
		num_bumped_already=$((num_bumped_already+1))
		continue
	fi

	# At this point, we have a potential version bump necessary. We will ask the user to decide what to do.

	cur_major_minor=$(echo $cur_version | sed -rn 's/Bundle-Version:\s([0-9][0-9]*\.[0-9][0-9]*).*/\1/p')
	cur_micro=$(echo $cur_version | sed -rn 's/Bundle-Version:\s[0-9][0-9]*\.[0-9][0-9]*\.([0-9][0-9]*).*/\1/p')
	next_micro=$cur_micro
	let next_micro+=1
	git diff v$PREV_RELEASE_VERSION $commit_id -- "$plugin_path"
	echo "Might need bump:  $plugin_path   ($cur_version)   ($commit_summary)"
	while true; do
		read -p "Bump version from $cur_major_minor.$cur_micro to $cur_major_minor.$next_micro? (y/n) " answer
		case $answer in
			[Yy]* ) break;;
			[Nn]* ) break;;
			* ) echo "yes (y) or no (n).";;
		esac
	done

	if [[ $answer == "Y" || $answer == "y" ]]
	then
		num_bumped=$((num_bumped+1))
		sed -i -E  's/(Bundle-Version:\s)[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*(.*)/\1'$cur_major_minor.$next_micro'\2/g' "$plugin_path/META-INF/MANIFEST.MF"
	else
		num_not_bumped=$((num_not_bumped+1))
	fi
done

# Print a little summary of how the plugins were processed

echo Total plugins: ${#ALL_PLUGIN_PATHS[@]}
echo New: $num_new_plugins
echo Already bumped: $num_bumped_already
echo Bumped: $num_bumped
echo Not bumped by choice: $num_not_bumped
echo No bump needed: $num_no_bump_needed

num_processed=$(($num_new_plugins + $num_bumped_already + $num_bumped + $num_not_bumped + $num_no_bump_needed))
if [[ $num_processed -ne ${#ALL_PLUGIN_PATHS[@]} ]]
then
	echo "Number of plugins processed mismatch! ($num_processed vs ${#ALL_PLUGIN_PATHS[@]})"
else
	echo "All plugins processed."
fi
