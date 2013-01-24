class-dumper
============

Simple application which allows to dump java class contents from a running application.
Basically, this is a built sample from this blog post - https://blogs.oracle.com/sundararajan/entry/retrieving_class_files_from_a

Usage:
  1. Download and unpack class-dumper.tag.gz;
  2. Execute the following command:
     java -cp $JAVA_HOME/lib/tools.jar:<full-path-to-unpacked-archive-content> org.Attacher <pid> <full-path-of-unpacked-agent.jar> dumpDir=<dir>,classes=<name-pattern>