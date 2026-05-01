# Space Diver - OOP Final Project

## Project Overview

**Space Diver** is a neon-themed arcade game inspired by Flappy Bird. Players pilot a colorful spaceship through electrified pipes in space with dynamic difficulty, vibrant visuals, and immersive sound effects.

**Technology:** Java with Swing GUI  
**Resolution:** 800x500 pixels  
**FPS:** 60 frames per second

---

## Object-Oriented Programming (OOP) Concepts Used

This project demonstrates all four pillars of OOP:

### 1. **Encapsulation**
Encapsulation bundles data and methods together, hiding internal details.

```java
// Private fields encapsulated within classes
static class Ring {
    double x;
    int gapTop;
    int gapSize;
    boolean scored = false;
    double angle = 0;
    
    Ring(double x, int gapTop, int gapSize) { ... }  // Constructor
}
```

**Benefits:**
- Data protection: Audio clips, game state (alive, started, score) are protected
- Controlled access through methods like `playSound()`, `thrust()`, `die()`

---

### 2. **Abstraction**
Abstraction hides complex implementation details behind simple interfaces.

**Example: Inner Classes abstracting different game entities**

```java
// Abstracts a game obstacle
static class Ring { /* ... */ }

// Abstracts visual particles
static class Particle { /* ... */ }

// Abstracts background stars
static class Star { /* ... */ }

// Abstracts decorative background objects
static class BackgroundObject { /* ... */ }
```

**Benefits:**
- Main game loop doesn't need to know HOW particles work, just that they `update()`
- Each entity manages its own behavior independently

---

### 3. **Inheritance**
The SpaceDiver class inherits from JPanel and implements multiple interfaces.

```java
public class SpaceDiver extends JPanel 
    implements ActionListener, KeyListener, MouseListener {
    // Inherits JPanel's painting and layout capabilities
    // Implements required interface methods for event handling
}
```

**What it inherits:**
- From `JPanel`: Graphics rendering, component lifecycle
- From `ActionListener`: `actionPerformed()` - runs game loop 60 times/second
- From `KeyListener`: Keyboard input (SPACE to thrust, R to restart)
- From `MouseListener`: Mouse input (click to thrust or restart)

**Benefits:**
- Reuses Swing GUI framework without reimplementing
- Standardized event handling through interfaces

---

### 4. **Polymorphism**
Polymorphism allows objects to take multiple forms through method overriding.

**Example: Interface Implementation (Runtime Polymorphism)**

```java
@Override 
public void actionPerformed(ActionEvent e) {
    // Game logic runs here every frame
}

@Override 
public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_SPACE) thrust();
}

@Override 
public void mousePressed(MouseEvent e) {
    if (!alive) reset();
    else thrust();
}
```

**Example: Inner Class Polymorphism**

Different BackgroundObject types render differently:

```java
void drawBackgroundObject(Graphics2D g, BackgroundObject obj) {
    if (obj.type == 0) drawAsteroid(g, sz, alpha, obj);
    else if (obj.type == 1) drawPlanet(g, sz, alpha, obj);
    else if (obj.type == 2) drawMoon(g, sz, alpha, obj);
    else if (obj.type == 3) drawMeteor(g, sz, alpha, obj);
}
```

**Benefits:**
- Same object type (BackgroundObject) behaves differently based on context
- Easy to extend with new types without changing main logic

---

## Architecture & Class Structure

### Main Class: `SpaceDiver`
- **Purpose:** Main game controller and renderer
- **Extends:** `JPanel` (for rendering)
- **Implements:** `ActionListener, KeyListener, MouseListener` (for events)

### Inner Classes (Nested Static Classes)

#### 1. **Ring** - Game Obstacles
```java
static class Ring {
    double x;              // Position on screen
    int gapTop;           // Top of safe passage
    int gapSize;          // Height of gap (100-220 pixels)
    boolean scored;       // Track if player scored
    double angle;         // For visual rotation
}
```

#### 2. **Particle** - Visual Effects
```java
static class Particle {
    double x, y, vx, vy;  // Position & velocity
    double life, maxLife; // Fade out over time
    Color color;
    double size;
    
    void update() { ... } // Physics simulation
    boolean dead() { ... } // Cleanup check
}
```

#### 3. **Star** - Background Parallax
```java
static class Star {
    double x, y, speed, brightness;
    // Creates depth illusion with multiple layers
}
```

#### 4. **BackgroundObject** - Decorative Elements
```java
static class BackgroundObject {
    double x, y, vx, vy;           // Movement
    double rotation, rotSpeed;     // Animation
    double size, opacity;          // Visual properties
    int type;                      // 0=asteroid, 1=planet, 2=moon, 3=meteor
    Color color;
}
```

---

## Game Loop & State Management

### Frame-Based Updates (60 FPS)

```java
@Override public void actionPerformed(ActionEvent e) {
    if (started && alive) {
        // Physics: Apply gravity, update position
        velY += GRAVITY;
        shipY += velY;
        
        // Collision detection
        for (Ring r : rings) {
            // Check if ship hits pipe
        }
        
        // Update all game objects
        updateStars();
        updateBackgroundObjects();
        updateParticles();
    }
    
    repaint();  // Trigger paintComponent()
}
```

### Game States
- **Before Start:** Show menu with orbiting planets
- **Playing:** Physics + collision detection active
- **Dead:** Show game over screen with restart option

---

## Key OOP Features Implemented

### 1. **Encapsulation with Access Control**
```java
// Private game state
private Map<String, Clip> soundClips;  // Audio management
private List<Ring> rings;              // Obstacles
private List<Particle> particles;      // Effects
```

### 2. **Method Organization by Responsibility**
```java
// Game Logic
void thrust() { ... }        // Player input
void die() { ... }           // Collision response
void reset() { ... }         // Game restart

// Rendering
void drawShip(Graphics2D g, double cx, double cy, double vy) { ... }
void drawRing(Graphics2D g, Ring r) { ... }
void drawBackgroundObject(Graphics2D g, BackgroundObject obj) { ... }

// Audio
void loadSounds() { ... }
void playSound(String name) { ... }
```

### 3. **Helper Methods for Code Reuse**
```java
Color getBackgroundObjectColor(int type) { ... }  // Consistent coloring
void drawCenteredText(...) { ... }               // Reusable text rendering
Color generateShipColor() { ... }                // Random ship colors
```

### 4. **Composition Over Inheritance**
Instead of deep inheritance hierarchies, we use composition:

```java
// SpaceDiver CONTAINS these objects, doesn't inherit from them
List<Ring> rings;              // Composed
List<Particle> particles;      // Composed
List<Star> stars;             // Composed
List<BackgroundObject> bgObjects;  // Composed
```

This follows the "favor composition over inheritance" principle.

---

## Detailed Code Walkthrough

### Initialization (Constructor)
```java
public SpaceDiver() {
    // Setup GUI
    setPreferredSize(new Dimension(W, H));
    setBackground(Color.BLACK);
    
    // Load resources
    loadSounds();
    
    // Initialize game objects
    initStars();
    shipColor = generateShipColor();
    spawnBackgroundObjects();
    spawnRing(W + 50);
    
    // Start 60 FPS game loop
    javax.swing.Timer timer = new javax.swing.Timer(1000 / FPS, this);
    timer.start();
}
```

### Physics Engine
```java
velY += GRAVITY;              // Apply gravity each frame
if (velY > MAX_FALL) velY = MAX_FALL;  // Terminal velocity
shipY += velY;                // Update position

// Boundary check
if (shipY < 8) { shipY = 8; velY = 0; }  // Ceiling
if (shipY > H - 11) die();                 // Floor
```

### Collision Detection
```java
// Uses bounding box collision
boolean inRingX = sx + 16 > r.x && sx - 16 < r.x + RING_WIDTH;
boolean inGap = sy - 16 > r.gapTop && sy + 16 < r.gapTop + r.gapSize;

if (inRingX && !inGap) die();  // Hit pipe
```

### Rendering Pipeline (Z-order)
1. Background gradient
2. Stars (parallax layers)
3. Background objects (asteroids, planets)
4. Shockwave effect
5. Pipes/Rings
6. Particles
7. Ship
8. Screen flash (on death)
9. HUD (score, text)

```java
@Override protected void paintComponent(Graphics g0) {
    Graphics2D g = (Graphics2D) g0;
    
    // Layer 1: Background
    drawGradient(g);
    
    // Layer 2: Stars
    for (Star s : stars) drawStar(g, s);
    
    // Layer 3-8: Game objects in order
    for (BackgroundObject obj : bgObjects) drawBackgroundObject(g, obj);
    for (Ring r : rings) drawRing(g, r);
    for (Particle p : particles) drawParticle(g, p);
    
    // Layer 9: HUD
    drawHUD(g);
}
```

---

## How to Compile & Run

### Prerequisites
- Java Development Kit (JDK) 8 or higher installed
- Command line/terminal access

### Windows
```batch
cd SpaceDiver
cd src
javac SpaceDiver.java
java SpaceDiver
```

### macOS/Linux
```bash
cd SpaceDiver/src
javac SpaceDiver.java
java SpaceDiver
```

### From Project Root
The audio will automatically find itself via path resolution:
1. Checks `audio/` (relative to current directory)
2. Checks `c:\Users\Nori\Downloads\audio` (absolute path)
3. Checks `[working_directory]\audio` (dynamic)

---

## Controls

| Input | Action |
|-------|--------|
| **SPACE** | Thrust upward |
| **CLICK** | Thrust upward (or restart when dead) |
| **R** | Restart game (when dead) |

---

## Game Features Demonstrating OOP

### 1. **Dynamic Difficulty (Polymorphism)**
```java
double speed() { 
    return 2.8 + (score / 5) * 0.4;  // Speed increases every 5 points
}
```
Different game speeds depending on score.

### 2. **Audio Management (Encapsulation)**
```java
Map<String, Clip> soundClips = new HashMap<>();
void playSound(String name) { ... }  // Private audio handling
```
Clips are encapsulated; external code just calls `playSound("point")`.

### 3. **State Management (Encapsulation)**
```java
boolean alive, started;
int score, bestScore;
// Private state, modified only through proper methods
```

### 4. **Object Spawning (Abstraction)**
```java
void spawnRing(double x) { ... }           // Creates obstacles
void spawnBackgroundObjects() { ... }      // Creates decorations
void spawnNewBackgroundObject() { ... }    // Maintains pool
```
Hides complexity of object creation.

---

## OOP Design Patterns Used

### 1. **Strategy Pattern**
Different rendering strategies based on object type:
```java
if (obj.type == 0) drawAsteroid(...);
else if (obj.type == 1) drawPlanet(...);
```

### 2. **Observer Pattern**
Swing's event model (ActionListener, KeyListener, MouseListener) follows Observer pattern.

### 3. **Composition Pattern**
SpaceDiver composes collections of game entities rather than inheriting.

### 4. **Singleton-like Behavior**
Single SpaceDiver instance manages entire game state.

---

## What Makes This Good OOP Design?

✅ **Single Responsibility:** Each class/method does one thing well  
✅ **Encapsulation:** Private state, public interfaces  
✅ **Abstraction:** Inner classes hide complexity  
✅ **Reusability:** Helper methods avoid code duplication  
✅ **Extensibility:** Easy to add new object types or features  
✅ **Maintainability:** Clear structure and organization  
✅ **Separation of Concerns:** Game logic, rendering, and input are separate  

---

## File Structure

```
SpaceDiver/
├── src/
│   └── SpaceDiver.java          # Main game (1200+ lines)
├── audio/
│   ├── wing.wav                  # Thrust sound
│   ├── hit.wav                   # Crash sound
│   ├── point.wav                 # Score sound
│   └── die.wav                   # (Optional alternative crash)
└── README.md                      # This file
```

---

## Presentation Tips

1. **Start with OOP Concepts** - Explain the 4 pillars using this code
2. **Show Inner Classes** - Ring, Particle, Star, BackgroundObject are great examples
3. **Demonstrate Polymorphism** - Show how BackgroundObject renders differently by type
4. **Explain Event Handling** - ActionListener, KeyListener, MouseListener pattern
5. **Discuss Design Patterns** - Composition, Strategy, Observer
6. **Live Demo** - Run the game and explain what's happening under the hood
7. **Code Structure** - Show how methods are organized by responsibility

---

## Summary

Space Diver successfully demonstrates:
- ✅ Encapsulation (protected state)
- ✅ Abstraction (inner classes, helper methods)
- ✅ Inheritance (extends JPanel, implements interfaces)
- ✅ Polymorphism (different rendering, interface implementation)
- ✅ Composition (objects contained, not inherited)
- ✅ Good OOP design principles (SRP, DRY, SOLID)

This project is a comprehensive example of practical OOP principles applied to a real game engine!
