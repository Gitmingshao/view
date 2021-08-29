package com.wjm.view

import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlin.collections.ArrayList

class FlowLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
//1

    private val mViewLines = arrayListOf<MutableList<View>>()
    private val mFlowData = ArrayList<String>()
    private var mResourceId = 0
    private var mMaxLines = 5
    private var mHorizontalInterval = dip2px(10f)
    private var mVerticalInterval =  dip2px(10f)
    private var onClickAction:(String)->Unit={}
    private var isAddFirst = false


    init {
        context.obtainStyledAttributes(attrs, R.styleable.FlowLayout).apply {
            mResourceId= getResourceId(R.styleable.FlowLayout_itemViewLayout, mResourceId)
            mMaxLines= getInt(R.styleable.FlowLayout_maxLines, mMaxLines)
            mHorizontalInterval = getDimension(R.styleable.FlowLayout_horizontalInterval, mHorizontalInterval)
            mVerticalInterval = getDimension(R.styleable.FlowLayout_verticalInterval, mVerticalInterval)
            recycle()
        }

        val animator = LayoutTransition()
        animator.setDuration(1000)
        val animIn = ObjectAnimator.ofFloat(this, "scaleY", 0F, 1F)
        animator.setAnimator(LayoutTransition.APPEARING,animIn)

        layoutTransition=animator

    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthParentSize = MeasureSpec.getSize(widthMeasureSpec)
        var heightParentSize = MeasureSpec.getSize(heightMeasureSpec)
        if (childCount==0) {
            setMeasuredDimension(widthParentSize,0)
            return
        }
        mViewLines.clear()
        var childViewItems = ArrayList<View>()
        mViewLines.add(childViewItems)
        val widthChildSize = MeasureSpec.makeMeasureSpec(widthParentSize, MeasureSpec.AT_MOST)
        val heightChildSize = MeasureSpec.makeMeasureSpec(heightParentSize,MeasureSpec.AT_MOST)
        for ( i in 0 until childCount ){
            val childView = getChildAt(i)
            measureChild(childView,widthChildSize,heightChildSize)
            if (childViewItems.size == 0) {
                childViewItems.add(childView)
            } else {
                val checkItemTotalState = checkItemTotalWidth(childViewItems, childView, widthParentSize)
                if (checkItemTotalState) {
                    childViewItems.add(childView)
                } else {
                    if (mViewLines.size < mMaxLines) {
                           childViewItems = addChangeLine(childView)
                    } else {
                        if (isAddFirst){
                            childViewItems = addChangeLine(childView)
                        }
                    }
                }

            }
        }
        val lines =if (isAddFirst) mMaxLines  else  mViewLines.size
        heightParentSize =  getChildAt(0).measuredHeight * lines + mVerticalInterval.toInt() *(lines+1) + paddingTop+paddingBottom
        setMeasuredDimension(widthParentSize,heightParentSize)
    }

    private fun addChangeLine(
        childView: View
    ): ArrayList<View> {
        var childViewItems:ArrayList<View> = ArrayList()
        childViewItems.add(childView)
        mViewLines.add(childViewItems)
        return childViewItems
    }


    private fun checkItemTotalWidth(
        childViewItems: MutableList<View>,
        childView: View,
        widthParentSize: Int
    ):Boolean{
        var totalWidth=paddingLeft
        val measuredWidth = childView.measuredWidth
        childViewItems.forEach {
            totalWidth+=it.measuredWidth + mHorizontalInterval.toInt()
        }
        totalWidth+=measuredWidth + mHorizontalInterval.toInt() +paddingRight
        return totalWidth <= widthParentSize
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount==0)  return
        var firstView = getChildAt(0)
        var currentTop = mVerticalInterval.toInt() + paddingTop
        var currentleft = paddingLeft
        var currentRight =  paddingLeft
        var currentBottom = firstView.measuredHeight + mVerticalInterval.toInt() + paddingTop
        mViewLines.forEach { lines ->
            lines.forEach {
                currentRight += it.measuredWidth
                if (currentRight> measuredWidth -paddingRight) {
                    currentRight= measuredWidth -paddingRight
                }
                it.layout(currentleft, currentTop, currentRight, currentBottom)
                currentleft = currentRight+mHorizontalInterval.toInt()
                currentRight += mHorizontalInterval.toInt()

            }
            currentleft= paddingLeft
            currentRight= mHorizontalInterval.toInt()
            currentTop+=firstView.measuredHeight + mVerticalInterval.toInt()
            currentBottom+=firstView.measuredHeight + mVerticalInterval.toInt()
        }
    }


    fun setItemData(list:List<String>){
        mFlowData.clear()
        removeAllViews()
        mFlowData.addAll(list)
        for (msg in mFlowData) {
            addTimeView(msg)
        }
    }


    fun addItemData(msg: String){
        mFlowData.add(msg)
        isAddFirst=true
        addTimeView(msg,isAddFirst)
    }


    private fun addTimeView(msg: String,addFirst:Boolean=false){
        val textView =if (mResourceId != 0) {
            try {
                LayoutInflater.from(context).inflate(mResourceId, null, false) as TextView
            }catch (e:Exception){
                TextView(context)
            }
        } else {
            TextView(context)
        }
        textView.text=msg
        textView.setOnClickListener {
            onClickAction(textView.text.toString())
        }
        if (addFirst) {
            addView(textView,0)
        } else {
            addView(textView)
        }
    }




    fun setOnClickListener(block:(String)->Unit){
        onClickAction=block
    }


    private fun dip2px(value:Float):Float{
        val density = context.resources.displayMetrics.density
        return (value * density+0.5f)
    }


}