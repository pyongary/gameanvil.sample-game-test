package com.nhn.gameflex.sample.test.stress._handler;

import static org.junit.Assert.assertTrue;

import com.nhn.gameflexcore.connector.callback.parent.IDispatchPacket;
import com.nhn.gameflexcore.connector.protocol.Packet;
import com.nhn.gameflexcore.connector.protocol.result.LeaveRoomResult;
import com.nhn.gameflex.sample.test.stress.SampleUserClass;

public class CallbackLeaveRoomRes implements IDispatchPacket<SampleUserClass> {

    @Override
    public void dispatch(Packet packet, SampleUserClass user) {
        LeaveRoomResult leaveRoomResult = user.parseLeaveRoomResult(packet);
        assertTrue(leaveRoomResult.isSuccess());
        if (leaveRoomResult.isSuccess()) {
            user.logout();
        } else {
            user.leaveRoom();
        }
    }

}
