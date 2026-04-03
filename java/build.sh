#! /bin/bash
#
# Tested on macOS with Java 24 installed via::
#
#   brew install openjdk
#   
# and, as per the install instructions, making::
#
#   /Library/Java/JavaVirtualMachines/openjdk.jdk
#
# be a symlink into it.

set -e
cd "$(dirname "$0")"
mkdir -p build

# Can use below to force Java version, if needed.
#
# if [[ -f "/usr/libexec/java_home" ]]; then
#     export JAVA_HOME=$(/usr/libexec/java_home -v1.8)
# fi

echo "Using Java: $(javac -version 2>&1)"

echo "Compiling..."
cd KnotJob
javac -encoding UTF8 -d ../build knotjob/**.java
cd ..

echo "Building jar file..."
cd build
jar cmf ../MANIFEST.MF ../KnotJob.jar knotjob
cd ..

echo "Testing..."
java -jar KnotJob.jar -kr0 -s0 -sgr -sq2e -sq2o0 -sq2o1 -ks0 < test_knots.txt

echo "Copying to python_src..."
cp KnotJob.jar ../python_src

echo "Build complete!"
