import { useGetTransactionOperationsQuery, type CardAccount } from "../../../lib/api/generatedApi";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "../../ui/dialog";
import { TransactionHistoryItem } from "../TransactionHistoryItem";

interface TransactionHistoryDialogProps{
    show: boolean,
    setShow: (show: boolean) => void,
    account: CardAccount,
}

export function TransactionHistoryDialog({show, setShow, account} : TransactionHistoryDialogProps){

    console.log(account)

    const {data, isLoading} = useGetTransactionOperationsQuery(
        {accountId: account.id!, pageIndex: 0, pageSize: 50},
        {skip: !show}
    )

    return (
        <Dialog open={show} onOpenChange={setShow}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>История операций</DialogTitle>
            <DialogDescription>
              Все транзакции по счету "{account.id?.substring(0, 4)}..."
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-2">
            {isLoading ? (
              <p className="text-center text-muted-foreground py-8">
                Загрузка истории операций...
              </p>
            ) : data?.content?.length === 0 ? (
              <p className="text-center text-muted-foreground py-8">
                Нет операций по этому счету
              </p>
            ) : (
                <div className="max-h-[70vh] overflow-y-auto space-y-2">
                    {
                    data?.content?.slice().reverse().map((transaction) => (
                      <TransactionHistoryItem transaction={transaction}/>
                    ))}
                </div>
            )}
          </div>
        </DialogContent>
      </Dialog>
    )
}