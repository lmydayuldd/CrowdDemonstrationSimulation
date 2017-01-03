package agents;

/**
 * Obstacle simulation object
 */
class Obstacle extends BasicSimObject {

    /**
     * Creates obstacle
     *
     * @param x - position on board x
     * @param y - position on board y
     */
    public Obstacle(Integer x, Integer y) {
        setActualPositionX(x);
        setActualPositionY(y);
        agent = false;
    }
}
