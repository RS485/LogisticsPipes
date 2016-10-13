# Building Logistics Pipes

If you are familiar with Minecraft Forge and Gradle or you are a Java developer
you should not have many problems starting with Logistics Pipes and your IDE.
But we have some required steps that you need to follow to make things work.

## Prerequisites

1. Having [git](http://git-scm.com/) or the GitHub app
([Windows](https://windows.github.com/)/[Mac](https://mac.github.com/))
installed
2. Having an IDE (we do use [Eclipse](https://eclipse.org/) and
[IntelliJ IDEA](https://www.jetbrains.com/idea/))
3. Having [Lombok](http://projectlombok.org/) installed with your IDE (in
IntelliJ IDEA there is a plugin for Lombok which requires annotation processing
to be enabled)
4. IRC wouldn't be bad if you want to communicate with other contributors.

## IRC

The server is esper.net and our channel is #RS485

If you have questions, be polite and have time until they get answered. Best
usage of IRC is to have an
[IRC Bouncer](http://en.wikipedia.org/wiki/BNC_%28software%29) (there are free
services around), because IRC does not guarantee instant answers on your
questions.

## Setting Up

### git

First off you need to clone Logistics Pipes from GitHub of course. If you only
want to browse the source, you can clone the official repository. But it is best
practice to actually fork the repository (yeah you need a GitHub account and yes
it is free) and clone your own copy. With the GitHub App cloning your own copy
is even easier as well.

The git command line is `git clone https://github.com/RS485/LogisticsPipes.git`
where you can replace RS485 by your own user name, if you forked the repository.

### Development environment

Once you have your copy of the Logistics Pipes sources, you can set up the
Minecraft sources and download Forge and everything by typing `gradlew extract
setupDecompWorkspace` in a console window in the very same folder, where you
just downloaded the sources to. This command will take a while, but download and
unpack everything you need.

Afterwards you need to set up the project files for your specific IDE. To do
that with gradle you only have two options:

* `gradlew eclipse` for Eclipse
* `gradlew idea` for IntelliJ IDEA

Then after opening the newly created project files with your IDE you need to add
the folders `api` and `dummy` to your list of source folders. You may need to
specify your Java JDK in the IDE as well. Logistics Pipes 0.10+ requires Java 8.

<strike>
Finally to use the predefined run/debug configurations with GradleStart you need
to add `--noCoreSearch` to the list of program arguments in both server and
client configurations. You can also set your ingame username by adding
`--username <name>` to the program arguments of the client config as well.
</strike>

Right now there is no official way on how to start Minecraft from your developer
environment. The striked paragraph above might still work for you though. This
might change with the switch to Minecraft 1.8+, because of ForgeGradle 2.0.

This should have set you up to build and run Minecraft with Logistics Pipes and
some included mods from your IDE. If something didn't work that well for you,
please leave an issue and tell us what happened and what you did to solve the
problem. **Happy coding!**
