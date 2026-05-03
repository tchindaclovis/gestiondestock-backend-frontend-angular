package com.tchindaClovis.gestiondestock.model;

public enum EEtatDocument {
    PRO_FORMAT, VALIDEE,VENDUE, LIVREE,ANNULEE;

    @Override
    public String toString() {
        switch (this) {
            case PRO_FORMAT:
                return "EtatDocument.PRO_FORMAT";
            case VALIDEE:
                return "EtatDocument.VALIDEE";
            case VENDUE:
                return "EtatDocument.VENDUE";
            case LIVREE:
                return "EtatDocument.LIVREE";
            case ANNULEE:
                return "EtatDocument.ANNULEE";
            default:
                return "";
        }
    }

}
