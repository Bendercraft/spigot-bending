This code is based on the Orion304's code.
Modifications done to fit with the french server : www.avatar-state.net


Maven local dependencies :

mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file  -Dfile=path-to-your-artifact-jar -DgroupId=your.groupId -DartifactId=your-artifactId -Dversion=version -Dpackaging=jar


mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file  -Dfile=./lib/factions-2.5.0.jar -DgroupId=net.minecraft -DartifactId=factions -Dversion=2.5.0 -Dpackaging=jar
mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file  -Dfile=./lib/massivecore-7.3.0.jar -DgroupId=net.minecraft -DartifactId=massivecore -Dversion=7.3.0 -Dpackaging=jar
mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file  -Dfile=./lib/worldedit-5.6.3.jar -DgroupId=net.minecraft -DartifactId=worldedit -Dversion=5.6.3 -Dpackaging=jar
mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file  -Dfile=./lib/worldguard-5.9.jar -DgroupId=net.minecraft -DartifactId=worldguard -Dversion=5.9 -Dpackaging=jar
mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file  -Dfile=./lib/CoreProtect_2.0.9.jar -DgroupId=net.minecraft -DartifactId=coreprotect -Dversion=2.0.9 -Dpackaging=jar
