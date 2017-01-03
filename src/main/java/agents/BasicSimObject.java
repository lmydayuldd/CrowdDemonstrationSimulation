package agents;

/**
 * Representation of basic object used in simulation (base for all others - agents and obstacles)
 */
public abstract class BasicSimObject {
    private Integer actualPositionX;
    private Integer actualPositionY;

    public boolean isAgent() {
        return agent;
    }

    boolean agent;

    @Override
    public String toString() {
        return "BasicSimObject{" +
                "X=" + actualPositionX +
                ", Y=" + actualPositionY +
                '}';
    }

    void setActualPositionX(Integer actualPositionX) {
        this.actualPositionX = actualPositionX;
    }

    void setActualPositionY(Integer actualPositionY) {
        this.actualPositionY = actualPositionY;
    }

    public Integer getActualPositionX() {
        return actualPositionX;
    }

    public Integer getActualPositionY() {
        return actualPositionY;
    }
}
