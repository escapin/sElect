#!/bin/bash

# Requires 'xdotool'
# Works with Firefox in autofocus
# sElect settings must be: {userChosenRandomness: false; showOtp: true}

numberOfChoices=8
maxChoicesPerVoter=3
maxTabs=$(($numberOfChoices/$maxChoicesPerVoter))
email="voter+"
provider="@test.org"
numberOfVoters=50
counter=0

sleep 3
while [ $counter -lt $numberOfVoters ]; do
	sleep 1.8
	xdotool key Tab
	xdotool type $email$counter$provider
	xdotool key Return
	sleep 1
	xdotool key Return
	sleep 0.3
	xdotool key Tab
	sleep 0.3
	xdotool key Return
	sleep 1
	choices=$(( ( RANDOM % $maxChoicesPerVoter ) +1 ))
	loop=0
	while [ $loop -lt $choices ]; do
		tabs=$(( ( RANDOM % $maxTabs ) +1 ))
		ccounter=0
		while [ $ccounter -lt $tabs ]; do
			xdotool key Tab
			let ccounter=ccounter+1
		done 
		sleep 0.3
		xdotool key space
		sleep 0.3
		let loop=loop+1
	done
	xdotool key Return
	sleep 1
	xdotool key ctrl+r
	let counter=counter+1
done

