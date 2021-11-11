package com.nhn.yut2.server.test.scenario.state;

import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.yut2.server.test.scenario.Yut2Actor;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

// 로그아웃 요청 상태
public class _8_LogoutState extends State<Yut2Actor> {
    private static final Logger logger = getLogger(_8_LogoutState.class);

    @Override
    protected void onEnter(Yut2Actor actor) {
        logger.info("Yut2Actor idx[{}] - onEnter : {}", actor.getIndex(), getStateName());
        actor.getUser().logout(result -> {
            if (result.isSuccess()) {
                actor.finish(true);
            } else {
                logger.error(
                    "[{}] Fail - uuid : {}, AccountId : {}\t{}, {}",
                    getStateName(),
                    actor.getConnection().getUuid(),
                    actor.getConnection().getAccountId(),
                    result.getErrorCode(),
                    result.getResultCode()
                );
                actor.changeState(_8_LogoutState.class);
            }
        });
    }

    @Override
    protected void onExit(Yut2Actor actor) {
        logger.info("Yut2Actor idx[{}] - onExit : {}", actor.getIndex(), getStateName());
    }

}

