# Chatter
A peer-to-peer communication program written in Java.

## Getting started
Clone this repo and navigate to the `chatter` folder.
```bash
# clone this repo
git clone https://github.com/chapmankyle/chatter.git

# navigate to chatter folder
cd chatter/
```

To compile the project, run the `make all` command:
```bash
# compiles and links all necessary files
make all
```

## Execution
**NB:** You will need at least two terminal shells open to run both the server
and the client(s).

To execute and run the program, the **server** needs to be started and then the
**client** needs to be started. This can be accomplished by running the following
commands:

**`terminal #1`**
```bash
# start the server
make server
```
**`terminal #2`**
```bash
# start the client
make client
```
If all goes well, you should see some output in both the `server` terminal and
the `client` terminal sessions.

If there are any problems, please email kyleichapman@gmail.com.
