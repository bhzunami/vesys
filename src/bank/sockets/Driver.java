/*
 * Copyright (c) 2000-2016 Fachhochschule Nordwestschweiz (FHNW)
 * All Rights Reserved. 
 */

package bank.sockets;

import java.net.ConnectException;

import bank.Bank;

public class Driver implements bank.BankDriver {
	private ClientBank bank = null;
	private String hostname;
	private int port;
	
	@Override
	public void connect(String[] args) throws ConnectException {

	    if(args.length < 2) {
	        throw new IllegalArgumentException("Host and port missing");
	    }
	    this.hostname = args[0];
	    this.port = Integer.parseInt(args[1]);
	    
		bank = new ClientBank(hostname, port);
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

}