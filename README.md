# Group Members

Chanyuphyea Lorn 6642038
Ye Htet Aung 6711266

# Space Invaders Java Project

This is our Space Invaders game made with Java Swing.

The game has three modes:

- Campaign: survive two five-minute scrolling stages, then defeat the stage-three boss
- Endless: play until all lives are lost
- Rush: get as many points as possible in 90 seconds

There are also speed and multi-shot power-ups.
Enemies use a five-frame exhaust animation only while moving forward.

## Controls

- Arrow keys or WASD: move
- Space or Enter: shoot and select
- P: pause
- M: turn music on or off
- R: retry
- Escape: go back to the menu

## How to run

Open `src/gdd/Main.java` in VS Code and click Run.

You can also run these commands from this folder:

```bash
javac -d out $(find src/gdd -name "*.java")
java -cp out gdd.Main
```

## Main folders

- `src/gdd`: Java code
- `src/images`: game images
- `src/audio`: music and sounds
- `src/maps`: CSV files used by the game
