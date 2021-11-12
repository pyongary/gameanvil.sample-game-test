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
        logger.info("Yut2Actor idx[{}] - onEnter : {}", actor.getUser().getUserId(), getStateName());

        // 방나가기
        Yut2GameProto.ReserveFlagToS.Builder reserveFlag = Yut2GameProto.ReserveFlagToS.newBuilder();
        reserveFlag.setReserveFlag(Yut2GameProto.ReserveFlag.LeaveRoom);

        // send complete
        sendComplete(actor, Yut2GameProto.ClientCompleteType.Game_Result);

        // 게임 종료
        actor.getUser().leaveRoom((leaveRoomResult) -> {
            if (leaveRoomResult.isSuccess()) {
                actor.reset();
                actor.changeState(_8_LogoutState.class);
            } else {
                logger.error(
                    "[{}] Fail - uuid : {}, AccountId : {}\t{}, {}",
                    getStateName(),
                    actor.getConnection().getUuid(),
                    actor.getConnection().getAccountId(),
                    leaveRoomResult.getErrorCode(),
                    leaveRoomResult.getPacketResultCode()
                );

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                actor.changeState(_7_LeaveRoomState.class);
            }
        }, reserveFlag);
    }

    void sendComplete(Yut2Actor actor, Yut2GameProto.ClientCompleteType type){
        Yut2GameProto.CompleteTypeToS.Builder complete = Yut2GameProto.CompleteTypeToS.newBuilder();
        complete.setCompleteType(type);
        actor.getUser().send(complete.build());
        logger.info("-->CompleteTypeToS[{}]: {}", actor.getNickname(), complete.getCompleteType());
    }

    @Override
    protected void onExit(Yut2Actor actor) {
        logger.info("Yut2Actor idx[{}] - onExit : {}", actor.getIndex(), getStateName());
    }

}
