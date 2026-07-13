package edu.gpnu.bigdata.web;

import io.javalin.Javalin;
import java.util.Map;

public class ApiServer {
    public static void main(String[] args) {
        var app = Javalin.create().start(8080);

        // ★★★ 同学只需修改下面这个 Map 中的数据 ★★★
        app.get("/api/stats", ctx -> {
            Map<String, Object> data = Map.of(
                    "labels", new String[]{"浏览", "购物车", "下单", "支付"},
                    "values", new int[]{10000, 4000, 2000, 1600}
            );
            ctx.json(data);
        });

        app.get("/api/health", ctx -> ctx.result("OK"));

        // 跨域支持（加分扩展时使用）
        app.before(ctx -> ctx.header("Access-Control-Allow-Origin", "*"));

        System.out.println("Server started at http://localhost:8080/api/stats");
    }
}