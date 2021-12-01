package com.nhn.yut2.server.test.scenario.state;

import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.gameanvil.gamehammer.tester.PacketResult;
import com.nhn.gameanvil.gamehammer.tester.Timer;
import com.nhn.gameanvil.gamehammer.tester.User;
import com.nhn.yut2.server.protocol.Yut2GameProto;
import com.nhn.yut2.server.test.scenario.Yut2Actor;
import org.slf4j.Logger;

import java.io.IOException;

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
        if (actor.isListenerResisted) return;
        else actor.isListenerResisted = true;

        User user = actor.getUser();
        user.addListener(Yut2GameProto.EnterRoomToC.getDescriptor(), packetResult -> OnEnterRoomToC( actor, packetResult ));
        user.addListener(Yut2GameProto.GameReadyCompleteNoti.getDescriptor(), packetResult -> OnGameReadyCompleteNoti( actor, packetResult ));
        user.addListener(Yut2GameProto.GameStartToC.getDescriptor(), packetResult -> OnGameStartToC( actor, packetResult ));
        user.addListener(Yut2GameProto.SelectGameLeaderToC.getDescriptor(), packetResult -> OnSelectGameLeaderToC( actor, packetResult ));
        user.addListener(Yut2GameProto.ResultGameLeaderNoti.getDescriptor(), packetResult -> OnResultGameLeaderNoti( actor, packetResult ));
        user.addListener(Yut2GameProto.GameInitializeNoti.getDescriptor(), packetResult -> OnGameInitializedNoti( actor, packetResult ));
        user.addListener(Yut2GameProto.CurrentTurnNoti.getDescriptor(), packetResult -> OnCurrentTurnNoti( actor, packetResult ));
        user.addListener(Yut2GameProto.TossYutToC.getDescriptor(), packetResult -> OnTossYutToC( actor, packetResult ));
        user.addListener(Yut2GameProto.MovePawnToC.getDescriptor(), packetResult -> OnMovePawnToC( actor, packetResult ));
        user.addListener(Yut2GameProto.GamePlayCompleteNoti.getDescriptor(), packetResult -> OnGamePlayCompleteNoti( actor, packetResult ));
    }

    private void OnEnterRoomToC(Yut2Actor actor, PacketResult packetResult) {
        Yut2GameProto.EnterRoomToC message = null;
        try {
            message = Yut2GameProto.EnterRoomToC.parseFrom(packetResult.getStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (message != null) {
            if (message.getRetCode() == 0) {
                int hostNo = message.getRoomData().getHostSeatNo();
                logger.info("<==EnterRoomToC[{}-{}]: roomid={} host={}",actor.getIndex(), actor.getNickname() , message.getRoomData().getRoomId(), hostNo);

                for (Yut2GameProto.RoomMemberInfo info : message.getRoomData().getMembersList()) {
                    if (info.getBaseInfo().getMemberId().equals(actor.getNickname())) {
                        actor.seatNo = info.getSeatNo();
                        actor.isHost = (actor.seatNo == hostNo);
                        logger.info("actor [{}] : seatNo {} - Host {}",  actor.getNickname(), actor.seatNo, actor.isHost);
                    }
                }

                boolean isMyTurn = (actor.seatNo == message.getRoomData().getCurrentTurnInfo().getSeatNo());

                switch (message.getRoomData().getRoomState()) {
                    case ROOM_READY :
                        sendComplete(actor, Yut2GameProto.ClientCompleteType.Enter_Room);
                        break;
                    case ROOM_ELECT_LEADER:
                        sendComplete(actor, Yut2GameProto.ClientCompleteType.Result_Leader);
                        break;
                    case ROOM_TOSS_YUT:
                        if (!isMyTurn) {
                            sendComplete(actor, Yut2GameProto.ClientCompleteType.Toss_Yut);
                        }
                        break;
                    case ROOM_MOVE_PAWN:
                        if (!isMyTurn) {
                            sendComplete(actor, Yut2GameProto.ClientCompleteType.Move_Pawn);
                        }
                        break;
                    case ROOM_FINISH:
                        actor.changeState(_7_LeaveRoomState.class);
                        break;
                    default:
                        logger.warn("EnterRoomToC : unhandle message - {}" , message.getRoomData().getRoomState());
                        break;
                }

            }
            else logger.error("EnterRoomToC Error : {}", actor.getNickname());
        }
        else logger.error("EnterRoomToC is Null");
    }

    private void OnGameReadyCompleteNoti(Yut2Actor actor, PacketResult packetResult) {
        Yut2GameProto.GameReadyCompleteNoti message = null;
        try {
            message = Yut2GameProto.GameReadyCompleteNoti.parseFrom(packetResult.getStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (message != null) {
            User user = actor.getUser();
            logger.info("<==GameReadyCompleteNoti[{}-{}]", actor.getIndex(), actor.getNickname());

            // 선이면 시작 눌러주자
            if (actor.isHost && !actor.isPlay) {
                Yut2GameProto.GameStartToS.Builder packet = Yut2GameProto.GameStartToS.newBuilder();
                user.send(packet.build());
                logger.info("-->GameStartToS[{}]", actor.getNickname());
            }
        }
        else logger.error("GameReadyCompleteNoti Error [{}] : message null", actor.getNickname());
    }

    private void OnGameStartToC(Yut2Actor actor, PacketResult packetResult) {
        Yut2GameProto.GameStartToC message = null;
        try {
            message = Yut2GameProto.GameStartToC.parseFrom(packetResult.getStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (message != null) {
            User user = actor.getUser();
            if (message.getRetCode() == 0) {
                logger.info("<==GameStartToC [{}-{}] RetCode : {}",  actor.getIndex(), actor.getNickname(), message.getRetCode());
                actor.isPlay = true;
                if (message.getGameLeaderSeatNo() == -1) {
                    Yut2GameProto.SelectGameLeaderToS.Builder req = Yut2GameProto.SelectGameLeaderToS.newBuilder();
                    req.setYutIndex(-1);
                    user.send(req.build());
                    logger.info("-->SelectGameLeaderToS[{}]: -1", actor.getNickname());
                }
                else {
                    sendComplete(actor, Yut2GameProto.ClientCompleteType.Play_Game_Ready);
                }
            }
            else if (message.getRetCode() != 3003){
                logger.error("GameStartToC [{}] RetCode : {}", actor.getNickname(), message.getRetCode());
            }
        }
        else logger.error("GameStartToC Error : {}", actor.getNickname());
    }

    private void OnSelectGameLeaderToC(Yut2Actor actor, PacketResult packetResult) {
        Yut2GameProto.SelectGameLeaderToC message = null;
        try {
            message = Yut2GameProto.SelectGameLeaderToC.parseFrom(packetResult.getStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (message != null) {
            actor.isPlay = true;
            logger.info("<==SelectGameLeaderToC[{}-{}]: seatNo={}", actor.getIndex(), actor.getNickname(), message.getSeatNo());
            // 단순 선표시이므로 선 종료가 아님
        }
        else logger.error("SelectGameLeaderToC Error : {}", actor.getNickname());
    }

    private void OnResultGameLeaderNoti(Yut2Actor actor, PacketResult packetResult) {
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
    }

    private void OnGameInitializedNoti(Yut2Actor actor, PacketResult packetResult) {
        Yut2GameProto.GameInitializeNoti message = null;
        try {
            message = Yut2GameProto.GameInitializeNoti.parseFrom(packetResult.getStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (message != null) {
            actor.isPlay = true;
            logger.info("<==GameInitializeNoti [{}]: seatNo={}", actor.getNickname(), actor.seatNo);
            sendComplete(actor, Yut2GameProto.ClientCompleteType.Default_Bet);
        }
        else logger.error("GameInitializeNoti Error : {}", actor.getNickname());
    }

    private void OnGamePlayCompleteNoti(Yut2Actor actor, PacketResult packetResult) {
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
    }

    private void OnMovePawnToC(Yut2Actor actor, PacketResult packetResult) {
        Yut2GameProto.MovePawnToC message = null;
        try {
            message = Yut2GameProto.MovePawnToC.parseFrom(packetResult.getStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (message != null) {
            if (message.getRetCode() == 0) {
                sendComplete(actor, Yut2GameProto.ClientCompleteType.Move_Pawn);
            }
            else {
                logger.info("<==MovePawnToC [{}]: seatNo={} Error={}", actor.getNickname(), message.getSeatNo(), message.getRetCode());
            }
        }
        else {
            logger.error("MovePawnToC is Null");
        }
    }

    private void OnTossYutToC(Yut2Actor actor, PacketResult packetResult) {
        Yut2GameProto.TossYutToC message = null;
        try {
            message = Yut2GameProto.TossYutToC.parseFrom(packetResult.getStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (message != null ) {
            if (message.getRetCode() == 0) {
                sendComplete(actor, Yut2GameProto.ClientCompleteType.Toss_Yut);
            }
            else {
                logger.info("<==TossYutToC [{}]: seatNo={} Error = {}", actor.getNickname(), message.getSeatNo(), message.getRetCode());
            }
        }
        else {
            logger.error("<== TossYutToC is Null");
        }
    }

    private void OnCurrentTurnNoti(Yut2Actor actor, PacketResult packetResult) {
        Yut2GameProto.CurrentTurnNoti message = null;
        try {
            message = Yut2GameProto.CurrentTurnNoti.parseFrom(packetResult.getStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (message != null) {
            /*
            User user = actor.getUser();
            logger.info("<== CurrentTurnNoti [{}] : turn {}", actor.getNickname(), message.getSeatNo());
            if (actor.seatNo == message.getSeatNo()) {
                if (message.getCanToss()) {
                    // Toss Yut
                    Yut2GameProto.TossYutToS.Builder req = Yut2GameProto.TossYutToS.newBuilder();
                    req.setSelectChanceIndex(-1);
                    user.send(req.build());
                    logger.info("-->TossYutToS[{}]: -1", user.getUserId());
                } else {
                    logger.info("wait server action [{}], remain cnt : {}", actor.getUser().getUserId(), message.getRemainPaeListCount());
                }
            }
            */
        }
        else logger.error("CurrentTurnNoti Error : {}", actor.getNickname());
    }

    void enterRoomAction(Yut2Actor actor) {
        Yut2GameProto.EnterRoomToS.Builder enterRoom = Yut2GameProto.EnterRoomToS.newBuilder();
        actor.getUser().send(enterRoom.build());

        actor.setTimer(() -> checkGameStart(actor), 3000);
        logger.info("-->EnterRoomToS[{}]", actor.getNickname());
    }

    //
    void checkGameStart(Yut2Actor actor) {
        if (actor.isPlay) {
            logger.info("checkGameStart - Play Game! [{}] - {}", actor.getNickname(), actor.waitCount);
        }
        else if (actor.waitCount < 5) {
            actor.waitCount++;
            actor.setTimer(() -> checkGameStart(actor), 1000);
            logger.info("checkGameStart [{}]: wait : {}", actor.getNickname(), actor.waitCount);
        }
        else {
            logger.info("checkGameStart [{}]: leaveRoom - {}", actor.getNickname(), actor.waitCount);
            actor.changeState(_7_LeaveRoomState.class);
        }
    }

    void sendComplete(Yut2Actor actor, Yut2GameProto.ClientCompleteType type){
        Yut2GameProto.CompleteTypeToS.Builder complete = Yut2GameProto.CompleteTypeToS.newBuilder();
        complete.setCompleteType(type);
        actor.getUser().send(complete.build());
        // logger.info("-->CompleteTypeToS[{}]: {}", actor.getNickname(), complete.getCompleteType());
    }

    @Override
    protected void onExit(Yut2Actor actor) {
        logger.info("Yut2Actor idx[{}-{}] - onExit : {}", actor.getIndex(), actor.getNickname(), getStateName());
        /*
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
        */
    }
}

