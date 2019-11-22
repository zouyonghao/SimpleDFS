set -ex

mvn clean
mvn package

$GRAALVM_HOME/bin/native-image -cp ./target/SimpleDFS-1.0-SNAPSHOT-jar-with-dependencies.jar -H:Name=sdfsMaster -H:Class=cn.edu.tsinghua.sdfs.server.master.Master -H:+ReportUnsupportedElementsAtRuntime --allow-incomplete-classpath

$GRAALVM_HOME/bin/native-image -cp ./target/SimpleDFS-1.0-SNAPSHOT-jar-with-dependencies.jar -H:Name=sdfsSlave -H:Class=cn.edu.tsinghua.sdfs.server.slave.SlaveKt -H:+ReportUnsupportedElementsAtRuntime --allow-incomplete-classpath

$GRAALVM_HOME/bin/native-image -cp ./target/SimpleDFS-1.0-SNAPSHOT-jar-with-dependencies.jar -H:Name=sdfsClient -H:Class=cn.edu.tsinghua.sdfs.client.Client -H:+ReportUnsupportedElementsAtRuntime --allow-incomplete-classpath
