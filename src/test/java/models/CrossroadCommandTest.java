package models;

import cz.jkremlacek.ib053.models.Crossroad;
import cz.jkremlacek.ib053.models.CrossroadCommand;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Jakub Kremláček
 */
public class CrossroadCommandTest {

    @Test
    public void stateCommandTest() {
        CrossroadCommand cc = new CrossroadCommand(Crossroad.CrossroadState.ONE);
        assertEquals(Crossroad.CrossroadState.ONE, cc.getState());
        assertEquals(-1, cc.getCrossroadNumber());
        assertTrue(cc.isTramCommand());
    }

    @Test
    public void crossroadCommandTest() {
        CrossroadCommand cc = new CrossroadCommand(10);
        assertEquals(null, cc.getState());
        assertEquals(10, cc.getCrossroadNumber());
        assertFalse(cc.isTramCommand());
    }

    @Test
    public void equalsTest() {
        CrossroadCommand ccOne1 = new CrossroadCommand(Crossroad.CrossroadState.ONE);
        CrossroadCommand ccOne2 = new CrossroadCommand(Crossroad.CrossroadState.ONE);
        CrossroadCommand ccTwo = new CrossroadCommand(Crossroad.CrossroadState.TWO);
        CrossroadCommand ccNum1 = new CrossroadCommand(5);
        CrossroadCommand ccNum2 = new CrossroadCommand(5);

        assertTrue(ccOne1.equals(ccOne2));
        assertTrue(ccNum1.equals(ccNum2));
        assertFalse(ccOne1.equals(ccTwo));
        assertFalse(ccOne1.equals(ccNum1));
    }
}
