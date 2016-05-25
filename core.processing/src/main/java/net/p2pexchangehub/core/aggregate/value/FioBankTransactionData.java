package net.p2pexchangehub.core.aggregate.value;

public class FioBankTransactionData implements BankSpecificTransactionData {

    private final String id;
    
    private final String message;
    
    public FioBankTransactionData(String id, String message) {
        super();
        this.id = id;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String getHash() {
        return id;
    }
        
}
