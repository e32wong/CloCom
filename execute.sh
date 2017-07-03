#!/bin/sh

# Argument is the path of the configuration file
java -Xmx5000m -cp "./lib/*:./lib2/*:." CloneDigger -configPath $1
