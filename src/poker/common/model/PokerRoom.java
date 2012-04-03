package poker.common.model;

import java.util.List;

import common.model.AbstractRoom;
import common.model.User;
import common.util.StringUtil;

public class PokerRoom extends AbstractRoom {

   public PokerRoom() {
   }

   public PokerRoom(int id, int puntosApostados, List<User> players) {
      super(id, puntosApostados, players);
   }

   @Override
   public String getDisplayText() {
      StringBuilder ret = new StringBuilder("[");
      List<User> players = getPlayers();
      if (players.size() > 0) {
         ret.append(StringUtil.truncate(players.get(0).getName(), 20));
      }
      if (players.size() > 1) {
         ret.append(" + " + (players.size() - 1));
      }
      ret.append("] x" + getPuntosApostados());

      return ret.toString();
   }


   @Override
   public boolean isFull() {
      return getPlayers().size() >= 6;
   }

   @Override
   public int compareTo(AbstractRoom o) {
      if (o.isStarted() == isStarted()) {
         return getPlayers().size() - o.getPlayers().size();
      }
      else if (o.isStarted() && !isStarted()) {
         return 1;
      }
      else {
         return -1;
      }
   }
}