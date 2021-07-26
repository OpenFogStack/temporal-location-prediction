package me.mbe.prp.core

import me.mbe.prp.base.OneElementCache

abstract class NodesGetter {
    protected abstract fun getClosestNodeInternal(p: Location): Node

    abstract val nodes: List<Node>

    private val cache = OneElementCache(this::getClosestNodeInternal)

    fun getClosestNode(p: Positioned): Node = cache.get(p.location)
}