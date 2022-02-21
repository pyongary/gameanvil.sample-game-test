package com.nhn.yut2.server.test.common;

/**
 * 게임에서 사용하는 상수값들 정의
 */
public class GameConstants {
    // 게임서비스 스테이스 이름
    public static final String GAME_NAME = "YUT2";
    public static final String GAME_USER_TYPE = "YUT2";
    public static final String GAME_ROOM_TYPE_CLASSIC = "TYPE_CLASSIC";

    public static final int WAIT_TIME_OUT = 10000;

    public static enum GameChannelType
    {
        Seed1k, Seed5k, Seed10k, Seed3k, Seed30k, Seed50k, Seed100k, Seed20k
    };
}
