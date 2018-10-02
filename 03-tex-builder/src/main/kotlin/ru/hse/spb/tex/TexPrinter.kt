package ru.hse.spb.tex

class TexPrinter(private val document: Document) : TexVisitor<Unit, Unit> {
    private val sb = StringBuilder()

    companion object {
        private const val DOCUMENT = "document"
        private const val FRAME = "frame"

        private const val ITEMIZE = "itemize"
        private const val ENUMERATE = "enumerate"

        private const val FLUSH_LEFT = "flushleft"
        private const val FLUSH_RIGHT = "flushright"
        private const val CENTER = "center"

        private const val ARGUMENT_SEPARATOR = ", "
    }

    private fun AlignmentType.asString(): String = when (this) {
        AlignmentType.FLUSH_LEFT -> FLUSH_LEFT
        AlignmentType.FLUSH_RIGHT -> FLUSH_RIGHT
        AlignmentType.CENTER -> CENTER
    }

    private fun ListType.asString(): String = when (this) {
        ListType.ENUMERATE -> ENUMERATE
        ListType.ITEMIZE -> ITEMIZE
    }

    private fun Map<String, String>.asArgumentsString() = if (isEmpty()) {
        null
    } else {
        entries.joinToString(ARGUMENT_SEPARATOR) { (key, value) -> "$key=$value" }
    }

    private fun begin(title: String, arguments: String? = null) {
        sb.append("\\begin{$title}")
        if (arguments != null) {
            sb.append("[$arguments]")
        }
        sb.appendln()
    }

    private fun end(title: String) {
        sb.appendln()
        sb.appendln("\\end{$title}")
    }

    override fun visitDocument(document: Document, data: Unit) {
        sb.appendln("\\documentclass{${document.documentClass}}")
        sb.appendln("\\userpackage{${document.packages.joinToString(ARGUMENT_SEPARATOR)}}")
        sb.appendln()
        begin(DOCUMENT)
        document.acceptChildren(this, Unit)
        end(DOCUMENT)
    }

    override fun visitFrame(frame: Frame, data: Unit) {
        begin(FRAME, frame.arguments.asArgumentsString())
        if (frame.frameTitle != null) {
            sb.appendln("\\frametitle{${frame.frameTitle}}")
        }
        frame.body.accept(this, Unit)
        end(FRAME)
    }

    override fun visitBody(body: Body, data: Unit) {
        body.acceptChildren(this, Unit)
    }

    override fun visitText(text: Text, data: Unit) {
        sb.append(text.text)
    }

    override fun visitMath(math: Math, data: Unit) {
        sb.append('$')
        sb.append(math.text)
        sb.append('$')
    }

    override fun visitList(list: LatexList, data: Unit) {
        begin(list.type.asString())
        list.acceptChildren(this, Unit)
        end(list.type.asString())
    }

    override fun visitListItem(listItem: ListItem, data: Unit) {
        sb.append("\\item ")
        listItem.body.accept(this, Unit)
        sb.appendln()
    }

    override fun visitAlignment(alignment: Alignment, data: Unit) {
        begin(alignment.alignmentType.asString())
        alignment.body.accept(this, Unit)
        end(alignment.alignmentType.asString())
    }

    override fun visitCustomTag(customTag: CustomTag, data: Unit) {
        begin(customTag.name, customTag.arguments.asArgumentsString())
        customTag.body.accept(this, Unit)
        end(customTag.name)
    }

    fun buildTexDocument(): String {
        document.accept(this, Unit)
        return sb.toString().split("\n").joinToString("\n") { it.trimEnd() }
    }
}