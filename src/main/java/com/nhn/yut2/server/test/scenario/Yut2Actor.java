package com.nhn.yut2.server.test.scenario;

import com.nhn.gameanvil.gamehammer.scenario.ScenarioActor;
import com.nhn.gameanvil.gamehammer.tester.User;

// 시나리오 유저
public class Yut2Actor extends ScenarioActor<Yut2Actor> {
    private User user;
    public int seatNo;
    public boolean isHost = false;
    public boolean isPlay = false;
    public boolean isListenerResisted = false;
    public int waitCount = 0;
    // public Timer timer;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getNickname() {
        return this.getConnection().getAccountId();
    }

    public void reset() {
        isHost = false;
        isPlay = false;
        waitCount = 0;
    }
}
