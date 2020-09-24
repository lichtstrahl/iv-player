package root.iv.ivplayer.service.command;

import lombok.Getter;

public abstract class BaseCommand implements Runnable {

    protected boolean active;
    @Getter
    protected boolean started;
    protected Runnable action;

    public BaseCommand(Runnable action) {
        active = true;
        started = false;
    }

    @Override
    public void run() {
        started = true;

        while (active) {
            action.run();
        }
    }

    public void stop() {
        active = false;
    }
}
