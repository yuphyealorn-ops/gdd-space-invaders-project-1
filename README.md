# Group Members

Chanyuphyea Lorn 6642038

Ye Htet Aung 6711266

# Space Invaders Java Project

This is our Space Invaders game made with Java Swing.

The game has three modes:

- Campaign: clear each enemy target or survive its five-minute limit, then defeat the stage-three boss
- Endless: loop the same two enemy stages and boss stage until all lives are lost
- Rush: score as many points as possible in one 60-second stage

There are also speed and multi-shot power-ups.
Enemies use a five-frame exhaust animation only while moving forward.

## Controls

- Arrow keys or WASD: move
- Space or Enter: shoot and select
- X: use ultimate move
- P: pause
- M: turn music on or off
- R: retry
- Escape: go back to the menu

## Power-ups

Speed up: Adds +2 movement speed to the player. 15 second timer.
Multi-shot: Adds +2 bullet count when shooting. 15 second timer.
Ultimate: Piercing Bullet: Fires a giant beam of laser that pierces through all the enemies. Deals 5x damage to the boss. X to activate.

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
