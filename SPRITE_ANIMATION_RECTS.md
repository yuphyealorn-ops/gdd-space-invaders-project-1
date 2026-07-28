## Start

Context, this is from another person's project. you can copy the animations as we are using the same default spite sheet but do not copy the RGB colors for the background. use something else slightly different I guess

# Sprite Animation Rectangles

Coordinates are source-pixel rectangles in `(x, y, width, height)` order,
measured from the top-left corner of each image. Rendered sizes are the sizes
the current Java code scales or draws those frames to at runtime.

## Gameplay Background

The gameplay background is generated in `src/gdd/scene/Scene1.java`, not loaded
from a background image.

- Gradient: `drawMap(...)` caches a `BufferedImage` and fills 4-pixel horizontal
  bands by interpolating between two RGB colors.
- Stage 1 gradient: top `(3, 12, 34)`, bottom `(8, 35, 65)`.
- Stage 2 gradient: top `(28, 4, 35)`, bottom `(45, 9, 25)`.
- Stage 3 gradient: top `(35, 8, 4)`, bottom `(65, 15, 8)`.
- Background objects: `drawFarStars`, `drawMidStars`, `drawDustClouds`,
  `drawNebula`, `drawWorldDecorations`, `drawStarCluster`, and
  `drawNearStreaks` draw stars, clouds, nebula ellipses, the stage-1 planet,
  stage-2/3 asteroids, clustered stars, and near streaks with `Graphics2D`.
- Star-cluster layout: `maps.txt` supplies the `0`/`1` map data loaded by
  `src/gdd/MapLoader.java`. `1` cells are rendered as star clusters.
- For the background make it an RGB gradient that matches outer space colors, then draws far stars, mid stars, dust clouds, nebula shapes, the planet/asteroids, star clusters from a txt with 3 different stages.

## `src/images/sprites.png`

Image size: `448 x 176`

Used by: `src/gdd/sprite/Player.java`

Rendered player clip size: source size multiplied by `SCALE_FACTOR = 2`, so
each `24 x 14` source frame renders as `48 x 28`.

| Frame | Rectangle |
| --- | --- |
| player_idle_1 | `(24, 10, 24, 14)` |
| player_idle_2 | `(56, 10, 24, 14)` |
| player_left | `(88, 10, 24, 14)` |
| player_right | `(120, 10, 24, 14)` |

Player explosion on death, sequencing in order
player_death_1 '(24, 58, 24, 17)'
player_death_2 '(56, 57, 24, 18)'
player_death_3 '(88, 58, 16, 17)'

Generated from the first idle clip, not copied from extra sheet rectangles:

| Animation | Runtime size | Notes |
| --- | --- | --- |
| player_shot_flash | `48 x 28` | Adds muzzle-flash rectangles over `player_idle_1`. |
| player_hit | `48 x 28` | Adds a red overlay and white cross lines over `player_idle_1`. |
| player_explosion | `48 x 28`, 6 frames | Procedural expanding oval frames. |

## `src/images/alien/boss_sprite.png`

| Frame | Rectangle |
| --- | --- |
| Idle / Base form | `(137, 193, 244, 214)` |
| Weapon Deployment (Stage 1) | `(560, 215, 268, 164)` |
| Weapon Deployment (Stage 2) | `(989, 231, 296, 165)` |
| Primary Cannon Attack | `(271, 419, 297, 189)` |
| Armored / Powered-up Mode | `(855, 414, 278, 201)` |
| Final Evolution / Enraged Form | `(358, 664, 288, 201)` |

Side note: use these dimensions and then flip the boss horizontally otherwise it will face away from the player.

## `src/images/alien/enemy_projectile.png`

Alien plasma projectile, will gradually fade if does not hit player. Moves slowly and is quite predictable but deals high damage
| Frame | Rectangle |
| --- | --- |
| bomb_0 | `(18, 10, 67, 64)` |
| bomb_1 | `(124, 12, 65, 62)` |
| bomb_2 | `(230, 12, 61, 62)` |
| bomb_3 | `(23, 119, 59, 55)` |
| bomb_4 | `(129, 119, 54, 54)` |
| bomb_5 | `(237, 121, 47, 49)` |
| bomb_6 | `(30, 228, 41, 44)` |
| bomb_7 | `(136, 230, 38, 38)` |
| bomb_8 | `(243, 233, 35, 34)` |

## `src/images/alien/enemy_explosion.png`

| Frame | Rectangle |
| --- | --- |
| explosion_0 | `(62, 70, 70, 69)` |
| explosion_1 | `(223, 36, 129, 112)` |
| explosion_2 | `(397, 19, 167, 152)` |
| explosion_3 | `(580, 0, 182, 182)` |
| explosion_4 | `(768, 0, 177, 176)` |
| explosion_5 | `(11, 208, 173, 160)` |
| explosion_6 | `(210, 216, 162, 152)` |

## Static Or Procedural Sprites

These are not clipped from sprite-sheet rectangles in the current code:

| Sprite | Source | Runtime handling |
| --- | --- | --- |
| alien1 | `src/images/alien/Enemy1.png` It is currently facing down, so make it face right to left (toward the player)
| shot | `src/images/shot.png`, `1 x 5` | Full image scaled to `3 x 10`, then rotated while drawing. |
| power-up icons | Generated in `src/gdd/powerup/*.java` | Each icon is drawn procedurally on a `48 x 48` image. |
I ask that you write the power up icons as code as well. Based on the types of power ups I will list later.
| boss death scraps | Current boss frame | Shattered at runtime into a `5 x 5` grid. |

# Audio

start.wav - when player presses space/enter during the titlescreen in order to start the game, this sound should play as a start sound to indicate the game is starting and it confirmed
shoot.wav - player shooting sound 
powerup.wav - player collecting a powerup sound 
player_hit.wav - play getting hit/damaged in any way
game_over.wav - the game over MUSIC, that should play fully ONCE when the game ends in any way
explosion.wav - enemy death explosion sound
boss_hit.wav - plays when the boss is hit by the player's shot
boss_fight.wav - boss fight MUSIC that plays when the boss fight starts, Should gradually transition from the scene1 theme to the boss theme and make it transition earlier maybe around after the enemies start to disappear and the boss about to show.
boss_death.wav - boss death explosions sound

# Rules

I've added a font in src/font, use it for all universally coded text in the project
Make the game count down 3, 2, 1 Start! before it actually starts.
To make the game easier for the player
- The player has 3 lives
- Give the player a health bar at 100% on the top left corner, with green color
- The plasma ball should deal 30%, while enemy bullets and the getting in contact with the enemy itself deals 20%
The spawn rate of enemies should be balanced and the spawn rate of shots and plasma ball should be random, where plasma ball is rarer than the shots.
- The shots should travel slowly and allow the player to dodge and not make the gameplay impossible.
Power Ups. Power ups should have a 15 second timer before it expires. Make a bar at the bottom showing all the powerups. They should be in a greyish color if not activated but light up once activated followed by a timer starting from 15, when it hits 0 it goes back to gray.
- Speed up - 2 steps 
- Multi-shot - 4 steps 
- Weapon upgrade to other types such as 3-way shots 
When an alien touches the left end of the screen the game ends, with game over screen.
When the player reaches 0 lives in any mode, ends with game over
When the player defeats the boss in campaign, ends with victory
When rush mode ends, game over with victory
When game ends, player can press Q to return to title screen or R to retry.

# End message
Tell me whether I have followed all of the requirements yet or not:

The game must be a side-scroll, not vertical. 

Code must be extended from the given codebase. 

Do not use your own code made during classes. 

Do not use other codebase as we need prove on originality and inconsistent vibe coding. 

Do not restrsucture the codebase 

Title Scene must have names of the team members 

Change the title scene image to yours 

At least Two stages (game scenes) 

Each stage must scroll enough to play for at least 5 minutes 

[OPTIONAL] You may load the array from external CSV. 
See Reading a CSV File into an Array | Baeldung 

The last stage has a Boss fight. 

At least Two types of enemy. 

All sprites must be animated - with pure drawing or clipping. 

Enemy's Bombs in a separated list. It is integrated as a part of Enemy. 

Power Ups 

Speed up - 2 steps 

Multi-shot - 4 steps 

[Optional] Weapon upgrade to other types such as 3-way shots 

Dashboard shows status 

Score 

Speed 

Shots upgrade 

NOTE: I will most likely add more detailing, polishing, sound effects, visual effects and more sprites because this project is still very incomplete and I'm already late for the submission but it is still submittable. So this much is it for now. There will also be a lot of testing needed. I also know I'm most likely missing some things you listed, So list the same tables again at the end once you're done with everything, I will see what exactly I'm still missing.