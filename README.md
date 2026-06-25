# Lucky Conquest

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and an `ApplicationAdapter` extension that draws libGDX logo.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.

## Presentation
Card Game: LUCKY CONQUEST

Gameplay: Battle between two characters (player and bot). The player has a deck of cards and a slot machine. The battle is turn-based. A turn is divided into three phases. 
Phase 1 --> Draw cards to rig the slot machine and improve the odds for certain symbols (the exact effects are to be determined, but the goal is to create an advantageous situation for Phase 2). 
Phase 2 --> Spin the slot machine, which will display symbols. The symbols determine the actions to be taken (attack, bonus, penalty, and others to be determined later)
Phase 3 --> bonus phase, which is activated only if the machine lines up three identical symbols (even with Phase 1 in play, this should be an extremely rare occurrence). This phase is designed to boost the machine’s outcome (enhanced attacks, penalty cancellation, jackpot).
The rest of the battle follows standard rules: characters lose health points until one of them is defeated.

Deck Building: The player can upgrade their cards, use new cards, and change the reels on their slot machine to achieve better synergy.

Conquest: The player must explore and conquer dungeons to obtain new cards. Some dungeons will specialize in a particular symbol to yield cards related to that symbol (the first dungeons will offer all types of cards to give players a glimpse of the deck-building possibilities).
