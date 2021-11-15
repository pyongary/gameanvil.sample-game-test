package com.nhn.yut2.server.test.scenario.state;

import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.gameanvil.gamehammer.tester.Packet;
import com.nhn.yut2.server.protocol.Yut2GameProto;
import com.nhn.yut2.server.test.common.GameConstants;
import com.nhn.yut2.server.test.scenario.Yut2Actor;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class _4_LoginState extends State<Yut2Actor> {
    private static final Logger logger = getLogger(_4_LoginState.class);

    @Override
    protected void onEnter(Yut2Actor actor) {

        logger.info("Yut2Actor idx[{}] - onEnter : {}", actor.getIndex(), getStateName());

        // 로그인 데이터
        Yut2GameProto.LoginToS.Builder loginReq = Yut2GameProto.LoginToS.newBuilder();
        Yut2GameProto.AuthenticationInfo.Builder info = Yut2GameProto.AuthenticationInfo.newBuilder();
        info.setAppId("YIfoKwzQ");
        info.setClientVersion("1.0.0");
        info.setDeviceInfo("Bot");
        info.setOsVersion("1.0.0");
        info.setSdkVersion("1.0.0");
        info.setStoreCode("NONE");
        loginReq.setInfo(info);

        actor.getUser().login(loginRes -> {
            if (loginRes.isSuccess()) {
                if (loginRes.getRejoinedRoomId() > 0) {
                    // Continue Game
                    logger.info("Yut2Actor Nick[{}] - Already Game Room", actor.getNickname());
                    Yut2GameProto.ReserveFlagToS.Builder req = Yut2GameProto.ReserveFlagToS.newBuilder();
                    req.setReserveFlag(Yut2GameProto.ReserveFlag.MobileResume);
                    actor.getUser().send(req.build());
                    actor.changeState(_6_EnterRoomState.class);
                }
                else {
                    // matchroom
                    actor.changeState(_5_MatchRoomState.class);
                }
            } else {
                /*
                try {
                    Packet payload = loginRes.getPacketFromPayload(Yut2GameProto.LoginToC.getDescriptor());
                    Yut2GameProto.LoginToC loginInfo = Yut2GameProto.LoginToC.parseFrom(payload.getStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                */
                logger.error(
                    "[{}] Fail - uuid : {}, AccountId : {}\t{}, {}",
                    getStateName(),
                    actor.getConnection().getUuid(),
                    actor.getConnection().getAccountId(),
                    loginRes.getErrorCode(),
                    loginRes.getResultCode()
                );
                actor.finish(false);
            }
        }, GameConstants.GAME_USER_TYPE, actor.getUser().getChannelId(), loginReq);
    }

    @Override
    protected void onExit(Yut2Actor actor) {
        logger.info("Yut2Actor idx[{}] - onExit : {}", actor.getIndex(), getStateName());
    }

}
