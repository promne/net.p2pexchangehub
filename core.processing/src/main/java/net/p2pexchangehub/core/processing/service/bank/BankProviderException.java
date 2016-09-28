package net.p2pexchangehub.core.processing.service.bank;

public class BankProviderException extends Exception {

    private static final long serialVersionUID = -4746078181377371123L;

    public BankProviderException(Throwable cause) {
        super(cause);
    }

    public BankProviderException(String message, Throwable cause) {
        super(message, cause);
    }

}
