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

    public boolean isTramCommand() {
        return state != null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!(o instanceof CrossroadCommand)) {
            return false;
        }

        CrossroadCommand other = (CrossroadCommand) o;

        return crossroadNumber == other.getCrossroadNumber() &&
            (state == null ?
                    other.getState() == null :
                    state.equals(other.getState()));
    }

    @Override
    public int hashCode() {
        return 13 * (state == null ? "NONE".hashCode() : state.hashCode()) + crossroadNumber;
    }
}
