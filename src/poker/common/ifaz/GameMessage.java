package poker.common.ifaz;

public interface GameMessage {
    // mensaje del server se ejecuta en el cliente
    public void execute(GameHandler game);
}
