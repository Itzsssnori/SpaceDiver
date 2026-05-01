#!/bin/bash
# Space Diver - Compile and Run Script for macOS/Linux

echo "Compiling Space Diver..."
cd src
javac SpaceDiver.java

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

echo "Compilation successful! Starting game..."
java SpaceDiver

