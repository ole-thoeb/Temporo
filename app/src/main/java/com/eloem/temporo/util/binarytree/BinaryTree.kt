package com.eloem.temporo.util.binarytree

interface BinaryTree<T>: MutableCollection<T>{
	val depth: Int
	fun find(key: Int): T?
}