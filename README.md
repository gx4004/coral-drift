# 🐙💖 Coral Drift

> *A dreamy underwater endless runner where a cute pink octopus collects hearts and avoids coral!*

<div align="center">

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21-007396?style=for-the-badge&logo=java&logoColor=white)
![Status](https://img.shields.io/badge/Status-Playable-00C851?style=for-the-badge)

**✨ 100% Procedural Graphics • No External Assets Required ✨**

</div>

---

## 🌊 About

**Coral Drift** is a polished, cozy endless runner built entirely with JavaFX. Guide your adorable pink octopus through a beautiful underwater world, collecting hearts and dodging colorful coral formations!

### ✨ Features

| 🎮 Gameplay | 🎨 Visuals | 🎯 Polish |
|-------------|------------|-----------|
| 💗 Heart collection with combo chains | 🌈 Beautiful gradient ocean layers | 🎪 Squash & stretch animations |
| 🪸 4 unique coral obstacle types | 🫧 Floating bubble particles | ⏱️ Coyote time & jump buffering |
| 📈 Progressive difficulty scaling | 🌟 Sparkle effects on collection | 📳 Screen shake on collision |
| 🏆 Local high score persistence | 🌿 Parallax seaweed backgrounds | 🎵 Sound system ready |

---

## 🎮 How to Play

| Key | Action |
|:---:|--------|
| `SPACE` `↑` `W` | 🦘 Jump |
| `P` `ESC` | ⏸️ Pause |
| `R` | 🔄 Restart |

### 💫 Pro Tips
- **Collect hearts in a row** to build your Harmony Chain multiplier!
- Hearts glow ✨ when you're on a streak
- Chain breaks after 3 seconds without a heart
- Some hearts require precise jumping — worth the risk! 💕

---

## 🚀 Quick Start

### Maven (Recommended)
```bash
mvn clean javafx:run
```

### IntelliJ IDEA
1. Open project → Ensure JDK 17+ is set
2. Run `MainApp.java`

### Manual
```bash
# Get JavaFX from https://openjfx.io/
javac --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.graphics,javafx.media -d out $(find src -name "*.java")
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.graphics,javafx.media -cp out com.coraldrift.MainApp
```

---

## 🎨 Color Palette

Our carefully crafted ocean aesthetic:

| Color | Hex | Use |
|-------|-----|-----|
| 🌊 Deep Ocean | `#0a1628` | Background depth |
| 🐙 Octopus Pink | `#ff69b4` | Our protagonist! |
| 💗 Heart Red | `#ff4757` | Collectibles |
| 🪸 Coral Pink | `#ff6b8a` | Obstacles |
| ✨ Sparkle Gold | `#ffd93d` | Effects |
| 🌿 Teal Accent | `#4ecdc4` | UI highlights |

---

## 📁 Project Structure

```
🐙 coral-drift/
├── 📦 src/main/java/com/coraldrift/
│   ├── 🎯 core/          # Game engine, loop, collision
│   ├── 🐙 entity/        # Player, coral, hearts
│   ├── 🎨 graphics/      # Renderers, particles, backgrounds
│   ├── 🖼️ scene/         # Menu, game, overlays
│   ├── 🏭 spawner/       # Obstacle & heart spawning
│   ├── 🎛️ ui/            # HUD, buttons, styling
│   ├── 🔊 audio/         # Sound system
│   └── 🔧 util/          # Constants, math, save data
├── 📄 pom.xml
└── 📖 README.md
```

---

## ⚙️ Tuning

All values in `Constants.java`:

```java
// 🦘 Jump Feel
GRAVITY = 2200.0
JUMP_VELOCITY = -920.0
COYOTE_TIME_MS = 150.0

// 📈 Difficulty  
BASE_SCROLL_SPEED = 300.0
MAX_SCROLL_SPEED = 650.0
MIN_OBSTACLE_GAP = 450.0

// 💗 Rewards
HEART_SPAWN_CHANCE = 0.5
CHAIN_MULTIPLIER = 0.5
```

---

## 🔮 Roadmap

- [ ] 🔊 Add sound effects
- [ ] 🛡️ Power-ups (bubble shield, magnet)
- [ ] 👗 Octopus skin customization
- [ ] 🌙 Day/night cycle
- [ ] 🏅 Achievement system

---

<div align="center">

### 🐙💖

**Swim through the depths, collect hearts, chase high scores!**

*Made with love and JavaFX*

</div>
