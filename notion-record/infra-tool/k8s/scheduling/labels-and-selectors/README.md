# Labels and Selectors

https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/

- **Labels**는 파드와 같은 객체에 부착되는 **키/값**
- 사용자가 이해하기 쉽고 의미 있는 속성을 지정하기 위해 사용
- 라벨은 코어 시스템에는 직접적인 의미 x, 객체를 조직화하고 특정 객체의 하위 집합을 선택하는데 유용
- 객체 생성 시 부착할 수 있으며, 이후 언제든지 추가 및 수정
- 각 객체는 고유한 키/값 라벨 세트를 가질 수 있으며, 각 키는 해당 객체에 대해 유일해야 함

```yaml
"metadata": {
  "labels": {
    "key1" : "value1",
    "key2" : "value2"
  }
}
```
