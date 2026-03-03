package com.tchindaClovis.gestiondestock.interceptor;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class Interceptor implements StatementInspector {

    @Override
    public String inspect(String sql) {
        if (StringUtils.hasLength(sql) && isSelectQuery(sql)) {
            return processSelectQuery(sql);
        }
        return sql;
    }

    private boolean isSelectQuery(String sql) {
        return sql.trim().toLowerCase().startsWith("select");
    }

    private String processSelectQuery(String sql) {
        try {
            String idEntreprise = MDC.get("idEntreprise");

            // ✅ CORRECTION : Vérifier que c'est un nombre valide
            if (!isValidEnterpriseId(idEntreprise)) {
                return sql;
            }

            // Extraction sécurisée du nom de l'entité
            String entityName = extractEntityName(sql);
            if (entityName == null) {
                return sql;
            }

            if (shouldAddTenantFilter(entityName, idEntreprise)) {
                return addTenantCondition(sql, entityName, idEntreprise);
            }

        } catch (Exception e) {
            System.err.println("Erreur intercepteur SQL: " + e.getMessage());
        }

        return sql;
    }

    // ✅ CORRECTION : Validation de l'ID entreprise
    private boolean isValidEnterpriseId(String idEntreprise) {
        if (!StringUtils.hasLength(idEntreprise)) {
            return false;
        }

        try {
            Integer.parseInt(idEntreprise); // Vérifier que c'est un nombre
            return true;
        } catch (NumberFormatException e) {
            System.err.println("ID entreprise invalide dans MDC: " + idEntreprise);
            return false;
        }
    }

    // ✅ CORRECTION : Extraction plus robuste du nom d'entité
    private String extractEntityName(String sql) {
        try {
            String trimmedSql = sql.trim().toLowerCase();

            // Ignorer les requêtes qui ne suivent pas le pattern attendu
            if (!trimmedSql.matches("^select\\s+\\w+\\..*")) {
                return null;
            }

            int fromIndex = trimmedSql.indexOf(" from ");
            if (fromIndex == -1) return null;

            String selectPart = trimmedSql.substring(0, fromIndex);
            String[] parts = selectPart.split("\\s+");
            if (parts.length < 2) return null;

            String entityReference = parts[1];
            int dotIndex = entityReference.indexOf(".");
            if (dotIndex == -1) return null;

            return entityReference.substring(0, dotIndex);

        } catch (Exception e) {
            return null;
        }
    }

    private boolean shouldAddTenantFilter(String entityName, String idEntreprise) {
        return StringUtils.hasLength(entityName)
                && !entityName.toLowerCase().contains("entreprise")
                && !entityName.toLowerCase().contains("roles")
                && isValidEnterpriseId(idEntreprise);
    }

    // ✅ CORRECTION : Utilisation correcte des valeurs numériques
    private String addTenantCondition(String sql, String entityName, String idEntreprise) {
        String lowerSql = sql.toLowerCase();

        // ✅ CORRECTION : L'ID entreprise est déjà validé comme nombre
        String condition = entityName + ".identreprise = " + idEntreprise;

        if (lowerSql.contains(" where ")) {
            return sql + " and " + condition;
        } else {
            int whereIndex = lowerSql.indexOf(" where ");
            int orderByIndex = lowerSql.indexOf(" order by ");
            int groupByIndex = lowerSql.indexOf(" group by ");

            int insertPosition = sql.length();
            if (whereIndex != -1) insertPosition = whereIndex;
            else if (orderByIndex != -1) insertPosition = orderByIndex;
            else if (groupByIndex != -1) insertPosition = groupByIndex;

            return sql.substring(0, insertPosition)
                    + " where " + condition
                    + sql.substring(insertPosition);
        }
    }
}








//package com.tchindaClovis.gestiondestock.interceptor;
//
//import org.hibernate.resource.jdbc.spi.StatementInspector;
//import org.slf4j.MDC;
//import org.springframework.util.StringUtils;
//import org.springframework.stereotype.Component;
//
//@Component
//public class Interceptor implements StatementInspector {
//
//    @Override
//    public String inspect(String sql) {
//        if (StringUtils.hasLength(sql) && isSelectQuery(sql)) {
//            return processSelectQuery(sql);
//        }
//        return sql;
//    }
//
//    private boolean isSelectQuery(String sql) {
//        return sql.trim().toLowerCase().startsWith("select");
//    }
//
//    private String processSelectQuery(String sql) {
//        try {
//            // ✅ Vérification plus robuste de la structure SQL
//            String trimmedSql = sql.trim().toLowerCase();
//
//            // Ignorer les requêtes qui ne suivent pas le pattern attendu
//            if (!trimmedSql.matches("^select\\s+\\w+\\.\\w+.*")) {
//                return sql;
//            }
//
//            // Extraction sécurisée du nom de l'entité
//            int fromIndex = trimmedSql.indexOf(" from ");
//            if (fromIndex == -1) return sql;
//
//            String selectPart = trimmedSql.substring(0, fromIndex);
//            String[] parts = selectPart.split("\\s+");
//            if (parts.length < 2) return sql;
//
//            // Le nom de l'entité est généralement après "select"
//            String entityReference = parts[1];
//            int dotIndex = entityReference.indexOf(".");
//            if (dotIndex == -1) return sql;
//
//            String entityName = entityReference.substring(0, dotIndex);
//            String idEntreprise = MDC.get("idEntreprise");
//
//            if (shouldAddTenantFilter(entityName, idEntreprise)) {
//                return addTenantCondition(sql, entityName, idEntreprise);
//            }
//
//        } catch (Exception e) {
//            System.err.println("Erreur intercepteur SQL pour: " + sql);
//            e.printStackTrace();
//        }
//
//        return sql;
//    }
//
//    private boolean shouldAddTenantFilter(String entityName, String idEntreprise) {
//        return StringUtils.hasLength(entityName)
//                && !entityName.toLowerCase().contains("entreprise")
//                && !entityName.toLowerCase().contains("roles")
//                && StringUtils.hasLength(idEntreprise);
//    }
//
//    private String addTenantCondition(String sql, String entityName, String idEntreprise) {
//        String lowerSql = sql.toLowerCase();
//
//        if (lowerSql.contains(" where ")) {
//            return sql + " and " + entityName + ".identreprise = " + idEntreprise;
//        } else {
//            // Trouver la position de la clause WHERE ou ORDER BY ou la fin
//            int whereIndex = lowerSql.indexOf(" where ");
//            int orderByIndex = lowerSql.indexOf(" order by ");
//            int groupByIndex = lowerSql.indexOf(" group by ");
//
//            int insertPosition = sql.length();
//            if (whereIndex != -1) insertPosition = whereIndex;
//            else if (orderByIndex != -1) insertPosition = orderByIndex;
//            else if (groupByIndex != -1) insertPosition = groupByIndex;
//
//            return sql.substring(0, insertPosition)
//                    + " where " + entityName + ".identreprise = " + idEntreprise
//                    + sql.substring(insertPosition);
//        }
//    }
//}






//package com.tchindaClovis.gestiondestock.interceptor;
//
//import org.hibernate.resource.jdbc.spi.StatementInspector;
//import org.slf4j.MDC;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//@Component
//public class  Interceptor implements StatementInspector {
//
//    @Override
//    public String inspect(String sql) {
//        if (StringUtils.hasLength(sql) && sql.toLowerCase().startsWith("select")) {
//            int dotIndex = sql.indexOf(".");
//            if (dotIndex > 7 && dotIndex <= sql.length()) {
//                final String entityName = sql.substring(7, dotIndex);
//                final String idEntreprise = MDC.get("idEntreprise");
//                if (StringUtils.hasLength(entityName)
//                        && !entityName.toLowerCase().contains("entreprise")
//                        && !entityName.toLowerCase().contains("roles")
//                        && StringUtils.hasLength(idEntreprise)) {
//
//                    if (sql.contains("where")) {
//                        sql = sql + " and " + entityName + ".identreprise = " + idEntreprise;
//                    } else {
//                        sql = sql + " where " + entityName + ".identreprise = " + idEntreprise;
//                    }
//                }
//            }
//        }
//        return sql;
//    }
//}








//package com.tchindaClovis.gestiondestock.interceptor;
//
//import org.hibernate.resource.jdbc.spi.StatementInspector;
//import org.slf4j.MDC;
//import org.springframework.util.StringUtils;
//
//public class Interceptor implements StatementInspector {
//
//    @Override
//    public String inspect(String sql) {
//        if (StringUtils.hasLength(sql) && sql.toLowerCase().startsWith("select")) {
//            // select utilisateu0_.
//            final String entityName = sql.substring(7, sql.indexOf("."));
//            final String idEntreprise = MDC.get("idEntreprise"); //récupérer l'idEntreprise de mon MDC
//            if (StringUtils.hasLength(entityName)
//                    && !entityName.toLowerCase().contains("entreprise") //la classe "Entreprise" ne contient pas l'identreprise
//                    && !entityName.toLowerCase().contains("roles") //la classe "Roles" ne contient pas l'idEntreprise
//                    && StringUtils.hasLength(idEntreprise)) {   // vérifier que l'idEntreprise n'est pas null
//
//                if (sql.contains("where")) {
//                    sql = sql + " and " + entityName + ".identreprise = " + idEntreprise;
//                } else {
//                    sql = sql + " where " + entityName + ".identreprise = " + idEntreprise;
//                }
//            }
//        }
//        return sql;
//    }
//}









//package com.tchindaClovis.gestiondestock.interceptor;
//
//import org.hibernate.resource.jdbc.spi.StatementInspector;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//@Component
//public class Interceptor implements StatementInspector {
//
//    private static final Logger logger = LoggerFactory.getLogger(Interceptor.class);
//
//    @Override
//    public String inspect(String sql) {
//        // Formater le SQL pour une meilleure lisibilité
//        String formattedSql = sql.replaceAll("\\s+", " ").trim();
//
//        logger.info("""
//            🐢 SQL Intercepté (Hibernate 6):
//            ┌────────────────────────────────────────────────────
//            │ {}
//            └────────────────────────────────────────────────────
//            """, formattedSql);
//
//        // Vous pouvez modifier le SQL ici si nécessaire
//        if(StringUtils.hasLength(sql) && sql.toLowerCase().startsWith("select")){
//            if(sql.contains("where")){
//                sql = sql + " and identreprise = 3";
//            } else {
//            sql = sql + " where identreprise = 3";
//            }
//        }
//
//        // return formattedSql + " /* intercepté */";
//        return sql; // Retourner le SQL original ou modifié
//    }
//}





//package com.tchindaClovis.gestiondestock.interceptor;
//
//import org.hibernate.resource.jdbc.spi.StatementInspector;
//import org.springframework.stereotype.Component;
//
//@Component
//public class Interceptor implements StatementInspector {
//    @Override
//    public String inspect(String sql) {
//        System.out.println("SQL préparé: " + sql);


// Modifier le SQL si nécessaire
//        if(StringUtils.hasLength(sql) && sql.toLowerCase().startsWith("select")){
//            if(sql.contains("where")){
//                sql = sql + " and idEntreprise = 1";
//            } else {
//            sql = sql + " idEntreprise = 1";
//            }
//        }


//        return sql;    // intercepte la requête sql entre la couche repo et la BD
//    }
//}


//package com.tchindaClovis.gestiondestock.interceptor;
//
//import org.hibernate.internal.EmptyInterceptor;
//import org.hibernate.resource.jdbc.spi.StatementInspector;
//
//public class Interceptor extends StatementInspector {
//    @Override
//    public String onPrepareStatement(String sql) {
//        return super.onPrepareStatement(sql);
//    }
//}