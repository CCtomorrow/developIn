package com.qingy.ble;

import java.util.UUID;

/**
 * Copyright (C), 2014-2022, qingy
 *
 * <b>Project:</b> DevelopIn <br>
 * <b>Package:</b> com.qingy.ble <br>
 * <b>Create Date:</b> 2022/11/17 <br>
 * <b>@author:</b> qingy <br>
 * <b>Address:</b> qingyongai@gmail.com <br>
 * <b>Description:</b>  <br>
 */
public interface Constant {

    // Constants that indicate the current connection state
    int STATE_NONE = 0;       // we're doing nothing
    int STATE_LISTEN = 1;     // now listening for incoming connections
    int STATE_CONNECTING = 2; // now initiating an outgoing connection
    int STATE_CONNECTED = 3;  // now connected to a remote device

    //Start flag
    byte VISE_COMMAND_START_FLAG = (byte) 0xFF;
    //Protocol version
    byte VISE_COMMAND_PROTOCOL_VERSION = (byte) 0x01;

    /*Send Command Type*/
    byte VISE_COMMAND_TYPE_NONE = (byte) 0x00;
    byte VISE_COMMAND_TYPE_TEXT = (byte) 0x01;
    byte VISE_COMMAND_TYPE_FILE = (byte) 0x02;
    byte VISE_COMMAND_TYPE_IMAGE = (byte) 0x03;
    byte VISE_COMMAND_TYPE_AUDIO = (byte) 0x04;
    byte VISE_COMMAND_TYPE_VIDEO = (byte) 0x05;

    /*KEY*/
    String NAME_SECURE = "BluetoothChatSecure";
    String NAME_INSECURE = "BluetoothChatInsecure";

    /*UUID*/
    UUID UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    UUID UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    /*Message Type*/
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;

}
