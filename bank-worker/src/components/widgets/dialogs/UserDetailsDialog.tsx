import { useState } from "react";
import { Eye, NotebookIcon, NotebookPen, NotebookPenIcon, PenIcon, PlusCircle } from "lucide-react";
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
import { toast } from "sonner";
import { useOpenAccountMutation, type UserDto } from "../../../lib/api/generatedApi";

interface UserDetailsDialogProps {
    user: UserDto,
}

export function UserDetailsDialog({
    user,
} : UserDetailsDialogProps){
    const [open, setOpen] = useState(false);

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger>
                <Button variant='outline'>
                    <Eye className="text-primary"/>
                </Button>
            </DialogTrigger>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>{user.name}</DialogTitle>
                    <DialogDescription>{user.email}</DialogDescription>
                </DialogHeader>
            </DialogContent>
        </Dialog>
    )
}