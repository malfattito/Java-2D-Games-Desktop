# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

JGames2D is a small, self-contained 2D game engine written in plain Java (AWT/Swing), plus a sample shoot-'em-up game built on it. There is no build system (no Maven/Gradle/Ant) — the project is a raw source tree meant to be opened as an Eclipse-style Java project with the jars in `Libs/` added to the build path (see `README.md`).

## Build & Run

Compile everything (source files use ISO-8859-1 — Portuguese comments contain accented chars):

```bash
javac -encoding ISO-8859-1 -cp "Libs/*" -d out JGames2D/*.java *.java
```

Run — the classpath must include the repo root so `Images/` and `Sounds/` resolve as classpath resources:

```bash
java -cp "out:.:Libs/*" GamePrincipal
```

There are no tests, no linter, and no CI.

## Architecture

**Two layers:** `JGames2D/` is the reusable engine package; the `Cena*.java` + `GamePrincipal.java` files in the repo root are the sample game and live in the default package.

**Engine lifecycle** (`JGEngine`): `main` constructs `JGEngine`, configures `engine.windowManager`, registers scenes with `addLevel(...)`, then `start()`. `JGEngine implements Runnable` and runs the game loop on its own thread: `update()` → `swapBuffers()` → `pause()`, targeting `FRAME_TIME = 33` ms (~30 FPS) with the frame's own processing time discounted from the sleep. Each `update()` calls, in this order, `execute()` (scene logic) → `update()` (sprites/layers) → `clearBackBuffer()` → `render()`. If `execute()` switched the current level, the frame is abandoned and the new level draws on the next one. Uncaught `RuntimeException`s from scene code are logged and shut the engine down through `free()` — otherwise the game thread would die and leave an orphan window holding the JVM alive.

**Rendering** is manual double buffering: `JGWindowManager` (a `JFrame`) owns a `BufferedImage` back buffer and publishes its `Graphics2D` as `engine.graphics`. Everything draws into `engine.graphics`; `repaint()` blits the back buffer in the overridden `paint()`. Note `setResolution()` recreates the back buffer and re-assigns `gameManager.graphics`, so it must be called before any drawing state (font/color) is set.

**Window geometry**: the drawing area is `windowManager.width/height` (also `getResolutionWidth()/getResolutionHeight()`). Do **not** use the inherited `getWidth()/getHeight()` — those are the outer window size, borders and title bar included, and return 0 before the window is shown. In windowed mode `showWindow()` grows the frame by its insets so the client area matches the requested resolution exactly, `paint()` blits at the inset origin, and `JGInputManager` subtracts that same origin from mouse coordinates.

**Scenes** (`JGLevel`, abstract): subclasses implement `init()` (called on every activation via `setCurrentLevel`) and `execute()` (per-frame logic), and may override `render()` (call `super.render()` first, then draw HUD text). `JGLevel` owns `vetSprites`/`vetLayers` and auto-renders/updates them; create children only through `createSprite(url, lines, columns)` and `createLayer(...)` so they get registered. Constructors are called once at startup and are used for preloading sounds; `init()` re-acquires them.

**Scene switching uses hard-coded integer indices** into the engine's level list, in `addLevel` registration order. In `GamePrincipal` the order is: 0 Abertura, 1 Menu, 2 Game, 3 Créditos, 4 (Game registered a second time — a duplicate), 5 Controles. Adding or reordering `addLevel` calls silently breaks every `setCurrentLevel(n)` call across the `Cena*` files.

**Sprites** (`JGSprite`): a sprite is a sprite-sheet image sliced into a `lines × columns` frame grid. `addAnimation(fps, repeat, frames...)` appends a `JGAnimation` to the sprite's animation list, selected by index with `setCurrentAnimation(i)`. Position/speed/zoom/direction are all `JGVector2D` (doubles). `position` is the sprite's *center* — `render()` offsets by half the frame size. Collision is `Rectangle` intersection via `collide(other)`.

**Layers** (`JGLayer`): tilemaps. The interesting constructor path builds the map from an indexed *bitmap*: each pixel color in a `lay_*.bmp` is mapped through a `JGColorIndex[]` (color → tile frame index) onto tiles cut from a tile sheet. `speed` + `scrollLayer()` give the scrolling background.

**Resource managers** are static singletons that cache by URL and hand back `JGImage` / `JGSoundEffect` / `JGMusic`. `JGImageManager` is reference-counted: `loadImage()` increments on every cache hit and `free(image)` decrements, discarding the pixels only when the last owner lets go — so `JGSprite.free()` and `JGLayer.free()` can release their images without breaking sprites that share the same file. A missing image or malformed sprite grid throws `IllegalArgumentException` at load time rather than surfacing as an NPE later. All resource loading goes through `getClass().getResource(...)` — game code wraps this in a private `getURL()` helper per scene. Paths are inconsistent in existing code (`"/Images/x.png"` absolute vs `"Sounds/x.wav"` relative); both happen to resolve because the game classes are in the default package. Prefer the leading-slash form.

**Audio**: `JGSoundEffect` wraps `javax.sound.sampled` for short WAVs; `JGMusic` wraps the JavaZOOM BasicPlayer stack (the jars in `Libs/`) for streamed MP3/OGG, with an iOS-`AVAudioPlayer`-shaped API (`setNumberOfLoops(-1)` = loop forever).

**Input** (`JGInputManager`): registered as key/mouse listener on the window. Distinguishes `keyPressed` (held) from `keyTyped` (edge-triggered, consumed on read) and likewise `mousePressed` vs `mouseClicked`. Use the `Typed`/`Clicked` variants for menu actions so a single click doesn't fire across frames. `setCurrentLevel` calls `inputManager.reset()`.

**Timing**: `JGTimeManager` is a static per-frame delta clock; `JGTimer` instances are countdown timers used for spawn cadence, fire rate, invincibility windows, etc. `JGTimer.update()` must be pumped manually from the scene's `execute()`.

## Conventions

- Engine code (`JGames2D/`) is in English with a banner comment block (`Name / Description / Parameters / Return`) above every method; game code (`Cena*.java`) is in Portuguese with uppercase inline comments. Match whichever file you are editing.
- Classes expose public fields directly (`sprite.position`, `sprite.visible`, `engine.graphics`) rather than accessors; follow that style rather than introducing getters.
- Every class has a manual `free()` that nulls its references; new engine classes are expected to provide one and callers are expected to call it on teardown.
- Indentation is tabs, Allman braces.
