package graphics;

import agents.BaseAgent;
import controller.SimController;
import org.apache.commons.math3.util.Pair;
import simulation.Parameters;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

public class DraggablePanel extends ImagePanel implements MouseMotionListener, MouseListener {

    private static final long serialVersionUID = 1L;

    private Rectangle selection; // selection rectangle
    private Point anchor;
    private final SimController controller;
    private boolean isDefaultCursor = true;
    private boolean isDraggable = true;

    public DraggablePanel(String img, int x, int y, int width, int height,
                          SimController controller) {
        super(img, x, y, width, height);
        addMouseListener(this);
        addMouseMotionListener(this);
        this.controller = controller;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (selection != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(225, 225, 255, 128));
            g2d.fill(selection);
            g2d.setColor(Color.white);
            g2d.draw(selection);
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        LinkedList<BaseAgent> agents = controller.getBoard().getAgents();
        LinkedList<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> obstaclesRect = controller
                .getBoard().getObstaclesRect();
        Ellipse2D agentCircle = new Ellipse2D.Double();
        Rectangle2D obstacle = new Rectangle2D.Double();
        for (BaseAgent act : agents) {
            agentCircle.setFrameFromCenter(act.getActualPositionX(),
                    act.getActualPositionY(), act.getActualPositionX()
                            + Parameters.agentRadius, act.getActualPositionY()
                            + Parameters.agentRadius);
            g2.setPaint(act.getColour());
            g2.fill(agentCircle);
        }
        for (Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> act : obstaclesRect) {
            obstacle.setFrame(act.getFirst().getFirst(), act.getFirst()
                    .getSecond(), act.getSecond().getFirst()
                    - act.getFirst().getFirst(), act.getSecond().getSecond()
                    - act.getFirst().getSecond());
            g2.setPaint(Color.BLACK);
            g2.fill(obstacle);
        }
        agentCircle.setFrameFromCenter(Parameters.desiredPoint.getFirst(), Parameters.desiredPoint.getSecond(), Parameters.desiredPoint.getFirst() + 2, Parameters.desiredPoint.getSecond() + 2);
        g2.setPaint(Color.WHITE);
        g2.fill(agentCircle);
        this.validate();
    }

    public void changeCursor() {
        if (!controller.isRunning()) {
            if (isDefaultCursor) {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                isDraggable = false;
                isDefaultCursor = false;
            } else {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                isDraggable = true;
                isDefaultCursor = true;
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        if (!controller.isRunning()) {
            if (isDraggable) {
                anchor = e.getPoint();
                selection = new Rectangle(anchor);
            } else {
                anchor = e.getPoint();
                Parameters.desiredPoint = new Pair<>(anchor.x, anchor.y);
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                isDraggable = true;
                isDefaultCursor = true;
            }
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (!controller.isRunning()) {
            if (isDraggable) {
                selection.setBounds(Math.min(anchor.x, e.getX()),
                        Math.min(anchor.y, e.getY()),
                        Math.abs(e.getX() - anchor.x),
                        Math.abs(e.getY() - anchor.y));
                repaint();
            }
        }

    }

    public void mouseReleased(MouseEvent e) {
        if (!controller.isRunning()) {
            if (isDraggable) {
                controller.generateObjects(new Pair<>((int) anchor.getX(),
                        (int) anchor.getY()), new Pair<>(e.getX(), e.getY()));
                selection = null;
                repaint();
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mouseMoved(MouseEvent arg0) {
    }

}
