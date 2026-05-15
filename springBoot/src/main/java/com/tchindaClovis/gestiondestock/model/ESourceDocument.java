package com.tchindaClovis.gestiondestock.model;

public enum ESourceDocument {
    COMMANDE_CLIENT, COMMANDE_FOURNISSEUR, VENTE, CORRECTION_STOCK;

    @Override
    public String toString() {
        switch (this) {
            case COMMANDE_CLIENT:
                return "ESourceDocument.COMMANDE_CLIENT"; //Commande des articles par un client
            case COMMANDE_FOURNISSEUR:
                return "ESourceDocument.COMMANDE_FOURNISSEUR"; //Commande des articles par un fournisseur
            case VENTE:
                return "ESourceDocument.VENTE"; //Vente des articles
            case CORRECTION_STOCK:
                return "ESourceDocument.CORRECTION_STOCK"; //Correction du stock suite à un évènement

            default:
                return "";
        }
    }
}
