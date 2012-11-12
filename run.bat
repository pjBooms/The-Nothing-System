set JDK_DIR=C:\jdk1.6.0
pushd sysDir
java -cp .;%JDK_DIR%/lib/tools.jar com.excelsior.nothing.Main
popd
