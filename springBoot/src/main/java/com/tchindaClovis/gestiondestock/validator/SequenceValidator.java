//package com.tchindaClovis.gestiondestock.validator;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//
//import javax.sql.DataSource;
//import java.sql.Connection;
//import java.sql.Statement;
//
//@Component
//public class SequenceValidator implements ApplicationRunner {
//
//    @Autowired
//    private DataSource dataSource;
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        try (Connection conn = dataSource.getConnection();
//             Statement stmt = conn.createStatement()) {
//
//            String[] sequences = {"entreprise_seq", "utilisateur_seq", "roles_seq","category_seq", "article_seq"};
//
//            for (String seq : sequences) {
//                String table = seq.replace("_seq", "");
//                String sql = String.format(
//                        "SELECT setval('%s', COALESCE((SELECT MAX(id) FROM %s), 1), false)",
//                        seq, table
//                );
//                stmt.execute(sql);
//                System.out.println("Séquence " + seq + " vérifiée et synchronisée");
//            }
//        }
//    }
//}
