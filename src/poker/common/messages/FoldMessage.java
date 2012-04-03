package poker.common.messages;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;

import poker.common.ifaz.ClientGameMessage;
import poker.common.ifaz.GameHandler;
import poker.common.ifaz.GameMessage;
import poker.common.ifaz.PokerSaloonHandler;

import common.ifaz.BasicServerHandler;
import common.messages.VariableLengthMessageAdapter;
import common.model.User;

public class FoldMessage extends VariableLengthMessageAdapter implements
      ClientGameMessage, GameMessage {

   private User user, next;

   public FoldMessage() {
   }

   public FoldMessage(User user, User next) {
      this.user = user;
      this.next = next;
   }

   @Override
   public void execute(IoSession session, BasicServerHandler salon) {
      execute(session, (PokerSaloonHandler) salon);
   }

   @Override
   public void execute(IoSession session, PokerSaloonHandler salon) {
      salon.fold(session);
   }

   @Override
   public void execute(GameHandler game) {
      game.fold(user, next);
   }

   @Override
   public String toString() {
      return user + " folds | next: " + next;
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x83;
   }

   @Override
   public void decode(ByteBuffer buff) {
      user = User.readFrom(buff);
      next = User.readFrom(buff);
   }

   @Override
   public ByteBuffer encodedContent() {
      ByteBuffer buf = ByteBuffer.allocate(32);
      buf.setAutoExpand(true);

      User.writeTo(user, buf);
      User.writeTo(next, buf);

      return buf.flip();
   }
}
