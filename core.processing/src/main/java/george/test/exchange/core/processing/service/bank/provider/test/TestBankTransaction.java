package george.test.exchange.core.processing.service.bank.provider.test;

import javax.persistence.Entity;
import javax.persistence.Table;

import george.test.exchange.core.domain.entity.bank.ExternalBankTransaction;

@Entity
@Table
public class TestBankTransaction extends ExternalBankTransaction {

    private String tbDetail;
    
    private String tbId;

    public String getTbDetail() {
        return tbDetail;
    }

    public void setTbDetail(String testBankData) {
        this.tbDetail = testBankData;
    }

    public String getTbId() {
        return tbId;
    }

    public void setTbId(String tbId) {
        this.tbId = tbId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((tbDetail == null) ? 0 : tbDetail.hashCode());
        result = prime * result + ((tbId == null) ? 0 : tbId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TestBankTransaction other = (TestBankTransaction) obj;
        if (tbDetail == null) {
            if (other.tbDetail != null) {
                return false;
            }
        } else if (!tbDetail.equals(other.tbDetail)) {
            return false;
        }
        if (tbId == null) {
            if (other.tbId != null) {
                return false;
            }
        } else if (!tbId.equals(other.tbId)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matchesReferenceId(String referenceId) {
        return tbDetail!=null && tbDetail.contains(referenceId);
    }
    
}
