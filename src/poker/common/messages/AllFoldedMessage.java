package poker.common.messages;

import org.apache.mina.common.ByteBuffer;

import poker.common.ifaz.GameHandler;
import poker.common.ifaz.GameMessage;

import common.messages.VariableLengthMessageAdapter;
import common.model.User;

public class AllFoldedMessage extends VariableLengthMessageAdapter implements
      GameMessage {

   private User ganador, next;

   public AllFoldedMessage() {
   }

   public AllFoldedMessage(User ganador, User next) {
      this.ganador = ganador;
      this.next = next;
   }

   @Override
   public void execute(GameHandler game) {
      game.allFolded(ganador, next);
   }

   @Override
   public String toString() {
      return "All Folded | next: " + next;
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x94;
   }

   @Override
   public void decode(ByteBuffer buff) {
      ganador = User.readFrom(buff);
      next = User.readFrom(buff);
   }

   @Override
   public ByteBuffer encodedContent() {
      ByteBuffer buf = ByteBuffer.allocate(32);
      buf.setAutoExpand(true);

      User.writeTo(ganador, buf);
      User.writeTo(next, buf);

      return buf.flip();
   }
}
