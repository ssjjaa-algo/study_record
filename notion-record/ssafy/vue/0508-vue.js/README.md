# 0508 vue.js

computed는 return 필수, function이 아니라 property이다.

다른 property의 변경사항을 반영할 수 있다.

내 property값이 다른 property값과 긴밀하게 연결되어 있는 것.

watch는 function이고, data에 있는 어떤 값을 바라보고 있어야 한다.

어떤 값이 변경되었을 때 자동 호출 되는 것.

Component

- 전역 컴포넌트
    - Vue.component
        - 태그 집합 : html 집합
        - 권장하는 컴포넌트 이름 : 케밥 표기법(전부 소문자, -)

```html
<body>
    <div id="app1">
      <my-global></my-global>
      <my-global></my-global>
    </div>
    <div id="app2">
      <my-global></my-global>
      <my-global></my-global>
    </div>
    <script>
      Vue.component("MyGlobal", {
        template: "<h2>전역 컴포넌트임</h2>",
      });
      new Vue({
        el: "#app1",
      });
      new Vue({
        el: "#app2",
      });
    </script>
  </body>
```

- 지역 컴포넌트
    - local

```html
<body>
    <div id="app1">
      <my-local></my-local>
      <my-local></my-local>
    </div>
    <div id="app2">
      <my-local></my-local>
      <my-local></my-local>
    </div>
    <script>
      new Vue({
        el: "#app1",
        components: {
          MyLocal: {
            template: "<h2>지역 컴포넌트</h2>",
          },
        },
      });
      new Vue({
        el: "#app2",
      });
    </script>
  </body>
```

- 컴포넌트 자체의 의미를 따지면 공유의 개념과는 거리가 조금 있다?

- vue cli?
