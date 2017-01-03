package agents;

import org.jscience.mathematics.vector.Float64Vector;
import simulation.Constants;
import simulation.Parameters;

import java.awt.*;

/**
 * Trouble maker - the most active and aggressive participant
 */
public class TroubleMaker extends BaseAgent {

    /**
     * Creates policeman agent
     *
     * @param x     - position on board x
     * @param y     - position on board y
     * @param board - simulation board
     */
    public TroubleMaker(int x, int y, Board board) {
        super(x, y, board);
        type = AgentsTypes.TROUBLEMAKER;
        colour = Color.RED;
    }

    /**
     * Agent pushes target agent with own force (acceleration times mass)
     * Agent can push Policeman or other agent, if he's pushing Policeman
     * Value of push force depends on emotions level and mass of agent
     *
     * @param target - agent to push
     */
    void pushSomebody(BaseAgent target) {
        state = AgentsStates.NONE;
        if (target != null) {
            double maxVelocity = Parameters.maxVelocityTrouble;
            double normVelocityValue = velocity.normValue();
            if (getType() == AgentsTypes.MODERATEACTIVE) {
                maxVelocity = Parameters.maxVelocityModerate;
            }
            if (target.getType() == AgentsTypes.POLICEMAN) {
                state = AgentsStates.PUSHING_POLICEMAN;
                target.bePushed(Float64Vector.valueOf(velocity.get(0).doubleValue() * maxVelocity / normVelocityValue, velocity.get(1).doubleValue() * maxVelocity / normVelocityValue)
                        .times(m * 0.8).plus(pushToApply));
            } else if (target.getState() == AgentsStates.PUSHING_POLICEMAN || target.getState() == AgentsStates.PUSHING_OTHER) {
                state = AgentsStates.PUSHING_OTHER;
                target.bePushed(Float64Vector.valueOf(velocity.get(0).doubleValue() * maxVelocity / normVelocityValue, velocity.get(1).doubleValue() * maxVelocity / normVelocityValue)
                        .times(m * 0.8).plus(pushToApply));
            }
        }
    }

    @Override
    protected void socialForces(Float64Vector oldVelocity) {
        acceleration = velocity.minus(oldVelocity).times(1.0 / Constants.timePeriod);
        acceleration = acceleration.plus(detectOtherAgents(oldVelocity).times(1.0 / m));
        acceleration = acceleration.plus(detectObstacles(oldVelocity).times(1.0 / m));
    }

    @Override
    protected void chooseVelocity(Float64Vector oldVelocity) {
        velocity = findDesiredDirection(Parameters.desiredPoint).times(10);
    }

    @Override
    public void changePosition() {
        super.changePosition();
        pushSomebody(findClosestInDesiredDirection());
    }
}
