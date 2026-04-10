ARG dockerProxy
FROM ${dockerProxy}maven:3.9.1-eclipse-temurin-17 AS build
ARG mavenUrl
ARG mavenUser
ARG mavenPw
RUN echo '<settings> \
    <servers> \
        <server> \
            <id>central</id> \
            <username>${mavenUser}</username> \
            <password>${mavenPw}</password> \
        </server> \
    </servers> \
    <mirrors> \
        <mirror> \
            <id>central</id> \
            <url>${mavenUrl}</url> \
            <mirrorOf>central</mirrorOf> \
        </mirror> \
    </mirrors> \
</settings>' > /usr/share/maven/conf/settings.xml
RUN mkdir /project
WORKDIR /project
RUN git clone https://github.com/RyanYeung2019/weaver-view.git
WORKDIR /project/weaver-view
RUN --mount=type=cache,target=/root/.m2 mvn clean install -DskipTests
COPY src /project/weaver-view-demo/src
COPY pom.xml /project/weaver-view-demo/pom.xml
WORKDIR /project/weaver-view-demo
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

FROM ${dockerProxy}openjdk:17-ea-oracle
RUN mkdir /app
COPY --from=build /project/weaver-view-demo/target/weaver-view-demo.jar /app/weaver-view-demo.jar
WORKDIR /app
