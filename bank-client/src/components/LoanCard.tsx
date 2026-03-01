import { useState } from "react";
import { CreditCard, CheckCircle2, XCircle } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { Badge } from "./ui/badge";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "./ui/dialog";
import { Progress } from "./ui/progress";
import { useMakeEnrollmentMutation } from "../lib/api/generatedApi";
import { toast } from "sonner";
import { type Loan } from "../lib/types/loan";

interface LoanCardProps {
  loan: Loan,
  accounts: {
    id: string;
    name: string;
    balance: number;
  }[];
  onUpdate: () => void;
}

export function LoanCard({ loan, accounts, onUpdate }: LoanCardProps) {
  const [paymentAmount, setPaymentAmount] = useState("");
  const [showPayment, setShowPayment] = useState(false);
  const [isAmountValid, setIsAmountValid] = useState<boolean | null>(null);
  const [makeEnrollment, { isLoading: isPaying }] = useMakeEnrollmentMutation();

  const account = accounts.find((acc) => acc.id === loan.accountId);
  const isPaid = loan.currentDebt === 0 && loan.interestDebtSum === 0;

  const payProgress = (loan.currentDebt / loan.initialDebt) * 100 

  const validatePaymentAmount = (value: string) => {
    setPaymentAmount(value);
    const num = parseFloat(value);
    if (!value) {
      setIsAmountValid(null);
    } else if (
      isNaN(num) ||
      num <= 0 ||
      num > loan.currentDebt ||
      (account && num > account.balance)
    ) {
      setIsAmountValid(false);
    } else {
      setIsAmountValid(true);
    }
  };

  const handlePayment = async () => {
    const amount = parseFloat(paymentAmount);
    if (isNaN(amount) || amount <= 0) {
      toast.error("Введите корректную сумму");
      return;
    }

    if (amount > loan.currentDebt + loan.interestDebtSum) {
      toast.error("Сумма погашения превышает остаток по кредиту");
      return;
    }

    if (account && amount > account.balance) {
      toast.error("Недостаточно средств на счете");
      return;
    }

    try {
      await makeEnrollment({
        cardAccountId: loan.accountId,
        money: amount,
      }).unwrap();

      toast.success(`Кредит погашен на ${amount.toFixed(2)} ₽`);
      setPaymentAmount("");
      setIsAmountValid(null);
      setShowPayment(false);
      onUpdate();
    } catch {
      toast.error("Не удалось погасить кредит");
    }
  };

  return (
    <>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <CreditCard className="h-5 w-5 text-primary" />
              <CardTitle>Кредит #{loan.id.slice(-6)}</CardTitle>
            </div>
            <Badge variant={isPaid ? "outline" : "default"}>
              {isPaid ? "Погашен" : "Активный"}
            </Badge>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Счет</span>
              <span>{account?.name || "Неизвестный счет"}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">К выплате за этот месяц</span>
              <span className="font-medium">{loan.percentDebt.toFixed(2)} ₽</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Процентная ставка</span>
              <span className="font-medium">{loan.interestRate}%</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Остаток</span>
              <span className="font-bold text-destructive">
                {loan.currentDebt.toFixed(2)} ₽
              </span>
            </div>
          </div>

          <div className="space-y-2">
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Прогресс погашения</span>
              <span>{payProgress.toFixed(0)}%</span>
            </div>
            <Progress value={payProgress} />
          </div>

          {!isPaid && (
            <Button
              size="sm"
              variant="outline"
              className="w-full"
              onClick={() => setShowPayment(true)}
            >
              Погасить кредит
            </Button>
          )}

          <p className="text-xs text-muted-foreground">
            Оформлен: {new Date(loan.createdAt).toLocaleDateString("ru-RU")}
          </p>
        </CardContent>
      </Card>

      {/* Payment Dialog */}
      <Dialog open={showPayment} onOpenChange={setShowPayment}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="text-2xl">Погасить кредит</DialogTitle>
            <DialogDescription>
              {loan.productName} - Остаток #{loan.id.slice(-6)}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="paymentAmount" className="text-base">
                Сумма погашения
              </Label>
              <div className="relative">
                <Input
                  id="paymentAmount"
                  type="number"
                  step="0.01"
                  placeholder="1000"
                  value={paymentAmount}
                  onChange={(e) => validatePaymentAmount(e.target.value)}
                  className={`bg-input h-14 text-lg pr-12 transition-all ${
                    isAmountValid === true
                      ? "border-primary ring-2 ring-primary/20"
                      : isAmountValid === false
                      ? "border-destructive ring-2 ring-destructive/20"
                      : ""
                  }`}
                />
                <div className="absolute right-3 top-1/2 -translate-y-1/2">
                  {isAmountValid === true && (
                    <CheckCircle2 className="h-6 w-6 text-primary" />
                  )}
                  {isAmountValid === false && (
                    <XCircle className="h-6 w-6 text-destructive" />
                  )}
                </div>
              </div>
              {isAmountValid === false && (
                <p className="text-sm text-destructive flex items-center gap-1">
                  <XCircle className="h-4 w-4" />
                  {parseFloat(paymentAmount) > loan.currentDebt + loan.interestDebtSum
                    ? "Сумма превышает остаток по кредиту"
                    : account && parseFloat(paymentAmount) > account.balance
                    ? "Недостаточно средств на счете"
                    : "Введите корректную сумму"}
                </p>
              )}
            </div>
            <div className="space-y-1 text-sm font-mono">
              <p className="text-muted-foreground">
                Остаток по кредиту: {loan.currentDebt.toFixed(2)} ₽
              </p>
              <p className="text-muted-foreground">
                Доступно на счете: {account?.balance.toFixed(2) || 0} ₽
              </p>
            </div>
            <Button
              onClick={handlePayment}
              className="w-full h-12 text-lg"
              disabled={isAmountValid !== true || isPaying}
            >
              Погасить
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
}