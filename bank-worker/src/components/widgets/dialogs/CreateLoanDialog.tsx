import { useMemo, useState, useEffect } from "react";
import { HandCoins, CheckCircle2, XCircle, Loader2 } from "lucide-react";
import { Button } from "../../ui/button";
import { Input } from "../../ui/input";
import { Label } from "../../ui/label";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "../../ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "../../ui/select";
import { Badge } from "../../ui/badge";
import { Skeleton } from "../../ui/skeleton";
import {
  useGetAllCreditRulesQuery,
  useCreateCreditMutation,
  type CreditRule,
} from "../../../lib/api/generatedApi";
import { toast } from "sonner";
import type { LoanProduct } from "../../../lib/types/loanProduct";

interface CreateLoanDialogProps {
  userId: string;
  accounts: {
    id: string;
    name: string;
    balance: number;
  }[];
  onLoanCreated: () => void;
}

export function CreateLoanDialog({
  userId,
  accounts,
  onLoanCreated,
}: CreateLoanDialogProps) {
  const [open, setOpen] = useState(false);
  const [accountId, setAccountId] = useState("");
  const [amount, setAmount] = useState("");
  const [selectedProduct, setSelectedProduct] = useState<LoanProduct | null>(null);
  const [isAmountValid, setIsAmountValid] = useState<boolean | null>(null);
  const { data: creditRules, isLoading: isRulesLoading } = useGetAllCreditRulesQuery(
    undefined,
    { skip: !open }
  );
  const [createCredit, { isLoading: isCreating }] = useCreateCreditMutation();
  useEffect(() => {
    validateAmount();
  }, [amount, selectedProduct]);

  //TODO: add real data from backend
  const loanProducts: LoanProduct[] = useMemo(
    () =>
      (creditRules ?? []).map((rule: CreditRule) => ({
        id: rule.id ?? "",
        name: rule.ruleName ?? "Кредит",
        interestRate: rule.percentage ?? 0,
        // временные значения до появления реальных ограничений на бэкенде
        minAmount: 10000,
        maxAmount: 5000000,
        description: "Кредитный продукт по правилу " + (rule.ruleName ?? ""),
      })),
    [creditRules]
  );

  const validateAmount = () => {
    if (!amount || !selectedProduct) {
      setIsAmountValid(null);
      return;
    }

    const numAmount = parseFloat(amount);
    if (
      isNaN(numAmount) ||
      numAmount < selectedProduct.minAmount ||
      numAmount > selectedProduct.maxAmount
    ) {
      setIsAmountValid(false);
    } else {
      setIsAmountValid(true);
    }
  };

  const handleProductSelect = (productId: string) => {
    const product = loanProducts.find((p) => p.id === productId);
    setSelectedProduct(product || null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!accountId) {
      toast.error("Выберите счет");
      return;
    }

    if (!selectedProduct) {
      toast.error("Выберите кредитный продукт");
      return;
    }

    const loanAmount = parseFloat(amount);
    if (isNaN(loanAmount) || loanAmount <= 0) {
      toast.error("Введите корректную сумму");
      return;
    }

    if (loanAmount < selectedProduct.minAmount) {
      toast.error(`Минимальная сумма: ${selectedProduct.minAmount.toLocaleString("ru-RU")} ₽`);
      return;
    }

    if (loanAmount > selectedProduct.maxAmount) {
      toast.error(`Максимальная сумма: ${selectedProduct.maxAmount.toLocaleString("ru-RU")} ₽`);
      return;
    }

    try {
      await createCredit({
        creditCreateModelDto: {
          userId,
          cardAccount: accountId,
          totalDebt: loanAmount,
          creditRuleId: selectedProduct.id,
        },
      }).unwrap();

      toast.success("Кредит успешно оформлен");
      setAmount("");
      setAccountId("");
      setSelectedProduct(null);
      setIsAmountValid(null);
      setOpen(false);
      onLoanCreated();
    } catch {
      toast.error("Не удалось оформить кредит");
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="outline" className="h-12">
          <HandCoins className="h-5 w-5 mr-2" />
          Взять кредит
        </Button>
      </DialogTrigger>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="text-2xl">Оформить кредит</DialogTitle>
          <DialogDescription className="text-base">
            Выберите кредитный продукт и укажите сумму
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Loan Products Selection */}
          <div className="space-y-3">
            <Label className="text-base">Кредитный продукт</Label>
            {isRulesLoading ? (
              <div className="space-y-3">
                {[1, 2, 3].map((i) => (
                  <Skeleton key={i} className="h-24 w-full" />
                ))}
              </div>
            ) : (
              <div className="space-y-3">
                {loanProducts.map((product) => (
                  <button
                    key={product.id}
                    type="button"
                    onClick={() => handleProductSelect(product.id)}
                    className={`w-full p-4 border-2 rounded-lg text-left transition-all ${
                      selectedProduct?.id === product.id
                        ? "border-primary bg-primary/10"
                        : "border-border hover:border-primary/50"
                    }`}
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-1">
                          <h4 className="font-bold text-lg">{product.name}</h4>
                          <Badge variant="outline" className="text-primary">
                            {product.interestRate}%
                          </Badge>
                        </div>
                        <p className="text-sm text-muted-foreground mb-2">
                          {product.description}
                        </p>
                        <p className="text-xs text-muted-foreground font-mono">
                          От {product.minAmount.toLocaleString("ru-RU")} до{" "}
                          {product.maxAmount.toLocaleString("ru-RU")} ₽
                        </p>
                      </div>
                      {selectedProduct?.id === product.id && (
                        <CheckCircle2 className="h-5 w-5 text-primary flex-shrink-0" />
                      )}
                    </div>
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Account Selection */}
          <div className="space-y-2">
            <Label htmlFor="loanAccount" className="text-base">
              Счет для зачисления
            </Label>
            <Select value={accountId} onValueChange={setAccountId}>
              <SelectTrigger id="loanAccount" className="bg-input h-12 text-base">
                <SelectValue placeholder="Выберите счет" />
              </SelectTrigger>
              <SelectContent>
                {accounts.map((account) => (
                  <SelectItem key={account.id} value={account.id}>
                    <div className="flex items-center justify-between gap-4">
                      <span>{account.name}</span>
                      <span className="text-muted-foreground font-mono text-sm">
                        {account.balance.toFixed(2)} ₽
                      </span>
                    </div>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {/* Amount Input with Validation */}
          <div className="space-y-2">
            <Label htmlFor="loanAmount" className="text-base">
              Сумма кредита
            </Label>
            <div className="relative">
              <Input
                id="loanAmount"
                type="number"
                step="0.01"
                placeholder={
                  selectedProduct
                    ? `От ${selectedProduct.minAmount.toLocaleString("ru-RU")} ₽`
                    : "Сначала выберите продукт"
                }
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                disabled={!selectedProduct}
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

            {selectedProduct && amount && (
              <div className="text-sm space-y-1">
                {isAmountValid === false && (
                  <p className="text-destructive flex items-center gap-1">
                    <XCircle className="h-4 w-4" />
                    Сумма должна быть от {selectedProduct.minAmount.toLocaleString("ru-RU")} до{" "}
                    {selectedProduct.maxAmount.toLocaleString("ru-RU")} ₽
                  </p>
                )}
                {isAmountValid === true && (
                  <div className="space-y-1 text-muted-foreground">
                    <p className="flex items-center gap-1">
                      <CheckCircle2 className="h-4 w-4 text-primary" />
                      Сумма корректна
                    </p>
                    <p className="font-mono">
                      Ежемесячный платеж: ~
                      {(
                        (parseFloat(amount) *
                          (1 + selectedProduct.interestRate / 100)) /
                        12
                      ).toFixed(2)}{" "}
                      ₽
                    </p>
                  </div>
                )}
              </div>
            )}
          </div>

          <Button
            type="submit"
            className="w-full h-14 text-lg"
            disabled={
              accounts.length === 0 ||
              !selectedProduct ||
              !isAmountValid ||
              isRulesLoading ||
              isCreating
            }
          >
            {isRulesLoading || isCreating ? (
              <>
                <Loader2 className="h-5 w-5 mr-2 animate-spin" />
                Загрузка...
              </>
            ) : accounts.length === 0 ? (
              "Сначала создайте счет"
            ) : !selectedProduct ? (
              "Выберите кредитный продукт"
            ) : (
              "Оформить кредит"
            )}
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  );
}
