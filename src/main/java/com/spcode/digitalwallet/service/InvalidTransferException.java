package com.spcode.digitalwallet.service;

public class InvalidTransferException extends RuntimeException{
    public InvalidTransferException(String message){
        super(message);
    }
}
