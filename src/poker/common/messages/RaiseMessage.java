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

public class RaiseMessage extends VariableLengthMessageAdapter implements
      GameMessage, ClientGameMessage {

   protected User sender, next;

   protected int fichas;

   public RaiseMessage() {
   }

   public RaiseMessage(int fichas) {
      this.fichas = fichas;
   }

   public RaiseMessage(User user, int fichas, User next) {
      this.sender = user;
      this.fichas = fichas;
      this.next = next;
   }

   @Override
   public void execute(GameHandler game) {
      game.raise(sender, fichas, next);
   }

   @Override
   public void execute(IoSession session, BasicServerHandler salon) {
      execute(session, (PokerSaloonHandler) salon);
   }

   @Override
   public void execute(IoSession session, PokerSaloonHandler salon) {
      salon.raise(session, fichas);
   }

   @Override
   public String toString() {
      return sender + " raises " + fichas + " | next: " + next;
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x81;
   }

   @Override
   public void decode(ByteBuffer buff) {
      sender = User.readFrom(buff);
      fichas = buff.getInt();
      next = User.readFrom(buff);
   }

   @Override
   public ByteBuffer encodedContent() {
      ByteBuffer buf = ByteBuffer.allocate(32);
      buf.setAutoExpand(true);

      User.writeTo(sender, buf);
      buf.putInt(fichas);
      User.writeTo(next, buf);

      return buf.flip();
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + fichas;
      result = prime * result + ((next == null) ? 0 : next.hashCode());
      result = prime * result + ((sender == null) ? 0 : sender.hashCode());
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
      final RaiseMessage other = (RaiseMessage) obj;
      if (fichas != other.fichas)
         return false;
      if (next == null) {
         if (other.next != null)
            return false;
      }
      else if (!next.equals(other.next))
         return false;
      if (sender == null) {
         if (other.sender != null)
            return false;
      }
      else if (!sender.equals(other.sender))
         return false;
      return true;
   }

}
