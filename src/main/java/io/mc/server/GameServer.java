//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.mc.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class GameServer {
    int anzPlayers = 0;
    int maxPlayers = 36;
    Player[] players;
    GameServer.KonsolEingabe konsole;
    GameServer.Receptionist receptionist;
    int GAMETIMEOUT;

    public GameServer(int gameTimeout) {
        this.players = new Player[this.maxPlayers];
        this.receptionist = new GameServer.Receptionist(this);
        this.konsole = new GameServer.KonsolEingabe();
        this.GAMETIMEOUT = gameTimeout;
    }

    public static void main(String[] args) {
        int gameTimeout = 0;
        if (args.length > 0) {
            try {
                gameTimeout = Integer.parseInt(args[0]);
            } catch (Exception var3) {
                ;
            }
        }

        new GameServer(gameTimeout);
    }

    public synchronized boolean addPlayer(Socket s) {
        if (this.anzPlayers < this.maxPlayers) {
            this.players[this.anzPlayers++] = new Player(s, this, this.GAMETIMEOUT);
            return true;
        } else {
            return false;
        }
    }

    public synchronized void removePlayer(Player kill) {
        int ikill = this.maxPlayers;
        kill.pleaseStop();

        int i;
        for(i = 0; i < this.anzPlayers; ++i) {
            if (this.players[i] == kill) {
                ikill = i;
            }
        }

        for(i = ikill; i < this.anzPlayers - 1; ++i) {
            this.players[i] = this.players[i + 1];
        }

        --this.anzPlayers;
        this.consoleOut("remove " + kill.name);
    }

    void stopPlayers() {
        for(int i = 0; i < this.anzPlayers; ++i) {
            this.players[i].pleaseStop();
        }

        this.anzPlayers = 0;
    }

    public synchronized String listAvailablePlayers(Player thisPlayer) {
        String list = "";

        for(int i = 0; i < this.anzPlayers; ++i) {
            if (this.players[i] != thisPlayer && this.players[i].name != null && this.players[i].isAvailable()) {
                list = list + this.players[i].name + "  ";
            }
        }

        if (list.equals("")) {
            list = "Niemand frei!";
        }

        if (this.anzPlayers == 1) {
            list = "Sonst keiner da!";
        }

        return list;
    }

    synchronized String listPlayers() {
        String list = "";
        Set done = new HashSet();

        int i;
        for(i = 0; i < this.anzPlayers; ++i) {
            if (this.players[i].name != null && this.players[i].partner == null) {
                list = list + this.players[i].name + "  ";
            } else if (this.players[i].name == null) {
                list = list + "anonymous ";
            }
        }

        for(i = 0; i < this.anzPlayers; ++i) {
            if (this.players[i].name != null && this.players[i].partner != null && !done.contains(this.players[i])) {
                list = list + "(" + this.players[i].name + " " + this.players[i].partner.name + ")  ";
                done.add(this.players[i].partner);
            }
        }

        if (list.equals("")) {
            list = "Keiner da!";
        }

        return list;
    }

    synchronized Player findPlayer(String name) {
        for(int i = 0; i < this.anzPlayers; ++i) {
            if (name.equals(this.players[i].name)) {
                return this.players[i];
            }
        }

        return null;
    }

    void consoleOut(String ausgabe) {
        System.out.print(ausgabe + "\n> ");
    }

    public void stopService() {
        this.receptionist.pleaseStop();
        this.konsole.pleaseStop();
    }

    class KonsolEingabe implements Runnable {
        BufferedReader consoleIn = null;
        String command = null;
        boolean stopped = false;

        KonsolEingabe() {
            (new Thread(this)).start();
        }

        public void pleaseStop() {
            this.stopped = true;
        }

        public void run() {
            BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
            GameServer.this.consoleOut("");

            while(!this.stopped) {
                try {
                    this.command = consoleIn.readLine();
                    if (this.command.equals("reset")) {
                        GameServer.this.stopPlayers();
                        GameServer.this.consoleOut("Server reset; all users disconnceted");
                    } else if (this.command.equals("stop")) {
                        GameServer.this.stopPlayers();
                        this.stopped = true;
                        System.out.println("Server terminated");
                    } else if (this.command.equals("quit")) {
                        GameServer.this.stopPlayers();
                        this.stopped = true;
                        System.out.println("Server terminated");
                    } else if (this.command.equals("help")) {
                        GameServer.this.consoleOut("Commands available: help reset quit stop");
                    } else if (this.command.equals("list")) {
                        GameServer.this.consoleOut(GameServer.this.listPlayers());
                    } else {
                        GameServer.this.consoleOut("Command unknown: " + this.command);
                        GameServer.this.consoleOut("Commands available: help reset quit stop");
                    }
                } catch (IOException var3) {
                    ;
                }
            }

            System.exit(0);
        }
    }

    class Receptionist implements Runnable {
        ServerSocket listener = null;
        boolean stopped = false;
        GameServer server = null;

        public Receptionist(GameServer server) {
            try {
                this.listener = new ServerSocket(1078);
                System.out.println("start GameServer Version 2.0.0\nServer-IP: " + InetAddress.getLocalHost().getHostAddress());
            } catch (IOException var4) {
                System.err.println("Server-Port bereits belegt\nServer stop");
                System.exit(-1);
                return;
            }

            this.server = server;
            (new Thread(this)).start();
        }

        public void run() {
            while(!this.stopped) {
                try {
                    Socket newSock = this.listener.accept();
                    if (this.server.addPlayer(newSock)) {
                        this.server.consoleOut("Verbindung akzeptiert, neuer Spieler wird erzeugt");
                    } else {
                        this.server.consoleOut("Zu viele Spieler");
                        newSock.close();
                    }
                } catch (IOException var3) {
                    ;
                }
            }

            try {
                this.listener.close();
            } catch (IOException var2) {
                ;
            }

        }

        public void pleaseStop() {
            this.stopped = true;
        }
    }
}

