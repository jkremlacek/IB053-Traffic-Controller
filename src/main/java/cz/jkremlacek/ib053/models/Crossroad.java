package cz.jkremlacek.ib053.models;

import cz.jkremlacek.ib053.CrossroadManager;

import java.util.*;

import static cz.jkremlacek.ib053.CrossroadManager.INTERCHANGE_TIMEOUT;

/**
 * @author Jakub Kremláček
 */
public class Crossroad {

    private final int MIN_STATE_TIME = 10000;

    //ONE - vertical direction
    //TWO - horizontal direction
    public enum CrossroadState {ONE, TWO, STOP}

    private static final Map<CrossroadState, Integer> stateTime;
    static {
        stateTime = new HashMap<>();
        stateTime.put(CrossroadState.ONE, 20000);
        stateTime.put(CrossroadState.TWO, 15000);
        stateTime.put(CrossroadState.STOP, 5000);
    }

    private final List<Map.Entry<Semaphore, CrossroadState>> trafficSemaphores = new LinkedList<>();
    private final List<Map.Entry<Semaphore, CrossroadState>> pedestrianSemaphores = new LinkedList<>();
    private final List<Map.Entry<Semaphore, CrossroadState>> pedestrianSemaphoresManual = new LinkedList<>();

    private CrossroadState state = CrossroadState.STOP;

    public int getMinStateTime() {
        return MIN_STATE_TIME;
    }

    public static Crossroad getSimpleCrossroad() {
        Crossroad c = new Crossroad();

        c.pedestrianSemaphores.add(new AbstractMap.SimpleEntry<>(new Semaphore(Semaphore.SemaphoreType.PEDESTRIAN), CrossroadState.TWO));
        c.pedestrianSemaphores.add(new AbstractMap.SimpleEntry<>(new Semaphore(Semaphore.SemaphoreType.PEDESTRIAN), CrossroadState.TWO));

        c.pedestrianSemaphoresManual.add(new AbstractMap.SimpleEntry<>(new Semaphore(Semaphore.SemaphoreType.PEDESTRIAN), CrossroadState.ONE));
        c.pedestrianSemaphoresManual.add(new AbstractMap.SimpleEntry<>(new Semaphore(Semaphore.SemaphoreType.PEDESTRIAN), CrossroadState.ONE));

        c.trafficSemaphores.add(new AbstractMap.SimpleEntry<>(new Semaphore(Semaphore.SemaphoreType.TRAFFIC), CrossroadState.ONE));
        c.trafficSemaphores.add(new AbstractMap.SimpleEntry<>(new Semaphore(Semaphore.SemaphoreType.TRAFFIC), CrossroadState.ONE));
        c.trafficSemaphores.add(new AbstractMap.SimpleEntry<>(new Semaphore(Semaphore.SemaphoreType.TRAFFIC), CrossroadState.TWO));
        c.trafficSemaphores.add(new AbstractMap.SimpleEntry<>(new Semaphore(Semaphore.SemaphoreType.TRAFFIC), CrossroadState.TWO));

        return c;
    }

    public int getStateWaitTime() {
        return stateTime.get(state);
    }

    public void switchState() {
        switch (state) {
            case ONE:
                switchStateTo(CrossroadState.TWO);
                break;
            case TWO:
                switchStateTo(CrossroadState.ONE);
                break;
            case STOP:
                switchStateTo(CrossroadState.ONE);
                break;
        }
    }

    public boolean switchStateTo(int manualNum, boolean bothSideChange) {
        if (pedestrianSemaphoresManual.size() < manualNum || manualNum < 0) {
            throw new IllegalArgumentException("ManualNum must be within bounds: 0-" + pedestrianSemaphoresManual.size());
        }

        if (pedestrianSemaphoresManual.get(manualNum).getValue() == state) {
            //do not change crossroad state to same state just with button, keep it for after one cycle
            return false;
        }

        switchStateTo(pedestrianSemaphoresManual.get(manualNum).getValue(), manualNum, bothSideChange);
        return true;
    }

    public void switchStateTo(CrossroadState state) {
        switchStateTo(state, -1, false);
    }

    public boolean isSameState(CrossroadState state) {
        return this.state == state;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> returnMap = new HashMap<>();

        returnMap.put("trafficSemaphores", Crossroad.transformSemaphoreEntry(trafficSemaphores));
        returnMap.put("pedestrianSemaphores", Crossroad.transformSemaphoreEntry(pedestrianSemaphores));
        returnMap.put("pedestrianSemaphoresManual", Crossroad.transformSemaphoreEntry(pedestrianSemaphoresManual));

        return returnMap;
    }

    private void switchStateTo(CrossroadState state, int manualNum, boolean bothSideChange) {
        if (this.state == state && manualNum == -1) {
            return;
        }

        state = state == null ? this.state : state;

        switchLights(state, manualNum, bothSideChange);

        try {
            CrossroadManager.getInstance().setExpectedChangeTime(INTERCHANGE_TIMEOUT);
            Thread.sleep(INTERCHANGE_TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        switchLights(state, manualNum, bothSideChange);

        this.state = state;
    }

    private void switchLights(CrossroadState state, int manualNum, boolean bothSideChange) {
        for (Map.Entry<Semaphore, CrossroadState> s : pedestrianSemaphores) {
            s.getKey().changeColor(s.getValue() == state ? Semaphore.SemaphoreColor.GREEN : Semaphore.SemaphoreColor.RED);
        }

        for (Map.Entry<Semaphore, CrossroadState> s : trafficSemaphores) {
            s.getKey().changeColor(s.getValue() == state ? Semaphore.SemaphoreColor.GREEN : Semaphore.SemaphoreColor.RED);
        }

        if (manualNum != -1) {
            for (int i = 0; i < pedestrianSemaphoresManual.size(); i++) {
                if (i == manualNum || bothSideChange) {
                    pedestrianSemaphoresManual.get(i).getKey().changeColor(Semaphore.SemaphoreColor.GREEN);
                } else {
                    pedestrianSemaphoresManual.get(i).getKey().changeColor(Semaphore.SemaphoreColor.RED);
                }
            }
        } else {
            for (Map.Entry<Semaphore, CrossroadState> pedestrianSemaphoreManual : pedestrianSemaphoresManual) {
                pedestrianSemaphoreManual.getKey().changeColor(Semaphore.SemaphoreColor.RED);
            }
        }
    }

    private static List<Object> transformSemaphoreEntry(List<Map.Entry<Semaphore, CrossroadState>> entries) {
        List<Object> l = new LinkedList<>();

        for (Map.Entry<Semaphore, CrossroadState> entry : entries) {
            Map <String, Object> m = new HashMap<>();

            m.put("greenAt", entry.getValue());
            m.put("semaphoreState", entry.getKey());

            l.add(m);
        }

        return l;
    }
}
