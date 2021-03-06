package bank.sockets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import bank.InactiveException;
import bank.OverdrawException;

public class ServerBank {//implements Bank {
    
    private final Map<String, bank.Account> accounts = new HashMap<>();

    
    public String createAccount(String owner) throws IOException {
        Account a = new Account(owner);
        if (accounts.containsKey(a.getNumber()) ) {
            return null;
        }
        accounts.put(a.getNumber(), a);
        return a.getNumber();
    }

    
    public boolean closeAccount(String number) throws IOException {
        Account account = (Account) accounts.get(number);
        
        if(account == null || account.getBalance() != 0.0 || !account.isActive() ) {
            return false;
        }
        
        account.active = false;
        return true;
    }

    
    public String getAccountNumbers() throws IOException {
        // Java 8 Filter 
        // http://zeroturnaround.com/rebellabs/java-8-explained-applying-lambdas-to-java-collections/
        // *!%&ç*!F***
//        return accounts.entrySet().stream()
//                .filter(account -> account.getValue().isActive() )
//                .map(account -> account.getKey())
//                .reduce("", (x,y) -> x+";"+y);
        String a = "";
        for(String key : accounts.keySet()) {
            if(accounts.get(key).isActive() ) {
                a += accounts.get(key).getNumber()+";";
            }
        }        
        return a;
    }

    
    public Account getAccount(String number) throws IOException {
        return (Account)accounts.get(number);
    }

    
    public void transfer(bank.Account from, bank.Account to, double amount)
            throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
        if( !from.isActive() || !to.isActive()) {
            throw new InactiveException("One of the account is inactive");
        }
        
        // Avoid negative amount
        if(amount < 0.0) amount *= -1;
        
        if( amount > from.getBalance() ) {
            throw new OverdrawException("Not enough money");
        }
       
        from.withdraw(amount);
        to.deposit(amount);

        
    }
    
    class Account implements bank.Account {
        private String number;
        private String owner;
        private double balance;
        private boolean active = true;

        Account(String owner) {
            this.owner = owner;
            // Calculate number
            this.number = Integer.toString(owner.hashCode());
        }

        public String toString() {
            // IMPORTATN! Add owner at the end of the String cause the names are fuzzy!
            return getNumber() + ";" + isActive() + ";" + getBalance()+ ";" + getOwner();
        }
        
        @Override
        public double getBalance() {
            return balance;
        }

        @Override
        public String getOwner() {
            return owner;//.replaceAll("%", ";").replaceAll("*", ":");
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
        public void deposit(double amount) throws InactiveException {
            if( this.active ) {
                
                if(amount < 0.0) amount *= -1;
                
                balance += amount;
                
                return;
            }
            throw new InactiveException("Account is not active");
        }

        @Override
        public void withdraw(double amount) throws InactiveException, OverdrawException {
            
            // Check if account active
            if( !this.active ) {
                throw new InactiveException("Account is not active");
            }
            
            // avoid negative amount
            if(amount < 0.0) amount *= -1;
            
            // Check if account has enough money
            if( amount > this.balance) {
                throw new OverdrawException("You have not enough money!");
            }
            
            // Calculate new balance
            balance -= amount;
           
            return;
            
        }

    }

}
