package agents;

import org.jscience.mathematics.vector.Float64Vector;
import simulation.Parameters;

import java.awt.*;

/**
 * Passive participant of demonstration
 */
public class PassiveParticipant extends BaseAgent {

    /**
     * Creates policeman agent
     *
     * @param x     - position on board x
     * @param y     - position on board y
     * @param board - simulation board
     */
    public PassiveParticipant(int x, int y, Board board) {
        super(x, y, board);
        type = AgentsTypes.PASSIVE;
        colour = Color.GREEN;
    }

    /**
     * Checks if in vicinity (range of viewRange) is any policeman or troublemaker
     *
     * @return true if there is any
     */
    private boolean isVicinityActive() {
        boolean isPoliceman = false, isTrouble = false;
        for (BaseAgent neighbor : neighbors) {
            if (neighbor.getType() == AgentsTypes.POLICEMAN) {
                isPoliceman = true;
            } else if (neighbor.getType() == AgentsTypes.TROUBLEMAKER) {
                isTrouble = true;
            }
        }
        return isPoliceman || isTrouble;
    }

    @Override
    protected void chooseVelocity(Float64Vector oldVelocity) {
        basicDesiredVelocity = findDesiredDirection(Parameters.desiredPoint);
        if (!isVicinityActive()) {
            if (neighbors.size() > 1) {
                velocity = velocity.minus(velocity);
                double sumOfValues = 0;
                for (BaseAgent neighbor : neighbors) {
                    velocity = velocity.plus(neighbor.getVelocity());
                    sumOfValues += neighbor.getVelocity().normValue();
                }
                sumOfValues += basicDesiredVelocity.normValue();
                velocity = velocity.plus(basicDesiredVelocity);
                velocity = velocity.times(1.0 / sumOfValues).times(oldVelocity.normValue());
                if (itersWithoutMove > 5 && velocity.normValue() < Parameters.maxVelocityPassive * 0.5) {
                    velocity = basicDesiredVelocity;
                }
            } else {
                velocity = basicDesiredVelocity;
            }
        } else {
            velocity = addFluctuations(basicDesiredVelocity.times(-2), Parameters.maxVelocityPassive);
        }
    }
}
