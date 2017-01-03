package agents;

import org.apache.commons.math3.util.Pair;
import org.jscience.mathematics.number.Float64;
import org.jscience.mathematics.vector.Float64Vector;
import simulation.Constants;
import simulation.Parameters;
import utils.Utils;

import java.awt.*;
import java.util.LinkedList;

/**
 * Model of base agent
 */
public abstract class BaseAgent extends BasicSimObject {
    private final Board board;

    double m = 60.0;  // mass of agent
    LinkedList<BaseAgent> neighbors = new LinkedList<>(); // list of neighbors (agents in range of view)
    private LinkedList<Obstacle> obstacles = new LinkedList<>();  // list of obstacles (in range of view)

    Float64Vector velocity = Float64Vector.valueOf(0.8, 0.8); // velocity vector
    Float64Vector basicDesiredVelocity = Float64Vector.valueOf(0.8, 0.8); // velocity vector
    Float64Vector acceleration = Float64Vector.valueOf(0.0, 0.0); // acceleration vector
    Float64Vector pushed = Float64Vector.valueOf(0.0, 0.0); // force used on agent by other agent
    Float64Vector pushToApply = Float64Vector.valueOf(0.0, 0.0); // force used on agent by other agent
    AgentsTypes type; // type of agent
    Color colour = Color.GRAY;  // color
    AgentsStates state; // state of agent
    int itersWithoutMove = 0; // number of iterations without any move

    public AgentsStates getState() {
        return state;
    }

    AgentsTypes getType() {
        return type;
    }

    public Color getColour() {
        return colour;
    }

    Float64Vector getVelocity() {
        return velocity;
    }

    Float64Vector getAcceleration() {
        return acceleration;
    }

    /**
     * Uses other agent's force on this agent.
     *
     * @param force - force (vector) which other uses on this agent
     */
    void bePushed(Float64Vector force) {
        pushed = pushed.plus(force);
    }

    /**
     * Creates agent
     *
     * @param x     - position on board x
     * @param y     - position on board y
     * @param board - simulation board
     */
    BaseAgent(int x, int y, Board board) {
        setActualPositionX(x);
        setActualPositionY(y);
        agent = true;
        this.board = board;
        m += Utils.rand.nextGaussian() * 15;
        if (m > 90)
            m = 90;
        else if (m < 45)
            m = 45;
    }

    /**
     * Find agents and obstacles in range of view
     *
     * @param x         - x coordinate to start looking for, if set to -1 uses position X of agent
     * @param y         - y coordinate to start looking for, if set to -1 uses position Y of agent
     * @param neighbors - list to insert neighbors
     * @param obstacles - list to insert obstacles
     */
    void findNeighbors(int x, int y, LinkedList<BaseAgent> neighbors, LinkedList<Obstacle> obstacles) {
        if (x == -1) {
            x = getNewPositionX();
        }
        if (y == -1) {
            y = getNewPositionY();
        }

        int startX = (x - Parameters.viewRange > 0) ? x - Parameters.viewRange : 0;
        int stopX = (x + Parameters.viewRange < board.getSizeX() - 1) ? x + Parameters.viewRange : board.getSizeX() - 1;
        int startY = (y - Parameters.viewRange > 0) ? y - Parameters.viewRange : 0;
        int stopY = (y + Parameters.viewRange < board.getSizeY() - 1) ? y + Parameters.viewRange : board.getSizeY() - 1;
        for (int i = startX; i <= stopX; i++) {
            for (int j = startY; j <= stopY; j++) {
                if ((Math.pow(x - i, 2) + Math.pow(y - j, 2)) < Parameters.viewRange * Parameters.viewRange) {
                    BasicSimObject temp = board.getField(i, j);
                    if (temp != null) {
                        if (temp.isAgent()) {
                            if (this != temp)
                                neighbors.add((BaseAgent) temp);
                        } else {
                            obstacles.add((Obstacle) temp);
                        }
                    }
                }
            }
        }
    }

    /**
     * Counts the direction where agent should go to achieve desired point
     *
     * @param desiredPoint - point to achieve
     */
    Float64Vector findDesiredDirection(Pair<Integer, Integer> desiredPoint) {
        int desiredX = desiredPoint.getFirst(), desiredY = desiredPoint.getSecond();
        if (desiredX == getActualPositionX() && desiredY == getActualPositionY()) {
            return Float64Vector.valueOf(0, 0);
        }
        return Float64Vector.valueOf(desiredX - getActualPositionX(), desiredY - getActualPositionY()).times(1 / Math.sqrt(Utils.calculateSquareDistancePoints(desiredX, desiredY, getActualPositionX(), getActualPositionY())));
    }

    public void applyPushForces() {
        pushToApply = pushed;
        pushed = Float64Vector.valueOf(0, 0);
    }

    /**
     * Finds closest neighbor after moving with desired velocity (even if motion is impossible)
     *
     * @return closest agent
     */
    BaseAgent findClosestInDesiredDirection() {
        double actualMin = 4 * Parameters.agentRadius * Parameters.agentRadius;
        BaseAgent actualClosest = null;
        for (BaseAgent neighbor : neighbors) {
            double distance = utils.Utils.calculateSquareDistancePoints(getNewPositionX(), getNewPositionY(), neighbor.getActualPositionX(), neighbor.getActualPositionY());
            if (distance < actualMin) {
                actualMin = distance;
                actualClosest = neighbor;
            }
        }
        return actualClosest;
    }

    /**
     * Counts new velocity (intended by agent) basing on neighbors velocities
     *
     * @param oldVelocity - velocity vector from previous iteration
     */
    protected abstract void chooseVelocity(Float64Vector oldVelocity);

    /**
     * Method responsible for molecular dynamics
     * <p/>
     * Implementation of Helbing's model, includes willed velocity, other agents and obstacles in neighbourhood
     *
     * @param oldVelocity - velocity vector from previous iteration
     */
    void socialForces(Float64Vector oldVelocity) {
        acceleration = velocity.minus(oldVelocity).times(1.0 / Constants.timePeriod);
        acceleration = acceleration.plus(detectOtherAgents(oldVelocity).times(1.0 / m));
        acceleration = acceleration.plus(detectObstacles(oldVelocity).times(1.0 / m));
        acceleration = acceleration.plus(pushToApply.times(1.0 / m));
    }

    /**
     * Calculates obstacles' impact on agent
     *
     * @param oldVelocity - velocity vector from previous iteration
     * @return vector representing impact force
     */
    Float64Vector detectObstacles(Float64Vector oldVelocity) {
        int tempX = getActualPositionX(), tempY = getActualPositionY();
        Float64Vector toReturn = Float64Vector.valueOf(0.0, 0.0);
        for (Obstacle act : obstacles) {
            double dist = Math.sqrt(Utils.calculateSquareDistancePoints(tempX, tempY, act.getActualPositionX(), act.getActualPositionY()));
            // interact
            Float64Vector nij = calculateNIJVector(act);
            Float64Vector interactForce = calculateInteractForceO(nij, dist);
            //body
            if (Parameters.agentRadius > dist) {
                Float64Vector bodyForce = calculateBodyForceO(nij, dist);
                Float64Vector slidForce = calculateSlidForceO(nij, oldVelocity, dist);
                toReturn = toReturn.plus(interactForce).plus(bodyForce).plus(slidForce);
            } else {
                toReturn = toReturn.plus(interactForce);
            }
        }
        return toReturn;
    }

    /**
     * Calculates agents' impact on agent
     *
     * @param oldVelocity - velocity vector from previous iteration
     * @return vector representing impact force
     */
    Float64Vector detectOtherAgents(Float64Vector oldVelocity) {
        Float64Vector toReturn = Float64Vector.valueOf(0.0, 0.0);
        for (BaseAgent act : neighbors) {
            double dist = Math.sqrt(Utils.calculateSquareDistancePoints(getActualPositionX(), getActualPositionY(), act.getActualPositionX(), act.getActualPositionY()));
            // interact
            Float64Vector nij = calculateNIJVector(act);
            Float64Vector interactForce = calculateInteractForce(nij, dist);
            //body
            if (Parameters.agentRadius + Parameters.agentRadius > dist) {
                Float64Vector bodyForce = calculateBodyForce(nij, dist);
                Float64Vector slidForce = calculateSlidForce(nij, act, oldVelocity, dist);
                toReturn = toReturn.plus(interactForce).plus(bodyForce).plus(slidForce);
            } else {
                toReturn = toReturn.plus(interactForce);
            }
        }
        return toReturn;
    }

    /**
     * From Helbing's model
     *
     * @param obj - simulation object
     * @return nij vector
     */
    Float64Vector calculateNIJVector(BasicSimObject obj) {
        Float64Vector nij = Float64Vector.valueOf(getActualPositionX() - obj.getActualPositionX(), getActualPositionY() - obj.getActualPositionY());
        return nij.times(1.0 / nij.normValue());
    }

    /**
     * From Helbing's model
     *
     * @param nij  - nij vector
     * @param dist - distance between considered agents
     * @return internal force vector
     */
    Float64Vector calculateInteractForce(Float64Vector nij, double dist) {
        return nij.times(Constants.a).times(Math.exp((Parameters.agentRadius + Parameters.agentRadius - dist) / Constants.b));
    }

    /**
     * From Helbing's model
     *
     * @param nij  - nij vector
     * @param dist - distance between considered agents
     * @return body force vector
     */
    Float64Vector calculateBodyForce(Float64Vector nij, double dist) {
        return nij.times(Constants.k).times(Parameters.agentRadius + Parameters.agentRadius - dist);
    }

    /**
     * From Helbing's model
     *
     * @param nij         - nij vector
     * @param agent       - considered second agent
     * @param oldVelocity - old velocity of current agent
     * @param dist        - distance between considered agents
     * @return slid force vector
     */
    Float64Vector calculateSlidForce(Float64Vector nij, BaseAgent agent, Float64Vector oldVelocity, double dist) {
        return Float64Vector.valueOf(-1 * nij.get(1).doubleValue(), nij.get(0).doubleValue()).times(agent.getVelocity().normValue() - oldVelocity.normValue()).times(Constants.k2).times(Parameters.agentRadius + Parameters.agentRadius - dist);
    }

    /**
     * From Helbing's model for obstacles
     *
     * @param nij  - nij vector
     * @param dist - distance between considered agent and obstacle
     * @return interact force vector
     */
    Float64Vector calculateInteractForceO(Float64Vector nij, double dist) {
        return nij.times(Constants.aw).times(Math.exp((Parameters.agentRadius - dist) / Constants.bw));
    }

    /**
     * From Helbing's model for obstacles
     *
     * @param nij  - nij vector
     * @param dist - distance between considered agent and obstacle
     * @return body force vector
     */
    Float64Vector calculateBodyForceO(Float64Vector nij, double dist) {
        return nij.times(Constants.k).times(Parameters.agentRadius - dist);
    }

    /**
     * From Helbing's model for obstacles
     *
     * @param nij         - nij vector
     * @param oldVelocity -  old velocity of current agent
     * @param dist        - distance between considered agent and obstacle
     * @return slid force vector
     */
    Float64Vector calculateSlidForceO(Float64Vector nij, Float64Vector oldVelocity, double dist) {
        return Float64Vector.valueOf(-1 * nij.get(1).doubleValue(), nij.get(0).doubleValue()).times(-oldVelocity.normValue()).times(Constants.k2).times(Parameters.agentRadius - dist);
    }

    /**
     * Calculates new position of agent (based on velocity)
     *
     * @return new X coordinate
     */
    private int getNewPositionX() {
        return getActualPositionX() + (int) (Math.round(velocity.get(0).doubleValue() * Constants.timePeriod));
    }

    /**
     * Calculates new position of agent (based on velocity)
     *
     * @return new Y coordinate
     */
    private int getNewPositionY() {
        return getActualPositionY() + (int) (Math.round(velocity.get(1).doubleValue() * Constants.timePeriod));
    }

    /**
     * Function move agent on the board
     * <p/>
     * Firstly finds obstacles and neighbors, chooses new desired velocity and includes social forces (it's updating); at the end changes position of agent on board
     */
    public void move() {
        updateState();
        changePosition();
    }

    /**
     * Adds fluctuations to given vector (every dimensions)
     *
     * @param vector           - input vector
     * @param fluctuationsSize - size of fluctuations
     * @return vector with added fluctuations
     */
    Float64Vector addFluctuations(Float64Vector vector, double fluctuationsSize) {
        LinkedList<Float64> fluctuations = new LinkedList<>();
        for (int i = 0; i < vector.getDimension(); i++) {
            fluctuations.add(Float64.valueOf((Utils.rand.nextBoolean()) ? fluctuationsSize : -fluctuationsSize));
        }
        return vector.plus(Float64Vector.valueOf(fluctuations));

    }

    /**
     * Updates state of agent
     */
    public void updateState() {
        Float64Vector oldVelocity = getVelocity().copy();
        neighbors = new LinkedList<>();
        obstacles = new LinkedList<>();
        findNeighbors(-1, -1, neighbors, obstacles);
        chooseVelocity(oldVelocity);
        socialForces(oldVelocity);
        velocity = oldVelocity.plus(getAcceleration().times(Constants.timePeriod));
        switch (this.getType()) {
            case PASSIVE: {
                velocity = normalizeVector(velocity, Parameters.maxVelocityPassive);
                break;
            }
            case MODERATEACTIVE: {
                velocity = normalizeVector(velocity, Parameters.maxVelocityModerate);
                break;
            }
            case TROUBLEMAKER: {
                velocity = normalizeVector(velocity, Parameters.maxVelocityTrouble);
                break;
            }
            case POLICEMAN: {
                velocity = normalizeVector(velocity, Parameters.maxVelocityTrouble);
                break;
            }
        }
    }

    /**
     * Changes position of agent to new one, only if the new one is free
     */
    public void changePosition() {
        int tempX = getNewPositionX();
        int tempY = getNewPositionY();
        if (tempX != getActualPositionX() || tempY != getActualPositionY()) {
            if (board.getField(tempX, tempY) == null) {
                board.updateField(new Pair<>(getActualPositionX(), getActualPositionY()), null);
                setActualPositionX(tempX);
                setActualPositionY(tempY);
                board.updateField(new Pair<>(getActualPositionX(), getActualPositionY()), this);
                itersWithoutMove = 0;
            } else {
                itersWithoutMove++;
            }
        } else {
            itersWithoutMove++;
        }
    }

    /**
     * Normalizes vector to given maxValue
     *
     * @param vec      - input vector
     * @param maxValue - max value of vector
     * @return normalized vector
     */
    Float64Vector normalizeVector(Float64Vector vec, double maxValue) {
        if (vec.normValue() > maxValue) {
            return vec.times(maxValue / vec.normValue());
        }
        return vec;
    }
}
