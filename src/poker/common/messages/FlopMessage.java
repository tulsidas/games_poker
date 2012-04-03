package poker.common.messages;

import org.apache.mina.common.ByteBuffer;

import poker.common.ifaz.GameHandler;
import poker.common.ifaz.GameMessage;
import poker.common.model.Card;

import common.messages.VariableLengthMessageAdapter;
import common.model.User;

/**
 * Mensaje del server que da las 3 cartas de FLOP
 */
public class FlopMessage extends VariableLengthMessageAdapter implements
      GameMessage {

   private User next;

   private Card c1, c2, c3;

   public FlopMessage() {
   }

   public FlopMessage(Card c1, Card c2, Card c3, User next) {
      this.c1 = c1;
      this.c2 = c2;
      this.c3 = c3;

      this.next = next;
   }

   public void execute(GameHandler game) {
      game.flop(c1, c2, c3, next);
   }

   @Override
   public String toString() {
      return "Flop: " + c1 + ", " + c2 + ", " + c3;
   }

   @Override
   public ByteBuffer encodedContent() {
      ByteBuffer buff = ByteBuffer.allocate(16);
      buff.setAutoExpand(true);

      Card.writeTo(c1, buff);
      Card.writeTo(c2, buff);
      Card.writeTo(c3, buff);

      User.writeTo(next, buff);

      return buff.flip();
   }

   @Override
   public void decode(ByteBuffer buff) {
      c1 = Card.readFrom(buff);
      c2 = Card.readFrom(buff);
      c3 = Card.readFrom(buff);

      next = User.readFrom(buff);
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x84;
   }
}
