import java.io.*;
import java.net.*;


public class Client {


    public static void main(String[] args) {
        int port = 4998;
        CommandReader reader = new CommandReader();
        boolean connected = false;
        Status status = Status.NOTLOGINED;
        try {
            if (args.length > 0) port = Integer.valueOf(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Не верный формат порта, введите значение целое число");
            System.exit(1);
        }
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.err.println("Неизвестный хост");
            reader.save = false;
            System.exit(1);
        }
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(10000);
        } catch (SocketException e) {
            System.err.println("Ошибка связанная с сокетами");
            reader.save = false;
            System.exit(1);
        }
        InetAddress finalAddress = address;
        DatagramSocket finalSocket = socket;
        int finalPort = port;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            String a[] = {"logout", "save"};
            for (int i = 0; i < 2; i++) {
                try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                     ObjectOutputStream objectOutputStream = new ObjectOutputStream(out)) {
                    boolean send = true;
                    if(i==1&&!reader.save) send=false;
                    Command command = new Command();
                    command.setName(a[i]);
                    command.setLogin(reader.login);
                    objectOutputStream.writeObject(command);
                    DatagramPacket packet = new DatagramPacket(out.toByteArray(), 0, out.toByteArray().length, finalAddress, finalPort);
                    if (send) finalSocket.send(packet);
                } catch (IOException e) {
                    System.err.println("Ошибка, которая не должна была возникнуть, свяжитесь с разработчиком (код ошибки: 03)");
                    System.exit(1);
                }
            }
        }));
        Command command = new Command();
        command.setName("connect");
        while (true) {
            if (connected && status == Status.LOGINED) {
                System.out.println("Используйте команду help, чтобы получить информацию о командах");
                command = reader.readCommand();
            } else if (connected) {
                command = reader.login(status);
            }
            byte[] a = new byte[8192];
            for (int i = 0; i < 3; i++) {
                try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                     ObjectOutputStream objectOutputStream = new ObjectOutputStream(out)) {
                    objectOutputStream.writeObject(command);
                    DatagramPacket packet = new DatagramPacket(out.toByteArray(), 0, out.toByteArray().length, address, port);
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                DatagramPacket packet1 = new DatagramPacket(a, a.length);
                try {
                    socket.receive(packet1);
                    break;
                } catch (SocketTimeoutException e) {
                    if (i == 0) System.out.println("Сервер не отвечает");
                    System.out.println("Соединение с сервером...");
                } catch (IOException e) {
                    System.err.println("Ошибка, которая не должна была возникнуть, свяжитесь с разработчиком (код ошибки: 02)");
                    reader.save = false;
                    System.exit(1);
                }
            }
            try (ByteArrayInputStream in = new ByteArrayInputStream(a);
                 ObjectInputStream objectInputStream = new ObjectInputStream(in)) {
                Response response = (Response) objectInputStream.readObject();
                status = response.getStatus();
                System.out.println(response.getResponse());
                connected = true;
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Сервер не доступен");
                reader.save = false;
                System.exit(1);
            }
        }

    }

}
