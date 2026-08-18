package com.md.circle_backend.service;

import com.md.network.tcp.TcpClient;
import com.md.circle_backend.protocol.CircleProtocol;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
public class CircleTcpService {
    private final CircleStateService stateService;
    private final String host;
    private final int port;
    private TcpClient client;
    private final SimpMessagingTemplate messagingTemplate;

    public CircleTcpService(
            CircleStateService stateService,
            SimpMessagingTemplate messagingTemplate,
            @Value("${circle.server.host}") String host,
            @Value("${circle.server.port}") int port) {

        this.stateService = stateService;
        this.messagingTemplate = messagingTemplate;
        this.host = host;
        this.port = port;
    }

    @PostConstruct
    public void start() {

        System.out.println("[CIRCLE TCP] Connecting to " + host + ":" + port);
        client = new TcpClient(host, port);
        //equivalent to "frame -> this.handleFrame(frame)". Means "When network-lib receives one frame, it calls handleFrame(frame)"
        client.setMessageHandler(this::handleFrame);
        client.start();
    }

    @PreDestroy
    public void stop() {
        if (client != null) {
            client.shutdown();
        }
    }

    private void handleFrame(byte[] frame) {
        System.out.println("[CIRCLE TCP RX] " + toHex(frame));

        if (frame == null || frame.length < 11) {
            System.out.println("[CIRCLE TCP] Frame too short");
            return;
        }

        if (((frame[0] & 0xFF) != CircleProtocol.SYNC_1 ) || ((frame[1] & 0xFF) != CircleProtocol.SYNC_2)) {
            System.out.println("[CIRCLE TCP] Invalid sync");
            return;
        }

        int version = frame[2] & 0xFF;

        if (version != CircleProtocol.VERSION) {
            System.out.println("[CIRCLE TCP] Invalid version");
            return;
        }

        int msgType = frame[3] & 0xFF;

        int len = CircleProtocol.readUInt16(frame, 7);

        int expectedLength = 9 + len + 2;

        if (frame.length != expectedLength) {
            System.out.println("[CIRCLE TCP] Invalid length");
            return;
        }

        int receivedCrc = CircleProtocol.readUInt16(frame, 9 + len);

        int calculatedCrc = CircleProtocol.crc16CcittFalse(frame, 2, 7 + len);

        if (receivedCrc != calculatedCrc) {
            System.out.println("[CIRCLE TCP] Invalid CRC");
            return;
        }

        if (msgType == CircleProtocol.MSG_TYPE_STATUS) {
            handleStatus(frame, len);
            return;
        }

        if (msgType == CircleProtocol.MSG_TYPE_ACK) {
            handleAck(frame, len);
            return;
        }

        if (msgType == CircleProtocol.MSG_TYPE_NACK) {
            handleNack(frame, len);
            return;
        }

        System.out.println("[CIRCLE TCP] Unknown message type: " + msgType);
    }

    private void handleStatus(byte[] frame, int len) {

        if (len != 3) {
            System.out.println("[STATUS] Invalid payload length");
            return;
        }

        int angleX10 = CircleProtocol.readUInt16(frame, 9);
        boolean rotating = (frame[11] & 0xFF) == 1;
        double angle = angleX10 / 10.0;

        publishState(angle, rotating);

        System.out.println("[STATUS] angle=" + angle + ", rotating=" + rotating);
    }

    private void handleAck(byte[] frame, int len) {

        if (len != 4) {
            System.out.println("[ACK] Invalid payload length");
            return;
        }

        int seq = CircleProtocol.readUInt16(frame, 4);

        int cmdId = frame[6] & 0xFF;

        int angleX10 = CircleProtocol.readUInt16(frame, 10);

        boolean rotating = (frame[12] & 0xFF) == 1;

        double angle = angleX10 / 10.0;

       publishState(angle, rotating);

        System.out.println("[ACK] seq=" + seq + ", cmd=" + cmdId + ", angle=" + angle + ", rotating=" + rotating);
    }

    private void handleNack(byte[] frame, int len) {

        if (len != 4) {
            System.out.println("[NACK] Invalid payload length");
            return;
        }

        int errorCode = frame[9] & 0xFF;

        int angleX10 = CircleProtocol.readUInt16(frame, 10);

        boolean rotating = (frame[12] & 0xFF) == 1;

        publishState(angleX10 / 10.0, rotating);

        System.out.println("[NACK] errorCode=" + errorCode);
    }

    public void startRotation() {
        client.sendMessage(CircleProtocol.buildStartFrame());
    }

    public void stopRotation() {
        client.sendMessage(CircleProtocol.buildStopFrame());
    }

    public void goToAngle(double angle) {
        client.sendMessage(CircleProtocol.buildGoToAngleFrame(angle));
    }

    private String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder();

        for (byte b : data) {
            if (sb.length() > 0) {
                sb.append(' ');
            }

            sb.append(String.format("%02X", b & 0xFF));
        }
        return sb.toString();
    }

    private void publishState(double angle, boolean rotating) {
        stateService.updateState(angle, rotating);
        messagingTemplate.convertAndSend(
                "/topic/circle",
                stateService.getState()
        );
    }
}