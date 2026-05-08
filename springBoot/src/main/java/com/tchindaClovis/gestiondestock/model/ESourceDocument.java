package com.tchindaClovis.gestiondestock.model;

public enum ESourceDocument {
    COMMANDE_CLIENT, COMMANDE_FOURNISSEUR, VENTE, CORRECTION_STOCK;

    @Override
    public String toString() {
        switch (this) {
            case COMMANDE_CLIENT:
                return "Commande des articles par un client";
            case COMMANDE_FOURNISSEUR:
                return "Commande des articles par un fournisseur";
            case VENTE:
                return "Vente des articles";
            case CORRECTION_STOCK:
                return "Correction du stock suite à un évènement";

            default:
                return "";
        }
    }
}
