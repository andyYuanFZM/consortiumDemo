## 搭建JAVA-SDK开发环境
### 下载JAVA-SDK
1. 下载最新版本的JAVA-SDK包[[下载链接]](https://github.com/33cn/chain33-sdk-java/releases/download/1.0.18/chain33-sdk-java-1.0.18.zip)  
2. 解压JAVA-SDK压缩包。  
3. 将SDK压缩包中的JAR包安装到本地仓库  
```
# 在jar包所在目录，执行如下命令
mvn install:install-file -Dfile=chain33-sdk-java.jar -DgroupId=cn.chain33 -DartifactId=chain33-sdk-java -Dversion=1.0.18 -Dpackaging=jar
```
执行结果中打印BUILD SUCCESS，表明添加成功。
如果因time out导致构建失败，可以再次执行以上命令，直至构建成功。

### 引用JAVA-SDK
在现有JAVA项目中导入JAVA-SDK以及相关依赖, 下文以新建一个工程为例说明。

1. 使用IDE创建一个基于Maven构建的工程（新建工程时的GroupId、ArtifactId、Version等参数根据实际需求设置）  
2. 在工程的pom.xml中添加下述依赖,并进行依赖安装  
```
<dependencies>
       <dependency>
         <groupId>cn.chain33</groupId>
         <artifactId>chain33-sdk-java</artifactId>
         <version>1.0.18</version>
       </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-api</artifactId>
            <version>2.17.1</version>
        </dependency>
        <dependency>
            <groupId>com.google.protobuf</groupId>
            <artifactId>protobuf-java</artifactId>
            <version>3.16.1</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.47</version>
        </dependency>
        <dependency>
            <groupId>org.bitcoinj</groupId>
            <artifactId>bitcoinj-core</artifactId>
            <version>0.14.7</version>
        </dependency>
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpclient</artifactId>
            <version>4.5.13</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>1.7.25</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>1.7.25</version>
        </dependency>
        <dependency>
            <groupId>org.bouncycastle</groupId>
            <artifactId>bcprov-jdk15on</artifactId>
            <version>1.67</version>
        </dependency>
        <dependency>
            <groupId>net.vrallev.ecc</groupId>
            <artifactId>ecc-25519-java</artifactId>
            <version>1.0.3</version>
        </dependency>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-all</artifactId>
            <version>1.34.1</version>
        </dependency>
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>30.0-jre</version>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-collections4</artifactId>
            <version>4.4</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-core</artifactId>
            <version>2.12.4</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
            <version>2.12.4</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.12.4</version>
        </dependency>
</dependencies>
```
如果导入依赖缓慢，或出现Connection timed out的报错信息，则可能是因为默认中央仓库下载超时，可以切换成mvn的阿里云镜像重试。  
```
<!—阿里云镜像 -->
<mirror> 
    <id>alimaven</id> 
    <name>aliyun maven</name>
    <url>http://maven.aliyun.com/nexus/content/groups/public/</url> 
    <mirrorOf>central</mirrorOf>         
</mirror>
```
如果结果中打印BUILD SUCCESS则表明执行成功；否则根据报错信息检查并修复错误  