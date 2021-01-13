1. 새로운 기능을 추가해야 하는데 프로그램의 코드가 새로운 기능을 추가하기 쉽도록 구조화 되어 있지 않은 경우에는 먼저 리팩토링을 해서 프로그램에 기능을 추가하기 쉽게 하고, 그 다음에 기능을 추가한다.


2. 리팩토링을 시작하기 전에 견고한 테스트 셋을 가지고 있는지 확인하라. 이 테스트는 자제 검사 여야 한다.


3. 리팩토링은 작은 단계로 나누어 프로그램을 변경한다. 실수를 하게 되더라도 쉽게 버그를 찾을 수있다.


4. 컴퓨터가 이해할 수 있는 코드는 어느 바보나 다 짤 수 있다. 좋은 프로그래머는 사람이 이해할 수 있는 코드를 짠다.


5. 리팩토링(Refactoring) 명사 - 소프트웨어를 보다 쉽게 이해할 수 있고, 적은 비용으로 수정할 수 있도록 겉으로 보이는 동작의 변화 없이 내부 구조를 변경하는것.


6. 리팩토링(Refactoring) 동사 - 일련의 리팩토링을 적용하여 겉으로 보이는 동작의 변화 없이 소 프트웨어의 구조를 바꾸다.


7. 스트라이크 세 개면 리팩토링을 한다. (스트라이크 - 중복성 작업)


8. 주석을 써야 할 것 같은 생각이 들면, 먼저 코드를 리팩토링 하여 주석이 불필요 하도록 하라.


9. 패턴은 우리가 있고 싶은 곳이고, 리팩토링은 그곳에 이르는 방법이다.


출처: https://www.crocus.co.kr/1413 [Crocus]



* mvn clean 등 Maven 오류가 나면 setting.json에 아래와 같이 maven 실행 경로를 추가하라.
  "maven.executable.path": "c:/maven/bin/mvn"
  ref: https://github.com/Microsoft/vscode-maven/blob/master/Troubleshooting.md

* Slf4j log 가 오류 날 때. -> vscode lombok 설치
  ref: https://marketplace.visualstudio.com/items?itemName=GabrielBB.vscode-lombok