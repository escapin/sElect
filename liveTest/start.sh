#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

gnome-terminal --working-directory=$DIR/.. -e 'make cleanElection'
gnome-terminal --working-directory=$DIR/../CollectingServer -e './run.sh'
gnome-terminal --working-directory=$DIR/../VotingBooth -e './run.sh'
gnome-terminal --working-directory=$DIR/../BulletinBoard -e './run.sh'
gnome-terminal --working-directory=$DIR/../mix/00 -e './run.sh'
gnome-terminal --working-directory=$DIR/../mix/01 -e './run.sh'
gnome-terminal --working-directory=$DIR/../mix/02 -e './run.sh'
gnome-terminal --working-directory=$DIR/../Authenticator -e './run.sh'
