import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

// Inheritance: Seal สืบทอดจาก Entity
public class Seal extends Entity {
    public static final int TYPE_CHASE = 0;  // เดินไล่ตามเพนกวินรอบฉากและหลบสิ่งกีดขวางเอง
    public static final int TYPE_PATROL = 1; // เคลื่อนที่เฉพาะบริเวณของตัวเอง

    private int behaviorType;
    private double minX, maxX, minY, maxY;
    private double patrolVX, patrolVY;

    // Constructor Overloading #1: สำหรับแมวน้ำที่ไล่ตามเพนกวินทั่วฉาก (TYPE_CHASE)
    public Seal(double x, double y, int size, double speed) {
        super(x, y, size, speed);
        this.behaviorType = TYPE_CHASE;
        loadSprite("seal.png");
    }

    // Constructor Overloading #2: สำหรับแมวน้ำที่เคลื่อนที่เฉพาะบริเวณของตัวเอง (TYPE_PATROL)
    public Seal(double x, double y, int size, double speed, double minX, double maxX, double minY, double maxY) {
        super(x, y, size, speed);
        this.behaviorType = TYPE_PATROL;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.patrolVX = (maxX > minX) ? speed : 0;
        this.patrolVY = (maxY > minY && maxX == minX) ? speed : 0;
        loadSprite("seal.png");
    }

    @Override
    public void update() {
        // เมธอดมาตรฐานจาก Entity
    }

    // อัลกอริทึมควบคุมการเคลื่อนที่ของแมวน้ำทั้ง 2 รูปแบบ
    public void updateAI(Penguin target, int[][] map, int offsetX, int offsetY, int tileSize) {
        if (behaviorType == TYPE_PATROL) {
            updatePatrolMovement(map, offsetX, offsetY, tileSize);
        } else {
            updateChaseMovement(target, map, offsetX, offsetY, tileSize);
        }
    }

    // 1. รูปแบบเฝ้าบริเวณของตัวเอง (TYPE_PATROL) ในด่านที่ 2 และ 3
    private void updatePatrolMovement(int[][] map, int offsetX, int offsetY, int tileSize) {
        double nextX = x + patrolVX;
        double nextY = y + patrolVY;

        if (patrolVX != 0) {
            if (nextX > maxX || nextX < minX || !canMoveTo(nextX, y, map, offsetX, offsetY, tileSize)) {
                patrolVX = -patrolVX;
            } else {
                x = nextX;
            }
        }
        if (patrolVY != 0) {
            if (nextY > maxY || nextY < minY || !canMoveTo(x, nextY, map, offsetX, offsetY, tileSize)) {
                patrolVY = -patrolVY;
            } else {
                y = nextY;
            }
        }
    }

    // 2. รูปแบบเดินไล่ตามเพนกวินรอบฉากโดยหลบสิ่งกีดขวางอัตโนมัติ (BFS Pathfinding + Smooth Tracking)
    private void updateChaseMovement(Penguin target, int[][] map, int offsetX, int offsetY, int tileSize) {
        int rows = map.length;
        int cols = map[0].length;

        int sCol = Math.max(0, Math.min(cols - 1, (int) ((x + size / 2.0 - offsetX) / tileSize)));
        int sRow = Math.max(0, Math.min(rows - 1, (int) ((y + size / 2.0 - offsetY) / tileSize)));
        int tCol = Math.max(0, Math.min(cols - 1, (int) ((target.getX() + size / 2.0 - offsetX) / tileSize)));
        int tRow = Math.max(0, Math.min(rows - 1, (int) ((target.getY() + size / 2.0 - offsetY) / tileSize)));

        double goalX = target.getX();
        double goalY = target.getY();

        // หากอยู่คนละช่องตาราง ให้ค้นหาช่องทางเดินถัดไปที่หลบสิ่งกีดขวางด้วย BFS
        if (sRow != tRow || sCol != tCol) {
            int[] nextStep = findNextTileBFS(sRow, sCol, tRow, tCol, map);
            goalX = offsetX + nextStep[1] * tileSize;
            goalY = offsetY + nextStep[0] * tileSize;
        }

        double dx = goalX - x;
        double dy = goalY - y;
        double dist = Math.hypot(dx, dy);
        if (dist < 0.5) return;

        double vx = (dx / dist) * speed;
        double vy = (dy / dist) * speed;

        // เคลื่อนที่แบบลื่นไหลพร้อมระบบปรับศูนย์กลางช่องทางเดินไม่ให้ติดมุมบล็อก
        if (canMoveTo(x + vx, y, map, offsetX, offsetY, tileSize)) {
            x += vx;
        } else {
            double centerY = offsetY + sRow * tileSize;
            if (Math.abs(y - centerY) > 1.5) {
                y += Math.signum(centerY - y) * speed;
            }
        }

        if (canMoveTo(x, y + vy, map, offsetX, offsetY, tileSize)) {
            y += vy;
        } else {
            double centerX = offsetX + sCol * tileSize;
            if (Math.abs(x - centerX) > 1.5) {
                x += Math.signum(centerX - x) * speed;
            }
        }
    }

    // อัลกอริทึม Breadth-First Search (BFS) หาเส้นทางอ้อมสิ่งกีดขวางที่สั้นที่สุดไปยังเพนกวิน
    private int[] findNextTileBFS(int sR, int sC, int tR, int tC, int[][] map) {
        int rows = map.length;
        int cols = map[0].length;
        boolean[][] visited = new boolean[rows][cols];
        int[][][] parent = new int[rows][cols][2];

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{sR, sC});
        visited[sR][sC] = true;

        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        boolean found = false;

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int r = curr[0];
            int c = curr[1];

            if (r == tR && c == tC) {
                found = true;
                break;
            }

            for (int[] d : dirs) {
                int nr = r + d[0];
                int nc = c + d[1];
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && !visited[nr][nc]) {
                    int tile = map[nr][nc];
                    // 0 = พื้นน้ำแข็ง, 4 = บ้านเพนกวิน (แมวน้ำเดินผ่านได้ แต่จะหลบ 1, 2, 3)
                    if (tile == 0 || tile == 4) {
                        visited[nr][nc] = true;
                        parent[nr][nc][0] = r;
                        parent[nr][nc][1] = c;
                        queue.add(new int[]{nr, nc});
                    }
                }
            }
        }

        if (!found) {
            return new int[]{tR, tC};
        }

        // ย้อนรอยเส้นทางกลับมาหาก้าวแรกสุดที่แมวน้ำต้องเดินไป
        int cr = tR;
        int cc = tC;
        while (parent[cr][cc][0] != sR || parent[cr][cc][1] != sC) {
            int pr = parent[cr][cc][0];
            int pc = parent[cr][cc][1];
            cr = pr;
            cc = pc;
        }
        return new int[]{cr, cc};
    }

    @Override
    public void draw(Graphics2D g2) {
        int sx = (int) Math.round(x);
        int sy = (int) Math.round(y);

        // เงาพื้นน้ำแข็ง
        g2.setColor(new Color(135, 190, 230, 160));
        g2.fillOval(sx + 6, sy + size - 18, size - 12, 14);

        if (sprite != null) {
            g2.drawImage(sprite, sx + 2, sy + 4, size - 4, size - 8, null);
        } else {
            // แมวน้ำไล่ล่า (สีเทาเข้ม) vs แมวน้ำเฝ้าโซนตัวเอง (สีเทาอมฟ้า)
            g2.setColor(behaviorType == TYPE_PATROL ? new Color(125, 155, 185) : new Color(150, 160, 170));
            g2.fillOval(sx + 8, sy + size / 3, size - 16, size / 2);
            g2.fillOval(sx + 14, sy + size / 6, size - 28, size / 2);

            g2.setColor(new Color(115, 125, 135));
            g2.fillOval(sx + size / 2 + 4, sy + size / 2, 6, 5);
            g2.fillOval(sx + size / 2 + 12, sy + size / 2 + 6, 5, 4);

            g2.setColor(new Color(215, 220, 225));
            g2.fillOval(sx + 20, sy + size * 3 / 8, size - 40, size / 5);

            g2.setColor(Color.BLACK);
            g2.fillOval(sx + 22, sy + size / 4 + 4, 7, 8);
            g2.fillOval(sx + size - 29, sy + size / 4 + 4, 7, 8);
            g2.setStroke(new BasicStroke(2.2f));
            g2.drawLine(sx + 20, sy + size / 4, sx + 29, sy + size / 4 + 4);
            g2.drawLine(sx + size - 20, sy + size / 4, sx + size - 29, sy + size / 4 + 4);

            g2.fillOval(sx + size / 2 - 4, sy + size * 3 / 8, 8, 5);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawLine(sx + 12, sy + size * 3 / 8 + 2, sx + 22, sy + size * 3 / 8 + 4);
            g2.drawLine(sx + 12, sy + size * 3 / 8 + 7, sx + 22, sy + size * 3 / 8 + 6);
            g2.drawLine(sx + size - 12, sy + size * 3 / 8 + 2, sx + size - 22, sy + size * 3 / 8 + 4);
            g2.drawLine(sx + size - 12, sy + size * 3 / 8 + 7, sx + size - 22, sy + size * 3 / 8 + 6);
        }
    }

    public int getBehaviorType() { return behaviorType; }
}