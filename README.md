# 🎮 Space Diver - OOP Final Project

> A neon-themed arcade game showcasing Object-Oriented Programming principles in Java

![Java](https://img.shields.io/badge/Java-11+-orange)
![Swing](https://img.shields.io/badge/GUI-Swing-blue)
![Resolution](https://img.shields.io/badge/Resolution-800x500-brightgreen)
![FPS](https://img.shields.io/badge/FPS-60-green)

## 🎮 Overview

Space Diver is a neon-themed arcade game inspired by Flappy Bird. Players pilot a colorful spaceship through electrified pipes in space, facing dynamic difficulty, vibrant visuals, and immersive sound effects.

| Property | Value |
|----------|-------|
| **Language** | Java (Swing GUI) |
| **Resolution** | 800x500 pixels |
| **Frame Rate** | 60 FPS |
| **Difficulty** | Dynamic scaling |

## ✨ Features

- ⚡ Smooth physics engine with gravity and thrust mechanics
- 📈 Dynamic difficulty scaling (game speed increases with score)
- ✨ Vibrant particle effects and parallax star backgrounds
- 🔊 Immersive audio feedback for thrust, scoring, and collisions
- 🌟 Multiple decorative background objects (asteroids, planets, moons, meteors)
- ⌨️ Responsive controls via keyboard and mouse

## 🧩 OOP Concepts Demonstrated

This project showcases all **four pillars** of Object-Oriented Programming:

### **Encapsulation**
- Private fields for game state and audio clips
- Controlled access through methods like `playSound()`, `thrust()`, `die()`

### **Abstraction**
- Inner classes abstract entities: `Ring`, `Particle`, `Star`, `BackgroundObject`
- Each entity manages its own behavior independently

### **Inheritance**
- `SpaceDiver` extends `JPanel` and implements `ActionListener`, `KeyListener`, `MouseListener`
- Reuses Swing GUI framework and standardized event handling

### **Polymorphism**
- Interface method overriding (`actionPerformed`, `keyPressed`, `mousePressed`)
- Background objects render differently based on type

## 🛠️ Architecture

### Main Class
- **SpaceDiver** - game controller and renderer

### Inner Classes
| Class | Purpose |
|-------|---------|
| `Ring` | Obstacles |
| `Particle` | Visual effects |
| `Star` | Parallax background |
| `BackgroundObject` | Decorative elements |

### Design Principles
- 🎯 Favor composition over inheritance
- 🔄 Clear separation of concerns (logic, rendering, input)
- 🔧 Helper methods for code reuse and maintainability

## 🔄 Game Loop

- ⏱️ Runs at **60 FPS** via `javax.swing.Timer`
- 🔄 Updates physics, collision detection, and object states each frame
- 🎨 Rendering pipeline ensures proper Z-order layering (background → objects → HUD)

## 🎨 Controls

| Input | Action |
|-------|--------|
| <kbd>SPACE</kbd> | Thrust upward |
| <kbd>CLICK</kbd> | Thrust upward / Restart |
| <kbd>R</kbd> | Restart game |

## 📂 File Structure

```
SpaceDiver/
├── src/
│   └── SpaceDiver.java
├── audio/
│   ├── wing.wav
│   ├── hit.wav
│   ├── point.wav
│   └── die.wav
└── README.md
```

## ▶️ How to Run

### Prerequisites
- **JDK 8** or higher

### Windows
```bash
cd SpaceDiver/src
javac SpaceDiver.java
java SpaceDiver
```

### macOS/Linux
```bash
cd SpaceDiver/src
javac SpaceDiver.java
java SpaceDiver
```

## 📖 Design Patterns Used

- 🎯 **Strategy Pattern** - Different rendering strategies for background objects
- 👁️ **Observer Pattern** - Swing event model (listeners)
- 🔗 **Composition Pattern** - Game entities contained within SpaceDiver
- 🔒 **Singleton-like Behavior** - Single SpaceDiver instance manages state

## ✅ Why This is Good OOP Design

- ✔️ **Single Responsibility Principle (SRP)** - Each class has one clear purpose
- 🔐 **Encapsulation of state and behavior** - Controlled access to internals
- 🎭 **Abstraction of complex systems** - Hide implementation details
- ♻️ **Reusability and extensibility** - Easy to build upon or modify
- 🧹 **Maintainable and organized structure** - Clean, readable code

## 🎤 Presentation Tips

1. Start with OOP pillars and show examples from code
2. Demonstrate polymorphism with background objects
3. Explain event handling via listeners
4. Highlight design patterns (Strategy, Observer, Composition)
5. Run a live demo and narrate the underlying mechanics

## 📌 Summary

Space Diver is a comprehensive showcase of OOP principles applied to a real game engine. It demonstrates **encapsulation**, **abstraction**, **inheritance**, **polymorphism**, and **composition** in a practical, engaging project.