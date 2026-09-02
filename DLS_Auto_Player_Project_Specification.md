# DLS Auto Player - Project Specification

## 1. Overview

DLS Auto Player is an Android APK that can autonomously control a
football game running on the same Android device.

Main pipeline:

    Screen Capture
          ↓
    Vision Engine
          ↓
    Game State
          ↓
    Decision Engine
          ↓
    Input Controller
          ↓
    Game
          ↓
    Next Frame

The application must run directly on Android without requiring: - PC -
Python runtime - ADB during production - Cloud API - External server

------------------------------------------------------------------------

# 2. Technology Stack

## Android

-   Kotlin
-   Jetpack Compose
-   MVVM + Clean Architecture
-   Kotlin Coroutines
-   StateFlow
-   Gradle Kotlin DSL

## System Components

  Component                 Technology
  ------------------------- --------------------------------
  Screen capture            MediaProjection
  Touch control             AccessibilityService
  Image processing          OpenCV
  AI inference (optional)   TensorFlow Lite / ONNX Runtime
  Storage                   DataStore

------------------------------------------------------------------------

# 3. Architecture

    DLS Game

        ↓

    MediaProjection Screen Capture

        ↓

    Vision Engine

    - Ball detection
    - Player detection
    - Goal detection
    - UI detection

        ↓

    Tracking

        ↓

    Game State

        ↓

    Decision Engine

    - Attack AI
    - Defense AI
    - Movement AI

        ↓

    Input Engine

    - Joystick
    - Pass
    - Shoot
    - Cross
    - Switch player
    - Tackle

        ↓

    DLS Game

------------------------------------------------------------------------

# 4. Development Phases

## Phase 0 - Android Foundation

Goal:

Create a working APK.

Features:

-   Main screen
-   Screen capture permission
-   Accessibility permission
-   Start/Stop Bot
-   Foreground service

Acceptance:

-   APK builds
-   APK installs
-   Service starts
-   Service stops safely

------------------------------------------------------------------------

# Phase 1 - Screen Capture

Use:

    MediaProjection
            ↓
    VirtualDisplay
            ↓
    ImageReader
            ↓
    Frame Buffer

Requirements:

-   Process frames outside UI thread
-   Drop old frames
-   Avoid memory leaks

Target:

-   15-30 FPS

------------------------------------------------------------------------

# Phase 2 - Input Controller

Use:

    AccessibilityService

Required functions:

``` kotlin
tap()
swipe()
moveJoystick()
pass()
shoot()
cross()
switchPlayer()
tackle()
```

The AI must not directly call AccessibilityService.

Use abstraction:

    Decision Engine
            ↓
    GameInputController
            ↓
    AccessibilityService

------------------------------------------------------------------------

# Phase 3 - Vision Engine

Start with OpenCV.

Do not use ML immediately.

Detect:

-   Ball
-   Controlled player
-   Teammates
-   Opponents
-   Goal
-   Controls

Models:

``` kotlin
BallDetection
PlayerDetection
GoalDetection
```

Every detection must include confidence.

------------------------------------------------------------------------

# Phase 4 - Tracking

Track:

-   Ball movement
-   Player movement
-   Velocity

Use:

-   Exponential moving average
-   Kalman filter if required

------------------------------------------------------------------------

# Phase 5 - Game State

Create:

``` kotlin
data class GameState(
    val ball: BallState?,
    val controlledPlayer: PlayerState?,
    val teammates: List<PlayerState>,
    val opponents: List<PlayerState>,
    val phase: GamePhase
)
```

Game phases:

    ATTACK
    DEFENSE
    KICKOFF
    DEAD_BALL
    GOAL
    PAUSED
    MATCH_END

------------------------------------------------------------------------

# Phase 6 - Decision Engine

Decision engine returns actions.

Example:

    GameState
          ↓
    DecisionEngine
          ↓
    GameAction

Actions:

``` kotlin
Move
Pass
Shoot
Cross
SwitchPlayer
Tackle
Stop
```

------------------------------------------------------------------------

# 7. AI Logic

## Movement AI

First goal:

Move controlled player toward ball.

Algorithm:

    ball position
          +
    player position

    calculate vector

    convert to joystick direction

------------------------------------------------------------------------

## Attack AI

Priority:

    1. Shoot opportunity
    2. Pass opportunity
    3. Dribble

Example:

    Near goal + clear angle
            ↓
          Shoot

    Open teammate
            ↓
          Pass

    Otherwise
            ↓
          Dribble

------------------------------------------------------------------------

## Defense AI

Behaviors:

-   Press opponent
-   Intercept
-   Tackle
-   Switch player

Do not always choose nearest player.

Use scoring:

    distance
    +
    interception chance
    +
    position quality

------------------------------------------------------------------------

# 8. Performance Requirements

Target:

  Component            Target
  --------------- -----------
  Capture              30 FPS
  Vision            15-30 FPS
  Decision          10-20 FPS
  Input latency       \<100ms

Optimization:

-   Crop unnecessary screen areas
-   Resize images before processing
-   Avoid Bitmap allocations
-   Process latest frame only

------------------------------------------------------------------------

# 9. Package Structure

    com.dlsautoplayer

    presentation/
    service/
    capture/
    vision/
    tracking/
    game/
    ai/
    input/
    overlay/
    config/
    util/

------------------------------------------------------------------------

# 10. Safety System

The bot must stop when:

-   User presses stop
-   Screen capture lost
-   Accessibility disconnected
-   Vision confidence too low
-   Internal error

On stop:

-   Release joystick
-   Stop gestures
-   Release resources

------------------------------------------------------------------------

# 11. Debug Overlay

Display:

    DLS BOT RUNNING

    FPS:
    Game State:
    Ball:
    Player:
    Action:
    Confidence:

Allow:

-   Enable/disable overlay
-   Save debug frame
-   Show detection boxes

------------------------------------------------------------------------

# 12. Engineering Rules

1.  Kotlin first.
2.  No production dependency on PC.
3.  No cloud AI.
4.  Use interfaces between modules.
5.  Keep AI independent from Android framework.
6.  Use normalized coordinates.
7.  Avoid hardcoded coordinates.
8.  Do not block main thread.
9.  Add emergency stop.
10. Prefer OpenCV before ML.
11. Add ML only when required.
12. Test every milestone before moving forward.

------------------------------------------------------------------------

# 13. MVP Definition

First MVP:

    APK
     ↓
    Capture screen
     ↓
    Detect ball
     ↓
    Detect controlled player
     ↓
    Move toward ball
     ↓
    Stop safely

Full autonomous football behavior is developed only after MVP stability.
