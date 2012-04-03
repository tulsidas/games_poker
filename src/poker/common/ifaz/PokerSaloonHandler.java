package poker.common.ifaz;

import org.apache.mina.common.IoSession;

/**
 * Interfaz de los mensajes que recibe el Saloon de los clientes
 */
public interface PokerSaloonHandler {

   void proximaMano(IoSession session);

   void raise(IoSession session, int fichas);

   void call(IoSession session);

   void allIn(IoSession session);

   void fold(IoSession session);

   void startGame(IoSession session);
}