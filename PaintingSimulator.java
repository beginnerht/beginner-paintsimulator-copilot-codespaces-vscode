import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * PaintingSimulator - A simple raster graphics painting application.
 *
 * A square brush is moved around the canvas using the arrow keys.
 * Every position the brush visits is painted onto the canvas, like
 * dragging a paintbrush across paper.
 *
 * Controls:
 *   Arrow keys       - Move the brush and paint
 *   1-7              - Change brush color
 *   +/-              - Increase / decrease brush size
 *   C                - Clear the canvas
 *   H                - Show / hide this help overlay
 */
public class PaintingSimulator extends JFrame {

    // --------------- constants ---------------
    private static final int CANVAS_W   = 800;
    private static final int CANVAS_H   = 600;
    private static final int STEP           = 4;   // pixels per key press
    private static final int MIN_BRUSH_SIZE = 4;
    private static final int MAX_BRUSH_SIZE = 80;

    private static final Color[] PALETTE = {
        Color.BLACK,
        new Color(220,  50,  50),   // red
        new Color( 50, 160,  50),   // green
        new Color( 50,  50, 220),   // blue
        new Color(220, 160,   0),   // yellow
        new Color(150,  50, 200),   // purple
        Color.WHITE
    };

    private static final String[] COLOR_NAMES = {
        "Black", "Red", "Green", "Blue", "Yellow", "Purple", "White"
    };

    // --------------- state ---------------
    private final BufferedImage canvas;
    private final Graphics2D    canvasG;

    private int brushX;
    private int brushY;
    private int brushSize  = 20;
    private int colorIndex = 0;

    private boolean showHelp = true;

    // --------------- constructor ---------------
    public PaintingSimulator() {
        super("Painting Simulator");

        canvas  = new BufferedImage(CANVAS_W, CANVAS_H, BufferedImage.TYPE_INT_RGB);
        canvasG = canvas.createGraphics();
        clearCanvas();

        brushX = (CANVAS_W - brushSize) / 2;
        brushY = (CANVAS_H - brushSize) / 2;

        // Paint the initial brush position
        paintBrush();

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(canvas, 0, 0, null);
                drawBrushOutline(g);
                if (showHelp) drawHelp(g);
                drawStatusBar(g);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(CANVAS_W, CANVAS_H);
            }
        };

        panel.setFocusable(true);
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKey(e.getKeyCode());
                panel.repaint();
            }
        });

        add(panel);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Make sure the panel gets keyboard focus
        panel.requestFocusInWindow();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                panel.requestFocusInWindow();
            }
        });
    }

    // --------------- input handling ---------------
    private void handleKey(int key) {
        int newX = brushX;
        int newY = brushY;

        switch (key) {
            case KeyEvent.VK_LEFT  -> newX -= STEP;
            case KeyEvent.VK_RIGHT -> newX += STEP;
            case KeyEvent.VK_UP    -> newY -= STEP;
            case KeyEvent.VK_DOWN  -> newY += STEP;
            case KeyEvent.VK_C     -> clearCanvas();
            case KeyEvent.VK_H     -> showHelp = !showHelp;
            case KeyEvent.VK_PLUS, KeyEvent.VK_EQUALS,
                 KeyEvent.VK_ADD   -> brushSize = Math.min(brushSize + 4, MAX_BRUSH_SIZE);
            case KeyEvent.VK_MINUS,
                 KeyEvent.VK_SUBTRACT -> brushSize = Math.max(brushSize - 4, MIN_BRUSH_SIZE);
            // number keys 1-7 select color
            default -> {
                if (key >= KeyEvent.VK_1 && key <= KeyEvent.VK_7) {
                    colorIndex = key - KeyEvent.VK_1;
                }
            }
        }

        // Clamp to canvas boundaries
        newX = Math.max(0, Math.min(newX, CANVAS_W - brushSize));
        newY = Math.max(0, Math.min(newY, CANVAS_H - brushSize));

        // Only paint when the brush position has actually changed
        if (newX != brushX || newY != brushY) {
            brushX = newX;
            brushY = newY;
            paintBrush();
        }
    }

    // --------------- drawing helpers ---------------
    /** Fills the current brush rectangle on the persistent canvas. */
    private void paintBrush() {
        canvasG.setColor(PALETTE[colorIndex]);
        canvasG.fillRect(brushX, brushY, brushSize, brushSize);
    }

    /** White background */
    private void clearCanvas() {
        canvasG.setColor(Color.WHITE);
        canvasG.fillRect(0, 0, CANVAS_W, CANVAS_H);
    }

    /** Draws a semi-transparent outline around the brush (not painted). */
    private void drawBrushOutline(Graphics g) {
        g.setColor(new Color(0, 0, 0, 120));
        g.drawRect(brushX, brushY, brushSize - 1, brushSize - 1);
        // inner highlight
        g.setColor(new Color(255, 255, 255, 80));
        g.drawRect(brushX + 1, brushY + 1, brushSize - 3, brushSize - 3);
    }

    /** Draws a small status bar at the bottom of the panel. */
    private void drawStatusBar(Graphics g) {
        int barH = 22;
        g.setColor(new Color(40, 40, 40, 200));
        g.fillRect(0, CANVAS_H - barH, CANVAS_W, barH);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));

        String status = String.format(
            "Color: %s (1-7)   Size: %d (+/-)   [C] Clear   [H] Help",
            COLOR_NAMES[colorIndex], brushSize);
        g.drawString(status, 8, CANVAS_H - 6);

        // Color swatch
        g.setColor(PALETTE[colorIndex]);
        g.fillRect(CANVAS_W - 30, CANVAS_H - barH + 3, 20, barH - 6);
        g.setColor(Color.GRAY);
        g.drawRect(CANVAS_W - 30, CANVAS_H - barH + 3, 20, barH - 6);
    }

    /** Draws a translucent help overlay in the centre of the canvas. */
    private void drawHelp(Graphics g) {
        String[] lines = {
            "  PAINTING SIMULATOR  ",
            "",
            "  Arrow keys  - Move brush & paint",
            "  1 - 7       - Change color",
            "  + / -       - Brush size",
            "  C           - Clear canvas",
            "  H           - Toggle this help",
        };

        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        int lineH  = fm.getHeight() + 4;
        int boxW   = 280;
        int boxH   = lines.length * lineH + 20;
        int boxX   = (CANVAS_W - boxW) / 2;
        int boxY   = (CANVAS_H - boxH) / 2;

        // Background
        g.setColor(new Color(20, 20, 20, 200));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 12, 12);
        g.setColor(new Color(180, 180, 180, 200));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 12, 12);

        // Text
        for (int i = 0; i < lines.length; i++) {
            if (i == 0) {
                g.setColor(new Color(255, 220, 80));
            } else {
                g.setColor(Color.WHITE);
            }
            g.drawString(lines[i], boxX + 10, boxY + 18 + i * lineH);
        }
    }

    // --------------- entry point ---------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new PaintingSimulator().setVisible(true);
        });
    }
}
