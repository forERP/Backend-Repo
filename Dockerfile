## 빌드 스테이지: Maven으로 Spring Boot JAR 빌드
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 의존성 캐시를 위해 pom.xml 먼저 복사 후 dependency만 미리 다운로드
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# 나머지 소스 복사 후 패키징
COPY src ./src

# 테스트는 CI에서 실행하고, 여기서는 패키징만 수행
RUN mvn -B -q package -DskipTests

## 런타임 스테이지: 빌드된 JAR만 가져와서 실행
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 타임존/로케일 (필요 시 한국 시간 설정)
ENV TZ=Asia/Seoul

# 빌드 스테이지에서 생성한 JAR 복사
COPY --from=builder /app/target/*.jar app.jar

# 외부에서 매핑할 기본 포트 (application.yaml의 server.port=8089와 맞춤)
EXPOSE 8089

# 프로파일, JVM 옵션 등을 외부에서 주입할 수 있도록 ENV 정의 (선택)
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

