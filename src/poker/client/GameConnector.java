package poker.client;

import java.util.List;
import java.util.Map;

import org.apache.mina.common.IoSession;

import poker.common.ifaz.GameHandler;
import poker.common.ifaz.GameMessage;
import poker.common.messages.PokerProtocolDecoder;
import poker.common.messages.ShowdownMessage.PlayerTuple;
import poker.common.model.Card;
import poker.common.model.Hand;
import client.AbstractGameConnector;

import common.model.User;

public class GameConnector extends AbstractGameConnector implements GameHandler {

    public GameConnector(String host, int port, int salon, String user,
            String pass, long version) {
        super(host, port, salon, user, pass, version,
                new PokerProtocolDecoder());
    }

    @Override
    public void messageReceived(IoSession sess, Object message) {
        super.messageReceived(sess, message);

        if (message instanceof GameMessage && gameHandler != null) {
            ((GameMessage) message).execute(this);
        }
    }

    // /////////////
    // GameHandler
    // /////////////
    @Override
    public void startMano(User dealer, int luz) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).startMano(dealer, luz);
        }
    }

    @Override
    public void startMano(Card c1, Card c2, User dealer, int luz) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).startMano(c1, c2, dealer, luz);
        }
    }

    @Override
    public void tuTurno() {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).tuTurno();
        }
    }

    @Override
    public void call(User user, User next) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).call(user, next);
        }
    }

    @Override
    public void allIn(User user, int fichas, User next) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).allIn(user, fichas, next);
        }
    }

    @Override
    public void fold(User user, User next) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).fold(user, next);
        }
    }

    @Override
    public void allFolded(User ganador, User next) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).allFolded(ganador, next);
        }
    }

    @Override
    public void raise(User user, int fichas, User next) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).raise(user, fichas, next);
        }
    }

    @Override
    public void flop(Card c1, Card c2, Card c3, User next) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).flop(c1, c2, c3, next);
        }
    }

    @Override
    public void turn(Card t, User next) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).turn(t, next);
        }
    }

    @Override
    public void river(Card r, User next) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).river(r, next);
        }
    }

    @Override
    public void showdown(Hand ganadora, String nombre,
            Map<User, PlayerTuple> manos, List<User> ganadores, int fichas,
            User next) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).showdown(ganadora, nombre, manos,
                    ganadores, fichas, next);
        }
    }
}