package edu.umn.bulletinboard.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.umn.bulletinboard.client.cli.BaseCommand;
import edu.umn.bulletinboard.client.cli.CommandFactory;
import edu.umn.bulletinboard.client.constants.CommandConstants;
import edu.umn.bulletinboard.client.exceptions.ClientNullException;
import edu.umn.bulletinboard.client.exceptions.IllegalCommandException;
import edu.umn.bulletinboard.common.constants.RMIConstants;
import edu.umn.bulletinboard.common.exception.IllegalIPException;
import edu.umn.bulletinboard.common.rmi.BulletinBoardService;
import edu.umn.bulletinboard.common.server.ServerInfo;
import edu.umn.bulletinboard.common.util.LogUtil;

/**
 * RMI Client, shell interface.
 * 
 * @author Abhijeet
 * 
 */
public class Client {

	private static final String CMD_PROMPT = "\nBulletinBoard-Client-1.0$ ";
	private static final String GOOD_BYE_MSG = "Good Bye!";

	private static final String USAGE_HELP = "arguments: <rmi_server_host> <rmi_server_port>";

	private BulletinBoardService client;

    private static Client instance = null;

    private boolean isRead = false;
    private boolean isRYW = false;

    public void setRead(boolean iR) {
        this.isRead = iR;
    }

    public boolean isReadSet() {
        return isRead;
    }

    public boolean isRYWSet() {
        return isRYW;
    }

    public void setRYW(boolean RYW) {
        this.isRYW = RYW;
    }

	private Client(){}

	private Client(BulletinBoardService client) throws MalformedURLException,
			RemoteException, NotBoundException {
		this.client = client;
	}

    public synchronized static Client getInstance() {
        if (null == instance) {
            instance = new Client();
        }

        return instance;
    }

    public synchronized static Client getInstance(BulletinBoardService client) throws RemoteException
            , NotBoundException, MalformedURLException {
        if (null == instance) {
            instance = new Client(client);
        }

        return instance;
    }

    public void setClient(BulletinBoardService cli) {
        this.client = cli;
    }

    public BulletinBoardService getClient() {
        return client;
    }

	void executeCmd(String cmdStr) throws NumberFormatException,
			ClientNullException {

		BaseCommand cmd;
		try {
			cmd = CommandFactory.getCommand(cmdStr);

            long start = System.nanoTime();
			if (!cmd.execute()) {
				LogUtil.info(CommandConstants.ERR_COMMAND_EXEC_FAILED);
			}

            System.out.println("Time elapsed: " + (System.nanoTime()
                    - start)/Math.pow(10,6) + " ms.");

		} catch (IllegalCommandException e) {
			LogUtil.error("", e.getMessage());
		} catch (RemoteException e) {
			LogUtil.catchedRemoteException(e);
		} catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

	/**
	 * Starts the shell and accepts commands. Ends when "exit" or "quit" is
	 * encountered.
	 * 
	 * @throws IOException
	 * @throws ClientNullException
	 * @throws IllegalCommandException
	 * @throws NumberFormatException
	 */
	public void startShell() throws IOException, NumberFormatException,
			ClientNullException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String cmd;

		try {

			// TODO: Improve; this makes it slow but for now needs to
			// synchronized with UDP thread printing data.

			System.out.print(CMD_PROMPT);

			while ((cmd = in.readLine()) != null) {
				if (cmd.isEmpty() || cmd.startsWith("#")) {
					System.out.print(CMD_PROMPT);
					continue;
				}

				if (cmd.trim().equalsIgnoreCase("exit")
						|| cmd.trim().equalsIgnoreCase("quit")) {
					break;
				}

				executeCmd(cmd);

                System.out.print(CMD_PROMPT);
			}

			System.out.print(CMD_PROMPT);
		} finally {
			LogUtil.info(GOOD_BYE_MSG);
		}
	}

	/**
	 * Drives the client execution.
	 * 
	 * TODO: Poor exception handling, everything is propagated and printed in
	 * this method currently, custom err messages should be printed where the
	 * exception is raised.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		try {

            if (1 == args.length) {
                Client.getInstance().setRYW(true);
            }

			Client.getInstance().startShell();

		} catch (RemoteException e) {
			LogUtil.catchedRemoteException(e);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientNullException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
