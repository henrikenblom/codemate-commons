package se.codemate.utils;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.UUID;

public class UUIDGenerator {

    private static Random random = new Random();

    private static int version = 0x1000;

    private static long lastTime = Long.MIN_VALUE;

    private static long clockSeqAndNode = 0x8000000000000000L;

    static {
        try {
            final NetworkInterface networkInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            final Method method = networkInterface.getClass().getMethod("getHardwareAddress");
            final byte[] macAddress = (byte[]) method.invoke(networkInterface);
            clockSeqAndNode |= (((long) macAddress[0]) << 40) & 0xFF0000000000L;
            clockSeqAndNode |= (((long) macAddress[1]) << 32) & 0xFF00000000L;
            clockSeqAndNode |= (((long) macAddress[2]) << 24) & 0xFF000000L;
            clockSeqAndNode |= (((long) macAddress[3]) << 16) & 0xFF0000L;
            clockSeqAndNode |= (((long) macAddress[4]) << 8) & 0xFF00L;
            clockSeqAndNode |= (((long) macAddress[5])) & 0xFFL;
        } catch (Exception exception) {
            try {
                final byte[] ipAddress = InetAddress.getLocalHost().getAddress();
                clockSeqAndNode |= ((long) random.nextInt(0xFFFF)) << 32;
                clockSeqAndNode |= (ipAddress[0] << 24) & 0xFF000000L;
                clockSeqAndNode |= (ipAddress[1] << 16) & 0xFF0000L;
                clockSeqAndNode |= (ipAddress[2] << 8) & 0xFF00L;
                clockSeqAndNode |= ipAddress[3] & 0xFF;
            } catch (UnknownHostException e) {
                clockSeqAndNode |= random.nextInt(0xFFFFFF) << 24;
                clockSeqAndNode |= random.nextInt(0xFFFFFF);
                version = 0x4000;
            }
        }
        clockSeqAndNode |= ((long) random.nextInt(0x3FFF)) << 48;
    }

    public static UUID generateUUID() {
        return new UUID(generateTime(), clockSeqAndNode);
    }

    private static synchronized long generateTime() {

        long time;

        // UTC time

        long timeMillis = (System.currentTimeMillis() * 10000) + 0x01B21DD213814000L;

        if (timeMillis > lastTime) {
            lastTime = timeMillis;
        } else {
            timeMillis = ++lastTime;
        }

        // time low

        time = timeMillis << 32;

        // time mid

        time |= (timeMillis & 0xFFFF00000000L) >> 16;

        // version

        time |= version;

        // time hi

        time |= ((timeMillis >> 48) & 0x0FFF);

        return time;

    }

    public static void main(String[] args) {
        System.out.println(UUIDGenerator.generateUUID());
    }

}