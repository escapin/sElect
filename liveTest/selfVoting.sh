#!/bin/bash

# Requires 'xdotool'
# sElect settings must be: {userChosenRandomness: false; showOtp: true}
# Works ONLY with Firefox in autofocus, with the keyboard layout set to "English(US)"


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
	echo -n -e "\rSimulated voters so far: $counter"
	let counter=counter+1
done

