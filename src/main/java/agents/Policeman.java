package agents;

import org.apache.commons.math3.util.Pair;
import org.jscience.mathematics.vector.Float64Vector;
import simulation.Constants;
import simulation.Parameters;
import utils.Utils;

import java.awt.*;
import java.util.LinkedList;

/**
 * Representation of policeman
 */
public class Policeman extends BaseAgent {

    private double maxReactionForce = 0.0; // maximal value of reaction force
    private final Pair<Integer, Integer> desiredPoint; // position to hold
    private int wasPushedItersAgo = -1; // how many iterations ago was policeman pushed
    private LinkedList<Policeman> pushing = new LinkedList<>(); // list of policemen which (indirectly) pushes current policeman

    /**
     * Creates policeman agent
     *
     * @param x     - position on board x
     * @param y     - position on board y
     * @param board - simulation board
     */
    public Policeman(int x, int y, Board board) {
        super(x, y, board);
        type = AgentsTypes.POLICEMAN;
        colour = Color.BLUE;
        velocity = Float64Vector.valueOf(0, 0);
        basicDesiredVelocity = Float64Vector.valueOf(0, 0);
        maxReactionForce = m * (Parameters.maxVelocityTrouble - 0) / Constants.timePeriod;
        desiredPoint = new Pair<>(x, y);
        state = AgentsStates.NONE;
        m = 80;
    }

    @Override
    public void bePushed(Float64Vector force) {
        pushed = pushed.plus(force);
        state = AgentsStates.IS_PUSHED;
        wasPushedItersAgo = 0;
    }

    /**
     * Pushes agent with force originating from other policeman
     *
     * @param force - push force
     */
    void bePushedByPolice(Float64Vector force) {
        pushed = pushed.plus(force);
        wasPushedItersAgo = 0;
    }

    /**
     * Assigns forces used on agent in previous iteration to pushedToApply vector, which will be considered in current iteration
     */
    public void applyPushForces() {
        pushToApply = pushed;
        pushed = Float64Vector.valueOf(0, 0);
        wasPushedItersAgo += 1;
        pushing = new LinkedList<>();
        state = AgentsStates.NONE;
    }

    /**
     * Adds policeman to list of pushing
     *
     * @param pushing - pushing policemen
     */
    void pushedBy(LinkedList<Policeman> pushing) {
        this.pushing.addAll(pushing);
    }

    /**
     * Important function
     * Considers forces acting on policeman, if resultant force is bigger than maximum reaction force,
     * the part of force which cannot be "reacted" by policeman is given to next one (closest in direction of force, if any).
     * <p/>
     * If there's no policeman to "give" him force, it is distributed (evenly) to every of policemen in pushing list
     * (pushing -> every policeman who was given and gave force in chain from pushed by crowd to actual)
     *
     * @return policeman who was given force by current one or null
     */
    public Policeman considerForces() {
        double pushForceValue = pushed.normValue();
        Float64Vector reaction;
        if (pushForceValue > maxReactionForce) {
            reaction = Float64Vector.valueOf(pushed.get(0).doubleValue() * maxReactionForce / pushForceValue, pushed.get(1).doubleValue() * maxReactionForce / pushForceValue).times(-1);
            Policeman closestPoliceman = findClosestInForceDirection(pushed);
            if (closestPoliceman != null) {
                closestPoliceman.bePushedByPolice(pushed.plus(reaction));
                pushed = reaction.times(-1);
                pushing.add(this);
                closestPoliceman.pushedBy(pushing);
                return closestPoliceman;
            } else {
                for (Policeman pol : pushing) {
                    pol.bePushedByPolice(pushed.plus(reaction).times(1.0 / (pushing.size() + 1)));
                }
                pushed = pushed.minus(pushed.plus(reaction).times(pushing.size() / (pushing.size() + 1)));
            }
        }
        return null;
    }

    /**
     * Finds closest policeman in force direction
     *
     * @param force - given force
     * @return policeman (may be null, if not exists)
     */
    Policeman findClosestInForceDirection(Float64Vector force) {
        double actualMin = 4 * Parameters.agentRadius * Parameters.agentRadius;
        Policeman actualClosest = null;
        Float64Vector forceNew = normalizeVector(force, 2);
        for (BaseAgent neighbor : neighbors) {
            if (neighbor.getType() == AgentsTypes.POLICEMAN) {
                double distance = utils.Utils.calculateSquareDistancePoints(getActualPositionX() + forceNew.get(0).doubleValue() * Constants.timePeriod, getActualPositionY() + forceNew.get(1).doubleValue() * Constants.timePeriod, neighbor.getActualPositionX(), neighbor.getActualPositionY());
                if (distance < actualMin) {
                    actualMin = distance;
                    actualClosest = (Policeman) neighbor;
                }
            }
        }
        return actualClosest;
    }

    /**
     * Reaction to push force
     *
     * @return vector of reaction force
     */
    private Float64Vector react() {
        Float64Vector reaction;
        double pushForceValue = pushToApply.normValue();
        if (pushForceValue > maxReactionForce) {
            reaction = Float64Vector.valueOf(pushToApply.get(0).doubleValue() * maxReactionForce / pushForceValue, pushToApply.get(1).doubleValue() * maxReactionForce / pushForceValue).times(-1);
            if (wasPushedItersAgo >= 0) {
                if (wasPushedItersAgo < 3) {
                    pushed = pushed.plus(pushToApply.plus(reaction).times(0.8));
                }
            }
        } else {
            reaction = pushToApply.times(-1);
        }
        return reaction;
    }


    /**
     * Checks if agent is on his desired position
     *
     * @return true if so
     */
    private boolean isNotOnDesiredPosition() {
        return !getActualPositionX().equals(desiredPoint.getFirst()) || !getActualPositionY().equals(desiredPoint.getSecond());
    }

    /**
     * Calculates agents' impact on agent (considers everyone except of policemen)
     *
     * @param oldVelocity - velocity vector from previous iteration
     * @param special     - if true, interact force is not calculated
     * @return vector representing impact force
     */
    Float64Vector detectOtherAgents(Float64Vector oldVelocity, boolean special) {
        Float64Vector toReturn = Float64Vector.valueOf(0.0, 0.0);
        for (BaseAgent act : neighbors) {
            double dist = Math.sqrt(Utils.calculateSquareDistancePoints(getActualPositionX(), getActualPositionY(), act.getActualPositionX(), act.getActualPositionY()));
            // interact
            Float64Vector nij = calculateNIJVector(act);
            Float64Vector interactForce = Float64Vector.valueOf(0, 0);
            if (act.getType() != AgentsTypes.POLICEMAN) {
                if (!special) {
                    interactForce = calculateInteractForce(nij, dist);
                }
            } else {
                Policeman actual = (Policeman) act;
                if (actual.isNotOnDesiredPosition() && actual.getState() == AgentsStates.IS_PUSHED) {
                    interactForce = calculateInteractForce(nij, dist);
                }
            }
            if (Parameters.agentRadius + Parameters.agentRadius > dist) {
                Float64Vector bodyForce = calculateBodyForce(nij, dist);
                Float64Vector slidForce = calculateSlidForce(nij, act, oldVelocity, dist);
                toReturn = toReturn.plus(interactForce).plus(bodyForce).plus(slidForce);
            } else {
                toReturn = toReturn.plus(interactForce.times(0.1));
            }
        }
        return toReturn;
    }

    @Override
    protected void socialForces(Float64Vector oldVelocity) {
        acceleration = velocity.minus(oldVelocity).times(1.0 / Constants.timePeriod);
        if (isNotOnDesiredPosition()) {
            acceleration = acceleration.plus(detectOtherAgents(oldVelocity, true).times(1.0 / m));
            acceleration = acceleration.plus(detectObstacles(oldVelocity).times(1.0 / m));
        }
//        else {
//            acceleration = acceleration.plus(detectOtherAgents(oldVelocity, true).times(1.0 / m));
//        }
        acceleration = acceleration.plus(pushToApply.times(1.0 / m));
        acceleration = acceleration.plus(react().times(1.0 / m));
    }

    @Override
    protected void chooseVelocity(Float64Vector oldVelocity) {
        if (react().normValue() < 0.5) {
            velocity = findDesiredDirection(desiredPoint);
        }
    }


}
