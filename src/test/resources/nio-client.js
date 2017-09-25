/*global Java*/
var SocketChannel = Java.type("java.nio.channels.SocketChannel");
var Selector = Java.type("java.nio.channels.Selector");
var SelectionKey = Java.type("java.nio.channels.SelectionKey");
var InetSocketAddress = Java.type("java.net.InetSocketAddress");
var ByteBuffer = Java.type("java.nio.ByteBuffer");
var Iterator = Java.type("java.util.Iterator");

var channel = SocketChannel.open();
channel.configureBlocking(false);
var selector = Selector.open();
channel.connect(new InetSocketAddress('127.0.0.1', 8233));
channel.register(selector, SelectionKey['OP_CONNECT']);

while (true) {
    selector.select();
    var ite = selector.selectedKeys().iterator();
    while (ite.hasNext()) {
        var key = ite.next();
        ite.remove();
        if (key.isConnectable()) {
            var client = key.channel();
            if (client.isConnectionPending()) {
                client.finishConnect();
            }
            client.configureBlocking(false);
            client.write(ByteBuffer.wrap("向服务端发送了一条信息".getBytes()));
            client.register(selector, SelectionKey['OP_READ']);
        } else if (key.isReadable()) {
        }
    }
}