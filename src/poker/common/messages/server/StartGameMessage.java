package poker.common.messages.server;

import org.apache.mina.common.IoSession;

import poker.common.ifaz.PokerSaloonHandler;
import poker.common.messages.PokerClientGameMessage;

public class StartGameMessage extends PokerClientGameMessage {

   @Override
   public int getContentLength() {
      return 0;
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x92;
   }

   @Override
   public void execute(IoSession session, PokerSaloonHandler salon) {
      salon.startGame(session);
   }
   
   @Override
   public String toString() {
      return "Start Game";
   }
}
