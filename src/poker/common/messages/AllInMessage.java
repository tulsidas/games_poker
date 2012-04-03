package poker.common.messages;

import org.apache.mina.common.IoSession;

import poker.common.ifaz.ClientGameMessage;
import poker.common.ifaz.GameHandler;
import poker.common.ifaz.GameMessage;
import poker.common.ifaz.PokerSaloonHandler;

import common.ifaz.BasicServerHandler;
import common.model.User;

public class AllInMessage extends RaiseMessage implements GameMessage,
      ClientGameMessage {

   public AllInMessage() {
   }

   public AllInMessage(User user, int fichas, User next) {
      super(user, fichas, next);
   }

   @Override
   public void execute(GameHandler game) {
      game.allIn(sender, fichas, next);
   }

   @Override
   public void execute(IoSession session, BasicServerHandler salon) {
      execute(session, (PokerSaloonHandler) salon);
   }

   @Override
   public void execute(IoSession session, PokerSaloonHandler salon) {
      salon.allIn(session);
   }

   @Override
   public String toString() {
      return sender + " goes all-in | next: " + next;
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x93;
   }
}
