import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

// Abstract Class: คลาสแม่แบบนามธรรมสำหรับวัตถุทั้งหมดในเกม
abstract class Entity {
    // Encapsulation: ซ่อนข้อมูลด้วย protected
    protected double x, y;
    protected double startX, startY;
    protected double speed;
    protected int size;
    protected BufferedImage sprite;

    // Constructor
    public Entity(double x, double y, int size, double speed) {
        this.x = x;
        this.y = y;
        this.startX = x;
        this.startY = y;
        this.size = size;
        this.speed = speed;
    }

    protected void loadSprite(String fileName) {
        try {
            File f = new File(fileName);
            if (f.exists()) {
                sprite = ImageIO.read(f);
            }
        } catch (Exception e) {
            sprite = null;
        }
    }

    // Abstract Methods บังคับให้คลาสลูกนำไป Override (Polymorphism)
    public abstract void draw(Graphics2D g2);
    public abstract void update();

    // Polymorphism (Method Overloading)
    public void resetPosition() {
        this.x = this.startX;
        this.y = this.startY;
    }

    public void resetPosition(double newX, double newY) {
        this.x = newX;
        this.y = newY;
    }

    // กรอบสี่เหลี่ยมสำหรับตรวจจับการชนกันระหว่างตัวละคร
    public Rectangle getBounds() {
        int pad = 16;
        return new Rectangle((int) Math.round(x) + pad, (int) Math.round(y) + pad, size - pad * 2, size - pad * 2);
    }

    // ตรวจสอบว่าพิกัด (nx, ny) เดินไปได้หรือไม่ (ไม่ชนบล็อกน้ำแข็ง ต้นสน ก้อนหิน หรือขอบสนาม)
    protected boolean canMoveTo(double nx, double ny, int[][] map, int offsetX, int offsetY, int tileSize) {
        int pad = 15;
        double left = nx + pad;
        double right = nx + size - pad;
        double top = ny + pad;
        double bottom = ny + size - pad;

        int mapWidth = map[0].length * tileSize;
        int mapHeight = map.length * tileSize;

        if (left < offsetX || right >= offsetX + mapWidth || top < offsetY || bottom >= offsetY + mapHeight) {
            return false;
        }

        double[][] checkPoints = {
            {left, top}, {right, top}, {left, bottom}, {right, bottom},
            {(left + right) / 2.0, top}, {(left + right) / 2.0, bottom},
            {left, (top + bottom) / 2.0}, {right, (top + bottom) / 2.0}
        };

        for (double[] pt : checkPoints) {
            int col = (int) ((pt[0] - offsetX) / tileSize);
            int row = (int) ((pt[1] - offsetY) / tileSize);
            if (row >= 0 && row < map.length && col >= 0 && col < map[0].length) {
                int tile = map[row][col];
                // 1 = บล็อกน้ำแข็ง, 2 = ต้นสน, 3 = ก้อนหิน (เดินผ่านไม่ได้)
                if (tile == 1 || tile == 2 || tile == 3) {
                    return false;
                }
            }
        }
        return true;
    }

    // Getters & Setters (Encapsulation)
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public int getSize() { return size; }
}

// Inheritance: Penguin สืบทอดคุณสมบัติจาก Entity
public class Penguin extends Entity {
    private int lives;
    private int collectedFishCount;
    private int hurtTimer;

    // Constructor
    public Penguin(double x, double y, int size, int initialLives) {
        super(x, y, size, 5.0); // ความเร็วเคลื่อนที่แบบลื่นไหล
        this.lives = initialLives;
        this.collectedFishCount = 0;
        this.hurtTimer = 0;
        loadSprite("penguin.png");
    }

    // การเคลื่อนที่แบบอิสระลื่นไหลทุกทิศทาง พร้อมระบบช่วยไถลผ่านมุมบล็อก (Corner Assist)
    public void moveSmooth(boolean up, boolean down, boolean left, boolean right, int[][] map, int offsetX, int offsetY, int tileSize) {
        double dx = 0;
        double dy = 0;

        if (up) dy -= speed;
        if (down) dy += speed;
        if (left) dx -= speed;
        if (right) dx += speed;

        // ปรับความเร็วแนวทแยงให้สมดุล
        if (dx != 0 && dy != 0) {
            dx *= 0.7071;
            dy *= 0.7071;
        }

        // เคลื่อนที่แกน X พร้อมระบบไถลหลบมุมอัตโนมัติ
        if (dx != 0) {
            if (canMoveTo(x + dx, y, map, offsetX, offsetY, tileSize)) {
                x += dx;
            } else if (dy == 0) {
                // ช่วยดันตัวละครหลบขอบมุมบน/ล่างเมื่อเดินเฉี่ยวสิ่งกีดขวาง
                for (int slide = 1; slide <= 10; slide++) {
                    if (canMoveTo(x + dx, y - slide, map, offsetX, offsetY, tileSize)) {
                        y -= 2.0;
                        break;
                    }
                    if (canMoveTo(x + dx, y + slide, map, offsetX, offsetY, tileSize)) {
                        y += 2.0;
                        break;
                    }
                }
            }
        }

        // เคลื่อนที่แกน Y พร้อมระบบไถลหลบมุมอัตโนมัติ
        if (dy != 0) {
            if (canMoveTo(x, y + dy, map, offsetX, offsetY, tileSize)) {
                y += dy;
            } else if (dx == 0) {
                // ช่วยดันตัวละครหลบขอบมุมซ้าย/ขวาเมื่อเดินเฉี่ยวสิ่งกีดขวาง
                for (int slide = 1; slide <= 10; slide++) {
                    if (canMoveTo(x - slide, y + dy, map, offsetX, offsetY, tileSize)) {
                        x -= 2.0;
                        break;
                    }
                    if (canMoveTo(x + slide, y + dy, map, offsetX, offsetY, tileSize)) {
                        x += 2.0;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void update() {
        if (hurtTimer > 0) {
            hurtTimer--;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        if (hurtTimer > 0 && (hurtTimer / 4) % 2 == 0) {
            return;
        }
        drawCustomPenguin(g2, (int) Math.round(x), (int) Math.round(y), size, false, hurtTimer > 0, false);
    }

    public void drawCustomPenguin(Graphics2D g2, int px, int py, int s, boolean isCrying, boolean isHurt, boolean showHeldFish) {
        g2.setColor(new Color(140, 195, 235, 160));
        g2.fillOval(px + s / 6, py + s - s / 5, s * 2 / 3, s / 6);

        if (sprite != null && !isCrying && !isHurt) {
            g2.drawImage(sprite, px + 4, py + 4, s - 8, s - 8, null);
        } else {
            g2.setColor(new Color(245, 140, 20));
            g2.fillOval(px + s / 5, py + s - s / 4, s / 4, s / 6);
            g2.fillOval(px + s - s * 9 / 20, py + s - s / 4, s / 4, s / 6);

            g2.setColor(new Color(25, 38, 50));
            g2.fillOval(px + s / 6, py + s / 6, s * 2 / 3, s * 3 / 4);
            g2.fillOval(px + s / 12, py + s * 2 / 5, s / 5, s * 2 / 5);
            g2.fillOval(px + s - s * 7 / 24, py + s * 2 / 5, s / 5, s * 2 / 5);

            g2.setColor(Color.WHITE);
            g2.fillOval(px + s / 4, py + s / 3, s / 2, s / 2);
            g2.fillOval(px + s / 4, py + s / 4, s / 4, s / 4);
            g2.fillOval(px + s / 2 - 2, py + s / 4, s / 4, s / 4);

            g2.setColor(Color.BLACK);
            if (isHurt) {
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawLine(px + s / 3, py + s / 3, px + s * 2 / 5, py + s * 3 / 8);
                g2.drawLine(px + s * 2 / 5, py + s * 3 / 8, px + s / 3, py + s * 5 / 12);
                g2.drawLine(px + s * 2 / 3, py + s / 3, px + s * 3 / 5, py + s * 3 / 8);
                g2.drawLine(px + s * 3 / 5, py + s * 3 / 8, px + s * 2 / 3, py + s * 5 / 12);
            } else if (isCrying) {
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawArc(px + s / 3, py + s / 3, s / 8, s / 10, 180, 180);
                g2.drawArc(px + s * 13 / 24, py + s / 3, s / 8, s / 10, 180, 180);
                g2.setColor(new Color(80, 195, 255));
                g2.fillOval(px + s * 7 / 24, py + s * 2 / 5, s / 10, s / 6);
                g2.fillOval(px + s * 15 / 24, py + s * 2 / 5, s / 10, s / 6);
            } else {
                int eyeW = Math.max(6, s / 9);
                g2.fillOval(px + s * 7 / 20, py + s / 3, eyeW, eyeW + 2);
                g2.fillOval(px + s * 11 / 20, py + s / 3, eyeW, eyeW + 2);
                g2.setColor(Color.WHITE);
                g2.fillOval(px + s * 7 / 20 + 2, py + s / 3 + 1, eyeW / 3, eyeW / 3);
                g2.fillOval(px + s * 11 / 20 + 2, py + s / 3 + 1, eyeW / 3, eyeW / 3);
            }

            g2.setColor(new Color(255, 175, 175));
            g2.fillOval(px + s * 5 / 18, py + s * 5 / 12, s / 10, s / 16);
            g2.fillOval(px + s * 11 / 18, py + s * 5 / 12, s / 10, s / 16);
            g2.setColor(new Color(255, 165, 30));
            g2.fillOval(px + s * 5 / 12, py + s * 3 / 8, s / 6, s / 9);
        }

        // ผ้าพันคอสีแดง
        g2.setColor(new Color(215, 35, 35));
        g2.fillRoundRect(px + s / 4, py + s * 11 / 24, s / 2, s / 8, 8, 8);
        g2.fillRoundRect(px + s * 11 / 20, py + s * 11 / 24, s / 8, s / 4, 6, 6);

        if (showHeldFish) {
            g2.setColor(new Color(35, 150, 250));
            g2.fillOval(px + s / 2, py + s * 3 / 5, s / 3, s / 5);
            Polygon tail = new Polygon(
                new int[]{px + s * 5 / 6, px + s, px + s},
                new int[]{py + s * 7 / 10, py + s * 3 / 5, py + s * 4 / 5}, 3
            );
            g2.fillPolygon(tail);
        }
    }

    // Getters & Setters (Encapsulation)
    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }
    public void loseLife() {
        if (this.lives > 0) this.lives--;
        this.hurtTimer = 70;
    }
    public int getCollectedFishCount() { return collectedFishCount; }
    public void setCollectedFishCount(int count) { this.collectedFishCount = count; }
    public void addFish() { this.collectedFishCount++; }
    public int getHurtTimer() { return hurtTimer; }
}