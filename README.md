## TemplateDevEnv

Template workspace for modding Minecraft 1.12.2. Licensed under MIT, it is made for public use.

This template currently utilizies **Gradle 8.1.1** + **[RetroFuturaGradle](https://github.com/GTNewHorizons/RetroFuturaGradle) 1.3.19** + **Forge 14.23.5.2860**.

With **coremod and mixin support** that is easy to configure.

### Instructions:

1. Click `use this template` at the top.
2. Clone the repository you have created with this template.
3. In the local repository, run the command `gradlew setupDecompWorkspace`
4. Open the project folder in IDEA.
5. Right-click in IDEA `build.gradle` of your project, and select `Link Gradle Project`, after completion, hit `Refresh All` in the gradle tab on the right.
6. Run `gradlew runClient` and `gradlew runServer`, or use the auto-imported run configurations in IntelliJ like `1. Run Client`.
