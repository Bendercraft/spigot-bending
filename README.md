# Spigot-bending
Spigot plugins allowings players to bend elements at their will. Based on "Avatar" franchise. This plugin is officially hosted and used on "Bendercraft _.net_ ", french community of the franchise.

## Warning
We dropped the development of this plugin

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
We used gradle to generate our JAR file.
To be able to effectively use it, you need to find and download the libraries listed in `/libs/PUT-HERE` and put them in the /libs folder.
Once the libraries are set, you need to run the command (in the plugin root folder) : 
```bash
./gradlew build
```
The resulting .jar can be put in the /plugins/ folder of your minecraft server alongside the libraries.
Note that you need a mysql database to store the plugin's data.
