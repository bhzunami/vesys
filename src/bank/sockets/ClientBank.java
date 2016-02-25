package bank.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bank.Bank;
import bank.InactiveException;
import bank.OverdrawException;

public class ClientBank implements Bank {
    private String host;
    private int port;
    private final Map<String, bank.Account> accounts = new HashMap<>();
    
    public ClientBank(String host, int port) throws ConnectException {
        this.host = host;
        this.port = port;
        System.out.println("Try to connect to socket: " +this.host +":"+this.port);
        
        // Check connection
        if(!"OK".equals(sendMessage(Task.HELLO)) ) {
            throw new ConnectException("Could not connect to Server.");
        }
    }
    
    
    private String sendMessage(Task task, String... data) {
        try (Socket socket = new Socket(this.host, this.port, null, 0)) {
            
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            out.println(task);
            String more = "";
            if( data.length > 0)
                more = data[0];
            if(data.length > 1) {
                more += ";"+data[1];
            }
            out.println(more);
            out.flush();
            // Read response
            String response = in.readLine();
            return response;
        } catch (IOException e) {
            System.err.println("Error in sending command "+task +"with data: " +data.toString() +" " +e);
            return "IOException";
        }
    }

    @Override
    public String createAccount(String owner) throws IOException {
        // Some people think they have to add special characters
//        owner = owner.replaceAll(";", "%").replaceAll(":", "*:");
        String accountNumber = sendMessage(Task.CREATE_ACCOUNT, owner);
        
        System.out.println("create Account -> Account number: " +accountNumber);
        return accountNumber;
    }

    @Override
    public boolean closeAccount(String number) throws IOException {
        boolean closed = Boolean.valueOf(sendMessage(Task.CLOSE_ACCOUNT, number) );
        System.out.println(closed);
        return closed;
    }

    @Override
    public Set<String> getAccountNumbers() throws IOException {
        String accounts = sendMessage(Task.GETNUMBERS);
        if( accounts.length() < 1) {
            return new HashSet<String>();
        }
        System.out.println("getAccountNumbers -> Accounts: " +accounts);
        List<String> accounts_as_list = Arrays.asList(sendMessage(Task.GETNUMBERS).split(";") );
        for(String s : accounts_as_list) {
            System.out.println(s);
        }
        return new HashSet<String>(accounts_as_list);

    }

    @Override
    public Account getAccount(String number) throws IOException {
        System.out.println("Get Account with number: " +number);
        String account_as_string = sendMessage(Task.GETACCOUNT, number);
        System.out.println("getAccount -> account_as_string: " +account_as_string);
        
        if ("null".equals(account_as_string))
            return null;
        
        // Update
        Account a;
        if(accounts.containsKey(number)) {
            a = (Account) accounts.get(number);
            a.createOrUpdateAccount(account_as_string);
            return a;
        }
        
        // New Account -> Not in hashMap
        a = new Account(account_as_string);
        accounts.put(number, a);
        return a;
    }

    @Override
    public void transfer(bank.Account a, bank.Account b, double amount)
            throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
        String transfer = a.getNumber()+";"+b.getNumber()+";"+amount;
        String response = sendMessage(Task.TRANSFER, transfer);
        switch (Answer.valueOf(response)) {
        case OK:
            // Update Account!
            getAccount(a.getNumber());
            return;
        case INACTIVE:
            throw new InactiveException("Account: " +a.getNumber() +" is inactive.");
        case OVERDRAW:
            throw new OverdrawException("Not enough money");
        case NOT_FOUND:
            throw new IllegalArgumentException("This account ins unknown!");
        default:
            throw new UncheckedIOException("ERROR:" + response, null);
        }
        
    }
    
    class Account implements bank.Account {
        private String number;
        private String owner;
        private double balance;
        private boolean active = true;

        Account(String account) {
            this.createOrUpdateAccount(account);
        }
        
        private void createOrUpdateAccount(String account) {
            System.out.println("Account as String: " +account);
            String[] a = account.split(";", 4);
            this.number = a[0];
            // onwer name are crap
            this.owner = a[3];
            this.active = Boolean.parseBoolean(a[1]);
            this.balance = Double.parseDouble(a[2]);
        }

        @Override
        public double getBalance() {
            return balance;
        }

        @Override
        public String getOwner() {
            return owner;
        }

        @Override
        public String getNumber() {
            return number;
        }

        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public void deposit(double amount) throws InactiveException, IOException {
            String response = sendMessage(Task.DEPOSIT, this.number, String.valueOf(amount));
            switch (response) {
            case "OK":
                // Update Account!
                getAccount(number);
                return;
            case "INACTIVE":
                throw new InactiveException("Account: " +this.number +" is inactive.");
            case "NOTFOUND":
                throw new IllegalArgumentException("This account ins unknown!");
            default:
                throw new UncheckedIOException("ERROR:" + response, null);
            }

        }

        @Override
        public void withdraw(double amount) throws InactiveException, OverdrawException, IOException {
            String response = sendMessage(Task.WITHDRAW, this.number, String.valueOf(amount));
            switch (Answer.valueOf(response)) {
            case OK:
                // Update Account!
                getAccount(number);
                return;
            case INACTIVE:
                throw new InactiveException("Account: " +this.number +" is inactive.");
            case OVERDRAW:
                throw new OverdrawException("Not enough money");
            case NOT_FOUND:
                throw new IllegalArgumentException("This account ins unknown!");
            default:
                throw new UncheckedIOException("ERROR:" + response, null);
            }
            
        }

    }

}
