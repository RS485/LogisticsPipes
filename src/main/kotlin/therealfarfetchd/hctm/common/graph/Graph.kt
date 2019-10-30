package therealfarfetchd.hctm.common.graph

// TODO move to somewhere else
class Graph<N, L> {
    var nodes: Set<Node<N, L>> = emptySet()
        private set

    fun add(data: N): Node<N, L> {
        val node = Node<N, L>(data)
        nodes.forEach { it.onAdded(node) }
        nodes += node
        return node
    }

    fun remove(node: Node<N, L>) {
        if (node in nodes) {
            nodes -= node
            nodes.forEach { it.onRemoved(node) }
        }
    }

    private fun moveBulkUnchecked(into: Graph<N, L>, nodes: Set<Node<N, L>>) {
        this.nodes -= nodes
        into.nodes += nodes
    }

    /**
     * Removes unconnected parts of the graph and returns them as new graphs.
     */
    fun split(): Set<Graph<N, L>> {
        val result = mutableSetOf<Graph<N, L>>()
        val toBeChecked = nodes.toMutableSet()
        while (toBeChecked.isNotEmpty()) {
            val connected = mutableSetOf<Node<N, L>>()
            fun descend(node: Node<N, L>) {
                connected += node
                toBeChecked -= node
                for (link in node.connections) {
                    val a = link.other(node)
                    if (a in toBeChecked) {
                        descend(a)
                    }
                }
            }
            descend(toBeChecked.first())

            if (toBeChecked.isNotEmpty()) {
                val net = Graph<N, L>()
                moveBulkUnchecked(net, connected)
                result += net
            }
        }
        return result
    }

    fun join(other: Graph<N, L>) {
        this.nodes += other.nodes
        other.nodes = emptySet()
    }

    fun link(node1: Node<N, L>, node2: Node<N, L>, data1: L, data2: L): Link<N, L> {
        val link = Link(node1, node2, data1, data2)
        node1.onLink(link)
        node2.onLink(link)
        return link
    }

    operator fun contains(node: Node<N, L>) = node in nodes
}

data class Node<N, L>(val data: N) {
    var connections: Set<Link<N, L>> = emptySet()
        @JvmSynthetic internal set

    fun onAdded(node: Node<N, L>) {}

    fun onRemoved(node: Node<N, L>) {
        connections = connections.filter { node !in it }.toSet()
    }

    fun onLink(link: Link<N, L>) {
        connections += link
    }
}

data class Link<N, L>(val first: Node<N, L>, val second: Node<N, L>, val data1: L, val data2: L) {
    operator fun contains(node: Node<N, L>) = node == first || node == second

    fun containsPort(data: L) = data == data1 || data == data2

    fun other(node: Node<N, L>) = if (node == second) first else second

    fun data(node: Node<N, L>) = if (node == first) data1 else data2

    fun otherData(node: Node<N, L>) = if (node == second) data1 else data2
}