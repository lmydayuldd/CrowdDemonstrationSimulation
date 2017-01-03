package simulation;

import org.apache.commons.math3.util.Pair;

/**
 * Parameters of simulation - values which can be changed by user
 */
public abstract class Parameters {
    /* Parameters to set before creating objects */
    public static int agentRadius = 3; // agent radius
    public static int agentRadiusGUI = 3; // agent radius (set in GUI)
    public static int crowdSparsity = 1;  // distance between generated agents
    public static int partOfModerate = 15; // percent of moderate active agents
    public static int partOfTroubleMakers = 5; // percent of trouble makers

    /* Parameters to set before simulation start */
    public static int fps = 30;  /* 2 to 50 */

    /* Whenever */
    public static double maxVelocityPassive = 1.2; /* 0.5 to 2 */
    public static double maxVelocityModerate = 1.6; /* 0.5 to 2 */
    public static double maxVelocityTrouble = 2.0; /* 0.5 to 2 */
    public static int viewRange = 10; // view range of agent

    /* Parameter to set before simulation start */
    public static Pair<Integer, Integer> desiredPoint = new Pair<>(350, 20); // can be chosen by pointing mouse
}
