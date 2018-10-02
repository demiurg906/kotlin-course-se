package ru.hse.spb.tex

import java.io.OutputStream

@DslMarker
annotation class LatexMarker

class LatexBuilderException(message: String? = null) : Exception(message)

fun document(init: DocumentBuilder.() -> Unit): Document {
    return DocumentBuilder().apply(init).buildDocument()
}

@LatexMarker
class DocumentBuilder {
    private var documentClass: String? = null
    private val packages = mutableListOf<String>()

    private val children = mutableListOf<DocumentNode>()

    fun documentClass(documentClass: String) {
        this.documentClass = documentClass
    }

    fun usepackage(vararg packages: String) {
        this.packages.addAll(packages)
    }

    fun frame(frameTitle: String? = null, vararg args: Pair<String, String>, init: BodyBuilder.() -> Unit) {
        val frameBody = createBody(init)
        val frame = Frame(frameTitle, args.toMap(), frameBody)
        children += frame
    }

    fun buildDocument(): Document {
        return Document(
                documentClass ?: throw  LatexBuilderException("document class is not initialized"),
                packages,
                children
        )
    }
}

@LatexMarker
class BodyBuilder {
    private val children = mutableListOf<BodyNode>()

    fun itemize(init: LatexListBuilder.() -> Unit) {
        val list = LatexListBuilder(ListType.ITEMIZE).apply(init).buildList()
        children += list
    }

    fun enumerate(init: LatexListBuilder.() -> Unit) {
        val list = LatexListBuilder(ListType.ENUMERATE).apply(init).buildList()
        children += list
    }

    fun flushLeft(init: BodyBuilder.() -> Unit) {
        alignment(AlignmentType.FLUSH_LEFT, init)
    }

    fun flushRight(init: BodyBuilder.() -> Unit) {
        alignment(AlignmentType.FLUSH_RIGHT, init)
    }

    fun center(init: BodyBuilder.() -> Unit) {
        alignment(AlignmentType.CENTER, init)
    }

    private fun alignment(alignmentType: AlignmentType, init: BodyBuilder.() -> Unit) {
        val body = createBody(init)
        val alignment = Alignment(alignmentType, body)
        children += alignment
    }

    operator fun String.unaryPlus() {
        val text = Text(this)
        children += text
    }

    fun math(expression: String) {
        val math = Math(expression)
        children += math
    }

    fun customTag(name: String, vararg args: Pair<String, String>, init: BodyBuilder.() -> Unit) {
        val body = createBody(init)
        val customTag = CustomTag(name, args.toMap(), body)
        children += customTag
    }

    fun buildBody(): Body {
        return Body(children)
    }
}

private fun createBody(init: BodyBuilder.() -> Unit) = BodyBuilder().apply(init).buildBody()

@LatexMarker
class LatexListBuilder(private val listType: ListType) {
    private val children = mutableListOf<ListItem>()

    fun item(init: BodyBuilder.() -> Unit) {
        val body = createBody(init)
        children += ListItem(body)
    }

    fun buildList(): LatexList {
        return LatexList(listType, children)
    }
}

fun Document.toLatexString(): String = TexPrinter(this).buildTexDocument()

fun Document.toOutputStream(outputStream: OutputStream) = outputStream.writer().use { it.write(toLatexString()) }