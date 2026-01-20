package com.buuz135.simpleclaims.util;

import com.buuz135.simpleclaims.claim.ClaimManager;

public class PartyInactivityThread extends Thread {

    private boolean running = true;

    public PartyInactivityThread() {
        this.setName("PartyInactivityTickingSystem");
        this.setDaemon(true);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        while (running) {
            try {
                ClaimManager.getInstance().disbandInactiveParties();
                Thread.sleep(10 * 60 * 1000); // Every 10 min
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    public void stopThread() {
        running = false;
        this.interrupt();
    }
}
