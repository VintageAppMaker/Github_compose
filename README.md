# Github compose
> Jetpack compose를 이용한 Github api 예제
> Jetpack compose의 빠른 활용이 목적인 예제

1. 사용하다 경험한 이슈
   - "Text size가 sp를 강제한다." <= sp의 경우, 사용자가 시스템 폰트를 변경하면 따라서 변경된다.
   - "Fragment, Activity의 lifecycle을 적용하기 애매하다." <= 레퍼런스 찾기기 쉽지않다
   - preview 기능을 구현하려면 "코딩수준"의 관리가 필요하다. <= 편한 것인지 불편한 것인지 판단이 힘들다.
   - 기존 XML에서 구현했던 다양한 기법들이 compose에서는 기본적인 것부터 검색하게 된다 <= 바로 시작할 프로젝트에서는 compose는 금물인 이유이다.
   - @OptIn(ExperimentalMaterialApi::class)를 사용할 때가 많다. 코드에 대한 확신이 떨어진다.  <= 아직은 실험수준의 코드가 많다.

2. 사용하다 경험한 좋은 점
   - 재미있게 생겼다.
   - 레퍼런스가 쌓이면 코드량이 현격히 줄어들 것이 예상된다(특히 악몽의 RecyclerView, ListView는 빠이빠이가 될 것이다)
   - compose가 멀티플랫폼 지원을 목표하고 있다. Flutter와 비슷한 행보를 가고 있다.

4. 현재시점(공식릴리즈 7개월 시점)의 활용법