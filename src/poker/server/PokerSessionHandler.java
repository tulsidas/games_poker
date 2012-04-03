package poker.server;

import poker.common.messages.PokerProtocolDecoder;
import server.ServerSessionHandler;

import com.google.common.collect.Lists;

public class PokerSessionHandler extends ServerSessionHandler {

    public PokerSessionHandler() {
        super(new PokerProtocolDecoder());

        salones = Lists.newArrayList();
        salones.add(new PokerSaloon(0, this));
        salones.add(new PokerSaloon(1, this));
        salones.add(new PokerSaloon(2, this));
    }

    @Override
    protected int getCodigoJuego() {
        // poker = 7 para la base
        return 7;
    }
}