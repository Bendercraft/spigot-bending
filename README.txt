This code is based on the Orion304's code.
TODO :
Re-structure and re-optimize the code
Add more customizations
Add some abilities

Modifications done to fit with the french server : www.avatar-state.net


Maven local dependencies :

mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file  -Dfile=path-to-your-artifact-jar -DgroupId=your.groupId -DartifactId=your-artifactId -Dversion=version -Dpackaging=jar




mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file  -Dfile=lib/factions.jar -DgroupId=net.minecrat -DartifactId=factions -Dversion=2.3.1 -Dpackaging=jar
mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file  -Dfile=lib/mcore.jar -DgroupId=net.minecrat -DartifactId=mcore -Dversion=7.1 -Dpackaging=jar
mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file  -Dfile=lib/worldedit.jar -DgroupId=net.minecrat -DartifactId=worldedit -Dversion=5.5.9 -Dpackaging=jar
mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file  -Dfile=lib/worldguard.jar -DgroupId=net.minecrat -DartifactId=worldguard -Dversion=5.8.1 -Dpackaging=jar