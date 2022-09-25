<!---
title: Crafting
icon: logisticspipes:crafting_table
--->
## Logistics Pipes Tutorial Part 3 - Automatic Crafting

In this part of the tutorial we explore how to use the items provided 
by our LP network to automatically craft other items.

### Crafting Pipes

Requesting something that is already in stock is useful, but requesting 
something that is not in stock, but we can craft, is even better and 
forms the basis of one of a LP network’s most powerful features. [Crafting 
Pipes](item://logisticspipes:pipe_crafting) tell the network that it can take a set of ingredients and feed them 
to the machine to which it is attached to create something else. As a 
quick test of this, let’s set up automatic crafting of wood planks using 
wood logs. Craft a Crafting Pipe and a [Logistics Crafting Table](item://logisticspipes:crafting_table) to connect 
it to. As before, we will need to use the Compiler and Programmer to create 
the Crafting Pipe.

Let’s place the Logistics Crafting Table in the world and choose a recipe 
for it. Unlike a regular crafting table, this one’s grid holds “ghosts” of 
the items needed for its crafting recipe rather than the items themselves; 
the grid’s job is simply to tell the crafting table what recipe to use. 
Items needed for crafting instead land in the inventory below the grid, 
which reserves some space for each slot in the grid. Let’s tell this one 
to craft wooden planks.

![LCT with a plank recipe](image://03-01-lct-recipe.png)

Now we need to connect a Crafting Pipe to the crafting table as well as 
the rest of the network, then use the [Pipe Manager](item://logisticspipes:pipe_manager) to open its settings. 
On this page we tell the pipe what the ingredients and products of its 
crafting recipe are. While we can set this up by hand, since this is 
connected to a Logistics Crafting Table we can simply press the “Import” 
button, and it will do that automatically.

Let’s open the [Request Pipe](item://logisticspipes:pipe_request) again. Now that something on the network can 
craft them, wood planks show up even though it shows that none are on hand. 
When we request one it will pull a block of wood out of storage, send it to 
the crafting table, and then extract the four planks that it crafted. The 
plank that we asked for will then make its way to the Request Pipe. The 
remaining three look for a pipe that can sink them and then go there so we
can store them to use them later.

![Request Pipe with craftable planks](image://03-02-craftable-planks.png)

Crafting Pipes can request ingredients from other Crafting Pipes, creating 
a “tree” of work to automatically do. By building a lot of Crafting Pipes 
and Logistics Crafting Tables we can make the LP network automatically 
craft all kinds of complex things. For instance, we may wish to automate 
crafting of more Crafting Pipes and other logistics pipes. In doing so 
we also need to automate crafting of [Basic pipes](item://logisticspipes:pipe_basic), [Unrouted pipes](item://logisticspipes:pipe_transport_basic), and so 
on, until the network has enough crafting recipes available to it that it 
can craft them all from raw materials.

The recipe for the Crafting Pipe is a little problematic because it 
contains a programmer that is not consumed during the crafting process. 
To handle this we can simply place the Programmer in the inventory of 
the crafting table with the Crafting Pipe’s recipe, then open the Crafting 
Pipe and remove the Programmer from the list of ingredients since the 
network does not have to craft more for the table to work. However, this 
dedicates the programmer we have used by hand up until now to automatically 
Crafting Pipe crafting, so we will need to craft a new one for our personal 
use instead. We will similarly need to craft dedicated Programmers for each 
other logistics pipe recipe that calls for one as well.

### Non-Crafting Recipes

Adding a recipe to a Crafting Pipe tells the network that if it inserts a 
set of ingredients into an inventory then the Crafting Pipe will eventually 
be able to extract the product of that recipe from the inventory to which 
it is attached. Crafting tables are the most obvious way of doing this, but 
Crafting Pipes can do other types of processing as well, such as smelting 
ore. By attaching a [Supplier Pipe](item://logisticspipes:pipe_supplier) to the side of a furnace to feed it with 
coal and a Crafting Pipe to the top to feed it iron ore we can tell the 
Crafting Pipe that the furnace will smelt one piece of iron ore into one 
iron ingot.

![This doesn't work](image://03-03-this-doesnt-work.png)

Or can we?

This setup doesn’t work because furnaces are sided – each side corresponds 
to a specific slot in the furnace’s inventory. To make automation with 
hoppers work well, the top feeds into the top slot, the sides feed into 
the fuel slot, and the bottom lets things pull from the output slot. The 
Crafting Pipe thus has to be on the bottom of the furnace, so it can pull 
iron ingots out of the output slot as they finish smelting. But since ore 
still has to go into the top of the furnace we need to craft a [Satellite 
Pipe](item://logisticspipes:pipe_satellite), which allows a Crafting Pipe to send some of its ingredients elsewhere – 
in this case, the top of the furnace.

![A Satellite Pipe](image://03-04-satellite-pipe.png)

After placing the Crafting Pipe on the bottom of the furnace and the Satellite 
Pipe on the top we need to tell them how to find each other. Use the Pipe 
Manager on the Satellite Pipe and give it a name.

![Naming the Satellite Pipe](image://03-05-naming-satellite-pipe.png)

Then we can go back to the Crafting Pipe and give it the same recipe, but 
this time using the Satellite Pipe. Clicking the “Select” button brings us 
to a list of available Satellite Pipes. After we choose one and go back to 
the Crafting Pipe’s recipe page the ingredients in the rightmost three slots 
will go to the Satellite Pipe we chose instead of the Crafting Pipe, so we 
just need to place a piece of iron ore in one of those slots instead of where 
we previously had it on the left.

![updated crafting recipe](image://03-06-satellite-recipe.png)

Now if we go to smelt iron ore the ore will go to the Satellite Pipe on the 
top of the furnace, while the Crafting Pipe waits for the ingot to appear on 
the bottom.

Some mods’ machines allow us to control which sides can access each part of 
its inventory, which can simplify things quite a bit. One crucial thing to 
remember when interactive with other mods is that machines must not automatically 
eject their crafting outputs into the LP network – the Crafting Pipe has to be 
the thing that pulls out crafting outputs for it to count the crafting operation 
as complete. This is in stark contrast with Applied Energistics, which expects 
us to take care of putting crafting outputs into the AE network and does not 
care where we do so.

In [part 4](page://dev_zero_guides/consolidating_pipes.md) we will work on 
combining pipes together to save ourselves future headaches.
