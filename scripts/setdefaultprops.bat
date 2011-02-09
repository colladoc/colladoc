@echo off

setlocal

:: Shell script to setup Colladoc's default.prop file with the sourcepath and
:: and classpath values needed to run.
::
echo Running setdefaultprops...
echo .

:: Figure out the full path of the colladoc/ folder
set colladocfolder=%~dp0..

:: Make sure that we use a full path for the source path.
set sourcepath=%colladocfolder%\..\demoproject
echo Using Sourcepath: %sourcepath%

:: and the props file we are writing to.
set propsfile=%colladocfolder%\src\main\resources\props\default.props

:: All paths in the props file need to have '/'
set colladocfolder=%colladocfolder:\=/%
set sourcepath=%sourcepath:\=/%

:: The list of compiler jars that need to be in the classpath. If the demo project
:: needs other libs then you need to add them yourself.
set jars=scala-compiler.jar ^
         scala-dbc.jar ^
         scala-library.jar ^
         scala-partest.jar ^
         scalap.jar ^
         scala-swing.jar

:: Here is the folder that contains these jars.
set classpath=%colladocfolder%/scala/build/pack/lib/

:: Now we have everything we need to write the props file.
echo Writing properties to %propsfile%.
echo # Default properties> %propsfile%
echo -doc-title=Colladoc>> %propsfile%
echo -doc-version=1.0-SNAPSHOT>> %propsfile%
echo -sourcepath=%sourcepath%>> %propsfile%
set /p =-classpath=<nul>> %propsfile%

for %%j in (%jars%) do (
    set /p =%classpath%%%j;<nul>>%propsfile%
)

echo Done.

endlocal