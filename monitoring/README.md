# Spring Boot Monitoring Stack

## 1. 목적

Spring Boot 서버를 운영 관점에서 점검하기 위한 보편형 모니터링 스택입니다.

이 스택은 모니터링 도구만 Docker Compose로 한 번에 실행합니다. 각 Spring Boot 서버에는 Actuator, Prometheus metric, 로그 파일 경로, target 등록 값만 수동으로 추가합니다.

주요 확인 대상:

- 서버 생존 여부와 `/actuator/health` 상태
- HTTP 요청량, 에러율, 응답시간 p50/p95/p99
- JVM heap/non-heap memory
- GC pause, GC count, GC overhead
- thread 수와 상태
- process/system CPU, process memory, disk, file descriptor
- HikariCP connection pool
- application log volume, level별 로그, 최근 로그

## 2. 모니터링 툴 스펙

| Tool | Image | 역할 | 기본 포트 |
| --- | --- | --- | --- |
| Grafana | `grafana/grafana:11.2.0` | dashboard, datasource provisioning | `3000` |
| Prometheus | `prom/prometheus:v2.53.3` | metric scrape, alert rule evaluation | `9090` |
| Loki | `grafana/loki:3.2.1` | application log 저장/검색 | `3100` |
| Grafana Alloy | `grafana/alloy:v1.16.1` | log file tailing, Loki forwarding | `12345` |
| Alertmanager | `prom/alertmanager:v0.27.0` | Prometheus alert routing | `9093` |
| Blackbox Exporter | `prom/blackbox-exporter:v0.25.0` | `/actuator/health` HTTP probe | `9115` |

Grafana dashboard:

- UID: `spring-observability-overview`
- Title: `Spring Boot Server Monitoring(스프링 부트 서버 모니터링)`
- Panel count: `48`

## 3. 사용법

```powershell
cd D:\github-repo\study_record\monitoring
docker compose up -d
```

접속 URL:

- Grafana: http://localhost:3000
- Prometheus: http://localhost:9090
- Prometheus targets: http://localhost:9090/targets
- Alertmanager: http://localhost:9093
- Loki: http://localhost:3100
- Alloy: http://localhost:12345

Grafana 기본 계정:

- ID: `admin`
- Password: `admin`

종료:

```powershell
docker compose down
```

volume까지 삭제:

```powershell
docker compose down -v
```

필요하면 같은 폴더에 `.env` 파일을 직접 만들어 기본값을 바꿀 수 있습니다.

```properties
GRAFANA_PORT=3000
PROMETHEUS_PORT=9090
ALERTMANAGER_PORT=9093
BLACKBOX_PORT=9115
LOKI_PORT=3100
ALLOY_PORT=12345
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=admin
PROMETHEUS_RETENTION=15d
OBSERVED_LOG_DIR=./observed-logs
```

## 4. 서버 가동 시 설정파일에 추가해야 할 것

### Gradle 의존성

```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
runtimeOnly 'io.micrometer:micrometer-registry-prometheus'
```

### Spring Boot `application.yml`

```yaml
spring:
  application:
    name: sample-api

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics,threaddump,loggers
  endpoint:
    health:
      probes:
        enabled: true
      show-details: always
  prometheus:
    metrics:
      export:
        enabled: true
  metrics:
    tags:
      application: ${spring.application.name}
      service_name: ${spring.application.name}
    distribution:
      percentiles-histogram:
        http:
          server:
            requests: true

logging:
  file:
    name: D:/github-repo/study_record/monitoring/observed-logs/${spring.application.name}/application.log
```

서버를 Docker 밖의 로컬 PC에서 실행하고, 모니터링 스택만 Docker Desktop에서 실행한다면 Prometheus target은 `host.docker.internal:<server-port>`를 사용합니다.

### Prometheus metric target

`config/prometheus/targets/spring-apps.yml`

```yaml
- targets:
    - host.docker.internal:8080
  labels:
    application: sample-api
    environment: local
    scheme: http
    metrics_path: /actuator/prometheus
```

### Health check target

`config/prometheus/targets/spring-health.yml`

```yaml
- targets:
    - http://host.docker.internal:8080/actuator/health
  labels:
    application: sample-api
    environment: local
```

### Loki log target

`config/alloy/config.alloy`

```alloy
{
  "__path__" = "/observed-logs/sample-api/*.log",
  "application" = "sample-api",
  "environment" = "local",
  "job" = "spring-boot",
},
```

`application` 값은 Spring Boot `spring.application.name`과 맞추는 것을 권장합니다.

로그 파일 위치를 다른 곳으로 바꿀 경우, `.env`의 `OBSERVED_LOG_DIR`은 실제 로그 루트 디렉터리로 맞추고 Alloy의 `__path__`는 컨테이너 내부 경로인 `/observed-logs/<application>/*.log` 형태로 맞춥니다.
