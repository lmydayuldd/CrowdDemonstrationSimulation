package graphics;

import controller.SimController;
import listeners.ChangerListener;
import simulation.Parameters;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;

public class CrowdFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    /**
     * enum to change panels.
     */
    private enum SubMenu {
        main, parameters
    }

    private DraggablePanel leftPanel;
    private JPanel settingPanel;
    private JPanel settingPanelMain;
    private JPanel settingPanelParameters;
    private ButtonGroup addingObjectsGroup;

    public DraggablePanel getLeftPanel() {
        return leftPanel;
    }

    public CrowdFrame(SimController controller) {
        initUI(controller);
    }

    public int getSimulationPanelWidth() {
        return leftPanel.getWidth();
    }

    public int getSimulationPanelHeight() {
        return leftPanel.getHeight();
    }

    private void initUI(final SimController controller) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Demonstration Simulation");
        setSize(1024, 668);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setLayout(null);

        // left panel -- drag panel
        leftPanel = new DraggablePanel("resources/background/bg_black.png",
                0, 0, -1, 644, controller);
        contentPane.add(leftPanel);

        // right panel
        ImagePanel rightPanel = new ImagePanel("resources/background/bg_green.png",
                753, 0, -1, -1);
        contentPane.add(rightPanel);

		/* 
         * controlled buttons to change right panel with parameters.
		 */
        ImageButton btnMain = new ImageButton("resources/background/bgBtnUp.png",
                "Main", 0, 0);
        btnMain.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                changePanel(SubMenu.main);
            }
        });
        rightPanel.add(btnMain);


        ImageButton btnSettings = new ImageButton("resources/background/bgBtnUp.png",
                "Parameters", btnMain.getWidth(), 0);
        btnSettings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                changePanel(SubMenu.parameters);
            }
        });
        rightPanel.add(btnSettings);
		
			
		/*
		 * Panels with parameters
		 */
        settingPanel = new JPanel();
        settingPanel.setOpaque(false);
        settingPanel.setBounds(0, btnMain.getHeight() + 1,
                rightPanel.getWidth(),
                rightPanel.getHeight() - btnMain.getHeight());
        settingPanel.setLayout(null);
        rightPanel.add(settingPanel);


        settingPanelMain = new JPanel();
        settingPanelMain.setOpaque(false);
        settingPanelMain.setBounds(0, -10, settingPanel.getWidth(),
                settingPanel.getHeight());
        settingPanelMain.setLayout(null);
        settingPanel.add(settingPanelMain);

        ImagePanel addingPanel = new ImagePanel("resources/background/panel.png",
                0, 0, -1, -1);
        addingPanel.add(createTitle("Adding objects", 25, 25));

        addingObjectsGroup = new ButtonGroup();

        JRadioButton rbCrowds = new JRadioButton("crowd");
        rbCrowds.setSelected(true);
        rbCrowds.setBounds(30, 50, 100, 20);
        rbCrowds.setOpaque(false);
        addingObjectsGroup.add(rbCrowds);
        addingPanel.add(rbCrowds);

        JRadioButton rbPoliceMan = new JRadioButton("police");
        rbPoliceMan.setBounds(130, 50, 100, 20);
        rbPoliceMan.setOpaque(false);
        addingObjectsGroup.add(rbPoliceMan);
        addingPanel.add(rbPoliceMan);

        JRadioButton rbObstacles = new JRadioButton("obstacles");
        rbObstacles.setBounds(30, 70, 100, 20);
        rbObstacles.setOpaque(false);
        addingObjectsGroup.add(rbObstacles);
        addingPanel.add(rbObstacles);

        final LabelSpinnerPanel crowdDensity = new LabelSpinnerPanel(
                30, rbObstacles.getY() + rbObstacles.getHeight(),
                Parameters.crowdSparsity, 1.0, 5.0, 1.0, "Crowd sparsity");
        crowdDensity.getSpinner().addChangeListener(new ChangerListener(crowdDensity,
                ChangerListener.Parameters.crowdSparsity));
        addingPanel.add(crowdDensity);

        LabelSpinnerPanel partOfModerate = new LabelSpinnerPanel(
                30, crowdDensity.getY() + crowdDensity.getHeight(),
                Parameters.partOfModerate, 11.0, 30.0, 1.0, "Part of moderate");
        partOfModerate.getSpinner().addChangeListener(new ChangerListener(partOfModerate,
                ChangerListener.Parameters.partOfModerate));
        addingPanel.add(partOfModerate);

        LabelSpinnerPanel partOfTroubleMakers = new LabelSpinnerPanel(
                30, partOfModerate.getY() + partOfModerate.getHeight(),
                Parameters.partOfTroubleMakers, 0.0, 10.0, 1.0, "Part of Trouble Makers");
        partOfTroubleMakers.getSpinner().addChangeListener(new ChangerListener(partOfTroubleMakers,
                ChangerListener.Parameters.partOfTroubleMakers));
        addingPanel.add(partOfTroubleMakers);

        LabelSpinnerPanel agentRadius = new LabelSpinnerPanel(
                30, partOfTroubleMakers.getY() + partOfTroubleMakers.getHeight(),
                Parameters.agentRadiusGUI, 2.0, 5.0, 1.0, "Agents radius");
        agentRadius.getSpinner().addChangeListener(new ChangerListener(agentRadius,
                ChangerListener.Parameters.agentRadius));
        addingPanel.add(agentRadius);

        addingPanel.updateHeight(10);
        settingPanelMain.add(addingPanel);

        ImagePanel simulationControlPanel = new ImagePanel("resources/background/panel.png",
                0, addingPanel.getY() + addingPanel.getHeight() - 20,
                -1, 190);
        simulationControlPanel.add(createTitle("Simulation Control", 25, 20));
        ImageButton btnStart = new ImageButton("resources/background/bgBtn.png", "Start", 27, 50);
        simulationControlPanel.add(btnStart);
        btnStart.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (!controller.isRunning()) {
                    controller.simulate();
                } else if (!controller.isSimulationRunning())
                    controller.resumeSimulation();
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
            }
        });
        ImageButton btnStop = new ImageButton("resources/background/bgBtn.png",
                "Stop", 27, 60 + btnStart.getHeight());
        simulationControlPanel.add(btnStop);
        btnStop.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (controller.isRunning()) {
                    if (controller.isSimulationRunning())
                        controller.pauseSimulation();
                }
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
            }
        });
        ImageButton btnReset = new ImageButton("resources/background/bgBtn.png",
                "Reset", 27, 70 + btnStart.getHeight() * 2);
        simulationControlPanel.add(btnReset);
        btnReset.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (!controller.isSimulationRunning())
                    controller.resetSimulation();
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
            }
        });
        settingPanelMain.add(simulationControlPanel);

        settingPanelParameters = new JPanel();
        settingPanelParameters.setOpaque(false);
        settingPanelParameters.setBounds(0, 0, settingPanel.getWidth(),
                settingPanel.getHeight());
        settingPanelParameters.setLayout(null);

        ImagePanel parametersFps = new ImagePanel("resources/background/panel_2.png",
                0, 5, -1, 130);

        parametersFps.add(createTitle("Frames per second", 25, 10));
        LabelSpinnerPanel fps = new LabelSpinnerPanel(30, 30,
                Parameters.fps, 10.0, 50.0, 1.0, "FPS");
        fps.getSpinner().addChangeListener(new ChangerListener(fps, ChangerListener.Parameters.fps));
        parametersFps.add(fps);
        parametersFps.updateHeight(10);
        settingPanelParameters.add(parametersFps);

        ImagePanel parametersAfterSimulation = new ImagePanel("resources/background/panel.png",
                0, parametersFps.getHeight() - 10, -1, 130);

        parametersAfterSimulation.add(createTitle("Parameters during sim", 25, 27));


        LabelSpinnerPanel maxVelocityPassive = new LabelSpinnerPanel(
                30, 50,
                Parameters.maxVelocityPassive, 0.5, 2.0, 0.1, "Max Velocity Passive");
        maxVelocityPassive.getSpinner().addChangeListener(new ChangerListener(maxVelocityPassive,
                ChangerListener.Parameters.maxVelocityPassive));
        parametersAfterSimulation.add(maxVelocityPassive);

        LabelSpinnerPanel maxVelocityModerate = new LabelSpinnerPanel(
                30, maxVelocityPassive.getY() + maxVelocityPassive.getHeight(),
                Parameters.maxVelocityModerate, 0.5, 2, 0.1, "Max Velocity Moderate");
        maxVelocityModerate.getSpinner().addChangeListener(new ChangerListener(maxVelocityModerate,
                ChangerListener.Parameters.maxVelocityModerate));
        parametersAfterSimulation.add(maxVelocityModerate);

        LabelSpinnerPanel maxVelocityTrouble = new LabelSpinnerPanel(
                30, maxVelocityModerate.getY() + maxVelocityModerate.getHeight(),
                Parameters.maxVelocityTrouble, 0.5, 2.0, 0.1, "Max Velocity Trouble");
        maxVelocityTrouble.getSpinner().addChangeListener(new ChangerListener(maxVelocityTrouble,
                ChangerListener.Parameters.maxVelocityTrouble));
        parametersAfterSimulation.add(maxVelocityTrouble);

        LabelSpinnerPanel viewRange = new LabelSpinnerPanel(
                30, maxVelocityTrouble.getY() + maxVelocityTrouble.getHeight(),
                Parameters.viewRange, 5.0, 30.0, 1.0, "View Range");
        viewRange.getSpinner().addChangeListener(new ChangerListener(viewRange,
                ChangerListener.Parameters.viewRange));
        parametersAfterSimulation.add(viewRange);


        ImageButton btnSetPoint = new ImageButton("resources/background/bgBtn.png",
                "Set point", 27, viewRange.getY() + viewRange.getHeight());
        parametersAfterSimulation.add(btnSetPoint);
        btnSetPoint.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
                leftPanel.changeCursor();
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
            }
        });

        parametersAfterSimulation.updateHeight(30);
        settingPanelParameters.add(parametersAfterSimulation);

        ImagePanel simulationControlPanel_2 = new ImagePanel("resources/background/panel.png",
                0, parametersAfterSimulation.getY() + parametersAfterSimulation.getHeight() - 20,
                -1, 190);
        simulationControlPanel_2.add(createTitle("Simulation Control", 25, 20));
        ImageButton btnStart_2 = new ImageButton("resources/background/bgBtn.png", "Start", 27, 50);
        simulationControlPanel_2.add(btnStart_2);
        btnStart_2.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (!controller.isRunning()) {
                    controller.simulate();
                } else if (!controller.isSimulationRunning())
                    controller.resumeSimulation();
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
            }
        });
        ImageButton btnStop_2 = new ImageButton("resources/background/bgBtn.png",
                "Stop", 27, 60 + btnStart.getHeight());
        simulationControlPanel_2.add(btnStop_2);
        btnStop_2.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (controller.isRunning()) {
                    if (controller.isSimulationRunning())
                        controller.pauseSimulation();
                }
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
            }
        });
        ImageButton btnReset_2 = new ImageButton("resources/background/bgBtn.png",
                "Reset", 27, 70 + btnStart.getHeight() * 2);
        simulationControlPanel_2.add(btnReset_2);
        btnReset_2.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (!controller.isSimulationRunning())
                    controller.resetSimulation();
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
            }
        });
        settingPanelParameters.add(simulationControlPanel_2);
    }

    private JTextField createTitle(String title, int x, int y) {
        JTextField titleTmp = new JTextField(title);
        titleTmp.setBounds(x, y, 200, 30);
        titleTmp.setForeground(Color.white);
        titleTmp.setOpaque(false);
        titleTmp.setBorder(null);
        titleTmp.setEditable(false);
        titleTmp.setFont(new Font("Verdana", Font.BOLD, 14));
        return titleTmp;
    }

    public String getSelectedAddingObject() {
        return getSelectedRadioButtonText(addingObjectsGroup);
    }

    private String getSelectedRadioButtonText(ButtonGroup btnGroup) {
        Enumeration<AbstractButton> allButtons = btnGroup.getElements();
        while (allButtons.hasMoreElements()) {
            JRadioButton jbutton = (JRadioButton) allButtons.nextElement();
            if (jbutton.isSelected()) {
                return jbutton.getText();
            }
        }
        return null;
    }

    private void changePanel(SubMenu modes) {
        settingPanel.removeAll();
        settingPanel.repaint();
        settingPanel.revalidate();

        switch (modes) {
            case main:
                settingPanel.add(settingPanelMain);
                break;
            case parameters:
                settingPanel.add(settingPanelParameters);
                break;
            default:
                break;

        }
        settingPanel.repaint();
        settingPanel.revalidate();
    }
}