package com.nhn.yut2.server.test.scenario;

import com.nhn.gameanvil.gamehammer.scenario.ScenarioMachine;
import com.nhn.gameanvil.gamehammer.scenario.ScenarioTest;
import com.nhn.gameanvil.gamehammer.tester.Tester;
import com.nhn.gameanvil.gamehammer.tester.TimeoutStatistics;
import com.nhn.yut2.server.protocol.Yut2GameProto;
import com.nhn.yut2.server.test.scenario.state.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class TestScenario {
    private static final Logger logger = getLogger(TestScenario.class);
    static ScenarioTest<Yut2Actor> scenarioTest;
    static Tester.Builder testerBuilder;

    @BeforeClass
    public static void beforeClass() {
        initConfig();
        initScenario();
    }

    @Test
    public void scenarioTest() {
        // 테스터 생성
        Tester tester = testerBuilder.Build();

        // 테스트 시작
        logger.info("Test Start!!!");
        scenarioTest.start(tester,
            Yut2Actor.class,
            _1_ConnectState.class,
            false
        );

        // 테스트 결과 출력
        logger.info(scenarioTest.printStatistics("Finished"));
        logger.info(TimeoutStatistics.getInstance().printClientTimeout());
    }

    private static void initConfig() {
        // 테스터 기본 프로토콜 설정
        testerBuilder = Tester.newBuilderWithConfig();
        testerBuilder.addProtoBufClass(0, Yut2GameProto.getDescriptor());
    }

    private static void initScenario() {
        // scenario 생성
        ScenarioMachine<Yut2Actor> scenario = getScenarioMachineType();
        scenarioTest = new ScenarioTest<>(scenario);
    }

    private static ScenarioMachine<Yut2Actor> getScenarioMachineType() {
        // 시나이로 머신에 상태 등록
        ScenarioMachine<Yut2Actor> scenario = new ScenarioMachine<>("Yut2");

        scenario.addState(new _1_ConnectState());
        scenario.addState(new _2_RampUpState());
        scenario.addState(new _3_AuthenticationState());
        scenario.addState(new _4_LoginState());
        scenario.addState(new _5_MatchRoomState());
        scenario.addState(new _6_StartGame());
        scenario.addState(new _7_LeaveRoomState());
        scenario.addState(new _8_LogoutState());

        return scenario;
    }


}
