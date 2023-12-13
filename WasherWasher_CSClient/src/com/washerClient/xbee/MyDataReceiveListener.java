package com.washerClient.xbee;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.models.XBeeMessage;

public class MyDataReceiveListener implements IDataReceiveListener {

	@Override
	public void dataReceived(XBeeMessage xbeeMessage) {
		XBee64BitAddress address = xbeeMessage.getDevice().get64BitAddress();
		checking64BitAddress(address);
		String receiveData = new String(xbeeMessage.getData());
		String[] templist = receiveData.split("/");
		// System.out.format("From %s >> %s | %s%n", address,
		// HexUtils.prettyHexString(HexUtils.byteArrayToHexString(xbeeMessage.getData())),
		// new String(xbeeMessage.getData()));
		for (int i = 0; i < templist.length; i++) {
			xbeeMain.deviceActCkeck.replace(address,xbeeMain.deviceActCkeck.get(address)+1);
			if (templist[i].equals("OK")) {
				xbeeMain.sendCheck = true;
			} else {
				String value = xbeeMain.deviceState.get(address);
				if (!value.equals(templist[i]) || value.equals("st")) {
					xbeeMain.deviceState.replace(address, templist[i]);
				}
				System.out.format("From %s >> %s %n", address, templist[i]);
			}
		}
	}

	private void checking64BitAddress(XBee64BitAddress address) {
		if (!xbeeMain.deviceMap.containsKey(address)) {
			RemoteXBeeDevice remoteDevice = new RemoteXBeeDevice(xbeeMain.myDevice, address);
			xbeeMain.deviceMap.put(address, remoteDevice);
			xbeeMain.deviceState.put(address, "st");
			xbeeMain.deviceActCkeck.put(address, 0);
			xbeeMain.deviceAddr.add(address);

		}
	}
}
