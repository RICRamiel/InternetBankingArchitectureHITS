export type Loan = {
    id: string;
    userId: string;
    accountId: string;
    percentDebt: number;
    currentDebt: number;
    initialDebt: number;
    interestDebtSum: number;
    interestRate: number;
    productName: string;
    createdAt: string;
  };