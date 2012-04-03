package poker.common.messages;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.common.ByteBuffer;

import poker.common.ifaz.GameHandler;
import poker.common.ifaz.GameMessage;
import poker.common.model.Card;
import poker.common.model.Hand;

import common.messages.VariableLengthMessageAdapter;
import common.model.User;

public class ShowdownMessage extends VariableLengthMessageAdapter implements
      GameMessage {
   private Hand ganadora;

   private String nombre;

   private Map<User, PlayerTuple> tuplas;

   private List<User> ganadores; // XXX optimizable a List<String>

   private int fichas;

   private User next;

   public ShowdownMessage() {
   }

   public ShowdownMessage(Hand ganadora, String nombre,
         Map<User, PlayerTuple> manos, List<User> ganadores, int fichas,
         User next) {
      this.ganadora = ganadora;
      this.nombre = nombre;
      this.tuplas = manos;
      this.ganadores = ganadores;
      this.fichas = fichas;
      this.next = next;
   }

   public void execute(GameHandler game) {
      game.showdown(ganadora, nombre, tuplas, ganadores, fichas, next);
   }

   @Override
   public String toString() {
      String ret = "SHOWDOWN: " + ganadora + "(" + nombre + ")\n";

      for (Map.Entry<User, PlayerTuple> entry : tuplas.entrySet()) {
         ret += entry.getKey() + ":" + entry.getValue().mano + "|"
               + entry.getValue().fichas + "\n";
      }

      ret += "Ganadores " + ganadores + ", fichas: " + fichas; // XXX
      ret += "next: " + next;

      return ret;
   }

   @Override
   public ByteBuffer encodedContent() {
      ByteBuffer buff = ByteBuffer.allocate(64);
      buff.setAutoExpand(true);

      // 5 cartas ganadoras
      Card.writeTo(ganadora.getCard(1), buff);
      Card.writeTo(ganadora.getCard(2), buff);
      Card.writeTo(ganadora.getCard(3), buff);
      Card.writeTo(ganadora.getCard(4), buff);
      Card.writeTo(ganadora.getCard(5), buff);

      // el nombre de la mano ganadora
      CharsetEncoder enc = Charset.forName("UTF-8").newEncoder();
      // username
      try {
         buff.putPrefixedString(nombre, enc);
      }
      catch (CharacterCodingException e) {
         e.printStackTrace();
      }

      // las manos de los otros

      // tamaño
      buff.put((byte) tuplas.size());

      for (Map.Entry<User, PlayerTuple> entry : tuplas.entrySet()) {
         // user
         User.writeTo(entry.getKey(), buff);

         // cartas
         Card.writeTo(entry.getValue().mano.getCard(1), buff);
         Card.writeTo(entry.getValue().mano.getCard(2), buff);

         // fichas
         buff.putInt(entry.getValue().fichas);
      }

      // los ganadores
      buff.put((byte) ganadores.size());
      for (User ganador : ganadores) {
         User.writeTo(ganador, buff);
      }

      // mis fichas
      buff.putInt(fichas);

      // next
      User.writeTo(next, buff);

      return buff.flip();
   }

   @Override
   public void decode(ByteBuffer buff) {
      CharsetDecoder dec = Charset.forName("UTF-8").newDecoder();

      // mano ganadora
      ganadora = new Hand();
      ganadora.addCard(Card.readFrom(buff));
      ganadora.addCard(Card.readFrom(buff));
      ganadora.addCard(Card.readFrom(buff));
      ganadora.addCard(Card.readFrom(buff));
      ganadora.addCard(Card.readFrom(buff));

      // el nombre de la mano ganadora
      nombre = "?";
      try {
         nombre = buff.getPrefixedString(dec);
      }
      catch (CharacterCodingException e) {
         e.printStackTrace();
      }

      // las de los otros
      tuplas = new HashMap<User, PlayerTuple>();

      int num = buff.get();

      for (int i = 0; i < num; i++) {
         User u = User.readFrom(buff);
         Card c1 = Card.readFrom(buff);
         Card c2 = Card.readFrom(buff);
         int fichas = buff.getInt();

         Hand h = new Hand();
         h.addCard(c1);
         h.addCard(c2);

         PlayerTuple pt = new PlayerTuple(h, fichas);

         tuplas.put(u, pt);
      }

      // ganador
      int size = buff.get();
      ganadores = new ArrayList<User>();
      for (int i = 0; i < size; i++) {
         ganadores.add(User.readFrom(buff));
      }

      // mis fichas
      fichas = buff.getInt();

      // next
      next = User.readFrom(buff);
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x89;
   }

   public static class PlayerTuple {
      public Hand mano;

      public int fichas;

      public PlayerTuple(Hand mano, int fichas) {
         this.mano = mano;
         this.fichas = fichas;
      }

      @Override
      public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + fichas;
         result = prime * result + ((mano == null) ? 0 : mano.hashCode());
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
         final PlayerTuple other = (PlayerTuple) obj;
         if (fichas != other.fichas)
            return false;
         if (mano == null) {
            if (other.mano != null)
               return false;
         }
         else if (!mano.equals(other.mano))
            return false;
         return true;
      }
   }
}
