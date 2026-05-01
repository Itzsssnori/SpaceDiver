# OOP Concepts in Space Diver - Presentation Guide

## Quick Reference for Your OOP Finals

This document breaks down each OOP concept with specific code examples from Space Diver.

---

## 1. ENCAPSULATION (Data Hiding + Access Control)

**Definition:** Bundle data and methods together, hiding internal details from the outside world.

### Example 1: Audio System Encapsulation
```java
// PRIVATE - Hidden from external access
private Map<String, Clip> soundClips = new HashMap<>();
private Clip bgMusic = null;

// PUBLIC - Controlled interface
void playSound(String name) {
    try {
        Clip clip = soundClips.get(name);
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        }
    } catch (Exception e) {
        // Handle gracefully
    }
}
```

**Why it's good:**
- Nobody can accidentally corrupt soundClips
- Audio loading and error handling are internal
- Only way to play sound is through `playSound()` method

### Example 2: Game State Encapsulation
```java
// Private game state
private double shipX = 180, shipY = H / 2.0;
private double velY = 0;
private boolean alive = true;
private int score = 0;

// Public controlled methods
void thrust() { /* ... */ }  // Only way to change velY
void die() { /* ... */ }     // Only way to set alive = false
void reset() { /* ... */ }   // Only way to restart game
```

**Analogy:** Like a car's engine - you don't access spark plugs directly, you turn the key!

---

## 2. ABSTRACTION (Hiding Complexity)

**Definition:** Expose only essential features, hide implementation details.

### Example 1: Inner Classes Abstract Complex Objects
```java
// Abstracts a pipe obstacle - simple interface
static class Ring {
    double x;           // Position
    int gapTop;        // Gap location
    int gapSize;       // Gap size
    boolean scored;    // Did player score?
    double angle;      // Rotation
    
    Ring(double x, int gapTop, int gapSize) {
        this.x = x;
        this.gapTop = gapTop;
        this.gapSize = gapSize;
    }
}

// Main game doesn't need to know HOW a Ring is drawn
// It just calls drawRing(g, ring)
```

**Why:**
- Ring's constructor is simple - only needs position and size
- Collision detection is transparent
- Rendering is abstracted away

### Example 2: Particle System Abstraction
```java
static class Particle {
    double x, y, vx, vy, life, maxLife;
    Color color;
    double size;
    
    boolean dead() { return life <= 0; }
    void update() {
        x += vx;
        y += vy;
        vy += 0.05;  // gravity
        life--;
    }
}

// Main game just calls:
for (Particle p : particles) p.update();
particles.removeIf(Particle::dead);
```

**What's abstracted:**
- Physics calculations (gravity, velocity)
- Lifetime management
- Collision response

### Example 3: Background Objects Abstraction
```java
static class BackgroundObject {
    double x, y, vx, vy, rotation, rotSpeed;
    double size, opacity;
    int type;  // 0=asteroid, 1=planet, 2=moon, 3=meteor
    Color color;
    
    void update() { x += vx; y += vy; rotation += rotSpeed; }
}

// Single object type, multiple visual representations:
void drawBackgroundObject(Graphics2D g, BackgroundObject obj) {
    if (obj.type == 0) drawAsteroid(g, ...);
    else if (obj.type == 1) drawPlanet(g, ...);
    else if (obj.type == 2) drawMoon(g, ...);
    else if (obj.type == 3) drawMeteor(g, ...);
}
```

---

## 3. INHERITANCE (Code Reuse Through Class Hierarchy)

**Definition:** Child class inherits properties and methods from parent class.

### Example 1: Extending JPanel
```java
public class SpaceDiver extends JPanel 
    implements ActionListener, KeyListener, MouseListener {
    
    // Inherits from JPanel:
    // - paintComponent() method
    // - Graphics rendering capabilities
    // - Component lifecycle methods
    // - Event handling infrastructure
}
```

**What SpaceDiver inherits from JPanel:**
- `paintComponent(Graphics g)` - for drawing
- `setPreferredSize()` - set window size
- `setBackground()` - set background color
- `addKeyListener()`, `addMouseListener()` - register listeners

### Example 2: Interface Implementation (Contract-based Inheritance)
```java
// Implement ActionListener for timer events
@Override public void actionPerformed(ActionEvent e) {
    // Called 60 times per second (game loop)
}

// Implement KeyListener for keyboard input
@Override public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_SPACE) thrust();
}

// Implement MouseListener for mouse input
@Override public void mousePressed(MouseEvent e) {
    if (!alive) reset();
    else thrust();
}
```

**Why interfaces instead of extending classes:**
- Java doesn't support multiple inheritance
- Interfaces define contracts (what methods must exist)
- Cleaner separation of concerns

---

## 4. POLYMORPHISM (Many Forms)

**Definition:** Objects can take multiple forms; same method call, different behavior.

### Example 1: Interface Polymorphism (Runtime Polymorphism)
```java
// Same thrust() method called by different events
@Override public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_SPACE) thrust();
}

@Override public void mousePressed(MouseEvent e) {
    else thrust();  // Same method, different trigger
}

// Result: Different input methods, same game behavior
```

### Example 2: Object Type Polymorphism
```java
// Same BackgroundObject, different rendering behavior
static class BackgroundObject { int type; /* ... */ }

// Polymorphic rendering:
if (obj.type == 0) drawAsteroid(g, sz, alpha, obj);     // Asteroid rendering
else if (obj.type == 1) drawPlanet(g, sz, alpha, obj);  // Planet rendering
else if (obj.type == 2) drawMoon(g, sz, alpha, obj);    // Moon rendering
else if (obj.type == 3) drawMeteor(g, sz, alpha, obj);  // Meteor rendering
```

**Same object type (BackgroundObject), four different visual outputs!**

### Example 3: Method Overriding (Compile-time Polymorphism)
```java
// Parent (JPanel) method:
protected void paintComponent(Graphics g) { ... }

// Child (SpaceDiver) method - OVERRIDES parent:
@Override protected void paintComponent(Graphics g0) {
    // Custom rendering for Space Diver
    Graphics2D g = (Graphics2D) g0;
    // Draw background, stars, pipes, ship, HUD, etc.
}

// When paintComponent() is called on a SpaceDiver object,
// SpaceDiver's version is called, not JPanel's
```

---

## 5. COMPOSITION (Objects Containing Other Objects)

**Definition:** Build complex objects by combining simpler objects (not inheritance).

### Example: SpaceDiver Composes Game Entities
```java
public class SpaceDiver extends JPanel /* ... */ {
    // Composed objects - contained, not inherited
    List<Ring> rings;              // Obstacles
    List<Particle> particles;      // Visual effects
    List<Star> stars;              // Background
    List<BackgroundObject> bgObjects;  // Decorations
    Map<String, Clip> soundClips;  // Audio
}
```

**Why composition over inheritance:**
```java
// DON'T DO THIS (Bad):
class Particle extends MovingObject extends VisualObject { }

// DO THIS INSTEAD (Good):
class Particle {
    double x, y, vx, vy;        // Position & velocity data
    Color color;                 // Visual data
    void update() { }            // Position update logic
}

class SpaceDiver {
    List<Particle> particles;    // COMPOSE particles
}
```

**Benefits:**
- More flexible
- Easier to change behavior
- No deep inheritance chains
- Objects are independent

---

## 6. ABSTRACTION IN ACTION: Game Object Lifecycle

### The Particle System (Complete Example)
```java
// 1. CREATE particles (abstraction hides creation)
void thrust() {
    for (int i = 0; i < 8; i++) {
        double vx = 1.5 + rng.nextDouble() * 2;
        double vy = (rng.nextDouble() - 0.5) * 2;
        Color c = rng.nextBoolean() ? CYAN : PURPLE;
        particles.add(new Particle(
            shipX - 12, shipY + 2,  // Position
            vx, vy,                 // Velocity
            18 + rng.nextInt(10),   // Life
            c,                      // Color
            3 + rng.nextDouble() * 3 // Size
        ));
    }
}

// 2. UPDATE particles (abstraction hides physics)
for (Particle p : particles) p.update();

// 3. REMOVE dead particles (abstraction hides cleanup)
particles.removeIf(Particle::dead);

// 4. RENDER particles (abstraction hides graphics)
for (Particle p : particles) {
    float alpha = (float)(p.life / p.maxLife);
    g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha * 220)));
    g.fillOval((int)(p.x - sz / 2.0), (int)(p.y - sz / 2.0), sz, sz);
}
```

---

## 7. DESIGN PATTERNS USED

### Pattern 1: Strategy Pattern
Different rendering strategies based on object type:
```java
void drawBackgroundObject(Graphics2D g, BackgroundObject obj) {
    // Choose strategy (rendering method) based on type
    if (obj.type == 0) drawAsteroid(...);
    else if (obj.type == 1) drawPlanet(...);
    // etc.
}
```

### Pattern 2: Observer Pattern
Swing's event model is the Observer pattern:
```java
// Game "observes" key presses
addKeyListener(this);

// When key pressed, Java notifies the observer:
@Override public void keyPressed(KeyEvent e) {
    // Reaction to observed event
}
```

### Pattern 3: Composition Pattern
SpaceDiver composes collections of objects:
```java
List<Ring> rings = new ArrayList<>();
List<Particle> particles = new ArrayList<>();
// Collections managed as a whole
```

---

## Presentation Checklist

- [ ] Explain all 4 OOP pillars with code examples
- [ ] Show Ring, Particle, Star, BackgroundObject inner classes
- [ ] Demonstrate polymorphism with BackgroundObject rendering
- [ ] Explain inheritance (JPanel + interfaces)
- [ ] Discuss composition vs inheritance
- [ ] Run the game live during presentation
- [ ] Point out error handling (try-catch in playSound)
- [ ] Explain game loop (ActionListener pattern)
- [ ] Show method organization by responsibility
- [ ] Mention design patterns (Strategy, Observer, Composition)

---

## Key Takeaway

Space Diver shows that OOP isn't just about making code work—it's about:
✅ Organizing code logically  
✅ Hiding complexity  
✅ Reusing code  
✅ Making code maintainable  
✅ Making code extensible (easy to add features)  

This is a **production-quality** example of OOP principles applied to a real game engine!
