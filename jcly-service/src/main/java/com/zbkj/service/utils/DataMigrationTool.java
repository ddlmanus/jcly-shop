package com.zbkj.service.utils;

import lombok.var;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据迁移工具类
 */
public class DataMigrationTool {

    private static final String DB_URL = "jdbc:mysql://47.98.199.1:3306/jcly-shop-dev?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true&rewriteBatchedStatements=true&connectTimeout=60000&socketTimeout=60000";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "gsafety@123";

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("                   数据迁移工具 (Java版本)");
        System.out.println("================================================================================");
        System.out.println();

        String sqlFilePath = "/Users/mac/tools/code/荆楚粮油云商城/jcly-shop/sql/data_migration_fixed.sql";

        try {
            // 加载MySQL驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[INFO] MySQL驱动加载成功");

            // 建立数据库连接
            System.out.println("[INFO] 正在连接数据库...");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            conn.setAutoCommit(false);
            System.out.println("[INFO] 数据库连接成功");
            System.out.println();

            // 读取SQL文件
            System.out.println("[INFO] 读取SQL文件: " + sqlFilePath);
            List<String> sqlStatements = readSqlFile(sqlFilePath);
            System.out.println("[INFO] 共读取 " + sqlStatements.size() + " 条SQL语句");
            System.out.println();

            // 执行SQL语句
            Statement stmt = conn.createStatement();
            int successCount = 0;
            int errorCount = 0;

            for (int i = 0; i < sqlStatements.size(); i++) {
                String sql = sqlStatements.get(i).trim();
                if (sql.isEmpty() || sql.startsWith("--")) {
                    continue;
                }

                try {
                    // 显示进度
                    if (i % 5 == 0) {
                        System.out.print("\r[进度] " + (i + 1) + "/" + sqlStatements.size() + " SQL语句已执行");
                    }

                    // 执行SQL
                    boolean hasResultSet = stmt.execute(sql);

                    // 如果是SELECT语句,显示结果
                    if (hasResultSet && sql.toUpperCase().trim().startsWith("SELECT")) {
                        var rs = stmt.getResultSet();
                        if (rs != null && rs.next()) {
                            int columnCount = rs.getMetaData().getColumnCount();
                            System.out.println();
                            for (int col = 1; col <= columnCount; col++) {
                                System.out.print(rs.getMetaData().getColumnName(col) + ": ");
                                System.out.print(rs.getString(col) + "  ");
                            }
                            System.out.println();
                        }
                    }

                    successCount++;

                } catch (Exception e) {
                    errorCount++;
                    System.err.println();
                    System.err.println("[ERROR] SQL执行失败:");
                    System.err.println("  SQL: " + (sql.length() > 100 ? sql.substring(0, 100) + "..." : sql));
                    System.err.println("  错误: " + e.getMessage());
                    System.err.println();

                    // 遇到错误是否继续
                    if (sql.toUpperCase().contains("INSERT") || sql.toUpperCase().contains("TRUNCATE")) {
                        System.err.println("[WARN] 关键操作失败,回滚事务");
                        conn.rollback();
                        throw e;
                    }
                }
            }

            System.out.println();
            System.out.println();
            System.out.println("================================================================================");
            System.out.println("[SUCCESS] 迁移完成!");
            System.out.println("  成功: " + successCount + " 条");
            System.out.println("  失败: " + errorCount + " 条");
            System.out.println("================================================================================");

            // 提交事务
            conn.commit();
            System.out.println("[INFO] 事务已提交");

            // 关闭连接
            stmt.close();
            conn.close();
            System.out.println("[INFO] 数据库连接已关闭");

        } catch (Exception e) {
            System.err.println();
            System.err.println("================================================================================");
            System.err.println("[FATAL ERROR] 迁移失败!");
            System.err.println("================================================================================");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 读取SQL文件并分割成独立的语句
     */
    private static List<String> readSqlFile(String filePath) throws Exception {
        List<String> statements = new ArrayList<>();
        StringBuilder currentStatement = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // 跳过空行和注释行
                if (line.isEmpty() || line.startsWith("--")) {
                    // 如果是信息性注释,保留它
                    if (line.contains("===")) {
                        if (currentStatement.length() > 0) {
                            statements.add(currentStatement.toString());
                            currentStatement = new StringBuilder();
                        }
                        // 添加SELECT语句来显示注释信息
                        String msg = line.replaceAll("--", "").replaceAll("=", "").trim();
                        if (!msg.isEmpty()) {
                            statements.add("SELECT '" + msg + "' as info");
                        }
                    }
                    continue;
                }

                // 累加当前语句
                currentStatement.append(line).append(" ");

                // 如果遇到分号,表示一条语句结束
                if (line.endsWith(";")) {
                    statements.add(currentStatement.toString());
                    currentStatement = new StringBuilder();
                }
            }

            // 添加最后一条语句(如果有)
            if (currentStatement.length() > 0) {
                statements.add(currentStatement.toString());
            }
        }

        return statements;
    }
}
