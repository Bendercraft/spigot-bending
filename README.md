# Spigot-bending
Spigot plugins allowings players to bend elements at their will. Based on "Avatar" franchise. This plugin is officially hosted and used on "Avatar-Realms", french community of the franchise.

## Features
 - Allow players to choose their element, either once or whenever they want
 - Choose if player unlocks gradually their abilities by using the ones they start with, or if they know everything before hands using permissions
 - Choose to deepen your bending with a specialization
 - Tweak your existing bending at your will with a perk system: "path"
 - Choose the Avatar, the one that can bend all 4 elements !
 
## Integration
 - Protect regions from specific bendings or all of them with WorldGuard and new flags
 - Allow your Citizens NPC to be "bendable" by adding a new trait to them
 
## Build & install
We currently use "Ant" to generate our JAR file. A build config has been provided under "build.xml.example". With it, you just need to execute ant with target "deploy".
An another target: "sonar" is provided in case you want your plugin to perform static analysis by a SonarQube instance.