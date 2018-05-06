//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.mc.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.StringTokenizer;

public class Player implements Runnable {
    static int NOTLOGGEDIN = 0;
    static int IDLE = 1;
    static int INVITED = 2;
    static int INVITING = 3;
    static int INGAME = 4;
    int state;
    String name;
    Socket sockToClient;
    BufferedReader pin;
    PrintStream pout;
    Player partner;
    GameServer server;
    boolean stopped;
    String messageOriginator;
    final int IDLE_TIMEOUT;
    final int RESPONSE_TIMEOUT;
    int GAME_TIMEOUT;
    int idleTime;
    int responseTime;
    boolean responseTimerActive;
    boolean isDieGame;
    int diePoints;

    Player(Socket sock, GameServer server) {
        this.state = NOTLOGGEDIN;
        this.name = null;
        this.sockToClient = null;
        this.pin = null;
        this.pout = null;
        this.partner = null;
        this.server = null;
        this.stopped = false;
        this.messageOriginator = "Server";
        this.IDLE_TIMEOUT = 300;
        this.RESPONSE_TIMEOUT = 60;
        this.GAME_TIMEOUT = 30;
        this.idleTime = 0;
        this.responseTime = 0;
        this.responseTimerActive = false;
        this.isDieGame = true;
        this.diePoints = 0;
        this.server = server;
        this.sockToClient = sock;
        this.state = NOTLOGGEDIN;

        try {
            this.sockToClient.setSoTimeout(1000);
        } catch (Exception var5) {
            ;
        }

        try {
            this.pin = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            this.pout = new PrintStream(sock.getOutputStream(), true);
            this.sendMessage("B", "Verbindung zum Server erstellt");
        } catch (IOException var4) {
            server.consoleOut("Can't get Stream");
        }

        (new Thread(this)).start();
    }

    Player(Socket sock, GameServer server, int gameTimeout) {
        this(sock, server);
        if (gameTimeout > 5 && gameTimeout < 300) {
            this.GAME_TIMEOUT = gameTimeout;
        }

    }

    public synchronized boolean isAvailable() {
        return this.state == IDLE;
    }

    synchronized void resetState() {
        this.state = IDLE;
        this.partner = null;
    }

    private int throwDice() {
        this.diePoints = (int)Math.round(Math.random() * 6.0D + 0.5D);
        return this.diePoints;
    }

    synchronized boolean askForGame(Player requester) {
        if (this.state == IDLE) {
            this.state = INVITED;
            this.partner = requester;
            this.sendMessage("Q", requester.name + " moechte gegen Sie spielen. o.k.? (Ja/Nein)");
            this.watchResponseTime();
            return true;
        } else {
            requester.sendMessage("E204", "Spieler " + this.name + " nicht verfuegbar!");
            return false;
        }
    }

    synchronized void acceptGame() {
        if (this.state == INVITED) {
            this.stopResponseTimer();
            this.state = INGAME;
            this.partner.acceptGame();
            this.sendMessage("B", "Spiel startet");
            this.sendMessage("Z", "Sie sind am Zug");
            if (this.isDieGame) {
                this.sendDice();
            }

            this.messageOriginator = this.partner.name;
            this.watchResponseTime();
        } else if (this.state == INVITING) {
            this.state = INGAME;
            this.sendMessage("B", this.partner.name + " akzeptiert");
            this.sendMessage("M", "Spiel startet. " + this.partner.name + " setzt zuerst");
            this.messageOriginator = this.partner.name;
        }

    }

    synchronized void rejectGame() {
        if (this.state == INVITED) {
            this.stopResponseTimer();
            this.state = IDLE;
            this.sendMessage("M", "Ablehnung an " + this.partner.name + " weitergegeben");
            this.partner.rejectGame();
            this.partner = null;
        }

        if (this.state == INVITING) {
            this.state = IDLE;
            this.sendMessage("E201", this.partner.name + " moechte nicht gegen Sie spielen");
            this.partner = null;
        }

    }

    synchronized void quitRequest() {
        if (this.state == INVITED) {
            this.state = IDLE;
            this.stopResponseTimer();
            this.partner.sendMessage("E201", this.partner.name + " moechte nicht gegen Sie spielen");
            this.partner.resetState();
            this.partner = null;
        }

        if (this.state == INVITING) {
            this.state = IDLE;
            this.partner.sendMessage("E208", this.partner.name + " zieht seine Spieleinladung zurueck");
            this.partner.resetState();
            this.partner = null;
        }

    }

    synchronized void endGame() {
        this.sendMessage("M", "Spielende");
        this.partner = null;
        this.state = IDLE;
        this.messageOriginator = "Server";
        this.stopResponseTimer();
    }

    void resetServer(){
        server.stopPlayers();
    }

    synchronized void disconnectGame(Player partner) {
        this.server.consoleOut("disconnecting " + this.name + " " + partner.name);
        partner.endGame();
        this.messageOriginator = "Server";
        this.endGame();
    }

    public void resetIdleTime() {
        this.idleTime = 0;
    }

    public void watchResponseTime() {
        this.responseTime = 0;
        this.responseTimerActive = true;
    }

    public void stopResponseTimer() {
        this.responseTime = 0;
        this.responseTimerActive = false;
    }

    void pleaseStop() {
        this.stopped = true;
    }

    public void sendMessage(String messageType, String message) {
        this.pout.print(this.messageOriginator + " " + messageType + "> " + message + "\r\n");
    }

    public void sendDice() {
        this.pout.print("Server Z> Würfel: " + this.throwDice() + "\r\n");
    }

    public void sendTicker() {
        this.pout.print("Server Z> Zug an " + this.partner.name + "\r\n");
    }

    public void run() {
        String inputBuffer = null;
        this.resetIdleTime();

        while(!this.stopped) {
            try {
                inputBuffer = this.pin.readLine();
                inputBuffer = inputBuffer.trim();
                this.resetIdleTime();
                if (this.state != INGAME) {
                    if (inputBuffer != null) {
                        this.handleDialog(inputBuffer);
                    } else {
                        this.server.removePlayer(this);
                    }
                } else {
                    if (inputBuffer == null || inputBuffer.equals("quit") || inputBuffer.equals("logout")) {
                        this.disconnectGame(this.partner);
                        if (inputBuffer.equals("logout")) {
                            this.handleDialog(inputBuffer);
                        }

                        if (inputBuffer == null) {
                            this.server.removePlayer(this);
                        }

                        inputBuffer = "";
                    }

                    if (this.state == INGAME) {
                        if (this.isDieGame) {
                            this.partner.sendMessage("(Würfel: " + this.diePoints + ") Z", inputBuffer);
                            this.partner.sendDice();
                        } else {
                            this.partner.sendMessage("Z", inputBuffer);
                        }

                        this.stopResponseTimer();
                        this.partner.watchResponseTime();
                        this.sendTicker();
                    }
                }
            } catch (SocketTimeoutException var4) {
                ++this.idleTime;
                if (this.responseTimerActive) {
                    ++this.responseTime;
                }

                if (this.state == IDLE && this.idleTime >= 300) {
                    this.sendMessage("E302", "Idle timeout");
                    this.server.consoleOut("Idle Timeout " + this.name);
                    this.server.removePlayer(this);
                } else if (this.state == INGAME && this.responseTime >= this.GAME_TIMEOUT) {
                    this.server.consoleOut("Game timeout " + this.name);
                    this.messageOriginator = "Server";
                    this.partner.messageOriginator = "Server";
                    this.sendMessage("E301", "Antwortzeit ueberschritten");
                    this.sendMessage("M", this.partner.name + " hat das Spiel gewonnen");
                    this.partner.sendMessage("M", this.name + ": Zeitueberschreitung");
                    this.partner.sendMessage("M", "Sie haben gewonnen!");
                    this.disconnectGame(this.partner);
                } else if (this.state == INVITED && this.responseTime >= 60) {
                    this.sendMessage("E303", "Spielanfrage: Antwortzeit ueberschritten");
                    this.quitRequest();
                }
            } catch (IOException var5) {
                if (this.state == INVITING || this.state == INVITED) {
                    this.quitRequest();
                }

                if (this.state == INGAME) {
                    this.disconnectGame(this.partner);
                }

                this.server.removePlayer(this);
                this.stopped = true;
            }
        }

        this.sendMessage("M", "disconnect");
        this.pout.close();

        try {
            this.pin.close();
            this.sockToClient.close();
        } catch (Exception var3) {
            ;
        }

    }

    void handleDialog(String message) {
        StringTokenizer comTokens = new StringTokenizer(message);
        if (!comTokens.hasMoreTokens()) {
            this.sendMessage("E001", "Befehl unbekannt: ");
        } else {
            String command = comTokens.nextToken();
            command = command.toLowerCase();
            String partnerName;
            if (command.equals("hilfe")) {
                partnerName = "Verfuegbare Befehle: login name; logout; hilfe; liste; werBinIch; spiel partnerName";
                this.sendMessage("B", partnerName);
            } else if (this.name == null) {
                if (!command.equals("login")) {
                    this.sendMessage("E101", "Bitte einloggen! (login name)");
                } else if (comTokens.hasMoreTokens()) {
                    partnerName = comTokens.nextToken();
                    if (this.server.findPlayer(partnerName) == null) {
                        this.name = partnerName;
                        this.sendMessage("B", partnerName + ", Sie sind angemeldet");
                        this.state = IDLE;
                        this.server.consoleOut("login " + partnerName);
                    } else {
                        this.sendMessage("E102", "Login nicht moeglich. Name existiert bereits: " + partnerName);
                    }
                } else {
                    this.sendMessage("E103", "Bitte login mit Namen eingeben");
                }

                return;
            }

            if (this.name != null) {
                if (command.equals("login")) {
                    this.sendMessage("E104", "Sie sind bereits eingeloggt");
                } else if (command.equals("logout")) {
                    this.server.consoleOut("logout " + this.name);
                    if (this.state == INVITING || this.state == INVITED) {
                        this.quitRequest();
                    }

                    this.sendMessage("B", "disconnect");
                    this.server.removePlayer(this);
                }else if(command.equals("resetserver")) {
                    resetServer();
                } else if (command.equals("liste")) {
                    partnerName = this.server.listAvailablePlayers(this);
                    this.sendMessage("B", "Folgende Spieler waeren bereit zu spielen: " + partnerName);
                } else if (command.equals("werbinich")) {
                    this.sendMessage("B", "Sie sind " + this.name);
                } else if (command.equals("spiel")) {
                    if (this.state == INVITED) {
                        this.sendMessage("E206", "Sie werden bereits angefragt");
                    } else if (this.state == INVITING) {
                        this.sendMessage("E207", "Sie haben bereits angefragt");
                    } else if (comTokens.hasMoreTokens()) {
                        partnerName = comTokens.nextToken();
                        Player partner = this.server.findPlayer(partnerName);
                        if (partner == null) {
                            this.sendMessage("E203", "Spieler nicht gefunden: " + partnerName);
                        } else if (partner == this) {
                            this.sendMessage("E205", "Sie koennen nicht gegen sich selbst spielen");
                        } else {
                            synchronized(this) {
                                if (this.state == IDLE && partner.askForGame(this)) {
                                    this.state = INVITING;
                                    this.partner = partner;
                                    this.sendMessage("M", "Hole Einverstaendnis von " + partnerName + ". Bitte warten!");
                                    partner.watchResponseTime();
                                }
                            }
                        }
                    } else {
                        this.sendMessage("E202", "spiel: Partnernamen angeben!");
                    }
                } else if (command.equals("ja") && this.state == INVITED) {
                    if (this.state == INVITED) {
                        this.acceptGame();
                        this.messageOriginator = this.partner.name;
                        this.server.consoleOut("connecting " + this.name + " " + this.partner.name);
                    }
                } else if (command.equals("nein") && this.state == INVITED) {
                    if (this.state == INVITED) {
                        this.rejectGame();
                    }
                } else if (!command.equals("hilfe")) {
                    this.sendMessage("E001", "Befehl unbekannt: " + command);
                }
            }

        }
    }
}

