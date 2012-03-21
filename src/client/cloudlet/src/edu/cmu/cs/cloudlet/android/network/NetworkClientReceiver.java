package edu.cmu.cs.cloudlet.android.network;

import java.io.DataInputStream;
import java.io.IOException;
import edu.cmu.cs.cloudlet.android.util.KLog;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class NetworkClientReceiver extends Thread {
	private Handler mHandler;
	private DataInputStream networkReader;
	private boolean isThreadRun = true;
	
	public NetworkClientReceiver(DataInputStream dataInputStream, Handler mHandler) {
		this.networkReader = dataInputStream;
		this.mHandler = mHandler;
	}

	@Override
	public void run() {
		while(isThreadRun == true){
			NetworkMsg msg;
			try {
				msg = this.receiveMsg(networkReader);
			} catch (IOException e) {
				KLog.printErr(e.toString());
				this.notifyStatus(CloudletConnector.NETWORK_ERROR, e.toString(), null);
				break;
			}
			
			if(msg == null){			
				try { Thread.sleep(200);} catch (InterruptedException e) {}
				continue;
			}else{
				this.notifyStatus(CloudletConnector.PROGRESS_MESSAGE, "received..", msg);				
			}
		}
	}
	
	private void notifyStatus(int command, String string, NetworkMsg recvMsg) {
		Message msg = Message.obtain();
		msg.what = command;
		msg.obj = recvMsg;
		Bundle data = new Bundle();
		data.putString("message", string);
		msg.setData(data);
		this.mHandler.sendMessage(msg);
	}

	private NetworkMsg receiveMsg(DataInputStream reader) throws IOException {
		NetworkMsg receiveMsg = null;
		int msgNumber = reader.readInt();
		int payloadLength = reader.readInt();
		byte[] jsonByte = new byte[payloadLength];
		reader.read(jsonByte, 0, jsonByte.length);			
		receiveMsg = new NetworkMsg(msgNumber, payloadLength, jsonByte);
	
		return receiveMsg;
	}

	/*
	 * Network Command Handler method
	 * --> Moved to to Cloudlet Connector Class
	 *
	private void handleVMList(NetworkMsg recvMsg) {
	}	
	private void handleTrasferStart(NetworkMsg msg) {
	}
	private void handleVMLaunch(NetworkMsg msg) {
	}
	private void handleVMStop(NetworkMsg msg) {
	}
	 */
	
	public void close() {
		this.isThreadRun = false;		
		try {
			if(this.networkReader != null)
				this.networkReader.close();
		} catch (IOException e) {
			KLog.printErr(e.toString());
		}
	}
}