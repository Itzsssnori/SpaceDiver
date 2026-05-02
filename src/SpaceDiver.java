import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.sound.sampled.*;
import javax.swing.*;

public class SpaceDiver extends JPanel implements ActionListener, KeyListener, MouseListener {

    // ── Window / game constants ──────────────────────────────────────────────
    static final int W = 800, H = 500;
    static final int FPS = 60;
    static final double GRAVITY = 0.22;
    static final double THRUST = -5.5;
    static final double MAX_FALL = 9.0;
    static final int GAP = 160;           // gap height between asteroid rings
    static final int RING_WIDTH = 28;
    static final int RING_SPAWN_DIST = 280;

    // ── Audio system ──────────────────────────────────────────────────────────
    Map<String, Clip> soundClips = new HashMap<>();

    void loadSounds() {
        try {
            String[] sounds = {"wing", "hit", "point", "die"};
            // Try multiple paths: relative first, then absolute
            java.io.File[] possiblePaths = {
                new java.io.File("audio"),
                new java.io.File("c:\\Users\\Nori\\Downloads\\audio"),
                new java.io.File(System.getProperty("user.dir") + "\\audio")
            };
            
            java.io.File audioDir = null;
            for (java.io.File path : possiblePaths) {
                if (path.exists() && path.isDirectory()) {
                    audioDir = path;
                    break;
                }
            }
            
            if (audioDir == null) {
                System.err.println("Audio directory not found");
                return;
            }
            
            for (String sound : sounds) {
                java.io.File file = new java.io.File(audioDir, sound + ".wav");
                if (file.exists()) {
                    AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                    Clip clip = AudioSystem.getClip();
                    clip.open(ais);
                    soundClips.put(sound, clip);
                } else {
                    System.err.println("Sound file not found: " + file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading sounds: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void playSound(String name) {
        try {
            Clip clip = soundClips.get(name);
            if (clip != null) {
                if (clip.isRunning()) {
                    clip.stop();  // Stop if already playing
                }
                clip.setFramePosition(0);  // Reset to beginning
                clip.start();
            }
        } catch (Exception e) {
            // Sound playback failed, but game continues
        }
    }

    // ── Random (initialize first, needed by other fields) ───────────────────
    Random rng = new Random();

    // ── Ship state ───────────────────────────────────────────────────────────
    double shipX = 180, shipY = H / 2.0;
    double velY = 0;
    boolean alive = true;
    boolean started = false;
    int score = 0;
    int bestScore = 0;
    Color shipColor;

    // ── Obstacles ─────────────────────────────────────────────────────────────
    static class Ring {
        double x;
        int gapTop;   // y-coordinate where gap starts
        int gapSize;  // Variable gap height (100-220)
        boolean scored = false;
        double angle = 0;

        Ring(double x, int gapTop, int gapSize) { 
            this.x = x; 
            this.gapTop = gapTop; 
            this.gapSize = gapSize; 
        }
    }
    java.util.List<Ring> rings = new java.util.ArrayList<>();

    // ── Particles (exhaust + death) ───────────────────────────────────────────
    static class Particle {
        double x, y, vx, vy, life, maxLife;
        Color color;
        double size;

        Particle(double x, double y, double vx, double vy, double life, Color color, double size) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.life = this.maxLife = life;
            this.color = color; this.size = size;
        }
        boolean dead() { return life <= 0; }
        void update() { x += vx; y += vy; vy += 0.05; life--; }
    }
    java.util.List<Particle> particles = new java.util.ArrayList<>();

    // ── Stars (parallax layers) ──────────────────────────────────────────────
    static class Star {
        double x, y, speed, brightness;
        Star(double x, double y, double speed, double brightness) {
            this.x = x; this.y = y; this.speed = speed; this.brightness = brightness;
        }
    }
    java.util.List<Star> stars = new java.util.ArrayList<>();

    // ── Background objects (decorative: asteroids, planets, moons) ──────────
    static class BackgroundObject {
        double x, y, vx, vy, rotation, rotSpeed;
        double size, opacity;
        int type; // 0 = asteroid, 1 = planet, 2 = moon
        Color color;

        BackgroundObject(double x, double y, double vx, double vy, double size, int type, Color color) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.size = size; this.type = type; this.color = color;
            this.rotation = Math.random() * Math.PI * 2;
            this.rotSpeed = (Math.random() - 0.5) * 0.02;
            this.opacity = 0.6 + Math.random() * 0.4;
        }
        void update() { x += vx; y += vy; rotation += rotSpeed; }
    }
    java.util.List<BackgroundObject> bgObjects = new java.util.ArrayList<>();

    // ── Death effect ─────────────────────────────────────────────────────────
    int flashFrames = 0;
    double shockX, shockY, shockR = 0;
    boolean shockActive = false;

    // ── Start screen orbiting planets ─────────────────────────────────────────
    java.util.List<BackgroundObject> orbitingPlanets = new java.util.ArrayList<>();
    double orbitRotation = 0;
    int clickAnimationPhase = 0;

    // ── Speed multiplier (increases with score) ───────────────────────────────
    double speed() { return 2.8 + (score / 5) * 0.4; }

    // ── Fonts ─────────────────────────────────────────────────────────────────
    Font fontHUD, fontBig, fontSmall;

    // ────────────────────────────────────────────────────────────────────────
    public SpaceDiver() {
        setPreferredSize(new Dimension(W, H));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        // Sci-fi fonts (using fallbacks to system fonts that are widely available)
        fontHUD   = new Font("Courier New", Font.BOLD, 32);   // Monospace for scores
        fontBig   = new Font("Courier New", Font.BOLD, 56);   // Large title font
        fontSmall = new Font("Courier New", Font.PLAIN, 16);  // Small text

        loadSounds();  // Load audio at startup
        initStars();
        shipColor = generateShipColor();
        spawnBackgroundObjects();
        spawnStartPlanets();
        spawnRing(W + 50);

        javax.swing.Timer timer = new javax.swing.Timer(1000 / FPS, this);
        timer.start();
    }

    void initStars() {
        stars.clear();
        for (int i = 0; i < 80; i++)
            stars.add(new Star(rng.nextInt(W), rng.nextInt(H), 0.3 + rng.nextDouble() * 0.4, 0.3 + rng.nextDouble() * 0.4));
        for (int i = 0; i < 40; i++)
            stars.add(new Star(rng.nextInt(W), rng.nextInt(H), 0.8 + rng.nextDouble() * 0.7, 0.7 + rng.nextDouble() * 0.3));
    }

    void spawnBackgroundObjects() {
        bgObjects.clear();
        // Distribute objects evenly across the screen with spacing - start with fewer
        int gridCols = 2;
        int gridRows = 2;
        int cellWidth = W / gridCols;
        int cellHeight = H / gridRows;
        
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                // Randomize position within each grid cell with padding
                int padding = 40;
                double x = W + col * cellWidth + padding + rng.nextInt(cellWidth - padding * 2);
                double y = row * cellHeight + padding + rng.nextInt(cellHeight - padding * 2);
                
                // Vary velocity slightly
                double vx = -(0.4 + rng.nextDouble() * 0.3);
                double vy = (rng.nextDouble() - 0.5) * 0.2;
                double size = 15 + rng.nextDouble() * 35;
                int type = rng.nextInt(4);
                Color color = getBackgroundObjectColor(type);
                bgObjects.add(new BackgroundObject(x, y, vx, vy, size, type, color));
            }
        }
    }

    Color getBackgroundObjectColor(int type) {
        switch(type) {
            case 0: // asteroid - gray/brown
                return new Color(120 + rng.nextInt(60), 100 + rng.nextInt(40), 80 + rng.nextInt(40));
            case 1: // planet - blue/orange
                return rng.nextBoolean() ? new Color(80, 140 + rng.nextInt(80), 200) : new Color(220, 140 + rng.nextInt(80), 60);
            case 2: // moon - pale
                return new Color(200 + rng.nextInt(55), 200 + rng.nextInt(55), 180 + rng.nextInt(40));
            case 3: // meteor - reddish/orange
                return new Color(255, 100 + rng.nextInt(100), 50 + rng.nextInt(80));
            default: return Color.GRAY;
        }
    }

    void spawnRing(double x) {
        // Variable gap heights like Flappy Bird - sometimes easy, sometimes hard
        int gapSize = 100 + rng.nextInt(120);  // Gap size between 100-220 pixels
        int minTop = 40;
        int maxTop = H - gapSize - 40;
        int gapTop = minTop + rng.nextInt(Math.max(1, maxTop - minTop));
        rings.add(new Ring(x, gapTop, gapSize));
    }

    void reset() {
        shipX = 180; shipY = H / 2.0; velY = 0;
        alive = true; started = false; score = 0;
        rings.clear(); particles.clear(); bgObjects.clear();
        flashFrames = 0; shockActive = false; shockR = 0;
        shipColor = generateShipColor();
        spawnBackgroundObjects();
        spawnRing(W + 50);
        initStars();
    }

    Color generateShipColor() {
        int colorType = rng.nextInt(5);
        switch(colorType) {
            case 0: return new Color(0, 220, 255);       // Cyan
            case 1: return new Color(255, 100, 200);     // Pink/Magenta
            case 2: return new Color(100, 255, 150);     // Green
            case 3: return new Color(255, 180, 100);     // Orange
            default: return new Color(200, 150, 255);    // Purple
        }
    }

    void thrust() {
        if (!alive) return;
        if (!started) started = true;
        playSound("wing");
        velY = THRUST;
        
        // Thruster burst - bright orange/yellow particles
        for (int i = 0; i < 10; i++) {
            double vx = 2.0 + rng.nextDouble() * 2.5;
            double vy = (rng.nextDouble() - 0.5) * 2.5;
            
            // Mix of orange and golden colors for thruster flame
            Color flameColor = rng.nextBoolean() 
                ? new Color(255, 140, 40, 200)   // Warm orange
                : new Color(255, 200, 80, 180);  // Golden
            
            particles.add(new Particle(
                shipX - 32, 
                shipY + (rng.nextDouble() - 0.5) * 4, 
                vx, 
                vy, 
                22 + rng.nextInt(14), 
                flameColor, 
                4 + rng.nextDouble() * 4
            ));
        }
    }

    void die() {
        alive = false;
        playSound("hit");
        bestScore = Math.max(bestScore, score);
        flashFrames = 12;
        shockX = shipX; shockY = shipY; shockR = 5; shockActive = true;
        
        // Debris burst - orange and white hot particles
        for (int i = 0; i < 50; i++) {
            double angle = rng.nextDouble() * Math.PI * 2;
            double spd = 2 + rng.nextDouble() * 6;
            
            Color debrisColor = rng.nextInt(3) == 0
                ? new Color(255, 255, 200, 220)  // Hot white
                : (rng.nextBoolean() 
                    ? new Color(255, 120, 40, 210)    // Orange
                    : new Color(255, 200, 80, 200));  // Golden
            
            particles.add(new Particle(
                shipX, 
                shipY, 
                Math.cos(angle) * spd, 
                Math.sin(angle) * spd, 
                40 + rng.nextInt(25), 
                debrisColor, 
                2.5 + rng.nextDouble() * 5
            ));
        }
    }

    // ── Game loop ────────────────────────────────────────────────────────────
    @Override public void actionPerformed(ActionEvent e) {
        if (started && alive) {
            // Physics
            velY += GRAVITY;
            if (velY > MAX_FALL) velY = MAX_FALL;
            shipY += velY;

            // Boundaries
            if (shipY < 8) { shipY = 8; velY = 0; }
            if (shipY > H - 11) die();

            // Stars parallax
            for (Star s : stars) {
                s.x -= s.speed * (speed() / 2.8);
                if (s.x < 0) s.x = W;
            }

            // Background objects
            double bgSpeed = speed() * 0.6;
            for (BackgroundObject obj : bgObjects) {
                obj.x -= bgSpeed;
                obj.update();
            }
            // Spawn new background objects and remove off-screen
            BackgroundObject last = bgObjects.get(bgObjects.size() - 1);
            if (last.x < W - 400) spawnNewBackgroundObject();
            bgObjects.removeIf(obj -> obj.x < -100);

            // Rings
            double sp = speed();
            for (Ring r : rings) {
                r.x -= sp;
                r.angle += 0.03;

                // Score
                if (!r.scored && r.x + RING_WIDTH < shipX) {
                    r.scored = true;
                    playSound("point");
                    score++;
                }

                // Collision (ship is now larger ~20x20 triangle - stricter)
                double sx = shipX, sy = shipY;
                boolean inRingX = sx + 16 > r.x && sx - 16 < r.x + RING_WIDTH;
                boolean inGap   = sy - 16 > r.gapTop && sy + 16 < r.gapTop + r.gapSize;
                if (inRingX && !inGap) die();
            }

            // Spawn new ring
            Ring lastRing = rings.get(rings.size() - 1);
            if (lastRing.x < W - RING_SPAWN_DIST) spawnRing(W + 30);

            // Remove off-screen rings
            rings.removeIf(r -> r.x < -60);

            // Exhaust trail (passive, continuous subtle glow)
            if (rng.nextInt(3) == 0) {
                Color c = rng.nextBoolean() 
                    ? new Color(255, 140, 60, 160)   // Warm orange
                    : new Color(255, 100, 40, 140);  // Deep orange
                particles.add(new Particle(
                    shipX - 32, 
                    shipY + (rng.nextDouble() - 0.5) * 5, 
                    1.2 + rng.nextDouble() * 0.8, 
                    (rng.nextDouble() - 0.5) * 0.8, 
                    15, 
                    c, 
                    2 + rng.nextDouble() * 2.5
                ));
            }
        }

        // Particles always update
        for (Particle p : particles) p.update();
        particles.removeIf(Particle::dead);

        if (flashFrames > 0) flashFrames--;
        if (shockActive) { shockR += 8; if (shockR > 120) shockActive = false; }

        // Update start screen animation
        if (!started && alive) {
            orbitRotation += 0.01;
            clickAnimationPhase = (clickAnimationPhase + 1) % 60;
        }

        repaint();
    }

    void spawnNewBackgroundObject() {
        // Spawn at right edge, distribute vertically
        double x = W + 50;
        double y = rng.nextInt(H / 3) * (H / (H / 3)); // Rough distribution across height
        if (rng.nextBoolean()) y = H / 3 + rng.nextInt(H / 3);
        if (rng.nextBoolean()) y = 2 * H / 3 + rng.nextInt(H / 3);
        
        double vx = -(0.4 + rng.nextDouble() * 0.3);
        double vy = (rng.nextDouble() - 0.5) * 0.2;
        double size = 15 + rng.nextDouble() * 35;
        int type = rng.nextInt(4);
        Color color = getBackgroundObjectColor(type);
        bgObjects.add(new BackgroundObject(x, y, vx, vy, size, type, color));
    }

    void spawnStartPlanets() {
        orbitingPlanets.clear();
        // Create 3 planets that will orbit at the start screen
        double[] sizes = { 35, 45, 30 };
        int[] types = { 1, 1, 2 }; // planets and moon
        for (int i = 0; i < 3; i++) {
            Color color = getBackgroundObjectColor(types[i]);
            BackgroundObject planet = new BackgroundObject(W/2, H/2, 0, 0, sizes[i], types[i], color);
            orbitingPlanets.add(planet);
        }
    }

    void drawOrbitingPlanets(Graphics2D g) {
        int centerX = W / 2;
        int centerY = H / 2 + 30;
        int[] orbitRadii = { 120, 200, 280 };

        for (int i = 0; i < orbitingPlanets.size(); i++) {
            BackgroundObject planet = orbitingPlanets.get(i);
            double angle = orbitRotation + (i * Math.PI * 2 / 3);
            int x = (int)(centerX + Math.cos(angle) * orbitRadii[i]);
            int y = (int)(centerY + Math.sin(angle) * orbitRadii[i]);

            AffineTransform old = g.getTransform();
            g.translate(x, y);
            g.rotate(planet.rotation);

            int sz = (int)planet.size;
            int alpha = (int)(planet.opacity * 180);
            if (planet.type == 1) {
                drawPlanet(g, sz, alpha, planet);
            } else {
                drawMoon(g, sz, alpha, planet);
            }

            g.setTransform(old);

            // Update planet rotation for next frame
            planet.rotation += planet.rotSpeed;
        }
    }

    // ── Rendering ────────────────────────────────────────────────────────────
    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background gradient - deep navy to dark purple space
        GradientPaint bg = new GradientPaint(0, 0, new Color(2, 5, 25), 0, H, new Color(15, 5, 35));
        g.setPaint(bg);
        g.fillRect(0, 0, W, H);

        // Nebula clouds in background for atmosphere
        drawNebula(g);

        // Stars (parallax layers)
        for (Star s : stars) {
            int alpha = (int)(s.brightness * 220);
            g.setColor(new Color(200, 210, 255, alpha));
            int sz = s.speed > 0.7 ? 2 : 1;
            g.fillOval((int)s.x, (int)s.y, sz, sz);
        }

        // Background objects
        for (BackgroundObject obj : bgObjects) {
            drawBackgroundObject(g, obj);
        }

        // Shockwave
        if (shockActive) {
            int alpha = Math.max(0, 180 - (int)(shockR * 1.5));
            g.setColor(new Color(255, 120, 40, alpha));
            g.setStroke(new BasicStroke(3));
            g.drawOval((int)(shockX - shockR), (int)(shockY - shockR), (int)(shockR * 2), (int)(shockR * 2));
        }

        // Electric barriers (rings)
        for (Ring r : rings) {
            drawRing(g, r);
        }

        // Particles
        for (Particle p : particles) {
            float alpha = (float)(p.life / p.maxLife);
            Color c = p.color;
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha * 220)));
            int sz = (int)(p.size * alpha + 1);
            g.fillOval((int)(p.x - sz / 2.0), (int)(p.y - sz / 2.0), sz, sz);
        }

        // Ship (sleek spaceship with thruster)
        if (alive || flashFrames > 0) {
            drawShip(g, shipX, shipY, velY);
        }

        // Screen flash on impact
        if (flashFrames > 0) {
            int alpha = (int)(flashFrames / 12.0 * 120);
            g.setColor(new Color(255, 80, 40, alpha));
            g.fillRect(0, 0, W, H);
        }

        // HUD
        drawHUD(g);
    }

    void drawNebula(Graphics2D g) {
        // Create multiple nebula clouds with purple/cyan/magenta tones
        Random r = new Random(42); // Fixed seed for consistent nebula
        
        // Layer 1 - Large purple nebula clouds
        g.setColor(new Color(100, 30, 150, 40));
        for (int i = 0; i < 3; i++) {
            int cx = (i * W / 2) + 150;
            int cy = H / 3 + (int)(30 * Math.sin(i * 1.5));
            int rad = 180 + i * 40;
            
            // Multiple overlapping circles for cloud effect
            for (int j = 0; j < 4; j++) {
                int ox = (int)(Math.cos(j * Math.PI / 2) * (rad / 2));
                int oy = (int)(Math.sin(j * Math.PI / 2) * (rad / 2));
                g.fillOval(cx + ox - rad/3, cy + oy - rad/3, rad*2/3, rad*2/3);
            }
        }
        
        // Layer 2 - Cyan/electric blue accents
        g.setColor(new Color(0, 100, 200, 30));
        for (int i = 0; i < 2; i++) {
            int cx = (int)(W * 0.3 + i * W * 0.4 + 50 * Math.sin(i));
            int cy = (int)(H * 0.6 + 40 * Math.cos(i * 1.3));
            int rad = 140 + i * 30;
            g.fillOval(cx - rad, cy - rad, rad * 2, rad * 2);
        }
        
        // Layer 3 - Subtle magenta/pink accents in distance
        g.setColor(new Color(150, 20, 100, 25));
        int cx2 = W / 4;
        int cy2 = H / 2;
        g.fillOval(cx2 - 200, cy2 - 150, 400, 300);
    }

    void drawRing(Graphics2D g, Ring r) {
        int x = (int)r.x;
        int gapTop = r.gapTop;
        int gapBot = gapTop + r.gapSize;

        // Draw electric barriers
        drawElectricBarrier(g, x, 0, gapTop, r.angle, true);
        drawElectricBarrier(g, x, gapBot, H - gapBot, r.angle, false);
    }

    void drawElectricBarrier(Graphics2D g, int x, int y, int height, double angle, boolean isTop) {
        int barrierW = 56;
        
        // Outer violet/purple glow aura
        for (int i = 8; i >= 1; i--) {
            int alpha = (int)(80 * (1.0 - i / 8.0));
            g.setColor(new Color(150 + i * 5, 50, 200 + i * 2, alpha));
            g.fillRect(x - i, y, barrierW + i * 2, height);
        }

        // Main barrier body - gradient from electric blue to cyan
        GradientPaint gradient = new GradientPaint(x, y, new Color(0, 100, 220, 200), x + barrierW, y, new Color(0, 200, 255, 200));
        g.setPaint(gradient);
        g.fillRect(x, y, barrierW, height);

        // Bright electric blue core
        g.setColor(new Color(100, 200, 255, 150));
        g.fillRect(x + 8, y, barrierW - 16, height);

        // Dynamic electrical crackling - jagged bolts
        Random r2 = new Random((long)(x * 31 + y + angle * 1000));
        
        // Multiple electrical strike patterns
        for (int bolt = 0; bolt < height / 20; bolt++) {
            int startY = y + bolt * 20 + r2.nextInt(12);
            int boltX = x + barrierW / 2;
            int boltY = startY;
            
            // Main bolt - bright white/cyan
            g.setColor(new Color(200, 255, 255, 220));
            int bolts = 3 + r2.nextInt(3);
            for (int i = 0; i < bolts; i++) {
                int nextX = boltX + r2.nextInt(20) - 10;
                int nextY = boltY + 6 + r2.nextInt(4);
                g.setStroke(new BasicStroke(2f));
                g.drawLine(boltX, boltY, nextX, nextY);
                
                // Secondary branches (thinner, purple-ish)
                if (r2.nextDouble() > 0.6) {
                    g.setColor(new Color(180, 100, 255, 150));
                    g.setStroke(new BasicStroke(1.2f));
                    int branchX = nextX + r2.nextInt(8) - 4;
                    int branchY = nextY + r2.nextInt(6);
                    g.drawLine(nextX, nextY, branchX, branchY);
                    g.setColor(new Color(200, 255, 255, 220));
                }
                boltX = nextX;
                boltY = nextY;
            }
        }

        // Electric hum pulse effect - pulsing glow rings
        int pulseAlpha = (int)(100 + 80 * Math.sin(angle * 8));
        g.setColor(new Color(100, 200, 255, pulseAlpha));
        g.setStroke(new BasicStroke(1.5f));
        for (int pulse = 0; pulse < height; pulse += 40) {
            int py = y + pulse;
            if (py < y + height) {
                g.drawRect(x - 4, py, barrierW + 8, 2);
            }
        }

        // Intense bright edge highlights (dangerous look)
        g.setColor(new Color(200, 255, 255, 200));
        g.setStroke(new BasicStroke(2.5f));
        g.drawRect(x, y, barrierW, height);
        
        // Additional cyan inner edge for that crackling look
        g.setColor(new Color(100, 220, 255, 150));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRect(x + 2, y + 2, barrierW - 4, height - 4);
    }

    void drawBackgroundObject(Graphics2D g, BackgroundObject obj) {
        int x = (int)obj.x;
        int y = (int)obj.y;
        int sz = (int)obj.size;
        int alpha = (int)(obj.opacity * 150);

        AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.rotate(obj.rotation);

        if (obj.type == 0) {
            // Asteroid - detailed rocky appearance
            drawAsteroid(g, sz, alpha, obj);
        } else if (obj.type == 1) {
            // Planet - enhanced with atmosphere and details
            drawPlanet(g, sz, alpha, obj);
        } else if (obj.type == 2) {
            // Moon - pale with craters
            drawMoon(g, sz, alpha, obj);
        } else if (obj.type == 3) {
            // Meteor - fiery comet with trail
            drawMeteor(g, sz, alpha, obj);
        }

        g.setTransform(old);
    }

    void drawAsteroid(Graphics2D g, int sz, int alpha, BackgroundObject obj) {
        // Main body - irregular rocky shape
        g.setColor(new Color(obj.color.getRed(), obj.color.getGreen(), obj.color.getBlue(), alpha));
        g.fillOval(-sz/2, -sz/2, sz, sz);

        // Shadow for depth
        g.setColor(new Color(40, 30, 20, alpha / 2));
        g.fillOval(-sz/2 + 3, -sz/2 + 3, sz - 6, sz - 6);

        // Multiple detailed craters
        g.setColor(new Color(50, 40, 30, alpha / 2));
        Random r2 = new Random((long)(obj.x * obj.y));
        for (int i = 0; i < 8; i++) {
            int cx = -sz/2 + 3 + r2.nextInt(sz - 6);
            int cy = -sz/2 + 3 + r2.nextInt(sz - 6);
            int cs = 2 + r2.nextInt(sz / 5);
            g.fillOval(cx - cs/2, cy - cs/2, cs, cs);
            
            // Inner crater highlights
            g.setColor(new Color(80, 70, 60, alpha / 3));
            g.fillOval(cx - cs/3, cy - cs/3, cs/2, cs/2);
            g.setColor(new Color(50, 40, 30, alpha / 2));
        }

        // Rock ridges and fault lines
        g.setColor(new Color(100, 85, 70, alpha / 3));
        g.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < 5; i++) {
            int x1 = -sz/2 + r2.nextInt(sz);
            int y1 = -sz/2 + r2.nextInt(sz);
            int x2 = x1 + r2.nextInt(sz / 3) - sz / 6;
            int y2 = y1 + r2.nextInt(sz / 3) - sz / 6;
            g.drawLine(x1, y1, x2, y2);
        }

        // Surface texture with small bumps
        g.setColor(new Color(120, 100, 80, alpha / 4));
        for (int i = 0; i < 10; i++) {
            int bx = -sz/2 + 2 + r2.nextInt(sz - 4);
            int by = -sz/2 + 2 + r2.nextInt(sz - 4);
            int bs = 1 + r2.nextInt(2);
            g.fillOval(bx, by, bs, bs);
        }

        // Edge highlight for depth
        g.setColor(new Color(150, 130, 110, alpha / 3));
        g.setStroke(new BasicStroke(2));
        g.drawOval(-sz/2, -sz/2, sz, sz);
    }

    void drawPlanet(Graphics2D g, int sz, int alpha, BackgroundObject obj) {
        // Atmospheric glow layers
        for (int i = 6; i >= 1; i--) {
            int glowAlpha = (int)(alpha * (1.0 - i / 6.0) * 0.5);
            Color glowColor = new Color(obj.color.getRed(), obj.color.getGreen(), obj.color.getBlue(), glowAlpha);
            g.setColor(glowColor);
            g.fillOval(-sz/2 - i*2, -sz/2 - i*2, sz + i*4, sz + i*4);
        }

        // Main planet body with radial gradient effect
        g.setColor(new Color(obj.color.getRed(), obj.color.getGreen(), obj.color.getBlue(), alpha));
        g.fillOval(-sz/2, -sz/2, sz, sz);

        // Cloud/storm bands with turbulent appearance
        Random r2 = new Random((long)(obj.x * obj.y * 7));
        int bandCount = 3 + r2.nextInt(3);
        for (int i = 0; i < bandCount; i++) {
            int bandY = -sz/2 + (int)((i + 0.5) * sz / (bandCount + 1));
            int bandH = 3 + r2.nextInt(5);
            
            // Wave effect for bands
            for (int x = -sz/2; x < sz/2; x += 8) {
                int waveHeight = (int)(2 * Math.sin(x * 0.02 + obj.rotation * 3) + 2);
                int bandAlpha = 30 + r2.nextInt(60);
                Color bandColor = new Color(
                    Math.max(0, obj.color.getRed() - 50),
                    Math.max(0, obj.color.getGreen() - 50),
                    Math.max(0, obj.color.getBlue() - 50),
                    bandAlpha
                );
                g.setColor(bandColor);
                g.fillRect(x, bandY + waveHeight, 8, bandH);
            }
        }

        // Swirling storm pattern
        g.setColor(new Color(255, 255, 100, alpha / 3));
        for (int i = 0; i < 2; i++) {
            int stormX = (int)(Math.cos(obj.rotation + i) * sz / 4);
            int stormY = (int)(Math.sin(obj.rotation + i * 1.5) * sz / 5);
            int stormSize = sz / 6 + r2.nextInt(sz / 8);
            g.fillOval(stormX - stormSize/2, stormY - stormSize/2, stormSize, stormSize);
        }

        // Planetary rings (tilted)
        if (r2.nextBoolean()) {
            g.setColor(new Color(200, 180, 150, alpha / 2));
            g.setStroke(new BasicStroke(2));
            AffineTransform ringTransform = g.getTransform();
            g.rotate(Math.PI / 6);
            g.drawOval(-sz/2 - 12, -sz/3, sz + 24, sz / 2);
            g.setTransform(ringTransform);
        }

        // Bright limb (edge lighting)
        g.setColor(new Color(255, 255, 255, alpha / 3));
        g.setStroke(new BasicStroke(3));
        g.drawOval(-sz/2 + 2, -sz/2 + 2, sz - 4, sz - 4);

        // Specular highlight (bright spot)
        g.setColor(new Color(255, 255, 255, alpha / 2));
        int hlSize = sz / 5;
        g.fillOval(-sz/4 - hlSize/2, -sz/4 - hlSize/2, hlSize, hlSize);
    }

    void drawMoon(Graphics2D g, int sz, int alpha, BackgroundObject obj) {
        // Subtle atmospheric glow
        for (int i = 3; i >= 1; i--) {
            int glowAlpha = (int)(alpha * (1.0 - i / 3.0) * 0.2);
            g.setColor(new Color(obj.color.getRed(), obj.color.getGreen(), obj.color.getBlue(), glowAlpha));
            g.fillOval(-sz/2 - i, -sz/2 - i, sz + i*2, sz + i*2);
        }

        // Moon body
        g.setColor(new Color(obj.color.getRed(), obj.color.getGreen(), obj.color.getBlue(), alpha));
        g.fillOval(-sz/2, -sz/2, sz, sz);

        // Surface shadowing for depth
        g.setColor(new Color(150, 140, 130, alpha / 3));
        g.fillArc(-sz/2, -sz/2, sz, sz, 45, 180);

        // Detailed craters of various sizes
        g.setColor(new Color(100, 90, 80, alpha / 2));
        Random r2 = new Random((long)(obj.x * obj.y));
        for (int i = 0; i < 6; i++) {
            int cx = -sz/2 + 3 + r2.nextInt(sz - 6);
            int cy = -sz/2 + 3 + r2.nextInt(sz - 6);
            int cs = 2 + r2.nextInt(sz / 4);
            
            // Crater rim
            g.setColor(new Color(120, 110, 100, alpha / 2));
            g.drawOval(cx - cs/2, cy - cs/2, cs, cs);
            
            // Crater interior (darker)
            g.setColor(new Color(70, 60, 50, alpha / 2));
            g.fillOval(cx - cs/2 + 1, cy - cs/2 + 1, cs - 2, cs - 2);
            
            // Center point (deepest part)
            g.setColor(new Color(50, 40, 30, alpha / 2));
            if (cs > 3) {
                g.fillOval(cx - 1, cy - 1, 2, 2);
            }
        }

        // Mountain ranges (bright ridges)
        g.setColor(new Color(200, 190, 180, alpha / 3));
        g.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < 3; i++) {
            int x1 = -sz/2 + 5 + r2.nextInt(sz - 10);
            int y1 = -sz/2 + 5 + r2.nextInt(sz - 10);
            int x2 = x1 + r2.nextInt(sz / 4) - sz / 8;
            int y2 = y1 + r2.nextInt(sz / 4) - sz / 8;
            g.drawLine(x1, y1, x2, y2);
        }

        // Bright limb (edge highlight)
        g.setColor(new Color(255, 255, 255, alpha / 2));
        g.setStroke(new BasicStroke(2));
        g.drawOval(-sz/2 + 1, -sz/2 + 1, sz - 2, sz - 2);

        // Prominent bright spot (light reflection from sun)
        g.setColor(new Color(255, 255, 255, alpha / 2));
        int hlSize = sz / 6;
        g.fillOval(-sz/3 - hlSize/2, -sz/3 - hlSize/2, hlSize, hlSize);
    }

    void drawMeteor(Graphics2D g, int sz, int alpha, BackgroundObject obj) {
        // Dynamically animated trail based on rotation (movement)
        int trailLength = (int)(50 + 30 * Math.sin(obj.rotation * 2));
        
        // Outer flame trail (orange/red)
        g.setColor(new Color(255, 120, 30, alpha / 3));
        g.fillRect(-sz/2 - trailLength - 20, -sz/4 - 15, trailLength + 20, sz/2 + 30);
        
        // Middle flame trail (bright orange)
        g.setColor(new Color(255, 160, 60, alpha / 2));
        g.fillRect(-sz/2 - trailLength, -sz/4 - 8, trailLength, sz/4 + 16);

        // Inner hot trail (yellow-white)
        g.setColor(new Color(255, 220, 100, alpha / 3));
        int innerTrail = (int)(trailLength * 0.6);
        g.fillRect(-sz/2 - innerTrail, -sz/6, innerTrail, sz/3);

        // Main meteor body - spherical rock
        g.setColor(new Color(obj.color.getRed(), obj.color.getGreen(), obj.color.getBlue(), alpha));
        g.fillOval(-sz/2, -sz/2, sz, sz);

        // Molten surface - glowing cracks
        Random r2 = new Random((long)(obj.rotation * 1000));
        g.setColor(new Color(255, 180, 50, alpha / 2));
        for (int i = 0; i < 3; i++) {
            int cx = -sz/2 + 3 + r2.nextInt(sz - 6);
            int cy = -sz/2 + 3 + r2.nextInt(sz - 6);
            int cw = 2 + r2.nextInt(sz / 3);
            int ch = 1 + r2.nextInt(2);
            g.fillRect(cx, cy, cw, ch);
        }

        // Hot plasma glow layers
        for (int i = 4; i >= 1; i--) {
            int glowAlpha = (int)(alpha * (1.0 - i / 4.0) * 0.4);
            g.setColor(new Color(255, 150 + i * 15, 50, glowAlpha));
            g.fillOval(-sz/2 - i*2, -sz/2 - i*2, sz + i*4, sz + i*4);
        }

        // Bright core/center
        g.setColor(new Color(255, 255, 150, alpha / 2));
        int coreSize = sz / 3;
        g.fillOval(-coreSize/2, -coreSize/2, coreSize, coreSize);

        // Extra bright hotspot
        g.setColor(new Color(255, 255, 200, alpha / 2));
        int spotSize = sz / 5;
        g.fillOval(-spotSize/2 + sz/6, -spotSize/2 - sz/8, spotSize, spotSize);
    }

    void drawAsteroidBar(Graphics2D g, int x, int y, int height, double angle) {
        // Gradient bar
        GradientPaint gp = new GradientPaint(x, y, new Color(100, 40, 200), x + RING_WIDTH, y + height, new Color(60, 20, 140));
        g.setPaint(gp);
        g.fillRect(x, y, RING_WIDTH, height);

        // Rock texture overlay — small ellipses
        g.setColor(new Color(80, 30, 160, 120));
        Random r2 = new Random(x * 7 + y);
        for (int i = 0; i < height / 12; i++) {
            int ex = x + r2.nextInt(RING_WIDTH);
            int ey = y + r2.nextInt(height);
            g.fillOval(ex, ey, 5 + r2.nextInt(8), 4 + r2.nextInt(6));
        }

        // Neon edge highlight
        g.setColor(new Color(180, 80, 255, 200));
        g.setStroke(new BasicStroke(2));
        g.drawRect(x, y, RING_WIDTH, height);
    }

    void drawPortalEdge(Graphics2D g, int x, int gapEdge, double angle) {
        // Pulsing neon ring at gap edge
        int cx = x + RING_WIDTH / 2;
        int r = 14;
        float pulse = (float)(0.6 + 0.4 * Math.sin(angle * 3));
        g.setColor(new Color(0, (int)(200 * pulse), 255, 200));
        g.setStroke(new BasicStroke(3));
        g.drawOval(cx - r, gapEdge - r / 2, r * 2, r);
    }

    void drawShip(Graphics2D g, double cx, double cy, double vy) {
        // Tilt based on vertical velocity (nose up when rising, nose down when falling)
        double tilt = vy * -0.06; // negative: up velocity = upward tilt
        tilt = Math.max(-0.6, Math.min(0.6, tilt));
        
        AffineTransform old = g.getTransform();
        g.translate(cx, cy);
        g.rotate(tilt);

        // ─── Engine Thruster Glow (back of ship) ───────────────────────────
        // Multiple concentric glows with warm orange colors
        int[] glowRadii = {28, 22, 16, 10};
        int[] glowAlphas = {40, 80, 120, 160};
        Color[] glowColors = {
            new Color(255, 100, 20, glowAlphas[0]),
            new Color(255, 150, 40, glowAlphas[1]),
            new Color(255, 180, 60, glowAlphas[2]),
            new Color(255, 200, 100, glowAlphas[3])
        };
        
        for (int i = 0; i < glowRadii.length; i++) {
            g.setColor(glowColors[i]);
            g.fillOval(-35 - glowRadii[i]/2, -glowRadii[i]/2, glowRadii[i], glowRadii[i]);
        }

        // ─── Ship Body (main fuselage) ───────────────────────────────────────
        // Rounded fuselage shape - sleek and aerodynamic
        Path2D fuselage = new Path2D.Double();
        fuselage.moveTo(16, -8);      // Front point (nose)
        fuselage.curveTo(18, -9, 12, -11, 0, -11);   // Top curve
        fuselage.lineTo(-24, -8);     // Back top
        fuselage.curveTo(-28, -6, -30, -4, -32, 0);  // Engine back
        fuselage.lineTo(-24, 8);      // Back bottom
        fuselage.lineTo(0, 11);       // Bottom curve
        fuselage.curveTo(12, 11, 18, 9, 16, 8);      // Front bottom
        fuselage.closePath();
        
        // Main body gradient using shipColor
        Color darkColor = new Color(
            Math.max(0, shipColor.getRed() - 80),
            Math.max(0, shipColor.getGreen() - 80),
            Math.max(0, shipColor.getBlue() - 80),
            220
        );
        GradientPaint bodyGrad = new GradientPaint(
            -20, -10, shipColor,
            16, 0, new Color(shipColor.getRed(), shipColor.getGreen(), shipColor.getBlue(), 255)
        );
        g.setPaint(bodyGrad);
        g.fill(fuselage);

        // ─── Wing Fins (port and starboard) ──────────────────────────────────
        // Upper wing
        Path2D upperWing = new Path2D.Double();
        upperWing.moveTo(-8, -11);
        upperWing.lineTo(-18, -18);
        upperWing.lineTo(-20, -14);
        upperWing.lineTo(-10, -9);
        upperWing.closePath();
        
        g.setColor(new Color(shipColor.getRed(), shipColor.getGreen(), shipColor.getBlue(), 200));
        g.fill(upperWing);
        
        // Lower wing
        Path2D lowerWing = new Path2D.Double();
        lowerWing.moveTo(-8, 11);
        lowerWing.lineTo(-18, 18);
        lowerWing.lineTo(-20, 14);
        lowerWing.lineTo(-10, 9);
        lowerWing.closePath();
        
        g.fill(lowerWing);

        // ─── Cockpit Window (front center) ───────────────────────────────────
        g.setColor(new Color(200, 255, 255, 200));
        g.fillOval(6, -6, 10, 12);
        
        // Cockpit shine/reflection
        g.setColor(new Color(255, 255, 255, 150));
        g.fillOval(7, -5, 4, 4);

        // ─── Engine Exhaust Ports (side vents) ───────────────────────────────
        // Left exhaust vent
        g.setColor(new Color(255, 150, 80, 180));
        g.fillOval(-24, -7, 5, 5);
        
        // Right exhaust vent
        g.fillOval(-24, 2, 5, 5);

        // ─── Hull Details (panel lines) ──────────────────────────────────────
        g.setColor(new Color(
            Math.max(0, shipColor.getRed() - 100),
            Math.max(0, shipColor.getGreen() - 100),
            Math.max(0, shipColor.getBlue() - 100),
            100
        ));
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(0, -9, -20, -7);  // Top panel line
        g.drawLine(0, 9, -20, 7);    // Bottom panel line

        // ─── Main Body Outline ───────────────────────────────────────────────
        Color brightColor = new Color(
            Math.min(255, shipColor.getRed() + 50),
            Math.min(255, shipColor.getGreen() + 50),
            Math.min(255, shipColor.getBlue() + 50),
            200
        );
        g.setColor(brightColor);
        g.setStroke(new BasicStroke(2));
        g.draw(fuselage);
        
        // Wing outlines
        g.draw(upperWing);
        g.draw(lowerWing);

        g.setTransform(old);

        // ─── Thruster Particle Trail ────────────────────────────────────────
        // Create trailing particles continuously when ship is falling
        if (vy > 1 && alive) {
            for (int i = 0; i < 2; i++) {
                double pvy = rng.nextDouble() * 0.8 + 0.5;
                double pvx = rng.nextDouble() * 1.2 - 0.6;
                Color trailColor = rng.nextBoolean() 
                    ? new Color(255, 150, 40, 180)  // Warm orange
                    : new Color(255, 180, 80, 160); // Golden orange
                particles.add(new Particle(
                    cx - 32, 
                    cy + (rng.nextDouble() - 0.5) * 8, 
                    pvx, 
                    pvy, 
                    20 + rng.nextInt(12), 
                    trailColor, 
                    2.5 + rng.nextDouble() * 3
                ));
            }
        }
    }

    void drawHUD(Graphics2D g) {
        // Score display with glow effect
        g.setFont(fontHUD);
        String scoreStr = String.format("%04d", score);
        
        // Glow layers for score
        for (int i = 4; i >= 1; i--) {
            int glowAlpha = (int)(60 * (1.0 - i / 4.0));
            g.setColor(new Color(0, 200, 255, glowAlpha));
            g.setFont(new Font(fontHUD.getName(), Font.BOLD, fontHUD.getSize() + i));
            FontMetrics fm = g.getFontMetrics();
            int x = W/2 - fm.stringWidth(scoreStr) / 2;
            g.drawString(scoreStr, x, 50);
        }
        
        // Main score text - bright cyan
        g.setFont(fontHUD);
        g.setColor(new Color(0, 255, 200, 255));
        FontMetrics fm = g.getFontMetrics();
        int x = W/2 - fm.stringWidth(scoreStr) / 2;
        g.drawString(scoreStr, x, 50);

        // TIER label - electric orange with subtle glow
        g.setFont(fontSmall);
        g.setColor(new Color(255, 120, 20, 200));
        int tier = score / 5 + 1;
        String tierStr = "TIER " + tier;
        
        // Tier glow
        g.setColor(new Color(255, 150, 50, 80));
        g.drawString(tierStr, 28, 34);
        
        g.setColor(new Color(255, 180, 60, 240));
        g.drawString(tierStr, 30, 32);

        // BEST label - violet with subtle glow
        String bestStr = "BEST " + bestScore;
        
        // Best glow
        g.setColor(new Color(150, 100, 200, 80));
        fm = g.getFontMetrics();
        g.drawString(bestStr, W - 125, 34);
        
        g.setColor(new Color(180, 120, 255, 230));
        g.drawString(bestStr, W - 122, 32);

        // Start screen
        if (!started && alive) {
            // Draw orbiting planets
            drawOrbitingPlanets(g);

            // Title
            drawCenteredText(g, "SPACE DIVER", fontBig, new Color(0, 220, 255), H/2 - 100);
            drawCenteredText(g, "DODGE THE BARRIERS", fontSmall, new Color(100, 180, 255), H/2 - 20);

            // Animated click indicator
            int clickAlpha = Math.max(0, Math.min(255, 100 + (int)(150 * Math.sin(clickAnimationPhase * Math.PI / 30))));
            g.setColor(new Color(255, 150, 60, clickAlpha));
            g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
            FontMetrics fmEmoji = g.getFontMetrics();
            String emoji = "👆";
            int emojiX = W/2 - fmEmoji.stringWidth(emoji) / 2;
            int emojiY = H/2 + 40;
            g.drawString(emoji, emojiX, emojiY);

            drawCenteredText(g, "PRESS SPACE OR CLICK TO START", fontSmall, new Color(150, 200, 255), H/2 + 95);
        }

        // Game over screen
        if (!alive) {
            drawCenteredText(g, "SHIP DESTROYED", fontBig, new Color(255, 80, 60), H/2 - 60);
            drawCenteredText(g, "SCORE  " + score, fontHUD, new Color(0, 220, 255), H/2);
            drawCenteredText(g, "BEST   " + bestScore, fontHUD, new Color(180, 120, 255), H/2 + 44);
            drawCenteredText(g, "PRESS R TO RETRY", fontSmall, new Color(150, 200, 255), H/2 + 90);
        }
    }

    void drawCenteredText(Graphics2D g, String text, Font font, Color color, int y) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics(font);
        int x = (W - fm.stringWidth(text)) / 2;
        // Shadow
        g.setColor(new Color(0, 0, 0, 140));
        g.drawString(text, x + 2, y + 2);
        g.setColor(color);
        g.drawString(text, x, y);
    }

    // ── Input ─────────────────────────────────────────────────────────────────
    @Override public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) thrust();
        if (e.getKeyCode() == KeyEvent.VK_R && !alive) reset();
    }
    @Override public void mousePressed(MouseEvent e) {
        if (!alive) reset();
        else thrust();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Space Diver");
            SpaceDiver game = new SpaceDiver();
            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }
}
