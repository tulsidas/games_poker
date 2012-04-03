package poker.client;

import pulpcore.scene.Scene;
import client.AbstractGameConnector;
import client.AbstractLoginScene;

import common.model.User;

/**
 * @author Tulsi
 */
public class LoginScene extends AbstractLoginScene {

   @Override
   protected AbstractGameConnector getGameConnector(String host, int salon,
         String user, String pass, long version) {
      return new GameConnector(host, getPort(), salon, user, pass, version);
   }

   @Override
   protected Scene getLobbyScene(User user, AbstractGameConnector connection) {
      return new LobbyScene(user, connection);
   }

   @Override
   protected int getPort() {
      return 8860;
   }
}