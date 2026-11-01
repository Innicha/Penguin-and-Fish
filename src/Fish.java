import java.awt.*;

// Inheritance: Fish สืบทอดจาก Entity
public class Fish extends Entity {
    private boolean collected;
    private int animTick;

    // Constructor
    public Fish(double x, double y, int size) {
        super(x, y, size, 0);
        this.collected = false;
        this.animTick = 0;
        loadSprite("fish.png");
    }

    @Override
    public void update() {
        animTick++;
    }

    @Override
    public void draw(Graphics2D g2) {
        if (collected) return;

        int fx = (int) Math.round(x);
        int fy = (int) Math.round(y);
        int bounce = (int) (Math.sin(animTick * 0.1) * 3);

        // ประกายดาวสีทองวิบวับรอบตัวปลา
        g2.setColor(new Color(255, 200, 40));
        drawSparkle(g2, fx + 8, fy + 12 + bounce, 5);
        drawSparkle(g2, fx + size - 10, fy + 10 - bounce, 4);
        drawSparkle(g2, fx + 12, fy + size - 12 - bounce, 4);
        drawSparkle(g2, fx + size - 12, fy + size - 10 + bounce, 5);

        if (sprite != null) {
            g2.drawImage(sprite, fx + 6, fy + 8 + bounce, size - 12, size - 16, null);
        } else {
            g2.setColor(new Color(15, 115, 220));
            Polygon tail = new Polygon(
                new int[]{fx + size - 22, fx + size - 6, fx + size - 6},
                new int[]{fy + size / 2 + bounce, fy + size / 4 + bounce, fy + size * 3 / 4 + bounce},
                3
            );
            g2.fillPolygon(tail);
            g2.fillArc(fx + size / 3, fy + size / 6 + bounce, size / 3, size / 4, 0, 180);

            g2.setColor(new Color(40, 160, 255));
            g2.fillOval(fx + 10, fy + size / 4 + bounce, size - 26, size / 2);
            g2.setColor(new Color(165, 225, 255));
            g2.fillArc(fx + 12, fy + size / 3 + bounce, size - 32, size / 3, 180, 180);

            g2.setColor(Color.WHITE);
            g2.fillOval(fx + 18, fy + size / 3 + bounce, 10, 10);
            g2.setColor(Color.BLACK);
            g2.fillOval(fx + 20, fy + size / 3 + 2 + bounce, 6, 6);
        }
    }

    private void drawSparkle(Graphics2D g2, int cx, int cy, int r) {
        Polygon star = new Polygon(
            new int[]{cx, cx + r / 2, cx + r, cx + r / 2, cx, cx - r / 2, cx - r, cx - r / 2},
            new int[]{cy - r, cy - r / 2, cy, cy + r / 2, cy + r, cy + r / 2, cy, cy - r / 2},
            8
        );
        g2.fillPolygon(star);
    }

    // Getters & Setters (Encapsulation)
    public boolean isCollected() { return collected; }
    public void setCollected(boolean collected) { this.collected = collected; }
}