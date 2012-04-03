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

public class CallMessage extends VariableLengthMessageAdapter implements
      GameMessage, ClientGameMessage {

   private User user, next;

   public CallMessage() {
   }

   public CallMessage(User user, User next) {
      this.user = user;
      this.next = next;
   }

   @Override
   public void execute(GameHandler game) {
      game.call(user, next);
   }

   @Override
   public void execute(IoSession session, BasicServerHandler salon) {
      execute(session, (PokerSaloonHandler) salon);
   }

   @Override
   public void execute(IoSession session, PokerSaloonHandler salon) {
      salon.call(session);
   }

   @Override
   public String toString() {
      return user + " calls | next: " + next;
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x82;
   }

   @Override
   public void decode(ByteBuffer buff) {
      user = User.readFrom(buff);
      next = User.readFrom(buff);
   }

   @Override
   public ByteBuffer encodedContent() {
      ByteBuffer buf = ByteBuffer.allocate(16);
      buf.setAutoExpand(true);

      User.writeTo(user, buf);
      User.writeTo(next, buf);

      return buf.flip();
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((next == null) ? 0 : next.hashCode());
      result = prime * result + ((user == null) ? 0 : user.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      final CallMessage other = (CallMessage) obj;
      if (next == null) {
         if (other.next != null)
            return false;
      }
      else if (!next.equals(other.next))
         return false;
      if (user == null) {
         if (other.user != null)
            return false;
      }
      else if (!user.equals(other.user))
         return false;
      return true;
   }
}
