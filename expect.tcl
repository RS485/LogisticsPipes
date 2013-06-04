set timeout -1
spawn java -jar svr.jar
expect {
 -re ".*INFO.*Done.*" {exit 0}
 -re ".*unexpected exception.*" {exit 1}
}