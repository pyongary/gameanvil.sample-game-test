package com.nhn.yut2.server.test.scenario.state;

import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.yut2.server.test.scenario.Yut2Actor;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

// 방생성 요청 상태
public class _6_StartGame extends State<Yut2Actor> {
    private static final Logger logger = getLogger(_6_StartGame.class);

    @Override
    protected void onEnter(Yut2Actor actor) {
        logger.debug("Yut2Actor idx[{}] - onEnter : {}", actor.getIndex(), getStateName());

    }

    @Override
    protected void onExit(Yut2Actor actor) {
        logger.debug("TapTapActor idx[{}] - onExit : {}", actor.getIndex(), getStateName());
    }
}

