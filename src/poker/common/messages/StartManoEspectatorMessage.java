package poker.common.messages;

import org.apache.mina.common.ByteBuffer;

import poker.common.ifaz.GameHandler;
import poker.common.ifaz.GameMessage;

import common.messages.VariableLengthMessageAdapter;
import common.model.User;

/**
 * Mensaje del server que da comienzo al juego
 */
public class StartManoEspectatorMessage extends VariableLengthMessageAdapter
        implements GameMessage {

    private User dealer;

    private int luz;

    public StartManoEspectatorMessage() {
    }

    public StartManoEspectatorMessage(User dealer, int luz) {
        this.dealer = dealer;
        this.luz = luz;
    }

    public void execute(GameHandler game) {
        game.startMano(dealer, luz);
    }

    @Override
    public String toString() {
        return "Start Hand";
    }

    @Override
    public ByteBuffer encodedContent() {
        ByteBuffer buff = ByteBuffer.allocate(16);
        buff.setAutoExpand(true);

        User.writeTo(dealer, buff);

        buff.putInt(luz);

        return buff.flip();
    }

    @Override
    public void decode(ByteBuffer buff) {
        dealer = User.readFrom(buff);
        luz = buff.getInt();
    }

    @Override
    public byte getMessageId() {
        return (byte) 0x96;
    }
}
