package com.android.happyplaces.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.happyplaces.R

/**
 * A abstract class which we will use for edit feature.
 */
abstract class SwipeItemCallback(private val context: Context, private val direction: Int) :
    ItemTouchHelper.SimpleCallback(0, direction) {
    private val icon = ContextCompat.getDrawable(
        context,
        getIcon()
    )

    private fun getIcon() =
        if (direction == ItemTouchHelper.RIGHT) R.drawable.ic_edit_white_24dp else R.drawable.ic_delete_white_24dp

    private fun getItemBound(itemView: View) =
        if (direction == ItemTouchHelper.RIGHT) itemView.left else itemView.right

    private fun getRightWidth(editIconMargin: Int): Int =
        if (direction == ItemTouchHelper.RIGHT) editIconMargin else intrinsicWidth - editIconMargin

    private fun getLeftWidth(editIconMargin: Int): Int =
        if (direction == ItemTouchHelper.RIGHT) intrinsicWidth - editIconMargin else editIconMargin

    private val intrinsicWidth = icon!!.intrinsicWidth
    private val intrinsicHeight = icon!!.intrinsicHeight
    private val background = ColorDrawable()
    private val backgroundColor =
        if (direction == ItemTouchHelper.RIGHT) Color.parseColor("#008000") else Color.parseColor("#FF0000")


    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        /**
         * To disable "swipe" for specific item return 0 here.
         * For example:
         * if (viewHolder?.itemViewType == YourAdapter.SOME_TYPE) return 0
         * if (viewHolder?.adapterPosition == 0) return 0
         */
        if (viewHolder.adapterPosition == 10) return 0
        return super.getMovementFlags(recyclerView, viewHolder)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {

        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            clearCanvas(
                c,
                itemView.left + dX,
                itemView.top.toFloat(),
                itemView.left.toFloat(),
                itemView.bottom.toFloat()
            )
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        // Draw the green background
        background.color = backgroundColor
        background.setBounds(
            getItemBound(itemView) + dX.toInt(),
            itemView.top,
            getItemBound(itemView),
            itemView.bottom
        )
        background.draw(c)

        // Calculate position of edit icon
        val editIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val editIconMargin = (itemHeight - intrinsicHeight)
        val editIconLeft = getItemBound(itemView) - getLeftWidth(editIconMargin)
        val editIconRight = getItemBound(itemView) + getRightWidth(editIconMargin)
        val editIconBottom = editIconTop + intrinsicHeight

        // Draw the icon
        icon!!.setBounds(editIconLeft, editIconTop, editIconRight, editIconBottom)
        icon.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }
}