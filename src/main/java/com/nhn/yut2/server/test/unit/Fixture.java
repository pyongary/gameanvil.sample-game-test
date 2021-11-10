package com.nhn.yut2.server.test.unit;

import com.google.protobuf.GeneratedMessageV3;
import com.nhn.gameanvil.gamehammer.tester.*;
import com.nhn.gameanvil.gamehammer.tool.UuidGenerator;

import com.nhn.yut2.server.protocol.Yut2GameProto;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.nhn.yut2.server.test.common.GameConstants.WAIT_TIME_OUT;
import static org.junit.Assert.*;
import static org.slf4j.LoggerFactory.getLogger;

public class Fixture {
    private static final Logger logger = getLogger(Fixture.class);
    private static Tester tester = null;
    protected static UuidGenerator connectionDeviceIdGenerator = new UuidGenerator("DeviceId");
    protected static AtomicInteger uuidCounter = new AtomicInteger(0);

    // 로그인까지 완료된 전달받은 명수 만큼 생성
    protected List<User> getCreateUsers(String serviceName, String userType, int count) {
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            userList.add(getCreateUser(serviceName, userType));
        }
        return userList;
    }

    // 로그인까지 완료된 유저 생성
    protected User getCreateUser(String serviceName, String userType) {
        // 커넥션 생성
        Connection connection = createConnection(uuidCounter.incrementAndGet());

        // 연결
        ResultConnect resultConnect = connect(connection);
        assertEquals(ResultConnect.ResultCodeConnect.CONNECT_SUCCESS, resultConnect.getResultCode());

        // 인증 검증을 위한 토큰 정보 생성
        Yut2GameProto.AuthenticationToS.Builder authenticationReq = Yut2GameProto.AuthenticationToS.newBuilder().setAccessToken("test-bot");

        // 인증 진행
        ResultAuthentication res = authentication(connection, "Bot" + connection.getUuid(), String.valueOf(connection.getUuid()), connectionDeviceIdGenerator.generateUniqueId(), authenticationReq);
        assertTrue(res.isSuccess());

        // 유저 생성
        User user = connection.createUser(serviceName, 1);
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

        logger.info("User AccountId:{}, UserId:{}, SubId:{}, ChannelId:{}", user.getConnection().getAccountId(), user.getUserId(), user.getSubId(), user.getChannelId());
        // 로그인 진행
        ResultLogin loginRes = login(user, userType, user.getChannelId(), loginReq);
        assertTrue(loginRes.isSuccess());

        assertNotEquals(user.getUserId(), 0);   // 유저 아이디가 유효한지 확인
        return user;
    }

    // 테스터 생성
    protected static void initConnector(String ipAddress, int port) {
        if (null != tester) {
            tester.close();
        }

        tester = Tester.newBuilder()
                .addRemoteInfo(new RemoteInfo(ipAddress, port))
                .setDefaultPacketTimeoutSeconds(3)
                .addProtoBufClass(0, Yut2GameProto.getDescriptor())
                .Build();
    }

    // 테스터 해제
    protected static void resetConnect() {
        if (tester != null) {
            tester.close();
            tester = null;
        }
    }

    // 모든 커넥션 해제
    protected static void closeAllConnections() {
        tester.closeAllConnections();
    }

    // 커넥션 생성
    protected Connection createConnection(int uuid) {
        return tester.createConnection(uuid);
    }

    // 커넥션 연결
    protected ResultConnect connect(Connection connection) {
        Future<ResultConnect> future = connection.connect(connection.getConfig().getNextRemoteInfo());
        try {
            return future.get(WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("InterruptedException", e);
        } catch (ExecutionException e) {
            logger.error("ExecutionException", e);
        } catch (TimeoutException e) {
            logger.error("TimeoutException", e);
        }
        return null;
    }

    // 인증 요청
    protected ResultAuthentication authentication(Connection connection, String accountId, String password, String deviceId, GeneratedMessageV3.Builder<?>... sendPayloads) {
        Future<ResultAuthentication> completableFuture = connection.authentication(accountId, password, deviceId, sendPayloads);
        try {
            return completableFuture.get(WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Fixture::authentication()", e);
        }
        return null;
    }

    // 로그인 요청
    protected ResultLogin login(User user, String userType, String channelId, GeneratedMessageV3.Builder<?>... payloads) {
        Future<ResultLogin> completableFuture = user.login(userType, channelId, payloads);
        try {
            return completableFuture.get(WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Fixture::login()", e);
        }
        return null;
    }

    // 로그아웃 요청
    protected ResultLogout logout(User user, GeneratedMessageV3.Builder<?>... payloads) {
        Future<ResultLogout> completableFuture = user.logout(payloads);

        try {
            return completableFuture.get(WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Fixture::logout()", e);
        }

        return null;
    }

    protected void userListLeaveRoom(List<User> userList, GeneratedMessageV3.Builder<?>... payload) {
        for (User user : userList) {
            // 나가기 예약
            ResultLeaveRoom leaveRoomRes = leaveRoom(user, payload);
            assertTrue(leaveRoomRes.isSuccess());
        }
    }

    // 로그아웃처리
    protected void userListLogout(List<User> userList, GeneratedMessageV3.Builder<?>... payload) {
        for (User user : userList) {
            // 로그아웃
            ResultLogout logoutResult = logout(user);
            assertTrue(logoutResult.isSuccess());
        }
    }

    // 룸생성 요청
    protected ResultCreateRoom createRoom(User user, String roomType, GeneratedMessageV3.Builder<?>... sendPayloads) {
        Future<ResultCreateRoom> completableFuture = user.createRoom(roomType, sendPayloads);

        try {
            return completableFuture.get(WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Fixture::createRoom()", e);
        }

        return null;
    }

    // 룸 나가기 요청
    protected ResultLeaveRoom leaveRoom(User user, GeneratedMessageV3.Builder<?>... sendPayloads) {
        Future<ResultLeaveRoom> completableFuture = user.leaveRoom(sendPayloads);

        try {
            return completableFuture.get(WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Fixture::leaveRoom()", e);
        }
        return null;
    }

    // 매치룸 요청
    protected ResultMatchRoom matchRoom(User user, String roomType, boolean isCreate, boolean isMoveRoom, GeneratedMessageV3.Builder<?>... sendPayloads) {
        Future<ResultMatchRoom> completableFuture = user.matchRoom(roomType, isCreate, isMoveRoom, sendPayloads);

        try {
            return completableFuture.get(WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Fixture::matchRoom()", e);
        }

        return null;
    }

    // 유저 매치 요청
    protected ResultMatchUserStart matchUserStart(User user, String roomType, String matchingGroup, GeneratedMessageV3.Builder<?>... sendPayloads) {
        Future<ResultMatchUserStart> completableFuture = user.matchUserStart(roomType, sendPayloads);

        try {
            return completableFuture.get(WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Fixture::matchUserStart()", e);
        }

        return null;
    }

}
