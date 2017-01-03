package listeners;

import graphics.LabelSpinnerPanel;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ChangerListener implements ChangeListener {
    public enum Parameters {
        agentRadius, crowdSparsity, viewRange, partOfModerate, partOfTroubleMakers,
        fps, maxVelocityPassive, maxVelocityModerate, maxVelocityTrouble
    }

    private final LabelSpinnerPanel labelSpinner;
    private final Parameters label;

    public ChangerListener(LabelSpinnerPanel labelSpinner, Parameters label) {
        this.labelSpinner = labelSpinner;
        this.label = label;
    }

    @Override
    public void stateChanged(ChangeEvent arg0) {
        switch (label) {
            case agentRadius: {
                simulation.Parameters.agentRadiusGUI = (int) labelSpinner.getSpinnerDoubleValue();
                break;
            }
            case crowdSparsity: {
                simulation.Parameters.crowdSparsity = (int) labelSpinner.getSpinnerDoubleValue();
                break;
            }
            case viewRange: {
                simulation.Parameters.viewRange = (int) labelSpinner.getSpinnerDoubleValue();
                break;
            }
            case partOfModerate: {
                simulation.Parameters.partOfModerate = (int) labelSpinner.getSpinnerDoubleValue();
                break;
            }
            case partOfTroubleMakers: {
                simulation.Parameters.partOfTroubleMakers = (int) labelSpinner.getSpinnerDoubleValue();
                break;
            }
            case maxVelocityPassive: {
                simulation.Parameters.maxVelocityPassive = labelSpinner.getSpinnerDoubleValue();
                break;
            }
            case maxVelocityModerate: {
                simulation.Parameters.maxVelocityModerate = labelSpinner.getSpinnerDoubleValue();
                break;
            }
            case maxVelocityTrouble: {
                simulation.Parameters.maxVelocityTrouble = (int) labelSpinner.getSpinnerDoubleValue();
                break;
            }
            case fps: {
                simulation.Parameters.fps = (int) labelSpinner.getSpinnerDoubleValue();
                break;
            }
        }
    }


}
