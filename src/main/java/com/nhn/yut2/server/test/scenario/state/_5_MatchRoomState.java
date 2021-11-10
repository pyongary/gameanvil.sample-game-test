package com.nhn.yut2.server.test.scenario.state;

import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.yut2.server.protocol.Yut2GameProto;
import com.nhn.yut2.server.test.common.GameConstants;
import com.nhn.yut2.server.test.scenario.Yut2Actor;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

// 방생성 요청 상태
public class _5_MatchRoomState extends State<Yut2Actor> {
    private static final Logger logger = getLogger(_5_MatchRoomState.class);

    @Override
    protected void onEnter(Yut2Actor actor) {
        logger.debug("YutUser idx[{}] - onEnter : {}", actor.getIndex(), getStateName());

        // 방입장
        Yut2GameProto.RoomOption.Builder roomOption = Yut2GameProto.RoomOption.newBuilder();
        roomOption.setChannelType(GameConstants.GameChannelType.Seed1k.ordinal());

        actor.getUser().matchRoom(matchRoomResult -> {
            if (matchRoomResult.isSuccess()) {
                actor.changeState(_6_StartGame.class);
            } else {
                logger.info(
                        "[{}] Fail - uuid : {}, AccountId : {}\t{}, {}",
                        getStateName(),
                        actor.getConnection().getUuid(),
                        actor.getConnection().getAccountId(),
                        matchRoomResult.getErrorCode(),
                        matchRoomResult.getPacketResultCode()
                );
                actor.changeState(_8_LogoutState.class);
            }
        }, GameConstants.GAME_ROOM_TYPE_CLASSIC, true, false, roomOption );

    }

    @Override
    protected void onExit(Yut2Actor actor) {
        logger.debug("TapTapActor idx[{}] - onExit : {}", actor.getIndex(), getStateName());
    }
}

