package cz.jkremlacek.ib053;

import cz.jkremlacek.ib053.models.Crossroad;
import cz.jkremlacek.ib053.models.CrossroadCommand;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Jakub Kremláček
 */
public class CrossroadManager extends Thread {
    private static CrossroadManager INSTANCE;

    //how often should be queue checked
    private static final int REFRESH_RATE = 1 * 1000;

    public static final int INTERCHANGE_TIMEOUT = 5000;

    private boolean init = true;

    private final Lock queueMutex = new ReentrantLock(true);
    private Crossroad crossroad = Crossroad.getSimpleCrossroad();
    private int timeout = 0;
    private long expectedChangeTime = 0;

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

        long remainingTime = expectedChangeTime - System.currentTimeMillis();

        m.put("remainingTime", remainingTime > 0 ? remainingTime : 0);
        m.put("semaphores", crossroad.toMap());
        m.put("queue", commandQueue);

        return m;
    }

    public void addCommand(CrossroadCommand cmd) {
        //TODO: tram priority over pedestrian buttons
        queueMutex.lock();
        commandQueue.add(cmd);
        queueMutex.unlock();
    }

    public void run() {
        while(true) {
            if (timeout > crossroad.getStateWaitTime()) {
                if (commandQueue.isEmpty()) {
                    //no external command, do regular swap
                    crossroad.switchState();
                } else {
                    //external command present

                    queueMutex.lock();

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

                    queueMutex.unlock();
                }

                timeout = 0;
            } else {
                timeout += REFRESH_RATE;
                expectedChangeTime = System.currentTimeMillis() + crossroad.getStateWaitTime() - timeout;
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
