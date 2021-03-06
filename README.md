This is a simple Bulletin Board System in which client can post, reply and read articles stored in the BB. The BB is maintained by a group of replicated servers that offer Sequential consistency, Quorum consistency and Read-your-Write consistency.

Authors:

Abhijeet Gaikwad 

Prashant Chaudhary 

Building:
This project was built using maven, hopefully you will not have to build as we developed
in Java and providing already compiled Jars. In case you will have to, these steps will
help on any of the cs machines in the lab:
  1. module load java/maven
  2. Extract the tar, change directory to extracted dir, let this dir be PROJ_HOME
  4. run (from PROJ_HOME): mvn clean install
  5. Build is created in this dir: PROJ_HOME/dist/target/bulletinboard-1.0-SNAPSHOT/ or
     PROJ_HOME/dist/target/bulletinboard-1.0-SNAPSHOT.tar.gz

Steps to Run:
  1. Extract the tar ball given, let this dir be PROJ_HOME:
  -- Following commands will run client and sever:
    Client:
	  java -cp ./client-1.0-SNAPSHOT.jar:./server-1.0-SNAPSHOT.jar:./common-1.0-SNAPSHOT.jar edu.umn.bulletinboard.client.Client &lt;ryw&gt;

	  `ryw` argument is optional and needs to be given only if Server is using Read Your Write Consistency.

	Server:
	  java -cp ./client-1.0-SNAPSHOT.jar:./server-1.0-SNAPSHOT.jar:./common-1.0-SNAPSHOT.jar edu.umn.bulletinboard.server.Server &lt;server_ip &gt; &lt;server_port&gt; &lt;properties_file_name&gt;

	  server_ip: Host name of the RMI server
	  server_port: Port on which RMI server should run
	  properties_file_name: Properties file path. Sample files can be found in PROJ_HOME/conf/. Contains following properties:
	    NR: Read Quorum
		NW: Write Quorum
		coordinatingServerIp: Coordinating RMI Server IP.
		coordinatingServerRMIPort: Coordinating RMI Server port.
		consistencyType: Consistency Type (can be: `Quorum`, `Sequential`, `Read-your-Write` only).

		If coordinatingServerIp and coordinatingServerRMIPort are not present in the conf file, the server is considered as coordinator.

  2. Once a server is started, there is nothing required from user unless he/she wants to
   shut it down. Logs can be monitored on Server which list the activities made by
   client on server and/or coordinator console.

  3. For a list of commands that can be used on Command line client, please see design.txt.
