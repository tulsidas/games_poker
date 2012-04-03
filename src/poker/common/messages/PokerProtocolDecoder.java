package poker.common.messages;

import poker.common.messages.server.FinJuegoMessage;
import poker.common.messages.server.StartGameMessage;
import poker.common.messages.server.TuTurnoMessage;

import common.messages.TaringaProtocolDecoder;

public class PokerProtocolDecoder extends TaringaProtocolDecoder {

   public PokerProtocolDecoder() {
      classes
            .put(new StartManoMessage().getMessageId(), StartManoMessage.class);
      classes.put(new RaiseMessage().getMessageId(), RaiseMessage.class);
      classes.put(new CallMessage().getMessageId(), CallMessage.class);
      classes.put(new FoldMessage().getMessageId(), FoldMessage.class);
      classes.put(new FlopMessage().getMessageId(), FlopMessage.class);
      classes.put(new ProximaManoMessage().getMessageId(),
            ProximaManoMessage.class);
      classes.put(new TurnMessage().getMessageId(), TurnMessage.class);
      classes.put(new RiverMessage().getMessageId(), RiverMessage.class);
      classes.put(new ShowdownMessage().getMessageId(), ShowdownMessage.class);
      classes
            .put(new StartGameMessage().getMessageId(), StartGameMessage.class);
      classes.put(new AllInMessage().getMessageId(), AllInMessage.class);
      classes
            .put(new AllFoldedMessage().getMessageId(), AllFoldedMessage.class);
      classes.put(new FinJuegoMessage().getMessageId(), FinJuegoMessage.class);
      classes.put(new StartManoEspectatorMessage().getMessageId(),
            StartManoEspectatorMessage.class);
      classes.put(new TuTurnoMessage().getMessageId(), TuTurnoMessage.class);
   }
}
