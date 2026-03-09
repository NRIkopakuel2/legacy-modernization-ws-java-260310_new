#!/bin/bash

# Development Environment Setup Script
# 開発環境のセットアップと便利なツールの提供

set -e

echo "=== Java 5 Development Environment Setup ==="

# カラー出力の設定
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "\n${BLUE}=== $1 ===${NC}"
}

# 環境情報の表示
show_environment() {
    print_header "Environment Information"
    echo "JAVA_HOME: $JAVA_HOME"
    echo "ANT_HOME: $ANT_HOME"
    echo "PATH: $PATH"
    echo "Current User: $(whoami)"
    echo "Current Directory: $(pwd)"
    echo "Container Hostname: $(hostname)"
    echo

    print_status "Java Version:"
    java -version 2>&1 | head -3
    echo

    print_status "Ant Version:"
    ant -version
    echo

    print_status "Available Memory:"
    free -h
    echo

    print_status "Disk Usage:"
    df -h /app
    echo
}

# プロジェクト構造の初期化
init_project() {
    print_header "Initializing Project Structure"

    # 基本ディレクトリ構造
    mkdir -p src/main/java/com/example/{action,form,dao,service,util}
    mkdir -p src/main/resources
    mkdir -p src/main/webapp/{WEB-INF,css,js,images}
    mkdir -p src/main/webapp/WEB-INF/{classes,lib}
    mkdir -p src/test/java/com/example
    mkdir -p lib
    mkdir -p build/{classes,test-classes,war}
    mkdir -p dist
    mkdir -p docs/{javadoc,design}
    mkdir -p config

    print_status "Directory structure created"
    tree . -d -L 3 2>/dev/null || find . -type d | head -20
}

# Struts設定ファイルのサンプル作成
create_struts_config() {
    print_header "Creating Struts Configuration Files"

    # web.xml の作成
    if [ ! -f "src/main/webapp/WEB-INF/web.xml" ]; then
        cat > src/main/webapp/WEB-INF/web.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE web-app PUBLIC
    "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"
    "http://java.sun.com/dtd/web-app_2_3.dtd">

<web-app>
    <display-name>Legacy Struts Application</display-name>

    <!-- Struts Action Servlet -->
    <servlet>
        <servlet-name>action</servlet-name>
        <servlet-class>org.apache.struts.action.ActionServlet</servlet-class>
        <init-param>
            <param-name>config</param-name>
            <param-value>/WEB-INF/struts-config.xml</param-value>
        </init-param>
        <init-param>
            <param-name>debug</param-name>
            <param-value>2</param-value>
        </init-param>
        <init-param>
            <param-name>detail</param-name>
            <param-value>2</param-value>
        </init-param>
        <load-on-startup>2</load-on-startup>
    </servlet>

    <!-- Struts Action Servlet Mapping -->
    <servlet-mapping>
        <servlet-name>action</servlet-name>
        <url-pattern>*.do</url-pattern>
    </servlet-mapping>

    <!-- Welcome File List -->
    <welcome-file-list>
        <welcome-file>index.jsp</welcome-file>
    </welcome-file-list>

    <!-- Session Timeout (30 minutes) -->
    <session-config>
        <session-timeout>30</session-timeout>
    </session-config>

</web-app>
EOF
        print_status "Created web.xml"
    fi

    # struts-config.xml の作成
    if [ ! -f "src/main/webapp/WEB-INF/struts-config.xml" ]; then
        cat > src/main/webapp/WEB-INF/struts-config.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE struts-config PUBLIC
    "-//Apache Software Foundation//DTD Struts Configuration 1.3//EN"
    "http://struts.apache.org/dtds/struts-config_1_3.dtd">

<struts-config>

    <!-- Form Beans -->
    <form-beans>
        <form-bean name="sampleForm" type="com.example.form.SampleForm"/>
    </form-beans>

    <!-- Action Mappings -->
    <action-mappings>
        <action path="/sample"
                type="com.example.action.SampleAction"
                name="sampleForm"
                scope="request"
                validate="true"
                input="/sample.jsp">
            <forward name="success" path="/success.jsp"/>
            <forward name="failure" path="/error.jsp"/>
        </action>
    </action-mappings>

    <!-- Message Resources -->
    <message-resources parameter="ApplicationResources"/>

</struts-config>
EOF
        print_status "Created struts-config.xml"
    fi
}

# Antビルドファイルの作成
create_build_xml() {
    print_header "Creating Ant Build File"

    if [ ! -f "build.xml" ]; then
        cat > build.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project name="legacy-struts-app" default="build" basedir=".">

    <!-- Properties -->
    <property name="src.dir" value="src/main/java"/>
    <property name="test.src.dir" value="src/test/java"/>
    <property name="webapp.dir" value="src/main/webapp"/>
    <property name="lib.dir" value="lib"/>
    <property name="build.dir" value="build"/>
    <property name="classes.dir" value="${build.dir}/classes"/>
    <property name="test.classes.dir" value="${build.dir}/test-classes"/>
    <property name="war.dir" value="${build.dir}/war"/>
    <property name="dist.dir" value="dist"/>
    <property name="docs.dir" value="docs"/>
    <property name="javadoc.dir" value="${docs.dir}/javadoc"/>

    <property name="app.name" value="legacy-app"/>
    <property name="war.file" value="${app.name}.war"/>

    <!-- Classpath -->
    <path id="compile.classpath">
        <fileset dir="${lib.dir}">
            <include name="**/*.jar"/>
        </fileset>
        <fileset dir="${webapp.dir}/WEB-INF/lib" erroronmissingdir="false">
            <include name="**/*.jar"/>
        </fileset>
    </path>

    <path id="test.classpath">
        <path refid="compile.classpath"/>
        <pathelement location="${classes.dir}"/>
    </path>

    <!-- Targets -->
    <target name="init">
        <mkdir dir="${build.dir}"/>
        <mkdir dir="${classes.dir}"/>
        <mkdir dir="${test.classes.dir}"/>
        <mkdir dir="${war.dir}"/>
        <mkdir dir="${dist.dir}"/>
        <mkdir dir="${docs.dir}"/>
    </target>

    <target name="clean">
        <delete dir="${build.dir}"/>
        <delete dir="${dist.dir}"/>
        <delete dir="${docs.dir}"/>
    </target>

    <target name="compile" depends="init">
        <javac srcdir="${src.dir}"
               destdir="${classes.dir}"
               classpathref="compile.classpath"
               encoding="UTF-8"
               source="1.5"
               target="1.5"
               debug="true"
               deprecation="true"/>

        <!-- Copy resources -->
        <copy todir="${classes.dir}">
            <fileset dir="src/main/resources" erroronmissingdir="false">
                <include name="**/*"/>
            </fileset>
        </copy>
    </target>

    <target name="compile-tests" depends="compile">
        <javac srcdir="${test.src.dir}"
               destdir="${test.classes.dir}"
               classpathref="test.classpath"
               encoding="UTF-8"
               source="1.5"
               target="1.5"
               debug="true"
               deprecation="true"/>
    </target>

    <target name="war" depends="compile">
        <!-- Copy webapp files -->
        <copy todir="${war.dir}">
            <fileset dir="${webapp.dir}"/>
        </copy>

        <!-- Copy classes -->
        <copy todir="${war.dir}/WEB-INF/classes">
            <fileset dir="${classes.dir}"/>
        </copy>

        <!-- Create WAR file -->
        <jar destfile="${dist.dir}/${war.file}" basedir="${war.dir}"/>
    </target>

    <target name="javadoc" depends="init">
        <javadoc sourcepath="${src.dir}"
                 destdir="${javadoc.dir}"
                 classpathref="compile.classpath"
                 encoding="UTF-8"
                 charset="UTF-8"
                 windowtitle="${app.name} API"
                 doctitle="${app.name} API Documentation"/>
    </target>

    <target name="build" depends="clean, compile, compile-tests, war, javadoc">
        <echo message="Build completed successfully!"/>
    </target>

</project>
EOF
        print_status "Created build.xml"
    fi
}

# 開発用ユーティリティスクリプトの作成
create_dev_scripts() {
    print_header "Creating Development Scripts"

    # デバッグスクリプト
    cat > debug.sh << 'EOF'
#!/bin/bash
echo "Starting Java application in debug mode..."
java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9000 \
     -cp "build/classes:lib/*" \
     "$@"
EOF
    chmod +x debug.sh
    print_status "Created debug.sh"

    # ログ監視スクリプト
    cat > logs.sh << 'EOF'
#!/bin/bash
LOG_DIR="/usr/local/tomcat/logs"
if [ -d "$LOG_DIR" ]; then
    echo "Monitoring Tomcat logs..."
    tail -f $LOG_DIR/catalina.out
else
    echo "Tomcat logs directory not found: $LOG_DIR"
    echo "Available log files:"
    find . -name "*.log" -type f 2>/dev/null || echo "No log files found"
fi
EOF
    chmod +x logs.sh
    print_status "Created logs.sh"
}

# サンプルコードの作成
create_sample_code() {
    print_header "Creating Sample Code"

    # Sample Action
    mkdir -p src/main/java/com/example/action
    cat > src/main/java/com/example/action/SampleAction.java << 'EOF'
package com.example.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.example.form.SampleForm;

/**
 * Sample Struts Action
 */
public class SampleAction extends Action {

    public ActionForward execute(ActionMapping mapping,
                               ActionForm form,
                               HttpServletRequest request,
                               HttpServletResponse response) throws Exception {

        SampleForm sampleForm = (SampleForm) form;

        // Business logic here
        String message = "Hello, " + sampleForm.getName() + "!";
        request.setAttribute("message", message);

        return mapping.findForward("success");
    }
}
EOF

    # Sample Form
    mkdir -p src/main/java/com/example/form
    cat > src/main/java/com/example/form/SampleForm.java << 'EOF'
package com.example.form;

import org.apache.struts.action.ActionForm;

/**
 * Sample Struts Form Bean
 */
public class SampleForm extends ActionForm {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void reset() {
        this.name = null;
    }
}
EOF

    # Sample JSP
    cat > src/main/webapp/sample.jsp << 'EOF'
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<html>
<head>
    <title>Sample Page</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body>
    <h1>Sample Struts Application</h1>

    <html:form action="/sample">
        <table>
            <tr>
                <td>Name:</td>
                <td><html:text property="name" size="30"/></td>
            </tr>
            <tr>
                <td colspan="2">
                    <html:submit value="Submit"/>
                </td>
            </tr>
        </table>
    </html:form>

</body>
</html>
EOF

    # Success JSP
    cat > src/main/webapp/success.jsp << 'EOF'
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<html>
<head>
    <title>Success</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body>
    <h1>Success!</h1>
    <p><bean:write name="message"/></p>
    <a href="sample.jsp">Back</a>
</body>
</html>
EOF

    # Index JSP
    cat > src/main/webapp/index.jsp << 'EOF'
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <title>Legacy Struts Application</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body>
    <h1>Welcome to Legacy Struts Application</h1>
    <ul>
        <li><a href="sample.jsp">Sample Form</a></li>
    </ul>
</body>
</html>
EOF

    print_status "Created sample Struts application code"
}

# メイン処理
main() {
    case "${1:-all}" in
        "env")
            show_environment
            ;;
        "init")
            init_project
            ;;
        "config")
            create_struts_config
            create_build_xml
            ;;
        "scripts")
            create_dev_scripts
            ;;
        "sample")
            create_sample_code
            ;;
        "all")
            show_environment
            init_project
            create_struts_config
            create_build_xml
            create_dev_scripts
            create_sample_code
            print_status "Development environment setup completed!"
            echo
            echo "Next steps:"
            echo "1. Add Struts JAR files to lib/ directory"
            echo "2. Run 'ant clean build' to build the application"
            echo "3. Deploy dist/legacy-app.war to Tomcat"
            echo "4. Start development with 'code .'"
            ;;
        "help"|"-h"|"--help")
            echo "Usage: $0 [command]"
            echo ""
            echo "Commands:"
            echo "  all (default) - Complete setup"
            echo "  env          - Show environment info"
            echo "  init         - Initialize directory structure"
            echo "  config       - Create configuration files"
            echo "  scripts      - Create development scripts"
            echo "  sample       - Create sample code"
            echo "  help         - Show this help"
            ;;
        *)
            echo "Unknown command: $1"
            echo "Use '$0 help' for usage information."
            exit 1
            ;;
    esac
}

main "$@"
