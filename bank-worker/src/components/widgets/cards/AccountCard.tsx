import { useState } from "react";
import { Wallet, TrendingUp, TrendingDown, Trash2, History, CheckCircle2, XCircle } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "../../ui/card";
import { Button } from "../../ui/button";
import { Input } from "../../ui/input";
import { Label } from "../../ui/label";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "../../ui/dialog";
import {
  useCloseAccountMutation,
  useEnrollMoneyMutation,
  useWithdrawMoneyMutation,
  useGetTransactionOperationsQuery,
  type TransactionOperation,
} from "../../../lib/api/generatedApi";
import { toast } from "sonner";
import { TransactionHistoryDialog } from "../dialogs/TransactionHistoryDialog";

interface AccountCardProps {
  account: {
    id: string;
    name: string;
    balance: number;
  };
  onUpdate: () => void;
}

export function AccountCard({ account, onUpdate }: AccountCardProps) {
  const [depositAmount, setDepositAmount] = useState("");
  const [withdrawAmount, setWithdrawAmount] = useState("");
  const [showDeposit, setShowDeposit] = useState(false);
  const [showWithdraw, setShowWithdraw] = useState(false);
  const [showHistory, setShowHistory] = useState(false);
  const [isDepositValid, setIsDepositValid] = useState<boolean | null>(null);
  const [isWithdrawValid, setIsWithdrawValid] = useState<boolean | null>(null);
  const [enroll, { isLoading: isEnrollLoading }] = useEnrollMoneyMutation();
  const [withdrawMoney, { isLoading: isWithdrawLoading }] = useWithdrawMoneyMutation();
  const [closeAccount, { isLoading: isCloseLoading }] = useCloseAccountMutation();
  const {
    data: transactionsPage,
    isLoading: isHistoryLoading,
  } = useGetTransactionOperationsQuery(
    { accountId: account.id, pageIndex: 0, pageSize: 50 },
    { skip: !showHistory }
  );

  const transactions: TransactionOperation[] = transactionsPage?.content ?? [];

  const validateDepositAmount = (value: string) => {
    setDepositAmount(value);
    const num = parseFloat(value);
    if (!value) {
      setIsDepositValid(null);
    } else if (isNaN(num) || num <= 0) {
      setIsDepositValid(false);
    } else {
      setIsDepositValid(true);
    }
  };

  const validateWithdrawAmount = (value: string) => {
    setWithdrawAmount(value);
    const num = parseFloat(value);
    if (!value) {
      setIsWithdrawValid(null);
    } else if (isNaN(num) || num <= 0 || num > account.balance) {
      setIsWithdrawValid(false);
    } else {
      setIsWithdrawValid(true);
    }
  };

  const handleDeposit = () => {
    const amount = parseFloat(depositAmount);
    if (isNaN(amount) || amount <= 0) {
      toast.error("Введите корректную сумму");
      return;
    }

    enroll({ enrollDto: { cardAccountId: account.id, sum: amount } })
      .unwrap()
      .then(() => {
        toast.success(`Счет пополнен на ${amount.toFixed(2)} ₽`);
        setDepositAmount("");
        setIsDepositValid(null);
        setShowDeposit(false);
        onUpdate();
      })
      .catch(() => {
        toast.error("Не удалось пополнить счет");
      });
  };

  const handleWithdraw = () => {
    const amount = parseFloat(withdrawAmount);
    if (isNaN(amount) || amount <= 0) {
      toast.error("Введите корректную сумму");
      return;
    }

    withdrawMoney({ withdrawDto: { cardAccountId: account.id, sum: amount } })
      .unwrap()
      .then(() => {
        toast.success(`Со счета снято ${amount.toFixed(2)} ₽`);
        setWithdrawAmount("");
        setIsWithdrawValid(null);
        setShowWithdraw(false);
        onUpdate();
      })
      .catch(() => {
        toast.error("Недостаточно средств на счете или ошибка операции");
      });
  };

  const handleDelete = () => {
    if (account.balance > 0) {
      toast.error("Невозможно закрыть счет с положительным балансом");
      return;
    }

    if (confirm(`Вы уверены, что хотите закрыть счет "${account.name}"?`)) {
      closeAccount({ accountId: account.id })
        .unwrap()
        .then(() => {
          toast.success("Счет успешно закрыт");
          onUpdate();
        })
        .catch(() => {
          toast.error("Не удалось закрыть счет");
        });
    }
  };

  return (
    <>
      <Card>
        <CardHeader>
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-2">
              <Wallet className="h-5 w-5 text-primary" />
              <CardTitle>{account.name}</CardTitle>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={handleDelete}
                disabled={isCloseLoading}
              className="hover:text-destructive"
            >
              <Trash2 className="h-4 w-4" />
            </Button>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div>
            <p className="text-sm text-muted-foreground">Баланс</p>
            <p className="text-2xl font-bold text-primary">
              {account.balance.toFixed(2)} ₽
            </p>
          </div>
          <div className="flex flex-wrap gap-2">
            <Button size="sm" onClick={() => setShowDeposit(true)}>
              <TrendingUp className="h-4 w-4 mr-2" />
              Внести
            </Button>
            <Button
              size="sm"
              variant="outline"
              onClick={() => setShowWithdraw(true)}
            >
              <TrendingDown className="h-4 w-4 mr-2" />
              Снять
            </Button>
            <Button size="sm" variant="ghost" onClick={() => setShowHistory(true)}>
              <History className="h-4 w-4 mr-2" />
              История
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Deposit Dialog */}
      <Dialog open={showDeposit} onOpenChange={setShowDeposit}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="text-2xl">Пополнить счет</DialogTitle>
            <DialogDescription>
              Введите сумму для пополнения счета "{account.name}"
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="depositAmount" className="text-base">Сумма</Label>
              <div className="relative">
                <Input
                  id="depositAmount"
                  type="number"
                  step="0.01"
                  placeholder="1000"
                  value={depositAmount}
                  onChange={(e) => validateDepositAmount(e.target.value)}
                  className={`bg-input h-14 text-lg pr-12 transition-all ${
                    isDepositValid === true
                      ? "border-primary ring-2 ring-primary/20"
                      : isDepositValid === false
                      ? "border-destructive ring-2 ring-destructive/20"
                      : ""
                  }`}
                />
                <div className="absolute right-3 top-1/2 -translate-y-1/2">
                  {isDepositValid === true && (
                    <CheckCircle2 className="h-6 w-6 text-primary" />
                  )}
                  {isDepositValid === false && (
                    <XCircle className="h-6 w-6 text-destructive" />
                  )}
                </div>
              </div>
              {isDepositValid === false && (
                <p className="text-sm text-destructive flex items-center gap-1">
                  <XCircle className="h-4 w-4" />
                  Введите корректную сумму (больше 0)
                </p>
              )}
            </div>
            <Button
              onClick={handleDeposit}
              className="w-full h-12 text-lg"
              disabled={isDepositValid !== true || isEnrollLoading}
            >
              Пополнить
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* Withdraw Dialog */}
      <Dialog open={showWithdraw} onOpenChange={setShowWithdraw}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="text-2xl">Снять средства</DialogTitle>
            <DialogDescription>
              Введите сумму для снятия со счета "{account.name}"
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="withdrawAmount" className="text-base">Сумма</Label>
              <div className="relative">
                <Input
                  id="withdrawAmount"
                  type="number"
                  step="0.01"
                  placeholder="1000"
                  value={withdrawAmount}
                  onChange={(e) => validateWithdrawAmount(e.target.value)}
                  className={`bg-input h-14 text-lg pr-12 transition-all ${
                    isWithdrawValid === true
                      ? "border-primary ring-2 ring-primary/20"
                      : isWithdrawValid === false
                      ? "border-destructive ring-2 ring-destructive/20"
                      : ""
                  }`}
                />
                <div className="absolute right-3 top-1/2 -translate-y-1/2">
                  {isWithdrawValid === true && (
                    <CheckCircle2 className="h-6 w-6 text-primary" />
                  )}
                  {isWithdrawValid === false && (
                    <XCircle className="h-6 w-6 text-destructive" />
                  )}
                </div>
              </div>
              {isWithdrawValid === false && (
                <p className="text-sm text-destructive flex items-center gap-1">
                  <XCircle className="h-4 w-4" />
                  {parseFloat(withdrawAmount) > account.balance
                    ? "Недостаточно средств на счете"
                    : "Введите корректную сумму (больше 0)"}
                </p>
              )}
            </div>
            <p className="text-sm text-muted-foreground font-mono">
              Доступно: {account.balance.toFixed(2)} ₽
            </p>
            <Button
              onClick={handleWithdraw}
              className="w-full h-12 text-lg"
              disabled={isWithdrawValid !== true || isWithdrawLoading}
            >
              Снять
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* Transaction History Dialog */}
      <TransactionHistoryDialog show={showHistory} setShow={setShowHistory} account={account}/>
    </>
  );
}