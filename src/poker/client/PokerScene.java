package poker.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import poker.client.Carder.Tipo;
import poker.common.ifaz.GameHandler;
import poker.common.messages.AllInMessage;
import poker.common.messages.CallMessage;
import poker.common.messages.FoldMessage;
import poker.common.messages.ProximaManoMessage;
import poker.common.messages.RaiseMessage;
import poker.common.messages.ShowdownMessage.PlayerTuple;
import poker.common.messages.server.StartGameMessage;
import poker.common.model.Card;
import poker.common.model.Hand;
import poker.common.model.PokerRoom;
import pulpcore.CoreSystem;
import pulpcore.Input;
import pulpcore.Stage;
import pulpcore.animation.BindFunction;
import pulpcore.animation.Easing;
import pulpcore.animation.Timeline;
import pulpcore.animation.event.TimelineEvent;
import pulpcore.image.Colors;
import pulpcore.image.CoreFont;
import pulpcore.image.CoreImage;
import pulpcore.math.CoreMath;
import pulpcore.scene.Scene;
import pulpcore.sound.Sound;
import pulpcore.sprite.Button;
import pulpcore.sprite.Group;
import pulpcore.sprite.ImageSprite;
import pulpcore.sprite.Label;
import pulpcore.sprite.Sprite;
import pulpcore.sprite.TextField;
import client.DisconnectedScene;
import client.PingScene;
import client.PulpcoreUtils;
import client.Spinner;
import client.DisconnectedScene.Reason;

import common.game.AbandonRoomMessage;
import common.messages.chat.RoomChatMessage;
import common.messages.server.RoomJoinedMessage;
import common.model.AbstractRoom;
import common.model.User;

public class PokerScene extends PingScene implements GameHandler {

    private static final int FICHAS_INICIALES = 100;

    private static final int BLIND = 2;

    //
    //

    private GameConnector connection;

    private PokerRoom room;

    private PokerChatArea chatArea;

    private TextField chatTF;

    private Button sendChat, abandonGame, disableSounds, startGame;

    private List<Button> botonitos;

    private User currentUser;

    private boolean creator, showdown, gameOver;

    // me toca jugar o responder
    private boolean miTurno;

    private boolean mustDisconnect;

    private Button repartir, raise, call, allin, fold;

    private int pot, miApuestaActual, apuestaActual, fichasYo;

    private Spinner bet;

    private List<ImageSprite> dealtCards;

    private ImageSprite button, bigBlind, smallBlind;

    // lo que falta para que me rajen
    int tiempoRestante;

    // el momento en que tengo que abandonar
    long timeToGo;

    private Label turno, fichasYoLbl, abajoLbl, luzLbl, potLabel, finalLabel,
            timerLabel;

    private List<Player> players;

    // SFX
    private Sound[] teToca;

    private Sound haha, beep;

    public PokerScene(GameConnector connection, User currentUser, PokerRoom room) {
        super(connection);

        this.connection = connection;
        this.room = room;
        this.creator = room.getPlayers().size() == 1;
        this.currentUser = currentUser;
        this.miTurno = false;
        this.mustDisconnect = true;

        fichasYo = FICHAS_INICIALES;

        dealtCards = new ArrayList<ImageSprite>(7);

        players = Collections.synchronizedList(new ArrayList<Player>(5));

        botonitos = new ArrayList<Button>();

        // inject
        this.connection.setGameHandler(this);
    }

    public void load() {
        // fondo
        add(new ImageSprite(CoreImage.load("imgs/background.jpg"), 0, 0));

        CoreFont din13 = CoreFont.load("imgs/DIN13.font.png");
        CoreFont din24 = CoreFont.load("imgs/DIN24.font.png").tint(0xffffff);
        CoreFont din18 = CoreFont.load("imgs/DIN18.font.png").tint(0xffffff);
        CoreFont din18numeric = CoreFont.load("imgs/DIN18_numeric.font.png")
                .tint(0xffffff);
        CoreFont din13white = din13.tint(0xffffff);
        CoreFont din30 = CoreFont.load("imgs/DIN30.font.png").tint(0xffffff);

        // chat box
        chatArea = new PokerChatArea(din13white, 550, 191, 144, 215);
        add(chatArea);

        // campo de texto donde se chatea
        chatTF = new TextField(din13white, din13, "", 554, 420, 138, -1);
        chatTF.setMaxNumChars(200);
        add(chatTF);

        // boton para enviar el chat (asociado al ENTER)
        sendChat = new Button(CoreImage.load("imgs/btn-send.png").split(3),
                700, 420);
        sendChat.setKeyBinding(Input.KEY_ENTER);
        add(sendChat);

        abandonGame = new Button(CoreImage.load("imgs/btn-salir.png").split(3),
                484, 0);
        add(abandonGame);

        disableSounds = new Button(CoreImage.load("imgs/sonidos.png").split(6),
                2, 38, true);
        disableSounds.setSelected(CoreSystem.isMute());
        add(disableSounds);

        button = new ImageSprite(CoreImage.load("imgs/button.png"), 400, 320);
        button.visible.set(false);
        add(button);

        bigBlind = new ImageSprite(CoreImage.load("imgs/big_blind.png"), 400,
                350);
        bigBlind.visible.set(false);
        add(bigBlind);

        smallBlind = new ImageSprite(CoreImage.load("imgs/small_blind.png"),
                400, 350);
        smallBlind.visible.set(false);
        add(smallBlind);

        // los otros de la sala
        for (User u : room.getPlayers()) {
            roomJoined(room, u);
        }

        startGame = Button.createLabeledButton("Empezar!", 290, 5);
        startGame.enabled.bindTo(new BindFunction() {
            @Override
            public Number f() {
                return players.size(); // == 0 > false, != 0 > true
            }
        });

        if (creator) {
            // add start game
            add(startGame);
        }

        turno = new Label(din18, "Esperando oponente", 305, 25);

        finalLabel = new Label(din30, "", 0, 180);
        finalLabel.visible.set(false);
        Group g = new Group();
        g.add(finalLabel);
        addLayer(g);

        // animo el alpha para que titile
        Timeline alphaCycle = new Timeline();
        int dur = 1000;
        alphaCycle.animate(turno.alpha, 255, 0, dur, Easing.NONE, 0);
        alphaCycle.animate(turno.alpha, 0, 255, dur, Easing.NONE, dur);
        alphaCycle.loopForever();
        addTimeline(alphaCycle);
        add(turno);

        // label con datos de la sala
        String pts = "por " + room.getPuntosApostados();
        add(new Label(din13, pts, 142, 44));
        add(new Label(din13white, pts, 140, 42));

        // timer (en un nuevo layer para estar encima de las cartas)
        timerLabel = new Label(din13white, "", 0, 0);
        Group h = new Group();
        h.add(timerLabel);
        addLayer(h);

        teToca = new Sound[7];
        for (int i = 1; i < 8; i++) {
            teToca[i - 1] = Sound.load("sfx/s" + i + ".wav");
        }

        haha = Sound.load("sfx/haha.wav");
        beep = Sound.load("sfx/beep.wav");

        // poker controls
        bet = new Spinner(240, 328, 50, 18, din18numeric, CoreImage.load(
                "imgs/btn-puntos.png").split(3));
        bet.setMinValue(1);
        bet.setStep(1);
        bet.setMaxValue(fichasYo);
        add(bet);

        repartir = Button.createLabeledButton("Repartir", 430, 380);
        repartir.enabled.set(false);
        repartir.alpha.set(0);
        add(repartir);

        raise = new Button(CoreImage.load("imgs/btn-subir.png").split(3), 300,
                325);
        raise.enabled.set(false);
        raise.alpha.set(0);
        add(raise);

        call = new Button(CoreImage.load("imgs/btn-pagar.png").split(3), 300,
                360);
        call.enabled.set(false);
        call.alpha.set(0);
        add(call);

        allin = Button.createLabeledButton("All In", 300, 395);
        allin.enabled.set(false);
        allin.alpha.set(0);
        add(allin);

        fold = new Button(CoreImage.load("imgs/btn-almazo.png").split(3), 430,
                410);
        fold.enabled.set(false);
        fold.alpha.set(0);
        add(fold);

        // mi nombre
        Label lbl = new Label(din24, currentUser.getName(), 0, 310);
        PulpcoreUtils.alignRight(lbl, 535);
        add(lbl);

        // mis fichas
        fichasYoLbl = new Label(din18, fichasYo + " fichas", 0, 340);
        PulpcoreUtils.alignRight(fichasYoLbl, 535);
        add(fichasYoLbl);

        abajoLbl = new Label(din13white, "", 0, 380);
        add(abajoLbl);

        luzLbl = new Label(din13white, "", 490, 360);
        add(luzLbl);

        // el pozo
        add(new Label(din18, "Pozo", 460, 215));
        potLabel = new Label(din30.tint(Colors.YELLOW), "0", 480, 235);
        add(potLabel);

        // envio mensaje que me uni a la sala correctamente
        connection.send(new RoomJoinedMessage());
    }

    public void unload() {
        if (mustDisconnect) {
            connection.disconnect();
        }
    }

    public void update(int elapsedTime) {
        super.update(elapsedTime);

        if (sendChat.isClicked() && chatTF.getText().trim().length() > 0) {
            connection.send(new RoomChatMessage(chatTF.getText()));

            String txt = currentUser.getName() + ": " + chatTF.getText();
            chatArea.addLine(new ColoredString(txt, Colors.WHITE));
            chatTF.setText("");
        }
        else if (disableSounds.isClicked()) {
            CoreSystem.setMute(disableSounds.isSelected());
        }
        else if (abandonGame.enabled.get() && abandonGame.isClicked()) {
            abandonGame();
        }
        if (startGame.isClicked()) {
            remove(startGame);
            connection.send(new StartGameMessage());
        }

        if (miTurno) {
            timeToGo -= elapsedTime;
            // actualizacion del timer
            int t = Math.round(timeToGo / 1000);

            if (t < 0) {
                abandonGame();
            }
            else if (t != tiempoRestante) {
                tiempoRestante = t;

                timerLabel.setText(Integer.toString(t));
                timerLabel.alpha.set(0xff);

                if (t >= 10) {
                    timerLabel.x.set(268);
                    timerLabel.y.set(25);
                }
                else if (t < 10) {
                    timerLabel.x.set(180);
                    timerLabel.y.set(180);

                    beep.play();

                    timerLabel.alpha.animateTo(0, 500);
                    timerLabel.width.animateTo(100, 500);
                    timerLabel.height.animateTo(100, 500);
                    timerLabel.x.animateTo(timerLabel.x.get() - 50, 500);
                    timerLabel.y.animateTo(timerLabel.y.get() - 50, 500);
                }
            }

            // limite de raises?
            if (raise.isClicked()) {
                int aPagar = (apuestaActual - miApuestaActual) + bet.getValue();

                if (aPagar <= fichasYo && bet.getValue() > 0) {
                    connection.send(new RaiseMessage(bet.getValue()));
                    disableButtons();
                    setMiTurno(false);
                }
                else {
                    // quiso apostar de mas (o cero), pongo el spinner en la
                    // apuesta
                    // máxima
                    bet.setValue(fichasYo - (apuestaActual - miApuestaActual));
                }
            }
            else if (call.isClicked()) {
                connection.send(new CallMessage());
                disableButtons();
                setMiTurno(false);
            }
            else if (allin.isClicked()) {
                // garpo todo
                connection.send(new AllInMessage());
                disableButtons();
                setMiTurno(false);
            }
            else if (fold.isClicked()) {
                connection.send(new FoldMessage());
                disableButtons();
                setMiTurno(false);
                updatePuntaje();
            }
            else if (repartir.isClicked()) {
                connection.send(new ProximaManoMessage());

                updatePuntaje();

                disableButtons();
                setMiTurno(false);
            }
        }
    }

    public void roomJoined(AbstractRoom unused, User user) {
        if (!user.equals(currentUser)) {
            Player p = new Player(user, players.size());
            if (!players.contains(p)) {
                players.add(p);
                add(p);
            }

            if (!room.getPlayers().contains(user)) {
                room.addPlayer(user);
            }
        }
    }

    public void incomingChat(final User from, final String msg) {
        invokeLater(new Runnable() {
            public void run() {
                String txt = from.getName() + ": " + msg;
                chatArea.addLine(new ColoredString(txt, Colors.WHITE));
            }
        });
    }

    private void setMiTurno(boolean miTurno) {
        if (miTurno) {
            turno.setText("Te toca");

            if (!this.miTurno) {
                // no era mi turno y ahora es

                invokeLater(new Runnable() {
                    public void run() {
                        teToca[(int) (Math.random() * teToca.length)].play();
                    }
                });
            }

            // en 30s abandonamos
            timeToGo = 30 * 1000;
        }
        else {
            turno.setText("Esperando jugada");
        }

        moveButtons(miTurno);
        this.miTurno = miTurno;
    }

    public void startMano(final User button, final int luz) {
        invokeLater(new Runnable() {
            public void run() {
                pot = players.size() * luz;

                luzLbl.setText("luz: " + luz);

                apuestaActual = 2 * BLIND;

                // saco los que perdieron y reseteo el resto
                updatePlayers();

                User empezador = betBlinds(button);

                teToca(getPlayer(empezador));

                sacarCartas();
                updatePuntaje();
            }
        });
    }

    private void updatePlayers() {
        // saco los players que perdieron
        synchronized (players) {
            Iterator<Player> it = players.iterator();
            while (it.hasNext()) {
                Player p = it.next();

                if (p.fichas == 0) {
                    it.remove();
                    room.removePlayer(p.user);
                    remove(p);
                }
                else {
                    p.apuestaActual = 0;
                    p.unfold();
                    p.removeBlinds();
                }
            }

            // acomodo los players, por si saqué alguno
            for (Player ply : players) {
                ply.setPos(players.indexOf(ply));
            }
        }

        // saco mis botones
        button.visible.set(false);
        bigBlind.visible.set(false);
        smallBlind.visible.set(false);

        // perdi yo?
        if (fichasYo == 0) {
            room.removePlayer(currentUser);
        }
    }

    /**
     * pone las blinds y devuelve quien empieza
     * 
     * @param buttonUser
     *            el que da las cartas
     * @return el que le toca empezar
     */
    private User betBlinds(User buttonUser) {
        // blinds
        List<User> blinds = new ArrayList<User>(room.getPlayers());
        Collections.rotate(blinds, -blinds.indexOf(buttonUser));

        // TODO? si no le alcanza para pagar el blind va all-in

        if (buttonUser.equals(currentUser)) {
            button.visible.set(true);
        }
        else {
            getPlayer(blinds.get(0)).button();
        }

        updatePlayers();

        if (blinds.size() > 2) {
            // small blind
            if (blinds.get(1).equals(currentUser)) {
                int smallB = Math.min(fichasYo, BLIND);
                fichasYo -= smallB;
                miApuestaActual = smallB;
                pot += smallB;
                smallBlind.visible.set(true);
            }
            else {
                Player small = getPlayer(blinds.get(1));
                int smallB = Math.min(small.fichas, BLIND);
                small.fichas -= smallB;
                small.apuestaActual = smallB;
                small.smallBlind();
                pot += smallB;
            }

            // big blind
            if (blinds.get(2).equals(currentUser)) {
                int bigB = Math.min(fichasYo, BLIND * 2);
                fichasYo -= bigB;
                miApuestaActual = bigB;
                pot += bigB;
                bigBlind.visible.set(true);
            }
            else {
                Player big = getPlayer(blinds.get(2));
                int bigB = Math.min(big.fichas, BLIND * 2);
                big.fichas -= bigB;
                big.apuestaActual = bigB;
                big.bigBlind();
                pot += bigB;
            }

            return blinds.get(3 % blinds.size());
        }
        // XXX mucho codigo duplicado
        else {
            // small blind
            if (blinds.get(0).equals(currentUser)) {
                int smallB = Math.min(fichasYo, BLIND);
                fichasYo -= smallB;
                miApuestaActual = smallB;
                pot += smallB;
                smallBlind.visible.set(true);
            }
            else {
                Player small = getPlayer(blinds.get(0));
                int smallB = Math.min(small.fichas, BLIND);
                small.fichas -= smallB;
                small.apuestaActual = smallB;
                small.smallBlind();
                pot += smallB;
            }

            // big blind
            if (blinds.get(1).equals(currentUser)) {
                int bigB = Math.min(fichasYo, BLIND * 2);
                fichasYo -= bigB;
                miApuestaActual = bigB;
                pot += bigB;
                bigBlind.visible.set(true);
            }
            else {
                Player big = getPlayer(blinds.get(1));
                int bigB = Math.min(big.fichas, BLIND * 2);
                big.fichas -= bigB;
                big.apuestaActual = bigB;
                big.bigBlind();
                pot += bigB;
            }

            return blinds.get(0);
        }
    }

    public void startMano(final Card c1, final Card c2, final User mano,
            final int luz) {
        showdown = false;

        invokeLater(new Runnable() {
            public void run() {
                remove(startGame);

                // visibilizo y habilito el boton de abandonar y label turno
                abandonGame.visible.set(true);
                abandonGame.enabled.set(true);
                turno.visible.set(true);

                // saco los que perdieron y reseteo el resto
                updatePlayers();

                pot = (players.size() + 1) * luz;

                apuestaActual = 2 * BLIND;
                miApuestaActual = 0;
                fichasYo -= luz;
                
                // todos pagan luz
                for (Player p : players) {
                    p.fichas -= luz;
                }

                luzLbl.setText("luz: " + luz);

                final User empezador = betBlinds(mano);
                sacarCartas();
                updatePuntaje();

                teToca(getPlayer(empezador));

                // saco el mensaje final
                finalLabel.visible.set(false);

                int time = 500;
                dealCard(c1, Tipo.MIAS, 30, 320, time, 0);
                dealCard(c2, Tipo.MIAS, 120, 320, time, time);

                addEvent(new TimelineEvent(time) {
                    @Override
                    public void run() {
                        setMiTurno(empezador.equals(currentUser));
                        // al 3ro
                        putButtons();
                    }
                });
            }
        });
    }

    @Override
    public void tuTurno() {
        invokeLater(new Runnable() {
            public void run() {
                setMiTurno(true);
                enable(botonitos.toArray(new Button[botonitos.size()]));
            }
        });
    }

    @Override
    public void fold(final User user, final User next) {
        invokeLater(new Runnable() {
            public void run() {
                setCanto(user, " se va", Colors.RED);

                if (!user.equals(currentUser)) {
                    getPlayer(user).fold();
                }

                teToca(getPlayer(next));

                setMiTurno(currentUser.equals(next));
                if (apuestaActual - miApuestaActual >= fichasYo) {
                    enable(fold, allin);
                }
                else {
                    putButtons();
                }
            }
        });
    }

    @Override
    public void allFolded(final User ganador, final User next) {
        invokeLater(new Runnable() {
            public void run() {
                if (ganador.equals(currentUser)) {
                    fichasYo += pot;
                }
                else {
                    getPlayer(ganador).fichas += pot;
                }

                updatePuntaje();

                disableButtons();
                setMiTurno(next.equals(currentUser));
                teToca(getPlayer(next));
                enable(repartir); // XXX darle un tiempo minimo de delay
            }
        });
    }

    public void flop(final Card c1, final Card c2, final Card c3,
            final User next) {
        invokeLater(new Runnable() {
            public void run() {
                int time = 500;
                dealCard(c1, Tipo.POZO, 22, 200, time, 0);
                dealCard(c2, Tipo.POZO, 97, 200, time, time);
                dealCard(c3, Tipo.POZO, 172, 200, time, time * 2);

                setMiTurno(currentUser.equals(next));
                teToca(getPlayer(next));

                addEvent(new TimelineEvent(time) {
                    @Override
                    public void run() {
                        if (!showdown) {
                            if (fichasYo == 0) {
                                enable(call);
                            }
                            else {
                                putButtons();
                            }
                        }
                    }
                });
            }
        });
    }

    public void turn(final Card turn, final User next) {
        invokeLater(new Runnable() {
            public void run() {
                int time = 500;
                dealCard(turn, Tipo.POZO, 247, 200, time, 0);

                setMiTurno(currentUser.equals(next));
                teToca(getPlayer(next));

                addEvent(new TimelineEvent(time) {
                    @Override
                    public void run() {
                        if (!showdown) {
                            if (fichasYo == 0) {
                                enable(call);
                            }
                            else {
                                putButtons();
                            }
                        }
                    }
                });
            }
        });
    }

    public void river(final Card river, final User next) {
        invokeLater(new Runnable() {
            public void run() {
                int time = 500;
                dealCard(river, Tipo.POZO, 322, 200, time, 0);

                setMiTurno(currentUser.equals(next));
                teToca(getPlayer(next));

                addEvent(new TimelineEvent(time) {
                    @Override
                    public void run() {
                        if (!showdown) {
                            if (fichasYo == 0) {
                                enable(call);
                            }
                            else {
                                putButtons();
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public void showdown(final Hand ganadora, final String nombre,
            final Map<User, PlayerTuple> manos, final List<User> ganadores,
            final int fichas, final User next) {

        showdown = true;

        invokeLater(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<User, PlayerTuple> entry : manos.entrySet()) {
                    Player p = getPlayer(entry.getKey());
                    PlayerTuple tup = entry.getValue();

                    p.showCards(Carder.getCartaImage(tup.mano.getCard(1),
                            Tipo.OPONENTE), Carder.getCartaImage(tup.mano
                            .getCard(2), Tipo.OPONENTE));

                    p.fichas = tup.fichas;
                    p.updateFichas();
                }

                // TODO griso las cartas que no son ganadoras
                // for (ImageSprite cartaImg : dealtCards) {
                // Card carta = (Card) cartaImg.getTag();
                // if (!ganadora.hasCard(carta)) {
                // cartaImg.setFilter(new Negative());
                // }
                // }

                fichasYo = fichas;

                if (ganadores.size() > 1) {
                    Iterator<User> it = ganadores.iterator();
                    String txt = it.next().getName();

                    while (it.hasNext()) {
                        txt += " y " + it.next().getName();
                    }

                    setCanto(txt + " ganan con su " + nombre, Colors.LIGHTGRAY);
                }
                else {
                    setCanto(ganadores.get(0), " gana con su " + nombre,
                            Colors.LIGHTGRAY);
                }
                updatePuntaje();

                // finMano
                apuestaActual = 2 * BLIND;
                miApuestaActual = 0;

                disableButtons();
                setMiTurno(currentUser.equals(next));
                teToca(getPlayer(next));

                // XXX dar un toque de delay para que vean las cartas
                enable(repartir);
            }
        });
    }

    /**
     * El otro paga
     */
    @Override
    public void call(final User user, final User next) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                if (user.equals(currentUser)) {
                    if (fichasYo > 0) {
                        int aPagar = apuestaActual - miApuestaActual;

                        pot += aPagar;
                        fichasYo -= aPagar;
                        miApuestaActual = apuestaActual;

                        if (aPagar > 0) {
                            setCanto(currentUser, " paga " + aPagar,
                                    Colors.GREEN);
                        }
                        else {
                            setCanto(currentUser, " pasa", Colors.GREEN);
                        }
                    }
                }
                else {
                    Player p = getPlayer(user);
                    int aPagar = apuestaActual - p.apuestaActual;
                    pot += aPagar;
                    p.fichas -= aPagar;
                    p.apuestaActual = apuestaActual;

                    if (aPagar > 0) {
                        setCanto(user, " paga " + aPagar, Colors.GREEN);
                    }
                    else {
                        setCanto(user, " pasa", Colors.GREEN);
                    }
                }

                updatePuntaje();
                teToca(getPlayer(next));

                setMiTurno(currentUser.equals(next));
                if (fichasYo == 0) {
                    enable(call);
                }
                else if (apuestaActual - miApuestaActual >= fichasYo) {
                    enable(allin, fold);
                }
                else {
                    putButtons();
                }
            }
        });
    }

    @Override
    /**
     * El otro subio la apuesta
     */
    public void raise(final User user, final int fichas, final User next) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                // XXX cap al spinner en lo maximo que puedo subir
                setMiTurno(currentUser.equals(next));

                if (user.equals(currentUser)) {
                    int aPagar = (apuestaActual - miApuestaActual) + fichas;

                    // pago lo que restaba pagar
                    pot += aPagar;
                    fichasYo -= aPagar;

                    if (apuestaActual == miApuestaActual) {
                        setCanto(currentUser, " sube " + bet.getValue(),
                                Colors.YELLOW);
                    }
                    else {
                        setCanto(currentUser, " paga "
                                + (apuestaActual - miApuestaActual)
                                + " y sube " + bet.getValue(), Colors.YELLOW);
                    }

                    apuestaActual += bet.getValue();
                    miApuestaActual = apuestaActual;
                    updatePuntaje();
                }
                else {
                    Player p = getPlayer(user);
                    int aPagar = (apuestaActual - p.apuestaActual) + fichas;

                    if (apuestaActual == p.apuestaActual) {
                        setCanto(user, " sube " + fichas, Colors.YELLOW);
                    }
                    else {
                        setCanto(user, " paga "
                                + (apuestaActual - p.apuestaActual)
                                + " y sube " + fichas, Colors.YELLOW);
                    }

                    pot += aPagar;
                    apuestaActual += fichas;

                    p.fichas -= aPagar;
                    p.apuestaActual = apuestaActual;

                    updatePuntaje();

                    bet.setValue(fichas);
                    // bet.setMinValue(fichas);
                    // bet.setStep(1);
                }

                teToca(getPlayer(next));

                if (apuestaActual - miApuestaActual >= fichasYo) {
                    enable(allin, fold);
                }
                else {
                    putButtons();
                }
            }
        });
    }

    public void allIn(final User user, final int fichas, final User next) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                setMiTurno(currentUser.equals(next));

                if (user.equals(currentUser)) {
                    int subido = fichasYo - (apuestaActual - miApuestaActual);
                    pot += fichasYo;

                    if (subido > 0) {
                        // el allIn sube la apuesta actual
                        apuestaActual += subido;
                        miApuestaActual = apuestaActual;
                    }
                    else {
                        // el allIn no alcanza a la apuesta actual
                        miApuestaActual += fichasYo;
                    }

                    fichasYo = 0;
                }
                else {
                    Player p = getPlayer(user);

                    // tengo que igualar todo lo que subio
                    int loQueSube = fichas - (apuestaActual - p.apuestaActual);
                    pot += fichas;

                    p.fichas = 0;

                    if (loQueSube > 0) {
                        // el allIn del otro sube la apuesta actual
                        apuestaActual += loQueSube;
                        p.apuestaActual = apuestaActual;
                    }
                    else {
                        // el allIn del otro no alcanza a la apuesta actual
                        p.apuestaActual += fichas;
                    }

                    if (loQueSube > 0) {
                        if (loQueSube >= fichasYo) {
                            // me subio mas que, o lo que tengo
                            enable(allin, fold);
                        }
                        else {
                            // me subio y me alcanza para pagar
                            putButtons();
                        }
                    }
                    else {
                        // no pago la apuesta actual
                        if (apuestaActual < fichasYo) {
                            // puedo pagar la apuestActual
                            enable(call, allin, fold);
                        }
                        else {
                            // no me alcanza para pagar la apuestActual
                            enable(allin, fold);
                        }
                    }
                }

                setCanto(user, " pone todas sus fichas", Colors.CYAN);
                teToca(getPlayer(next));

                updatePuntaje();

                // bet.setMinValue(fichas);
                // bet.setStep(1);
            }
        });
    }

    public void finJuego(final boolean victoria) {
        gameOver = true;

        invokeLater(new Runnable() {
            public void run() {
                if (victoria) {
                    // gane
                    finalLabel.setText("¡Ganaste! ¡Capo!");
                }
                else {
                    // perdi
                    finalLabel.setText("¡Perdiste, alcornoque!");
                    haha.play();
                }

                finalLabel.visible.set(true);
                PulpcoreUtils.centerSprite(finalLabel, 235, 319);

                disableButtons();

                // invisibilizo y deshabilito el boton de abandonar
                abandonGame.visible.set(false);
                abandonGame.enabled.set(false);

                // espero a que lleguen los puntos
                setMiTurno(false);

                // cambio texto del cartel
                turno.setText("Actualizando puntos");
            }
        });
    }

    public void updatePoints(int puntos) {
        // actualizo puntos
        invokeLater(new Runnable() {
            public void run() {
                turno.setText("Hora de irse");

                // visibilizo y habilito el boton de abandonar
                abandonGame.visible.set(true);
                abandonGame.enabled.set(true);
            }
        });
    }

    private void disableButtons() {
        disable(repartir, call, fold, raise, allin);
        botonitos = new ArrayList<Button>();
    }

    private void disable(Sprite... sprites) {
        for (Sprite s : sprites) {
            s.enabled.set(false);
            s.alpha.animateTo(0, 300);
        }
    }

    private void enable(Button... botones) {
        if (miTurno) {
            if (botones != null) {
                for (Button b : botones) {
                    b.enabled.set(true);
                    b.alpha.animateTo(255, 300);
                }
            }
        }
        else {
            // XXX si hay dos llamadas seguidas a enable una pisa la otra
            // los guardo
            // if (botonitos == null || botonitos.size() == 0) {
            botonitos = Arrays.asList(botones);
            // }
            // else {
            // botonitos.addAll(Arrays.asList(botones));
            // }
        }
    }

    private void moveButtons(boolean in) {
        // moveButtons(in, truco, retruco, vale4, quiero, noquiero, envido,
        // real,
        // falta, alMazo, repartir);
    }

    // private void moveButtons(boolean in, Button... botones) {
    // int x = in ? BOTON_X_IN : BOTON_X_OUT;
    // int y = BOTON_Y;
    // for (Button b : botones) {
    // if (b.enabled.get()) {
    // b.x.animateTo(x, 300, Easing.REGULAR_OUT);
    // b.y.animateTo(y, 300, Easing.REGULAR_OUT);
    // y += b.height.getAsInt() + 5;
    // }
    // }
    // }

    private void updatePuntaje() {
        // XXX efectito
        fichasYoLbl.setText(fichasYo + " fichas");
        potLabel.setText(Integer.toString(pot));

        if (apuestaActual > miApuestaActual) {
            abajoLbl.setText("debés " + (apuestaActual - miApuestaActual));
        }
        else {
            abajoLbl.setText("");
        }
        PulpcoreUtils.alignRight(abajoLbl, 525);

        synchronized (players) {
            for (Player ply : players) {
                ply.updateFichas();
            }
        }
    }

    private void sacarCartas() {
        for (ImageSprite c : dealtCards) {
            remove(c);
        }

        synchronized (players) {
            for (Player p : players) {
                p.sacarCartas();
            }
        }
    }

    public void oponenteAbandono(boolean enJuego, final User user) {
        invokeLater(new Runnable() {
            public void run() {
                synchronized (players) {
                    Player p = getPlayer(user);

                    players.remove(p);
                    remove(p);

                    setCanto(user, " abandono el juego", Colors.LIGHTGRAY);

                    // acomodo el resto
                    for (Player ply : players) {
                        ply.setPos(players.indexOf(ply));
                    }
                }
            }
        });

        if (!enJuego && !gameOver) {
            // soy el game owner?
            List<User> roomPlayers = room.getPlayers();
            if (roomPlayers.get(0).equals(user)
                    && roomPlayers.get(1).equals(currentUser)) {
                creator = true;

                // add start game
                add(startGame);
            }
        }

        // saco el user que se fue
        room.removePlayer(user);
    }

    public void kicked() {
        invokeLater(new Runnable() {
            public void run() {
                connection.disconnect();
                Stage.setScene(new DisconnectedScene(Reason.KICKED));
            }
        });
    }

    public void disconnected() {
        invokeLater(new Runnable() {
            public void run() {
                Stage.setScene(new DisconnectedScene(Reason.FAILED));
            }
        });
    }

    private final void setScene(final Scene s) {
        mustDisconnect = false;
        Stage.setScene(s);
    }

    private void abandonGame() {
        // envio abandono
        connection.send(new AbandonRoomMessage());

        invokeLater(new Runnable() {
            public void run() {
                // me rajo al lobby
                setScene(new LobbyScene(currentUser, connection));
            }
        });
    }

    @Override
    public void newGame() {
    }

    @Override
    public void startGame(boolean start) {
    }

    private void setCanto(String msg, int color) {
        chatArea.addLine(new ColoredString(msg, color));
    }

    private void setCanto(User user, String msg, int color) {
        setCanto(user.getName() + msg, color);
    }

    private int[] getRandomOutside() {
        Random r = new Random();

        int w = Stage.getWidth();
        int h = Stage.getHeight();

        int ret[] = null;

        int rand = r.nextInt(4);

        if (rand == 0) { // arriba
            ret = new int[] { r.nextInt(w), -200 };
        }
        else if (rand == 1) { // abajo
            ret = new int[] { r.nextInt(w), h + 200 };
        }
        else if (rand == 2) { // izq
            ret = new int[] { -200, r.nextInt(h) };
        }
        else if (rand == 3) { // der
            ret = new int[] { w + 200, r.nextInt(h) };
        }

        return ret;
    }

    private void dealCard(Card card, Tipo tipo, int x, int y, int time,
            int delay) {
        int[] xy = getRandomOutside();
        ImageSprite cardImg = new ImageSprite(Carder.getCartaImage(card, tipo),
                xy[0], xy[1]);
        cardImg.angle.setAsFixed(CoreMath.rand(0, CoreMath.TWO_PI));
        cardImg.setTag(card);
        add(cardImg);

        cardImg.x.animateTo(x, time, Easing.REGULAR_OUT, delay);
        cardImg.y.animateTo(y, time, Easing.REGULAR_OUT, delay);
        cardImg.angle.animateTo(0, time, Easing.REGULAR_OUT, delay);

        dealtCards.add(cardImg);
    }

    private Player getPlayer(User u) {
        synchronized (players) {
            for (Player p : players) {
                if (p.user.equals(u)) {
                    return p;
                }
            }
        }

        System.out.println("getPlayer(" + u + ") = NULL");
        new Exception().printStackTrace(System.out);
        return null;
    }

    private void putButtons() {
        List<Button> buttons = new ArrayList<Button>(4);
        buttons.add(allin);
        buttons.add(fold);

        if (fichasYo >= apuestaActual - miApuestaActual) {
            buttons.add(call);
        }
        if (fichasYo > apuestaActual - miApuestaActual && hayPlayersConFichas()) {
            buttons.add(raise);
        }

        enable(buttons.toArray(new Button[buttons.size()]));
    }

    private boolean hayPlayersConFichas() {
        if (fichasYo > apuestaActual - miApuestaActual) {
            synchronized (players) {
                for (Player p : players) {
                    if (p.fichas > 0 && !p.folded) {
                        return true;
                    }
                }
                return false;
            }
        }

        return false;
    }

    private void teToca(Player player) {
        synchronized (players) {
            for (Player p : players) {
                p.teToca(p == player);
            }
        }
    }
}

// TODO color para los distintos jugadores en el chat
// TODO scrollbar

// cuando tiras el all in y gana el de mayor numero de fichas, pasa que sobre
// las cinco cartas tira tres mas

// reparte sin sacar las cartas viejas

// no detecta bien gameover y reparte cartas de nuevo

// el "paga" es confuso

// finalLabel abajo de todo asi (donde van mis cartas) si perdi puedo ver bien