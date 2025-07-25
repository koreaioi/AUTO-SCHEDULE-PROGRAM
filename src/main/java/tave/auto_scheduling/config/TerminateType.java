package tave.auto_scheduling.config;

/*
* ONLY_HARD - Hard 조건만 충족하면 끝
* OPTIMIZATION - 10초간 더 나은 해를 탐색, 최대 60초
* HIGH_QUALITY - 20초간 더 나은 해를 탐색, 최대 60초, Hard 제약 보장,
* */

public enum TerminateType {

    ONLY_HARD,
    OPTIMIZATION,
    HIGH_QUALITY

}
