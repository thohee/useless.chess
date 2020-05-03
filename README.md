# de.thohee.useless.chess
Chess engine written in Java. Will probably not compete with average chess apps. But is a nice programming kata and may be a field for experimentation with deep learning some day.

## Install and Build
* Install JDK 11 or higher: [openjdk.java.net](http://openjdk.java.net)
* Install Maven: [maven.apache.org](https://maven.apache.org)
* Install git: [git-scm.com](https://git-scm.com)
* Clone git repository: [github.com/thohee](https://github.com/thohee/useless.chess.git)
* Build with maven: `mvn clean install`

## Use
* Find jar-file in target-folder or maven repository under de/thohee/useless.chess
* Configure jar-file as UCI engine in [Arena](http://www.playwitharena.de) or any other chess board app.

## Develop
* Import sources into IDE of your choice, e.g. [Eclipse](https://www.eclipse.org/downloads)
* Extend abstract class `de.thohee.useless.chess.player.EnginePlayer` or `de.thohee.useless.chess.player.MinimaxPlayer` to create your own individual engine.
* Extend method `Game.createPlayerConfiguration` for your engine class.
* Execute jar-file with command line parameter `--player <Simple name of your player class>`