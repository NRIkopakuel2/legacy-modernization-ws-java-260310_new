# Hibernate 3.6.x Libraries Required

To enable Hibernate 3.6.x support, please download and add the following JAR files to this `lib` directory:

## Core Hibernate Libraries (Hibernate 3.6.10.Final)
- `hibernate3.jar` (or `hibernate-core-3.6.10.Final.jar`)
- `hibernate-jpa-2.0-api-1.0.1.Final.jar`

## Required Dependencies
- `antlr-2.7.6.jar` - Parser generator
- `dom4j-1.6.1.jar` - XML processing
- `javassist-3.12.0.GA.jar` - Bytecode manipulation
- `jta-1.1.jar` - Java Transaction API
- `slf4j-api-1.6.1.jar` - Logging facade
- `slf4j-simple-1.6.1.jar` - Simple SLF4J implementation (or use slf4j-log4j12)

## MySQL JDBC Driver
- `mysql-connector-java-5.1.49.jar` - MySQL JDBC driver compatible with Java 5

## Hibernate Runtime Dependencies (required but often missed)
- `hibernate-commons-annotations-3.2.0.Final.jar` - Reflection/metadata support (**groupId: `org.hibernate`**, NOT `org.hibernate.common` which is Java 6+)
- `jboss-logging-3.1.0.GA.jar` - Logging bridge used by Hibernate internals

## Struts 1.3.10 and Servlet API (required for compilation)
- `struts-core-1.3.10.jar`
- `struts-taglib-1.3.10.jar`
- `struts-extras-1.3.10.jar`
- `struts-el-1.3.10.jar`
- `commons-beanutils-1.8.0.jar`
- `commons-digester-1.8.jar`
- `commons-logging-1.1.1.jar`
- `commons-validator-1.3.1.jar`
- `commons-chain-1.1.jar`
- `commons-fileupload-1.2.2.jar`
- `oro-2.0.8.jar`
- `log4j-1.2.17.jar`
- `servlet-api-2.5.jar` (compile only — excluded from WAR by build.xml)
- `jsp-api-2.1.jar` (compile only — excluded from WAR by build.xml)

## Optional but Recommended
- `commons-collections-3.2.1.jar` - Collections framework (if not already present)
- `ehcache-core-2.4.3.jar` - Second-level cache provider (optional)

## Download Sources

### Maven Central Repository
You can download these from Maven Central:
- https://repo1.maven.org/maven2/org/hibernate/hibernate-core/3.6.10.Final/
- https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.49/

### Direct Download (for older versions)
- Hibernate: http://sourceforge.net/projects/hibernate/files/hibernate3/
- MySQL Connector: https://dev.mysql.com/downloads/connector/j/5.1.html

## Quick Setup Commands

```bash
# Navigate to the lib directory
cd /home/shinyay/work/java/docker-java5-for-legacy-app/lib

# Example wget commands (adjust versions as needed):
wget https://repo1.maven.org/maven2/org/hibernate/hibernate-core/3.6.10.Final/hibernate-core-3.6.10.Final.jar
wget https://repo1.maven.org/maven2/org/hibernate/javax/persistence/hibernate-jpa-2.0-api/1.0.1.Final/hibernate-jpa-2.0-api-1.0.1.Final.jar
wget https://repo1.maven.org/maven2/antlr/antlr/2.7.6/antlr-2.7.6.jar
wget https://repo1.maven.org/maven2/dom4j/dom4j/1.6.1/dom4j-1.6.1.jar
wget https://repo1.maven.org/maven2/javassist/javassist/3.12.0.GA/javassist-3.12.0.GA.jar
wget https://repo1.maven.org/maven2/javax/transaction/jta/1.1/jta-1.1.jar
wget https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.6.1/slf4j-api-1.6.1.jar
wget https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.6.1/slf4j-simple-1.6.1.jar
wget https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.49/mysql-connector-java-5.1.49.jar
```

## Note
After adding these libraries, rebuild the project using:
```bash
ant clean build
```
