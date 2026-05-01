# Space Diver - Quick Start Guide

## Project Structure
```
SpaceDiver/
├── src/
│   └── SpaceDiver.java           # Main game code (1200+ lines)
├── audio/
│   ├── wing.wav                  # Thrust sound
│   ├── hit.wav                   # Crash sound  
│   ├── point.wav                 # Score sound
│   └── die.wav                   # (Alternative)
├── README.md                      # Full documentation (OOP principles)
├── OOP_CONCEPTS.md               # Presentation guide (4 pillars explained)
├── compile_and_run.bat           # Windows script (double-click to run)
├── compile_and_run.sh            # macOS/Linux script
└── QUICK_START.md                # This file
```

---

## How to Run

### Option 1: Easy - Windows Double-Click
1. Navigate to the `SpaceDiver` folder
2. Double-click `compile_and_run.bat`
3. Game launches automatically

### Option 2: Easy - macOS/Linux Terminal
```bash
cd SpaceDiver
bash compile_and_run.sh
```

### Option 3: Manual Compilation
```bash
cd SpaceDiver/src
javac SpaceDiver.java
java SpaceDiver
```

---

## Game Controls

| Key | Action |
|-----|--------|
| **SPACE** | Thrust ship upward |
| **MOUSE CLICK** | Thrust or restart |
| **R** | Restart (when dead) |

---

## Game Objectives

✅ **Avoid pipes** - Don't hit the electrified obstacles  
✅ **Navigate gaps** - Gap sizes vary (100-220 pixels)  
✅ **Survive longer** - Game speeds up as you score  
✅ **Beat your best** - Track high score across sessions  

---

## Features

- 🎮 60 FPS smooth gameplay
- 🎨 Neon space aesthetic with particle effects
- 🔊 Dynamic sound effects (wing, crash, score)
- 📊 Difficulty scaling (faster every 5 points)
- 🌙 Parallax starfield background
- 💫 Orbiting planets on start screen

---

## For OOP Presentation

**Key Files:**
- `README.md` - Complete documentation (all OOP concepts explained)
- `OOP_CONCEPTS.md` - Quick presentation reference (4 pillars with code)
- `src/SpaceDiver.java` - The source code (~1200 lines, well-commented)

**What to Present:**
1. Inner classes: Ring, Particle, Star, BackgroundObject
2. Encapsulation: Private state, public methods
3. Abstraction: Hidden complexity in classes
4. Inheritance: Extends JPanel, implements interfaces
5. Polymorphism: Different rendering for same object type
6. Composition: Objects contain other objects

---

## Troubleshooting

**Problem:** "Audio directory not found"  
**Solution:** Make sure `audio/` folder with `.wav` files is at project root

**Problem:** Game won't compile  
**Solution:** Check Java JDK is installed: `java -version`

**Problem:** Game starts but no sound  
**Solution:** Audio is optional - game works without it

---

## File Sizes

- SpaceDiver.java: ~1200 lines
- Audio files: ~50KB total
- Compiled class files: ~30KB

---

## System Requirements

- Java 8 or higher (any OS)
- 100MB free space
- Any modern display

---

## Questions?

Refer to:
- `README.md` for full explanation
- `OOP_CONCEPTS.md` for presentation tips
- Code comments in `SpaceDiver.java`

Good luck with your OOP finals! 🚀
