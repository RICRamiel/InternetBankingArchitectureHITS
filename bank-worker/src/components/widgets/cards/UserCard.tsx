import { Eye, UserIcon } from "lucide-react";
import { useDeleteUserByIdMutation, useEditUserMutation, useGetTransactionOperationsQuery, useGetUserCardAccountQuery, useGetUserCardAccountsQuery, type CardAccount, type UserDto, type UserEditModelDto } from "../../../lib/api/generatedApi";
import { Card, CardContent, CardHeader, CardTitle } from "../../ui/card";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "../../ui/dialog";
import { use, useState } from "react";
import { Button } from "../../ui/button";
import { TransactionHistoryDialog } from "../dialogs/TransactionHistoryDialog";
import { toast } from "sonner";

interface UserCardProps {
    user: UserDto,
    refetch: () => void
}

export function UserCard({ user, refetch }: UserCardProps) {
    console.log(user)
    const [showDialog, setShowDialog] = useState(false);
    const [isWorker, setWorkerRole] = useState(user.roles?.includes("WORKER"))
    const {
        data: userAccounts,
        isLoading: isAccountsLoading
    } = useGetUserCardAccountsQuery(
        { userId: user.id },
        { skip: !showDialog && user.roles?.includes("CLIENT") }
    );

    const [history, setHistory] = useState<{ show: boolean, selected: CardAccount | null }>({
        show: false,
        selected: null
    });

    const [edit, { isLoading: isEditLoading }] = useEditUserMutation()
    const [deleteUser, {isLoading : isDeletingLoading}] = useDeleteUserByIdMutation();

    const getUserRoles = (roles: typeof user.roles) => {
        return roles?.map((it) => {switch(it){
            case "CLIENT": return "клиент"
            case "WORKER": return "рабочий"
            case "BLOCKED_CLIENT": return "заблокированный клиент"
            case "BLOCKED_WORKER": return "заблокированный рабочий"
        }}).join(', ')
    }

    const toggleUserWorkerRole = async () => {
        const editModel: UserEditModelDto = {
            newRoles: isWorker ? ["CLIENT"] : ["CLIENT", "WORKER"]
        }

        try {
            const res = await edit({ userEditModelDto: editModel, id: user.id })
            toast.success(`Пользователь ${user.name} теперь ${isWorker ? "не" : ""} рабочий!`)
            setWorkerRole((old) => !old);
            setShowDialog(false);
            refetch();

        } catch (error) {
            toast.error(`Ошибка ${error}`)
        }

    }

    const handleDeleteUser = async (user: UserDto) => {
        try{

            await deleteUser({id: user.id})
            toast.success(`Пользователь ${user.name} заблокирован`);
            setShowDialog(false);
            refetch();
        } catch (error) {
            toast.error(`Ошиба при блокировки: ${error}`)
        }
    }

    const handleAccountSelection = (account?: CardAccount) => {
        if (account) {
            setHistory({
                show: true,
                selected: account
            })
        }
        else {
            setHistory({
                show: false,
                selected: null
            })
        }
    }

    const resetSelection = () => {
        handleAccountSelection()
    }

    return (
        <>
            <Card className="py-4 px-2 flex flex-col justify-between align-top">
                <CardHeader className="flex items-center space-x-1">

                    <UserIcon size={50} className={user.active ? "text-primary" : "text-red-700"} />

                    <div className="column">
                        <CardTitle>
                            {user.name} 
                            {!user.active && <span className="text-red-700"> [ЗАБЛОКИРОВАН]</span>}
                            </CardTitle>
                        <p className="text-muted-foreground">{user.email}</p>
                    </div>

                </CardHeader>

                <CardContent>

                    <p className="pb-4 text-primary">Роли: {getUserRoles(user.roles)}</p>

                    <Button
                        variant='outline'
                        onClick={() => { setShowDialog(true); }}
                        className="w-full"
                    >
                        {/* <Eye className="text-primary"/> */}
                        Подробнее
                    </Button>
                </CardContent>

            </Card>
            {/* User Details dialog */}
            <Dialog open={showDialog} onOpenChange={setShowDialog}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>{user.name}</DialogTitle>
                        <DialogDescription>{user.email}</DialogDescription>
                    </DialogHeader>
                    {isAccountsLoading &&
                        <p>Ищём счета пользователя</p>
                    }
                    {userAccounts?.content?.length === 0 &&
                        <p>У пользователя нет счетов</p>
                    }
                    {
                        userAccounts?.content?.map((account: CardAccount) => (
                            <button
                                className="border rounded-lg p-2 flex justify-between items-center hover:border-primary transition-all"
                                onClick={() => { handleAccountSelection(account) }}
                            >
                                <div className="column">
                                    <p className="text-left">Счёт <span className="text-primary">{account.id?.substring(0, 4)}...</span></p>
                                    <p className="text-left">Сумма {account.money?.toFixed(2)}</p>
                                </div>

                                <div>{account.deleted ? "Удалён" : null}</div>
                            </button>
                        ))
                    }
                    <DialogFooter className="flex justify-center w-full">
                        <Button variant={"outline"} onClick={toggleUserWorkerRole} disabled={!user.active}>
                            {user.active ? (
                                <span>{isWorker ? "Убрать" : "Добавить"} роль рабочего</span>
                            ) : (
                                <span>Невозможно менять роли</span>
                            )}
                            
                        </Button>
                        <Button variant={"outline"} disabled={!user.active} onClick={() => {handleDeleteUser(user)}}>
                            {user.active ? "заблокировать пользователя" : "Пользователь уже заблокирован"}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
            {history.selected &&
                <TransactionHistoryDialog
                    show={history.show}
                    setShow={() => { resetSelection() }}
                    account={history.selected}
                />
            }
        </>
    )
}