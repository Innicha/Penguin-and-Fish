import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener {
    public static final int SCREEN_WIDTH = 920;
    public static final int SCREEN_HEIGHT = 680;
    public static final int COLS = 10;
    public static final int ROWS = 7;
    public static final int TILE_SIZE = 72;
    public static final int OFFSET_X = (SCREEN_WIDTH - (COLS * TILE_SIZE)) / 2;
    public static final int OFFSET_Y = 125;

    // สถานะของเกม (Game States)
    public static final int STATE_MENU = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_WIN = 3;
    public static final int STATE_LOSE = 4;

    private int gameState = STATE_MENU;
    private int currentLevel = 1;
    private final int maxLevels = 3;
    private int score = 120;
    private int timeLeft = 60;
    private int frameCounter = 0;
    private int levelBannerTimer = 0;

    // สถานะปุ่มกดค้างเพื่อเคลื่อนที่แบบลื่นไหลอิสระ
    private boolean upPressed, downPressed, leftPressed, rightPressed;

    // แผนที่ตาราง 10x7 (0=พื้นน้ำแข็ง, 1=บล็อกน้ำแข็ง, 2=ต้นสน, 3=ก้อนหิน, 4=บ้าน Igloo)
    private final int[][] mapGrid = {
        {2, 0, 0, 1, 0, 0, 0, 0, 2, 0},
        {0, 0, 0, 1, 1, 0, 0, 0, 0, 3},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {3, 0, 1, 0, 0, 0, 1, 0, 0, 0},
        {0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
        {1, 1, 1, 0, 0, 0, 1, 1, 1, 0},
        {2, 0, 3, 1, 1, 0, 3, 0, 4, 0}
    };

    // Composition: GamePanel ประกอบด้วย Penguin, Fish และ Seal
    private Penguin penguin;
    private ArrayList<Fish> fishes;
    private ArrayList<Seal> seals;
    private Fish menuDecoFish;
    private Timer gameTimer;

    // ปุ่มคลิกเมาส์บนหน้าจอทั้งหมด (ไม่ต้องใช้คีย์บอร์ดกดเริ่มใหม่)
    private final Rectangle btnStart = new Rectangle(240, 395, 250, 75);
    private final Rectangle btnHowTo = new Rectangle(195, 495, 170, 52);
    private final Rectangle btnExit = new Rectangle(380, 495, 140, 52);
    private final Rectangle btnPause = new Rectangle(840, 20, 55, 55);

    // ปุ่มเมื่อชนะ เมื่อแพ้ และเมื่อกดหยุดเกม
    private final Rectangle btnRestart = new Rectangle(285, 545, 175, 56);
    private final Rectangle btnMenu = new Rectangle(475, 545, 160, 56);
    private final Rectangle btnResume = new Rectangle(360, 355, 200, 52);

    // Constructor
    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        menuDecoFish = new Fish(470, 235, 80);
        startNewGame();

        // อัปเดตหน้าจอ 60 FPS เพื่อการเดินที่ลื่นไหล
        gameTimer = new Timer(16, this);
        gameTimer.start();
    }

    // เริ่มต้นเกมใหม่ตั้งแต่ด่านที่ 1
    private void startNewGame() {
        currentLevel = 1;
        score = 120;
        penguin = new Penguin(tileToPxX(4), tileToPxY(3), TILE_SIZE, 3);
        setupLevel(currentLevel);
    }

    private void setupLevel(int level) {
        upPressed = downPressed = leftPressed = rightPressed = false;
        timeLeft = 60;
        frameCounter = 0;
        levelBannerTimer = 90;

        penguin.resetPosition(tileToPxX(4), tileToPxY(3));
        penguin.setCollectedFishCount(0);

        fishes = new ArrayList<>();
        seals = new ArrayList<>();

        if (level == 1) {
            // ================= ด่านที่ 1 =================
            fishes.add(new Fish(tileToPxX(6), tileToPxY(1), TILE_SIZE));
            
            // ปรับความเร็วแมวน้ำตัวที่ 1 (ค่าปัจจุบันคือ 1.55)
            seals.add(new Seal(tileToPxX(1), tileToPxY(1), TILE_SIZE, 1.55));

        } else if (level == 2) {
            // ================= ด่านที่ 2 =================
            fishes.add(new Fish(tileToPxX(6), tileToPxY(1), TILE_SIZE));
            fishes.add(new Fish(tileToPxX(1), tileToPxY(4), TILE_SIZE));

            // 1. แมวน้ำไล่ตามตัวที่ 1 (ปรับความเร็วตรงเลข 1.55)
            seals.add(new Seal(tileToPxX(1), tileToPxY(1), TILE_SIZE, 1.0));
            
            // 2. แมวน้ำเฝ้าพื้นที่ตัวที่ 1 (ปรับความเร็วตรงเลข 1.85)
            seals.add(new Seal(tileToPxX(7), tileToPxY(3), TILE_SIZE, 1.85,
                               tileToPxX(7), tileToPxX(9), tileToPxY(3), tileToPxY(3)));

        } else if (level == 3) {
            // ================= ด่านที่ 3 =================
            fishes.add(new Fish(tileToPxX(6), tileToPxY(1), TILE_SIZE));
            fishes.add(new Fish(tileToPxX(1), tileToPxY(4), TILE_SIZE));
            fishes.add(new Fish(tileToPxX(5), tileToPxY(6), TILE_SIZE));

            // 1. แมวน้ำไล่ตามตัวที่ 1 (ปรับความเร็วตรงเลข 1.60)
            seals.add(new Seal(tileToPxX(1), tileToPxY(1), TILE_SIZE, 0.5));
            
            // 2. แมวน้ำไล่ตามตัวที่ 2 (ปรับความเร็วตรงเลข 1.45)
            seals.add(new Seal(tileToPxX(5), tileToPxY(0), TILE_SIZE, 0.5));
            
            // 3. แมวน้ำเฝ้าพื้นที่ตัวที่ 1 (ปรับความเร็วตรงเลข 2.0)
            seals.add(new Seal(tileToPxX(7), tileToPxY(4), TILE_SIZE, 2.0,
                               tileToPxX(7), tileToPxX(9), tileToPxY(4), tileToPxY(4)));
        }
    }

    // ย้ายแมวน้ำกลับจุดเริ่มต้นของด่านเมื่อเพนกวินโดนชน (ไม่ให้แมวน้ำยืนทับจุดเกิดเพนกวิน)
    private void resetActorsAfterHit() {
        penguin.resetPosition(tileToPxX(4), tileToPxY(3));
        for (Seal s : seals) {
            s.resetPosition();
        }
    }

    private double tileToPxX(int col) { return OFFSET_X + col * TILE_SIZE; }
    private double tileToPxY(int row) { return OFFSET_Y + row * TILE_SIZE; }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == STATE_MENU) {
            menuDecoFish.update();
        } else if (gameState == STATE_PLAYING) {
            if (levelBannerTimer > 0) levelBannerTimer--;

            frameCounter++;
            if (frameCounter >= 60) {
                frameCounter = 0;
                timeLeft--;
                if (timeLeft <= 0) {
                    timeLeft = 0;
                    gameState = STATE_LOSE;
                }
            }

            // เคลื่อนที่เพนกวินแบบลื่นไหลตามปุ่มที่กดค้าง
            penguin.moveSmooth(upPressed, downPressed, leftPressed, rightPressed, mapGrid, OFFSET_X, OFFSET_Y, TILE_SIZE);
            penguin.update();

            // อัปเดตปลาและ AI แมวน้ำทุกตัว
            for (Fish f : fishes) {
                f.update();
            }
            for (Seal seal : seals) {
                seal.updateAI(penguin, mapGrid, OFFSET_X, OFFSET_Y, TILE_SIZE);
            }

            checkCollisions();
        }
        repaint();
    }

    private void checkCollisions() {
        Rectangle pBox = penguin.getBounds();

        // 1. ตรวจสอบการเก็บปลา
        for (Fish f : fishes) {
            if (!f.isCollected() && pBox.intersects(f.getBounds())) {
                f.setCollected(true);
                penguin.addFish();
                score += 130;
            }
        }

        // 2. ตรวจสอบการชนแมวน้ำ
        if (penguin.getHurtTimer() == 0) {
            for (Seal seal : seals) {
                if (pBox.intersects(seal.getBounds())) {
                    penguin.loseLife();
                    if (penguin.getLives() <= 0) {
                        gameState = STATE_LOSE;
                    } else {
                        resetActorsAfterHit();
                    }
                    break;
                }
            }
        }

        // 3. เมื่อเก็บปลาครบตามจำนวนของด่านแล้วเดินกลับถึงบ้านน้ำแข็ง (Igloo)
        Rectangle iglooBounds = new Rectangle((int) tileToPxX(7) + 20, (int) tileToPxY(5) + 15, TILE_SIZE * 2, TILE_SIZE * 2 - 15);
        if (penguin.getCollectedFishCount() >= fishes.size() && pBox.intersects(iglooBounds)) {
            if (currentLevel < maxLevels) {
                // ผ่านด่าน -> ไปด่านถัดไปทันที
                currentLevel++;
                score += 100;
                setupLevel(currentLevel);
            } else {
                // ครบทั้ง 3 ด่านและชีวิตยังเหลือ -> ชนะเกม!
                gameState = STATE_WIN;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (gameState == STATE_MENU) {
            drawMenuScreen(g2);
        } else {
            drawGameplayScreen(g2);
            if (gameState == STATE_PAUSED) {
                drawPauseOverlay(g2);
            } else if (gameState == STATE_WIN) {
                drawWinOverlay(g2);
            } else if (gameState == STATE_LOSE) {
                drawLoseOverlay(g2);
            }
        }
    }

    // ================= ฉากที่ 1: หน้าเริ่มเกม (Menu Screen) =================
    private void drawMenuScreen(Graphics2D g2) {
        GradientPaint skyGrad = new GradientPaint(0, 0, new Color(130, 195, 255), 0, SCREEN_HEIGHT, new Color(225, 245, 255));
        g2.setPaint(skyGrad);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g2.setColor(new Color(190, 225, 250));
        g2.fillPolygon(new int[]{0, 120, 260}, new int[]{480, 220, 480}, 3);
        g2.fillPolygon(new int[]{150, 290, 430}, new int[]{480, 280, 480}, 3);

        g2.setColor(new Color(235, 248, 255));
        g2.fillRect(0, 440, SCREEN_WIDTH, 240);

        drawIgloo(g2, 660, 250, 220);

        g2.setColor(new Color(210, 240, 255));
        g2.fillRoundRect(110, 95, 500, 240, 60, 60);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(122, 107, 476, 216, 50, 50);
        g2.setColor(new Color(140, 200, 245));
        g2.setStroke(new BasicStroke(4f));
        g2.drawRoundRect(110, 95, 500, 240, 60, 60);

        g2.setFont(new Font("SansSerif", Font.BOLD, 74));
        g2.setColor(new Color(10, 65, 145));
        g2.drawString("Penguin", 160, 205);
        g2.setFont(new Font("SansSerif", Font.BOLD, 44));
        g2.setColor(new Color(65, 90, 110));
        g2.drawString("and", 170, 285);
        g2.setFont(new Font("SansSerif", Font.BOLD, 80));
        g2.setColor(new Color(25, 135, 235));
        g2.drawString("Fish", 265, 295);

        menuDecoFish.draw(g2);

        drawStyledButton(g2, btnStart, "START", new Color(40, 165, 245), new Color(15, 125, 215), 36);
        drawStyledButton(g2, btnHowTo, "HOW TO PLAY", new Color(175, 225, 255), new Color(125, 190, 240), 18);
        drawStyledButton(g2, btnExit, "EXIT", new Color(175, 225, 255), new Color(125, 190, 240), 20);

        penguin.drawCustomPenguin(g2, 540, 320, 240, false, false, false);
    }

    // ================= ฉากเล่นเกม (Gameplay Screen) =================
    private void drawGameplayScreen(Graphics2D g2) {
        g2.setColor(new Color(210, 235, 255));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g2.setColor(new Color(125, 185, 235));
        g2.fillRoundRect(OFFSET_X - 18, OFFSET_Y - 18, COLS * TILE_SIZE + 36, ROWS * TILE_SIZE + 36, 25, 25);
        g2.setColor(new Color(165, 215, 250));
        g2.fillRoundRect(OFFSET_X - 8, OFFSET_Y - 8, COLS * TILE_SIZE + 16, ROWS * TILE_SIZE + 16, 15, 15);

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int tx = OFFSET_X + c * TILE_SIZE;
                int ty = OFFSET_Y + r * TILE_SIZE;

                g2.setColor((r + c) % 2 == 0 ? new Color(195, 230, 255) : new Color(185, 222, 252));
                g2.fillRect(tx, ty, TILE_SIZE, TILE_SIZE);
                g2.setColor(new Color(160, 208, 245));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRect(tx, ty, TILE_SIZE, TILE_SIZE);

                int tileType = mapGrid[r][c];
                if (tileType == 1) drawIceBlock(g2, tx, ty, TILE_SIZE);
                else if (tileType == 2) drawPineTree(g2, tx, ty, TILE_SIZE);
                else if (tileType == 3) drawRock(g2, tx, ty, TILE_SIZE);
            }
        }

        drawIgloo(g2, OFFSET_X + 7 * TILE_SIZE + 10, OFFSET_Y + 5 * TILE_SIZE - 15, TILE_SIZE * 2 + 10);
        drawHUD(g2);

        for (Fish f : fishes) {
            f.draw(g2);
        }
        for (Seal seal : seals) {
            seal.draw(g2);
        }

        if (gameState == STATE_WIN) {
            penguin.drawCustomPenguin(g2, OFFSET_X + 7 * TILE_SIZE - 5, OFFSET_Y + 5 * TILE_SIZE + 25, TILE_SIZE + 15, false, false, true);
        } else if (gameState != STATE_LOSE) {
            penguin.draw(g2);
        }

        // ป้ายแสดงด่านเมื่อเริ่มด่านที่ 1, 2 และ 3
        if (levelBannerTimer > 0 && gameState == STATE_PLAYING) {
            g2.setColor(new Color(15, 65, 130, 210));
            g2.fillRoundRect(300, 280, 320, 80, 25, 25);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 32));
            g2.drawString("STAGE " + currentLevel + " / 3", 365, 322);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
            g2.drawString("Collect " + fishes.size() + " Fish & Return to Igloo!", 340, 348);
        }
    }

    private void drawHUD(Graphics2D g2) {
        drawHudBox(g2, 20, 18, 255, 58);
        penguin.drawCustomPenguin(g2, 26, 22, 48, false, false, false);
        g2.setFont(new Font("SansSerif", Font.BOLD, 24));
        g2.setColor(new Color(20, 50, 95));
        int displayLives = (gameState == STATE_LOSE) ? 0 : penguin.getLives();
        g2.drawString("x " + displayLives, 78, 55);

        for (int i = 0; i < 3; i++) {
            boolean active = (i < penguin.getLives()) && (gameState != STATE_LOSE);
            drawHeart(g2, 132 + i * 40, 30, 32, active);
        }

        drawHudBox(g2, 290, 18, 165, 58);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.setColor(new Color(30, 60, 100));
        g2.drawString("STAGE " + currentLevel + "/3 • SCORE", 312, 37);
        g2.setFont(new Font("SansSerif", Font.BOLD, 26));
        g2.drawString(String.valueOf(score), 348, 65);

        drawHudBox(g2, 470, 18, 155, 58);
        g2.setColor(new Color(90, 55, 35));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval(485, 28, 36, 36);
        g2.drawLine(503, 46, 503, 34);
        g2.drawLine(503, 46, 513, 46);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(new Color(30, 60, 100));
        g2.drawString("TIME", 545, 38);
        g2.setFont(new Font("SansSerif", Font.BOLD, 28));
        g2.drawString(String.valueOf(timeLeft), 546, 66);

        drawHudBox(g2, 645, 18, 175, 58);
        g2.setColor(new Color(40, 160, 255));
        g2.fillOval(662, 35, 34, 22);
        g2.fillPolygon(new int[]{694, 706, 706}, new int[]{46, 36, 56}, 3);
        g2.setFont(new Font("SansSerif", Font.BOLD, 26));
        g2.setColor(new Color(20, 50, 95));
        g2.drawString(penguin.getCollectedFishCount() + " / " + fishes.size(), 722, 56);

        g2.setColor(new Color(45, 130, 225));
        g2.fillRoundRect(btnPause.x, btnPause.y, btnPause.width, btnPause.height, 16, 16);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(btnPause.x + 16, btnPause.y + 14, 8, 26, 4, 4);
        g2.fillRoundRect(btnPause.x + 31, btnPause.y + 14, 8, 26, 4, 4);
    }

    // ================= ฉากที่ 6: หน้าชนะเกม (YOU WIN!) พร้อมปุ่มกดเล่นใหม่อีกรอบ =================
    private void drawWinOverlay(Graphics2D g2) {
        int bx = 300, by = 415, bw = 320, bh = 105;
        g2.setColor(new Color(255, 230, 115));
        g2.fillRoundRect(bx, by, bw, bh, 28, 28);
        g2.setColor(new Color(235, 180, 45));
        g2.setStroke(new BasicStroke(4f));
        g2.drawRoundRect(bx, by, bw, bh, 28, 28);

        g2.setFont(new Font("SansSerif", Font.BOLD, 46));
        g2.setColor(new Color(215, 75, 25));
        g2.drawString("YOU WIN!", bx + 45, by + 58);

        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.setColor(new Color(140, 70, 20));
        g2.drawString("Cleared All 3 Stages!", bx + 78, by + 88);

        // ปุ่มคลิกเล่นใหม่อีกรอบ (PLAY AGAIN) และปุ่มกลับหน้าเมนู (MENU)
        drawStyledButton(g2, btnRestart, "PLAY AGAIN", new Color(55, 145, 235), new Color(25, 100, 195), 20);
        drawStyledButton(g2, btnMenu, "MENU", new Color(250, 190, 55), new Color(220, 145, 20), 22);
    }

    // ================= ฉากที่ 4: หน้าแพ้เกม (YOU LOSS!) พร้อมปุ่มกดเริ่มใหม่อีกรอบ =================
    private void drawLoseOverlay(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 74));
        String title = "YOU LOSS!";
        int tx = 250, ty = 265;
        g2.setColor(Color.WHITE);
        g2.drawString(title, tx - 3, ty - 3);
        g2.drawString(title, tx + 3, ty + 3);
        g2.setColor(new Color(210, 45, 30));
        g2.drawString(title, tx, ty);

        g2.setFont(new Font("SansSerif", Font.BOLD, 28));
        g2.setColor(new Color(15, 45, 95));
        String reason = (timeLeft <= 0) ? "Time's up!" : "No more lives left!";
        g2.drawString(reason, 335, 315);

        penguin.drawCustomPenguin(g2, 375, 330, 175, true, false, false);

        // ปุ่มคลิกเริ่มใหม่อีกรอบ (RESTART) และปุ่มกลับหน้าเมนู (MENU)
        drawStyledButton(g2, btnRestart, "RESTART", new Color(55, 145, 235), new Color(25, 100, 195), 22);
        drawStyledButton(g2, btnMenu, "MENU", new Color(250, 190, 55), new Color(220, 145, 20), 22);
    }

    private void drawPauseOverlay(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        g2.setColor(new Color(70, 140, 230));
        g2.fillRoundRect(260, 230, 400, 210, 30, 30);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(260, 230, 400, 210, 30, 30);

        g2.setFont(new Font("SansSerif", Font.BOLD, 42));
        g2.drawString("PAUSED", 375, 300);

        drawStyledButton(g2, btnResume, "RESUME", new Color(80, 200, 120), new Color(45, 160, 85), 22);
        drawStyledButton(g2, btnRestart, "RESTART", new Color(55, 145, 235), new Color(25, 100, 195), 22);
        drawStyledButton(g2, btnMenu, "MENU", new Color(250, 190, 55), new Color(220, 145, 20), 22);
    }

    private void drawIceBlock(Graphics2D g2, int x, int y, int s) {
        g2.setColor(new Color(75, 155, 225));
        g2.fillRoundRect(x + 3, y + 6, s - 6, s - 8, 14, 14);
        g2.setColor(new Color(165, 220, 255));
        g2.fillRoundRect(x + 5, y + 4, s - 10, s - 18, 12, 12);
        g2.setColor(new Color(225, 248, 255));
        g2.drawRoundRect(x + 7, y + 6, s - 14, s - 22, 10, 10);
    }

    private void drawPineTree(Graphics2D g2, int x, int y, int s) {
        g2.setColor(new Color(95, 60, 35));
        g2.fillRect(x + s / 2 - 6, y + s - 18, 12, 14);
        int[] layersY = {y + s - 14, y + s - 28, y + s - 42};
        for (int i = 0; i < 3; i++) {
            int w = (s - 12) - i * 10;
            g2.setColor(new Color(45, 105, 85));
            g2.fillPolygon(new int[]{x + s / 2, x + s / 2 - w / 2, x + s / 2 + w / 2},
                           new int[]{layersY[i] - 24, layersY[i], layersY[i]}, 3);
            g2.setColor(Color.WHITE);
            g2.fillPolygon(new int[]{x + s / 2, x + s / 2 - w / 3, x + s / 2 + w / 3},
                           new int[]{layersY[i] - 24, layersY[i] - 10, layersY[i] - 10}, 3);
        }
    }

    private void drawRock(Graphics2D g2, int x, int y, int s) {
        g2.setColor(new Color(95, 108, 120));
        g2.fillOval(x + 10, y + 18, s - 20, s - 24);
        g2.setColor(new Color(130, 145, 160));
        g2.fillArc(x + 14, y + 20, s - 28, s - 32, 0, 180);
    }

    private void drawIgloo(Graphics2D g2, int x, int y, int size) {
        g2.setColor(new Color(255, 230, 130, 150));
        g2.fillOval(x - 10, y + size / 2, size + 20, size / 2);

        g2.setColor(new Color(110, 70, 40));
        g2.fillRect(x + size / 2 - 2, y - 22, 4, 30);
        g2.setColor(new Color(215, 40, 35));
        g2.fillPolygon(new int[]{x + size / 2 + 2, x + size / 2 + 38, x + size / 2 + 2},
                       new int[]{y - 22, y - 12, y - 2}, 3);

        g2.setColor(new Color(215, 240, 255));
        g2.fillArc(x, y, size, size, 0, 180);
        g2.setColor(new Color(145, 195, 235));
        g2.setStroke(new BasicStroke(2f));
        g2.drawArc(x, y, size, size, 0, 180);

        int dw = size / 4;
        int dh = size / 3;
        g2.setColor(new Color(135, 85, 45));
        g2.fillRoundRect(x + size / 2 - dw / 2, y + size / 2 - dh + 10, dw, dh, 20, 20);
        g2.setColor(new Color(240, 200, 90));
        g2.fillOval(x + size / 2 + dw / 4 - 4, y + size / 2 - dh / 2 + 10, 6, 6);
    }

    private void drawHudBox(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, y, w, h, 18, 18);
        g2.setColor(new Color(115, 165, 215));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(x, y, w, h, 18, 18);
    }

    private void drawHeart(Graphics2D g2, int x, int y, int size, boolean active) {
        g2.setColor(active ? new Color(220, 45, 45) : new Color(130, 135, 140));
        g2.fillOval(x, y, size / 2 + 2, size / 2 + 2);
        g2.fillOval(x + size / 2 - 2, y, size / 2 + 2, size / 2 + 2);
        g2.fillPolygon(new int[]{x, x + size, x + size / 2},
                       new int[]{y + size / 3, y + size / 3, y + size}, 3);
    }

    private void drawStyledButton(Graphics2D g2, Rectangle r, String text, Color topCol, Color botCol, int fontSize) {
        g2.setColor(botCol);
        g2.fillRoundRect(r.x, r.y + 4, r.width, r.height, 24, 24);
        g2.setColor(topCol);
        g2.fillRoundRect(r.x, r.y, r.width, r.height - 4, 24, 24);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 8, 22, 22);

        g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(text)) / 2;
        int ty = r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent() - 2;
        g2.setColor(fontSize > 25 ? Color.WHITE : new Color(15, 50, 95));
        if (text.equals("RESTART") || text.equals("PLAY AGAIN") || text.equals("MENU") || text.equals("RESUME")) {
            g2.setColor(Color.WHITE);
        }
        g2.drawString(text, tx, ty);
    }

    // ================= Event Handling: กดปุ่มค้างเพื่อเคลื่อนที่แบบลื่นไหลอิสระ =================
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) upPressed = true;
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) downPressed = true;
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) leftPressed = true;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) rightPressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) upPressed = false;
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) downPressed = false;
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) leftPressed = false;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) rightPressed = false;
    }

    // ================= Event Handling: คลิกปุ่มด้วยเมาส์ทั้งหมด =================
    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();

        if (gameState == STATE_MENU) {
            if (btnStart.contains(p)) {
                startNewGame();
                gameState = STATE_PLAYING;
            } else if (btnHowTo.contains(p)) {
                JOptionPane.showMessageDialog(this,
                    "วิธีการเล่น (How to Play):\n"
                    + "1. กดปุ่มลูกศร ขึ้น, ลง, ซ้าย, ขวา (หรือ W, A, S, D) ค้างไว้เพื่อเดินอย่างอิสระ\n"
                    + "2. เกมมีทั้งหมด 3 ด่าน:\n"
                    + "   - ด่านที่ 1: เก็บปลา 1 ตัว | แมวน้ำไล่ตาม 1 ตัว\n"
                    + "   - ด่านที่ 2: เก็บปลา 2 ตัว | แมวน้ำไล่ตาม 2 ตัว + แมวน้ำเฝ้าพื้นที่ 1 ตัว\n"
                    + "   - ด่านที่ 3: เก็บปลา 3 ตัว | แมวน้ำไล่ตาม 3 ตัว + แมวน้ำเฝ้าพื้นที่ 1 ตัว\n"
                    + "3. เก็บปลาให้ครบแล้วเดินเข้าบ้านน้ำแข็ง (Igloo) เมื่อผ่านครบ 3 ด่านจะชนะเกม!",
                    "How to Play - Penguin and Fish", JOptionPane.INFORMATION_MESSAGE);
            } else if (btnExit.contains(p)) {
                System.exit(0);
            }
        } else if (gameState == STATE_PLAYING) {
            if (btnPause.contains(p)) {
                upPressed = downPressed = leftPressed = rightPressed = false;
                gameState = STATE_PAUSED;
            }
        } else if (gameState == STATE_PAUSED) {
            if (btnPause.contains(p) || btnResume.contains(p)) {
                gameState = STATE_PLAYING;
            } else if (btnRestart.contains(p)) {
                startNewGame();
                gameState = STATE_PLAYING;
            } else if (btnMenu.contains(p)) {
                gameState = STATE_MENU;
            }
        } else if (gameState == STATE_WIN || gameState == STATE_LOSE) {
            // กดปุ่ม PLAY AGAIN / RESTART หรือ MENU บนหน้าจอได้ทันที
            if (btnRestart.contains(p)) {
                startNewGame();
                gameState = STATE_PLAYING;
            } else if (btnMenu.contains(p)) {
                gameState = STATE_MENU;
            }
        }
        repaint();
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}