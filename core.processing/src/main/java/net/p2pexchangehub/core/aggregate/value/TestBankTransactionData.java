package net.p2pexchangehub.core.aggregate.value;

public class TestBankTransactionData implements BankSpecificTransactionData {

    private final String id;

    private final String detail;

    public TestBankTransactionData(String id, String detail) {
        super();
        this.id = id;
        this.detail = detail;
    }

    public String getId() {
        return id;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public String getHash() {
        return id;
    }
        
}
