package poker.common.messages;

import org.apache.mina.common.IoSession;

import poker.common.ifaz.ClientGameMessage;
import poker.common.ifaz.PokerSaloonHandler;


import common.ifaz.BasicServerHandler;
import common.messages.FixedLengthMessageAdapter;

public abstract class PokerClientGameMessage extends FixedLengthMessageAdapter
        implements ClientGameMessage {
    public abstract void execute(IoSession session, PokerSaloonHandler salon);

    public void execute(IoSession session, BasicServerHandler serverHandler) {
        execute(session, (PokerSaloonHandler) serverHandler);
    }
}