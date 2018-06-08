package com.playground.ewnclient;

import java.io.InputStreamReader;

public class App {
    public static void main( String[] args )
    {
        Runnable client = new ConsoleClient(new InputStreamReader(System.in));
        client.run();
    }
}
