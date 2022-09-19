# Building Logistics Pipes

If you are familiar with Minecraft Forge and Gradle or you are a Java developer
you should not have many problems starting with Logistics Pipes and your IDE.
But we have some required steps that you need to follow to make things work.


## Prerequisites

1. Having [git](http://git-scm.com/) and git-lfs installed
2. Having an IDE (we do use [IntelliJ IDEA](https://www.jetbrains.com/idea/))
3. Having Java Development Kit 8; newer versions are not supported by this
   Minecraft or Minecraft Forge version.
4. Matrix/Discord wouldn't be bad if you want to communicate with other
   contributors and us.


## Matrix

[Logistics Pipes Space](https://matrix.to/#/#logisticspipes+space:rs485.network)

- [Dev Channel](https://matrix.to/#/#logisticspipes+dev:rs485.network)

The channels are all linked to Discord and some even to IRC.


## How to set up a local development environment

### git

First off you need to clone Logistics Pipes from GitHub of course. If you only
want to browse the source, you can clone the official repository. But it is best
practice to actually fork the repository and clone your own copy.

The git command line is `git clone https://github.com/RS485/LogisticsPipes.git`
where you can replace *RS485* by your own user name, **if** you forked the
repository.


### git-lfs

If you didn't have git-lfs installed at the time of cloning the git repository,
jar files are "broken", because lfs did not download and replace them.
You can manually run the following commands to fix the situation peacefully:

```shell
$ git lfs install
$ git lfs fetch
$ git lfs checkout
```


### Gradle

Make sure you are running Gradle with Java 8: You can check the default
java version with `java -version` and control the version Gradle uses with
the `JAVA_HOME` environment variable. LP can be built with `./gradlew build`
and the output found in the directory `build/libs`.
If the task fails there may be something wrong with maven repositories or
[a Java update broke ForgeGradle 2](https://github.com/MinecraftForge/ForgeGradle/issues/652)
or something may be wrong with your setup. You may definitely ask for help on
the mentioned communication channels above, but please be sure to state your
issue as good as possible and be nice to others.

### IDEA Quirks

For IDEA to use the correct Java SDK, you might have to select the correct
JDK (Java JDK 8, OpenJDK build preferred) in `File > Project Structure` in the
project part of the project settings under SDK.

### Running Minecraft from your dev environment

After you successfully built LP you can probably run Minecraft directly from
your IDE after running the correct ForgeGradle generate run configuration task.
Please look at ForgeGradle documentation for more information.

If you are not using IntelliJ IDEA, there may be a ton of missing texture
errors and missing language files. The cause is newer ForgeGradle versions.
Our ForgeGradle version and our build script contain fixes for IntelliJ IDEA
only; sorry if you are not using IDEA. The solution is to remove
`build/classes/*/*` and `build/resources/*` from the run configuration
classpath and add the custom `build/run_classes` path to the classpath.
We are open for any contributions for a better solution or wider support.

The workaround is to copy your `build/resources/main/*` into 
`build/classes/java/main` before launching the game, or you may link those two
 together (Linux or WSL):

```shell
$ rm -r build/resources/main && ln -s ../classes/java/main build/resources/main
```

Windows `cmd`:

```
rd /s /q "build\\resources\\main" && mklink /D "build\\resources\\main" "..\\classes\\java\\main"
```
