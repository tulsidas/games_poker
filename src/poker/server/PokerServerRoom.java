package poker.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.mina.common.IoSession;

import poker.common.messages.AllFoldedMessage;
import poker.common.messages.AllInMessage;
import poker.common.messages.CallMessage;
import poker.common.messages.FlopMessage;
import poker.common.messages.FoldMessage;
import poker.common.messages.RaiseMessage;
import poker.common.messages.RiverMessage;
import poker.common.messages.ShowdownMessage;
import poker.common.messages.StartManoEspectatorMessage;
import poker.common.messages.StartManoMessage;
import poker.common.messages.TurnMessage;
import poker.common.messages.ShowdownMessage.PlayerTuple;
import poker.common.messages.server.FinJuegoMessage;
import poker.common.messages.server.TuTurnoMessage;
import poker.common.model.Card;
import poker.common.model.Deck;
import poker.common.model.Hand;
import poker.common.model.PokerRoom;
import poker.server.model.HandEvaluator;
import server.AbstractServerRoom;
import server.db.RedisManager;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import common.game.OponentAbandonedMessage;
import common.messages.server.UpdatedPointsMessage;
import common.model.AbstractRoom;
import common.model.User;

public class PokerServerRoom extends AbstractServerRoom {

    private int luz = 0;

    private static final int BLIND = 2;

    private Deck deck;

    private int pot, apuestaActual;

    private int pozoFichas; // lo que gana el que gana el partido

    private List<PlayerData> players;

    /**
     * los jugadores que aún tienen fichas para apostar, o sea que no perdieron
     */
    private List<PlayerData> activePlayers;

    /**
     * los que ya perdieron esta mesa
     */
    private List<PlayerData> pierdePuntos;

    // la lista de los siguientes que les toca jugar (siguiente() ==
    // siguientes(0))
    private List<PlayerData> siguientes;

    private Lock playersLock = new ReentrantLock();

    private enum Stage {
        FLOP, TURN, RIVER, SHOWDOWN
    }

    private Stage stage;

    // estamos en juego juego? sacamos puntaje si uno abandona?
    private boolean gameOn;

    /**
     * @param player
     *            el jugador creador
     * @param puntos
     *            los puntos apostados
     */
    public PokerServerRoom(PokerSaloon salon, IoSession session, int fichas) {
        super(salon, fichas);

        this.gameOn = false;

        this.deck = new Deck();

        players = Collections.synchronizedList(new ArrayList<PlayerData>());
        players.add(new PlayerData(session, salon.getUser(session)));

        activePlayers = Collections.synchronizedList(Lists
                .newArrayList(players));

        pierdePuntos = Lists.newArrayList();
    }

    public void proxMano() {
        try {
            playersLock.lock();
            // actualizo los que siguen jugando
            updateActivePlayers();

            luz++;

            // envio game over a los que perdieron
            for (PlayerData p : getInactivePlayers()) {
                p.session.write(new FinJuegoMessage(false));
                p.session.write(new UpdatedPointsMessage(p.user.getPuntos()));
            }

            if (activePlayers.size() <= 1) {
                PlayerData ganador = Iterables.getOnlyElement(activePlayers);
                ganador.session.write(new FinJuegoMessage(true));

                // saco y doy a la vez
                RedisManager.sacarPuntos(Iterables.transform(players,
                        new Function<PlayerData, User>() {
                            @Override
                            public User apply(PlayerData pd) {
                                return pd.user;
                            }
                        }), getPuntosApostados());

                RedisManager.sacarPuntos(Iterables.transform(pierdePuntos,
                        new Function<PlayerData, User>() {
                            @Override
                            public User apply(PlayerData pd) {
                                return pd.user;
                            }
                        }), getPuntosApostados());

                RedisManager.darPuntos(ganador.user, pozoFichas);

                ganador.session.write(new UpdatedPointsMessage(ganador.user
                        .getPuntos()));

                setEnJuego(false);
                gameOn = false;
            }
            else {
                // cambio la mano
                Collections.rotate(activePlayers, -1);

                // reseteo apuestaActual
                for (PlayerData ply : activePlayers) {
                    ply.proxMano();
                }

                stage = Stage.FLOP;

                pot = 0;
                apuestaActual = 2 * BLIND;

                deck.reset();
                deck.shuffle();
                deck.shuffle();
            }
        }
        finally {
            playersLock.unlock();
        }
    }

    @Override
    public boolean isGameOn() {
        return gameOn;
    }

    public void startMano() {
        try {
            playersLock.lock();
            // jugando
            setEnJuego(true);

            stage = Stage.FLOP;

            pot = activePlayers.size() * luz;

            siguientes = Lists.newArrayList(activePlayers);
            User dealer = null;

            // blinds
            if (activePlayers.size() > 2) {
                // small blind
                PlayerData small = activePlayers.get(1);
                int smallB = Math.min(small.fichas, BLIND);
                small.apuesta(smallB);
                pot += smallB;

                // big blind
                PlayerData big = activePlayers.get(2);
                int bigB = Math.min(big.fichas, BLIND * 2);
                big.apuesta(bigB);
                pot += bigB;

                // dealer = saloon
                // .getUser(activePlayers.get(3 %
                // activePlayers.size()).session);
                dealer = saloon.getUser(activePlayers.get(0).session);
                Collections.rotate(siguientes, -3 % activePlayers.size());
            }
            else if (activePlayers.size() == 2) {
                // small blind
                PlayerData small = activePlayers.get(0);
                int smallB = Math.min(small.fichas, BLIND);
                small.apuesta(smallB);
                pot += smallB;

                // big blind
                PlayerData big = activePlayers.get(1);
                int bigB = Math.min(big.fichas, BLIND * 2);
                big.apuesta(bigB);
                pot += bigB;

                dealer = saloon.getUser(activePlayers.get(0).session);
            }

            // luz
            apuestaActual += luz;
            for (PlayerData ply : activePlayers) {
                ply.apuesta(luz);

                Hand hand = new Hand();
                hand.addCard(deck.deal());
                hand.addCard(deck.deal());

                ply.hand = hand;
                ply.session.write(new StartManoMessage(hand.getCard(1), hand
                        .getCard(2), dealer, luz));
            }

            // mando a los espectadores
            for (PlayerData ply : getInactivePlayers()) {
                ply.session.write(new StartManoEspectatorMessage(dealer, luz));
            }
        }
        finally {
            playersLock.unlock();
        }
    }

    public void proximaMano() {
        startMano();
    }

    /**
     * @return a cuantos puntos se juega en esta sala
     */
    public int getPuntajeJuego() {
        return puntosApostados;
    }

    @Override
    public AbstractRoom createRoom() {
        try {
            playersLock.lock();
            return new PokerRoom(getId(), puntosApostados, getUsers());
        }
        finally {
            playersLock.unlock();
        }
    }

    @Override
    public void startGame() {
        try {
            playersLock.lock();

            pozoFichas = getPuntosApostados() * players.size();

            // XXX RedisManager.sacarPuntos(Iterables.transform(players,
            // new Function<PlayerData, User>() {
            // @Override
            // public User apply(PlayerData pd) {
            // return pd.user;
            // }
            // }), getPuntosApostados());

            for (PlayerData ply : players) {
                ply.user.setPuntos(ply.user.getPuntos() - apuestaActual);
            }

            setStarted(true);
            gameOn = true;
            proxMano();
            startMano();
        }
        finally {
            playersLock.unlock();
        }
    }

    public void raise(IoSession sender, int sube) {
        try {
            playersLock.lock();
            PlayerData pd = getPlayerData(sender);
            if (sube <= pd.fichas) {
                // paga la diferencia de la apuesta actual + lo que sube
                int fichas = (apuestaActual - pd.apuestaActual) + sube;
                pd.pagar(fichas);
                pot += fichas;

                apuestaActual += sube;

                List<PlayerData> bettingPlayers = getBettingPlayers();

                // pongo al resto en called = false
                for (PlayerData ply : bettingPlayers) {
                    if (ply != pd) {
                        ply.called = false;
                    }
                }

                int nextIndex = (bettingPlayers.indexOf(pd) + 1)
                        % bettingPlayers.size();

                Collections.rotate(bettingPlayers, -nextIndex);
                siguientes = bettingPlayers;

                // aviso al resto
                multicast(new RaiseMessage(saloon.getUser(sender), sube, saloon
                        .getUser(siguiente().session)));
            }
            else {
                throw new IllegalArgumentException(
                        "raise y no le alcanza a pagar");
            }
        }
        finally {
            playersLock.unlock();
        }
    }

    public void allIn(IoSession sender) {
        try {
            playersLock.lock();
            List<PlayerData> bettingPlayers = getBettingPlayers();

            PlayerData pd = getPlayerData(sender);
            int fichas = pd.fichas;
            pd.fichas -= fichas;
            pd.called = true;

            int loQueSube = fichas - (apuestaActual - pd.apuestaActual);
            pot += fichas;

            if (loQueSube > 0) {
                // el allIn sube la apuesta actual
                apuestaActual += loQueSube;
                pd.apuestaActual = apuestaActual;

                // pongo al resto en called = false
                for (PlayerData ply : bettingPlayers) {
                    if (ply != pd) {
                        ply.called = false;
                    }
                }
            }
            else {
                // el allIn no alcanza a la apuesta actual
                pd.apuestaActual += fichas; // ???
            }

            if (todosPagaron()) {
                // aviso al resto
                multicast(new AllInMessage(saloon.getUser(sender), fichas, null));

                sendCall(null);
            }
            else {
                int nextIndex = (bettingPlayers.indexOf(pd) + 1)
                        % bettingPlayers.size();

                Collections.rotate(bettingPlayers, -nextIndex);
                siguientes = bettingPlayers;

                // aviso al resto
                multicast(new AllInMessage(saloon.getUser(sender), fichas,
                        saloon.getUser(siguiente().session)));
            }
        }
        finally {
            playersLock.unlock();
        }
    }

    public void call(IoSession sender) {
        try {
            playersLock.lock();
            PlayerData pd = getPlayerData(sender);
            // paga la diferencia de la apuesta actual + lo que sube
            int abajo = apuestaActual - pd.apuestaActual;
            if (pd.fichas > 0) { // deberia ser siempre > abajo
                if (pd.fichas < abajo) {
                    throw new IllegalArgumentException(
                            "call y no le alcanza a pagar");
                }
                pd.pagar(abajo);
                pot += abajo;
            }
            else {
                // all in
                pd.called = true;
            }

            sendCall(sender);
        }
        finally {
            playersLock.unlock();
        }
    }

    public void fold(IoSession sender) {
        try {
            playersLock.lock();

            PlayerData player = getPlayerData(sender);
            player.folded = true;

            List<PlayerData> playingPlayers = getPlayingPlayers();

            if (playingPlayers.size() == 1) { // queda uno solo
                // mando el fold
                multicast(new FoldMessage(saloon.getUser(sender), null));

                // el que quedo gana el pozo
                PlayerData ganador = Iterables.getOnlyElement(playingPlayers);
                ganador.fichas += pot;

                siguientes = Lists.newArrayList(activePlayers);

                // mando AllFolded al resto
                multicast(new AllFoldedMessage(saloon.getUser(ganador.session),
                        saloon.getUser(siguiente().session)));

                proxMano();
            }
            else {
                User u = saloon.getUser(sender);

                if (todosPagaron()) {
                    // mando el fold a todos
                    multicast(new FoldMessage(u, null));

                    // siguiente mano
                    sendCall(null);
                }
                else {
                    // obtengo el siguiente de los apostadores
                    List<PlayerData> players = Lists
                            .newArrayList(activePlayers);
                    Collections.rotate(players, -players.indexOf(player));

                    siguientes = Lists.newArrayList(Iterables.filter(players,
                            new Predicate<PlayerData>() {
                                public boolean apply(PlayerData pd) {
                                    return !pd.folded && pd.fichas > 0;
                                }
                            }));

                    // mando el fold al resto
                    multicast(new FoldMessage(u, saloon
                            .getUser(siguiente().session)));
                }
            }
        }
        finally {
            playersLock.unlock();
        }

    }

    private void sendCall(IoSession sender) {
        if (todosPagaron()) {
            if (sender != null) {
                // mando el call
                multicast(new CallMessage(saloon.getUser(sender), null));
            }

            if (stage == Stage.FLOP) {
                Card c1 = deck.deal();
                Card c2 = deck.deal();
                Card c3 = deck.deal();

                siguientes = getBettingPlayers();

                for (PlayerData ply : players) {
                    ply.hand.addCard(c1);
                    ply.hand.addCard(c2);
                    ply.hand.addCard(c3);

                    // mandar al primer jugador que no este all in
                    // si estan todos all in, mandar las otras cartas
                    if (siguientes.size() > 1) {
                        // reset para la proxima mano
                        ply.called = false;

                        ply.session.write(new FlopMessage(c1, c2, c3, saloon
                                .getUser(siguiente().session)));
                    }
                    else {
                        ply.session.write(new FlopMessage(c1, c2, c3, null));
                    }
                }

                // new stage
                stage = Stage.TURN;

                if (siguientes.size() <= 1) {
                    sendCall(null);
                }
            }
            else if (stage == Stage.TURN) {
                Card turn = deck.deal();

                siguientes = getBettingPlayers();

                for (PlayerData ply : players) {
                    ply.hand.addCard(turn);

                    if (siguientes.size() > 1) {
                        // reset para la proxima mano
                        ply.called = false;

                        ply.session.write(new TurnMessage(turn, saloon
                                .getUser(siguiente().session)));
                    }
                    else {
                        ply.session.write(new TurnMessage(turn, null));
                    }
                }

                // new stage
                stage = Stage.RIVER;

                if (siguientes.size() <= 1) {
                    sendCall(null);
                }
            }
            else if (stage == Stage.RIVER) {
                Card river = deck.deal();

                siguientes = getBettingPlayers();

                for (PlayerData ply : players) {
                    ply.hand.addCard(river);

                    if (siguientes.size() > 1) {
                        // reset para la proxima mano
                        ply.called = false;

                        ply.session.write(new RiverMessage(river, saloon
                                .getUser(siguiente().session)));
                    }
                    else {
                        ply.session.write(new RiverMessage(river, null));
                    }
                }

                // new stage
                stage = Stage.SHOWDOWN;

                if (siguientes.size() <= 1) {
                    sendCall(null);
                }
            }
            else if (stage == Stage.SHOWDOWN) {
                List<PlayerData> currentPlayers = getPlayingPlayers();
                // List<PlayerData> currentPlayers =
                // Lists.newArrayList(activePlayers);

                // FIXME tambien tener en cuenta los que abandonaron la
                // partida

                // calculo los rank de todos
                for (PlayerData ply : currentPlayers) {
                    ply.handRank = HandEvaluator.rankHand(ply.hand);
                }

                // obtengo ganadores absolutos

                while (!currentPlayers.isEmpty()) {
                    // ordeno por mano de mayor a menor
                    Collections.sort(currentPlayers,
                            new Comparator<PlayerData>() {
                                public int compare(PlayerData p1, PlayerData p2) {
                                    return p2.handRank - p1.handRank;
                                }
                            });

                    final int maxHandRank = currentPlayers.get(0).handRank;

                    List<PlayerData> ganadores = Lists.newArrayList(Iterables
                            .filter(currentPlayers,
                                    new Predicate<PlayerData>() {
                                        public boolean apply(PlayerData pd) {
                                            return pd.handRank == maxHandRank;
                                        }
                                    }));

                    if (ganadores.size() == 1) {
                        PlayerData ganador = ganadores.get(0);

                        int apuestaGanadora = ganador.apuestaActual;

                        for (PlayerData ply : activePlayers) {
                            int subpozo = Math.min(ply.apuestaActual,
                                    apuestaGanadora);
                            ganador.fichas += subpozo;
                            ply.apuestaActual -= subpozo;
                        }

                        // saco al ganador y a los que quedaron en 0
                        currentPlayers.remove(ganador);
                        currentPlayers.removeAll(Lists.newArrayList(Iterables
                                .filter(currentPlayers,
                                        new Predicate<PlayerData>() {
                                            @Override
                                            public boolean apply(PlayerData pd) {
                                                return pd.apuestaActual == 0;
                                            }
                                        })));
                    }
                    else { // XXX codigo optimizable / reusable
                        // split pot

                        List<PlayerData> perdedores = Lists
                                .newArrayList(activePlayers);
                        perdedores.removeAll(ganadores);

                        // le asigno lo que le corresponde a cada ganador
                        // del resto
                        for (PlayerData loser : perdedores) {
                            int splitPot = loser.apuestaActual
                                    / ganadores.size();

                            for (PlayerData winner : ganadores) {
                                int subpozo = Math.min(winner.apuestaActual,
                                        splitPot);

                                // gana la division de lo que puso el
                                // perdedor
                                winner.fichas += subpozo;
                                loser.apuestaActual -= subpozo;
                            }
                        }

                        // lo que aposto cada ganador es suyo
                        for (PlayerData winner : ganadores) {
                            winner.fichas += winner.apuestaActual;
                            winner.apuestaActual = 0;
                        }

                        // saco a los ganador y a los que quedaron en 0
                        currentPlayers.removeAll(ganadores);
                        currentPlayers.removeAll(Lists.newArrayList(Iterables
                                .filter(currentPlayers,
                                        new Predicate<PlayerData>() {
                                            @Override
                                            public boolean apply(PlayerData pd) {
                                                return pd.apuestaActual == 0;
                                            }
                                        })));
                    }
                }

                siguientes = Lists.newArrayList(activePlayers);
                siguientes.removeAll(getPerdedores());

                // XXX suboptimisimo
                List<PlayerData> playingPlayers = getPlayingPlayers();
                Collections.sort(playingPlayers, new Comparator<PlayerData>() {
                    public int compare(PlayerData p1, PlayerData p2) {
                        return p2.handRank - p1.handRank;
                    }
                });

                final int maxHandRank = playingPlayers.get(0).handRank;
                Hand ganadora = HandEvaluator.getBest5CardHand(playingPlayers
                        .get(0).hand);

                Iterable<PlayerData> plyGanadores = Iterables.filter(
                        playingPlayers, new Predicate<PlayerData>() {
                            public boolean apply(PlayerData pd) {
                                return pd.handRank == maxHandRank;
                            }
                        });

                List<User> ganadores = Lists.newArrayList(Iterables.transform(
                        plyGanadores, new Function<PlayerData, User>() {
                            public User apply(PlayerData ply) {
                                return ply.user;
                            }
                        }));

                // mando a cada user un mensaje específico con las manos de
                // los otros
                for (PlayerData ply : players) {
                    Map<User, PlayerTuple> manos = Maps.newHashMap();

                    List<PlayerData> otherPlayers = getPlayingPlayers();
                    otherPlayers.remove(ply);

                    // armo el map de manos
                    for (PlayerData other : otherPlayers) {
                        Hand h = new Hand();
                        h.addCard(other.hand.getCard(1));
                        h.addCard(other.hand.getCard(2));

                        manos.put(other.user, new PlayerTuple(h, other.fichas));
                    }

                    ply.session.write(new ShowdownMessage(ganadora,
                            HandEvaluator.nameHand(ganadora), manos, ganadores,
                            ply.fichas, saloon.getUser(siguiente().session)));
                }

                proxMano();
            }
        }
        else {
            // aviso al que le toca
            PlayerData pd = getPlayerData(sender);

            List<PlayerData> bettingPlayers = getBettingPlayers();
            int nextIndex = (bettingPlayers.indexOf(pd) + 1)
                    % bettingPlayers.size();

            Collections.rotate(bettingPlayers, -nextIndex);
            siguientes = bettingPlayers;

            // mando el call
            multicast(new CallMessage(saloon.getUser(sender), saloon
                    .getUser(siguiente().session)));
        }
    }

    private PlayerData getPlayerData(final IoSession session) {
        try {
            playersLock.lock();
            return Iterables.find(players, new Predicate<PlayerData>() {
                public boolean apply(PlayerData pd) {
                    return pd.session == session;
                }
            });
        }
        finally {
            playersLock.unlock();
        }
    }

    /**
     * @return los jugadores que no se fueron al mazo y tienen fichas para
     *         apostar(@copy)
     */
    // protected para el test, podría ser private
    protected List<PlayerData> getBettingPlayers() {
        try {
            playersLock.lock();
            return Lists.newArrayList(Iterables.filter(activePlayers,
                    new Predicate<PlayerData>() {
                        public boolean apply(PlayerData pd) {
                            return !pd.folded && pd.fichas > 0;
                        }
                    }));
        }
        finally {
            playersLock.unlock();
        }
    }

    /**
     * @return los jugadores que no se fueron al mazo
     */
    protected List<PlayerData> getPlayingPlayers() {
        try {
            playersLock.lock();
            return Lists.newArrayList(Iterables.filter(activePlayers,
                    new Predicate<PlayerData>() {
                        public boolean apply(PlayerData pd) {
                            return !pd.folded;
                        }
                    }));
        }
        finally {
            playersLock.unlock();
        }
    }

    private List<PlayerData> getInactivePlayers() {
        List<PlayerData> ret = Lists.newArrayList(players);
        ret.removeAll(activePlayers);

        return ret;
    }

    private List<PlayerData> getPerdedores() {
        try {
            playersLock.lock();
            return Lists.newArrayList(Iterables.filter(activePlayers,
                    new Predicate<PlayerData>() {
                        public boolean apply(PlayerData pd) {
                            return pd.fichas == 0;
                        }
                    }));
        }
        finally {
            playersLock.unlock();
        }
    }

    private void updateActivePlayers() {
        try {
            playersLock.lock();
            Iterables.removeAll(activePlayers, getPerdedores());
        }
        finally {
            playersLock.unlock();
        }
    }

    /**
     * @return si todos los jugadores pagaron la apuesta actual o se fueron al
     *         mazo o estan all in
     */
    private boolean todosPagaron() {
        try {
            playersLock.lock();
            return Iterables.all(activePlayers, new Predicate<PlayerData>() {
                public boolean apply(PlayerData pd) {
                    return pd.called || pd.folded || pd.fichas == 0;
                }
            });
        }
        finally {
            playersLock.unlock();
        }
    }

    private PlayerData siguiente() {
        return siguientes.get(0);
    }

    // //////////////////////////
    // AbstractServerRoom methods
    // //////////////////////////
    @Override
    public void abandon(IoSession session) {
        try {
            playersLock.lock();

            PlayerData pd = getPlayerData(session);

            players.remove(pd);
            activePlayers.remove(pd);

            multicast(new OponentAbandonedMessage(isEnJuego(), saloon
                    .getUser(session)), session);

            if (siguientes != null && siguientes.size() > 0) {
                PlayerData siguiente = siguiente();

                siguientes.remove(pd);

                if (isEnJuego() && gameOn) {
                    // lo guardo para transferir puntos al final
                    pierdePuntos.add(pd);

                    // se fue el que le tocaba jugar
                    if (pd == siguiente) {
                        // paso el turno al que sigue
                        if (siguientes.size() > 1) {
                            siguiente().session.write(new TuTurnoMessage());
                        }
                    }

                    // se fueron todos, gameover
                    if (activePlayers.size() == 1) {
                        PlayerData ganador = Iterables
                                .getOnlyElement(activePlayers);
                        ganador.session.write(new FinJuegoMessage(true));
                        // TODO transfer points

                        ganador.session.write(new UpdatedPointsMessage(
                                ganador.user.getPuntos()
                                        + (pierdePuntos.size() + 1)
                                        * apuestaActual));
                    }
                    else {
                        // pasar turno
                        if (siguientes.size() > 1) {
                            siguiente().session.write(new TuTurnoMessage());
                        }
                        else {
                            // el que quedo gana el pozo
                            PlayerData ganador = siguiente();
                            ganador.fichas += pot;

                            siguientes = Lists.newArrayList(activePlayers);

                            // mando AllFolded al resto
                            multicast(new AllFoldedMessage(saloon
                                    .getUser(ganador.session), saloon
                                    .getUser(siguiente().session)));
                            proxMano();
                        }
                    }
                }
            }
        }
        finally {
            playersLock.unlock();
        }

    }

    @Override
    public int getMinimumPlayers() {
        return 1;
    }

    @Override
    public int getMinimumPlayingPlayers() {
        return 2;
    }

    @Override
    public Collection<IoSession> getUserSessions() {
        try {
            playersLock.lock();
            return Lists.newArrayList(Iterables.transform(players,
                    new Function<PlayerData, IoSession>() {
                        public IoSession apply(PlayerData pd) {
                            return pd.session;
                        }
                    }));
        }
        finally {
            playersLock.unlock();
        }
    }

    @Override
    public boolean isComplete() {
        return getUserSessions().size() == 6;
    }

    @Override
    public boolean join(IoSession session) {
        try {
            playersLock.lock();
            if (isComplete() || isStarted() || hasUser(session)) {
                return false;
            }
            else {
                PlayerData pd = new PlayerData(session, saloon.getUser(session));
                players.add(pd);
                activePlayers.add(pd);
                return true;
            }
        }
        finally {
            playersLock.unlock();
        }
    }

    @Override
    public void proximoJuego(IoSession session, boolean acepta) {
        throw new UnsupportedOperationException("proximoJuego");
    }

    private static class PlayerData {
        IoSession session;

        User user;

        Hand hand;

        int handRank;

        int fichas, apuestaActual;

        boolean folded, called;

        public PlayerData(IoSession session, User user) {
            this.session = session;
            this.user = user;
            this.fichas = 100;
            this.folded = false;
            this.called = false;
        }

        public void apuesta(int fichas) {
            this.fichas -= fichas;
            this.apuestaActual += fichas;
        }

        public void pagar(int fichas) {
            apuesta(fichas);
            this.called = true;
        }

        public void proxMano() {
            apuestaActual = 0;
            folded = false;
            called = false;
        }

        @Override
        public String toString() {
            return user.getName() + ": " + fichas + " (" + apuestaActual + ")";
        }
    }

    @Override
    public void joined(IoSession session) {
    }
}
