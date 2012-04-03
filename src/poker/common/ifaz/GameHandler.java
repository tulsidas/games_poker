package poker.common.ifaz;

import java.util.List;
import java.util.Map;

import poker.common.messages.ShowdownMessage.PlayerTuple;
import poker.common.model.Card;
import poker.common.model.Hand;

import common.ifaz.BasicGameHandler;
import common.model.User;

public interface GameHandler extends BasicGameHandler {
   // juego
   public void startMano(User dealer, int luz); // para los que perdieron

   // para los que juegan
   public void startMano(Card c1, Card c2, User dealer, int luz);

   public void tuTurno();

   // acciones
   public void call(User user, User next);

   public void raise(User user, int fichas, User next);

   public void fold(User user, User next);

   public void allIn(User user, int fichas, User next);

   public void allFolded(User ganador, User next);

   // cartas
   public void flop(Card c1, Card c2, Card c3, User next);

   public void turn(Card turn, User next);

   public void river(Card turn, User next);

   public void showdown(Hand ganadora, String nombre,
         Map<User, PlayerTuple> manos, List<User> ganadores, int fichas,
         User next);
}