package controller;

import agents.*;
import graphics.CrowdFrame;
import org.apache.commons.math3.util.Pair;
import simulation.Parameters;
import utils.Utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Application controller
 */
public class SimController implements ActionListener {
    private final int num = (Runtime.getRuntime().availableProcessors() - 1 > 0) ? Runtime.getRuntime().availableProcessors() - 1 : 1;  // number of available jvm's threads
    private final ExecutorService executorsPool = Executors.newFixedThreadPool(num); // executors
    private final Board board = new Board(); // board
    private CrowdFrame mainFrame = null; // application's main frame

    private boolean running = false; // if simulation is running (true even if paused, true means, that simulation was started and not reset)
    private boolean simulationRunning = false; // if simulation is running - pause = false

    private LinkedList<BaseAgent> agents; // all existing agents
    private LinkedList<AgentsTask> tasks; // list of tasks for executors
    private Timer timer; // swing's timer (fps)

    public boolean isSimulationRunning() {
        return simulationRunning;
    }

    public boolean isRunning() {
        return running;
    }

    public void setMainFrame(CrowdFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public Board getBoard() {
        return board;
    }

    /**
     * Initializes board
     *
     * @param x - size of board x
     * @param y - size of board y
     */
    public void initialize(int x, int y) {
        board.initialize(x, y);
    }

    /**
     * Creates new instance of Timer with fps from Parameters class
     */
    private void createTimer() {
        timer = new Timer(1000 / Parameters.fps, this);
    }

    /**
     * Generates objects - agents (different types) or obstacles, depending on chosen option in radio box (GUI)
     *
     * @param constraintLU left upper corner of rectangle (area to insert objects)
     * @param constraintRD - right bottom
     */
    public void generateObjects(Pair<Integer, Integer> constraintLU, Pair<Integer, Integer> constraintRD) {
        if (board.getAgents().size() == 0) {
            rewriteAgentRadiusFromGUI();
        }
        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> temp = Utils.makeRectangleProper(constraintLU, constraintRD);
        constraintLU = temp.getFirst();
        constraintRD = temp.getSecond();
        switch (getAddingObjectParameter()) {
            case "obstacles":
                SimulationObjectsGenerator.generateObstaclesAndUpdateBoard(board, constraintLU, constraintRD);
                break;
            case "crowd":
                SimulationObjectsGenerator.generateAgentsAndUpdateBoard(board, constraintLU, constraintRD);
                break;
            case "police":
                SimulationObjectsGenerator.generatePolicemenInRows(board, constraintLU, constraintRD);
                break;
        }
    }

    /**
     * Pauses simulation (sets simulationRunning false)
     */
    public void pauseSimulation() {
        simulationRunning = false;
    }

    /**
     * Resumes timer (sets simulationRunning true)
     */
    public void resumeSimulation() {
        createTimer();
        simulationRunning = true;
        timer.start();
    }

    /**
     * Destroys current simulation and creates new one
     */
    public void resetSimulation() {
        pauseSimulation();
        board.initialize(board.getSizeX(), board.getSizeY());
        running = false;
        if (mainFrame != null) {
            mainFrame.getLeftPanel().repaint();
        }
    }

    /**
     * Prepares list of tasks
     *
     * @param agents - list of existing agents
     * @return list of tasks
     */
    private LinkedList<AgentsTask> prepareTasks(LinkedList<BaseAgent> agents) {
        LinkedList<AgentsTask> tasks = new LinkedList<>();
        LinkedList<BaseAgent>[] agentsForTasks = new LinkedList[num];

        for (int i = 0; i < num; i++) {
            agentsForTasks[i] = new LinkedList<>();
        }
        int count = 0;
        for (BaseAgent agent : agents) {
            agentsForTasks[count++ % num].add(agent);
        }
        for (int i = 0; i < num; i++) {
            tasks.add(new AgentsTask(agentsForTasks[i]));
        }
        return tasks;
    }

    /**
     * Function controls forces used on policemen
     * <p/>
     * Function considers every policeman on which any force was used (policeman can "push" other policeman too, see considerForces in Policeman class)
     */
    private void considerPolicemen() {
        LinkedList<Policeman> policemenToConsider = new LinkedList<>();
        LinkedHashSet<Policeman> considered = new LinkedHashSet<>();
        for (Policeman pol : board.getPolicemen()) {
            if (pol.getState() == AgentsStates.IS_PUSHED) {
                considered.add(pol);
                Policeman toAdd = pol.considerForces();
                if (toAdd != null && !considered.contains(toAdd)) {
                    policemenToConsider.add(toAdd);
                }
            }
        }
        while (!policemenToConsider.isEmpty()) {
            Policeman toConsider = policemenToConsider.removeFirst();
            considered.add(toConsider);
            Policeman toAdd = toConsider.considerForces();
            if (toAdd != null && !considered.contains(toAdd)) {
                policemenToConsider.add(toAdd);
            }
        }
    }

    /**
     * Rewrites agents' radius from GUI
     */
    private void rewriteAgentRadiusFromGUI() {
        Parameters.agentRadius = Parameters.agentRadiusGUI;
    }

    /**
     * Single iteration of simulation
     *
     * @param event - event (not used, may be null)
     */
    public void actionPerformed(ActionEvent event) {
        /* One thread, considering forces */
        considerPolicemen();
        for (BaseAgent agent : agents) {
            agent.applyPushForces();
        }

        /* Multi threads, updating agents state */
        try {
            executorsPool.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* One thread, changing agents position, repainting */
        for (BaseAgent agent : agents) {
            agent.changePosition();
        }

        if (mainFrame != null) {
            mainFrame.getLeftPanel().repaint();
        }

        if (!simulationRunning) {
            timer.stop();
        }
    }

    /**
     * Starts simulation
     */
    public void simulate() {
        createTimer();
        simulationRunning = true;
        running = true;
        agents = getBoard().getAgents();
        tasks = prepareTasks(agents);

        timer.setRepeats(true);
        timer.start();
    }

    /**
     * Returns actually set kind of object (in window)
     *
     * @return string - kind of object to create
     */
    private String getAddingObjectParameter() {
        return mainFrame.getSelectedAddingObject();
    }

    /**
     * Class created to disperse computation (updating state of agents)
     * <p/>
     * Holds list of agents
     */
    private final class AgentsTask implements Callable<String> {
        private final List<BaseAgent> agents;

        public AgentsTask(List<BaseAgent> agents) {
            this.agents = agents;
        }

        public String call() {
            for (BaseAgent agent : agents) {
                agent.updateState();
            }
            return "Run";
        }
    }
}
