# 0420 Springmvc Board

1. pom.xml에 java, spring, jdbc 버전 설정
2. web.xml에서

1. DispatcherServlet에 대한 정보 : servlet-context.xml

1. img, css, js…는 servlet이 처리하지 말도록 처리

```
<properties>
		<java-version>1.8</java-version> <!-- java의 버전 1.8 설정 -->
		<m2eclipse.wtp.contextRoot>board</m2eclipse.wtp.contextRoot> <!-- context-root가 board -->
		<!-- maven compiler plugin version :: https://maven.apache.org/plugins/maven-compiler-plugin/examples/set-compiler-source-and-target.html -->
		<maven.compiler.source>${java-version}</maven.compiler.source>
		<maven.compiler.target>${java-version}</maven.compiler.target>
		
		<org.springframework-version>5.3.26</org.springframework-version>
		<org.aspectj-version>1.9.9.1</org.aspectj-version>
		<log4j-version>1.2.17</log4j-version>
		<org.slf4j-version>1.7.36</org.slf4j-version>
		
		<mysql-connector-java-version>8.0.31</mysql-connector-java-version>
		<mybatis-version>3.5.9</mybatis-version>
		<mybatis-spring-version>2.0.7</mybatis-spring-version>
		
		<servlet-version>4.0.1</servlet-version>
		<jsp-version>2.3.3</jsp-version>
		<jstl-version>1.2</jstl-version>
		<commons-fileupload-version>1.5</commons-fileupload-version>
		<commons-dbcp2-version>2.9.0</commons-dbcp2-version>
		<jackson-databind-version>2.13.5</jackson-databind-version>
		<json-version>20230227</json-version>
		<lombok-version>1.18.26</lombok-version>
		
		<junit-version>4.13.2</junit-version>
	</properties>
```

```xml
<!-- POST 방식의 한글 처리. web.xml-->
	 <filter>
        <filter-name>encodingFilter</filter-name>
        <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
        <init-param>
          <param-name>encoding</param-name>
          <param-value>UTF-8</param-value>
        </init-param>
     </filter>
```

```xml
<!--
	AOP설정해야 하고, Component-scan 설정해야한다. 
	Source 옆 namespace에서 aop하고 context체크한다.
	root-context xml
	 -->	
	 
	 <!-- com.ssafy.*.model은 one depth, com.ssafy.**.model은 하위까지 다되는 것 -->
	 <context:component-scan base-package="com.ssafy.*.model,com.ssafy.board.aop,com.ssafy.util"></context:component-scan>
	 
	 <aop:aspectj-autoproxy></aop:aspectj-autoproxy>
```

```java
@ExceptionHandler(Exception.class)
// 컨트롤러에서만 가능하고 서비스나 DAO에서는 할 수 없다.
// 컨트롤러가 어디로 가라고 지정하는 역할이기 때문에 컨트롤러에서만 할 수 있는 것

```
