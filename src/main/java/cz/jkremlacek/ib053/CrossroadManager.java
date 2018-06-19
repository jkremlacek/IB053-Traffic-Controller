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

    private boolean unlocked = true;
    private Lock queueMutex = new ReentrantLock(true);

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
        queueMutex.lock();
        try {
            commandQueue.add(cmd);
        } finally {
            queueMutex.unlock();
        }
    }

    public void setExpectedChangeTime(long time) {
        expectedChangeTime = System.currentTimeMillis() + time;
    }

    public void run() {
        try {
            while(true) {
                //change crossroad state if its time or! if tram is waiting
                if (
                        timeout > crossroad.getStateWaitTime() ||
                        (!commandQueue.isEmpty() && getIterator().next().isTramCommand()))
                {
                    boolean stateChanged = true;

                    if (commandQueue.isEmpty()) {
                        //no external command, do regular swap
                        crossroad.switchState();
                    } else {
                        //external command present

                        queueMutex.lock();
                        unlocked = false;
                        try {
                            stateChanged = processCommand();
                        } finally {
                            if (!unlocked) {
                                queueMutex.unlock();
                                unlocked = true;
                            }
                        }
                    }

                    if (stateChanged) {
                        timeout = 0;
                    } else {
                        timeout += REFRESH_RATE;
                    }
                } else {
                    timeout += REFRESH_RATE;
                }

                setExpectedChangeTime(crossroad.getStateWaitTime() - timeout);

                try {
                    Thread.sleep(REFRESH_RATE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            //dirty catch everything - crossroad must be running at all costs, force restart
            queueMutex = new ReentrantLock(true);
            unlocked = true;
            crossroad = Crossroad.getSimpleCrossroad();
            commandQueue = Collections.synchronizedSet((Set) new LinkedHashSet<>()) ;
            timeout = 0;
            expectedChangeTime = 0;
            run();
        }
    }

    public Iterator<CrossroadCommand> getIterator() {
        return getIterator(true);
    }

    public Iterator<CrossroadCommand> getIterator(boolean tramPriority) {
        CrossroadCommand[] commandsInQueue = commandQueue.toArray(new CrossroadCommand[commandQueue.size()]);

        int j = 0;

        if (tramPriority) {
            for (int i = 0; i < commandsInQueue.length; i++) {
                if (commandsInQueue[i].getState() != null) {
                    j = i;
                    break;
                }
            }
        }

        Iterator<CrossroadCommand> it = commandQueue.iterator();

        for (int i = 0; i < j; i++) {
            it.next();
        }

        return it;
    }

    private Iterator<CrossroadCommand> otherSideRequest(boolean skipFirst) {
        Iterator<CrossroadCommand> itOther = getIterator(false);

        if (skipFirst) {
            itOther.next();
        }

        while (itOther.hasNext()) {
            CrossroadCommand other = itOther.next();

            if (other.getState() == null) {
                return itOther;
            }
        }
        return null;
    }

    private boolean processCommand() throws InterruptedException {
        //get top priority command (tram has higher priority than pedestrian)
        Iterator<CrossroadCommand> it = getIterator();
        CrossroadCommand cmd = it.next();

        if (cmd.getState() != null) {
            return processTramCommand(it, cmd);
        } else {
            processPedestrianCommand(it, cmd);
            return true;
        }
    }

    private void processPedestrianCommand(Iterator<CrossroadCommand> it, CrossroadCommand cmd) {
        Iterator<CrossroadCommand> other = otherSideRequest(true);

        if (crossroad.switchStateTo(cmd.getCrossroadNumber(), other != null)) {
            //command is done only if the next state changes traffic lights
            it.remove();

            if (other != null) {
                otherSideRequest(false).remove();
            }
        } else {
            //perform normal switching, keeping the button request for next cycle
            crossroad.switchState();
        }
    }

    private boolean processTramCommand(Iterator<CrossroadCommand> it, CrossroadCommand cmd) throws InterruptedException {
        if (crossroad.isSameState(cmd.getState())) {
            it.remove();
            return false;
        }

        if (timeout < crossroad.getMinStateTime()) {
            //wait for at least the minimal time period (otherwise tram could stop pedestrians just as they got green)
            it.remove();
            //unlock mutex prior to sleeping
            queueMutex.unlock();
            unlocked = true;

            setExpectedChangeTime(crossroad.getMinStateTime() - timeout);
            try {
                Thread.sleep(crossroad.getMinStateTime() - timeout);
            } finally {
                crossroad.switchStateTo(cmd.getState());
            }
        } else {
            crossroad.switchStateTo(cmd.getState());
            it.remove();
        }

        return true;
    }
}
