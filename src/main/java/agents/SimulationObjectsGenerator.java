package agents;

import org.apache.commons.math3.util.Pair;
import simulation.Parameters;
import utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public abstract class SimulationObjectsGenerator {

    /**
     * Removes points to remove from both other given lists
     *
     * @param allAvailableFields - all available fields
     * @param pointsToRemove     - fields to remove from available
     * @param availableFields    - fields available to create agents
     */
    private static void removeFieldsFromAvailable(LinkedHashSet<Pair<Integer, Integer>> allAvailableFields, LinkedList<Pair<Integer, Integer>> pointsToRemove, LinkedHashSet<Pair<Integer, Integer>> availableFields) {
        for (Pair<Integer, Integer> aPointsToRemove : pointsToRemove) {
            availableFields.remove(aPointsToRemove);
            allAvailableFields.remove(aPointsToRemove);
        }
    }

    /**
     * Randomly chooses fields from available.
     * Also modifies set of allAvailableFields.
     *
     * @param availableFields    - fields available to create agents
     * @param allAvailableFields - all available fields
     * @return list of chosen fields
     */
    private static ArrayList<Pair<Integer, Integer>> getFields(LinkedHashSet<Pair<Integer, Integer>> availableFields, LinkedHashSet<Pair<Integer, Integer>> allAvailableFields) {
        ArrayList<Pair<Integer, Integer>> fields = new ArrayList<>(availableFields.size() / 10);

        int index = 0;
        while (availableFields.size() > 0) {
            Object[] avArray = availableFields.toArray();
            fields.add((Pair<Integer, Integer>) avArray[Utils.rand.nextInt(availableFields.size())]);
            index++;
            LinkedList<Pair<Integer, Integer>> pointsToRemove = Utils.getPointsInVicinity(fields.get(index - 1).getFirst(), fields.get(index - 1).getSecond(), 2 * Parameters.agentRadius + Parameters.crowdSparsity);
            removeFieldsFromAvailable(allAvailableFields, pointsToRemove, availableFields);
        }
        return fields;
    }

    /**
     * Generates agents on chosen fields and inserts them to board.
     *
     * @param board        - simulation's board
     * @param constraintLU - left upper corner of rectangle (area to insert agents)
     * @param constraintRD - right bottom
     */
    public static void generateAgentsAndUpdateBoard(Board board, Pair<Integer, Integer> constraintLU, Pair<Integer, Integer> constraintRD) {
        ArrayList<Pair<Integer, Integer>> fields;
        fields = getFields(board.getAvailableFieldsWithConstraints(constraintLU, constraintRD, true), board.getAllAvailableFields());
        LinkedList<BaseAgent> agents = board.getAgents();
        for (Pair<Integer, Integer> actField : fields) {
            BaseAgent temp;
            int prob = Math.abs(Utils.rand.nextInt()) % 100;
            if (prob >= 100 - Parameters.partOfTroubleMakers) {
                temp = new TroubleMaker(actField.getFirst(), actField.getSecond(), board);
            } else if (prob >= 100 - Parameters.partOfModerate - Parameters.partOfTroubleMakers) {
                temp = new ModerateParticipant(actField.getFirst(), actField.getSecond(), board);
            } else {
                temp = new PassiveParticipant(actField.getFirst(), actField.getSecond(), board);
            }
            board.updateField(actField, temp);
            agents.add(temp);
        }
    }

    /**
     * Generates obstacles in specified area and inserts them to board.
     *
     * @param board        - simulation's board
     * @param constraintLU - left upper corner of rectangle (area to insert obstacles)
     * @param constraintRD - right bottom
     */
    public static void generateObstaclesAndUpdateBoard(Board board, Pair<Integer, Integer> constraintLU, Pair<Integer, Integer> constraintRD) {
        LinkedHashSet<Pair<Integer, Integer>> fields, allAvailableFields = board.getAllAvailableFields();
        fields = board.getAvailableFieldsWithConstraints(constraintLU, constraintRD, false);
        board.getObstaclesRect().add(new Pair<>(constraintLU, constraintRD));
        LinkedList<Obstacle> obstacles = board.getObstacles();
        for (Pair<Integer, Integer> temp : fields) {
            Obstacle obsTemp = new Obstacle(temp.getFirst(), temp.getSecond());
            board.updateField(temp, obsTemp);
            allAvailableFields.remove(temp);
            obstacles.add(obsTemp);
        }
    }

    /**
     * Generates policemen row by row in given area
     *
     * @param board         - simulation's board
     * @param constraintLU- left upper corner of rectangle (area to insert obstacles)
     * @param constraintRD  - right bottom
     */
    public static void generatePolicemenInRows(Board board, Pair<Integer, Integer> constraintLU, Pair<Integer, Integer> constraintRD) {
        LinkedHashSet<Pair<Integer, Integer>> availableFields = board.getAvailableFieldsWithConstraints(constraintLU, constraintRD, true);
        LinkedList<BaseAgent> agents = board.getAgents();
        LinkedList<Policeman> policemen = board.getPolicemen();
        int left = constraintLU.getFirst(), up = constraintLU.getSecond(), right = constraintRD.getFirst(), down = constraintRD.getSecond();
        if (right >= board.getSizeX()) right = board.getSizeX() - 1;
        if (left >= board.getSizeY()) left = board.getSizeY() - 1;
        int actX = right - Parameters.agentRadius - 1, actY;
        int step = 2 * Parameters.agentRadius;
        while (actX >= left + Parameters.agentRadius) {
            actY = up + Parameters.agentRadius;
            while (actY <= down - Parameters.agentRadius) {
                Pair<Integer, Integer> field = new Pair<>(actX, actY);
                if (availableFields.contains(field)) {
                    Policeman temp = new Policeman(actX, actY, board);
                    board.updateField(field, temp);
                    agents.add(temp);
                    policemen.add(temp);
                    LinkedList<Pair<Integer, Integer>> pointsToRemove = Utils.getPointsInVicinity(field.getFirst(), field.getSecond(), 2 * Parameters.agentRadius - 1);
                    removeFieldsFromAvailable(board.getAllAvailableFields(), pointsToRemove, availableFields);
                }
                actY += step;
            }
            actX -= step;
        }
    }
}
