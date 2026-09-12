package com.hospital;

/**
 * Standard launcher to circumvent JavaFX runtime module bootstrap checks in packaged JARs.
 */
public class MainLauncher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
