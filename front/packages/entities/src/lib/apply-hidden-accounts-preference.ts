import type { CardAccountEntity } from "@fins/api";

/**
 * Aligns `visible` with `GET /preferences` `hiddenAccounts` when the accounts
 * API does not set `visible` (or is out of sync). Main and deleted are left unchanged.
 */
export function applyHiddenAccountsPreference(
  accounts: CardAccountEntity[],
  hiddenAccountIds: string[] | undefined | null,
): CardAccountEntity[] {
  if (hiddenAccountIds == null || hiddenAccountIds.length === 0) {
    return accounts;
  }
  const hidden = new Set(hiddenAccountIds);
  return accounts.map((acc) => {
    const id = acc.id;
    if (!id || acc.main === true || acc.deleted === true) {
      return acc;
    }
    if (!hidden.has(id)) {
      return acc;
    }
    return { ...acc, visible: false };
  });
}
