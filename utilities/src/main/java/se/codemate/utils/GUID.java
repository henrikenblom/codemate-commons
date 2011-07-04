package se.codemate.utils;

import java.rmi.server.UID;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

/**
 * @deprecated Use UUIDGenerator instead
 */
public final class GUID {

    private static String HEX[] = {
            "0", "1", "2", "3", "4",
            "5", "6", "7", "8", "9",
            "A", "B", "C", "D", "E", "F"
    };

    private String guid;

    public GUID() {
        UID uid = new UID();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            bos.write(InetAddress.getLocalHost().getAddress());
        } catch (Exception exception) {
            // NO-OP
        }
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            uid.write(dos);
        } catch (IOException exception) {
            // NO-OP
        }
        byte[] bytes = bos.toByteArray();
        StringBuilder stringBuilder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            stringBuilder.append(HEX[b >> 4 & 0x0F]);
            stringBuilder.append(HEX[b & 0x0F]);
        }
        guid = stringBuilder.toString();
    }

    public String toString() {
        return guid;
    }
}
