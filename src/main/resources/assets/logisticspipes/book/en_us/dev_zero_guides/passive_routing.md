<!---
title: Passive Routing
icon: logisticspipes:module_item_sink
--->

## Logistics Pipes Tutorial Part 1 - Sorting Items

In this part of the tutorial we craft our first logistics pipes 
and build a basic item-sorting system.

### Getting Started

The Logistics Pipes mod has two major types of pipes. Routed pipes 
connect to each other to form a network that can intelligently 
pass items between connected pipes and the rest of the world. 
[Unrouted pipes](item://logisticspipes:pipe_transport_basic) lack 
the intelligence of routed pipes; they connect only to other pipes 
to provide transportation between them.

Let’s build a simple item-sorting system using logistics pipes. 
Start with some chests.

![Some chests](image://01-01-some-chests.png)

Logistics pipes require power to operate. To supply this power 
we construct a [Power Junction](item://logisticspipes:power_junction) 
and then connect it to some sort of generator, such as this 
coal-powered generator.

![Power Junction connected to a generator](image://01-02-power.png)

To actually sort items, let’s craft some [Basic Logistics Pipes](item://pipe_basic) 
and attach one to each chest. A Basic pipe also needs to be 
connected to a side of the Power Junction (not the top or bottom), 
but it is fine for one pipe can do double-duty and connect to both. 
If there is space in between the basic pipes we can connect them 
to each other using unrouted pipes.

![Pipes attached to the chests](image://01-03-pipes.png)

### Passive Routing

When a routed pipe such as one of these Basic pipes encounters 
an item it will attempt to find a _sink_ for that item – a pipe 
that can accept it and add it to the chest or other inventory 
to which it is connected. It will then _sink_ the item by sending 
it in that direction. Each routed pipe along the way will send 
it in the right direction until it finally reaches the pipe that 
offered to sink the item. That pipe then adds it to its chest.

Each Basic pipe contains a list that we can use to control 
which types of items that it can sink. To edit this list, 
let’s craft a [Pipe Manager](item://logisticspipes:pipe_manager), 
right-click a pipe, and add some items to its list.

Wrenches from many other mods can substitute for the Pipe Manager 
as well.

![The Basic pipe configuration dialog](image://01-04-config.png)

We can continue this process for the other pipes to assign 
different types of items to different chests. We also need to 
set at least one pipe as a _default route_, a pipe that can sink 
items that have nowhere else to go. A LP network with no default 
route will drop items on the floor if it cannot find a place to 
sink them.

![Setting a default route](image://01-05-default-route.png)

All that is left is to connect a hopper for adding items to the 
network. The sorting system is now complete! Let’s drop some items 
into the hopper and watch it work.

![Sorting items](image://01-06-working.png)

In [part 2](page://dev_zero_guides/active_routing.md) we will expand 
on this system to make its inventory more accessible.
