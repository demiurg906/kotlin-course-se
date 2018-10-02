package ru.hse.spb.tex

import org.junit.Assert
import org.junit.Test

class TexBuilderTests {
    @Test
    fun test1() {
        val rows = (1..5).map { "row number $it" }

        val actual = document {
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
        }.toLatexString()

        val expected = """
            \documentclass{beamer}
            \userpackage{babel, russian}

            \begin{document}
            \begin{frame}[arg1=arg2]
            \frametitle{frametitle}
            \begin{itemize}
            \item row number 1 text
            \item row number 2 text
            \item row number 3 text
            \item row number 4 text
            \item row number 5 text

            \end{itemize}
            \begin{pyglist}[language=kotlin]

                           |val a = 1
                           |

            \end{pyglist}

            \end{frame}

            \end{document}

        """.trimIndent()

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun test2() {
        val actual = document {
            documentClass("beamer")

            frame("first slide", "foo" to "bar", "bar" to "baz") {
                center {
                    math("1 + 1 ^ 1")
                    +"lalala"

                    enumerate {
                        for (n in 1..5) {
                            item {
                                flushLeft {
                                    +"item № $n\n"
                                    math("$n \\times $n = $n")
                                }
                            }
                        }
                    }
                }
            }

            frame("second slide") {
                customTag("NoArgsTag") {
                    +"hello"
                }
            }
        }.toLatexString()

        val expected = """
            \documentclass{beamer}
            \userpackage{}

            \begin{document}
            \begin{frame}[foo=bar, bar=baz]
            \frametitle{first slide}
            \begin{center}
            ${'$'}1 + 1 ^ 1${'$'}lalala\begin{enumerate}
            \item \begin{flushleft}
            item № 1
            ${'$'}1 \times 1 = 1${'$'}
            \end{flushleft}

            \item \begin{flushleft}
            item № 2
            ${'$'}2 \times 2 = 2${'$'}
            \end{flushleft}

            \item \begin{flushleft}
            item № 3
            ${'$'}3 \times 3 = 3${'$'}
            \end{flushleft}

            \item \begin{flushleft}
            item № 4
            ${'$'}4 \times 4 = 4${'$'}
            \end{flushleft}

            \item \begin{flushleft}
            item № 5
            ${'$'}5 \times 5 = 5${'$'}
            \end{flushleft}


            \end{enumerate}

            \end{center}

            \end{frame}
            \begin{frame}
            \frametitle{second slide}
            \begin{NoArgsTag}
            hello
            \end{NoArgsTag}

            \end{frame}

            \end{document}

        """.trimIndent()

        Assert.assertEquals(expected, actual)
    }
}