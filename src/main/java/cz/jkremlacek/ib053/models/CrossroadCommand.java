package cz.jkremlacek.ib053.models;

/**
 * @author Jakub Kremláček
 */
public class CrossroadCommand {
    private Crossroad.CrossroadState state = null;
    private int crossroadNumber = -1;

    public CrossroadCommand(Crossroad.CrossroadState state) {
        this.state = state;
    }

    public CrossroadCommand(int crossroadNumber) {
        this.crossroadNumber = crossroadNumber;
    }

    public Crossroad.CrossroadState getState() {
        return state;
    }

    public int getCrossroadNumber() {
        return crossroadNumber;
    }

    @Override
    public boolean equals(Object o) {
        CrossroadCommand other = (CrossroadCommand) o;
        return state.equals(other.getState()) && crossroadNumber == other.getCrossroadNumber();
    }

    @Override
    public int hashCode() {
        return 13 * (state == null ? "NONE".hashCode() : state.hashCode()) + crossroadNumber;
    }
}
