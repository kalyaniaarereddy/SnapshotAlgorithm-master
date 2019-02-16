# See README.txt.

.PHONY: all java clean

all: java

java:   controller   branch

clean:
	rm -f controller branch
	rm -f javac_middleman Controller*.class Branch*.class *.class
	rm -f protoc_middleman Bank.java

protoc_middleman: bank.proto
	protoc $$PROTO_PATH --java_out=. bank.proto
	@touch protoc_middleman


javac_middleman: Controller.java Branch.java protoc_middleman
	javac -cp $$CLASSPATH Controller.java Branch.java Bank.java
	@touch javac_middleman

controller: javac_middleman
	@echo "Writing shortcut script controller..."
	@echo '#! /bin/sh' > controller
	@echo 'java -classpath .:$$CLASSPATH Controller "$$@"' >> controller
	@chmod +x controller

branch: javac_middleman
	@echo "Writing shortcut script branch..."
	@echo '#! /bin/sh' > branch
	@echo 'java -classpath .:$$CLASSPATH Branch "$$@"' >> branch
	@chmod +x branch

