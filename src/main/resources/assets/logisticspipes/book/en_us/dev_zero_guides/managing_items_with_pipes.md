<!---
title: Managing Items with Modules
icon: logisticspipes:module_blank
--->
## Logistics Pipes Tutorial Part 5 - Managing Items with Modules

In this part of the tutorial we use more types of modules to 
improve our item management.

### Upgrades

After returning home from a log mining trip one of the first 
things we want to do is empty our inventory out. Hoppers work great 
for this, but they only pull from their tops and they are not 
especially fast at moving large numbers of items. Let’s build 
an [Extractor Module](item://logisticspipes:module_extractor), 
which constantly pulls items out of whatever it is attached to and 
injects them into the LP network to be sunk somewhere. Then we just 
need to craft a [Chassis](item://logisticspipes:pipe_chassis_mk1), 
attach that Chassis to a chest, and add that module to it. Now we can 
dump everything that we want to get rid of into this chest and it will 
make its way into the network.

![Dump chest](image://05-01-dump-chest.png)

Out of the box, this module actually runs more slowly than 
a hopper, extracting one item every five seconds. We can 
improve this with [Item Extraction Upgrades](item://logisticspipes:upgrade_item_extraction), 
which increase the number of items it extracts each time, and [Action Speed 
Upgrades](item://logisticspipes:upgrade_action_speed), which 
increase how frequently it runs. I tend to 
use five or six Action Speed Upgrades and a few more Item 
Extraction Upgrades for this sort of item-dumping chest, but 
they can go as high as 16 of each if necessary. Remember that 
extraction uses power.

We can’t use the [Pipe Controller](item://logisticspipes:pipe_controller) to add these upgrades 
since we need to add them to the [Extractor Module](item://logisticspipes:module_extractor), not 
the Chassis. Instead, we need to craft a [Module Upgrade](item://logisticspipes:upgrade_module_upgrade), 
an upgrade that, when installed into a Chassis, gives 
us slots to add upgrades to individual modules. After 
adding that to the Chassis we can grab the [Pipe Manager](item://logisticspipes:pipe_manager), 
open the Chassis, and add modules in the new slots that 
appeared on the right.

![Module upgrades](image://05-01-dump-chest.png)

[Item Extraction Upgrades](item://logisticspipes:upgrade_item_extraction) also work on [Provider](item://logisticspipes:pipe_crafting) and [Crafting 
Pipes](item://logisticspipes:pipe_crafting). This can be useful when something needs to request 
an enormous quantity of something, as the extraction speed 
of the pipe itself can otherwise be a bottleneck. The same 
goes for [Provider](item://logisticspipes:module_provider) and [Crafting Modules](item://logisticspipes:module_crafter).

### Item Sink Priority

When an item enters the network and more than one pipe 
can serve as a sink for it the network uses a list of 
priorities to decide where it is needed most:

- [Passive Supplier Modules](item://logisticspipes:module_passive_supplier)
- Item Sinks, including [Basic Pipes](item://logisticspipes:pipe_basic) and all ItemSink Modules
- [Terminus Modules](item://logisticspipes:module_terminus)
- Default routes

[Passive Supplier Modules](item://logisticspipes:module_passive_supplier) are largely a relic from the 
time before [Active Supplier Modules](item://logisticspipes:module_active_supplier) existed. They attempt 
to keep a certain number of an item in stock, but unlike 
[Supplier Pipes](item://logisticspipes:pipe_supplier) and [Active Supplier Modules](item://logisticspipes:module_active_supplier), they will not 
request them from the network. Instead, when an item a 
Passive Supplier Module needs enters the network it will 
sink that item before anything else has a chance to do so.

[Terminus Modules](item://logisticspipes:module_terminus), with their lower-than-normal priority, 
usually serve as a means of getting rid of excess items 
once storage for it fills up. A LP network will frequently 
have a Terminus Module that sinks excess cobblestone and 
dirt into a trash can, matter condenser, or even pool of 
lava so that it doesn’t fill up the default route’s storage.

![Trash can](image://05-01-dump-chest.png)

### Re-sorting Items

Supplier Pipes, Crafting Pipes, and similar things are 
notably absent from the list above. Supplier Pipes do not 
sink items when they enter the network because they use 
active routing, which can only request items from providers. 
By contrast, Basic Pipes and other item sinks do not request 
items because they use passive routing, which means they can 
only respond to items entering the network that need a place 
to go.

This dichotomy can become a problem for the default route. 
Even if it has a provider attached to it, the default route 
may not empty itself out over time; there is no priority 
system for providers. If the default route runs out of space 
then the network will begin spilling items onto the floor. 
This is where the [QuickSort Module](item://logisticspipes:module_quick_sort), along with a little 
attention on our part, can come in handy.

A QuickSort module inspects the items it has and looks for 
somewhere else on the network that can sink it that is not a 
default route. If it finds one it injects it back into the 
network so the network can re-sink it. This is a valuable way 
to keep a default route chest clean – when storage becomes 
available for something the QuickSort module sees to it that 
it goes elsewhere. Let’s put this into practice by upgrading 
our default route’s Mk2 Chassis to Mk3 and then add a 
QuickSort Module to it.

![QuickSort Module](image://05-01-dump-chest.png)

With this setup in place we can check the chest periodically 
to see if anything is building up. If it is then we can take 
one out of the chest, place it in a chest with a [Polymorphic 
sink](item://logisticspipes:module_item_sink_polymorphic), and the [QuickSort Module](item://logisticspipes:module_quick_sort) will quickly move the rest of 
that type of item over. Or we can add it to a Terminus Module 
somewhere and have the QuickSort Module send it there for 
disposal. Or we can do something more involved. This combination 
of modules makes for an efficient and easy-to-manage system 
for item storage.

[QuickSort Module](item://logisticspipes:module_quick_sort) are also great alternatives to Extractor Modules 
for a factory’s post-outing, item-dumping chest. They have the 
added benefit of not extracting items that have nowhere other 
than the default route to go, which makes it immediately obvious 
when something is in need of storage. In fact, in most of my 
factories the default route and the item-dump chest are one and 
the same.

A word of warning: do not place a QuickSort Module in the same 
place as an item sink unless that item sink is a default route. 
If you choose to ignore this warning, do not do so more than 
once in a given network or the QuickSort Modules will endlessly 
trade items with one another.
