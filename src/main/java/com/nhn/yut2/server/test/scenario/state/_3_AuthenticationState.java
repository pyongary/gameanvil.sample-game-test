package com.nhn.yut2.server.test.scenario.state;

import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.gameanvil.gamehammer.tool.UuidGenerator;

import com.nhn.yut2.server.protocol.Yut2GameProto;
import com.nhn.yut2.server.test.scenario.Yut2Actor;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

// 인증 처리 상태
public class _3_AuthenticationState extends State<Yut2Actor> {
    private static final Logger logger = getLogger(_3_AuthenticationState.class);
    protected static UuidGenerator connectionDeviceIdGenerator = new UuidGenerator("DeviceId");

    @Override
    protected void onEnter(Yut2Actor actor) {
        logger.info("Yut2Actor idx[{}] - onEnter : {}", actor.getIndex(), getStateName());

        // 인증 메세지 생성
        Yut2GameProto.AuthenticationToS.Builder authenticationReq = Yut2GameProto.AuthenticationToS.newBuilder().setAccessToken("test-bot");
        // Authentication.AuthenticationReq.Builder authenticationReq = Authentication.AuthenticationReq.newBuilder().setAccessToken("TapTap_AccessToken!!!!");

        actor.nickname = "Bot" + actor.getConnection().getUuid();
        // 인증 요청
        actor.getConnection().authentication(authenticationResult -> {
            if (authenticationResult.isSuccess()) {
                actor.changeState(_4_LoginState.class);
            } else {
                logger.info("[{}] Fail - uuid : {}, AccountId : {} \t{}, {}",
                    getStateName(),
                    actor.getConnection().getUuid(),
                    actor.getConnection().getUuid(),
                    authenticationResult.getErrorCode(),
                    authenticationResult.getResultCode()
                );
                actor.finish(false);
            }
        }, actor.nickname, String.valueOf(actor.getConnection().getUuid()), connectionDeviceIdGenerator.generateUniqueId(), authenticationReq);
    }

    @Override
    protected void onExit(Yut2Actor actor) {
        logger.info("Yut2Actor idx[{}] - onExit : {}", actor.getIndex(), getStateName());
    }
}
