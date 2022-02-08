package com.nhn.yut2.server.test.unit;

import com.nhn.gameanvil.gamehammer.tester.*;

import com.nhn.yut2.server.protocol.Yut2GameProto;
import com.nhn.yut2.server.test.common.GameConstants;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.slf4j.LoggerFactory.getLogger;

public class CheckGame extends Fixture {
    private static final Logger logger = getLogger(CheckGame.class);

    //-------------------------------------------------------------------------------------

    @BeforeClass
    public static void beforeClass() {
        // 테스트 하려는 서버의 IP 와 Port 를 지정합니다.
        // initConnector("dev-sinyutnori2.hangame.com", 11300);
        initConnector("dev-sinyutnori2.hangame.com", 11300, true);
    }

    @AfterClass
    public static void afterClass() {
        resetConnect();
    }

    @After
    public void after() {
        // 테스트 한번 끝날때 마다 모든 커넥션 해제
        closeAllConnections();
    }

    //-------------------------------------------------------------------------------------
    @Test
    public void ConnectAIServer() throws IOException {
        String data = "{\"gn\":2222, \"fen\":\"2/000000|000000|//0|0|/0/2/\"}";
        String res = HttpConnector.Post(data);

        logger.info("ConnectAIServer : " + res);
    }

    public void matchFor2Player() {
        // 유저 2명 생성

        List<User> userList = getCreateUsers(GameConstants.GAME_NAME, GameConstants.GAME_USER_TYPE, 2);
        for (User user : userList) {
            registGameListener(user);
        }

        for (User user : userList) {
            enterRoom(user, GameConstants.GameChannelType.Seed1k);
        }

        for (User user : userList) {
            leaveRoom(user);
            disconnect(user);
        }
    }

    void registGameListener(User user){
        user.addListener(Yut2GameProto.EnterRoomToC.getDescriptor(), packetResult -> {
            Yut2GameProto.EnterRoomToC message = null;
            try {
                message = Yut2GameProto.EnterRoomToC.parseFrom(packetResult.getStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            assertTrue(message != null);
            OnEnterRoomToC(message, user);
        });

        user.addListener(Yut2GameProto.GameReadyCompleteNoti.getDescriptor(), packetResult -> {
            Yut2GameProto.GameReadyCompleteNoti message = null;
            try {
                message = Yut2GameProto.GameReadyCompleteNoti.parseFrom(packetResult.getStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            assertTrue(message != null);
            OnGameReadyCompleteNoti(user);
        });
    }

    void enterRoom (User user, GameConstants.GameChannelType channelType) {
        Yut2GameProto.RoomOption.Builder roomOption = Yut2GameProto.RoomOption.newBuilder();
        roomOption.setChannelType(channelType.ordinal());

        ResultMatchRoom res = matchRoom(user, GameConstants.GAME_ROOM_TYPE_CLASSIC, true, false, roomOption);
        assertTrue(res.isSuccess());
        logger.info("enterRoom: roomid={}, userid={}", res.getRoomId(), user.getUserId());

        Yut2GameProto.EnterRoomToS.Builder enterRoom = Yut2GameProto.EnterRoomToS.newBuilder();
        user.send(enterRoom.build());
    }

    void leaveRoom (User user) {
        // 나가기 예약
        Yut2GameProto.ReserveFlagToS.Builder reserveFlag = Yut2GameProto.ReserveFlagToS.newBuilder();
        reserveFlag.setReserveFlag(Yut2GameProto.ReserveFlag.LeaveRoom);

        // 방나가기
        ResultLeaveRoom leaveRoomRes = leaveRoom(user, reserveFlag);
        assertTrue(leaveRoomRes.isSuccess());

        logger.info("leaveRoom: roomid={}, userid={}", leaveRoomRes.getRoomId(), user.getUserId());
    }

    void disconnect(User user) {
        // 로그아웃
        ResultLogout logoutResult = logout(user);
        assertTrue(logoutResult.isSuccess());

        logger.info("disconnect: resultCode={}, userid={}", logoutResult.getResultCode(), user.getUserId());
    }

    public void matchRoomCheck() {
        // 기본 로그인
        User user = getCreateUser(GameConstants.GAME_NAME, GameConstants.GAME_USER_TYPE);
        enterRoom(user, GameConstants.GameChannelType.Seed1k );
        leaveRoom(user);
        disconnect(user);
    }

    public void createRoomCheck() {
        // 기본 로그인
        User user = getCreateUser(GameConstants.GAME_NAME, GameConstants.GAME_USER_TYPE);

        Yut2GameProto.RoomOption.Builder roomOption = Yut2GameProto.RoomOption.newBuilder();
        roomOption.setChannelType(GameConstants.GameChannelType.Seed1k.ordinal());

        // 방생성
        ResultCreateRoom res = createRoom(user, GameConstants.GAME_ROOM_TYPE_CLASSIC, roomOption);
        assertTrue(res.isSuccess());
        assertNotEquals(0, res.getRoomId());

        // 나가기 예약
        Yut2GameProto.ReserveFlagToS.Builder reserveFlag = Yut2GameProto.ReserveFlagToS.newBuilder();
        reserveFlag.setReserveFlag(Yut2GameProto.ReserveFlag.LeaveRoom);

        // 게임 종료
        ResultLeaveRoom leaveRoomRes = leaveRoom(user, reserveFlag);
        assertTrue(leaveRoomRes.isSuccess());

        // 로그아웃
        ResultLogout logoutResult = logout(user);
        assertTrue(logoutResult.isSuccess());
    }

    /////////
    // Room protocol Handler
    /////////
    void OnEnterRoomToC(Yut2GameProto.EnterRoomToC msg, User user){
        logger.info("<==OnEnterRoomToC[{}]: roomid={}", user.getUserId(), msg.getRoomData().getRoomId());
        Yut2GameProto.CompleteTypeToS.Builder complete = Yut2GameProto.CompleteTypeToS.newBuilder();
        complete.setCompleteType(Yut2GameProto.ClientCompleteType.Play_Game_Ready);
        user.send(complete.build());
        logger.info("-->CompleteTypeToS[{}]: {}", user.getUserId(), Yut2GameProto.ClientCompleteType.Play_Game_Ready);
    }

    void OnGameReadyCompleteNoti(User user){
        logger.info("<==OnGameReadyCompleteNoti[{}]", user.getUserId());
        Yut2GameProto.GameStartToS.Builder packet = Yut2GameProto.GameStartToS.newBuilder();
        user.send(packet.build());
        logger.info("-->GameStartToS[{}]", user.getUserId());
    }


}
