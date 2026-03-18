import type { PercentageStrategy } from "../types/percentageStrategy";

export function getPercentageStrategy(strat: PercentageStrategy) {
    switch(strat){
        case "FROM_REMAINING_DEBT": return "от оставшегося долга";
        case "FROM_TOTAL_DEBT": return "от общего долга";
    }
}