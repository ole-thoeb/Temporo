package com.eloem.temporo.util.binarytree

import java.lang.StringBuilder
import java.util.*
import kotlin.NoSuchElementException

data class TreeNode<T>(
	var left: TreeNode<T>?,
	var right: TreeNode<T>?,
	var parent: TreeNode<T>?,
	var value: T
) {

	fun insertRight(node: TreeNode<T>) {
		node.parent = this
		right = node
	}

	fun insertLeft(node: TreeNode<T>) {
		node.parent = this
		left = node
	}

	override fun toString(): String {
		val str = StringBuilder()
		for (value in inOrderIterator()) {
			str.append(value)
			str.append(", ")
		}
		return str.toString()
	}

	companion object {
		fun <T>valueOf(value: T): TreeNode<T> = TreeNode(null, null, null, value)
	}
}

fun <T> TreeNode<T>.inOrderIterator(): Iterator<T> = BinaryTreeIterator(this)

internal class BinaryTreeIterator<T>(mTree: TreeNode<T>?): Iterator<T>{
	private val stack = Stack<TreeNode<T>>()
	private var inOrderCourser: TreeNode<T>? = mTree
	private var currentVal: TreeNode<T>? = null

	init {
		continueInorder()
	}

	private fun continueInorder(){
		while (true){
			val courser = inOrderCourser
			if (courser != null){
				stack.push(courser)
				inOrderCourser = courser.left
			}
			else break
		}
	}

	override fun hasNext(): Boolean = !stack.empty() || inOrderCourser != null

	override fun next(): T {
		if (stack.empty()) throw NoSuchElementException()

		val nextVal = stack.pop()
		currentVal = nextVal
		inOrderCourser = nextVal.right
		continueInorder()
		return nextVal.value
	}
}