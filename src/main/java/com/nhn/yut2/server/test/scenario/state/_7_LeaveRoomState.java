package com.nhn.yut2.server.test.scenario.state;

import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.gameanvil.gamehammer.tester.ResultLeaveRoom;
import com.nhn.yut2.server.protocol.Yut2GameProto;
import com.nhn.yut2.server.test.scenario.Yut2Actor;
import org.slf4j.Logger;

import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

// 방나가는 요청 상태
public class _7_LeaveRoomState extends State<Yut2Actor> {
    private static final Logger logger = getLogger(_7_LeaveRoomState.class);

    @Override
    protected void onEnter(Yut2Actor actor) {
        logger.debug("Yut2Actor idx[{}] - onEnter : {}", actor.getIndex(), getStateName());

        // 방나가기
        Yut2GameProto.ReserveFlagToS.Builder reserveFlag = Yut2GameProto.ReserveFlagToS.newBuilder();
        reserveFlag.setReserveFlag(Yut2GameProto.ReserveFlag.LeaveRoom);

        // 게임 종료
        actor.getUser().leaveRoom((leaveRoomResult) -> {
            if (leaveRoomResult.isSuccess()) {
                actor.changeState(_8_LogoutState.class);
            } else {
                logger.info(
                    "[{}] Fail - uuid : {}, AccountId : {}\t{}, {}",
                    getStateName(),
                    actor.getConnection().getUuid(),
                    actor.getConnection().getAccountId(),
                    leaveRoomResult.getErrorCode(),
                    leaveRoomResult.getPacketResultCode()
                );
                actor.changeState(_8_LogoutState.class);
            }
        }, reserveFlag);


    }

    @Override
    protected void onExit(Yut2Actor actor) {
        logger.debug("TapTapActor idx[{}] - onExit : {}", actor.getIndex(), getStateName());
    }

}
