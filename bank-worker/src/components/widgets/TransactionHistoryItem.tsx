import { TrendingDown, TrendingUp } from "lucide-react";
import type { TransactionOperation } from "../../lib/api/generatedApi";
import { Badge } from "../ui/badge";

interface TransactionHistioryItemProps{
    transaction: TransactionOperation
}

export function TransactionHistoryItem({transaction} : TransactionHistioryItemProps){

    const getTransactionIcon = (type?: TransactionOperation["transactionType"]) => {
        switch (type) {
          case "ENROLLMENT":
            return <TrendingUp className="h-4 w-4 text-primary" />;
          case "WITHDRAWAL":
            return <TrendingDown className="h-4 w-4 text-destructive" />;
        }
      };
    
      const getTransactionLabel = (type?: TransactionOperation["transactionType"]) => {
        switch (type) {
          case "ENROLLMENT":
            return "Пополнение";
          case "WITHDRAWAL":
            return "Снятие";
        }
      };

    return (
        <div
                  key={transaction.id}
                  className="flex items-center justify-between p-3 border border-border rounded-lg"
                >
                  <div className="flex items-center gap-3">
                    {getTransactionIcon(transaction.transactionType)}
                    <div>
                      <p className="font-medium">
                        {getTransactionLabel(transaction.transactionType)}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        {transaction.dateTime
                          ? new Date(transaction.dateTime).toLocaleString("ru-RU")
                          : ""}
                      </p>
                    </div>
                  </div>
                  <div className="text-right">
                    <Badge variant="outline">
                      {getTransactionLabel(transaction.transactionType)}
                    </Badge>
                    <p
                      className={`font-bold ${
                        transaction.transactionType === "ENROLLMENT"
                          ? "text-primary"
                          : "text-destructive"
                      }`}
                    >
                      {transaction.transactionType === "ENROLLMENT" ? "+" : "-"}
                      {(transaction.money ?? 0).toFixed(2)} ₽
                    </p>
                  </div>
                </div>
    )
}