export interface User {
  id: string;
  nom: string;
  prenom: string;
  email: string;
  role: 'USER' | 'ADMIN';
}
export interface Account {
  id: string;
  dateCreation: string;
  solde: string;
  etat: 'ACTIVE' | 'FERME' | 'BLOQUE';
  type: 'CURRENT' | 'SAVINGS';
  clientId: string;
  clientEmail: string;
}
export interface Operation {
  id: string;
  compteId: string;
  dateOperation: string;
  montant: string;
  type: 'CREDIT' | 'DEBIT';
  reference: string;
}
export interface Summary {
  clients: number;
  accounts: number;
  transactions: number;
}

export interface PageView<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  last: boolean;
}
export interface AccountTotals {
  total: string;
  count: number;
}
