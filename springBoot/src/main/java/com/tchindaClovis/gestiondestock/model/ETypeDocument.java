package com.tchindaClovis.gestiondestock.model;

public enum ETypeDocument {
    commande_client, commande_fournisseur ,vente, mouvement_stock;

    @Override
    public String toString() {
        switch (this) {
            case commande_client:
                return "Commande des articles par un client";
            case commande_fournisseur:
                return "Commande des articles par un fournisseur";
            case vente:
                return "Vente des articles";
            case mouvement_stock:
                return "Mouvement positif ou négatif de stock";

            default:
                return "";
        }
    }
}
