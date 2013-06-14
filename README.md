#LogisticsPipes

Logistics Pipes is an extensive overhaul of the Buildcraft pipe system. It allows for better distribution of items via pipes, more organised stockkeeping and easier automated crafting.

Look into the [Minecraft Forum Thread](http://www.minecraftforum.net/topic/1831791-) for more information.
We advice anyone to go there, who is not a developer.

You are free to use this mod in your modpack, because its released under the MMPL.

Credits for the code go to all [contributors](https://github.com/RS485/LogisticsPipes/contributors).
Credits for the idea and basic code go to Krapht.

##Download

[Releases](http://ci.thezorro266.com/job/LogisticsPipes/)

[Developer builds](http://ci.thezorro266.com/job/LogisticsPipes-dev/)
_Note: Developer builds are not always stable._

##Builds

[Jenkins](http://ci.thezorro266.com/view/Logistics Pipes/)

###Building

1. Clone this repo to a folder named src inside an empty folder
2. Run `ant setup` or `ant vars package-simple` if you want to keep mcp/forge instance, but setup must be performed before

###Developing

1. Set up Minecraft Forge and your IDE
   * Add forge/mcp/src/minecraft as source
   * Add all the libraries that come with Forge/Minecraft to the build path
2. Clone this repo and the BuildCraft repo
   * Add <buildcraft>/common as source
   * Add <buildcraft>/buildcraft_resources as source
   * Add <logisticspipes>/common as source
   * Add <logisticspipes>/dummy as source
   * Add <logisticspipes>/resources as source
3. Setup your IDE for [`lombok`](http://projectlombok.org/download.html)
4. Get all the APIs and add them as source
   * ComputerCraft API
   * Forestry API
   * IndustrialCraft 2 API
   * Thaumcraft API
   * Thermal Expansion API
