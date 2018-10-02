package ru.hse.spb.tex

enum class ListType {
    ENUMERATE, ITEMIZE
}

enum class AlignmentType {
    FLUSH_LEFT,
    FLUSH_RIGHT,
    CENTER
}

interface LatexNode {
    fun <D, R> accept(visitor: TexVisitor<D, R>, data: D): R
}

interface LatexNodeWithChildren {
    fun <D> acceptChildren(visitor: TexVisitor<D, Unit>, data: D)
}

interface DocumentNode : LatexNode

class Document(
        val documentClass: String,
        val packages: List<String>,
        val children: List<DocumentNode>
) : LatexNode, LatexNodeWithChildren {
    override fun <D, R> accept(visitor: TexVisitor<D, R>, data: D): R = visitor.visitDocument(this, data)

    override fun <D> acceptChildren(visitor: TexVisitor<D, Unit>, data: D) {
        children.forEach { it.accept(visitor, data) }
    }
}

class Frame(val frameTitle: String?, val arguments: Map<String, String>, val body: Body) : DocumentNode, LatexNode {
    override fun <D, R> accept(visitor: TexVisitor<D, R>, data: D): R = visitor.visitFrame(this, data)
}

class Body(val children: List<BodyNode>) : LatexNode, LatexNodeWithChildren {
    override fun <D, R> accept(visitor: TexVisitor<D, R>, data: D): R = visitor.visitBody(this, data)

    override fun <D> acceptChildren(visitor: TexVisitor<D, Unit>, data: D) {
        children.forEach { it.accept(visitor, data) }
    }
}

interface BodyNode : LatexNode

open class Text(val text: String) : BodyNode {
    override fun <D, R> accept(visitor: TexVisitor<D, R>, data: D): R = visitor.visitText(this, data)
}

class LatexList(val type: ListType, val children: List<ListItem>) : BodyNode, LatexNodeWithChildren {
    override fun <D, R> accept(visitor: TexVisitor<D, R>, data: D): R = visitor.visitList(this, data)

    override fun <D> acceptChildren(visitor: TexVisitor<D, Unit>, data: D) {
        children.forEach { it.accept(visitor, data) }
    }
}

class ListItem(val body: Body) : LatexNode {
    override fun <D, R> accept(visitor: TexVisitor<D, R>, data: D): R = visitor.visitListItem(this, data)
}

class Math(val text: String) : BodyNode {
    override fun <D, R> accept(visitor: TexVisitor<D, R>, data: D): R = visitor.visitMath(this, data)
}

class Alignment(val alignmentType: AlignmentType, val body: Body) : BodyNode {
    override fun <D, R> accept(visitor: TexVisitor<D, R>, data: D): R = visitor.visitAlignment(this, data)
}

class CustomTag(val name: String, val arguments: Map<String, String>, val body: Body) : BodyNode {
    override fun <D, R> accept(visitor: TexVisitor<D, R>, data: D): R = visitor.visitCustomTag(this, data)
}