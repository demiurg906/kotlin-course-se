package ru.hse.spb.tex

fun main(args: Array<String>) {
    val rows = (1..5).map { "row number $it" }

    document {
        documentClass("beamer")
        usepackage("babel", "russian")
        frame("frametitle", "arg1" to "arg2") {
            itemize {
                for (row in rows) {
                    item { +"$row text" }
                }
            }

            // begin{pyglist}[language=kotlin]...\end{pyglist}
            customTag("pyglist", "language" to "kotlin") {
                +"""
               |val a = 1
               |
            """
            }
        }
    }.toOutputStream(System.out)
}