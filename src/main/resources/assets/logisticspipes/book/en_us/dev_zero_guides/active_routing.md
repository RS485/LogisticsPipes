<!---
title: Active Routing
icon: logisticspipes:pipe_supplier
--->

## Logistics Pipes Tutorial Part 2 - Active Routing

In this part of the tutorial we make it possible to request items from storage.

### Provider Pipes

Previously, we discussed passive routing, in which a Logistics Pipes network
decides what to do with an item when one presents itself. In addition to this,
LP networks support _active routing_, in which pipes can _provide_ items to the
network so other pipes can actively request them. When that happens the pipe
providing an item will pull it out of its inventory and send it toward the
pipe that requested it. By crafting some new types of logistics pipes we can
use this to pull items out of storage without having to know which chests
contain them.

Pipes more complicated than a Basic pipe need a little more infrastructure
to craft. Let’s begin by crafting the [Logistics Program Compiler](item://logisticspipes:program_compiler), placing
it in the world, and providing it with power by attaching a [Basic pipe](item://logisticspipes:pipe_basic) to
its side and connecting that to the rest of the LP network.

![The Compiler](image://02-01-compiler.png)

The Compiler provides us with the programs needed to craft each type of
pipe, but to get access to those we first need to unlock them. Click “Basic”,
then “Unlock” to start the process.

![Unlocking Basic tech](image://02-02-basic.png)

This will take some time. While we wait for it to complete we can craft a
[Logistics Programmer](item://logisticspipes:logistics_programmer). The Programmer
is part of the crafting recipes for
pipes and more. The Compiler builds a program specific to one type of pipe
and loads it onto the Programmer for use in crafting.

Let’s craft some [Provider pipes](item://logisticspipes:pipe_provider), which
will provide the items we have in
storage to the LP network. When the Compiler is ready, open it, click
“Provider Logistics Pipe”, then click “Compile”. This is only necessary
once per type of pipe; the [disk](item://logisticspipes:disk) we inserted
earlier stores it for future
use. When that finishes, we need only put the Programmer in the slot on
the top right of the Compiler and click “Flash” to load the program for
that type of pipe onto the Programmer.

![Flashing the Programmer](image://02-03-flashing.png)

With this program loaded we can finally use the Programmer to craft some
Provider pipes. Thankfully, while it is part of pipe’s recipe, the Programmer
is not consumed in the process. After crafting a few we can connect one to
each chest that we want to provide to the network.

![Provider pipes](image://02-04-provider-pipes.png)

### Request Pipes

To request items from the Provider pipes we just placed we can use a [Request
pipe](item://logisticspipes:pipe_request), which will serve as our way of accessing
the contents of the LP network.
Crafting this works the same way as the Provider pipe: go back to the Compiler,
compile the “Request Logistics Pipe” program, and load it onto the Programmer.
Then we can use it to craft pipes as before.

We can connect the Request pipe to the network anywhere. If the pipe leads to
a dead end it will drop whatever you request on the floor. If it is connected
to a chest it will instead add whatever you request to the chest.

![Request pipe with an unrouted intersection](image://02-05-request-pipe-err.png)

Here we placed a Request pipe overhead, where we can order items to be dropped
into our inventory. Notice that one of the unrouted pipes on the left turned
red, indicating an issue. Unrouted pipes do not know where to send incoming
items, so if an item arrives at an unrouted pipe, and it has more than one
direction that it can send it then it will choose a direction at random. If
it chooses the wrong direction the next routed pipe the item encounters will
send it back where it came from and the unrouted pipe will randomly choose a
direction again. This wastes time. We can ensure that items always go in the
correct direction by replacing the pipe at the intersection with a pipe that
can do routing – a Basic pipe. As a general rule, never use an unrouted pipe
at an intersection.

![Request pipe with a routed intersection](image://02-06-request-pipe-fixed.png)

With that problem resolved we can now request items from the LP network. Upon
right-clicking the Request pipe we are presented with a box that shows every
item provided by the network. Click an item in the list on the top, choose how
many to request on the bottom, and click “Request”. The Provider pipes with the
item we requested will pull that item out of its chest and send it to the Request
pipe we used to ask for it.

It is important to note that the two pipes connected to each chest play different
roles: the Basic pipes are sinks for items and do not provide items to the network,
while the Provider pipes provide items to the network and cannot sink items themselves.

### Supplier Pipes

The generator that powers our stuff needs a continuous source of coal to keep running.
We can automate this with a [Supplier pipe](item://logisticspipes:pipe_request), an
automated version of the Request pipe that monitors an inventory and tries to keep
items in stock by requesting more when needed. Go back to the Compiler, compile and
flash the “Supplier Logistics Pipe” program, and use it to craft a Supplier pipe.

Now let’s attach the Supplier pipe to the generator and right-click it with
the Pipe Manager to tell it how many to keep in stock. If the item is on hand
this is easy: just click the item into the list. If the item is not on hand but
JEI is installed then we can instead click an item in JEI’s list to get a
“ghost” item that we can place into the list instead. In either case we can
use the mouse wheel to change the number to keep in stock.

![Supplier pipe attached to the generator](image://02-07-supplier-pipe.png)

The Supplier pipe can use any of several strategies to keep items in stock:

- Partial: request more items every time too few are in stock
- Bulk50: wait until only half of the desired amount is in stock, then order enough
  to refill
- Bulk100: wait until none of the desired item remains, then order enough to refill
- Full: same as Bulk100, but do not request anything unless the entire order can
  be fulfilled
- Infinite: ignore the requested number of items and continue requesting more until
  the inventory is full

I prefer to use Bulk50 for most things. It reduces the number of requests in
flight at once, which is more power-efficient and can make machines spend less
time busy. Infinite mode is useful for pulling all of a certain item out of a network.

In [part 3](page://dev_zero_guides/crafting.md) we will add automatic crafting
capabilities to our LP network.  

