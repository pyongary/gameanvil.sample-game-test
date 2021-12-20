package com.nhn.yut2.server.test.scenario.state;

import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.gameanvil.gamehammer.tester.PacketResult;
import com.nhn.yut2.server.protocol.Yut2GameProto;
import com.nhn.yut2.server.test.scenario.Yut2Actor;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

// 방 입퇴장을 리스트를 보고 들어가자
public class _9_RoomListState extends State<Yut2Actor> {
    private static final Logger logger = getLogger(_9_RoomListState.class);

    @Override
    protected void onEnter(Yut2Actor actor) {
        logger.info("Yut2Actor idx[{}] - onEnter : {}", actor.getIndex(), getStateName());

        addListener(actor);

        Yut2GameProto.RoomListToS.Builder req = Yut2GameProto.RoomListToS.newBuilder();
        req.setChannelType(2); // 1만쩐
        actor.getUser().send(req.build());
    }

    private void addListener(Yut2Actor actor) {
        if (actor.isChannelListenerRegistered) return;
        else actor.isChannelListenerRegistered = true;

        actor.getUser().addListener(Yut2GameProto.RoomListToC.getDescriptor(), packetResult -> OnRoomList( actor, packetResult ));
    }

    private void OnRoomList(Yut2Actor actor, PacketResult packetResult) {
        Yut2GameProto.RoomListToC message = null;
        try {
            message = Yut2GameProto.RoomListToC.parseFrom(packetResult.getStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (message != null) {
            logger.info("<==RoomListToC[{}-{}]", actor.getIndex(), actor.getNickname());

            // room option
            Yut2GameProto.RoomOption.Builder option = Yut2GameProto.RoomOption.newBuilder();
            option.setRoomTitle("CreateByBot");
            option.setChannelType(2);
            option.setMaxPlayer(3);
/*
            List<Yut2GameProto.RoomListData> listRoom = message.getRoomListList();
            for(Yut2GameProto.RoomListData data : listRoom) {
                if (data.getMaxPlayer() > data.getCurPlayer()) {
                    DoJoinRoom(actor, data.getRoomId(), option);
                    return;
                }
            }
            // join시도가 실패했다면 방만들기를 하자
 */
            DoCreateRoom(actor, option);

        }
        else logger.error("RoomListToC Error [{}] : message null", actor.getNickname());
    }

    private void DoCreateRoom(Yut2Actor actor, Yut2GameProto.RoomOption.Builder option) {
        actor.getUser().createRoom(resultCreateRoom -> {
            if(resultCreateRoom.isSuccess()) {
                actor.changeState(_6_EnterRoomState.class);
            } else {
                logger.warn( "[{}] Fail - uuid : {}, AccountId : {}\t{}, {}",
                    getStateName(),
                    actor.getConnection().getUuid(),
                    actor.getConnection().getAccountId(),
                    resultCreateRoom.getErrorCode(),
                    resultCreateRoom.getPacketResultCode()
                );
            }
        }, "TYPE_CUSTOM_CLASSIC", option);
    }

    private void DoJoinRoom(Yut2Actor actor, int roomId, Yut2GameProto.RoomOption.Builder option) {
        actor.getUser().joinRoom(resultJoinRoom -> {
            if (resultJoinRoom.isSuccess()) {
                actor.changeState(_6_EnterRoomState.class);
            } else {
                logger.warn( "[{}] Fail - uuid : {}, AccountId : {}\t{}, {}",
                    getStateName(),
                    actor.getConnection().getUuid(),
                    actor.getConnection().getAccountId(),
                    resultJoinRoom.getErrorCode(),
                    resultJoinRoom.getPacketResultCode()
                );
                actor.changeState(_9_RoomListState.class);
            }
        }, "TYPE_CUSTOM_CLASSIC", roomId, option);
    }

    @Override
    protected void onExit(Yut2Actor actor) {
        logger.info("Yut2Actor idx[{}] - onExit : {}", actor.getUser().getUserId(), getStateName());
    }

}

