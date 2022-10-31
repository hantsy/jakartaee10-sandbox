# Jakarta Persistence 3.1

Jakarta Persistence(aka JPA) 3.1 brings a collection of improvements.

* The `UUID` class now is treated as Basic Java Type. To support UUID type `ID` in `Entity` class, JPA introduces a new UUID generator.
* Several numeric functions and some date/time specific functions are added in JPQL and type-safe Criteria API.

More details please read [What's New in Jakarta Persistence 3.1](https://newsroom.eclipse.org/eclipse-newsletter/2022/march/what%E2%80%99s-new-jakarta-persistence-31).

Next let's explore these features by writing some real example codes.

## Hibernate 6.1

Generate a simple **Java application** project via [Maven Quickstart archetype](https://maven.apache.org/archetypes/maven-archetype-quickstart/).

```bash
mvn archetype:generate
    -DarchetypeGroupId=org.apache.maven.archetypes
    -DarchetypeArtifactId=maven-archetype-quickstart
    -DarchetypeVersion=1.4
```

There are some interactive steps to guide you setup the project info, such as groupId, artifact, version etc. In this example project, we use `com.example` as groupId, and `demo` as artifactId. Then confirm and begin to generate the project source codes.

After it is done, open the project in a Java IDE such as IntelliJ IDEA(Community Edition is free), or Eclipse Java/Java EE bundle, or NetBeans IDE, or a simple text editor, eg. VS Code.

Modify the *pom.xml* in the project root folder, add Hibernate 6.1, and JUnit etc. into project dependencies, and setup Maven compiler plugin to use Java 17 to compile the source codes.

The final *pom.xml* looks like the following.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>hibernate6</artifactId>
    <version>1.0-SNAPSHOT</version>
    <parent>
        <groupId>com.example</groupId>
        <artifactId>jakartaee10-sandbox-parent</artifactId>
        <version>1.0-SNAPSHOT</version>
        <relativePath>..</relativePath>
    </parent>

    <name>hibernate6</name>
    <description>Jakarta EE 10 Sandbox: Hibernate 6/JPA 3.1 example</description>

    <properties>
        <maven.compiler.release>17</maven.compiler.release>

        <!-- requires 6.1.2.Final or higher -->
        <hibernate.version>6.1.4.Final</hibernate.version>
        <h2.version>2.1.214</h2.version>

        <!-- test deps -->
        <junit-jupiter.version>5.9.1</junit-jupiter.version>
        <assertj-core.version>3.23.1</assertj-core.version>

        <slf4j.version>2.0.3</slf4j.version>
        <logback.version>1.4.4</logback.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>${hibernate.version}</version>
        </dependency>
        <dependency>
            <groupId>jakarta.persistence</groupId>
            <artifactId>jakarta.persistence-api</artifactId>
            <version>3.1.0</version>
        </dependency>

        <!-- H2 Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>${h2.version}</version>
        </dependency>

        <!-- logging with logback -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>${slf4j.version}</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>jcl-over-slf4j</artifactId>
            <version>${slf4j.version}</version>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-core</artifactId>
            <version>${logback.version}</version>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>${logback.version}</version>
        </dependency>

        <!-- test dependencies -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit-jupiter.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>${assertj-core.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-testing</artifactId>
            <version>${hibernate.version}</version>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>junit</groupId>
                    <artifactId>junit</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.10.1</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0-M7</version>
            </plugin>
        </plugins>
    </build>
</project>
```

In this example project, we use H2 embedded database for test purpose. Hibernate 6.1 implements the features of Jakarta Persistence 3.1, but it includes a Jakarta Persistence 3.0 API in the transitive dependency tree.

To use Jakarta Persistence 3.1 API, we have to add `jakarta.persistence:jakarta.persistence-api` 3.1 explicitly.

In the *src/main/resources/META-INF*, add a new file named *persistence.xml*.

```xml
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence https://jakarta.ee/xml/ns/persistence/persistence_3_1.xsd"
             version="3.1">

    <persistence-unit name="defaultPU" transaction-type="RESOURCE_LOCAL">

        <description>Hibernate test case template Persistence Unit</description>
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

        <exclude-unlisted-classes>false</exclude-unlisted-classes>

        <properties>
            <property name="hibernate.archive.autodetection" value="class, hbm"/>

            <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
            <property name="hibernate.connection.driver_class" value="org.h2.Driver"/>
            <property name="hibernate.connection.url" value="jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1"/>
            <property name="hibernate.connection.username" value="sa"/>

            <property name="hibernate.connection.pool_size" value="5"/>

            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
            <property name="hibernate.hbm2ddl.auto" value="create-drop"/>

            <property name="hibernate.max_fetch_depth" value="5"/>

            <property name="hibernate.cache.region_prefix" value="hibernate.test"/>
            <property name="hibernate.cache.region.factory_class"
                      value="org.hibernate.testing.cache.CachingRegionFactory"/>

            <!--NOTE: hibernate.jdbc.batch_versioned_data should be set to false when testing with Oracle-->
            <property name="hibernate.jdbc.batch_versioned_data" value="true"/>

            <property name="jakarta.persistence.validation.mode" value="NONE"/>
            <property name="hibernate.service.allow_crawling" value="false"/>
            <property name="hibernate.session.events.log" value="true"/>
        </properties>

    </persistence-unit>
</persistence>
```

We use logback as the logging framework in this project. In the *src/main/resources*, add a *logback.xml* to configure logback.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <property name="LOGS" value="./logs"/>

    <appender name="Console"
              class="ch.qos.logback.core.ConsoleAppender">
        <layout class="ch.qos.logback.classic.PatternLayout">
            <Pattern>
                %green(%d{ISO8601}) %highlight(%-5level) [%blue(%t)] %yellow(%C{1.}): %msg%n%throwable
            </Pattern>
        </layout>
    </appender>

    <appender name="RollingFile"
              class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOGS}/app.log</file>
        <encoder
                class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <Pattern>%d %p %C{1.} [%t] %m%n</Pattern>
        </encoder>

        <rollingPolicy
                class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- rollover daily and when the file reaches 10 MegaBytes -->
            <fileNamePattern>${LOGS}/archived/app-%d{yyyy-MM-dd}.%i.log
            </fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy
                    class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>10MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
    </appender>

    <!-- LOG everything at INFO level -->
    <root level="info">
        <appender-ref ref="RollingFile"/>
        <appender-ref ref="Console"/>
    </root>

    <!-- Debug hibernate SQL,  see: https://thorben-janssen.com/hibernate-logging-guide/ -->
    <logger name="org.hibernate.SQL" level="DEBUG"/>
    <logger name="org.hibernate.type.descriptor.sql" level="trace"/>

    <!-- Custom debug level for the application code -->
    <logger name="com.example" level="debug" additivity="false">
        <appender-ref ref="RollingFile"/>
        <appender-ref ref="Console"/>
    </logger>
</configuration>
```

We set `org.hibernate.SQL` logging level to `DEBUG` and `org.hibernate.type.descriptor.sql` to `trace`, it will help you to dig into the Hibernate generated sql at runtime.

