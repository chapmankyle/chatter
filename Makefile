# Java Makefile

# JAVA flags
JFLAGS = -g
JVM = java
JC = javac

# directories
JCLASSDIR = bin
JSOURCEDIR = src
JGUISOURCEDIR = $(JSOURCEDIR)/scene-builder

# compiling the class
%.class: %.java
	$(JC) $(JFLAGS) $<

# set sources
SRCS := $(wildcard $(JSOURCEDIR)/*.java)

# set classes
CLS := $(wildcard $(JCLASSDIR)/*.class)

# location of main functions
SERVER = Server
CLIENT = Client

# rules
default: all

# `make all`
all:
	mkdir -p $(JCLASSDIR)
	$(JC) -d ./$(JCLASSDIR) $(JFLAGS) $(SRCS)

# `make server`
server:
	mkdir -p $(JCLASSDIR)
	$(JC) -d ./$(JCLASSDIR) $(JFLAGS) $(SRCS)
	$(JVM) -cp ./$(JCLASSDIR) $(SERVER)

# `make client`
client:
	mkdir -p $(JCLASSDIR)
	$(JC) -d ./$(JCLASSDIR) $(JFLAGS) $(SRCS)
	$(JVM) -cp ./$(JCLASSDIR) $(CLIENT)

# `make clean`
.PHONY: clean
clean:
	rm -rf $(JCLASSDIR)
