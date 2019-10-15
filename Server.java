/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.awt.GridLayout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author FERNANDO
 */
public class Server {

    private static Set<String> usuarios = new HashSet<>();
    private static Hashtable<Integer,String> sala= new Hashtable();
    private static Set<PrintWriter> writers = new HashSet<>();
    private static HashMap<String, PrintWriter> mapa = new HashMap<String, PrintWriter>();

    static int x = 10, y = 10;
    static tablero tablero = new tablero(x, y, 2);
   
    public static void main(String[] args) throws Exception {
        System.out.println("El chat esta corriendo... ");
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try (ServerSocket listener = new ServerSocket(59001)) {
            while (true) {
                pool.execute(new Handler(listener.accept()));

            }
        }

    }

    private static class Handler implements Runnable {

        private String name;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;
        DataOutputStream salida;
        DataInputStream entrada;
        private ObjectOutputStream oos;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            int a=0;
            int room=1;
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);
                oos = new ObjectOutputStream(socket.getOutputStream());
                salida = new DataOutputStream(socket.getOutputStream());
        
                while (true) {
                    System.out.println();
                    out.println("SUBMITNAME");
                    name = in.nextLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (usuarios) {
                        if (!usuarios.contains(name)) {
                            usuarios.add(name);
                            sala.put(room, name);
                            a++;
                            if(a==3)
                            {
                                room++;
                            }
                            break;
                        }
                     
                    }
                }
                out.println("NAMEACCEPTED " + name);
                out.println("Sperando jugadors");
            
                out.println("coor 10" + " 10");

               System.out.println(tablero.getTablero());
                oos.writeObject(tablero.getTablero());
                System.out.println(tablero.getTablero());
                 System.out.println("q loco");
       
                for (PrintWriter writer : writers) {

                    writer.println("MESSAGE " + name + " has joined");
                }
                writers.add(out);

                synchronized (mapa) {
                    if (!mapa.containsKey(name)) {
                        mapa.put(name, out);
                    }
                }
                while (true) {
                    String input = in.nextLine();
                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    } else {
                        for (PrintWriter writer : writers) {
                            writer.println("MESSAGE " + name + ": " + input);
                            System.out.println(name + "dice: " + input);
                            if (input.toLowerCase().startsWith("cor")) {
                                System.out.println("mensajee:" + input.substring(4));
                                String[] parts = input.substring(4).split("-");
                                int part1 = Integer.parseInt(parts[0]); // primera coordenada
                                int part2 = Integer.parseInt(parts[1]); // Segunda
                                System.out.println("1: " + part1);
                                System.out.println("2: " + part2);
                                tablero.esmina(part1, part2);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                if (out != null) {
                    writers.remove(out);
                }
                if (name != null) {
                    System.out.println("El usuario: " + name + " se a ido");
                    usuarios.remove(name);
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + " se a ido");
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }

    }

}
