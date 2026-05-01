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
