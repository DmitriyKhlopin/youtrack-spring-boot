package fsight.youtrack.mail

import org.springframework.stereotype.Service

@Service
class HtmlBuilder : IHtmlBuilder {
    override fun buildMessage(body: String): String {
        return """
            <html>
                    ${getHead(null, null)}
                        <body>
                            $body                                           
                        </body>
                    </html>   
        """.trimIndent().replace("[\n\r]".toRegex(), "").replace("\\s+".toRegex(), " ")
    }

    override fun createFeaturesMessage(agent: String, tableBody: String): String {
        val body = """
            ${agent}, добрый день.<br>
            Просьба оценить внешние фичи, находящиеся на <a href='https://tfsprod.fsight.ru/Foresight/AP/_boards/board/t/Technical%20Support%20Team/Features'>доске</a>.<br><br>
            <table border="1">
                <thead><tr><th><p>ID</p></th><th><p>Задача</p></th><th><p>Проект</p></th><th><p>Приоритет</p></th><th><p>Дата создания</p></th><th><p>Дата изменения</p></th></tr></thead>
                <tbody>$tableBody</tbody>
            </table>
        """.trimIndent()
        return """
            <html>
                    ${getHead(2, 50)}
                        <body>
                            $body                                           
                        </body>
                    </html>   
        """.trimIndent().replace("[\n\r]".toRegex(), "").replace("\\s+".toRegex(), " ").replace("> <", "><").replace("\\\"", "\"")
    }

    fun getHead(index: Int?, firstColumnLimit: Int?): String {
        return """
         <head>
    <meta charset="UTF-8">
    <title>Title</title>
    <style type="text/css">
        body {
            font-family: Verdana, Arial, Helvetica, sans-serif;
            color: #1f2326;
        }

        table {
            border-collapse: collapse;
            border: 1px solid black;
        }

        th, td {
            padding: 4px 8px;
            text-align: left;
            border: 1px solid black;
        }
        ${if (firstColumnLimit != null) """td::nth-child(${index}n) { width: ${firstColumnLimit}%;}""" else ""}
        tr {
            padding: 0
        }
    </style>
</head>
    """.trimIndent()
    }
}
