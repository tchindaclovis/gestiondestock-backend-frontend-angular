package com.tchindaClovis.gestiondestock.model;

public enum EActionDocument {
    ACTION_REJET, VALIDATION, TRANSFERT, DMDVALIDATION, DMDENREGISTREMENT, INITIALISATION, DMDDEROGATION, DEROGATION, REJETDEROGATION;

    @Override
    public String toString() {
        switch (this) {
            case ACTION_REJET:
                return "EActionDocument.ACTION_REJET";
            case VALIDATION:
                return "EActionDocument.VALIDATION";
            case TRANSFERT:
                return "EActionDocument.TRANSFERT";
            case DMDVALIDATION:
                return "EActionDocument.DMDVALIDATION";
            case DMDENREGISTREMENT:
                return "EActionDocument.DMDENREGISTREMENT";
            case INITIALISATION:
                return "EActionDocument.INITIALISATION";
            case DMDDEROGATION:
                return "EActionDocument.DMDDEROGATION";
            case DEROGATION:
                return "EActionDocument.DEROGATION";
            case REJETDEROGATION:
                return "EActionDocument.REJETDEROGATION";
            default:
                return "";
        }
    }

    public String getCodeActionDocument() {
        switch (this) {
            case ACTION_REJET:
                return "ACT001";
            case VALIDATION:
                return "ACT002";
            case TRANSFERT:
                return "ACT003";
            case DMDVALIDATION:
                return "ACT004";
            case DMDENREGISTREMENT:
                return "ACT005";
            case INITIALISATION:
                return "ACT006";
            case DMDDEROGATION:
                return "ACT007";
            case DEROGATION:
                return "ACT008";
            case REJETDEROGATION:
                return "ACT009";
        }
        return null;
    }
}
