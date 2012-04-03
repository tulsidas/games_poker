package poker.client;

import pulpcore.animation.Easing;
import pulpcore.image.Colors;
import pulpcore.image.CoreFont;
import pulpcore.image.CoreImage;
import pulpcore.sprite.Group;
import pulpcore.sprite.ImageSprite;
import pulpcore.sprite.Label;
import client.PulpcoreUtils;

import common.model.User;

public class Player extends Group {
   private static final int[] POS_X = { 15, 154, 293, 432, 571 };

   User user;

   int fichas;

   int apuestaActual;

   boolean folded;

   private CoreFont font;

   private Label nombreLabel, fichasLabel;

   private ImageSprite carta1, carta2, button, bigBlind, smallBlind;

   public Player(User user, int pos) {
      super(380, -200);

      this.user = user;
      this.fichas = 100;

      this.x.animateTo(POS_X[pos], 1000, Easing.ELASTIC_IN_OUT);
      this.y.animateTo(58, 1000, Easing.ELASTIC_IN_OUT);

      button = new ImageSprite(CoreImage.load("imgs/button.png"), 95, 90);
      button.visible.set(false);
      add(button);

      bigBlind = new ImageSprite(CoreImage.load("imgs/big_blind.png"), 105, 90);
      bigBlind.visible.set(false);
      add(bigBlind);

      smallBlind = new ImageSprite(CoreImage.load("imgs/small_blind.png"), 105,
            90);
      smallBlind.visible.set(false);
      add(smallBlind);

      font = CoreFont.load("imgs/DIN13.font.png").tint(Colors.WHITE);

      nombreLabel = new Label(font, user.getName(), 0, 84);
      PulpcoreUtils.centerSprite(nombreLabel, 0, 134);
      add(nombreLabel);

      fichasLabel = new Label(font, "", 0, 94);
      updateFichas();
      add(fichasLabel);
   }

   public void setPos(int pos) {
      int r = (int) (300 + Math.random() * 500);
      x.animateTo(POS_X[pos], r, Easing.ELASTIC_IN_OUT);
   }

   public void updateFichas() {
      fichasLabel.setText(Integer.toString(fichas));
      PulpcoreUtils.centerSprite(fichasLabel, 0, 134);
   }

   public void showCards(CoreImage img1, CoreImage img2) {
      carta1 = new ImageSprite(img1, 10, 5);
      add(carta1);

      carta2 = new ImageSprite(img2, 75, 5);
      add(carta2);
   }

   public void sacarCartas() {
      remove(carta1);
      remove(carta2);
   }

   @Override
   public String toString() {
      return user.getName() + ": " + fichas + " (" + apuestaActual + ")";
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((user == null) ? 0 : user.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final Player other = (Player) obj;
      if (user == null) {
         if (other.user != null) {
            return false;
         }
      }
      else if (!user.equals(other.user)) {
         return false;
      }
      return true;
   }

   public void removeBlinds() {
      button.visible.set(false);
      bigBlind.visible.set(false);
      smallBlind.visible.set(false);
   }

   public void button() {
      button.visible.set(true);
   }

   public void smallBlind() {
      smallBlind.visible.set(true);
   }

   public void bigBlind() {
      bigBlind.visible.set(true);
   }

   public void fold() {
      folded = true;
      nombreLabel.alpha.animateTo(0x55, 1000);
      fichasLabel.alpha.animateTo(0x55, 1000);
   }

   public void unfold() {
      folded = false;
      nombreLabel.alpha.animateTo(0xFF, 500);
      fichasLabel.alpha.animateTo(0xFF, 500);
   }

   public void teToca(boolean b) {
      nombreLabel.setFont(b ? font.tint(Colors.YELLOW) : font
            .tint(Colors.WHITE));
   }
}