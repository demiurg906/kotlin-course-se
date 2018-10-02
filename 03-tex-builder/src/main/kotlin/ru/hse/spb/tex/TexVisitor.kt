package ru.hse.spb.tex

interface TexVisitor<D, R> {
    fun visitDocument(document: Document, data: D): R

    fun visitFrame(frame: Frame, data: D): R

    fun visitBody(body: Body, data: D): R

    fun visitText(text: Text, data: D): R

    fun visitMath(math: Math, data: D): R

    fun visitList(list: LatexList, data: D): R

    fun visitListItem(listItem: ListItem, data: D): R

    fun visitAlignment(alignment: Alignment, data: D): R

    fun visitCustomTag(customTag: CustomTag, data: D): R
}