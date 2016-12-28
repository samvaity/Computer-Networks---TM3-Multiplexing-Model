

import java.io.IOException;

public class RemoteProxyUplink extends Thread { 

	private final RemoteSocketConnection remoteConnection;

	RemoteProxyUplink(RemoteSocketConnection remoteConnection) {
		this.remoteConnection = remoteConnection;
	}

	public void run() {
		try {
			while (true) {

				if (remoteConnection.getRemoteEndpointSocket().isClosed()) {
					break;
				}

				if( remoteConnection.getUplinkData().containsKey(remoteConnection.getExpectedSequenceNumber()))
				{

					/**
					 * 
					 * Send data to remote server in order of the sequence number
					 */

					byte[] data = remoteConnection.getUplinkData().get(remoteConnection.getExpectedSequenceNumber());

					// check for fin/rst and break;

					if(data.length==1 && remoteConnection.isFin())
					{
						// FIN or RESET 
						break;
					}
					else
					{
						remoteConnection.setExpectedSequenceNumber((remoteConnection.getExpectedSequenceNumber() + 1));
						remoteConnection.getRemoteEndpointSocket().getOutputStream().write(data);
						remoteConnection.getRemoteEndpointSocket().getOutputStream().flush();
					}
				}

			}
		}

		catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!remoteConnection.getRemoteEndpointSocket().isClosed()) {
				try {
					remoteConnection.getRemoteEndpointSocket().close();
					System.out.println("Connection Closed by FIN or RST");
					Constants.connectionMap.remove(remoteConnection.getTcpConnectionID());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			Thread.currentThread().stop();
		}
	}

}
