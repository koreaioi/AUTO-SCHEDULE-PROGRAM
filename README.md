# AUTO-SCHEDULE-PROGRAM

- 면접시간 자동 매칭(스케줄링) Tave 자동화 도구
- Use `Optaplanner` based on CSP Algorithm

## 제공 가치

- `Convenience` : 면접 배정 방식을 `수동` -> `자동`, `편리함` 제공 
- `Concentration` : `귀찮지만 해야할 일`을 빠르게 해결하고 Tave 성장에 `집중`할 수 있도록 함.
- `Courage` : Tave는 만들 수 있다. `용기` 독려

<hr>

## 서비스 목적

### :trollface: Pain Point

지원자가 선택한 `면접 가능 시간` 중에서 운영진이 면접을 수동으로 배정하는 방식.   
이는 다음과 같은 불편함과 비효율을 야기함.

- :one: `많은 시간 소모` : 예를 들어, 120명의 지원자 면접시간을 배정한다고 할 때, 한명 당 1분으로 잡아도 2시간이 걸린다. 물론 여러 선호도 조건을 생각하면 2시간은 훌쩍 넘는다.
- :two: `높은 오류 발생 가능성` : 많은 경우의 수, 제약 사항, 선호 조건을 동시에 만족하는 경우를 사람의 능력으로 배정하려는 경우, 수많은 오류 가능성이 존재. 이를 방지하기 위한 교차 검증 또한 결국 인력 소모
- :three: `낮은 최적해 도달 가능성` : 수동 배정으로는 그저 '실행 가능한' 면접 스케줄을 만들 뿐, 가장 좋은 최적의 면접 스케줄을 만드는 건 매우 어렵다.

### :smile: Solution

기존의 문제점을 해결하기 위해 다음과 같은 솔루션을 제공!

- :one: `Optaplanner` : Constraint Satisfaction Problems에 적합한 Java 기반 제약 조건 최적화 스케줄링 도구.
- :two: `다양한 결과` : `어떤 조건`에 `가중치`를 줄 것인지에 대한 정의된 API를 제공하여, `여러 결과`를 제공.
- :three: `시간 효율` : 기본 종료 조건은 10초간 더 나은 결과를 찾고, 최대 60초간 최적해를 찾음. 따라서 면접 배정 프로세스 과정 `10~60초` 예상

<hr>

## :smiley_cat: 기술 스택

Spring Boot, OptaPlanner, Apache Poi


<hr>

## :construction: 구조 도식화

### 구조

<img width="700" height="805" alt="Enhanced OptaPlanner Flow" src="https://github.com/user-attachments/assets/f7420567-436c-4be5-9fe7-060d8f128098" />

### Optaplanner 관리대상

#### InterviewSlot
- 면접이 가능한 하나의 `날짜+시간` 단위를 나타냄.
- 추후 InterviewSchedule의 필드로서 @ValueRangeProvider를 사용하여, 각 지원자가 전체 InterviewSlot중 하나를 배정받음.

#### Applicant
- 실제 엑셀에서 가져오는 `지원자 기본 정보`

#### ApplicantAssignment 
- OptaPlanner의 핵심 최적화 대상
- 하나의 지원자를 어떤 시간에 배치할지 결정하는 단위
- OptaPlanner가 최적의 InterviewSlot을 계산하여 할당함

#### InterviewSchedule 
- 전체 스케줄링 결과를 담는 클래스

### Optaplanner 구성
#### InterviewConstraintProvider
- Hard, Soft 제약 조건을 제공

#### OptaPlannerSoverConfig, TerminationPolicyConfig
- SoverManager 주입 역할
- TerminationType에 따른 적절한 종료 조건 주입

#### SolverManager
- solve(), solveAndListen()을 사용하여, 문제를 실제로 푸는 역할

<hr>

### 시연 영상

100명의 면접가능시간을 무작위로 담은 테스트 Excel을 
3가지 옵션 중 가장 빠른 결과를 내는 ONLY_HARD를 적용.

(GIF가 10MB 이하여야 하기에..... 가장 빨리 끝나는 ONLY_HARD 선택)

![자동배치영상압축](https://github.com/user-attachments/assets/4e85c360-1c9b-4880-aec0-fa897c547c1b)
