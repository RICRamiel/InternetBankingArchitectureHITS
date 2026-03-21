import { useEffect, useMemo } from "react";
import { useNavigate } from "react-router";
import { Wallet, CreditCard } from "lucide-react";
import { Header } from "../components/Header";
import { AccountCard } from "../components/AccountCard";
import { LoanCard } from "../components/LoanCard";
import { CreateAccountDialog } from "../components/CreateAccountDialog";
import { CreateLoanDialog } from "../components/CreateLoanDialog";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "../components/ui/tabs";
import { type Loan } from "../lib/types/loan";

type Account = {
  id: string;
  userId: string;
  name: string;
  balance: number;
  createdAt: string;
  deleted: boolean;
};

import {
  useGetUserQuery,
  useGetUserCardAccountsQuery,
  useGetByUserIdQuery,
} from "../lib/api/generatedApi";

export function Dashboard() {
  const navigate = useNavigate();
  const { data: user, isLoading: isUserLoading, isSuccess, isError, error} = useGetUserQuery();
  const userId = user?.id;
  const {
    data: accountsPage,
    isLoading: isAccountsLoading,
    refetch: refetchAccounts,
  } = useGetUserCardAccountsQuery(
    { userId: userId ?? "", pageIndex: 0, pageSize: 50 },
    { skip: !userId }
  );
  const {
    data: credits,
    isLoading: isCreditsLoading,
    refetch: refetchCredits,
  } = useGetByUserIdQuery(
    { userId: userId ?? "" },
    { skip: !userId }
  );

  console.log("-----------DASBOARD LOADED--------------")

  useEffect(() => {
    console.log(isUserLoading, isSuccess, isError);
    console.log(error);
    // if ((isSuccess || isError) && !user) {
    //   console.log("WTF")
    //   navigate("/");
    //   return;
    // }
  }, [user, isSuccess, isError, error, isUserLoading, navigate]);

  const accounts: Account[] = useMemo(
    () =>
      accountsPage?.content?.map((acc) => ({
        id: acc.id ?? "",
        userId: acc.userId ?? userId ?? "",
        name: `Счет ${acc.id?.slice(-4) ?? ""}`,
        balance: acc.money ?? 0,
        createdAt: "",
        deleted: acc.deleted ?? false
      })) ?? [],
    [accountsPage, userId]
  );

  const handleAccountsUpdate = () => {
    void refetchAccounts();
  };

  const handleLoansUpdate = () => {
    void refetchCredits();
  };

  if (isUserLoading || isAccountsLoading || isCreditsLoading) {
    return null;
  }

  const loans: Loan[] =
    credits?.map((credit) => ({
      id: credit.id ?? "",
      userId: credit.userId ?? userId ?? "",
      accountId: credit.cardAccount ?? "",
      percentDebt: credit.creditRule?.percentage ?? 0,
      currentDebt: credit.currentDebtSum ?? 0,
      initialDebt: credit.initialDebt ?? 0,
      interestDebtSum: credit.interestDebtSum ?? 0,
      interestRate: credit.creditRule?.percentage ?? 0,
      productName: credit.creditRule?.ruleName ?? "Кредит",
      createdAt: credit.creditRule?.openingDate ?? "",
    })) ?? [];

  const totalBalance = accounts.reduce((sum, acc) => sum + acc.balance, 0);
  const totalLoans = loans.reduce((sum, loan) => sum + loan.currentDebt, 0);

  return (
    <div className="dark min-h-screen bg-background">
      <Header user={user!}/>
      <main className="container mx-auto px-4 py-8 max-w-7xl">
        <div className="mb-10">
          <h1 className="text-5xl mb-3 font-bold tracking-tight text-primary">Личный кабинет</h1>
          <p className="text-lg text-muted-foreground">
            Управляйте своими счетами и кредитами
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-10">
          <div className="bg-gradient-to-br from-primary/20 to-primary/5 border-2 border-primary/30 rounded-xl p-8 shadow-lg">
            <div className="flex items-center gap-3 text-primary mb-3">
              <Wallet className="h-6 w-6" />
              <h3 className="text-xl">Общий баланс</h3>
            </div>
            <p className="text-5xl font-bold mb-2 font-mono text-primary">{totalBalance.toFixed(2)} ₽</p>
            <p className="text-base text-muted-foreground">
              На {accounts.length} {accounts.length === 1 ? "счете" : "счетах"}
            </p>
          </div>

          <div className="bg-gradient-to-br from-destructive/20 to-destructive/5 border-2 border-destructive/30 rounded-xl p-8 shadow-lg">
            <div className="flex items-center gap-3 text-destructive mb-3">
              <CreditCard className="h-6 w-6" />
              <h3 className="text-xl">Задолженность</h3>
            </div>
            <p className="text-5xl font-bold mb-2 font-mono text-destructive">{totalLoans.toFixed(2)} ₽</p>
            <p className="text-base text-muted-foreground">
              По {loans.filter((l) => l.currentDebt > 0).length}{" "}
              {loans.filter((l) => l.currentDebt > 0).length === 1
                ? "кредиту"
                : "кредитам"}
            </p>
          </div>
        </div>

        <Tabs defaultValue="accounts" className="space-y-6">
          <TabsList>
            <TabsTrigger value="accounts">Счета</TabsTrigger>
            <TabsTrigger value="loans">Кредиты</TabsTrigger>
          </TabsList>

          <TabsContent value="accounts" className="space-y-4">
            <div className="flex justify-between items-center">
              <h2>Мои счета</h2>
              {userId && (
                <CreateAccountDialog
                  userId={userId}
                  onAccountCreated={handleAccountsUpdate}
                />
              )}
            </div>

            {accounts.length === 0 ? (
              <div className="bg-card border border-border rounded-lg p-12 text-center">
                <Wallet className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="mb-2">У вас нет открытых счетов</h3>
                <p className="text-muted-foreground mb-4">
                  Создайте первый счет для начала работы с банком
                </p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {accounts.map((account) => (
                  !account.deleted &&
                  <AccountCard
                    key={account.id}
                    account={account}
                    onUpdate={handleAccountsUpdate}
                  />
                ))}
              </div>
            )}
          </TabsContent>

          <TabsContent value="loans" className="space-y-4">
            <div className="flex justify-between items-center">
              <h2>Мои кредиты</h2>
              <CreateLoanDialog
                userId={userId ?? ""}
                accounts={accounts}
                onLoanCreated={handleLoansUpdate}
              />
            </div>

            {loans.length === 0 ? (
              <div className="bg-card border border-border rounded-lg p-12 text-center">
                <CreditCard className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="mb-2">У вас нет кредитов</h3>
                <p className="text-muted-foreground mb-4">
                  Оформите кредит для получения дополнительных средств
                </p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {loans.map((loan) => (
                  <LoanCard
                    key={loan.id}
                    loan={loan}
                    accounts={accounts}
                    onUpdate={handleLoansUpdate}
                  />
                ))}
              </div>
            )}
          </TabsContent>
        </Tabs>
      </main>
    </div>
  );
}