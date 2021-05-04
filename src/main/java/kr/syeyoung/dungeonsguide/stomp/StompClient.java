package kr.syeyoung.dungeonsguide.stomp;

import lombok.Getter;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import sun.security.ssl.SSLSocketFactoryImpl;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public class StompClient extends WebSocketClient implements StompInterface {
    public StompClient(URI serverUri, final String token, CloseListener closeListener) throws Exception {
        super(serverUri);
        this.closeListener = closeListener;
        addHeader("Authorization", token);

        System.out.println("connecting websocket");
        if (!connectBlocking()) {
            throw new RuntimeException("Can't connect to ws");
        }
        System.out.println("connected, stomp handshake");
        while(this.stompClientStatus == StompClientStatus.CONNECTING);
        System.out.println("fully connected");
    }
    private final CloseListener closeListener;

    @Getter
    private volatile StompClientStatus stompClientStatus = StompClientStatus.CONNECTING;

    @Getter
    private StompPayload errorPayload;


    @Override
    public void onOpen(ServerHandshake handshakedata) {
        send(new StompPayload().method(StompHeader.CONNECT)
                .header("accept-version","1.2")
                .header("host",uri.getHost()).getBuilt()
        );
    }

    @Override
    public void onMessage(String message) {
        try {
            StompPayload payload = StompPayload.parse(message);
            if (payload.method() == StompHeader.CONNECTED) {
                stompClientStatus = StompClientStatus.CONNECTED;
            } else if (payload.method() == StompHeader.ERROR) {
                errorPayload = payload;
                stompClientStatus = StompClientStatus.ERROR;
                this.close();
            } else if (payload.method() == StompHeader.MESSAGE) {
                // mesage
                StompSubscription stompSubscription = stompSubscriptionMap.get(Integer.parseInt(payload.headers().get("subscription")));
                try {
                    stompSubscription.getStompMessageHandler().handle(this, payload);
                    if (stompSubscription.getAckMode() != StompSubscription.AckMode.AUTO) {
                        send(new StompPayload().method(StompHeader.ACK)
                                .header("id",payload.headers().get("ack")).getBuilt()
                        );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (stompSubscription.getAckMode() != StompSubscription.AckMode.AUTO) {
                        send(new StompPayload().method(StompHeader.NACK)
                                .header("id",payload.headers().get("ack")).getBuilt()
                        );
                    }
                }
            } else if (payload.method() == StompHeader.RECEIPT) {
                String receipt_id = payload.headers().get("receipt-id");
                StompPayload payload1 = receiptMap.remove(Integer.parseInt(receipt_id));
                if (payload1.method() == StompHeader.DISCONNECT) {
                    stompClientStatus = StompClientStatus.DISCONNECTED;
                    close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        closeListener.onClose(code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    private final Map<Integer, StompSubscription> stompSubscriptionMap = new HashMap<Integer, StompSubscription>();
    private final Map<Integer, StompPayload> receiptMap = new HashMap<Integer, StompPayload>();

    private int idIncrement = 0;

    @Override
    public void send(StompPayload payload) {
        if (stompClientStatus != StompClientStatus.CONNECTED) throw new IllegalStateException("not connected");
        payload.method(StompHeader.SEND);
        if (payload.headers().get("receipt") != null)
            receiptMap.put(Integer.parseInt(payload.headers().get("receipt")), payload);
        send(payload.getBuilt());
    }

    @Override
    public void subscribe(StompSubscription stompSubscription) {
        if (stompClientStatus != StompClientStatus.CONNECTED) throw new IllegalStateException("not connected");
        stompSubscription.setId(++idIncrement);

        send(new StompPayload().method(StompHeader.SUBSCRIBE)
                .header("id",String.valueOf(stompSubscription.getId()))
                .header("destination", stompSubscription.getDestination())
                .header("ack", stompSubscription.getAckMode().getValue()).getBuilt()
        );

        stompSubscriptionMap.put(stompSubscription.getId(), stompSubscription);
    }

    @Override
    public void unsubscribe(StompSubscription stompSubscription) {
        if (stompClientStatus != StompClientStatus.CONNECTED) throw new IllegalStateException("not connected");
        send(new StompPayload().method(StompHeader.UNSUBSCRIBE)
                .header("id",String.valueOf(stompSubscription.getId())).getBuilt()
        );
        stompSubscriptionMap.remove(stompSubscription.getId());
    }

    @Override
    public void disconnect() {
        if (stompClientStatus != StompClientStatus.CONNECTED) throw new IllegalStateException("not connected");
        StompPayload stompPayload;
        stompClientStatus =StompClientStatus.DISCONNECTING;
        send((stompPayload = new StompPayload().method(StompHeader.DISCONNECT)
                .header("receipt", String.valueOf(++idIncrement)))
                .getBuilt()
        );
        receiptMap.put(idIncrement, stompPayload);
    }
}
