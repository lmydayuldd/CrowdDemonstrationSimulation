package agents;

import org.apache.commons.math3.util.Pair;
import simulation.Parameters;

import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * Representation of board
 */
public class Board {
    private BasicSimObject[][] elements; // board of elements
    private int sizeX, sizeY;  // sizes of board
    private LinkedList<Obstacle> obstacles; // list of obstacles
    private LinkedList<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> obstaclesRect; // list of pairs of points representing rectangles filled with obstacles
    private LinkedList<BaseAgent> agents; // list of agents
    private LinkedList<Policeman> policemen; // list of policemen

    public LinkedHashSet<Pair<Integer, Integer>> getAllAvailableFields() {
        return allAvailableFields;
    }

    private LinkedHashSet<Pair<Integer, Integer>> allAvailableFields; // list of points where new agent can be created (applicable only before simulations start)

    public LinkedList<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> getObstaclesRect() {
        return obstaclesRect;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public LinkedList<Obstacle> getObstacles() {
        return obstacles;
    }

    public LinkedList<BaseAgent> getAgents() {
        return agents;
    }

    public LinkedList<Policeman> getPolicemen() {
        return policemen;
    }

    /**
     * Default constructor, doing nothing
     */
    public Board() {
    }

    /**
     * Initializes board
     *
     * @param length - length of board
     * @param height - height of board
     */
    public void initialize(int length, int height) {
        elements = new BasicSimObject[length][height];
        agents = new LinkedList<>();
        obstacles = new LinkedList<>();
        obstaclesRect = new LinkedList<>();
        policemen = new LinkedList<>();
        sizeX = length;
        sizeY = height;
        allAvailableFields = getAvailableFields();
        setBordersAsObstacles();

    }

    /**
     * Creates obstacles on borders of board
     */
    private void setBordersAsObstacles() {
        SimulationObjectsGenerator.generateObstaclesAndUpdateBoard(this, new Pair<>(0, 0), new Pair<>(getSizeX() - 1, Parameters.agentRadius));
        SimulationObjectsGenerator.generateObstaclesAndUpdateBoard(this, new Pair<>(0, 0), new Pair<>(Parameters.agentRadius, getSizeY() - 1));
        SimulationObjectsGenerator.generateObstaclesAndUpdateBoard(this, new Pair<>(getSizeX() - Parameters.agentRadius - 1, 0), new Pair<>(getSizeX() - 1, getSizeY() - 1));
        SimulationObjectsGenerator.generateObstaclesAndUpdateBoard(this, new Pair<>(0, getSizeY() - Parameters.agentRadius - 1), new Pair<>(getSizeX() - 1, getSizeY() - 1));
    }

    /**
     * Updates field of board with given element
     *
     * @param position - position on board to update
     * @param element  - element to insert
     */
    public void updateField(Pair<Integer, Integer> position, BasicSimObject element) {
        elements[position.getFirst()][position.getSecond()] = element;
    }

    /**
     * @param x - coordinate x
     * @param y - coordinate y
     * @return element on specified position on board
     */
    public BasicSimObject getField(int x, int y) {
        return elements[x][y];
    }

    /**
     * Finds all empty fields on board
     *
     * @return set of positions of empty fields
     */
    LinkedHashSet<Pair<Integer, Integer>> getAvailableFields() {
        LinkedHashSet<Pair<Integer, Integer>> availableFields = new LinkedHashSet<>(getSizeX() * getSizeY());
        int starti = 0, startj = 0, stopi = getSizeX(), stopj = getSizeY();
        for (int i = starti; i < stopi; i++) {
            for (int j = startj; j < stopj; j++) {
                if (elements[i][j] == null) {
                    availableFields.add(new Pair<>(i, j));
                }
            }
        }
        return availableFields;
    }

    /**
     * Finds all empty (able to use) fields on board in given area
     * Empty is not the best word, because some empty fields are excluded when they are inside of agents radius (or area near)
     *
     * @param constraintLU - left upper corner of rectangle to search for fields
     * @param constraintRD - right bottom
     * @param agents       - if true, fields in vicinity of existing objects are excluded
     * @return set of positions of available for use fields
     */
    public LinkedHashSet<Pair<Integer, Integer>> getAvailableFieldsWithConstraints(Pair<Integer, Integer> constraintLU, Pair<Integer, Integer> constraintRD, boolean agents) {
        LinkedHashSet<Pair<Integer, Integer>> availableFields = new LinkedHashSet<>(getSizeX() + getSizeY());

        int x1 = constraintLU.getFirst(),
                x2 = constraintRD.getFirst(),
                y1 = constraintLU.getSecond(),
                y2 = constraintRD.getSecond();

        if (agents) {
            x1 += Parameters.agentRadius;
            x2 -= Parameters.agentRadius;
            y1 += Parameters.agentRadius;
            y2 -= Parameters.agentRadius;

            excludeObstaclesFromAvailable();
        }
        for (Pair<Integer, Integer> temp : allAvailableFields) {
            if (temp.getFirst() > x1 && temp.getFirst() < x2 && temp.getSecond() > y1 && temp.getSecond() < y2) {
                availableFields.add(temp);
            }
        }
        return availableFields;
    }

    /**
     * Removes points (top - radius, left - radius, right + radius, down + radius) near obstacles rectangles
     * <p/>
     * Radius is agentRadius
     */
    private void excludeObstaclesFromAvailable() {
        for (Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> temp : obstaclesRect) {
            excludeObstacleFromAvailable(temp);
        }
    }

    /**
     * Removes (see excludeObstaclesFromAvailable)
     *
     * @param obstacleRect - pair of points representing rectangle filed with obstacles
     */
    private void excludeObstacleFromAvailable(Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> obstacleRect) {
        int x1 = obstacleRect.getFirst().getFirst(),
                x2 = obstacleRect.getSecond().getFirst(),
                y1 = obstacleRect.getFirst().getSecond(),
                y2 = obstacleRect.getSecond().getSecond();
        x1 = (x1 - Parameters.agentRadius > 0) ? x1 - Parameters.agentRadius : 0;
        y1 = (y1 - Parameters.agentRadius > 0) ? y1 - Parameters.agentRadius : 0;
        x2 = (x2 + Parameters.agentRadius < getSizeX()) ? x2 + Parameters.agentRadius : getSizeX() - 1;
        y2 = (y2 + Parameters.agentRadius < getSizeY()) ? y2 + Parameters.agentRadius : getSizeY() - 1;

        for (int i = x1; i <= x2; i++) {
            for (int j = y1; j <= y1 + Parameters.agentRadius; j++) {
                allAvailableFields.remove(new Pair<>(i, j));
            }
            for (int j = y2 - Parameters.agentRadius; j <= y2; j++) {
                allAvailableFields.remove(new Pair<>(i, j));
            }
        }
        for (int j = obstacleRect.getFirst().getSecond(); j <= obstacleRect.getSecond().getSecond(); j++) {
            for (int i = x1; i <= x1 + Parameters.agentRadius; i++) {
                allAvailableFields.remove(new Pair<>(i, j));
            }
            for (int i = x2 - Parameters.agentRadius; i <= x2; i++) {
                allAvailableFields.remove(new Pair<>(i, j));
            }
        }
    }

    /**
     * Calculates centre of gravity of existing agents
     *
     * @return point representing centre of gravity
     */
    public Pair<Integer, Integer> getCenterOfGravity() {
        int counter = 0, xsum = 0, ysum = 0;
        for (int i = 0; i < getSizeX(); i++) {
            for (int j = 0; j < getSizeY(); j++) {
                if (elements[i][j] != null && elements[i][j].isAgent()) {
                    xsum += i;
                    ysum += j;
                    counter++;
                }
            }
        }
        return new Pair<>(xsum / counter, ysum / counter);
    }
}

