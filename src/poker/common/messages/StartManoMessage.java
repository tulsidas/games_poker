package poker.common.messages;

import org.apache.mina.common.ByteBuffer;

import poker.common.ifaz.GameHandler;
import poker.common.ifaz.GameMessage;
import poker.common.model.Card;

import common.messages.VariableLengthMessageAdapter;
import common.model.User;

/**
 * Mensaje del server que da comienzo al juego
 */
public class StartManoMessage extends VariableLengthMessageAdapter implements
        GameMessage {

    private Card c1, c2;

    private User dealer;

    private int luz;

    public StartManoMessage() {
    }

    public StartManoMessage(Card c1, Card c2, User dealer, int luz) {
        super();
        this.c1 = c1;
        this.c2 = c2;
        this.dealer = dealer;
        this.luz = luz;
    }

    public void execute(GameHandler game) {
        game.startMano(c1, c2, dealer, luz);
    }

    @Override
    public String toString() {
        return "Start Hand: " + c1 + ", " + c2;
    }

    @Override
    public ByteBuffer encodedContent() {
        ByteBuffer buff = ByteBuffer.allocate(16);
        buff.setAutoExpand(true);

        Card.writeTo(c1, buff);
        Card.writeTo(c2, buff);

        User.writeTo(dealer, buff);
        
        buff.putInt(luz);

        return buff.flip();
    }

    @Override
    public void decode(ByteBuffer buff) {
        c1 = Card.readFrom(buff);
        c2 = Card.readFrom(buff);

        dealer = User.readFrom(buff);
        
        luz = buff.getInt();
    }

    @Override
    public byte getMessageId() {
        return (byte) 0x80;
    }
}
