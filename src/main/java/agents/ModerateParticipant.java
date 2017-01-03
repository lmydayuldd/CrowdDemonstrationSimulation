package agents;

import org.jscience.mathematics.vector.Float64Vector;
import simulation.Parameters;

import java.awt.*;

/**
 * Moderate active participant of demonstration
 */
public class ModerateParticipant extends TroubleMaker {

    /**
     * Creates policeman agent
     *
     * @param x     - position on board x
     * @param y     - position on board y
     * @param board - simulation board
     */
    public ModerateParticipant(int x, int y, Board board) {
        super(x, y, board);
        type = AgentsTypes.MODERATEACTIVE;
        colour = Color.YELLOW;
    }

    @Override
    protected void chooseVelocity(Float64Vector oldVelocity) {
        basicDesiredVelocity = findDesiredDirection(Parameters.desiredPoint).times(5);
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
            if (itersWithoutMove > 2 && velocity.normValue() < Parameters.maxVelocityModerate * 0.5) {
                velocity = basicDesiredVelocity;
            }
        } else {
            velocity = basicDesiredVelocity;
        }
    }

    @Override
    public void changePosition() {
        super.changePosition();
        pushSomebody(findClosestInDesiredDirection());
    }
}
