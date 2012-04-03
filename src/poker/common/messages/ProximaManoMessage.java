package poker.common.messages;

import org.apache.mina.common.IoSession;

import poker.common.ifaz.ClientGameMessage;
import poker.common.ifaz.PokerSaloonHandler;


/**
 * Mensaje del server que da comienzo al proximo turno
 */
public class ProximaManoMessage extends PokerClientGameMessage implements
      ClientGameMessage {

   // @Override
   public void execute(IoSession session, PokerSaloonHandler salon) {
      salon.proximaMano(session);
   }

   @Override
   public String toString() {
      return "Proxima Mano";
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x86;
   }

   @Override
   public int getContentLength() {
      return 0;
   }
}