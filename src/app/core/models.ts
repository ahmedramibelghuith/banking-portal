export interface User {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  role: 'USER' | 'ADMIN';
}
export interface Account {
  id: number;
  dateCreation: string;
  solde: number;
  etat: 'ACTIVE' | 'FERME' | 'BLOQUE';
  type: 'CURRENT' | 'SAVINGS';
  clientId: number;
  clientEmail: string;
}
export interface Operation {
  id: number;
  compteId: number;
  dateOperation: string;
  montant: number;
  type: 'CREDIT' | 'DEBIT';
  reference: string;
}
export interface Summary {
  clients: number;
  accounts: number;
  transactions: number;
}
