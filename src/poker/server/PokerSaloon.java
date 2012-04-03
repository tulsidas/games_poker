package poker.server;

import org.apache.mina.common.IoSession;

import poker.common.ifaz.PokerSaloonHandler;
import server.AbstractSaloon;

import common.ifaz.POSTHandler;
import common.messages.server.GameStartedMessage;

public class PokerSaloon extends AbstractSaloon implements PokerSaloonHandler {

    public PokerSaloon(int id, POSTHandler poster) {
        super(id, poster);
    }

    @Override
    public void createRoom(IoSession session, int puntos) {
        createRoom(session, puntos, new PokerServerRoom(this, session, puntos));
    }

    // //////

    @Override
    protected PokerServerRoom getRoom(IoSession session) {
        // TODO si esto es null tira NPE, pero no parece molestar
        return (PokerServerRoom) super.getRoom(session);
    }

    @Override
    public void proximaMano(IoSession session) {
        getRoom(session).proximaMano();
    }

    @Override
    public void call(IoSession session) {
        getRoom(session).call(session);
    }

    @Override
    public void fold(IoSession session) {
        getRoom(session).fold(session);
    }

    @Override
    public void raise(IoSession session, int fichas) {
        getRoom(session).raise(session, fichas);
    }

    @Override
    public void allIn(IoSession session) {
        getRoom(session).allIn(session);
    }

    @Override
    public void startGame(IoSession session) {
        PokerServerRoom room = getRoom(session);
        room.startGame();

        // aviso al lobby que empezo el juego en esta sala
        broadcastLobby(new GameStartedMessage(room.getId()));
    }
}