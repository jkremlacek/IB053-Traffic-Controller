package cz.jkremlacek.ib053;

import cz.jkremlacek.ib053.models.Crossroad;
import cz.jkremlacek.ib053.models.CrossroadCommand;

import java.util.*;

/**
 * @author Jakub Kremláček
 */
public class CrossroadManager extends Thread {
    private static CrossroadManager INSTANCE;

    //time for how long one switch holds
    private static final int TIMEOUT_THRESHOLD = 15 * 1000;

    //how often should be queue checked
    private static final int REFRESH_RATE = 1 * 1000;

    private Crossroad crossroad = Crossroad.getSimpleCrossroad();
    private int timeout = 0;

    private Set<CrossroadCommand> commandQueue = Collections.synchronizedSet((Set) new LinkedHashSet<>()) ;

    public static CrossroadManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CrossroadManager();
            new Thread(INSTANCE).start();
        }

        return INSTANCE;
    }

    public Map<String, Object> getCurrentCrossroadState() {
        Map<String, Object> m = new HashMap<>();

        int remainingTime = TIMEOUT_THRESHOLD - timeout;

        m.put("remainingTime", remainingTime > 0 ? remainingTime : 0);
        m.put("semaphores", crossroad.toMap());

        return m;
    }

    public void addCommand(CrossroadCommand cmd) {
        //TODO: tram priority over pedestrian buttons
        commandQueue.add(cmd);
    }

    public void run() {
        while(true) {
            if (timeout > TIMEOUT_THRESHOLD) {
                if (commandQueue.isEmpty()) {
                    //no external command, do regular swap
                    crossroad.switchState();
                } else {
                    //external command present

                    //get top priority command (tram has higher priority than pedestrian)
                    Iterator<CrossroadCommand> it = getIterator();

                    CrossroadCommand cmd = it.next();

                    if (cmd.getState() != null) {
                        //tram change
                        crossroad.switchStateTo(cmd.getState());
                    } else {
                        //pedestrian button change
                        crossroad.switchStateTo(cmd.getCrossroadNumber());
                    }

                    it.remove();
                }

                timeout = 0;
            } else {
                timeout += REFRESH_RATE;
            }

            try {
                Thread.sleep(REFRESH_RATE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Iterator<CrossroadCommand> getIterator() {
        CrossroadCommand[] commandsInQueue = commandQueue.toArray(new CrossroadCommand[commandQueue.size()]);

        int j = 0;

        for (int i = 0; i < commandsInQueue.length; i++) {
            if (commandsInQueue[i].getState() == Crossroad.CrossroadState.ONE) {
                j = i;
                break;
            }
        }

        Iterator<CrossroadCommand> it = commandQueue.iterator();

        for (int i = 0; i < j; i++) {
            it.next();
        }

        return it;
    }
}
