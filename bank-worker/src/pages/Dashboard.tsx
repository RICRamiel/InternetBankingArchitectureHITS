import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import { Wallet, CreditCard, UserIcon, PlusIcon, Plus } from "lucide-react";
import { Header } from "../components/Header";
import { AccountCard } from "../components/widgets/cards/AccountCard";
import { LoanCard } from "../components/widgets/cards/LoanCard";
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
  useRevokeMutation,
  generatedApi,
  useGetAllUsersQuery,
  type UserDto,
  useGetAllCreditRulesQuery,
} from "../lib/api/generatedApi";
import { toast } from "sonner";
import { clearTokens, getRefreshToken } from "../lib/auth/tokenStorage";
import { useDispatch } from "react-redux";
import { UserDetailsDialog } from "../components/widgets/dialogs/UserDetailsDialog";
import { Card, CardContent, CardHeader, CardTitle } from "../components/ui/card";
import { UserCard } from "../components/widgets/cards/UserCard";
import { LoanProductCard } from "../components/widgets/cards/LoanProductCard";
import { Button } from "../components/ui/button";
import { CreateLoanProductDialog } from "../components/widgets/dialogs/CreateLoanProductDialog";

export function Dashboard() {
  const navigate = useNavigate();
  
  const { data: user, isLoading: isUserLoading, isSuccess, isError, error } = useGetUserQuery();
  const {data: userList, isLoading: isUserListLoading} = useGetAllUsersQuery();
  const {data: loanProducs, isLoading: isLoanProducsLoading, refetch: refetchProducs} = useGetAllCreditRulesQuery();

  const [showCreateLoanProduct, setShowCreateLoanProduct] = useState(false);

  const userId = user?.id;
  const isUserWorker = user?.roles?.includes("WORKER")

  console.log(user)
  console.log(isUserWorker)

  const {
    data: accountsPage,
    isLoading: isAccountsLoading,
    refetch: refetchAccounts,
  } = useGetUserCardAccountsQuery(
    { userId: userId ?? "", pageIndex: 0, pageSize: 50 }
  );

  const {
    data: credits,
    isLoading: isCreditsLoading,
    refetch: refetchCredits,
  } = useGetByUserIdQuery(
    { userId: userId ?? "" },
    { skip: !userId }
  );

  const [revoke] = useRevokeMutation();
  const dispatch = useDispatch();

  const logOut = async () => {
    const refreshToken = getRefreshToken();
    if (refreshToken) {
      try {
        await revoke({ tokenRefreshModelDto: { value: refreshToken } }).unwrap();
      } catch {
        // игнорируем ошибку отзыва токена при логауте
      }
    }
    clearTokens();
    dispatch(generatedApi.util.resetApiState());
    navigate("/");
  }

  useEffect(() => {
    if (user && !user?.roles?.includes("WORKER")) {
      toast.error("Вы не рабочий");
      logOut();
      return;
    }
  }, [user, isSuccess, isError, error, isUserLoading, navigate]);

  const users : UserDto[] = useMemo(() => userList ?? [], [userList])

  const handleAccountsUpdate = () => {
    void refetchAccounts();
  };

  const handleLoansUpdate = () => {
    void refetchCredits();
  };

  if (isUserLoading || isAccountsLoading || isCreditsLoading) {
    return null;
  }

  return (
    <div className="dark min-h-screen bg-background pr-[calc(100vw-100%)]">
      <Header user={user!} />
      <main className="container mx-auto px-4 py-8 max-w-7xl">
        <div className="mb-10">
          <h1 className="text-5xl mb-3 font-bold tracking-tight text-primary">Панель администрации</h1>
        </div>

        <Tabs defaultValue="users" className="space-y-6">
          <TabsList>
            <TabsTrigger value="users">Клиенты</TabsTrigger>
            <TabsTrigger value="loanProducts">Кредитные предложения</TabsTrigger>
          </TabsList>

          <TabsContent value="users" className="space-y-4">
            
            {users.length === 0 ? (
              <div className="bg-card border border-border rounded-lg p-12 text-center">
                {/* <Wallet className="h-12 w-12 text-muted-foreground mx-auto mb-4" /> */}
                <h3 className="mb-2 text-primary">Список пользователей пуст</h3>
                <p className="text-muted-foreground mb-4">
                  Как вы вообще тут находитесь?
                </p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {users.map((user: UserDto) => (
                  <UserCard user={user} key={user.id} refetch={() => {refetchAccounts()}}/>
                )) }
              </div>
            )}
          </TabsContent>

          <TabsContent value="loanProducts" className="space-y-4">
            <div className="w-full flex justify-end">
              <Button 
              variant={"outline"} 
              className="text-primary"
              onClick={() => {setShowCreateLoanProduct(true)}}
              >
                <PlusIcon/>
                Новый продукт
              </Button>

              <CreateLoanProductDialog 
              show={showCreateLoanProduct} 
              setShow={setShowCreateLoanProduct}
              />

            </div>
            {
              isLoanProducsLoading &&
              <div className="bg-card border border-border rounded-lg p-12 text-center">
                <h3 className="mb-2 text-primary">Получаем список кредитных продуктов</h3>
                <p className="text-muted-foreground mb-4">
                  Нужно подождать...
                </p>
              </div>
            }
            {
              loanProducs?.length === 0 ? (
                <div className="bg-card border border-border rounded-lg p-12 text-center">
                <h3 className="mb-2 text-primary">Нет кредитных продуктов</h3>
                <p className="text-muted-foreground mb-4">
                  Справа сверху кнопка для создания
                </p>
              </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {loanProducs?.map((product) => (
                    <LoanProductCard loanProduct={product} key={product.id}/>
                  ))}
                </div>
              )
            }
          </TabsContent>
        </Tabs>
      </main>
    </div>
  );
}