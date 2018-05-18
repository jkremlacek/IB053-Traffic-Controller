package cz.jkremlacek.ib053.models;

import cz.jkremlacek.ib053.CrossroadManager;

import java.util.*;

import static cz.jkremlacek.ib053.CrossroadManager.INTERCHANGE_TIMEOUT;

/**
 * @author Jakub Kremláček
 */
public class Crossroad {

    private final int MIN_STATE_TIME = 10000;

    public enum CrossroadState {ONE, TWO, STOP}

    private static final Map<CrossroadState, Integer> stateTime;
    static {
        stateTime = new HashMap<>();
        stateTime.put(CrossroadState.ONE, 20000);
        stateTime.put(CrossroadState.TWO, 15000);
        stateTime.put(CrossroadState.STOP, 5000);
    }

    private List<Map.Entry<Semaphore, CrossroadState>> trafficSemaphores = new LinkedList<>();
    private List<Map.Entry<Semaphore, CrossroadState>> pedestrianSemaphores = new LinkedList<>();
    private List<Map.Entry<Semaphore, CrossroadState>> pedestrianSemaphoresManual = new LinkedList<>();

    private CrossroadState state = CrossroadState.STOP;

    public Crossroad() {

    }

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
                switchStateTo(CrossroadState.TWO, -1);
                break;
            case TWO:
                switchStateTo(CrossroadState.ONE, -1);
                break;
            case STOP:
                switchStateTo(CrossroadState.ONE, -1);
                break;
        }
    }

    public boolean switchStateTo(int manualNum) {
        if (pedestrianSemaphoresManual.size() < manualNum || manualNum < 0) {
            throw new IllegalArgumentException("ManualNum must be within bounds: 0-" + pedestrianSemaphoresManual.size());
        }

        if (pedestrianSemaphoresManual.get(manualNum).getValue() == state) {
            //do not change crossroad state to same state just with button, keep it for after one cycle
            return false;
        }

        switchStateTo(pedestrianSemaphoresManual.get(manualNum).getValue(), manualNum);
        return true;
    }

    public void switchStateTo(CrossroadState state) {
        switchStateTo(state, -1);
    }

    public void switchStateTo(CrossroadState state, int manualNum) {
        if (this.state == state && manualNum == -1) {
            return;
        }

        state = state == null ? this.state : state;

        //Lock

        switchLights(state, manualNum);

        try {
            CrossroadManager.getInstance().setExpectedChangeTime(INTERCHANGE_TIMEOUT);
            Thread.sleep(INTERCHANGE_TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        switchLights(state, manualNum);

        this.state = state;

        //Unlock
    }

    private void switchLights(CrossroadState state, int manualNum) {
        for (Map.Entry<Semaphore, CrossroadState> s : pedestrianSemaphores) {
            s.getKey().changeColor(s.getValue() == state ? Semaphore.SemaphoreColor.GREEN : Semaphore.SemaphoreColor.RED);
        }

        for (Map.Entry<Semaphore, CrossroadState> s : trafficSemaphores) {
            s.getKey().changeColor(s.getValue() == state ? Semaphore.SemaphoreColor.GREEN : Semaphore.SemaphoreColor.RED);
        }

        if (manualNum != -1) {
            pedestrianSemaphoresManual.get(manualNum).getKey().changeColor(Semaphore.SemaphoreColor.GREEN);

            for (int i = 0; i < pedestrianSemaphoresManual.size(); i++) {
                if (i != manualNum) {
                    pedestrianSemaphoresManual.get(i).getKey().changeColor(Semaphore.SemaphoreColor.RED);
                }
            }
        } else {
            for (int i = 0; i < pedestrianSemaphoresManual.size(); i++) {
                pedestrianSemaphoresManual.get(i).getKey().changeColor(Semaphore.SemaphoreColor.RED);
            }
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> returnMap = new HashMap<>();

        //Lock

        returnMap.put("trafficSemaphores", Crossroad.transformSemaphoreEntry(trafficSemaphores));
        returnMap.put("pedestrianSemaphores", Crossroad.transformSemaphoreEntry(pedestrianSemaphores));
        returnMap.put("pedestrianSemaphoresManual", Crossroad.transformSemaphoreEntry(pedestrianSemaphoresManual));

        //Unlock

        return returnMap;
    }

    public static List<Object> transformSemaphoreEntry(List<Map.Entry<Semaphore, CrossroadState>> entries) {
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
