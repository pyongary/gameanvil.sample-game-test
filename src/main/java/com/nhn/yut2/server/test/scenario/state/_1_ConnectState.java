package com.nhn.yut2.server.test.scenario.state;

import com.nhn.gameanvil.gamehammer.config.TesterConfigLoader;
import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.gameanvil.gamehammer.tester.ResultConnect.ResultCodeConnect;
import com.nhn.yut2.server.test.common.GameConstants;
import com.nhn.yut2.server.test.scenario.Yut2Actor;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

// 연결 요청 상태
public class _1_ConnectState extends State<Yut2Actor> {
    private static final Logger logger = getLogger(_1_ConnectState.class);

    @Override
    protected void onScenarioTestStart(Yut2Actor scenarioActor) {
        // 서버에서 연결이 끊겼을때 처리
        scenarioActor.getConnection().addListenerDisconnect((resultDisconnect) -> {
            logger.warn("[{}] Disconnected - UUID : {}",
                scenarioActor.getCurrentStateName(),
                scenarioActor.getConnection().getUuid()
            );
            scenarioActor.finish(false);
        });

        scenarioActor.setUser(scenarioActor.getConnection().createUser(GameConstants.GAME_NAME, 1));
    }

    @Override
    protected void onEnter(Yut2Actor actor) {
        logger.info("Yut2Actor idx[{}] - onEnter : {}", actor.getIndex(), getStateName());

        if (actor.getConnection().isConnected()) {
            actor.changeState(_3_AuthenticationState.class);
        } else {
            long connectTime = actor.getCurrTime();
            // 연결 요청
            actor.getConnection().connect(TesterConfigLoader.getInstance().getTesterConfig().getNextTargetServer(), resultConnect -> {
                if (ResultCodeConnect.CONNECT_SUCCESS == resultConnect.getResultCode()) {
                    actor.changeState(_2_RampUpState.class);
                } else {
                    logger.error("[{}] Fail - UUID : {}, errorCode : {}, resultCode : {}, socketException : {}, elapsedTime : {}",
                        getStateName(),
                        actor.getConnection().getUuid(),
                        resultConnect.getErrorCode(),
                        resultConnect.getResultCode(),
                        resultConnect.getSocketError(),
                        actor.getCurrTime() - connectTime
                    );
                    actor.changeState(_1_ConnectState.class);
                }
            });
        }
    }

    @Override
    protected void onExit(Yut2Actor actor) {
        logger.info("Yut2Actor idx[{}] - onExit : {}", actor.getIndex(), getStateName());
    }

}
