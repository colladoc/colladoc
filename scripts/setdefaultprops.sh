#!/bin/bash
# Shell script to setup Colladoc's default.prop file with the sourcepath and
# and classpath values needed to run.
#
echo Running setdefaultprops [sourcepath]...
echo 

origpath=$(pwd)

# Figure out the full path of the colladoc/ folder
cd $(dirname $0)/..
colladocfolder=$(pwd)
cd $origpath

# Make sure that we use a full path for the source path.
sourcepath=$1
if [ $# == 0 ]
    then
        # No source path given so use a default.
        sourcepath=$colladocfolder/../demoproject
        echo No sourcepath was given. Defaulting to:
        echo $sourcepath
        echo 
fi

if [ ! -d $sourcepath ]
    then
        # Source Path does not exist.
        echo Sourcepath does not exist or is not a directory.
        echo $sourcepath
        echo 
        exit 100    # 100
fi

cd $sourcepath
sourcepath=$(pwd)

# and the props file we are writing to.
propsfile=$colladocfolder/src/main/resources/props/default.props

# Now we have everything we need to write the props file.
echo Writing properties to $propsfile.
echo '# Default properties'> $propsfile     # Overwrite all existing file contents here.
echo -doc-title=Colladoc>> $propsfile
echo -doc-version=1.0-SNAPSHOT>> $propsfile
echo -sourcepath=$sourcepath>> $propsfile
echo -n -classpath=>> $propsfile

# Here is the folder that contains the jars we need to put in the classpath.
classpath=$colladocfolder/scala/build/pack/lib/

# Puts the base classpath requirement jars in the props file. The classpath may
# need to be edited manually if the project being modeled in colladoc requires
# additional libs.
echo -n $classpath$'scala-compiler.jar:'$classpath$'scala-library.jar:'>>$propsfile

echo Done.
