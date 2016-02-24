package bank.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import bank.InactiveException;
import bank.OverdrawException;


public class Server {
    
    public static void main(String args[]) throws IOException {
        int port = 1234;
        ServerBank bank = new ServerBank();
        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                Socket s = server.accept();
                Thread t = new Thread(new Transaction(s, bank));
                t.start();
            }
        }
    }
    
    private static class Transaction implements Runnable {
        private final Socket s;
        private ServerBank bank;

        private Transaction(Socket s, ServerBank bank) {
            this.s = s;
            this.bank = bank;
        }

        @Override
        public void run() {
            String number;
            String owner;
            Double amount;
            String line;
            
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(s
                        .getInputStream()));
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);

                String input = in.readLine();
                
                switch (Task.valueOf(input)) {
                case HELLO:
                    out.println(Answer.OK);
                    break;

                case GETNUMBERS:
                    out.println(this.bank.getAccountNumbers());
                    break;
                 
                case CREATE_ACCOUNT:
                    owner = in.readLine();
                    out.println(bank.createAccount(owner));
                    break;
                    
                case CLOSE_ACCOUNT:
                    number = in.readLine();
                    out.println(bank.closeAccount(number));
                    break;
                
                case GETACCOUNT:
                    number = in.readLine();
                    out.println(bank.getAccount(number));
                    break;
                    
                case DEPOSIT:
                    line = in.readLine();
                    number = line.split(";")[0];
                    amount = Double.parseDouble(line.split(";")[1]);
                    try {
                        bank.getAccount(number).deposit(amount.doubleValue());
                        out.println(Answer.OK);
                    } catch (InactiveException e) {
                        out.println(Answer.INACTIVE);
                    }
                    break;
                case WITHDRAW:
                    line = in.readLine();
                    number = line.split(";")[0];
                    amount = Double.parseDouble(line.split(";")[1]);
                    try {
                        bank.getAccount(number).withdraw(amount.doubleValue());
                        out.println(Answer.OK);
                    } catch( InactiveException e ) {
                        out.println(Answer.INACTIVE);
                    } catch( OverdrawException e) {
                        out.println(Answer.OVERDRAW);
                    }

                default:
                    out.println(Answer.NOT_FOUND);
                }

            } catch (IOException e) {
                System.err.println(e);
                throw new RuntimeException(e);
            } finally {
                try {
                    s.close();
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
            
        }
    }

}
