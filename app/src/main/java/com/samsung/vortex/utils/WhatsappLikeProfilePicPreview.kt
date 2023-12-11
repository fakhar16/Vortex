package com.samsung.vortex.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import com.samsung.vortex.R
import com.squareup.picasso.Picasso
import java.io.File

class WhatsappLikeProfilePicPreview{

    companion object {
        private var currentAnimator: Animator? = null
        private var shortAnimationDuration: Int = 200
        private lateinit var startBounds: RectF
        private  var startScale: Float = 0.0f
        private lateinit var mExpandedImageCardView: CardView
        @SuppressLint("StaticFieldLeak")
        private lateinit var mExpandedVideoCardView: FrameLayout
        private lateinit var mExpandedAudioView: RelativeLayout
        @SuppressLint("StaticFieldLeak")
        private lateinit var mThumbView: View


        fun zoomImageFromThumb(thumbView: View, expandedImageCardView: CardView, expandedImageView: ImageView, container: View, imageUrl: File) {
            mExpandedImageCardView = expandedImageCardView
            mThumbView = thumbView

            currentAnimator?.cancel()
            Picasso.get().load(imageUrl).placeholder(R.drawable.profile_image).into(expandedImageView)

            val startBoundsInt = Rect()
            val finalBoundsInt = Rect()
            val globalOffset = Point()
            thumbView.getGlobalVisibleRect(startBoundsInt)

            container.getGlobalVisibleRect(finalBoundsInt, globalOffset)
            startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
            finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

            startBounds = RectF(startBoundsInt)
            val finalBounds = RectF(finalBoundsInt)

            if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
                startScale = startBounds.height() / finalBounds.height()
                val startWidth: Float = startScale * finalBounds.width()
                val deltaWidth: Float = (startWidth - startBounds.width()) / 2
                startBounds.left -= deltaWidth.toInt()
                startBounds.right += deltaWidth.toInt()
            } else {
                startScale = startBounds.width() / finalBounds.width()
                val startHeight: Float = startScale * finalBounds.height()
                val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
                startBounds.top -= deltaHeight.toInt()
                startBounds.bottom += deltaHeight.toInt()
            }

            expandedImageCardView.visibility = View.VISIBLE

            expandedImageCardView.pivotX = 0f
            expandedImageCardView.pivotY = 0f

            currentAnimator = AnimatorSet().apply {
                play(
                    ObjectAnimator.ofFloat(
                        expandedImageCardView,
                        View.X,
                        startBounds.left,
                        finalBounds.left)
                ).apply {
                    with(ObjectAnimator.ofFloat(expandedImageCardView, View.Y, startBounds.top, finalBounds.top))
                    with(ObjectAnimator.ofFloat(expandedImageCardView, View.SCALE_X, startScale, 1f))
                    with(ObjectAnimator.ofFloat(expandedImageCardView, View.SCALE_Y, startScale, 1f))
                }
                duration = shortAnimationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        currentAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        currentAnimator = null
                    }
                })
                start()
            }
        }

        fun dismissPhotoPreview() {
                currentAnimator?.cancel()

                currentAnimator = AnimatorSet().apply {
                    play(ObjectAnimator.ofFloat(mExpandedImageCardView, View.X, startBounds.left)).apply {
                        with(ObjectAnimator.ofFloat(mExpandedImageCardView, View.Y, startBounds.top))
                        with(ObjectAnimator.ofFloat(mExpandedImageCardView, View.SCALE_X, startScale))
                        with(ObjectAnimator.ofFloat(mExpandedImageCardView, View.SCALE_Y, startScale))
                    }
                    duration = shortAnimationDuration.toLong()
                    interpolator = DecelerateInterpolator()
                    addListener(object : AnimatorListenerAdapter() {

                        override fun onAnimationEnd(animation: Animator) {
                            mThumbView.alpha = 1f
                            mExpandedImageCardView.visibility = View.GONE
                            currentAnimator = null
                        }

                        override fun onAnimationCancel(animation: Animator) {
                            mThumbView.alpha = 1f
                            mExpandedImageCardView.visibility = View.GONE
                            currentAnimator = null
                        }
                    })
                    start()
                }
        }

        fun zoomVideoFromThumb(thumbView: View, expandVideoCardView: FrameLayout, container: View) {
            mExpandedVideoCardView = expandVideoCardView
            mThumbView = thumbView

            currentAnimator?.cancel()

            //todo Show video here

            val startBoundsInt = Rect()
            val finalBoundsInt = Rect()
            val globalOffset = Point()
            thumbView.getGlobalVisibleRect(startBoundsInt)

            container.getGlobalVisibleRect(finalBoundsInt, globalOffset)
            startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
            finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

            startBounds = RectF(startBoundsInt)
            val finalBounds = RectF(finalBoundsInt)

//            val startScale: Float
            if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
                startScale = startBounds.height() / finalBounds.height()
                val startWidth: Float = startScale * finalBounds.width()
                val deltaWidth: Float = (startWidth - startBounds.width()) / 2
                startBounds.left -= deltaWidth.toInt()
                startBounds.right += deltaWidth.toInt()
            } else {
                startScale = startBounds.width() / finalBounds.width()
                val startHeight: Float = startScale * finalBounds.height()
                val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
                startBounds.top -= deltaHeight.toInt()
                startBounds.bottom += deltaHeight.toInt()
            }

            expandVideoCardView.visibility = View.VISIBLE

            expandVideoCardView.pivotX = 0f
            expandVideoCardView.pivotY = 0f

            currentAnimator = AnimatorSet().apply {
                play(
                    ObjectAnimator.ofFloat(
                        expandVideoCardView,
                        View.X,
                        startBounds.left,
                        finalBounds.left)
                ).apply {
                    with(ObjectAnimator.ofFloat(expandVideoCardView, View.Y, startBounds.top, finalBounds.top))
                    with(ObjectAnimator.ofFloat(expandVideoCardView, View.SCALE_X, startScale, 1f))
                    with(ObjectAnimator.ofFloat(expandVideoCardView, View.SCALE_Y, startScale, 1f))
                }
                duration = shortAnimationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        currentAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        currentAnimator = null
                    }
                })
                start()
            }
        }

        fun dismissVideoPreview() {
            currentAnimator?.cancel()

            currentAnimator = AnimatorSet().apply {
                play(ObjectAnimator.ofFloat(mExpandedVideoCardView, View.X, startBounds.left)).apply {
                    with(ObjectAnimator.ofFloat(mExpandedVideoCardView, View.Y, startBounds.top))
                    with(ObjectAnimator.ofFloat(mExpandedVideoCardView, View.SCALE_X, startScale))
                    with(ObjectAnimator.ofFloat(mExpandedVideoCardView, View.SCALE_Y, startScale))
                }
                duration = shortAnimationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        mThumbView.alpha = 1f
                        mExpandedVideoCardView.visibility = View.GONE
                        currentAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        mThumbView.alpha = 1f
                        mExpandedVideoCardView.visibility = View.GONE
                        currentAnimator = null
                    }
                })
                start()
            }
        }

        fun zoomAudioFromThumb(thumbView: View, expandedAudioView: RelativeLayout, container: View) {
            mExpandedAudioView = expandedAudioView
            mThumbView = thumbView

            currentAnimator?.cancel()

            //todo Show video here

            val startBoundsInt = Rect()
            val finalBoundsInt = Rect()
            val globalOffset = Point()
            thumbView.getGlobalVisibleRect(startBoundsInt)

            container.getGlobalVisibleRect(finalBoundsInt, globalOffset)
            startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
            finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

            startBounds = RectF(startBoundsInt)
            val finalBounds = RectF(finalBoundsInt)

//            val startScale: Float
            if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
                startScale = startBounds.height() / finalBounds.height()
                val startWidth: Float = startScale * finalBounds.width()
                val deltaWidth: Float = (startWidth - startBounds.width()) / 2
                startBounds.left -= deltaWidth.toInt()
                startBounds.right += deltaWidth.toInt()
            } else {
                startScale = startBounds.width() / finalBounds.width()
                val startHeight: Float = startScale * finalBounds.height()
                val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
                startBounds.top -= deltaHeight.toInt()
                startBounds.bottom += deltaHeight.toInt()
            }

            expandedAudioView.visibility = View.VISIBLE

            expandedAudioView.pivotX = 0f
            expandedAudioView.pivotY = 0f

            currentAnimator = AnimatorSet().apply {
                play(
                    ObjectAnimator.ofFloat(
                        expandedAudioView,
                        View.X,
                        startBounds.left,
                        finalBounds.left)
                ).apply {
                    with(ObjectAnimator.ofFloat(expandedAudioView, View.Y, startBounds.top, finalBounds.top))
                    with(ObjectAnimator.ofFloat(expandedAudioView, View.SCALE_X, startScale, 1f))
                    with(ObjectAnimator.ofFloat(expandedAudioView, View.SCALE_Y, startScale, 1f))
                }
                duration = shortAnimationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        currentAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        currentAnimator = null
                    }
                })
                start()
            }
        }

        fun dismissAudioPreview() {
            currentAnimator?.cancel()

            currentAnimator = AnimatorSet().apply {
                play(ObjectAnimator.ofFloat(mExpandedAudioView, View.X, startBounds.left)).apply {
                    with(ObjectAnimator.ofFloat(mExpandedAudioView, View.Y, startBounds.top))
                    with(ObjectAnimator.ofFloat(mExpandedAudioView, View.SCALE_X, startScale))
                    with(ObjectAnimator.ofFloat(mExpandedAudioView, View.SCALE_Y, startScale))
                }
                duration = shortAnimationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        mThumbView.alpha = 1f
                        mExpandedAudioView.visibility = View.GONE
                        currentAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        mThumbView.alpha = 1f
                        mExpandedAudioView.visibility = View.GONE
                        currentAnimator = null
                    }
                })
                start()
            }
        }
    }
}
