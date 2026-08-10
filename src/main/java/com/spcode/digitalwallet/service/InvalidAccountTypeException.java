package com.spcode.digitalwallet.service;

public class InvalidAccountTypeException extends RuntimeException{
    public InvalidAccountTypeException(String type){
            super("Invalid account type: " + type + ". Must be one of CUSTOMER, SYSTEM, FEE, ESCROW.");
    }
}
