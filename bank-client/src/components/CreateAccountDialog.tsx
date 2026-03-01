import { useState } from "react";
import { PlusCircle } from "lucide-react";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "./ui/dialog";
import { toast } from "sonner";
import { useOpenAccountMutation } from "../lib/api/generatedApi";

interface CreateAccountDialogProps {
  userId: string;
  onAccountCreated: () => void;
}

export function CreateAccountDialog({
  userId,
  onAccountCreated,
}: CreateAccountDialogProps) {
  const [open, setOpen] = useState(false);
  const [name, setName] = useState("");
  const [openAccount, { isLoading }] = useOpenAccountMutation();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      toast.error("Введите название счета");
      return;
    }

    try {
      await openAccount({ userId }).unwrap();
      toast.success("Счет успешно создан");
      setName("");
      setOpen(false);
      onAccountCreated();
    } catch {
      toast.error("Не удалось создать счет");
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button>
          <PlusCircle className="h-4 w-4 mr-2" />
          Открыть счет
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Открыть новый счет</DialogTitle>
          <DialogDescription>
            Введите название для нового банковского счета
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="accountName">Название счета</Label>
            <Input
              id="accountName"
              placeholder="Основной счет"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="bg-input"
            />
          </div>
          <Button type="submit" className="w-full" disabled={isLoading}>
            Создать счет
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  );
}
