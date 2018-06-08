package com.playground.ewnclient;

import java.io.Reader;
import java.util.Arrays;
import java.util.Scanner;

public class ConsoleClient implements IClient, Runnable {

    boolean isRunning;
    private Reader reader;

    ConsoleClient(Reader reader) {
        this.reader = reader;
        this.isRunning = true;
    }

    String readInput() {
        Scanner scanner = new Scanner(reader);

        String input = scanner.next();
        System.out.println("Received input: " + input);

        return input;
    }

    void processInput(String input) {
        ClientActions clientAction = ClientActions.valueOf(input.toUpperCase());

        switch (clientAction) {
            case LOGIN:
                login();
                break;
            case LOGOUT:
                logout();
                break;
            case CONNECT:
                connect();
                break;
            case PLAY:
                play();
                break;
            case HELP:
                help();
                break;
            case EXIT:
                exit();
                break;
            default:
                System.out.println("Unknown command. Type 'help' for a list of valid commands.");
        }
    }

    public void login() {

    }

    public void logout() {

    }

    public void connect() {

    }

    public void play() {

    }

    public void help() {
        System.out.println("Available commands (case insensitive): " + Arrays.toString(ClientActions.values()));
    }

    public void exit() {
        System.out.println("Exiting");
        this.isRunning = false;
    }

    public void run() {
        while (this.isRunning) {
            this.processInput(this.readInput());
        }
    }
}
