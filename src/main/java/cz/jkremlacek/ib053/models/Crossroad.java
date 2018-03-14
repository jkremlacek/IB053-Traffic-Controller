package cz.jkremlacek.ib053.models;

import java.util.*;

/**
 * @author Jakub Kremláček
 */
public class Crossroad {

    public enum CrossroadState {ONE, TWO, STOP}

    private List<Map.Entry<Semaphore, CrossroadState>> trafficSemaphores = new LinkedList<>();
    private List<Map.Entry<Semaphore, CrossroadState>> pedestrianSemaphores = new LinkedList<>();
    private List<Map.Entry<Semaphore, CrossroadState>> pedestrianSemaphoresManual = new LinkedList<>();

    private CrossroadState state = CrossroadState.STOP;

    public Crossroad() {

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

    public void switchStateTo(int manualNum) {
        switchStateTo(pedestrianSemaphoresManual.get(manualNum).getValue(), manualNum);
    }

    public void switchStateTo(CrossroadState state) {
        switchStateTo(state, -1);
    }

    public void switchStateTo(CrossroadState state, int manualNum) {
        if (this.state == state) {
            return;
        }

        state = state == null ? this.state : state;

        //Lock

        switchLights(state, manualNum);

        try {
            Thread.sleep(5000);
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
                    pedestrianSemaphoresManual.get(manualNum).getKey().changeColor(Semaphore.SemaphoreColor.RED);
                }
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

            m.put("pos", entry.getValue());
            m.put("semaphoreState", entry.getKey());

            l.add(m);
        }

        return l;
    }
}
