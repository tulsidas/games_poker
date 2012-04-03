package poker.client;

import poker.common.model.Card;
import pulpcore.image.CoreImage;

public class Carder {
   private static CoreImage[] mias, pozo, otro;

   public enum Tipo {
      MIAS, POZO, OPONENTE
   }

   private Carder() {
   }

   static {
      // imagenes
      mias = CoreImage.load("imgs/mias.png").split(13, 4);
      pozo = CoreImage.load("imgs/pozo.png").split(13, 4);
      otro = CoreImage.load("imgs/otro.png").split(13, 4);
   }

   public static CoreImage getCartaImage(Card carta, Tipo tipo) {
      CoreImage[] imgs = tipo == Tipo.MIAS ? mias : tipo == Tipo.POZO ? pozo
            : otro;

      if (carta.getRank() != Card.ACE) {
         return imgs[carta.getIndex() + 1];
      }
      else {
         return imgs[carta.getIndex() - 12];
      }
   }
}
