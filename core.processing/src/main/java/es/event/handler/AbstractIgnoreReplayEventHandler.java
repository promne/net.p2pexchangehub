package es.event.handler;

import org.axonframework.eventhandling.replay.ReplayAware;

public abstract class AbstractIgnoreReplayEventHandler implements ReplayAware {

    private boolean replay;
    
    @Override
    public void beforeReplay() {
        replay = true;
    }

    @Override
    public void afterReplay() {
        replay = false;
    }

    @Override
    public void onReplayFailed(Throwable cause) {
        replay = false;
    }

    protected boolean isLive() {
        return !replay;
    }
    
}
