/*
 * Copyright (c) 2000-2016 Fachhochschule Nordwestschweiz (FHNW)
 * All Rights Reserved. 
 */

package bank.local;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import bank.InactiveException;
import bank.OverdrawException;

public class Driver implements bank.BankDriver {
	private Bank bank = null;

	@Override
	public void connect(String[] args) {
		bank = new Bank();
		System.out.println("connected...");
	}

	@Override
	public void disconnect() {
		bank = null;
		System.out.println("disconnected...");
	}

	@Override
	public Bank getBank() {
		return bank;
	}

	static class Bank implements bank.Bank {

		private final Map<String, Account> accounts = new HashMap<>();

		@Override
		public Set<String> getAccountNumbers() {
		    Set<String> activeAccounts = new HashSet<>();
		    for(String key : accounts.keySet()) {
		        if(accounts.get(key).isActive()) {
		            activeAccounts.add(key);
		        }
		    }
		    return activeAccounts;
		}

		@Override
		public String createAccount(String owner) {
		    Account a = new Account(owner);
		    
		    if (accounts.containsKey(a.getNumber()) ) {
		        return null;
		    }
		    
		    accounts.put(a.getNumber(), a);
		    
			return a.getNumber();
		}

		@Override
		public boolean closeAccount(String number) {
		    Account a = accounts.get(number);
		    if(a == null || !a.isActive() ) {
		        // Not founded or inactive account can always be closed
		        return false;
		    }
		    
		    // check if saldiert
		    if( a.getBalance() != 0.0 ) {
		        return false;
		    }
		    a.active = false;
		    
			return true;
		}

		@Override
		public bank.Account getAccount(String number) {
			return accounts.get(number);
		}

		@Override
		public void transfer(bank.Account from, bank.Account to, double amount)
				throws IOException, InactiveException, OverdrawException {
		    
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

	}

	static class Account implements bank.Account {
		private String number;
		private String owner;
		private double balance;
		private boolean active = true;

		Account(String owner) {
			this.owner = owner;
			// Calculate number
			this.number = Integer.toString(owner.hashCode());
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