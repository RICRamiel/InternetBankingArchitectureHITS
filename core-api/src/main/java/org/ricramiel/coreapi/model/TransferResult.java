package org.ricramiel.coreapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ricramiel.common.enums.TransactionStatus;
import org.ricramiel.coreapi.entity.TransactionOperation;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResult {
    TransactionOperation withdrawalOperation;
    TransactionOperation enrollmentOperation;

    public boolean isSucceded(){
        return withdrawalOperation.getTransactionStatus() == TransactionStatus.COMPLETE;
    }
}
