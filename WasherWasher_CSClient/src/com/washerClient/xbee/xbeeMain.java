package com.washerClient.xbee;

import java.util.ArrayList;
import java.util.HashMap;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.XBee64BitAddress;

public class xbeeMain {

	// Xbee64BitAddress : 占쏙옙占쏙옙占� XBEEAddr
	// RemotXBeeDevice : 占쏙옙占쏙옙 占쏙옙占쌈듸옙 XBEE
	// deviceAddr : 占쏙옙占쏙옙占� device Array
	public static HashMap<XBee64BitAddress, RemoteXBeeDevice> deviceMap = new HashMap<>();
	public static HashMap<XBee64BitAddress, String> deviceState = new HashMap<>();
	public static HashMap<XBee64BitAddress, Integer> deviceActCkeck = new HashMap<>();
	public static ArrayList<XBee64BitAddress> deviceAddr = new ArrayList<>();
	
	public static XBeeDevice myDevice;

//	private static final String PORT = "COM11"; // Coordinator
	private static final String PORT = "/dev/ttyUSB0"; // Coordinator
	private static final int BAUD_RATE = 9600;
	static boolean sendCheck = false;

	public void startXbee() {
		myDevice = new XBeeDevice(PORT, BAUD_RATE);

		try {
			myDevice.open();
			myDevice.addDataListener(new MyDataReceiveListener());

		} catch (XBeeException e) {
			e.printStackTrace();
			myDevice.close();
			System.exit(1);
		} catch (Exception e) {
			myDevice.close();
			System.exit(1);
		}
	}
	
	// 占쏙옙占쏙옙占쏙옙 처占쏙옙 -> JSon -> String -> Byte
	public static void sendXBeecode(String xbeeAdd, String getLockState) {
		//lockState=true -> open device , false -> close device
		byte[] dataToSend= getLockState.getBytes();		
		RemoteXBeeDevice sendDevice =deviceMap.get(new XBee64BitAddress(xbeeAdd));

		try {
			System.out.format("Sending data to %s >> %s %n", sendDevice.get64BitAddress(), new String(dataToSend));
			myDevice.sendData(sendDevice, dataToSend);

		} catch (XBeeException e) {
			System.out.println("Sending Error");
			e.printStackTrace();
			//System.exit(1);
		} 
	}
	
}
