FROM debian:bullseye-slim

# メタデータ
LABEL maintainer="shinyay"
LABEL description="Java 1.5.0_22 (J2SE 5.0) environment for legacy Struts applications"
LABEL java.version="1.5.0_22"
LABEL java.vendor="Sun Microsystems"

# 開発に必要なパッケージ
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    ca-certificates \
    rpm2cpio \
    cpio \
    coreutils \
    tzdata \
    # 開発ツール
    curl \
    wget \
    git \
    vim \
    nano \
    unzip \
    zip \
    tree \
    less \
    htop \
    procps \
    # ビルドツール
    ant \
    # ネットワークツール
    telnet \
    netcat \
    # デバッグツール
    strace \
    tcpdump \
    # SSH関連
    openssh-client \
    # 日本語フォント（コンソール表示用）
    fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

# タイムゾーンの設定
ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 非特権ユーザーの作成
RUN groupadd -r javauser && useradd -r -g javauser -d /app -s /bin/bash javauser

# JDKバイナリのコピー（ローカルに存在する場合）
# ファイルが存在しない場合はビルド中にArchive.orgからダウンロード
COPY jdk-1_5_0_22-linux-amd64-rpm.bin* /tmp/

# JDKインストール（rootユーザーで実行）
# ローカルファイルが無い場合はArchive.orgから自動ダウンロード
ARG JDK_SHA256=fe2005d11c60b093b86461bc82f2b3590b7298c077195657b1973afae340127b
RUN cd /tmp && \
    # ローカルファイルが無い場合はダウンロード
    if [ ! -f jdk-1_5_0_22-linux-amd64-rpm.bin ]; then \
        echo "JDK binary not found locally. Downloading from Archive.org..." && \
        wget -q --show-progress -O jdk-1_5_0_22-linux-amd64-rpm.bin \
            "https://archive.org/download/Java_5_update_22/jdk-1_5_0_22-linux-amd64-rpm.bin" && \
        echo "Download complete. Verifying checksum..." && \
        ACTUAL_SHA256=$(sha256sum jdk-1_5_0_22-linux-amd64-rpm.bin | awk '{print $1}') && \
        if [ "$ACTUAL_SHA256" != "$JDK_SHA256" ]; then \
            echo "ERROR: SHA256 checksum mismatch!" && \
            echo "  Expected: $JDK_SHA256" && \
            echo "  Actual:   $ACTUAL_SHA256" && \
            rm -f jdk-1_5_0_22-linux-amd64-rpm.bin && \
            exit 1; \
        fi && \
        echo "Checksum verified successfully."; \
    else \
        echo "Using locally provided JDK binary."; \
    fi && \
    # JDKインストーラーの実行権限付与
    chmod +x jdk-1_5_0_22-linux-amd64-rpm.bin && \
    echo "Starting automated JDK installation..." && \
    # 複数の方法でライセンス同意とインストールを試行
    (yes | timeout 300 ./jdk-1_5_0_22-linux-amd64-rpm.bin) || \
    (printf " \n\nyes\n\n\n" | timeout 300 ./jdk-1_5_0_22-linux-amd64-rpm.bin) || \
    (echo "yes" | timeout 300 ./jdk-1_5_0_22-linux-amd64-rpm.bin -x) || \
    (printf "yes\nyes\nyes\n\n\n\n " | timeout 300 ./jdk-1_5_0_22-linux-amd64-rpm.bin) || \
    { echo "All JDK installation methods failed" && exit 1; } && \
    echo "JDK installer execution completed" && \
    # RPMファイルの確認
    if ! ls *.rpm 1> /dev/null 2>&1; then \
        echo "Error: No RPM files found after installation" && \
        find /tmp -type f -name "*" && \
        exit 1; \
    fi && \
    # RPMファイルを直接展開
    for rpm in *.rpm; do \
        if [ -f "$rpm" ]; then \
            echo "Extracting RPM: $rpm" && \
            rpm2cpio "$rpm" | cpio -idmv || { \
                echo "Error: Failed to extract $rpm" && \
                exit 1; \
            }; \
        fi; \
    done && \
    # Javaディレクトリの作成
    mkdir -p /usr/java && \
    # JDKディレクトリの検索と移動
    jdk_dir="" && \
    if [ -d "usr/java/jdk1.5.0_22" ]; then \
        jdk_dir="usr/java/jdk1.5.0_22"; \
    elif [ -d "jdk1.5.0_22" ]; then \
        jdk_dir="jdk1.5.0_22"; \
    else \
        jdk_dir=$(find . -name "jdk1.5.0_22" -type d | head -1); \
    fi && \
    # JDKディレクトリの存在確認と移動
    if [ -z "$jdk_dir" ] || [ ! -d "$jdk_dir" ]; then \
        echo "Error: JDK directory not found" && \
        echo "Available directories:" && \
        find . -name "*jdk*" -type d && \
        find . -name "*java*" -type d && \
        exit 1; \
    fi && \
    echo "Moving JDK from: $jdk_dir" && \
    mv "$jdk_dir" /usr/java/ && \
    # インストール確認
    if [ ! -d "/usr/java/jdk1.5.0_22" ]; then \
        echo "Error: JDK installation failed" && \
        exit 1; \
    fi && \
    echo "JDK installation completed successfully"

# JDK pack200形式のjarファイルを解凍（別のRUNコマンドで明確化）
RUN echo "Unpacking compressed JAR files..." && \
    cd /usr/java/jdk1.5.0_22/jre/lib && \
    /usr/java/jdk1.5.0_22/bin/unpack200 rt.pack rt.jar && \
    /usr/java/jdk1.5.0_22/bin/unpack200 jsse.pack jsse.jar && \
    /usr/java/jdk1.5.0_22/bin/unpack200 charsets.pack charsets.jar && \
    cd /usr/java/jdk1.5.0_22/jre/lib/ext && \
    /usr/java/jdk1.5.0_22/bin/unpack200 localedata.pack localedata.jar && \
    cd /usr/java/jdk1.5.0_22/lib && \
    /usr/java/jdk1.5.0_22/bin/unpack200 tools.pack tools.jar && \
    echo "JAR files unpacked successfully"

# 不要ファイルの削除とディレクトリの権限設定
RUN echo "Cleaning up unnecessary files..." && \
    # pack200ファイルの削除（解凍後は不要）
    rm -f /usr/java/jdk1.5.0_22/jre/lib/*.pack && \
    rm -f /usr/java/jdk1.5.0_22/jre/lib/ext/*.pack && \
    rm -f /usr/java/jdk1.5.0_22/lib/*.pack && \
    # 一時ファイルの削除
    rm -rf /tmp/* && \
    # ディレクトリの権限設定
    chown -R javauser:javauser /usr/java/jdk1.5.0_22 && \
    chmod -R 755 /usr/java/jdk1.5.0_22 && \
    # アプリケーションディレクトリの準備
    mkdir -p /app && \
    chown javauser:javauser /app && \
    # 開発用ディレクトリの作成
    mkdir -p /app/src /app/lib /app/build /app/dist /app/docs && \
    chown -R javauser:javauser /app && \
    echo "Cleanup completed"

# Apache Antのダウンロードとインストール（Antパッケージが古い可能性があるため）
RUN echo "Installing Apache Ant 1.6.5 (compatible with Java 5)..." && \
    cd /tmp && \
    wget -q https://archive.apache.org/dist/ant/binaries/apache-ant-1.6.5-bin.tar.gz && \
    tar -xzf apache-ant-1.6.5-bin.tar.gz && \
    mv apache-ant-1.6.5 /opt/ant && \
    chown -R javauser:javauser /opt/ant && \
    rm -f apache-ant-1.6.5-bin.tar.gz && \
    echo "Apache Ant installation completed"

# 環境変数の設定（ホストの環境変数を確実に上書き）
ENV JAVA_HOME=/usr/java/jdk1.5.0_22
ENV ANT_HOME=/opt/ant
ENV PATH=/usr/java/jdk1.5.0_22/bin:/opt/ant/bin:$PATH
ENV CLASSPATH=.

# デフォルトユーザーを非特権ユーザーに変更
USER javauser

# ワーキングディレクトリの設定
WORKDIR /app

# ヘルスチェックの追加
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD java -version > /dev/null 2>&1 || exit 1

# デフォルトコマンド
CMD ["/bin/bash"]
