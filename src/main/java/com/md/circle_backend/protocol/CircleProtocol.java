package com.md.circle_backend.protocol;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

public final class CircleProtocol {

    public static final int SYNC_1 = 0xA5;
    public static final int SYNC_2 = 0x5A;
    public static final int VERSION = 0x01;

    public static final int MSG_TYPE_COMMAND = 0x01;
    public static final int MSG_TYPE_ACK = 0x81;
    public static final int MSG_TYPE_NACK = 0x82;
    public static final int MSG_TYPE_STATUS = 0x83;

    public static final int CMD_START_ROTATION = 0x01;
    public static final int CMD_STOP_ROTATION = 0x02;
    public static final int CMD_GO_TO_ANGLE = 0x03;

    private static final AtomicInteger sequenceNumber = new AtomicInteger(1);

    private CircleProtocol() {
    }

    public static byte[] buildStartFrame() {
        return buildCommandFrame(CMD_START_ROTATION, new byte[0]);
    }

    public static byte[] buildStopFrame() {
        return buildCommandFrame(CMD_STOP_ROTATION, new byte[0]);
    }

    public static byte[] buildGoToAngleFrame(double angle) {
        int angleX10 = (int) Math.round(angle * 10.0);

        byte[] payload = new byte[] {
                (byte) ((angleX10 >> 8) & 0xFF),
                (byte) (angleX10 & 0xFF)
        };

        return buildCommandFrame(CMD_GO_TO_ANGLE, payload);
    }

    private static byte[] buildCommandFrame(int cmdId, byte[] payload) {

        int len = payload.length;
        int seq = sequenceNumber.getAndIncrement();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write(SYNC_1);
        out.write(SYNC_2);
        out.write(VERSION);
        out.write(MSG_TYPE_COMMAND);

        out.write((seq >> 8) & 0xFF);
        out.write(seq & 0xFF);

        out.write(cmdId & 0xFF);

        out.write((len >> 8) & 0xFF);
        out.write(len & 0xFF);

        for (byte b : payload) {
            out.write(b & 0xFF);
        }

        byte[] withoutCrc = out.toByteArray();

        int crc = crc16CcittFalse(withoutCrc, 2, withoutCrc.length - 2);

        out.write((crc >> 8) & 0xFF);
        out.write(crc & 0xFF);

        return out.toByteArray();
    }

    public static int readUInt16(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
    }

    public static int crc16CcittFalse(byte[] data, int offset, int length) {
        int crc = 0xFFFF;

        for (int i = offset; i < offset + length; i++) {
            crc ^= (data[i] & 0xFF) << 8;

            for (int bit = 0; bit < 8; bit++) {

                if ((crc & 0x8000) != 0) {
                    crc = ((crc << 1) ^ 0x1021)  & 0xFFFF;
                } else {
                    crc = (crc << 1)  & 0xFFFF;
                }
            }
        }

        return crc & 0xFFFF;
    }
}