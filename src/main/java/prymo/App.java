

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class App extends JFrame {

    public App() {
        setTitle("Snake Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new GamePanel());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::new);
    }
}

class GamePanel extends JPanel implements ActionListener {

    /* ================= CONFIG ================= */
    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS =
            (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);

    static final int DELAY = 120; // 🔒 CONSTANT SPEED

    /* ================= STATE ================= */
    enum State { START, RUNNING, PAUSED, GAME_OVER }
    State gameState = State.START;

    /* ================= DATA ================= */
    final int[] x = new int[GAME_UNITS];
    final int[] y = new int[GAME_UNITS];

    int bodyParts;
    int applesEaten;
    int highScore;

    int appleX;
    int appleY;

    char direction;
    boolean wallsEnabled = true;

    Timer timer;
    Random random = new Random();

    /* ================= INIT ================= */
    GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.black);
        setFocusable(true);
        addKeyListener(new KeyHandler());
    }

    /* ================= GAME FLOW ================= */
    void startGame() {
        bodyParts = 6;
        applesEaten = 0;
        direction = 'R';

        for (int i = 0; i < bodyParts; i++) {
            x[i] = 100 - i * UNIT_SIZE;
            y[i] = 100;
        }

        newApple();
        gameState = State.RUNNING;

        timer = new Timer(DELAY, this);
        timer.start();
    }

    void togglePause() {
        if (gameState == State.RUNNING) {
            timer.stop();
            gameState = State.PAUSED;
        } else if (gameState == State.PAUSED) {
            timer.start();
            gameState = State.RUNNING;
        }
    }

    void gameOver() {
        timer.stop();
        highScore = Math.max(highScore, applesEaten);
        gameState = State.GAME_OVER;
    }

    /* ================= LOGIC ================= */
    void newApple() {
        appleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
        appleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
    }

    void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U' -> y[0] -= UNIT_SIZE;
            case 'D' -> y[0] += UNIT_SIZE;
            case 'L' -> x[0] -= UNIT_SIZE;
            case 'R' -> x[0] += UNIT_SIZE;
        }

        if (!wallsEnabled) wrapAround();
    }

    void wrapAround() {
        if (x[0] < 0) x[0] = SCREEN_WIDTH - UNIT_SIZE;
        if (x[0] >= SCREEN_WIDTH) x[0] = 0;
        if (y[0] < 0) y[0] = SCREEN_HEIGHT - UNIT_SIZE;
        if (y[0] >= SCREEN_HEIGHT) y[0] = 0;
    }

    void checkApple() {
        if (x[0] == appleX && y[0] == appleY) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }

    void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                gameOver();
                return;
            }
        }

        if (wallsEnabled &&
                (x[0] < 0 || x[0] >= SCREEN_WIDTH ||
                 y[0] < 0 || y[0] >= SCREEN_HEIGHT)) {
            gameOver();
        }
    }

    /* ================= DRAW ================= */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        switch (gameState) {
            case START -> drawStart(g);
            case RUNNING -> drawGame(g);
            case PAUSED -> drawPaused(g);
            case GAME_OVER -> drawGameOver(g);
        }
    }

    void drawGame(Graphics g) {
        g.setColor(Color.red);
        g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

        for (int i = 0; i < bodyParts; i++) {
            g.setColor(i == 0 ? Color.green : new Color(45, 180, 0));
            g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
        }

        g.setColor(Color.white);
        g.setFont(new Font("Consolas", Font.BOLD, 16));
        g.drawString("Score: " + applesEaten, 10, 20);
        g.drawString("High: " + highScore, 10, 40);
        g.drawString("Walls: " + (wallsEnabled ? "ON" : "OFF"), 10, 60);
    }

    void centerText(Graphics g, String text, int y, int size) {
        g.setFont(new Font("Ink Free", Font.BOLD, size));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text,
                (SCREEN_WIDTH - fm.stringWidth(text)) / 2, y);
    }

    void drawStart(Graphics g) {
        g.setColor(Color.white);
        centerText(g, "SNAKE GAME", 260, 40);
        centerText(g, "Press SPACE to Start", 310, 20);
    }

    void drawPaused(Graphics g) {
        drawGame(g);
        g.setColor(Color.yellow);
        centerText(g, "PAUSED", 300, 40);
    }

    void drawGameOver(Graphics g) {
        g.setColor(Color.red);
        centerText(g, "GAME OVER", 260, 40);
        g.setColor(Color.white);
        centerText(g, "Score: " + applesEaten, 310, 20);
        centerText(g, "Press SPACE to Restart", 350, 18);
    }

    /* ================= TIMER ================= */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == State.RUNNING) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    /* ================= INPUT ================= */
    class KeyHandler extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {

            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                if (gameState == State.START || gameState == State.GAME_OVER) {
                    startGame();
                } else {
                    togglePause();
                }
            }

            switch (e.getKeyCode()) {
                case KeyEvent.VK_W, KeyEvent.VK_UP ->
                        { if (direction != 'D') direction = 'U'; }
                case KeyEvent.VK_S, KeyEvent.VK_DOWN ->
                        { if (direction != 'U') direction = 'D'; }
                case KeyEvent.VK_A, KeyEvent.VK_LEFT ->
                        { if (direction != 'R') direction = 'L'; }
                case KeyEvent.VK_D, KeyEvent.VK_RIGHT ->
                        { if (direction != 'L') direction = 'R'; }
                case KeyEvent.VK_T ->
                        wallsEnabled = !wallsEnabled;
            }
        }
    }
}
