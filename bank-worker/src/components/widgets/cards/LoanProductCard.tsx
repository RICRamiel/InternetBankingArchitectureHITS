import type { CreditRule } from "../../../lib/api/generatedApi"
import { type LoanProduct } from "../../../lib/types/loanProduct"
import type { PercentageStrategy } from "../../../lib/types/percentageStrategy"
import { getPercentageStrategy } from "../../../lib/utils/getPercentageStrategy"

interface LoanProductCardProps{
    loanProduct: CreditRule
}

const calculatePeriod = (period: number) => {
    return `${(period / 60).toFixed(2)} мин.`
}

export function LoanProductCard({loanProduct}: LoanProductCardProps) {

    return (
        <div className="column bg-card p-4 rounded-lg border text-primary space-y-1">
            
            <h4 className="text-primary">
                {loanProduct.ruleName}
            </h4>

            <p className="text-primary">
                Стратегия: {getPercentageStrategy(loanProduct.percentageStrategy as PercentageStrategy)}
            </p>
            <p className="text-primary">
                Процент = {loanProduct.percentage?.toFixed(2)}%
            </p>
            <p>Период: {(loanProduct.collectionPeriodSeconds ? calculatePeriod(loanProduct.collectionPeriodSeconds): "неизвестно")}</p>
        </div>
    )
}