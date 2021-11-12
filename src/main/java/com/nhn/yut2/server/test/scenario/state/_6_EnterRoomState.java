package com.nhn.yut2.server.test.scenario.state;

import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.gameanvil.gamehammer.tester.ResultMatchRoom;
import com.nhn.gameanvil.gamehammer.tester.User;
import com.nhn.yut2.server.protocol.Yut2GameProto;
import com.nhn.yut2.server.test.common.GameConstants;
import com.nhn.yut2.server.test.scenario.Yut2Actor;
import org.slf4j.Logger;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

// 방생성 요청 상태
public class _6_EnterRoomState extends State<Yut2Actor> {
    private static final Logger logger = getLogger(_6_EnterRoomState.class);

    @Override
    protected void onEnter(Yut2Actor actor) {
        logger.info("Yut2Actor idx[{}-{}] - onEnter : {}", actor.getIndex(), actor.getNickname(), getStateName());
        addRoomListener(actor);
        enterRoomAction(actor);
    }

    private void addRoomListener(Yut2Actor actor) {
        User user = actor.getUser();

        user.addListener(Yut2GameProto.EnterRoomToC.getDescriptor(), packetResult -> {
            Yut2GameProto.EnterRoomToC message = null;
            try {
                message = Yut2GameProto.EnterRoomToC.parseFrom(packetResult.getStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (message != null) {
                int hostNo = message.getRoomData().getHostSeatNo();
                logger.info("<==EnterRoomToC[{}-{}]: roomid={} host={}",actor.getIndex(), actor.getNickname() , message.getRoomData().getRoomId(), hostNo);

                for (Yut2GameProto.RoomMemberInfo info : message.getRoomData().getMembersList()) {
                    if (info.getBaseInfo().getMemberId().equals(actor.getNickname())) {
                        actor.seatNo = info.getSeatNo();
                        actor.isHost = (actor.seatNo == hostNo);
                        logger.info("actor [{}] : seatNo {} - Host {}",  actor.getNickname(), actor.seatNo, actor.isHost);
                    }
                }

                sendComplete(actor, Yut2GameProto.ClientCompleteType.Enter_Room);
            }
            else logger.error("EnterRoomToC Error : {}", actor.getNickname());
        });

        user.addListener(Yut2GameProto.GameReadyCompleteNoti.getDescriptor(), packetResult -> {
            Yut2GameProto.GameReadyCompleteNoti message = null;
            try {
                message = Yut2GameProto.GameReadyCompleteNoti.parseFrom(packetResult.getStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (message != null) OnGameReadyCompleteNoti(actor);
            else logger.error("GameReadyCompleteNoti Error [{}] : message null", actor.getNickname());
        });

        user.addListener(Yut2GameProto.GameStartToC.getDescriptor(), packetResult -> {
            Yut2GameProto.GameStartToC message = null;
            try {
                message = Yut2GameProto.GameStartToC.parseFrom(packetResult.getStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (message != null) OnGameStartToC(actor, message);
            else logger.error("GameStartToC Error : {}", actor.getNickname());
        });

        user.addListener(Yut2GameProto.SelectGameLeaderToC.getDescriptor(), packetResult -> {
            Yut2GameProto.SelectGameLeaderToC message = null;
            try {
                message = Yut2GameProto.SelectGameLeaderToC.parseFrom(packetResult.getStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (message != null) {
                logger.info("<==SelectGameLeaderToC[{}-{}]: seatNo={}", actor.getIndex(), actor.getNickname(), message.getSeatNo());
                // 단순 선표시이므로 선 종료가 아님
            }
            else logger.error("SelectGameLeaderToC Error : {}", actor.getNickname());
        });

        user.addListener(Yut2GameProto.ResultGameLeaderNoti.getDescriptor(), packetResult -> {
            Yut2GameProto.ResultGameLeaderNoti message = null;
            try {
                message = Yut2GameProto.ResultGameLeaderNoti.parseFrom(packetResult.getStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (message != null) {
                logger.info("<==ResultGameLeaderNoti[{}-{}]: Sun={}", actor.getIndex(), actor.getNickname(), message.getSeatNo());
                // 선선택 결과
                sendComplete(actor, Yut2GameProto.ClientCompleteType.Result_Leader);
            }
            else logger.error("ResultGameLeaderNoti Error : {}", actor.getNickname());
        });


        user.addListener(Yut2GameProto.GameInitializeNoti.getDescriptor(), packetResult -> {
            Yut2GameProto.GameInitializeNoti message = null;
            try {
                message = Yut2GameProto.GameInitializeNoti.parseFrom(packetResult.getStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (message != null) {
                logger.info("<==GameInitializeNoti [{}]: seatNo={}", actor.getNickname(), actor.seatNo);
                sendComplete(actor, Yut2GameProto.ClientCompleteType.Default_Bet);
            }
            else logger.error("GameInitializeNoti Error : {}", actor.getNickname());
        });

        user.addListener(Yut2GameProto.CurrentTurnNoti.getDescriptor(), packetResult -> {
            Yut2GameProto.CurrentTurnNoti message = null;
            try {
                message = Yut2GameProto.CurrentTurnNoti.parseFrom(packetResult.getStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (message != null) {
                OnCurrentTurnNoti(message, actor);
            }
            else logger.error("CurrentTurnNoti Error : {}", actor.getNickname());
        });

        user.addListener(Yut2GameProto.TossYutToC.getDescriptor(), packetResult -> {
            Yut2GameProto.TossYutToC message = null;
            try {
                message = Yut2GameProto.TossYutToC.parseFrom(packetResult.getStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (message != null && message.getRetCode() == 0) {
                // logger.info("<==TossYutToC [{}]: seatNo={}", actor.getNickname(), message.getSeatNo());
                sendComplete(actor, Yut2GameProto.ClientCompleteType.Toss_Yut);
            }
            else logger.error("TossYutToC Error[{}] : {}", actor.getNickname(), message.getRetCode());
        });

        user.addListener(Yut2GameProto.MovePawnToC.getDescriptor(), packetResult -> {
            Yut2GameProto.MovePawnToC message = null;
            try {
                message = Yut2GameProto.MovePawnToC.parseFrom(packetResult.getStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (message != null && message.getRetCode() == 0) {
                // logger.info("<==MovePawnToC [{}]: seatNo={}", actor.getNickname(), message.getSeatNo());
                sendComplete(actor, Yut2GameProto.ClientCompleteType.Move_Pawn);
            }
            else logger.error("MovePawnToC Error[{}] : {}", actor.getNickname(), message.getRetCode());
        });

        user.addListener(Yut2GameProto.GamePlayCompleteNoti.getDescriptor(), packetResult -> {
            Yut2GameProto.GamePlayCompleteNoti message = null;
            try {
                message = Yut2GameProto.GamePlayCompleteNoti.parseFrom(packetResult.getStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (message != null) {
                logger.info("<==GamePlayCompleteNoti [{}]: WinNo={}", actor.getNickname(), message.getWinSeatNo());
                actor.changeState(_7_LeaveRoomState.class);
            }
            else logger.error("GamePlayCompleteNoti Error[{}]", actor.getNickname());
        });
    }

    void enterRoomAction(Yut2Actor actor) {
        Yut2GameProto.EnterRoomToS.Builder enterRoom = Yut2GameProto.EnterRoomToS.newBuilder();
        actor.getUser().send(enterRoom.build());
        logger.info("-->EnterRoomToS[{}]", actor.getNickname());
    }

    void sendComplete(Yut2Actor actor, Yut2GameProto.ClientCompleteType type){
        Yut2GameProto.CompleteTypeToS.Builder complete = Yut2GameProto.CompleteTypeToS.newBuilder();
        complete.setCompleteType(type);
        actor.getUser().send(complete.build());
        // logger.info("-->CompleteTypeToS[{}]: {}", actor.getNickname(), complete.getCompleteType());
    }

    void OnGameReadyCompleteNoti(Yut2Actor actor){
        User user = actor.getUser();
        logger.info("<==GameReadyCompleteNoti[{}-{}]", actor.getIndex(), actor.getNickname());

        // 선이면 시작 눌러주자
        if (actor.isHost && actor.isPlay == false) {
            Yut2GameProto.GameStartToS.Builder packet = Yut2GameProto.GameStartToS.newBuilder();
            user.send(packet.build());
            logger.info("-->GameStartToS[{}]", actor.getNickname());
        }
    }

    void OnGameStartToC(Yut2Actor actor, Yut2GameProto.GameStartToC msg) {
        User user = actor.getUser();
        logger.info("<==GameStartToC [{}-{}] RetCode : {}",  actor.getIndex(), actor.getNickname(), msg.getRetCode());
        if (msg.getRetCode() == 0) {
            actor.isPlay = true;
            if (msg.getGameLeaderSeatNo() == -1) {
                Yut2GameProto.SelectGameLeaderToS.Builder req = Yut2GameProto.SelectGameLeaderToS.newBuilder();
                req.setYutIndex(-1);
                user.send(req.build());
                logger.info("-->SelectGameLeaderToS[{}]: -1", actor.getNickname());
            }
            else {
                Yut2GameProto.CompleteTypeToS.Builder complete = Yut2GameProto.CompleteTypeToS.newBuilder();
                complete.setCompleteType(Yut2GameProto.ClientCompleteType.Play_Game_Ready);
                user.send(complete.build());
                logger.info("-->CompleteTypeToS[{}]: {}", actor.getNickname(), complete.getCompleteType());
            }
        }
        else {
            logger.error("GameStartToC [{}] RetCode : {}", actor.getNickname(), msg.getRetCode());
        }
    }

    void OnCurrentTurnNoti(Yut2GameProto.CurrentTurnNoti msg, Yut2Actor actor) {
        // User user = actor.getUser();
        // logger.info("<== CurrentTurnNoti [{}] : turn {}", actor.getNickname(), msg.getSeatNo());
        /*
        if (actor.seatNo == msg.getSeatNo()) {
            if (msg.getCanToss()) {
                // Toss Yut
                Yut2GameProto.TossYutToS.Builder req = Yut2GameProto.TossYutToS.newBuilder();
                req.setSelectChanceIndex(-1);
                user.send(req.build());
                logger.info("-->TossYutToS[{}]: -1", user.getUserId());
            } else {
                logger.info("wait server action [{}], remain cnt : {}", actor.getUser().getUserId(), msg.getRemainPaeListCount());
            }
        }
        */
    }

    @Override
    protected void onExit(Yut2Actor actor) {
        actor.getUser().removeAllListener(Yut2GameProto.EnterRoomToC.getDescriptor());
        actor.getUser().removeAllListener(Yut2GameProto.GameReadyCompleteNoti.getDescriptor());
        actor.getUser().removeAllListener(Yut2GameProto.GameStartToC.getDescriptor());
        actor.getUser().removeAllListener(Yut2GameProto.SelectGameLeaderToC.getDescriptor());
        actor.getUser().removeAllListener(Yut2GameProto.ResultGameLeaderNoti.getDescriptor());
        actor.getUser().removeAllListener(Yut2GameProto.GameInitializeNoti.getDescriptor());
        actor.getUser().removeAllListener(Yut2GameProto.CurrentTurnNoti.getDescriptor());
        actor.getUser().removeAllListener(Yut2GameProto.TossYutToC.getDescriptor());
        actor.getUser().removeAllListener(Yut2GameProto.MovePawnToC.getDescriptor());
        actor.getUser().removeAllListener(Yut2GameProto.GamePlayCompleteNoti.getDescriptor());

        logger.info("Yut2Actor idx[{}-{}] - onExit : {}", actor.getIndex(), actor.getNickname(), getStateName());
    }
}

