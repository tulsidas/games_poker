package poker.client;

import poker.common.model.PokerRoom;
import pulpcore.image.CoreImage;
import pulpcore.scene.Scene;
import pulpcore.sprite.ImageSprite;
import pulpcore.sprite.Sprite;
import client.AbstractGameConnector;
import client.AbstractLobbyScene;

import common.model.AbstractRoom;
import common.model.User;

/**
 * @author Tulsi
 */
public class LobbyScene extends AbstractLobbyScene {

   public LobbyScene(User user, AbstractGameConnector connection) {
      super(user, connection);
   }

   @Override
   protected boolean puedeCrearSala() {
      return true;
   }

   @Override
   protected Scene getGameScene(AbstractGameConnector connection, User usr,
         AbstractRoom room) {
      return new PokerScene((GameConnector) connection, usr, (PokerRoom) room);
   }

   @Override
   protected Sprite getGameImage() {
      return new ImageSprite(CoreImage.load("imgs/logo-poker.png"), 495, 10);
   }
}
