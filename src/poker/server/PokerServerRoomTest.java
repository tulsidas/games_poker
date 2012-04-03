package poker.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.apache.mina.common.IoSession;
import org.junit.Test;

import poker.common.messages.AllFoldedMessage;
import poker.common.messages.AllInMessage;
import poker.common.messages.CallMessage;
import poker.common.messages.FlopMessage;
import poker.common.messages.FoldMessage;
import poker.common.messages.RaiseMessage;
import poker.common.messages.RiverMessage;
import poker.common.messages.ShowdownMessage;
import poker.common.messages.StartManoMessage;
import poker.common.messages.TurnMessage;
import poker.common.messages.server.FinJuegoMessage;
import poker.common.messages.server.TuTurnoMessage;

import common.game.OponentAbandonedMessage;
import common.messages.server.UpdatedPointsMessage;
import common.model.User;

public class PokerServerRoomTest {

    @Test
    public void testIsComplete() {
        IoSession p1 = mock(IoSession.class);
        IoSession p2 = mock(IoSession.class);
        IoSession p3 = mock(IoSession.class);
        IoSession p4 = mock(IoSession.class);
        IoSession p5 = mock(IoSession.class);
        IoSession p6 = mock(IoSession.class);

        PokerServerRoom psr = new PokerServerRoom(mock(PokerSaloon.class), p1,
                100);

        assertFalse(psr.isComplete());

        assertTrue(psr.join(p2));
        assertFalse(psr.isComplete());

        assertTrue(psr.join(p3));
        assertFalse(psr.isComplete());

        assertTrue(psr.join(p4));
        assertFalse(psr.isComplete());

        assertTrue(psr.join(p5));
        assertFalse(psr.isComplete());

        assertTrue(psr.join(p6));
        assertTrue(psr.isComplete());
    }

    @Test
    public void testJoin() {
        IoSession p1 = mock(IoSession.class);
        IoSession p2 = mock(IoSession.class);
        IoSession p3 = mock(IoSession.class);
        IoSession p4 = mock(IoSession.class);
        IoSession p5 = mock(IoSession.class);
        IoSession p6 = mock(IoSession.class);
        IoSession p7 = mock(IoSession.class);

        PokerServerRoom psr = new PokerServerRoom(mock(PokerSaloon.class), p1,
                100);
        assertFalse(psr.join(p1));

        assertTrue(psr.join(p2));
        assertFalse(psr.join(p2));

        assertTrue(psr.join(p3));
        assertFalse(psr.join(p3));

        assertTrue(psr.join(p4));
        assertFalse(psr.join(p4));

        assertTrue(psr.join(p5));
        assertFalse(psr.join(p5));

        assertTrue(psr.join(p6));
        assertFalse(psr.join(p6));

        assertFalse(psr.join(p7));
    }

    @Test
    public void testHasUser() {
        IoSession p1 = mock(IoSession.class);
        IoSession p2 = mock(IoSession.class);
        IoSession p3 = mock(IoSession.class);
        IoSession p4 = mock(IoSession.class);

        PokerServerRoom psr = new PokerServerRoom(mock(PokerSaloon.class), p1,
                100);
        assertTrue(psr.hasUser(p1));

        assertFalse(psr.hasUser(p2));
        assertTrue(psr.join(p2));
        assertTrue(psr.hasUser(p2));

        assertFalse(psr.hasUser(p3));
        assertTrue(psr.join(p3));
        assertTrue(psr.hasUser(p3));

        assertFalse(psr.hasUser(p4));
        assertTrue(psr.join(p4));
        assertTrue(psr.hasUser(p4));
    }

    @Test
    public void testGetUserSessions() {
        IoSession p1 = mock(IoSession.class);
        IoSession p2 = mock(IoSession.class);
        IoSession p3 = mock(IoSession.class);
        IoSession p4 = mock(IoSession.class);
        IoSession p5 = mock(IoSession.class);
        IoSession p6 = mock(IoSession.class);
        IoSession p7 = mock(IoSession.class);

        PokerServerRoom psr = new PokerServerRoom(mock(PokerSaloon.class), p1,
                100);
        assertEquals(1, psr.getUserSessions().size());

        assertTrue(psr.join(p2));
        assertEquals(2, psr.getUserSessions().size());

        assertTrue(psr.join(p3));
        assertEquals(3, psr.getUserSessions().size());

        assertTrue(psr.join(p4));
        assertEquals(4, psr.getUserSessions().size());

        assertTrue(psr.join(p5));
        assertEquals(5, psr.getUserSessions().size());

        assertTrue(psr.join(p6));
        assertEquals(6, psr.getUserSessions().size());

        assertFalse(psr.join(p7));
        assertEquals(6, psr.getUserSessions().size());
    }

    @Test
    public void testGetPlayingUsers() {
        IoSession p1 = mock(IoSession.class);
        IoSession p2 = mock(IoSession.class);
        IoSession p3 = mock(IoSession.class);
        IoSession p4 = mock(IoSession.class);
        IoSession p5 = mock(IoSession.class);
        IoSession p6 = mock(IoSession.class);
        IoSession p7 = mock(IoSession.class);

        PokerServerRoom psr = new PokerServerRoom(mock(PokerSaloon.class), p1,
                100);
        assertEquals(1, psr.getBettingPlayers().size());

        assertTrue(psr.join(p2));
        assertEquals(2, psr.getBettingPlayers().size());

        assertTrue(psr.join(p3));
        assertEquals(3, psr.getBettingPlayers().size());

        assertTrue(psr.join(p4));
        assertEquals(4, psr.getBettingPlayers().size());

        assertTrue(psr.join(p5));
        assertEquals(5, psr.getBettingPlayers().size());

        assertTrue(psr.join(p6));
        assertEquals(6, psr.getBettingPlayers().size());

        assertFalse(psr.join(p7));
        assertEquals(6, psr.getBettingPlayers().size());
    }

    @Test
    public void testSimpleCall() {
        IoSession p1 = mock(IoSession.class);
        IoSession p2 = mock(IoSession.class);
        IoSession p3 = mock(IoSession.class);
        IoSession p4 = mock(IoSession.class);

        User u1 = new User("p1");
        User u2 = new User("p2");
        User u3 = new User("p3");
        User u4 = new User("p4");

        PokerSaloon saloon = mock(PokerSaloon.class);

        when(saloon.getUser(p1)).thenReturn(u1);
        when(saloon.getUser(p2)).thenReturn(u2);
        when(saloon.getUser(p3)).thenReturn(u3);
        when(saloon.getUser(p4)).thenReturn(u4);

        PokerServerRoom psr = new PokerServerRoom(saloon, p1, 100);
        psr.join(p2);
        psr.join(p3);
        psr.join(p4);

        // startGame()
        psr.startGame();

        verify(p1).write(any(StartManoMessage.class));
        verify(p2).write(any(StartManoMessage.class));
        verify(p3).write(any(StartManoMessage.class));
        verify(p4).write(any(StartManoMessage.class));

        // call p1
        psr.call(p1);
        verify(p2).write(new CallMessage(u1, u2));
        verify(p3).write(new CallMessage(u1, u2));
        verify(p4).write(new CallMessage(u1, u2));

        // call p2
        psr.call(p2);
        verify(p1).write(new CallMessage(u2, u3));
        verify(p3).write(new CallMessage(u2, u3));
        verify(p4).write(new CallMessage(u2, u3));

        // call p3
        psr.call(p3);
        verify(p1).write(new CallMessage(u3, u4));
        verify(p2).write(new CallMessage(u3, u4));
        verify(p4).write(new CallMessage(u3, u4));

        // call p4
        psr.call(p4);
        verify(p1).write(new CallMessage(u4, null));
        verify(p2).write(new CallMessage(u4, null));
        verify(p3).write(new CallMessage(u4, null));

        // cartas
        verify(p1).write(any(FlopMessage.class));
        verify(p2).write(any(FlopMessage.class));
        verify(p3).write(any(FlopMessage.class));
        verify(p4).write(any(FlopMessage.class));
    }

    @Test
    public void testSimpleRaise() {
        IoSession p1 = mock(IoSession.class);
        IoSession p2 = mock(IoSession.class);
        IoSession p3 = mock(IoSession.class);
        IoSession p4 = mock(IoSession.class);

        User u1 = new User("p1");
        User u2 = new User("p2");
        User u3 = new User("p3");
        User u4 = new User("p4");

        PokerSaloon saloon = mock(PokerSaloon.class);

        when(saloon.getUser(p1)).thenReturn(u1);
        when(saloon.getUser(p2)).thenReturn(u2);
        when(saloon.getUser(p3)).thenReturn(u3);
        when(saloon.getUser(p4)).thenReturn(u4);

        PokerServerRoom psr = new PokerServerRoom(saloon, p1, 100);
        psr.join(p2);
        psr.join(p3);
        psr.join(p4);

        // startGame()
        psr.startGame();
        verify(p1).write(any(StartManoMessage.class));
        verify(p2).write(any(StartManoMessage.class));
        verify(p3).write(any(StartManoMessage.class));
        verify(p4).write(any(StartManoMessage.class));

        // raise p1
        psr.raise(p1, 10);
        verify(p2).write(new RaiseMessage(u1, 10, u2));
        verify(p3).write(new RaiseMessage(u1, 10, u2));
        verify(p4).write(new RaiseMessage(u1, 10, u2));

        // raise p2
        psr.raise(p2, 10);
        verify(p1).write(new RaiseMessage(u2, 10, u3));
        verify(p3).write(new RaiseMessage(u2, 10, u3));
        verify(p4).write(new RaiseMessage(u2, 10, u3));

        // raise p3
        psr.raise(p3, 10);
        verify(p1).write(new RaiseMessage(u3, 10, u4));
        verify(p2).write(new RaiseMessage(u3, 10, u4));
        verify(p4).write(new RaiseMessage(u3, 10, u4));

        // raise p4
        psr.raise(p4, 10);
        verify(p1).write(new RaiseMessage(u4, 10, u1));
        verify(p2).write(new RaiseMessage(u4, 10, u1));
        verify(p3).write(new RaiseMessage(u4, 10, u1));

        // call p1
        psr.call(p1);
        verify(p2).write(new CallMessage(u1, u2));
        verify(p3).write(new CallMessage(u1, u2));
        verify(p4).write(new CallMessage(u1, u2));

        // call p2
        psr.call(p2);
        verify(p1).write(new CallMessage(u2, u3));
        verify(p3).write(new CallMessage(u2, u3));
        verify(p4).write(new CallMessage(u2, u3));

        // call p3
        psr.call(p3);
        verify(p1).write(new CallMessage(u3, null));
        verify(p2).write(new CallMessage(u3, null));
        verify(p4).write(new CallMessage(u3, null));

        // cartas
        verify(p1).write(any(FlopMessage.class));
        verify(p2).write(any(FlopMessage.class));
        verify(p3).write(any(FlopMessage.class));
        verify(p4).write(any(FlopMessage.class));
    }

    @Test
    public void testComplexRaise() {
        IoSession p1 = mock(IoSession.class);
        IoSession p2 = mock(IoSession.class);
        IoSession p3 = mock(IoSession.class);
        IoSession p4 = mock(IoSession.class);

        User u1 = new User("p1");
        User u2 = new User("p2");
        User u3 = new User("p3");
        User u4 = new User("p4");

        PokerSaloon saloon = mock(PokerSaloon.class);

        when(saloon.getUser(p1)).thenReturn(u1);
        when(saloon.getUser(p2)).thenReturn(u2);
        when(saloon.getUser(p3)).thenReturn(u3);
        when(saloon.getUser(p4)).thenReturn(u4);

        PokerServerRoom psr = new PokerServerRoom(saloon, p1, 100);
        psr.join(p2);
        psr.join(p3);
        psr.join(p4);

        // startGame()
        psr.startGame();
        verify(p1).write(any(StartManoMessage.class));
        verify(p2).write(any(StartManoMessage.class));
        verify(p3).write(any(StartManoMessage.class));
        verify(p4).write(any(StartManoMessage.class));

        // raise p1
        psr.raise(p1, 10);
        verify(p2).write(new RaiseMessage(u1, 10, u2));
        verify(p3).write(new RaiseMessage(u1, 10, u2));
        verify(p4).write(new RaiseMessage(u1, 10, u2));

        // raise p2
        psr.raise(p2, 10);
        verify(p1).write(new RaiseMessage(u2, 10, u3));
        verify(p3).write(new RaiseMessage(u2, 10, u3));
        verify(p4).write(new RaiseMessage(u2, 10, u3));

        // raise p3
        psr.raise(p3, 10);
        verify(p1).write(new RaiseMessage(u3, 10, u4));
        verify(p2).write(new RaiseMessage(u3, 10, u4));
        verify(p4).write(new RaiseMessage(u3, 10, u4));

        // raise p4
        psr.raise(p4, 10);
        verify(p1).write(new RaiseMessage(u4, 10, u1));
        verify(p2).write(new RaiseMessage(u4, 10, u1));
        verify(p3).write(new RaiseMessage(u4, 10, u1));

        // call p1
        psr.call(p1);
        verify(p2).write(new CallMessage(u1, u2));
        verify(p3).write(new CallMessage(u1, u2));
        verify(p4).write(new CallMessage(u1, u2));

        // call p2
        psr.call(p2);
        verify(p1).write(new CallMessage(u2, u3));
        verify(p3).write(new CallMessage(u2, u3));
        verify(p4).write(new CallMessage(u2, u3));

        // call p3
        psr.call(p3);
        verify(p1).write(new CallMessage(u3, null));
        verify(p2).write(new CallMessage(u3, null));
        verify(p4).write(new CallMessage(u3, null));

        // cartas
        verify(p1).write(any(FlopMessage.class));
        verify(p2).write(any(FlopMessage.class));
        verify(p3).write(any(FlopMessage.class));
        verify(p4).write(any(FlopMessage.class));
    }

    @Test
    public void testLastFold() {
        IoSession p1 = mock(IoSession.class);
        IoSession p2 = mock(IoSession.class);
        IoSession p3 = mock(IoSession.class);

        PokerSaloon saloon = mock(PokerSaloon.class);

        User u1 = new User("p1");
        User u2 = new User("p2");
        User u3 = new User("p3");

        when(saloon.getUser(p1)).thenReturn(u1);
        when(saloon.getUser(p2)).thenReturn(u2);
        when(saloon.getUser(p3)).thenReturn(u3);

        PokerServerRoom psr = new PokerServerRoom(saloon, p1, 100);
        psr.join(p2);
        psr.join(p3);

        psr.startGame();

        verify(p1).write(any(StartManoMessage.class));
        verify(p2).write(any(StartManoMessage.class));
        verify(p3).write(any(StartManoMessage.class));

        psr.allIn(p1);
        psr.allIn(p2);

        verify(p1, times(2)).write(any(AllInMessage.class));
        verify(p2, times(2)).write(any(AllInMessage.class));
        verify(p3, times(2)).write(any(AllInMessage.class));

        psr.fold(p3);

        verify(p1).write(any(FoldMessage.class));
        verify(p2).write(any(FoldMessage.class));
        verify(p3).write(any(FoldMessage.class));

        verify(p1).write(any(FlopMessage.class));
        verify(p2).write(any(FlopMessage.class));
        verify(p3).write(any(FlopMessage.class));

        verify(p1).write(any(TurnMessage.class));
        verify(p2).write(any(TurnMessage.class));
        verify(p3).write(any(TurnMessage.class));

        verify(p1).write(any(RiverMessage.class));
        verify(p2).write(any(RiverMessage.class));
        verify(p3).write(any(RiverMessage.class));

        verify(p1).write(any(ShowdownMessage.class));
        verify(p2).write(any(ShowdownMessage.class));
        verify(p3).write(any(ShowdownMessage.class));

        verify(p2).write(any(FinJuegoMessage.class));
        verify(p2).write(any(UpdatedPointsMessage.class));

        verifyNoMoreInteractions(p1, p2, p3);
    }

    @Test
    public void testCasiLastFold() {
        IoSession p1 = mock(IoSession.class);
        IoSession p2 = mock(IoSession.class);
        IoSession p3 = mock(IoSession.class);
        IoSession p4 = mock(IoSession.class);

        PokerSaloon saloon = mock(PokerSaloon.class);

        User u1 = new User("p1");
        User u2 = new User("p2");
        User u3 = new User("p3");
        User u4 = new User("p4");

        when(saloon.getUser(p1)).thenReturn(u1);
        when(saloon.getUser(p2)).thenReturn(u2);
        when(saloon.getUser(p3)).thenReturn(u3);
        when(saloon.getUser(p4)).thenReturn(u4);

        PokerServerRoom psr = new PokerServerRoom(saloon, p1, 100);
        psr.join(p2);
        psr.join(p3);
        psr.join(p4);

        psr.startGame();

        verify(p1).write(any(StartManoMessage.class));
        verify(p2).write(any(StartManoMessage.class));
        verify(p3).write(any(StartManoMessage.class));
        verify(p4).write(any(StartManoMessage.class));

        psr.allIn(p1);
        psr.allIn(p2);

        verify(p1, times(2)).write(any(AllInMessage.class));
        verify(p2, times(2)).write(any(AllInMessage.class));
        verify(p3, times(2)).write(any(AllInMessage.class));
        verify(p4, times(2)).write(any(AllInMessage.class));

        psr.fold(p3);

        verify(p1).write(any(FoldMessage.class));
        verify(p2).write(any(FoldMessage.class));
        verify(p3).write(any(FoldMessage.class));
        verify(p4).write(any(FoldMessage.class));

        verifyNoMoreInteractions(p1, p2, p3, p4);
    }

    @Test
    public void testSubPozos() {
        // IoSession p1 = mock(IoSession.class);
        // IoSession p2 = mock(IoSession.class);
        // IoSession p3 = mock(IoSession.class);
        // IoSession p4 = mock(IoSession.class);
        //
        // PokerSaloon saloon = mock(PokerSaloon.class);
        //
        // User u1 = new User("p1");
        // User u2 = new User("p2");
        // User u3 = new User("p3");
        // User u4 = new User("p4");
        //
        // when(saloon.getUser(p1)).thenReturn(u1);
        // when(saloon.getUser(p2)).thenReturn(u2);
        // when(saloon.getUser(p3)).thenReturn(u3);
        // when(saloon.getUser(p4)).thenReturn(u4);
        //
        // PokerServerRoom psr = new PokerServerRoom(saloon, p1, 100);
        // psr.join(p2);
        // psr.join(p3);
        // psr.join(p4);
        //
        // // startGame()
        // psr.startGame();
        //
        // raiseAndCall(1, 10, psr, p2, p3, p4, p1);
        //
        // // los call
        // verify(p1, times(11)).write(any(CallMessage.class));
        // verify(p2, times(12)).write(any(CallMessage.class));
        // verify(p3, times(11)).write(any(CallMessage.class));
        // verify(p4, times(11)).write(any(CallMessage.class));
        //
        // verify(p3).write(any(RaiseMessage.class));
        // verify(p4).write(any(RaiseMessage.class));
        // verify(p1).write(any(RaiseMessage.class));
        //
        // // showdown
        // Hand ganadora = new Hand(new Card("6C"), new Card("6D"), new
        // Card("5C"),
        // new Card("5S"), new Card("TS"));
        //
        // String nombre = "Par doble de seis y cincos";
        //
        // PlayerTuple pt1 = new PlayerTuple(
        // new Hand(new Card("9H"), new Card("6C")), 130);
        // PlayerTuple pt2 = new PlayerTuple(
        // new Hand(new Card("JD"), new Card("2C")), 90);
        // PlayerTuple pt3 = new PlayerTuple(
        // new Hand(new Card("3S"), new Card("JH")), 90);
        // PlayerTuple pt4 = new PlayerTuple(
        // new Hand(new Card("KH"), new Card("9D")), 90);
        //
        // Map<User, PlayerTuple> map1 = Maps.newHashMap();
        // map1.put(u2, pt2);
        // map1.put(u3, pt3);
        // map1.put(u4, pt4);
        //
        // Map<User, PlayerTuple> map2 = Maps.newHashMap();
        // map2.put(u1, pt1);
        // map2.put(u3, pt3);
        // map2.put(u4, pt4);
        //
        // Map<User, PlayerTuple> map3 = Maps.newHashMap();
        // map3.put(u2, pt2);
        // map3.put(u1, pt1);
        // map3.put(u4, pt4);
        //
        // Map<User, PlayerTuple> map4 = Maps.newHashMap();
        // map4.put(u2, pt2);
        // map4.put(u1, pt1);
        // map4.put(u3, pt3);
        //
        // verify(p1).write(
        // new ShowdownMessage(ganadora, nombre, map1, u1, 130, false));
        // verify(p2).write(
        // new ShowdownMessage(ganadora, nombre, map2, u1, 90, true));
        // verify(p3).write(
        // new ShowdownMessage(ganadora, nombre, map3, u1, 90, false));
        // verify(p4).write(
        // new ShowdownMessage(ganadora, nombre, map4, u1, 90, false));
        //
        // // //////////////
        // // segunda mano
        // // //////////////
        // psr.proximaMano();
        //
        // raiseAndCall(2, 30, psr, p3, p4, p1, p2);
        //
        // // los call
        // verify(p1, times(22)).write(any(CallMessage.class));
        // verify(p2, times(23)).write(any(CallMessage.class));
        // verify(p3, times(23)).write(any(CallMessage.class));
        // verify(p4, times(22)).write(any(CallMessage.class));
        //
        // verify(p3).write(any(RaiseMessage.class));
        // verify(p4, times(2)).write(any(RaiseMessage.class));
        // verify(p1, times(2)).write(any(RaiseMessage.class));
        //
        // // showdown
        // ganadora = new Hand(new Card("AH"), new Card("JH"), new Card("9H"),
        // new Card("7H"), new Card("3H"));
        //
        // nombre = "Color al as";
        //
        // pt1.mano = new Hand(new Card("TH"), new Card("TC"));
        // pt1.fichas = 100;
        //
        // pt2.mano = new Hand(new Card("AH"), new Card("7H"));
        // pt2.fichas = 180;
        //
        // pt3.mano = new Hand(new Card("6S"), new Card("8D"));
        // pt3.fichas = 60;
        //
        // pt4.mano = new Hand(new Card("7S"), new Card("2H"));
        // pt4.fichas = 60;
        //
        // verify(p1).write(
        // new ShowdownMessage(ganadora, nombre, map1, u2, 100, false));
        // verify(p2).write(
        // new ShowdownMessage(ganadora, nombre, map2, u2, 180, false));
        // verify(p3).write(
        // new ShowdownMessage(ganadora, nombre, map3, u2, 60, true));
        // verify(p4).write(
        // new ShowdownMessage(ganadora, nombre, map4, u2, 60, false));
        //
        // //////////////
        // tercera mano
        // //////////////
        // psr.proximaMano();
        //
        // verify(p1, times(3)).write(any(StartManoMessage.class));
        // verify(p2, times(3)).write(any(StartManoMessage.class));
        // verify(p3, times(3)).write(any(StartManoMessage.class));
        // verify(p4, times(3)).write(any(StartManoMessage.class));
        //
        // // empieza p4
        // psr.fold(p4);
        // psr.raise(p1, 30);
        // psr.call(p2);
        // psr.call(p3);
        //
        // verify(p4, times(3)).write(any(RaiseMessage.class));
        // verify(p1, times(2)).write(any(RaiseMessage.class));
        // verify(p2, times(2)).write(any(RaiseMessage.class));
        //
        // verify(p1).write(any(FoldMessage.class));
        // verify(p2).write(any(FoldMessage.class));
        // verify(p3).write(any(FoldMessage.class));
        //
        // verify(p1, times(3)).write(any(FlopMessage.class));
        // verify(p2, times(3)).write(any(FlopMessage.class));
        // verify(p3, times(3)).write(any(FlopMessage.class));
        // verify(p4, times(3)).write(any(FlopMessage.class));
        //
        // // flop
        // psr.call(p3);
        // psr.call(p1);
        // psr.call(p2);
        //
        // verify(p1, times(3)).write(any(TurnMessage.class));
        // verify(p2, times(3)).write(any(TurnMessage.class));
        // verify(p3, times(3)).write(any(TurnMessage.class));
        // verify(p4, times(3)).write(any(TurnMessage.class));
        //
        // // turn
        // psr.call(p3);
        // psr.call(p1);
        // psr.call(p2);
        //
        // verify(p1, times(3)).write(any(RiverMessage.class));
        // verify(p2, times(3)).write(any(RiverMessage.class));
        // verify(p3, times(3)).write(any(RiverMessage.class));
        // verify(p4, times(3)).write(any(RiverMessage.class));
        //
        // // river
        // psr.call(p3);
        // psr.call(p1);
        // psr.call(p2);
        //
        // // los call
        // verify(p1, times(30)).write(any(CallMessage.class));
        // verify(p2, times(30)).write(any(CallMessage.class));
        // verify(p3, times(30)).write(any(CallMessage.class));
        // verify(p4, times(33)).write(any(CallMessage.class));
        //
        // // 3ra mano showdown
        // ganadora = new Hand(new Card("AD"), new Card("AH"), new Card("JC"),
        // new Card("JS"), new Card("8C"));
        //
        // nombre = "Par doble de Ases y J";
        //
        // pt2.mano = new Hand(new Card("3S"), new Card("6D"));
        // pt2.fichas = 150;
        //
        // pt3.mano = new Hand(new Card("6C"), new Card("8S"));
        // pt3.fichas = 30;
        //
        // pt3.mano = new Hand(new Card("8C"), new Card("AD"));
        // pt3.fichas = 120;
        //
        // pt4.mano = new Hand(new Card("TH"), new Card("TC"));
        // pt4.fichas = 100;
        //
        // map1.remove(u4);
        // map2.remove(u4);
        // map3.remove(u4);
        //
        // verify(p1).write(
        // new ShowdownMessage(ganadora, nombre, map1, u3, 150, false));
        // verify(p2).write(
        // new ShowdownMessage(ganadora, nombre, map2, u3, 30, false));
        // verify(p3).write(
        // new ShowdownMessage(ganadora, nombre, map3, u3, 120, false));
        // verify(p4).write(
        // new ShowdownMessage(ganadora, nombre, map4, u3, 100, false));
        //
        // // //////////////
        // // cuarta mano
        // // //////////////
        // psr.proximaMano();
        //
        // verify(p1, times(4)).write(any(StartManoMessage.class));
        // verify(p2, times(4)).write(any(StartManoMessage.class));
        // verify(p3, times(4)).write(any(StartManoMessage.class));
        // verify(p4, times(4)).write(any(StartManoMessage.class));
        //
        // // fichas
        // // p4: 100
        // // p1: 150
        // // p2: 30
        // // p3: 120
        // psr.fold(p4);
        //
        // verify(p1, times(2)).write(any(FoldMessage.class));
        // verify(p2, times(2)).write(any(FoldMessage.class));
        // verify(p3, times(2)).write(any(FoldMessage.class));
        //
        // psr.raise(p1, 10);
        // psr.allIn(p2);
        // psr.allIn(p3);
        // psr.call(p1);
        //
        // verify(p1, times(2)).write(any(AllInMessage.class));
        // verify(p3, times(1)).write(any(AllInMessage.class));
        //
        // verify(p1, times(4)).write(any(FlopMessage.class));
        // verify(p2, times(4)).write(any(FlopMessage.class));
        // verify(p3, times(4)).write(any(FlopMessage.class));
        // verify(p4, times(4)).write(any(FlopMessage.class));
        //
        // verify(p1, times(4)).write(any(TurnMessage.class));
        // verify(p2, times(4)).write(any(TurnMessage.class));
        // verify(p3, times(4)).write(any(TurnMessage.class));
        // verify(p4, times(4)).write(any(TurnMessage.class));
        //
        // verify(p1, times(4)).write(any(RiverMessage.class));
        // verify(p2, times(4)).write(any(RiverMessage.class));
        // verify(p3, times(4)).write(any(RiverMessage.class));
        // verify(p4, times(4)).write(any(RiverMessage.class));
        //
        // // los call
        // verify(p1, times(30)).write(any(CallMessage.class));
        // verify(p2, times(31)).write(any(CallMessage.class));
        // verify(p3, times(31)).write(any(CallMessage.class));
        // verify(p4, times(34)).write(any(CallMessage.class));
        //
        // // SUBPOZO SHOWDOWN!
        // ganadora = new Hand(new Card("5S"), new Card("5C"), new Card("AD"),
        // new Card("8H"), new Card("7S"));
        //
        // nombre = "Par de Cincos";
        //
        // pt1.mano = new Hand(new Card("9S"), new Card("JH"));
        // pt1.fichas = 30;
        //
        // pt2.mano = new Hand(new Card("3D"), new Card("AD"));
        // pt2.fichas = 90;
        //
        // pt3.mano = new Hand(new Card("JS"), new Card("KD"));
        // pt3.fichas = 180;
        //
        // pt4.mano = new Hand(new Card("4S"), new Card("2C"));
        // pt4.fichas = 100;
        //
        // verify(p1).write(
        // new ShowdownMessage(ganadora, nombre, map1, u2, 30, false));
        // verify(p2).write(
        // new ShowdownMessage(ganadora, nombre, map2, u2, 90, false));
        // verify(p3).write(
        // new ShowdownMessage(ganadora, nombre, map3, u2, 180, false));
        // verify(p4).write(
        // new ShowdownMessage(ganadora, nombre, map4, u2, 100, false));
    }

    @Test
    public void testAbandon4() {
        IoSession p1 = mock(IoSession.class);
        IoSession p2 = mock(IoSession.class);
        IoSession p3 = mock(IoSession.class);
        IoSession p4 = mock(IoSession.class);

        PokerSaloon saloon = mock(PokerSaloon.class);

        User u1 = new User("p1");
        User u2 = new User("p2");
        User u3 = new User("p3");
        User u4 = new User("p4");

        when(saloon.getUser(p1)).thenReturn(u1);
        when(saloon.getUser(p2)).thenReturn(u2);
        when(saloon.getUser(p3)).thenReturn(u3);
        when(saloon.getUser(p4)).thenReturn(u4);

        PokerServerRoom psr = new PokerServerRoom(saloon, p1, 100);
        psr.join(p2);
        psr.join(p3);
        psr.join(p4);

        psr.startGame();

        verify(p1).write(any(StartManoMessage.class));
        verify(p2).write(any(StartManoMessage.class));
        verify(p3).write(any(StartManoMessage.class));
        verify(p4).write(any(StartManoMessage.class));

        psr.abandon(p1);

        verify(p2).write(any(OponentAbandonedMessage.class));
        verify(p3).write(any(OponentAbandonedMessage.class));
        verify(p4).write(any(OponentAbandonedMessage.class));

        verify(p2, times(2)).write(any(TuTurnoMessage.class));

        verifyNoMoreInteractions(p1, p2, p3, p4);
    }

    @Test
    public void testAbandon3() {
        IoSession p1 = mock(IoSession.class);
        IoSession p2 = mock(IoSession.class);
        IoSession p3 = mock(IoSession.class);

        PokerSaloon saloon = mock(PokerSaloon.class);

        User u1 = new User("p1");
        User u2 = new User("p2");
        User u3 = new User("p3");

        when(saloon.getUser(p1)).thenReturn(u1);
        when(saloon.getUser(p2)).thenReturn(u2);
        when(saloon.getUser(p3)).thenReturn(u3);

        PokerServerRoom psr = new PokerServerRoom(saloon, p1, 100);
        psr.join(p2);
        psr.join(p3);

        psr.startGame();

        verify(p1).write(any(StartManoMessage.class));
        verify(p2).write(any(StartManoMessage.class));
        verify(p3).write(any(StartManoMessage.class));

        psr.abandon(p2);

        verify(p1).write(any(OponentAbandonedMessage.class));
        verify(p3).write(any(OponentAbandonedMessage.class));

        verify(p3, times(2)).write(any(TuTurnoMessage.class));

        verifyNoMoreInteractions(p1, p2, p3);
    }

    @Test
    public void testSplitPot() {
        // Deck seed debe ser 14
        IoSession p1 = mock(IoSession.class);
        IoSession p2 = mock(IoSession.class);
        IoSession p3 = mock(IoSession.class);
        IoSession p4 = mock(IoSession.class);

        PokerSaloon saloon = mock(PokerSaloon.class);

        User u1 = new User("p1");
        User u2 = new User("p2");
        User u3 = new User("p3");
        User u4 = new User("p4");

        when(saloon.getUser(p1)).thenReturn(u1);
        when(saloon.getUser(p2)).thenReturn(u2);
        when(saloon.getUser(p3)).thenReturn(u3);
        when(saloon.getUser(p4)).thenReturn(u4);

        PokerServerRoom psr = new PokerServerRoom(saloon, p1, 100);
        psr.join(p2);
        psr.join(p3);
        psr.join(p4);

        psr.startGame();

        verify(p1).write(any(StartManoMessage.class));
        verify(p2).write(any(StartManoMessage.class));
        verify(p3).write(any(StartManoMessage.class));
        verify(p4).write(any(StartManoMessage.class));

        psr.allIn(p1);
        psr.allIn(p2);
        psr.allIn(p3);
        psr.allIn(p4);

        verify(p1, times(4)).write(any(AllInMessage.class));
        verify(p2, times(4)).write(any(AllInMessage.class));
        verify(p3, times(4)).write(any(AllInMessage.class));
        verify(p4, times(4)).write(any(AllInMessage.class));

        verify(p1).write(any(FlopMessage.class));
        verify(p2).write(any(FlopMessage.class));
        verify(p3).write(any(FlopMessage.class));
        verify(p4).write(any(FlopMessage.class));

        verify(p1).write(any(TurnMessage.class));
        verify(p2).write(any(TurnMessage.class));
        verify(p3).write(any(TurnMessage.class));
        verify(p4).write(any(TurnMessage.class));

        verify(p1).write(any(RiverMessage.class));
        verify(p2).write(any(RiverMessage.class));
        verify(p3).write(any(RiverMessage.class));
        verify(p4).write(any(RiverMessage.class));

        verify(p1).write(any(ShowdownMessage.class));
        verify(p2).write(any(ShowdownMessage.class));
        verify(p3).write(any(ShowdownMessage.class));
        verify(p4).write(any(ShowdownMessage.class));

        verify(p1).write(any(FinJuegoMessage.class));
        verify(p4).write(any(FinJuegoMessage.class));

        verify(p1).write(any(UpdatedPointsMessage.class));
        verify(p4).write(any(UpdatedPointsMessage.class));

        verifyNoMoreInteractions(p1, p2, p3, p4);
    }

    @Test
    public void testSplitPotWithFold() {
        // Deck seed debe ser 14
        IoSession p1 = mock(IoSession.class);
        IoSession p2 = mock(IoSession.class);
        IoSession p3 = mock(IoSession.class);
        IoSession p4 = mock(IoSession.class);

        PokerSaloon saloon = mock(PokerSaloon.class);

        User u1 = new User("p1");
        User u2 = new User("p2");
        User u3 = new User("p3");
        User u4 = new User("p4");

        when(saloon.getUser(p1)).thenReturn(u1);
        when(saloon.getUser(p2)).thenReturn(u2);
        when(saloon.getUser(p3)).thenReturn(u3);
        when(saloon.getUser(p4)).thenReturn(u4);

        PokerServerRoom psr = new PokerServerRoom(saloon, p1, 100);
        psr.join(p2);
        psr.join(p3);
        psr.join(p4);

        psr.startGame();

        verify(p1).write(any(StartManoMessage.class));
        verify(p2).write(any(StartManoMessage.class));
        verify(p3).write(any(StartManoMessage.class));
        verify(p4).write(any(StartManoMessage.class));

        psr.raise(p1, 6);
        psr.call(p2);
        psr.call(p3);
        psr.call(p4);

        // flop
        psr.call(p1);
        psr.call(p2);
        psr.call(p3);
        psr.fold(p4);

        // turn
        psr.call(p1);
        psr.call(p2);
        psr.call(p3);
        // psr.call(p4);

        // river
        psr.call(p1);
        psr.call(p2);
        psr.call(p3);
    }

    @Test
    public void testJoin7() {
        IoSession elviejoestranza = mock(IoSession.class);
        IoSession koonek = mock(IoSession.class);
        IoSession prince_of_Pershia = mock(IoSession.class);
        IoSession homerolebron = mock(IoSession.class);
        final IoSession tappercinn = mock(IoSession.class);
        IoSession sleepwalker_84 = mock(IoSession.class);
        final IoSession p7 = mock(IoSession.class);

        final PokerServerRoom psr = new PokerServerRoom(
                mock(PokerSaloon.class), elviejoestranza, 100);

        psr.join(koonek);
        psr.join(prince_of_Pershia);
        psr.join(homerolebron);
        psr.join(tappercinn);
        psr.join(sleepwalker_84);

        // listo los 7

        Thread t1 = new Thread() {
            @Override
            public void run() {
                psr.abandon(tappercinn);
            };
        };

        Thread t2 = new Thread() {
            @Override
            public void run() {
                psr.join(p7);
            };
        };

        t1.start();
        t2.start();

        try {
            t1.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            t2.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test8Diciembre() {
        IoSession Rasec666 = mock(IoSession.class);
        IoSession gorcolocolo3 = mock(IoSession.class);
        IoSession cachungo = mock(IoSession.class);
        IoSession _ubu_ = mock(IoSession.class);

        PokerSaloon saloon = mock(PokerSaloon.class);
        when(saloon.getUser(Rasec666)).thenReturn(new User("Rasec666"));
        when(saloon.getUser(gorcolocolo3)).thenReturn(new User("gorcolocolo3"));
        when(saloon.getUser(cachungo)).thenReturn(new User("cachungo"));
        when(saloon.getUser(_ubu_)).thenReturn(new User("_ubu_"));

        PokerServerRoom psr = new PokerServerRoom(saloon, Rasec666, 0);

        psr.join(gorcolocolo3);
        psr.join(_ubu_);
        psr.join(cachungo);

        psr.startGame();

        psr.fold(Rasec666);
        psr.call(gorcolocolo3);
        psr.call(_ubu_);
        psr.call(cachungo);
        psr.allIn(gorcolocolo3);
    }

    @Test
    public void testAbandonoConMazo() {
        IoSession p1 = mock(IoSession.class);
        IoSession p2 = mock(IoSession.class);
        IoSession p3 = mock(IoSession.class);

        PokerSaloon saloon = mock(PokerSaloon.class);
        when(saloon.getUser(p1)).thenReturn(new User("p1"));
        when(saloon.getUser(p2)).thenReturn(new User("p2"));
        when(saloon.getUser(p3)).thenReturn(new User("p3"));

        PokerServerRoom psr = new PokerServerRoom(saloon, p1, 0);

        psr.join(p2);
        psr.join(p3);

        psr.startGame();
        verify(p1).write(any(StartManoMessage.class));
        verify(p2).write(any(StartManoMessage.class));
        verify(p3).write(any(StartManoMessage.class));

        psr.fold(p1);
        verify(p1).write(any(FoldMessage.class));
        verify(p2).write(any(FoldMessage.class));
        verify(p3).write(any(FoldMessage.class));

        psr.call(p2);
        verify(p1).write(any(CallMessage.class));
        verify(p2).write(any(CallMessage.class));
        verify(p3).write(any(CallMessage.class));

        psr.abandon(p3);
        verify(p1).write(any(OponentAbandonedMessage.class));
        verify(p2).write(any(OponentAbandonedMessage.class));

        verify(p1).write(any(AllFoldedMessage.class));
        verify(p2).write(any(AllFoldedMessage.class));

        verifyNoMoreInteractions(p1);
        verifyNoMoreInteractions(p2);
        verifyNoMoreInteractions(p3);
    }
}