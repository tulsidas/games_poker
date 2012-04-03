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
public class TurnMessage extends VariableLengthMessageAdapter implements
      GameMessage {

   private User next;

   private Card turn;

   public TurnMessage() {
   }

   public TurnMessage(Card turn, User next) {
      this.turn = turn;
      this.next = next;
   }

   public void execute(GameHandler game) {
      game.turn(turn, next);
   }

   @Override
   public String toString() {
      return "TURN: " + turn;
   }

   @Override
   public ByteBuffer encodedContent() {
      ByteBuffer buff = ByteBuffer.allocate(16);
      buff.setAutoExpand(true);

      Card.writeTo(turn, buff);
      User.writeTo(next, buff);

      return buff.flip();
   }

   @Override
   public void decode(ByteBuffer buff) {
      turn = Card.readFrom(buff);
      next = User.readFrom(buff);
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x87;
   }
}
