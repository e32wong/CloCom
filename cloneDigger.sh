#!/bin/sh

# Argument is the path of the configuration file
java -Xmx3g -cp "./lib/*:." CloneDigger -loadConfig $1
