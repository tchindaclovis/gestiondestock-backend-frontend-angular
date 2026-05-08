package com.tchindaClovis.gestiondestock.model;

public enum ETypeDocument {
    ENTREE, SORTIE, SORTIE_VTE, CORRECTION_POS, CORRECTION_NEG,
    CORRECTION_POS_VENTE_RED, CORRECTION_NEG_VENTE_AUG, CORRECTION_NEG_RETOUR_FOURNISSEUR;

    @Override
    public String toString() {
        switch (this) {
            case ENTREE:
                return "Article livré par le fournisseur";
            case SORTIE:
                return "Article à command confirmé";
            case SORTIE_VTE:
                return "Article vendu au client";
            case CORRECTION_POS:
                return "Correction pr compenser erreur de comptage";
            case CORRECTION_NEG:
                return "Correction pour considérer une perte";
            case CORRECTION_POS_VENTE_RED:
                return "Correction pr retour d'un article vendu";
            case CORRECTION_NEG_VENTE_AUG:
                return "Correction pr l'ajout du nbre d'un produit vendu";
            case CORRECTION_NEG_RETOUR_FOURNISSEUR:
                return "Correction pr le retour au fournisseur d'un produit acheté";
            default:
                return "";
        }
    }
}
