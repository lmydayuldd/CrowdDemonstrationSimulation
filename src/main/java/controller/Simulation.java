package controller;

import graphics.CrowdFrame;

import java.awt.*;

/**
 * Main simulation class
 */
class Simulation {

    /**
     * Main
     *
     * @param args - program arguments
     */
    public static void main(String[] args) {
        final SimController controller = new SimController();
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                CrowdFrame crowdFrame = new CrowdFrame(controller);
                controller.setMainFrame(crowdFrame);
                controller.initialize(crowdFrame.getSimulationPanelWidth(), crowdFrame.getSimulationPanelHeight());
                crowdFrame.setVisible(true);
            }
        });
    }
}
