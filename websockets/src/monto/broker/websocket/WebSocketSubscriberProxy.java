package monto.broker.websocket;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.net.InetSocketAddress;

public class WebSocketSubscriberProxy extends WebSocketServer {

    private ZContext context;
    private ZMQ.Socket socket;
    private WebSocket webSocket;
    private boolean running;

    public WebSocketSubscriberProxy(InetSocketAddress webSocketAddress,String zmqAddress, ZContext context) {
        super(webSocketAddress);
        this.context = context;
        connect(zmqAddress);
    }

    private void connect(String address) {
        socket = context.createSocket(ZMQ.SUB);
        socket.connect(address);
        socket.subscribe(new byte[] {});
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        this.webSocket = webSocket;
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        this.webSocket = null;
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {

    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void start() {
        super.start();
        running = true;
        while (running) {
            String msg = socket.recvStr(ZMQ.NOBLOCK);
            if (webSocket != null && msg != null) {
                System.out.println("zmq -> websocket");
                webSocket.send(msg);
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() throws IOException, InterruptedException {
        super.stop();
        running = false;
    }
}
