import { useState } from "react";
import { Dialog, DialogContent, DialogFooter, DialogHeader } from "../../ui/dialog"
import { useCreateCreditRuleMutation, type CreditRule } from "../../../lib/api/generatedApi";
import { Input } from "../../ui/input";
import { Label } from "../../ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../../ui/select";

import { type PercentageStrategy } from "../../../lib/types/percentageStrategy";
import { getPercentageStrategy } from "../../../lib/utils/getPercentageStrategy";
import { Button } from "../../ui/button";
import { toast } from "sonner";

interface CreateLoanProductDialogProps{
    show: boolean,
    setShow: (show: boolean) => void
}

export function CreateLoanProductDialog({show, setShow} : CreateLoanProductDialogProps) {

    const [name, setName] = useState("")
    const [percentageStrategy, setStrategy] = useState<"FROM_REMAINING_DEBT" | "FROM_TOTAL_DEBT">("FROM_REMAINING_DEBT");
    const [percentage, setPercentage] = useState(0.0);
    
    //**
    // in secconds
    //  */
    const [collectionPeriod, setCollectPeriod] = useState(0);

    const [create, {isLoading}] = useCreateCreditRuleMutation()

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()

        if (name === ""){
            toast.error("Имя не должн быть пустым");
        }

        const newCreditRule : CreditRule = {
            ruleName: name,
            percentage: percentage,
            percentageStrategy: percentageStrategy,
            collectionPeriodSeconds: collectionPeriod,
            openingDate: new Date().toISOString(),
        }

        try{
            const newRule = await create({creditRuleDto: newCreditRule});
            toast.success(`Новое правило ${newRule.data?.ruleName} создано!`)

        } catch (error) {
            toast.error(`Ошибка ${error}`)
        }
    }
    
    return (
        <Dialog open={show} onOpenChange={setShow}>
            <DialogContent>
                <DialogHeader>
                    <h3 className="text-primary">Создание нового кредитного продукта</h3>
                </DialogHeader>

                <form onSubmit={handleSubmit}>
                    <div className="column space-y-4">

                        <div>
                            <Label htmlFor="name" className="pb-2">Название</Label>
                            <Input
                            type="text"
                            id="name"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            />
                        </div>

                        <div>
                            <Label htmlFor="strategy" className="pb-2">Стратегия:</Label>
                            <Select
                            value={percentageStrategy}
                            onValueChange={(value: PercentageStrategy) => {setStrategy(value)}}
                            >
                                <SelectTrigger id="loanStrategy">
                                    <SelectValue placeholder="Выберите стратегию"/>
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value={"FROM_REMAINING_DEBT"}>{getPercentageStrategy("FROM_REMAINING_DEBT")}</SelectItem>
                                    <SelectItem value={"FROM_TOTAL_DEBT"}>{getPercentageStrategy("FROM_TOTAL_DEBT")}</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>

                        <div>
                            <Label htmlFor="percentage" className="pb-2">Процент:</Label>
                            <Input
                            type="number"
                            id="percentage"
                            value={percentage}
                            onChange={(e) => setPercentage(parseFloat(e.target.value))}
                            step="0.1"
                            min="0"
                            max="1000"
                            />
                        </div>

                        <div>
                            <Label htmlFor="collectionPeriod" className="pb-2">Период взыскания (в секундах):</Label>
                            <Input
                            type="number"
                            id="collectionPeriod"
                            value={collectionPeriod}
                            onChange={(e) => setCollectPeriod(parseInt(e.target.value))}
                            min="0"
                            />
                        </div>
                    
                        <Button variant={"outline"} className="w-full" type="submit">Submit</Button>

                    </div>

                    </form>

                <DialogFooter>

                </DialogFooter>
            </DialogContent>
        </Dialog>
    )
}