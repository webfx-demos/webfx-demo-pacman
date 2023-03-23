@echo on
rem Builds the Pac-Man Java FX implementation and deploys into a zip file
rem Expects that the Git repositories "pacman-basic" and "pacman-javafx" are locally available
pushd ..\..\pacman-basic\pacman-core
call mvn clean install
popd
call mvn clean install
popd
call mvn -f deploy.xml clean install
popd
